package com.api.apollo.atom.service.impl;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.Constants.ErrorCode;
import com.api.apollo.atom.constant.UserRole;
import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.planner.DispatchPlanFilterDto;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.master.MTItem;
import com.api.apollo.atom.entity.plan.DispatchPlan;
import com.api.apollo.atom.entity.plan.DispatchPlanItemInfo;
import com.api.apollo.atom.entity.plan.DispatchPlanItemTempInfo;
import com.api.apollo.atom.repository.master.BatchCodeRepository;
import com.api.apollo.atom.repository.master.ItemRepository;
import com.api.apollo.atom.repository.master.LocationRepository;
import com.api.apollo.atom.repository.master.MTPlantBatchRepository;
import com.api.apollo.atom.repository.planner.DispatchPlanItemErrorInfoRepository;
import com.api.apollo.atom.repository.planner.DispatchPlanItemInfoRepository;
import com.api.apollo.atom.repository.planner.DispatchPlanItemTempInfoRepository;
import com.api.apollo.atom.repository.planner.DispatchPlanRepository;
import com.api.apollo.atom.service.PlanResolveErrorService;
import com.api.apollo.atom.service.PlannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class PlanResolveErrorServiceImpl implements PlanResolveErrorService {

  @Autowired
  private LocationRepository locationRepo;

  @Autowired
  private DispatchPlanItemTempInfoRepository dispatchPlanItemTempInfoRepo;

  @Autowired
  private DispatchPlanItemErrorInfoRepository dispatchPlanItemErrorInfoRepo;

  @Autowired
  private BatchCodeRepository batchCodeRepo;

  @Autowired
  private ItemRepository itemRepo;

  @Autowired
  private PlannerService plannerService;

  @Autowired
  DispatchPlanRepository dispatchPlanRepository;

  @Autowired
  DispatchPlanItemInfoRepository itemInfoRepository;

  @Autowired
  MTPlantBatchRepository mtPlantBatchRepository;

  @Autowired
  ItemRepository itemRepository;

  /*
   * Source & destination plan record error resolver
   */
  @Override
  public ApiResponse resolveC1(DispatchPlanFilterDto searchDto, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(searchDto.getSourceCode()) || StringUtils.isEmpty(searchDto.getDestinationCode()))
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("location code is mandatory !"));
    List<String> errorMessages = new ArrayList<String>();
    List<Map<String,String>> sourceLocation = locationRepo.findDestDescWtihDestinations(Collections.singletonList(searchDto.getSourceCode()));
    List<Map<String,String>> destinationLocation = locationRepo.findDestDescWtihDestinations(Collections.singletonList(searchDto.getDestinationCode()));
    if (sourceLocation.size() == 0)
      errorMessages.add(String.format("given source location %s is not found in system", searchDto.getSourceCode()));
    if (destinationLocation.size() == 0)
      errorMessages.add(String.format("given destination location %s is not found in system", searchDto.getDestinationCode()));

    /*Validation if user inserts same source and destination location */
    if(!StringUtils.isEmpty(searchDto.getSourceCode()) && !StringUtils.isEmpty(searchDto.getDestinationCode())){
      if (searchDto.getSourceCode().equalsIgnoreCase(searchDto.getDestinationCode())){
        errorMessages.add("Destination location should not be Source location");
      }
    }
    /*Validation if user tries to insert source location other than the plant location */
    if (!UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole()) && !StringUtils.isEmpty(searchDto.getSourceCode())
        && !(searchDto.getSourceCode().equalsIgnoreCase(loggedInUser.getPlantCode()))) {
      errorMessages.add("Source location should be " + loggedInUser.getPlantCode());
    }
    if (!errorMessages.isEmpty()) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "", errorMessages);
    }
    if (searchDto.getPlanItemId() == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("plan item id is missing !"));
    DispatchPlanItemTempInfo planItemTempInfo = dispatchPlanItemTempInfoRepo.findPlanIdAndLineNumberByPlanItemId(searchDto.getPlanItemId());
    if (planItemTempInfo == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("plan item not found !"));
    dispatchPlanItemTempInfoRepo.updateBySourceAndDestinationCode(searchDto.getSourceCode(), searchDto.getDestinationCode(), destinationLocation.get(0).get("DESTDESC"), searchDto.getPlanItemId());
    return this.deletePlanErrorRecord(planItemTempInfo, Collections.singletonList(ErrorCode.valueOf(searchDto.getErrorCode())), loggedInUser);
  }

//  private void validateBatchCode(List<String> errorMessages, DispatchPlanItemTempInfo dispatchPlanItemTempInfo, DispatchPlanFilterDto searchDto, ApplicationUser loggedInUser) {
//    boolean isMaterialCodeTyre = false;
//    if (!StringUtils.isEmpty(dispatchPlanItemTempInfo.getItemId())) {
//      if (itemRepository.existsByIdAndClassification(dispatchPlanItemTempInfo.getItemId(), Constants.ItemClassification.TYRE.name())){
//        isMaterialCodeTyre = true;
//      }
//    }
//    if (isMaterialCodeTyre) {
//      List<Map<String, Object>> mtPlantBatches = mtPlantBatchRepository.findAllBatchCodePrefix();
//      if (mtPlantBatches != null && !mtPlantBatches.isEmpty()) {
//        Optional<Map<String, Object>> locationMap = mtPlantBatches.parallelStream().filter(m -> dispatchPlanItemTempInfo.getSourceLocation().equals(m.get("locationId").toString())).findFirst();
//        if (locationMap.isPresent() && !searchDto.getBatchCode().startsWith(locationMap.get().get("batchCodePrefix").toString())){
//          errorMessages.add("The batch code "+searchDto.getBatchCode()+" is not valid at the source "+ dispatchPlanItemTempInfo.getSourceLocation());
//        }
//      }
//    }
//  }
//

  /*
   * Material code plan record error resolver
   */
  @Override
  public ApiResponse resolveC2(DispatchPlanFilterDto searchDto, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(searchDto.getMaterialCode()))
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("material code is mandatory !"));
		/*if (!itemRepo.existsById(searchDto.getMaterialCode()))
			return new ApiResponse(HttpStatus.NOT_FOUND, "",Collections.singletonList(String.format("Material code %s is not found in system", searchDto.getMaterialCode())));*/
    MTItem masterItem = itemRepo.findTteAndCategoryById(searchDto.getMaterialCode());
    if (masterItem == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList(String.format("Material code %s is not found in system", searchDto.getMaterialCode())));
    if (masterItem.getTte() == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList(String.format("TTE is not found for Material %s in system", searchDto.getMaterialCode())));
    if (masterItem.getCategory() == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList(String.format("category is not found for Material %s in system", searchDto.getMaterialCode())));
    if (searchDto.getPlanItemId() == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("plan item id is missing !"));
    DispatchPlanItemTempInfo planItemTempInfo = dispatchPlanItemTempInfoRepo.findPlanIdAndLineNumberByPlanItemId(searchDto.getPlanItemId());
    if (planItemTempInfo == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("plan item not found !"));
    dispatchPlanItemTempInfoRepo.updateByMaterialCodeAndTteAndCategory(searchDto.getMaterialCode(), masterItem.getTte(), masterItem.getCategory(), masterItem.getDescription(), searchDto.getPlanItemId());
    List<ErrorCode> errorCodes = new ArrayList<>();
    errorCodes.add(ErrorCode.valueOf(searchDto.getErrorCode()));
    errorCodes.add(Constants.ErrorCode.C3);
    errorCodes.add(Constants.ErrorCode.C4);
    return this.deletePlanErrorRecord(planItemTempInfo, errorCodes, loggedInUser);
  }

  /*
   * Material TTE plan record error resolver
   */
  @Override
  public ApiResponse resolveC3(DispatchPlanFilterDto searchDto, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(searchDto.getMaterialCode()))
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("material code is mandatory !"));
    MTItem masterItem = itemRepo.findTteAndCategoryById(searchDto.getMaterialCode());
    if (masterItem == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList(String.format("Material code %s is not found in system", searchDto.getMaterialCode())));
    if (masterItem.getTte() == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList(String.format("TTE is not found for Material %s in system", searchDto.getMaterialCode())));
    if (searchDto.getPlanItemId() == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("plan item id is missing !"));
    DispatchPlanItemTempInfo planItemTempInfo = dispatchPlanItemTempInfoRepo.findPlanIdAndLineNumberByPlanItemId(searchDto.getPlanItemId());
    if (planItemTempInfo == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("plan item not found !"));
    dispatchPlanItemTempInfoRepo.updateByTte(masterItem.getTte(), searchDto.getPlanItemId());
    return this.deletePlanErrorRecord(planItemTempInfo, Collections.singletonList(ErrorCode.valueOf(searchDto.getErrorCode())), loggedInUser);
  }


  /*
   * Item category plan record error resolver
   */
  @Override
  public ApiResponse resolveC4(DispatchPlanFilterDto searchDto, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(searchDto.getMaterialCode()))
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("material code is mandatory !"));
    MTItem masterItem = itemRepo.findTteAndCategoryById(searchDto.getMaterialCode());
    if (masterItem == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList(String.format("Material code %s is not found in system", searchDto.getMaterialCode())));
    if (masterItem.getCategory() == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList(String.format("category is not found for Material %s in system", searchDto.getMaterialCode())));
    if (searchDto.getPlanItemId() == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("plan item id is missing !"));
    DispatchPlanItemTempInfo planItemTempInfo = dispatchPlanItemTempInfoRepo.findPlanIdAndLineNumberByPlanItemId(searchDto.getPlanItemId());
    if (planItemTempInfo == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("plan item not found !"));
    dispatchPlanItemTempInfoRepo.updateByCategory(masterItem.getCategory(), searchDto.getPlanItemId());
    return this.deletePlanErrorRecord(planItemTempInfo, Collections.singletonList(ErrorCode.valueOf(searchDto.getErrorCode())), loggedInUser);
  }


  /*
   * Batch code plan record error resolver
   */
  @Override
  public ApiResponse resolveC5(DispatchPlanFilterDto searchDto, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(searchDto.getBatchCode()))
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("batch code is mandatory !"));
    if (!batchCodeRepo.existsByCode(searchDto.getBatchCode()))
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList(String.format("batch code %s is not found in system", searchDto.getBatchCode())));
    if (searchDto.getPlanItemId() == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("plan item id is missing !"));
   /*Checking if Material code (C2) error OR Location Code (C1) errors are there in errorPlans table.
   * If any of the errors are there for */
//    Optional<DispatchPlanItemTempInfo> dispatchPlanItemTempInfo = dispatchPlanItemTempInfoRepo.findById(searchDto.getPlanItemId());
//    if (dispatchPlanItemTempInfo.isPresent() ) {
//      List<String> errorMessages = new ArrayList<>();
//      if (!dispatchPlanItemErrorInfoRepo.existsByPlanIdAndLineNumberAndCode(dispatchPlanItemTempInfo.get().getPlanId(), dispatchPlanItemTempInfo.get().getLineNumber(), ErrorCode.C2)&&
//          !dispatchPlanItemErrorInfoRepo.existsByPlanIdAndLineNumberAndCode(dispatchPlanItemTempInfo.get().getPlanId(), dispatchPlanItemTempInfo.get().getLineNumber(), ErrorCode.C1)) {
//        validateBatchCode(errorMessages, dispatchPlanItemTempInfo.get(), searchDto, loggedInUser);
//      } else {
//        errorMessages.add("Please resolve Location/Material code error first...");
//      }
//      if (errorMessages != null && !errorMessages.isEmpty()) {
//        return new ApiResponse(HttpStatus.NOT_FOUND, "",errorMessages);
//      }
//    }
    DispatchPlanItemTempInfo planItemTempInfo = dispatchPlanItemTempInfoRepo.findPlanIdAndLineNumberByPlanItemId(searchDto.getPlanItemId());
    if (planItemTempInfo == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("plan item not found !"));
    dispatchPlanItemTempInfoRepo.updateByBatchCode(searchDto.getBatchCode(), searchDto.getPlanItemId());
    return this.deletePlanErrorRecord(planItemTempInfo, Collections.singletonList(ErrorCode.valueOf(searchDto.getErrorCode())), loggedInUser);
  }


  /*
   * Duplicate plan record error resolver
   */
  @Override
  public ApiResponse resolveC6(DispatchPlanFilterDto searchDto, ApplicationUser loggedInUser) {
    if (searchDto.getPlanItemId() == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("plan item id is missing !"));
    DispatchPlanItemTempInfo dispatchPlanItemTempInfo = dispatchPlanItemTempInfoRepo.findById(searchDto.getPlanItemId()).get();
    if (dispatchPlanItemTempInfo == null)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("plan item not found !"));

    /*Removing C6 error and allowing Duplicate record in the main table.*/
    if (searchDto.isDuplicateAllowed()) {

      /*Deleting the C6 error from dispatch_plan_Temp_error table*/
      dispatchPlanItemErrorInfoRepo.deleteByPlanIdAndCodesInAndLineNumber(dispatchPlanItemTempInfo.getPlanId(), Collections.singletonList(ErrorCode.valueOf(searchDto.getErrorCode())), dispatchPlanItemTempInfo.getLineNumber());

      /*Checking if there are any errors left in error table.
      * All the items related to that perticular planId are sent to main table only when there are no errors left with that planId*/
      if (!dispatchPlanItemErrorInfoRepo.existsByPlanId(dispatchPlanItemTempInfo.getPlanId())) {
//        String saveStatus = storeDuplicateRecord(dispatchPlanItemTempInfo, loggedInUser);
//        if (!StringUtils.isEmpty(saveStatus)) {
//          return plannerService.getDispatchPlanErrorInfoCount(dispatchPlanItemTempInfo.getPlanId(), loggedInUser);
//        } else {
//          return new ApiResponse(HttpStatus.NOT_FOUND, "Something went wrong while moving duplicate record to main table");
//        }
        /*Calling the createDispatchPlan service*/
        ApiResponse createPlanResponse = plannerService.createDispatchPlan(dispatchPlanItemTempInfo.getPlanId(), loggedInUser);
        if (createPlanResponse.getStatusCode() == 200 ){
          return plannerService.getDispatchPlanErrorInfoCount(dispatchPlanItemTempInfo.getPlanId(), loggedInUser);
        }else {
          return new ApiResponse(HttpStatus.NOT_FOUND, "Something went wrong while moving duplicate record to main table");
        }
      } else {
        return plannerService.getDispatchPlanErrorInfoCount(dispatchPlanItemTempInfo.getPlanId(), loggedInUser);
      }
    } else {
      //dispatchPlanItemTempInfoRepo.deleteById(searchDto.getPlanItemId());
      return this.deletePlanErrorRecord(dispatchPlanItemTempInfo, Collections.singletonList(ErrorCode.valueOf(searchDto.getErrorCode())), loggedInUser);
    }
  }

  private ApiResponse deletePlanErrorRecord(DispatchPlanItemTempInfo dispatchPlanItemTempInfo, List<ErrorCode> errorCodes, ApplicationUser loggedInUser) {
    dispatchPlanItemErrorInfoRepo.deleteByPlanIdAndCodesInAndLineNumber(dispatchPlanItemTempInfo.getPlanId(), errorCodes, dispatchPlanItemTempInfo.getLineNumber());
    //if (errorId == null)
    //return new ApiResponse(HttpStatus.NOT_FOUND, "", Collections.singletonList("Error record not found for plan Item "+searchDto.getPlanItemId()));
    return plannerService.getDispatchPlanErrorInfoCount(dispatchPlanItemTempInfo.getPlanId(), loggedInUser);
  }

  /*Here we well fetch all the items in plan from the temp table and sent to main table*/
  @Override
  @Transactional(rollbackOn = Exception.class)
  public String storeDuplicateRecord(DispatchPlanItemTempInfo PlanItemTempInfo, ApplicationUser applicationUser) {
    if (!StringUtils.isEmpty(PlanItemTempInfo)) {
      List<DispatchPlanItemTempInfo> dispatchPlanItemTempInfoList = dispatchPlanItemTempInfoRepo.findAllByPlanId(PlanItemTempInfo.getPlanId());
      if (dispatchPlanItemTempInfoList.size() > 0) {
        List<DispatchPlanItemInfo> dispatchPlanItemInfoList = new ArrayList<>();
        DispatchPlan dispatchPlan = dispatchPlanRepository.findOneById(PlanItemTempInfo.getPlanId()).get();
        for (DispatchPlanItemTempInfo dispatchPlanItemTempInfo : dispatchPlanItemTempInfoList) {
          /*Double-check if there are any errors left with that planId and line Number*/
          if (dispatchPlanItemErrorInfoRepo.existsByPlanIdAndLineNumber(dispatchPlanItemTempInfo.getPlanId(), dispatchPlanItemTempInfo.getLineNumber())) {
            break;
          }
          DispatchPlanItemInfo dispatchPlanItemInfo = new DispatchPlanItemInfo();
          dispatchPlanItemInfo.setStatus(Constants.Status.OPEN);
          dispatchPlanItemInfo.setAppStatus(Constants.DispatchPlanItemAppStatus.APPROVAL_PENDING);
          dispatchPlanItemInfo.setAvailableQuantity(0);
          dispatchPlanItemInfo.setReservedQuantity(0);
          dispatchPlanItemInfo.setTotalAvailableQuantity(0);
          dispatchPlanItemInfo.setLoadedQty(0);
          dispatchPlanItemInfo.setUnapprovedQuantity(0);
          dispatchPlanItemInfo.setApprovedQuantity(0);
          dispatchPlanItemInfo.setDispatchedQuantity(0);
          dispatchPlanItemInfo.setBatchCode(dispatchPlanItemTempInfo.getBatchCode());
          dispatchPlanItemInfo.setCategory(dispatchPlanItemTempInfo.getCategory());
          dispatchPlanItemInfo.setDeletedQuantity(0);
          dispatchPlanItemInfo.setDestinationDescription(dispatchPlanItemTempInfo.getDestinationDescription());
          dispatchPlanItemInfo.setDestinationLocation(dispatchPlanItemTempInfo.getDestinationLocation());
          dispatchPlanItemInfo.setDispatchDate(dispatchPlanItemTempInfo.getDispatchDate());
          dispatchPlanItemInfo.setItemDescription(dispatchPlanItemTempInfo.getItemDescription());
          dispatchPlanItemInfo.setItemId(dispatchPlanItemTempInfo.getItemId());
          dispatchPlanItemInfo.setMarketSegment(dispatchPlanRepository.getMarketSegmentProcedure(dispatchPlanItemTempInfo.getSourceLocation(),dispatchPlanItemTempInfo.getDestinationLocation()));
          dispatchPlanItemInfo.setPriority(dispatchPlanItemTempInfo.getPriority());
          dispatchPlanItemInfo.setQuantity(dispatchPlanItemTempInfo.getQuantity());
          dispatchPlanItemInfo.setTte(dispatchPlanItemTempInfo.getTte());
          dispatchPlanItemInfo.setUnapprovedDeletedQuantity(0);
          dispatchPlanItemInfo.setComments(dispatchPlanItemTempInfo.getComments());
          dispatchPlanItemInfo.setDispatchPlan(dispatchPlan);
          dispatchPlanItemInfo.setInsertDate(new Date());
          dispatchPlanItemInfo.setInsertUser(applicationUser.getUserId());
          dispatchPlanItemInfo.setLineNumber(dispatchPlanItemTempInfo.getLineNumber());
          dispatchPlanItemInfo.setSourceLocation(dispatchPlanItemTempInfo.getSourceLocation());
          dispatchPlanItemInfo.setUpdateDate(new Date());
          dispatchPlanItemInfo.setUpdateUser(applicationUser.getUserId());

          dispatchPlanItemInfoList.add(dispatchPlanItemInfo);
          dispatchPlanItemTempInfoRepo.deleteById(dispatchPlanItemTempInfo.getId());
        }
        /*Checking if temp table and main table has same number of items*/
        if (dispatchPlanItemInfoList.size() == dispatchPlanItemTempInfoList.size()) {
          /*Changing status of plan in disp_plan table.
          * This will reflect while fetching pending-plans api in errors plans page*/
          dispatchPlan.setStatus(Constants.Status.OPEN);
          dispatchPlanRepository.save(dispatchPlan);
          itemInfoRepository.saveAll(dispatchPlanItemInfoList);
          return "Plans Updated Successfully";
        }
      }
    }
    return null;
  }

}
