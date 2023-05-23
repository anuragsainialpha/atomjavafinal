package com.api.apollo.atom.service.impl;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.Constants.DispatchPlanItemAppStatus;
import com.api.apollo.atom.constant.Constants.ErrorCode;
import com.api.apollo.atom.constant.Constants.Status;
import com.api.apollo.atom.constant.LocationType;
import com.api.apollo.atom.constant.UserRole;
import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.planner.*;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.master.*;
import com.api.apollo.atom.entity.plan.DispatchPlan;
import com.api.apollo.atom.entity.plan.DispatchPlanItemErrorInfo;
import com.api.apollo.atom.entity.plan.DispatchPlanItemInfo;
import com.api.apollo.atom.entity.plan.DispatchPlanItemTempInfo;
import com.api.apollo.atom.repository.UserRepository;
import com.api.apollo.atom.repository.master.*;
import com.api.apollo.atom.repository.ops.LoadslipDetailRepository;
import com.api.apollo.atom.repository.planner.DispatchPlanItemErrorInfoRepository;
import com.api.apollo.atom.repository.planner.DispatchPlanItemInfoRepository;
import com.api.apollo.atom.repository.planner.DispatchPlanItemTempInfoRepository;
import com.api.apollo.atom.repository.planner.DispatchPlanRepository;
import com.api.apollo.atom.service.FilterService;
import com.api.apollo.atom.service.PlanResolveErrorService;
import com.api.apollo.atom.service.PlannerService;
import com.api.apollo.atom.service.UtilityService;
import com.api.apollo.atom.util.DateUtils;
import com.api.apollo.atom.util.Utility;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Clob;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class PlannerServiceImpl implements PlannerService {

  @Autowired
  private LocationRepository locationRepo;

  @Autowired
  private ItemRepository itemRepo;

  @Autowired
  private BatchCodeRepository batchCodeRepo;

  @Autowired
  private DispatchPlanRepository dispatchPlanRepo;

  @Autowired
  private DispatchPlanItemTempInfoRepository dispatchPlanItemTempInfoRepo;

  @Autowired
  private DispatchPlanItemInfoRepository dispatchPlanItemInfoRepo;

  @Autowired
  private DispatchPlanItemErrorInfoRepository dispatchPlanItemErrorInfoRepo;

  @Autowired
  private PlanResolveErrorService planResolveErrorService;

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  FilterService filterService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UtilityService utilityService;

  @Autowired
  private LoadslipDetailRepository loadslipDetailRepository;

  @Autowired
  private TruckTypeInfoRepository truckTypeInfoRepository;

  @Autowired
  private MTPlantBatchRepository mtPlantBatchRepository;

  private TaskExecutor executor = new ConcurrentTaskExecutor(Executors.newFixedThreadPool(500));

  private List<String> validatePlanRecord(PlanUploadDto itemInfo, ApplicationUser loggedInUser, int row) {
   // String materialDesc = itemRepo.findDescriptionById(itemInfo.getMaterialCode());
    List<String> errorMessages = new ArrayList<>();
    String validBatchCode = null;
    if (StringUtils.isEmpty(itemInfo.dispatchDate))
      errorMessages.add("Dispatch date is missing at row " + row);
    if (itemInfo.dispatchDate != null) {
      /*if (!(DateUtils.isDateValid(itemInfo.dispatchDate, Constants.PLAN_RECORD_DATE_FORMAT))) {
        errorMessages.add("Please enter valid dispatch date,it should be in DD/MM/YYYY format at row " + row);
      } else {
        Date dispatchDate = DateUtils.formatDate(itemInfo.getDispatchDate(), Constants.PLAN_RECORD_DATE_FORMAT);
        Date today = DateUtils.atStartOfDay(new Date());
        if (dispatchDate.before(today)) {
          errorMessages.add(String.format("Given dispatch date %s should not before current date at row %d", itemInfo.getDispatchDate(), row));
        }
      }*/
      if(DateUtils.constantDateFormat(itemInfo.getDispatchDate()) == null){
        errorMessages.add("Please enter valid dispatch date,it should be in DD/MM/YYYY or DD-MM-YYYY format at row " + row);
      }else {
        if (DateUtils.constantDateFormat(itemInfo.getDispatchDate()).before(DateUtils.atStartOfDay(new Date()))){
          errorMessages.add(String.format("Given dispatch date %s should not before current date at row %d", itemInfo.getDispatchDate(), row));
        }
      }
    }
    if (StringUtils.isEmpty(itemInfo.sourceLocation))
      errorMessages.add("Source location is missing at row " + row);
    if (StringUtils.isEmpty(itemInfo.destinationLocation))
      errorMessages.add("Destination location is missing at row " + row);
    if (StringUtils.isEmpty(itemInfo.materialCode))
      errorMessages.add("Material code is missing at row " + row);
    /*if (!StringUtils.isEmpty(itemInfo.getMaterialDescription()) && !itemInfo.materialDescription.equals(materialDesc))
      errorMessages.add("Material description is not matching with existing materialCode description at row " + row);*/
    if (StringUtils.isEmpty(itemInfo.batchCode))
      errorMessages.add("Batch code is missing at row " + row);
    if (!StringUtils.isEmpty(itemInfo.marketingSegment) && itemInfo.marketingSegment.trim().length() > 10)
      errorMessages.add("Marketing segment length should be less than 10 characters at row " + row);
    if (StringUtils.isEmpty(itemInfo.quantity))
      errorMessages.add("Quantity is missing at row " + row);
   /* if (StringUtils.isEmpty(itemInfo.priority))
      errorMessages.add("Priority is missing at row " + row);*/
    if (!Utility.isNumeric(itemInfo.quantity) || Integer.parseInt(itemInfo.quantity) <= 0)
      errorMessages.add("Please enter valid quantity at row " + row);
    if (!StringUtils.isEmpty(itemInfo.priority)) {
      if (!Utility.isNumeric(itemInfo.priority))
        errorMessages.add("Please enter valid priority at row " + row);
    }
    if (!UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole()) && !StringUtils.isEmpty(itemInfo.sourceLocation)
        && !(itemInfo.sourceLocation.equalsIgnoreCase(loggedInUser.getPlantCode()))) {
      errorMessages.add("Source location should be " + loggedInUser.getPlantCode() + " at row " + row);
    }
    if (itemInfo.sourceLocation.equals(itemInfo.getDestinationLocation()))
      errorMessages.add("Destination location should not be Source location at row " + row);

    /*If uncommented then get the batch code prefixed from MTPlantBatch table*/
  /*  if (!StringUtils.isEmpty(itemInfo.batchCode)) {
      //batch code validation with plant codes
//    Map<String, String> batchCodePrefixForSource = Utility.getBatchCodePrefixForPlant();
      Map<String, Object> batchCodePrefixForSource = batchCodePrefixList.parallelStream().filter(map -> map.get("locationId").equals(itemInfo.getSourceLocation())).findFirst().get();
      //Checking if item classification is TYRE
      *//*If batchCodePrefixForSource is EMPTY then no need to check the prefix for that LOC
      * Since Batch code prefix is checked only for sourceLoc = 1001, 1002,1004, 1007 and EXT_WAREHOUSES(TNR2,TNR4,TNR5,TNR6,TNR7) *//*
      if (!batchCodePrefixForSource.isEmpty() && itemRepo.existsByIdAndClassification(itemInfo.getMaterialCode(), "TYRE")) {
        *//*The first two charecters of Item-Batch code should be equal to the PREFIX of the PLANT LOC*//*
        if (!itemInfo.batchCode.startsWith(batchCodePrefixForSource.get("batchCodePrefix").toString())) {
          errorMessages.add("Batch code: " + itemInfo.batchCode + " is not valid for " + itemInfo.getSourceLocation() + " at row number " + row);
        }
      }
    }*/
    return errorMessages;
  }

  public List<String> validateDispatchPlan(DispatchPlanDto plan, ApplicationUser loggedInUser) throws Exception {
    List<PlanUploadDto> planItems = plan.planItems;
    String userPlantCode = loggedInUser.getPlantCode();
    List<CompletableFuture<List<String>>> pendingFutures = new ArrayList<>();
    for (int i = 0; i < planItems.size(); i++) {
      PlanUploadDto record = planItems.get(i);
      int index = i;
      pendingFutures.add(CompletableFuture.supplyAsync(() -> {
        return this.validatePlanRecord(record, loggedInUser, index + 2);
      }, executor));
    }
    return Utility.joinAll(pendingFutures).get().parallelStream().flatMap(List::stream)
        .collect(Collectors.toList());

  }

  private DispatchPlanResultDto callDispatchPlanProcedure(DispatchPlanDto planDto, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("atl_business_flow_pkg.upload_dispatch_plan");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.registerStoredProcedureParameter("p_tot_error_records", Integer.class, ParameterMode.OUT);
    storedProcedure.registerStoredProcedureParameter("p_total_tyre_count", Integer.class, ParameterMode.OUT);
    storedProcedure.registerStoredProcedureParameter("p_c1_count", Integer.class, ParameterMode.OUT);
    storedProcedure.registerStoredProcedureParameter("p_c2_count", Integer.class, ParameterMode.OUT);
    storedProcedure.registerStoredProcedureParameter("p_c3_count", Integer.class, ParameterMode.OUT);
    storedProcedure.registerStoredProcedureParameter("p_c4_count", Integer.class, ParameterMode.OUT);
    storedProcedure.registerStoredProcedureParameter("p_c5_count", Integer.class, ParameterMode.OUT);
    storedProcedure.registerStoredProcedureParameter("p_c6_count", Integer.class, ParameterMode.OUT);
    storedProcedure.registerStoredProcedureParameter("p_plan_id", Long.class, ParameterMode.OUT);
    storedProcedure.registerStoredProcedureParameter("p_plan_status", String.class, ParameterMode.OUT);

    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(planDto)));
    storedProcedure.setParameter("p_root_element", "planItems");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    // System.out.println(new Gson().toJson(storedProcedure.getSingleResult()));
    return new DispatchPlanResultDto(storedProcedure);
  }

  /**
   * <b>Uploading the plan using excel/Creating Manual Plan </b>
   * @param planDto - Required Dispatch plan dto for upload or create the plan {@link DispatchPlanDto}
   * @param loggedInUser - Logged In User
   * @return - Success/Error message along with the error lines and error code count
   * @throws Exception
   */
  @Override
  public ApiResponse uploadDispatchPlan(DispatchPlanDto planDto, ApplicationUser loggedInUser) throws Exception {
    if (planDto.planItems.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please upload a valid document")));
    List<String> errorMessages = this.validateDispatchPlan(planDto, loggedInUser);
    if (errorMessages.size() > 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", errorMessages);
    return new ApiResponse(HttpStatus.OK, "Plan created", this.callDispatchPlanProcedure(planDto, loggedInUser));
  }

  /**
   * <b>Fetching the error info of the plan</b>
   * @param planId - required dispatch plan id
   * @param errorCode - required error code for that plan
   * @param index - starting index of the page
   * @param pageLength - no of rows in the page
   * @return - list of plan item with the given error code
   * @throws Exception
   */
  @Override
  public ApiResponse getErrorPlanInfo(Long planId, String errorCode, int index, int pageLength) {
    // Removed pagination and showing all records
    /*return new ApiResponse(HttpStatus.OK, "",
        dispatchPlanItemTempInfoRepo
            .findByPlanIdAndErrorCode(planId, ErrorCode.valueOf(errorCode), PageRequest.of(index, pageLength)).parallelStream()
            .map(DispatchPlanTempInfoDto::new).collect(Collectors.toList()));*/
    return new ApiResponse(HttpStatus.OK, "",
        dispatchPlanItemTempInfoRepo
            .findByPlanIdAndErrorCode(planId, ErrorCode.valueOf(errorCode)).parallelStream()
            .map(DispatchPlanTempInfoDto::new).collect(Collectors.toList()));
  }

  /**
   * <b>Getting the count of each error code for a plan</b>
   * @param planId - required dispatch plan id
   * @param loggedInUser - Logged in user
   * @return - count of each error code of the plan
   * @throws java.nio.file.AccessDeniedException
   */
  @Override
  public ApiResponse getDispatchPlanErrorInfoCount(Long planId, ApplicationUser loggedInUser)
      throws AccessDeniedException {
    if (!UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
      if (!(dispatchPlanRepo.existsByIdAndInsertUser(planId, loggedInUser.getUserId())))
        return new ApiResponse(HttpStatus.OK, "Plan: " + planId + " deleted successfully");
    } else {
      if (!(dispatchPlanRepo.existsById(planId)))
        return new ApiResponse(HttpStatus.OK, "Plan: " + planId + " deleted successfully");
    }
    Object[] result = (Object[]) dispatchPlanItemTempInfoRepo.findDispatchPlanResultCount(planId);
    DispatchPlanResultDto resultDto = new DispatchPlanResultDto(((BigDecimal) result[0]).intValue(),
        ((BigDecimal) result[1]).intValue(), ((BigDecimal) result[2]).intValue(),
        ((BigDecimal) result[3]).intValue(), ((BigDecimal) result[4]).intValue(),
        ((BigDecimal) result[5]).intValue(), ((BigDecimal) result[6]).intValue(),
        ((BigDecimal) result[7]).intValue(), ((BigDecimal) result[8]).intValue(), ((String) result[9]), planId);

    return new ApiResponse(HttpStatus.OK, "Plan created", resultDto);
  }

  /**
   * <b>Fetching the list of dispatch plan created with error</b>
   * @param searchDto - required filter criteria {@link DispatchPlanFilterDto}
   * @param loggedInUser - Logged in user
   * @return - List of Dispatch plans which are created with errors
   */
  @Override
  public ApiResponse getUserPendingDispatchPlans(ApplicationUser loggedInUser, DispatchPlanFilterDto filterDto) {
    Page<DispatchPlan> plans = null;
    if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
      plans = dispatchPlanRepo.findByStatus(Status.ERROR, PageRequest.of(filterDto.getIndex(), filterDto.getPageLength(), Sort.Direction.DESC, "insertDate"));
    } else {
      plans = dispatchPlanRepo.findByInsertUserAndStatus(loggedInUser.getUserId(),
          Status.ERROR, PageRequest.of(filterDto.getIndex(), filterDto.getPageLength(), Sort.Direction.DESC, "insertDate"));
    }
    List<DispatchPlanDto> planDto = plans.stream().parallel().map(DispatchPlanDto::new)
        .collect(Collectors.toList());
    filterDto.setTotal(plans.getTotalElements());
    filterDto.setPlans(planDto);
    return new ApiResponse(HttpStatus.OK, "", filterDto);
  }

  /**
   * <b>Getting the dispatch plan details</b>
   * @param searchDto - required filter criteria {@link DispatchPlanFilterDto}
   * @param loggedInUser - Logged in user
   * @return - list of dispatch plans which are created with out any errors
   */
  @Override
  public ApiResponse getDispatchPlanInfo(DispatchPlanFilterDto filterDto, ApplicationUser loggedInUser) {
    Page<DispatchPlanItemInfo> dispatchPlanItemInfos;
    DispatchPlanFilterDto dispatchPlanFilterDto = null;
    Optional<MTLocation> mtLocation = locationRepo.findByLocationId(loggedInUser.getPlantCode());
    List<String> extWarehouseLocs = utilityService.getLinkedExtWareHouse("1007");
    if (!filterDto.isSearchFilter()) {
      // LoggedIn User belongs to FGS Planner or Operations role, then get all Plans for that Plant(Plant specific Plans)
      /*if (loggedInUser.getRole().isFGSUserRole()) {*/
      Page<Map<String, Object>> dispatchPlanMap = null;
        List<String> appStatusList = new ArrayList<>();
        if (filterDto.getStatuses() == null || filterDto.getStatuses().size() == 0){
          appStatusList = DispatchPlanItemAppStatus.getFGSPlannerStatus().parallelStream().map(status-> status.name()).collect(Collectors.toList());
        }else {
          appStatusList = filterDto.getStatuses().parallelStream().map(status-> status.name()).collect(Collectors.toList());
        }

      if (mtLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equalsIgnoreCase(mtLocation.get().getLocationClass()) && filterDto.getIsViewPlans()) {
        List<String> sourceList = new ArrayList<>();
        sourceList.add(loggedInUser.getPlantCode());
        sourceList.add("1007"); // mtLocation.get().getLinkedPlant() == 1007, for now 1007 plant only having external warehouses
        dispatchPlanMap = dispatchPlanItemInfoRepo.findAllPlansBySourceLocationInAndAppStatusInEXT(sourceList, appStatusList, extWarehouseLocs,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()));

      } else {
        /*DP_REP, L1_MGR, L2_MGR*/
        if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {

          dispatchPlanMap = dispatchPlanItemInfoRepo.findAllPlansByAppStatusInDPREP(appStatusList, PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()));
        } else {
          List<String> userIds = userRepository.findUserIdByPlantCode(loggedInUser.getPlantCode()).parallelStream().map(ApplicationUser::getUserId).collect(Collectors.toList());
          dispatchPlanMap = dispatchPlanItemInfoRepo.findAllPlansByAppStatusInAndSourceLocationNRM(appStatusList, loggedInUser.getPlantCode(),
              PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()));
        }
      }
      dispatchPlanItemInfos = new PageImpl<>(dispatchPlanMap.stream().parallel().map(DispatchPlanItemInfo::new).collect(Collectors.toList()),
          PageRequest.of(filterDto.getIndex(), filterDto.getPageLength(), Sort.Direction.DESC, "dispatch_date"), dispatchPlanMap.getTotalElements());
      /*Removing data when source = 1007 and dest_loc = EXT_WAREHOUSE*/
      //Commented This because now checking the condition in the query itself
//      if (mtLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equalsIgnoreCase(mtLocation.get().getLocationClass()) && filterDto.getIsViewPlans()) {
//        dispatchPlanItemInfos = new PageImpl<>(dispatchPlanItemInfos.stream().parallel().filter(dp -> !(dp.getSourceLocation().equals("1007") && extWarehouseLocs.contains(dp.getDestinationLocation())))
//            .collect(Collectors.toList()), PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), dispatchPlanItemInfos.getTotalElements());
//      }
      /*Getting STANDARD_FTL truck Details*/
      MTTruckTypeInfo mtTruckTypeInfo = truckTypeInfoRepository.findOneByType("STANDARD_FTL").parallelStream().filter(mt -> StringUtils.isEmpty(mt.getVariant1())).findFirst().get();
      dispatchPlanFilterDto = new DispatchPlanFilterDto(dispatchPlanItemInfos, mtTruckTypeInfo, loggedInUser);

    } else {
      // Otherwise get Only current LoggedIn user created Plans
      //dispatchPlanItemInfos = filterService.filterDispatchPlanItems(filterDto, loggedInUser, mtLocation);
      dispatchPlanFilterDto = filterService.filterDisplanPlanInfo(filterDto, loggedInUser, mtLocation);
    }
    return new ApiResponse(HttpStatus.OK, "",dispatchPlanFilterDto);
  }

  /**
   * <b>Resolving the error</b>
   * @param filterDto - required changed made in the dispatch plan
   * @param loggedInUser - logged in user
   * @return - remaining error count after resolving a perticular error
   */
  @Override
  public ApiResponse resolvePlanErrors(DispatchPlanFilterDto filterDto, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(filterDto.getErrorCode()))
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("Error code is missing"));
    if (Constants.ErrorCode.valueOf(filterDto.getErrorCode()) == Constants.ErrorCode.C1)
      return planResolveErrorService.resolveC1(filterDto, loggedInUser);
    if (Constants.ErrorCode.valueOf(filterDto.getErrorCode()) == Constants.ErrorCode.C2)
      return planResolveErrorService.resolveC2(filterDto, loggedInUser);
    if (Constants.ErrorCode.valueOf(filterDto.getErrorCode()) == Constants.ErrorCode.C3)
      return planResolveErrorService.resolveC3(filterDto, loggedInUser);
    if (Constants.ErrorCode.valueOf(filterDto.getErrorCode()) == Constants.ErrorCode.C4)
      return planResolveErrorService.resolveC4(filterDto, loggedInUser);
    if (Constants.ErrorCode.valueOf(filterDto.getErrorCode()) == Constants.ErrorCode.C5)
      return planResolveErrorService.resolveC5(filterDto, loggedInUser);
    if (Constants.ErrorCode.valueOf(filterDto.getErrorCode()) == Constants.ErrorCode.C6)
      return planResolveErrorService.resolveC6(filterDto, loggedInUser);
    return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("Invalid Error code"));
  }

  /**
   * <b>Approve/Unapprove the dispatch plan</b>
   * @param searchDto - required quantity to be approved or unapproved
   * @param loggedInUser - logged in user
   * @return - updated dispatch plan details
   */
  @Override
  public ApiResponse approveDispatchPlanInfo(DispatchPlanFilterDto filterDto, ApplicationUser loggedInUser) {
    List<DispatchPlanItemDto> planItemDtos = filterDto.getPlanItems();
//    List<String> errorMessages = planItemDtos.stream().parallel()
//        .map(planItem -> planItem.validateApprovalQuantity()).filter(error -> error != null)
//        .collect(Collectors.toList());
    List<String> errorMessages = planItemDtos.stream().parallel()
        .map(DispatchPlanItemDto::validateApprovalQuantity).filter(Objects::nonNull)
        .collect(Collectors.toList());
    if (errorMessages.size() > 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", errorMessages);
    //replaced with method refference
    List<DispatchPlanItemInfo> dispatchPlanItems = dispatchPlanItemInfoRepo.findAllByIdIn(filterDto.getPlanItems()
        .stream().parallel().map(DispatchPlanItemDto::getId).collect(Collectors.toList()));

    List<String> errorForConcurrentUser = new ArrayList<>();
    for (DispatchPlanItemInfo dispatchPlanItemInfo : dispatchPlanItems) {
      /*Here we were checking with plant code.*/
      /* if (!UserRole.DP_REP.equals(applicationUser.getRole()) && !applicationUser.getPlantCode().equals(dispatchPlanItemDto.getSourceLocation())) {
        return new ApiResponse(HttpStatus.NOT_FOUND, "You are not allowed to modify the plan: " + dispatchPlanItemDto.getPlanId());
      }*/

      /*Now we need to check with insertUserId. A plan can be modified only by the user who created it*/
      if (!loggedInUser.getUserId().equals(dispatchPlanItemInfo.getInsertUser())) {
        errorMessages.add("You are not allowed to approve the plan: " + dispatchPlanItemInfo.getDispatchPlan().getId() + ". The logged in user is not same as the dispatch plan insert user.");
        return new ApiResponse(HttpStatus.NOT_FOUND, "", errorMessages);
      }
      Optional<DispatchPlanItemDto> optionalPlanItemDto = planItemDtos.parallelStream()
          .filter(item -> item.getId().equals(dispatchPlanItemInfo.getId())).findFirst();
      if (optionalPlanItemDto.isPresent()) {
        DispatchPlanItemDto planItemDto = optionalPlanItemDto.get();
        int quantity = dispatchPlanItemInfo.getQuantity();
        int approvedQty = dispatchPlanItemInfo.getApprovedQuantity() == null ? 0
            : dispatchPlanItemInfo.getApprovedQuantity();
        //int deletedQuantity = dispatchPlanItemInfo.getDeletedQuantity();
        //int availableQuantity = dispatchPlanItemInfo.getAvailableQuantity();
        //int dispatchedQuantity = dispatchPlanItemInfo.getDispatchedQuantity();


        /*Validation when multiple user trying to approve same plan items*/
        if ( planItemDto.getApprovalQuantity() > 0) {
          if (quantity > 0 && planItemDto.getApprovalQuantity() > quantity) {
            errorForConcurrentUser.add("Approval Qty Cannot Be Greater Than Planned Qty! For The Plan: " + dispatchPlanItemInfo.getDispatchPlan().getId());
            break;
          }
          if (dispatchPlanItemInfo.getApprovedQuantity() > 0) {
            if (quantity == approvedQty) {
              errorForConcurrentUser.add("The plan: " + dispatchPlanItemInfo.getDispatchPlan().getId() + " is already approved.");
              break;
            }
          }
          /*if(planItemDto.getApprovalQuantity() > (dispatchPlanItemInfo.getTotalAvailableQuantity() - (dispatchPlanItemInfo.getApprovedQuantity() - dispatchPlanItemInfo.getDeletedQuantity()))){*/
          if (planItemDto.getApprovalQuantity() > (dispatchPlanItemInfo.getUnapprovedQuantity())) {
            errorForConcurrentUser.add("Invalid approved quantity for plan id " + dispatchPlanItemInfo.getDispatchPlan().getId() + " at line no: " + dispatchPlanItemInfo.getLineNumber());
            break;
          }
        }

        /*Validation When multiple user are trying to Unapprove some qty on the same plan items */
        if (planItemDto.getUnapprovalQuantity() > 0) {
          //if (planItemDto.getUnapprovalQuantity() > (dispatchPlanItemInfo.getApprovedQuantity() - dispatchPlanItemInfo.getDeletedQuantity() - dispatchPlanItemInfo.getReservedQuantity()))
          if (planItemDto.getUnapprovalQuantity() > (dispatchPlanItemInfo.getAvailableQuantity())) {
            errorForConcurrentUser.add("Qty to be unApproved should be less than or equal to Approved quantity !");
            break;
          }
        }

        if (planItemDto.getApprovalQuantity() != 0) {
          dispatchPlanItemInfo.setApprovedQuantity(approvedQty + planItemDto.getApprovalQuantity());
          dispatchPlanItemInfo.setUnapprovedQuantity(quantity - (dispatchPlanItemInfo.getApprovedQuantity() + dispatchPlanItemInfo.getUnapprovedDeletedQuantity()));
        } else {
          dispatchPlanItemInfo.setApprovedQuantity(approvedQty - planItemDto.getUnapprovalQuantity());
          dispatchPlanItemInfo.setUnapprovedQuantity(dispatchPlanItemInfo.getUnapprovedQuantity() + planItemDto.getUnapprovalQuantity());
        }
        dispatchPlanItemInfo.setAvailableQuantity(dispatchPlanItemInfo.getApprovedQuantity() - (dispatchPlanItemInfo.getDeletedQuantity() +
            dispatchPlanItemInfo.getReservedQuantity() + dispatchPlanItemInfo.getDispatchedQuantity() + (dispatchPlanItemInfo.getLoadedQty() != null ? dispatchPlanItemInfo.getLoadedQty() : 0)));
        dispatchPlanItemInfo.setTotalAvailableQuantity(dispatchPlanItemInfo.getAvailableQuantity() + dispatchPlanItemInfo.getUnapprovedQuantity());
        if (dispatchPlanItemInfo.getApprovedQuantity().intValue() == dispatchPlanItemInfo.getQuantity().intValue()) {
          dispatchPlanItemInfo.setAppStatus(DispatchPlanItemAppStatus.APPROVED);
        } else {
          if (dispatchPlanItemInfo.getApprovedQuantity() > 0) {
            dispatchPlanItemInfo.setAppStatus(DispatchPlanItemAppStatus.APPROVED_PART);
          } else if (dispatchPlanItemInfo.getApprovedQuantity() == 0) {
            dispatchPlanItemInfo.setAppStatus(DispatchPlanItemAppStatus.APPROVAL_PENDING);
          }
        }
      }
      /*Setting last modified user details*/
      dispatchPlanItemInfo.setUpdateDate(new Date());
      dispatchPlanItemInfo.setUpdateUser(loggedInUser.getUserId());

      /*Set Status as completed when total available quantity = 0 & loaded_qty = 0 & reserved_qty = 0(When plan item used in other loadslips,
      the qty will be in loaded/reserved qty till the second LS is Confirmed) & Status is OPEN & AppStatus is APPROVED or APPROVED-PART*/
      if (Status.OPEN.equals(dispatchPlanItemInfo.getStatus()) && dispatchPlanItemInfo.getTotalAvailableQuantity() == 0
          && dispatchPlanItemInfo.getReservedQuantity() == 0 && dispatchPlanItemInfo.getLoadedQty() == 0
          && (DispatchPlanItemAppStatus.APPROVED.equals(dispatchPlanItemInfo.getAppStatus()) || DispatchPlanItemAppStatus.APPROVED_PART.equals(dispatchPlanItemInfo.getAppStatus()))) {
        dispatchPlanItemInfo.setStatus(Status.COMPLETED);
      } else {
        dispatchPlanItemInfo.setStatus(Status.OPEN);
      }

    }
    if (!StringUtils.isEmpty(errorForConcurrentUser) && errorForConcurrentUser.size() > 0) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "", errorForConcurrentUser);
    }
    dispatchPlanItemInfoRepo.saveAll(dispatchPlanItems);
    /*Getting STANDARD_FTL truck Details*/
    MTTruckTypeInfo mtTruckTypeInfo = truckTypeInfoRepository.findOneByType("STANDARD_FTL").parallelStream().filter(mt -> StringUtils.isEmpty(mt.getVariant1())).findFirst().get();

    DispatchPlanFilterDto dispatchPlanFilterDto= new DispatchPlanFilterDto(dispatchPlanItems, mtTruckTypeInfo, loggedInUser);

    return new ApiResponse(HttpStatus.OK, "Plan records approved successfully", dispatchPlanFilterDto);
  }

  /**
   * <b>Deleting the dispatch plan details</b>
   * @param ids - ids of the plans to be deleted
   * @param applicationUser - logged in user
   * @return - success/error message
   */
  @Override
  public ApiResponse deleteDispatchPlanInfo(List<Long> ids, ApplicationUser applicationUser) {

    List<String> errorMessgages = new ArrayList<>();
    if (ids == null || ids.isEmpty()) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Selected plans may contain Reserved or Dispatched or Loaded Quantity.");
    }
    List<DispatchPlanItemInfo> dispatchPlanItemInfoList = dispatchPlanItemInfoRepo.findAllByIdIn(ids);
    if (dispatchPlanItemInfoList != null && dispatchPlanItemInfoList.size() > 0) {
      for (DispatchPlanItemInfo dispatchPlanItemInfo : dispatchPlanItemInfoList){
        if (!dispatchPlanItemInfo.getInsertUser().equals(applicationUser.getUserId())){
          return new ApiResponse(HttpStatus.NOT_FOUND, "You are not allowed to delete the plan: "+dispatchPlanItemInfo.getDispatchPlan().getId() +
              "  The logged in user is not same as the dispatch plan insert user ");
        }
      }
      if (errorMessgages == null || errorMessgages.size() == 0) {
        for (DispatchPlanItemInfo dispatchPlanItemInfo : dispatchPlanItemInfoList) {
          DispatchPlan dispatchPlan = dispatchPlanItemInfo.getDispatchPlan();
          dispatchPlanItemInfoRepo.delete(dispatchPlanItemInfo);
          dispatchPlanItemInfoRepo.deleteDispatchPlanBom(dispatchPlanItemInfo.getLineNumber(), dispatchPlanItemInfo.getDispatchPlan().getId());
          if (dispatchPlanItemInfoRepo.countByDispatchPlanId(dispatchPlan.getId()) == 0) {
            dispatchPlanRepo.deleteById(dispatchPlan.getId());
          } else {
            dispatchPlan.setTotalQuantity(dispatchPlan.getTotalQuantity() - dispatchPlanItemInfo.getQuantity());
            dispatchPlanRepo.save(dispatchPlan);
          }
        }
        return new ApiResponse(HttpStatus.OK, "Plan records deleted successfully");
      }else {
        return new ApiResponse(HttpStatus.NOT_FOUND, "", errorMessgages);
      }
    } else {
      return  new ApiResponse (HttpStatus.NOT_FOUND, "Plan details not found!");
    }

  }

  /**
   * <b>Deleting the error plan record</b>
   * @param planItemId - id of the plan to be deleted
   * @param errorCode - error of the plan to be deleted
   * @param status - required boolean value to check if ther are multiple error line with the error code for the plan
   * @param loggedInUser - logged in user
   * @return - remaining error count after deleting a perticular error
   */
  @Override
  public ApiResponse deleteDispatchPlanTempInfo(Long planItemId, String errorCode, ApplicationUser loggedInUser, boolean status) {
    if (StringUtils.isEmpty(errorCode))
      return new ApiResponse(HttpStatus.NOT_FOUND, "Error code is mandatory");
    DispatchPlanItemTempInfo dispatchPlanItemInfo = dispatchPlanItemTempInfoRepo
        .findPlanIdAndLineNumberByPlanItemId(planItemId);
    if (dispatchPlanItemInfo == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "Plan item not found with given id ");
    if (!status) {
      List<DispatchPlanItemErrorInfo> errors = dispatchPlanItemErrorInfoRepo.findByPlanIdAndLineNumber(
          dispatchPlanItemInfo.getPlanId(), dispatchPlanItemInfo.getLineNumber());
      if (errors.size() > 1) {
        String errorDesc = errors.parallelStream().map(error -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, error.getCode().getDesc()))
            .collect(Collectors.joining(","));
        return new ApiResponse(HttpStatus.METHOD_NOT_ALLOWED,
            String.format("Selected record has multiple errors (%s),do you want to delete all ?", errorDesc));
      }
    }
    dispatchPlanItemErrorInfoRepo.deleteByPlanIdAndLineNumber(dispatchPlanItemInfo.getPlanId(),
        dispatchPlanItemInfo.getLineNumber());
    dispatchPlanItemTempInfoRepo.deleteById(planItemId);
    if (dispatchPlanItemTempInfoRepo.countByPlanId(dispatchPlanItemInfo.getPlanId()) == 0) {
      dispatchPlanRepo.deleteById(dispatchPlanItemInfo.getPlanId());
    } else {
      Optional<DispatchPlan> optionalDispatchPlan = dispatchPlanRepo.findOneById(dispatchPlanItemInfo.getPlanId());
//        DispatchPlan dispatchPlan = optionalDispatchPlan.get();
      optionalDispatchPlan.ifPresent(dispatchPlan -> {
        dispatchPlan.setTotalQuantity(dispatchPlan.getTotalQuantity() - dispatchPlanItemInfo.getQuantity());
        dispatchPlanRepo.save(dispatchPlan);
      });
      /*Checking if there are any errors left in error table.
     If there are still errors with the planId then do nothing else move the remaining planItems to main table*/
//      if (!dispatchPlanItemErrorInfoRepo.existsByPlanId(dispatchPlanItemInfo.getPlanId())) {
//        planResolveErrorService.storeDuplicateRecord(dispatchPlanItemInfo, loggedInUser);
//      }
    }
    return this.getDispatchPlanErrorInfoCount(dispatchPlanItemInfo.getPlanId(), loggedInUser);
  }

  /**
   * <b>Calling the create dispatch plan procedure after resolving all the errors</b>
   * @param planId - id of the plan to be created
   * @param loggedInUser - - logged in user
   * @return - success message along with plan id/error message
   */
  @Override
  public ApiResponse createDispatchPlan(Long planId, ApplicationUser loggedInUser) {
    if (dispatchPlanItemErrorInfoRepo.findCountByPlanId(planId) > 0) {
      return new ApiResponse(HttpStatus.METHOD_NOT_ALLOWED, "Please resolve the all errors to create the plan !");
    }
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("atl_business_flow_pkg.create_dispatch_plan_manual");
    storedProcedure.registerStoredProcedureParameter("p_disp_plan_id", Long.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_status", String.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_disp_plan_id", planId);
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    String status = (String) storedProcedure.getOutputParameterValue("p_status");
    if (status == null || Constants.PlanStatus.ERROR.name().equals(status))
      return new ApiResponse(HttpStatus.BAD_REQUEST, "Some thing went wrong while creating the plan !");
    return new ApiResponse(HttpStatus.OK, String.format("Plan ID : %s created successfully.", planId));
  }

  /**
   *<b>Modify diapatch plan</b>
   * @param dispatchPlanItemDto - required changed to be made in the dispatch plan {@link DispatchPlanItemDto}
   * @param applicationUser - logged in user
   * @return - the updated dispatch plan details
   */
  @Override
  @Transactional(rollbackOn = Exception.class)
  public ApiResponse modifyDispatchPlan(List<DispatchPlanItemDto> dispatchPlanItemDtoList, ApplicationUser applicationUser) {
    List<DispatchPlanItemDto> planItemDtos = new ArrayList<>();

    List<DispatchPlanItemInfo> dispatchPlanItemInfoList = new ArrayList<>();
    for (DispatchPlanItemDto dispatchPlanItemDto : dispatchPlanItemDtoList) {
     /*Here we were checking with plant code.*/
      /* if (!UserRole.DP_REP.equals(applicationUser.getRole()) && !applicationUser.getPlantCode().equals(dispatchPlanItemDto.getSourceLocation())) {
        return new ApiResponse(HttpStatus.NOT_FOUND, "You are not allowed to modify the plan: " + dispatchPlanItemDto.getPlanId());
      }*/

      /*Now we need to check with insertUserId. A plan can be modified only by the user who created it*/
      if (!applicationUser.getUserId().equals(dispatchPlanItemDto.getInsertUser())) {
        return new ApiResponse(HttpStatus.NOT_FOUND, "You are not allowed to modify the plan: " + dispatchPlanItemDto.getPlanId() +
            ". The logged in user is not same as the dispatch plan insert user. ");
      }
      String error = dispatchPlanItemDto.validateModifyQuantity();
      if (!StringUtils.isEmpty(error))
        return new ApiResponse(HttpStatus.NOT_FOUND, error);
      if (dispatchPlanItemDto.getId() == null)
        return new ApiResponse(HttpStatus.NOT_FOUND, "Plan Id not found ");
      Optional<DispatchPlanItemInfo> optionalDispatchPlanItemInfo = dispatchPlanItemInfoRepo
          .findById(dispatchPlanItemDto.getId());
      if (!optionalDispatchPlanItemInfo.isPresent())
        return new ApiResponse(HttpStatus.NOT_FOUND, "Plan Id not found ");
      DispatchPlanItemInfo dispatchPlanItemInfo = optionalDispatchPlanItemInfo.get();
      if (!StringUtils.isEmpty(dispatchPlanItemDto.getDispatchDate())) {
        Date dispatchDate = DateUtils.formatDate(dispatchPlanItemDto.getDispatchDate(),
            Constants.PLAN_RECORD_DATE_FORMAT);
        /*
         * if (dispatchDate.before(new Date())) { return new
         * ApiResponse(HttpStatus.NOT_FOUND,
         * String.format("Given dispatch date should not before current date",
         * dispatchPlanItemDto.getDispatchDate())); }
         */
        dispatchPlanItemInfo.setDispatchDate(dispatchDate);
      }
      if (!StringUtils.isEmpty(dispatchPlanItemDto.getMarketSegment())) {
        if (dispatchPlanItemDto.getMarketSegment().length() > 10) {
          return new ApiResponse(HttpStatus.METHOD_NOT_ALLOWED, String.format(
              "Market segment lenght should not be more than 10 characters for plan Id : %s and line number : %s",
              dispatchPlanItemDto.getPlanId(), dispatchPlanItemDto.getLineNumber()));
        }
        dispatchPlanItemInfo.setMarketSegment(dispatchPlanItemDto.getMarketSegment());
      }
      if (!StringUtils.isEmpty(dispatchPlanItemDto.getDestinationLocation())) {
        List<Map<String, String>> destinationLocation = locationRepo.findDestDescWtihDestinations(Collections.singletonList(dispatchPlanItemDto.getDestinationLocation()));
        if (destinationLocation.size() == 0)
          return new ApiResponse(HttpStatus.NOT_FOUND, String.format("Given destination %s not found in system",
              dispatchPlanItemDto.getDestinationLocation()));
        dispatchPlanItemInfo.setDestinationLocation(destinationLocation.get(0).get("DESTLOC"));
        dispatchPlanItemInfo.setDestinationDescription(destinationLocation.get(0).get("DESTDESC"));
      }
      if (!StringUtils.isEmpty(dispatchPlanItemDto.getItemId())) {
        MTItem masterItem = itemRepo.findTteAndCategoryById(dispatchPlanItemDto.getItemId());
        if (masterItem == null)
          return new ApiResponse(HttpStatus.NOT_FOUND,
              String.format("Given material code %s not found in system", dispatchPlanItemDto.getItemId()));
        if (masterItem.getCategory() == null)
          return new ApiResponse(HttpStatus.NOT_FOUND, String
              .format("Given material code %s not have category in system", dispatchPlanItemDto.getItemId()));
        dispatchPlanItemInfo.setItemId(masterItem.getId());
        dispatchPlanItemInfo.setTte(masterItem.getTte());
        dispatchPlanItemInfo.setItemDescription(masterItem.getDescription());
        dispatchPlanItemInfo.setCategory(masterItem.getCategory());
      }
      if (!StringUtils.isEmpty(dispatchPlanItemDto.getBatchCode())) {
        /*OLD CODE By Vamshi*/
/*        Optional<MTBatchCode> batchCode = batchCodeRepo.findByCodeIgnoreCase(dispatchPlanItemDto.getBatchCode());
        if (!batchCode.isPresent())
          return new ApiResponse(HttpStatus.NOT_FOUND,
              String.format("Given batch code %s not found in system", dispatchPlanItemDto.getBatchCode()));
        dispatchPlanItemInfo.setBatchCode(batchCode.get().getCode());*/
        /*Checking if the given batch code is valid or not*/
        if (!batchCodeRepo.existsByCode(dispatchPlanItemDto.getBatchCode())) {
          return new ApiResponse(HttpStatus.NOT_FOUND,
              String.format("Given batch code %s not found in system", dispatchPlanItemDto.getBatchCode()));
        }else {
          dispatchPlanItemInfo.setBatchCode(dispatchPlanItemDto.getBatchCode().toUpperCase());
        }
      }
      if (dispatchPlanItemDto.getQuantity() != null) {
        if (dispatchPlanItemDto.getQuantity() == 0
            || dispatchPlanItemDto.getQuantity() < dispatchPlanItemInfo.getApprovedQuantity()) {
          return new ApiResponse(HttpStatus.NOT_FOUND, "Plan quantity should not less than approved quantity !");
        } else {
          dispatchPlanItemInfo.setQuantity(dispatchPlanItemDto.getQuantity());
        }
      }
      if (dispatchPlanItemDto.getPriority() != null) {
        if (dispatchPlanItemDto.getPriority() == 0)
          return new ApiResponse(HttpStatus.NOT_FOUND, "Please provide valid Priority !");
        else
          dispatchPlanItemInfo.setPriority(dispatchPlanItemDto.getPriority());
      }

      /*Validation when multiple user are trying to delete approved quantity on the same plan items
       * delete approve quantity should be less than (approved_qty - deleted_Qty - reserved_qty)*/
      if (dispatchPlanItemDto.getDeleteApprQuantity() > 0) {
        if (dispatchPlanItemDto.getDeleteApprQuantity() > (dispatchPlanItemInfo.getApprovedQuantity() -( dispatchPlanItemInfo.getReservedQuantity() + dispatchPlanItemInfo.getDeletedQuantity() + dispatchPlanItemInfo.getLoadedQty()+dispatchPlanItemInfo.getDispatchedQuantity()))) {
          return new ApiResponse(HttpStatus.NOT_FOUND, "Approved delete qty should be lessthan or equal to approved quantity !");
        }
      }
      /*Validation when multiple user are trying to delete unApproved quantity on the same plan items*/
      if (dispatchPlanItemDto.getDeleteUnApprQuantity() > 0) {
        if (dispatchPlanItemDto.getDeleteUnApprQuantity() > dispatchPlanItemInfo.getUnapprovedQuantity()) {
          return new ApiResponse(HttpStatus.NOT_FOUND, "Un Approved delete qty should be lessthan or equal to un approved quantity !");
        }
      }
      if (dispatchPlanItemDto.getDeleteApprQuantity() > 0) {
        if (dispatchPlanItemDto.getApprovedQuantity() < dispatchPlanItemDto.getDeleteApprQuantity()) {
          return new ApiResponse(HttpStatus.METHOD_NOT_ALLOWED, "Approved delete qty should be lessthan or equal to approved quantity !");
        } else if (dispatchPlanItemDto.getApprovedQuantity() - (dispatchPlanItemDto.getDeletedApprQuantity() + dispatchPlanItemDto.getReservedQuantity()) < dispatchPlanItemDto.getDeleteApprQuantity()) {
          return new ApiResponse(HttpStatus.METHOD_NOT_ALLOWED, String.format("%d quantity already reserved and %d was deleted", dispatchPlanItemDto.getReservedQuantity(), dispatchPlanItemDto.getDeletedApprQuantity()));
        }
        dispatchPlanItemInfo.setDeletedQuantity(dispatchPlanItemDto.getDeletedApprQuantity() + dispatchPlanItemDto.getDeleteApprQuantity());
      }
      if (dispatchPlanItemDto.getDeleteUnApprQuantity() > 0) {
        if (dispatchPlanItemDto.getUnapprovedQuantity() < dispatchPlanItemDto.getDeleteUnApprQuantity()) {
          return new ApiResponse(HttpStatus.METHOD_NOT_ALLOWED, "Un Approved delete qty should be lessthan or equal to un approved quantity !");
        }
        dispatchPlanItemInfo.setUnapprovedDeletedQuantity(dispatchPlanItemInfo.getUnapprovedDeletedQuantity() + dispatchPlanItemDto.getDeleteUnApprQuantity());
      }
      if (dispatchPlanItemDto.getQuantity() > 0 || dispatchPlanItemDto.getDeleteApprQuantity() > 0 || dispatchPlanItemDto.getDeleteUnApprQuantity() > 0) {
        dispatchPlanItemInfo.setUnapprovedQuantity(dispatchPlanItemDto.getQuantity() - (dispatchPlanItemInfo.getApprovedQuantity() + dispatchPlanItemInfo.getUnapprovedDeletedQuantity()));
        dispatchPlanItemInfo.setAvailableQuantity(dispatchPlanItemInfo.getApprovedQuantity() - (dispatchPlanItemInfo.getDeletedQuantity() +
            dispatchPlanItemInfo.getReservedQuantity() + dispatchPlanItemInfo.getDispatchedQuantity() + (dispatchPlanItemInfo.getLoadedQty() != null ? dispatchPlanItemInfo.getLoadedQty() : 0)));
        dispatchPlanItemInfo.setTotalAvailableQuantity(dispatchPlanItemInfo.getAvailableQuantity() + dispatchPlanItemInfo.getUnapprovedQuantity());
      }
      if (dispatchPlanItemInfo.getApprovedQuantity() > 0) {
        if (dispatchPlanItemInfo.getUnapprovedQuantity() == 0) {
          dispatchPlanItemInfo.setAppStatus(Constants.DispatchPlanItemAppStatus.APPROVED);
        } else {
          dispatchPlanItemInfo.setAppStatus(Constants.DispatchPlanItemAppStatus.APPROVED_PART);
        }
      }

      /*Set Status as completed when Total available quantity = 0 & loaded_qty = 0 & reserved_qty = 0(When plan item used in other loadslips,
      the qty will be in loaded/reserved qty till the second LS is Confirmed) & Status is OPEN & AppStatus is APPROVED or APPROVED-PART*/
      if (Status.OPEN.equals(dispatchPlanItemInfo.getStatus()) && dispatchPlanItemInfo.getTotalAvailableQuantity() == 0
          && dispatchPlanItemInfo.getLoadedQty() == 0 && dispatchPlanItemInfo.getReservedQuantity() == 0) {
        dispatchPlanItemInfo.setStatus(Status.COMPLETED);
      }

      /*Setting last modified user details*/
      dispatchPlanItemInfo.setUpdateDate(new Date());
      dispatchPlanItemInfo.setUpdateUser(applicationUser.getUserId());

      /*      dispatchPlanItemInfoRepo.save(dispatchPlanItemInfo);*/
      dispatchPlanItemInfoList.add(dispatchPlanItemInfo);
      planItemDtos.add(new DispatchPlanItemDto(dispatchPlanItemInfo));
    }

    if (dispatchPlanItemInfoList.size() == dispatchPlanItemDtoList.size()) {
      dispatchPlanItemInfoRepo.saveAll(dispatchPlanItemInfoList);
      /*Getting STANDARD_FTL truck Details*/
      MTTruckTypeInfo mtTruckTypeInfo = truckTypeInfoRepository.findOneByType("STANDARD_FTL").parallelStream().filter(mt -> StringUtils.isEmpty(mt.getVariant1())).findFirst().get();

      for (DispatchPlanItemDto planItemDto : planItemDtos) {
        planItemDto.calculateDispatchPlanBOMValues(planItemDto,applicationUser, mtTruckTypeInfo);
      }

      return new ApiResponse(HttpStatus.OK, "Plan record updated successfully",
          planItemDtos);
    } else {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Something Went Wrong!");
    }
  }

}
