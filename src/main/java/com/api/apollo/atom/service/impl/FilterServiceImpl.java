package com.api.apollo.atom.service.impl;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.Constants.Status;
import com.api.apollo.atom.constant.ItemType;
import com.api.apollo.atom.constant.LocationType;
import com.api.apollo.atom.constant.UserRole;
import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.master.*;
import com.api.apollo.atom.dto.ops.*;
import com.api.apollo.atom.dto.planner.DispatchPlanFilterDto;
import com.api.apollo.atom.dto.planner.DispatchPlanItemDto;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.master.*;
import com.api.apollo.atom.entity.ops.*;
import com.api.apollo.atom.entity.plan.DispatchPlanItemInfo;
import com.api.apollo.atom.repository.UserRepository;
import com.api.apollo.atom.repository.master.LocationRepository;
import com.api.apollo.atom.repository.master.TruckTypeInfoRepository;
import com.api.apollo.atom.service.FilterService;
import com.api.apollo.atom.service.UtilityService;
import com.api.apollo.atom.util.DateUtils;
import com.api.apollo.atom.util.Utility;
import com.google.gson.Gson;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.api.apollo.atom.constant.Constants.PLAN_RECORD_DATE_FORMAT;

@Service("FilterService")
public class FilterServiceImpl implements FilterService {

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private UserRepository userRepo;

  @Autowired
  private LocationRepository locationRepository;

  @Autowired
  private UtilityService utilityService;

  @Autowired
  private TruckTypeInfoRepository truckTypeInfoRepository;

  @Override
  public Page<DispatchPlanItemInfo> filterDispatchPlanItems(DispatchPlanFilterDto filterDto, ApplicationUser loggedInUser, Optional<MTLocation> mtLocation) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<DispatchPlanItemInfo> criteriaQuery = criteriaBuilder.createQuery(DispatchPlanItemInfo.class);
    Root<DispatchPlanItemInfo> root = criteriaQuery.from(DispatchPlanItemInfo.class);
    root.alias("dispatchPlanInfo");
    List<Predicate> predicates = getPredicatesForDispatchPlanItems(filterDto, loggedInUser, criteriaBuilder, root, mtLocation);

    criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
    criteriaQuery.orderBy(criteriaBuilder.desc(root.get("dispatchDate")));

    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    Root<DispatchPlanItemInfo> iRoot = countQuery.from(criteriaQuery.getResultType());
    iRoot.alias("dispatchPlanInfo");
    countQuery.select(criteriaBuilder.count(iRoot));
    Predicate restriction = criteriaQuery.getRestriction();
    if (restriction != null) {
      countQuery.where(restriction);
    }
    Long count = entityManager.createQuery(countQuery).getSingleResult();

    TypedQuery<DispatchPlanItemInfo> typedQuery = entityManager.createQuery(criteriaQuery);
    typedQuery.setFirstResult(filterDto.getIndex() * filterDto.getPageLength());
    typedQuery.setMaxResults(filterDto.getPageLength());
    List<DispatchPlanItemInfo> dispacthPlanItems = typedQuery.getResultList();
    return new PageImpl<>(dispacthPlanItems, PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }

  private List<Predicate> getPredicatesForDispatchPlanItems(DispatchPlanFilterDto filterDto, ApplicationUser loggedInUser, CriteriaBuilder criteriaBuilder, Root<DispatchPlanItemInfo> root, Optional<MTLocation> mtLocation) {
    List<Predicate> predicates = new ArrayList<>();
    /*for multiple selection */
    /*if (!StringUtils.isEmpty(filterDto.getStatus())) {
      //Filtering Plans with user selected status
      filterDto.setStatuses(Collections.singletonList(DispatchPlanItemAppStatus.valueOf(filterDto.getStatus())));
    } else {
      //if status is not included in filters,then take default FGS Planner statuses (APPROVAL_PENDING, APPROVED_PART & APPROVED)
      filterDto.setStatuses(DispatchPlanItemAppStatus.getFGSPlannerStatus());
    }*/
    // LoggedIn User belongs to FGS Planner or Operations role, then get all Plans for that Plant(Plant wise Plans)
/*    if (loggedInUser.getRole().isFGSUserRole()) {
      List<String> userIds = userRepo.findUserIdByPlantCode(loggedInUser.getPlantCode()).parallelStream().map(ApplicationUser::getUserId).collect(Collectors.toList());
      predicates.add(root.<String>get("insertUser").in(userIds));
    } else {
      // Otherwise get Only current LoggedIn user created Plans
      predicates.add(criteriaBuilder.equal(root.<String>get("insertUser"), loggedInUser.getUserId()));
    }*/

    /*When loggedIn planct is ext_warehouse then we are getting its linked plant(1007) plans also*/
    if (mtLocation.isPresent() && (LocationType.EXT_WAREHOUSE.name()).equals(mtLocation.get().getLocationClass()) && filterDto.getIsViewPlans()){
      if(!StringUtils.isEmpty(filterDto.getSourceCode())){
//        predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("sourceLocation")), "%" + filterDto.getSourceCode().toLowerCase() + "%"));
        predicates.add(criteriaBuilder.and(criteriaBuilder.like(criteriaBuilder.lower(root.get("sourceLocation")), "%" + filterDto.getSourceCode().toLowerCase() + "%"),
                criteriaBuilder.or(criteriaBuilder.equal(root.get("sourceLocation"), loggedInUser.getPlantCode()), criteriaBuilder.equal(root.get("sourceLocation"), mtLocation.get().getLinkedPlant()))));
      }else{
        List<String> sourceList = new ArrayList<>();
        sourceList.add(loggedInUser.getPlantCode());
        sourceList.add("1007"); // mtLocation.get().getLinkedPlant() == 1007, for now 1007 plant only having external warehouses
        predicates.add(root.get("sourceLocation").in(sourceList));
      }

    }else {
      /*DP_REP, L1_MGR, L2_MGR*/
      if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
        if (!StringUtils.isEmpty(filterDto.getSourceCode())) {
          predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("sourceLocation")),
                  "%" + filterDto.getSourceCode().toLowerCase() + "%"));
        }
        if (!StringUtils.isEmpty(filterDto.getInsertUser())) {
          predicates.add(root.<String>get("insertUser").in(filterDto.getInsertUser()));
        }
      } else {
        // NOT DP_REP ROLE
        if (!StringUtils.isEmpty(filterDto.getInsertUser())) {
          predicates.add(root.<String>get("insertUser").in(filterDto.getInsertUser()));
        }
        /*List<String> userIds = userRepo.findUserIdByPlantCode(loggedInUser.getPlantCode()).parallelStream().map(ApplicationUser::getUserId).collect(Collectors.toList());
        predicates.add(criteriaBuilder.or(root.<String>get("insertUser").in(userIds), criteriaBuilder.equal(root.get("sourceLocation"), loggedInUser.getPlantCode())));*/
        predicates.add(criteriaBuilder.equal(root.get("sourceLocation"), loggedInUser.getPlantCode()));

      }
    }


   /* // show only open plans
    predicates.add(criteriaBuilder.equal(root.get("status"), Status.OPEN));*/

    //Show Plans based on status OPEN or COMPLETED
    if (filterDto.getPlanStatus() != null && filterDto.getPlanStatus().size()>0) {
      List<Status> statuses = filterDto.getPlanStatus().parallelStream().map(Status::valueOf).collect(Collectors.toList());
      //predicates.add(criteriaBuilder.equal(root.get("status"), Status.valueOf(filterDto.getPlanStatus())));
      predicates.add(criteriaBuilder.and(root.get("status").in(statuses)));
    }

    if (filterDto.getPlanId() != null && filterDto.getPlanId() != 0)
      predicates.add(criteriaBuilder.equal(root.get("dispatchPlan").get("id"), filterDto.getPlanId()));
    if (!StringUtils.isEmpty(filterDto.getDestinationCode()))
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("destinationLocation")),
              "%" + filterDto.getDestinationCode().toLowerCase() + "%"));
    if (filterDto.getMarketSegment() != null && filterDto.getMarketSegment().size()>0)
      /*predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("marketSegment")),
              "%" + filterDto.getMarketSegment().toLowerCase() + "%"));*/
      predicates.add(criteriaBuilder.and(root.get("marketSegment").in(filterDto.getMarketSegment())));
    if (!StringUtils.isEmpty(filterDto.getMaterialCode()))
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("itemId")),
              "%" + filterDto.getMaterialCode().toLowerCase() + "%"));
    if (!StringUtils.isEmpty(filterDto.getMaterialDescription()))
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("itemDescription")),
              "%" + filterDto.getMaterialDescription().toLowerCase() + "%"));
    if (!StringUtils.isEmpty(filterDto.getMaterialGroup()) && filterDto.getMaterialGroup().size() > 0) {
    /*  predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("category")),
          "%" + filterDto.getMaterialGroup().toLowerCase() + "%"));*/
      predicates.add(root.<String>get("category").in(filterDto.getMaterialGroup()));
    }
    if (filterDto.getStatuses().size() > 0) {
      predicates.add(root.<String>get("appStatus").in(filterDto.getStatuses()));
    }

    /*Serach With updateUser*/
    if(!StringUtils.isEmpty(filterDto.getUpdateUser())){
      predicates.add(criteriaBuilder.equal(root.get("updateUser"), filterDto.getUpdateUser()));
    }

    //FromDispatch Date and ToDispatchDate
    if (!StringUtils.isEmpty(filterDto.getFromDispatchDate()) && !StringUtils.isEmpty(filterDto.getToDispatchDate())) {
      predicates.add(criteriaBuilder.and(
              criteriaBuilder.greaterThanOrEqualTo(root.get("dispatchDate"),
                      DateUtils.atStartOfDay(DateUtils.formatDate(filterDto.getFromDispatchDate(),
                              PLAN_RECORD_DATE_FORMAT))),
              criteriaBuilder.lessThanOrEqualTo(root.get("dispatchDate"), DateUtils.atEndOfDay(
                      DateUtils.formatDate(filterDto.getToDispatchDate(), PLAN_RECORD_DATE_FORMAT)))));
    }
    return predicates;
  }

  @Override
  public Page<IndentSummary> filterIndents(IndentFilterDto indentFilterDto, ApplicationUser loggedInUser) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<IndentSummary> criteriaQuery = criteriaBuilder.createQuery(IndentSummary.class);
    Root<IndentSummary> root = criteriaQuery.from(IndentSummary.class);
    root.alias("indentSummary");
    List<Predicate> predicates = getPredicatesForIndents(indentFilterDto, criteriaBuilder, root);

    criteriaQuery.multiselect(root.get("id"), root.get("indentId"), root.get("dispatchDate"), root.get("sourceLocation"), root.get("destinationLocation"), root.get("truckType"),
        root.get("loadFactor"), root.get("transporter"), root.get("category"), root.get("tte"), root.get("indented"), root.get("cancelled"),
        root.get("netRequested"), root.get("transConfirmed"), root.get("transDeclined"), root.get("transAssigned"), root.get("reported"), root.get("rejected"), root.get("netPlaced"),
        root.get("netBalance"), root.get("status"), root.get("comments"), root.get("indentAging"), root.get("insertUser"), root.get("updateUser"),
        root.get("insertDate"), root.get("updateDate"), root.get("isFreightAvailable"), root.get("destCountry"), root.get("pod"));

    criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
    criteriaQuery.orderBy(criteriaBuilder.desc(root.get("dispatchDate")));

//    Join<IndentSummary, MTLocation> join= root.join("MTLocation", JoinType.LEFT);
//    join.on(criteriaBuilder.equal(join.get("sourceLocation"), loggedInUser.getPlantCode()));

    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    Root<IndentSummary> iRoot = countQuery.from(criteriaQuery.getResultType());
    iRoot.alias("indentSummary");
    countQuery.select(criteriaBuilder.count(iRoot));
    Predicate restriction = criteriaQuery.getRestriction();
    if (restriction != null) {
      countQuery.where(restriction);
    }
    Long count = entityManager.createQuery(countQuery).getSingleResult();

    TypedQuery<IndentSummary> typedQuery = entityManager.createQuery(criteriaQuery);
    typedQuery.setFirstResult(indentFilterDto.getIndex() * indentFilterDto.getPageLength());
    typedQuery.setMaxResults(indentFilterDto.getPageLength());
    List<IndentSummary> indents = typedQuery.getResultList();
    return new PageImpl<>(indents, PageRequest.of(indentFilterDto.getIndex(), indentFilterDto.getPageLength()), count);
  }

  private List<Predicate> getPredicatesForIndents(IndentFilterDto indentFilterDto, CriteriaBuilder criteriaBuilder, Root<IndentSummary> root) {
    List<Predicate> predicates = new ArrayList<>();

    if (!StringUtils.isEmpty(indentFilterDto.getIndentID())) {

      Predicate p1 = criteriaBuilder.like(criteriaBuilder.lower(root.get("indentId")), "%" +indentFilterDto.getIndentID().toLowerCase());

      Predicate p2 = criteriaBuilder.like(criteriaBuilder.lower(root.get("indentId")), indentFilterDto.getIndentID().toLowerCase()+"%");

      predicates.add(criteriaBuilder.or(p1, p2));
    }

    if (!StringUtils.isEmpty(indentFilterDto.getSource())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("sourceLocation")), "%" + indentFilterDto.getSource().toLowerCase() + "%"));
    }
    if (!StringUtils.isEmpty(indentFilterDto.getTransporter())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("transporter")), "%" + indentFilterDto.getTransporter().toLowerCase() + "%"));
    }
    //FromDate and ToDate Predicates
    if (!StringUtils.isEmpty(indentFilterDto.getFromDispatchDate()) && !StringUtils.isEmpty(indentFilterDto.getToDispatchDate())) {
      predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("dispatchDate"),
              DateUtils.atStartOfDay(DateUtils.formatDate(indentFilterDto.getFromDispatchDate(), PLAN_RECORD_DATE_FORMAT))),
              criteriaBuilder.lessThanOrEqualTo(root.get("dispatchDate"), DateUtils.atEndOfDay(
                      DateUtils.formatDate(indentFilterDto.getToDispatchDate(), PLAN_RECORD_DATE_FORMAT)))));
    } /*else {

      if (indentFilterDto.getDispatchDate() != null && (DateUtils.isDateValid(indentFilterDto.getDispatchDate(), PLAN_RECORD_DATE_FORMAT))) {
        predicates.add(criteriaBuilder.and(
            criteriaBuilder.greaterThanOrEqualTo(root.get("dispatchDate"),
                DateUtils.atStartOfDay(DateUtils.formatDate(indentFilterDto.getDispatchDate(),
                    PLAN_RECORD_DATE_FORMAT))),
            criteriaBuilder.lessThanOrEqualTo(root.get("dispatchDate"), DateUtils.atEndOfDay(
                DateUtils.formatDate(indentFilterDto.getDispatchDate(), PLAN_RECORD_DATE_FORMAT)))));
      }
    }*/
    if (!StringUtils.isEmpty(indentFilterDto.getDestination())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("destinationLocation")), "%" + indentFilterDto.getDestination().toLowerCase() + "%"));
    }
    if (indentFilterDto.getTruckType() != null && indentFilterDto.getTruckType().size()>0) {
      //predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("truckType")), "%" + indentFilterDto.getTruckType().toLowerCase() + "%"));
			predicates.add(criteriaBuilder.and(root.get("truckType").in(indentFilterDto.getTruckType())));
    }
    if (!StringUtils.isEmpty(indentFilterDto.getIndentID())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("indentId")), "%" + indentFilterDto.getIndentID().toLowerCase() + "%"));
    }
    if (!StringUtils.isEmpty(indentFilterDto.getMaterialGrp()) && indentFilterDto.getMaterialGrp().size() > 0) {
//      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), "%" + indentFilterDto.getMaterialGrp().toLowerCase() + "%"));
      predicates.add(root.<String>get("category").in(indentFilterDto.getMaterialGrp()));
    }
    if (indentFilterDto.getStatus() != null && indentFilterDto.getStatus().size()>0) {
    	List<Constants.Status> statuses =indentFilterDto.getStatus().parallelStream().map(Constants.Status::valueOf).collect(Collectors.toList());
      //predicates.add(criteriaBuilder.equal(root.get("status"), Constants.Status.valueOf(indentFilterDto.getStatus())));
			predicates.add(criteriaBuilder.and(root.get("status").in(statuses)));
    }
    if(!StringUtils.isEmpty(indentFilterDto.getDestCountry())){
      predicates.add(criteriaBuilder.equal(root.get("destCountry"), indentFilterDto.getDestCountry()));
    }

    /*Filter using mutiple selection of status*/
    /*if ((!StringUtils.isEmpty(indentFilterDto.getStatus())) && indentFilterDto.getStatus().size() > 0) {
      List<Status> statusList = indentFilterDto.getStatus().parallelStream().map(Status::valueOf).collect(Collectors.toList());
      predicates.add(root.<String>get("status").in(statusList));
    }*/
    return predicates;
  }

  @Override
  public Page<TruckReport> filterReportedTrucks(TruckReportFilterDto truckReportFilterDto, ApplicationUser loggedInUser,
                                                List<Constants.TruckReportStatus> truckReportStatuses, String trucksType, List<String> shipmentIds) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<TruckReport> criteriaQuery = criteriaBuilder.createQuery(TruckReport.class);
    Root<TruckReport> root = criteriaQuery.from(TruckReport.class);
    root.alias("truckReport");
    List<Predicate> predicates = getPredicatesForTrucks(truckReportFilterDto, criteriaBuilder, root, loggedInUser, truckReportStatuses);
    // Inventory Trucks reporting location is user plant code
    if (trucksType.equals("INVENTORY_TRUCKS")) {
      predicates.add(criteriaBuilder.equal(root.get("reportLocation"), loggedInUser.getPlantCode()));
    }
    // Shipment Stop Shipment Ids
    if (shipmentIds != null && !shipmentIds.isEmpty()) {
      predicates.add(root.<String>get("shipmentID").in(shipmentIds));
    }

    criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
    criteriaQuery.orderBy(criteriaBuilder.desc(root.get("reportDate")));

    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    Root<TruckReport> iRoot = countQuery.from(criteriaQuery.getResultType());
    iRoot.alias("truckReport");
    countQuery.select(criteriaBuilder.count(iRoot));
    Predicate restriction = criteriaQuery.getRestriction();
    if (restriction != null) {
      countQuery.where(restriction);
    }
    Long count = entityManager.createQuery(countQuery).getSingleResult();

    TypedQuery<TruckReport> typedQuery = entityManager.createQuery(criteriaQuery);
    typedQuery.setFirstResult(truckReportFilterDto.getIndex() * truckReportFilterDto.getPageLength());
    typedQuery.setMaxResults(truckReportFilterDto.getPageLength());
    List<TruckReport> truckReports = typedQuery.getResultList();
    return new PageImpl<>(truckReports, PageRequest.of(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength()), count);
  }

  @Override
  public Page<TruckReport> filterTrucksInfo(TruckReportFilterDto truckReportFilterDto, List<Constants.TruckReportStatus> truckReportStatuses, List<String> incomingShipmentIds, ApplicationUser applicationUser, List<String> distinctShipmentIds) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<TruckReport> criteriaQuery = criteriaBuilder.createQuery(TruckReport.class);
    Root<TruckReport> root = criteriaQuery.from(TruckReport.class);
    root.alias("truckReport");

    List<Predicate> predicates = getPredicatesForTrucksInfo(truckReportFilterDto, criteriaBuilder, root, truckReportStatuses, incomingShipmentIds, applicationUser, distinctShipmentIds);

    criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
    criteriaQuery.orderBy(criteriaBuilder.desc(root.get("reportDate")));

    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    Root<TruckReport> iRoot = countQuery.from(criteriaQuery.getResultType());
    iRoot.alias("truckReport");
    countQuery.select(criteriaBuilder.count(iRoot));
    Predicate restriction = criteriaQuery.getRestriction();
    if (restriction != null) {
      countQuery.where(restriction);
    }
    Long count = entityManager.createQuery(countQuery).getSingleResult();
    TypedQuery<TruckReport> typedQuery = entityManager.createQuery(criteriaQuery);
    typedQuery.setFirstResult(truckReportFilterDto.getIndex() * truckReportFilterDto.getPageLength());
    typedQuery.setMaxResults(truckReportFilterDto.getPageLength());
    List<TruckReport> truckReports = typedQuery.getResultList();
    return new PageImpl<>(truckReports, PageRequest.of(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength()), count);

  }

  public Page<ExportShipment> filterExportShipmentData(ExportShipmentTractingDto exportShipmentBean){
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ExportShipment> criteriaQuery = criteriaBuilder.createQuery(ExportShipment.class);
    Root<ExportShipment> root = criteriaQuery.from(ExportShipment.class);
    root.alias("exportShipment");
    List<Predicate> predicates = getPredicatesForExportShipment(exportShipmentBean,criteriaBuilder,root);
    criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    Root<ExportShipment> iRoot = countQuery.from(criteriaQuery.getResultType());
    iRoot.alias("exportShipment");
    countQuery.select(criteriaBuilder.count(iRoot));
    Predicate restriction = criteriaQuery.getRestriction();
    if (restriction != null) {
      countQuery.where(restriction);
    }
    Long count = entityManager.createQuery(countQuery).getSingleResult();
    TypedQuery<ExportShipment> typedQuery = entityManager.createQuery(criteriaQuery);
    typedQuery.setFirstResult(exportShipmentBean.getIndex() * exportShipmentBean.getPageLength());
    typedQuery.setMaxResults(exportShipmentBean.getPageLength());
    List<ExportShipment> exportShipments = typedQuery.getResultList();
    return  new PageImpl<>(exportShipments,PageRequest.of(exportShipmentBean.getIndex(),exportShipmentBean.getPageLength()),count);
  }

  private  List<Predicate> getPredicatesForExportShipment(ExportShipmentTractingDto exportShipmentBean,CriteriaBuilder criteriaBuilder, Root<ExportShipment> root){
    List<Predicate> predicates = new ArrayList<>();

    if (!StringUtils.isEmpty(exportShipmentBean.getShipmentId())) {
      predicates.add(criteriaBuilder.equal(root.get("shipmentId"), exportShipmentBean.getShipmentId()));
    }
    if (!StringUtils.isEmpty(exportShipmentBean.getSourceLoc())) {
      predicates.add(criteriaBuilder.equal(root.get("sourceLoc"), exportShipmentBean.getSourceLoc()));
    }
    if (!StringUtils.isEmpty(exportShipmentBean.getPol())) {
      predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("pol")),  exportShipmentBean.getPol().toLowerCase()));
    }
    if (!StringUtils.isEmpty(exportShipmentBean.getPod())) {
      predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("pod")), exportShipmentBean.getPod().toLowerCase()));
    }
    if (!StringUtils.isEmpty(exportShipmentBean.getContainerNum())) {
      predicates.add(criteriaBuilder.equal(root.get("containerNum"),  exportShipmentBean.getContainerNum()));
    }
    if (!StringUtils.isEmpty(exportShipmentBean.getCustomerName())) {
      predicates.add(criteriaBuilder.equal(root.get("customerName"),  exportShipmentBean.getCustomerName()));
    }
    if (!StringUtils.isEmpty(exportShipmentBean.getForwarder())) {
      predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("forwarder")), exportShipmentBean.getForwarder().toLowerCase()));
    }
    if (!StringUtils.isEmpty(exportShipmentBean.getCha())) {
      predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("cha")), exportShipmentBean.getCha().toLowerCase() ));
    }
    if (!StringUtils.isEmpty(exportShipmentBean.getPiNum())) {
      predicates.add(criteriaBuilder.equal(root.get("piNum"),  exportShipmentBean.getPiNum()));
    }
    if (!StringUtils.isEmpty(exportShipmentBean.getSapInvoice())) {
      predicates.add(criteriaBuilder.equal(root.get("sapInvoice"),  exportShipmentBean.getSapInvoice() ));
    }
    if (!StringUtils.isEmpty(exportShipmentBean.getBookingNum())) {
      predicates.add(criteriaBuilder.equal(root.get("bookingNum"),  exportShipmentBean.getBookingNum()));
    }
    return predicates;
  }
  private List<Predicate> getPredicatesForTrucksInfo(TruckReportFilterDto truckReportFilterDto, CriteriaBuilder criteriaBuilder, Root<TruckReport> root, List<Constants.TruckReportStatus> truckReportStatuses, List<String> incomingShipmentIds, ApplicationUser applicationUser, List<String> distinctShipmentIds) {
    List<Predicate> predicates = new ArrayList<>();

    if (!StringUtils.isEmpty(truckReportFilterDto.getSource())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("sourceLocation")), "%" + truckReportFilterDto.getSource().toLowerCase() + "%"));
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getDestination())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("destinationLocation")), "%" + truckReportFilterDto.getDestination().toLowerCase() + "%"));
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getTransporter())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("servprov")), "%" + truckReportFilterDto.getTransporter().toLowerCase() + "%"));
    }
    if (truckReportFilterDto.getTruckType() !=null && truckReportFilterDto.getTruckType().size()>0) {
      //predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("reportedTruckType")), "%" + truckReportFilterDto.getTruckType().toLowerCase() + "%"));

			Predicate p1 = criteriaBuilder.and(criteriaBuilder.isNull(root.get("actualTruckType")),
					criteriaBuilder.and(root.get("truckType").in(truckReportFilterDto.getTruckType()))
			);
			Predicate p2 = criteriaBuilder.and(criteriaBuilder.isNotNull(root.get("actualTruckType")),
					criteriaBuilder.and(root.get("actualTruckType").in(truckReportFilterDto.getTruckType()))
			);

			predicates.add(criteriaBuilder.or(p1, p2));
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getTruckNumber())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("truck").get("truckNumber")), "%" + truckReportFilterDto.getTruckNumber().toLowerCase() + "%"));
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getIndentID())) {

      Predicate p1 = criteriaBuilder.like(criteriaBuilder.lower(root.get("indentSummary").get("indentId")), "%" +truckReportFilterDto.getIndentID().toLowerCase());

      Predicate p2 = criteriaBuilder.like(criteriaBuilder.lower(root.get("indentSummary").get("indentId")), truckReportFilterDto.getIndentID().toLowerCase()+ "%");

      predicates.add(criteriaBuilder.or(p1, p2));

      // predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("indentSummary").get("indentId")), "%" + truckReportFilterDto.getIndentID().toLowerCase() + "%"));
    }
    if (!truckReportFilterDto.getIndentCategoryList().isEmpty()){
      predicates.add(root.<String>get("indentSummary").get("category").in(truckReportFilterDto.getIndentCategoryList()));
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getActivity())) {
      // If activity is P then get trucks having activity is P or NULL
      if (truckReportFilterDto.getActivity().equals("P")) {
        predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("activity"), truckReportFilterDto.getActivity()), criteriaBuilder.isNull(root.get("activity"))));
      } else if (truckReportFilterDto.getActivity().equals("D")) {
        predicates.add(criteriaBuilder.equal(root.get("activity"), truckReportFilterDto.getActivity()));
      }
    }
    if (truckReportStatuses != null && !truckReportStatuses.isEmpty()) {
      predicates.add(root.<String>get("status").in(truckReportStatuses));
    }
    // Shipment Stop Shipment Ids
    if (incomingShipmentIds != null && !incomingShipmentIds.isEmpty()) {
      predicates.add(root.<String>get("shipmentID").in(incomingShipmentIds));
    }
    // In Intransit Menu,trucks report location should not equal to User LoggedIn plant code i.e here truckReportFilterDto.getReportLocation()
    /*DP_REP, L1_MGR, L2_MGR*/
    if (!StringUtils.isEmpty(truckReportFilterDto.getReportLocation()) && (!UserRole.getDPREPAccessRoles().contains(applicationUser.getRole()))) {
      if (truckReportFilterDto.getType().equalsIgnoreCase("INTRANSIT")) {
        predicates.add(criteriaBuilder.notEqual(root.get("reportLocation"), truckReportFilterDto.getReportLocation()));
      } else {
        predicates.add(criteriaBuilder.equal(root.get("reportLocation"), truckReportFilterDto.getReportLocation()));
      }
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getFromReportDate()) && !StringUtils.isEmpty(truckReportFilterDto.getToReportDate())) {
      predicates.add(criteriaBuilder.and(
              criteriaBuilder.greaterThanOrEqualTo(root.get("reportDate"),
                      DateUtils.atStartOfDay(DateUtils.formatDate(truckReportFilterDto.getFromReportDate(),
                              PLAN_RECORD_DATE_FORMAT))),
              criteriaBuilder.lessThanOrEqualTo(root.get("reportDate"), DateUtils.atEndOfDay(
                      DateUtils.formatDate(truckReportFilterDto.getToReportDate(), PLAN_RECORD_DATE_FORMAT)))));
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getFromGateOutDate()) && !StringUtils.isEmpty(truckReportFilterDto.getToGateOutDate())) {
      predicates.add(criteriaBuilder.and(
              criteriaBuilder.greaterThanOrEqualTo(root.get("gateOutDate"),
                      DateUtils.atStartOfDay(DateUtils.formatDate(truckReportFilterDto.getFromGateOutDate(),
                              PLAN_RECORD_DATE_FORMAT))),
              criteriaBuilder.lessThanOrEqualTo(root.get("gateOutDate"), DateUtils.atEndOfDay(
                      DateUtils.formatDate(truckReportFilterDto.getToGateOutDate(), PLAN_RECORD_DATE_FORMAT)))));
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getFromGateInDate()) && !StringUtils.isEmpty(truckReportFilterDto.getToGateInDate())) {
      predicates.add(criteriaBuilder.and(
              criteriaBuilder.greaterThanOrEqualTo(root.get("gateInDate"),
                      DateUtils.atStartOfDay(DateUtils.formatDate(truckReportFilterDto.getFromGateInDate(),
                              PLAN_RECORD_DATE_FORMAT))),
              criteriaBuilder.lessThanOrEqualTo(root.get("gateInDate"), DateUtils.atEndOfDay(
                      DateUtils.formatDate(truckReportFilterDto.getToGateInDate(), PLAN_RECORD_DATE_FORMAT)))));
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getShipmentID())) {
      predicates.add(criteriaBuilder.equal(root.get("shipmentID"), truckReportFilterDto.getShipmentID()));
    }

    if (!StringUtils.isEmpty(truckReportFilterDto.getDestCountry())){
      predicates.add(criteriaBuilder.equal(root.get("destCountry"), truckReportFilterDto.getDestCountry()));
    }

    if (truckReportFilterDto.getBayStatus() != null && truckReportFilterDto.getBayStatus().size()>0){
      List<Constants.BayStatus> bayStatuses = truckReportFilterDto.getBayStatus().parallelStream().map(Constants.BayStatus::valueOf).collect(Collectors.toList());
      predicates.add(criteriaBuilder.and(root.get("bayStatus").in(bayStatuses)));
    }

		if (truckReportFilterDto.getRejection() != null && truckReportFilterDto.getRejection().size()>0){
      List<Constants.TruckReportStatus> rejectedStatuses = truckReportFilterDto.getRejection().parallelStream().map(Constants.TruckReportStatus::valueOf).collect(Collectors.toList());
			predicates.add(criteriaBuilder.and(root.get("rejectedStatus").in(rejectedStatuses)));
		}

		/*To remove the distinct filter*/
		if (distinctShipmentIds != null && !distinctShipmentIds.isEmpty()){
      predicates.add(root.<String>get("shipmentID").in(distinctShipmentIds));
    }
    return predicates;
  }

  @Override
  public List<LoadslipMetaData> filterLoadslips(ApplicationUser loggedInUser, List<String> statuses, LoadslipFilterDto loadslipFilterDto) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<LoadslipMetaData> criteriaQuery = criteriaBuilder.createQuery(LoadslipMetaData.class);
    Root<Loadslip> loadslipTable = criteriaQuery.from(Loadslip.class);

    Join<Loadslip, Shipment> shipmentJoin = loadslipTable.join("shipment", JoinType.INNER);
    shipmentJoin.on(criteriaBuilder.equal(loadslipTable.get("shipment").get("shipmentId"), shipmentJoin.get("shipmentId")));

    //Join del-inv-header
    Join<Loadslip, LoadslipDetail> loadslipDetailJoin = loadslipTable.join("loadslipDetails", JoinType.INNER);
    loadslipDetailJoin.on(criteriaBuilder.and(criteriaBuilder.equal(loadslipDetailJoin.get("loadslip").get("loadslipId"), loadslipTable.get("loadslipId"))));

    Join<Loadslip, LoadslipDetailBom> loadslipDetailBomJoin = loadslipTable.join("loadslipDetailBoms", JoinType.INNER);
    loadslipDetailBomJoin.on(criteriaBuilder.and(criteriaBuilder.equal(loadslipDetailBomJoin.get("loadslip").get("loadslipId"), loadslipTable.get("loadslipId"))));

    loadslipFilterDto.setLoadslipsWithItems(true);
    List<Predicate> predicates = getPredicatesForLoadslips(loggedInUser, loadslipFilterDto, criteriaBuilder, loadslipTable, shipmentJoin, loadslipDetailJoin, loadslipDetailBomJoin, statuses);

    criteriaQuery.multiselect(loadslipTable.get("loadslipId"), shipmentJoin.get("shipmentId"), loadslipTable.get("qty"), loadslipTable.get("sourceLoc"), loadslipTable.get("destLoc"), loadslipTable.get("stoSoNum"),
        loadslipTable.get("delivery"), loadslipTable.get("sapInvoice"), loadslipTable.get("sapInvoiceDate"), loadslipTable.get("lrNum"), loadslipTable.get("lsprintDate"), loadslipTable.get("arrivedDate"), loadslipTable.get("lsDate"), loadslipTable.get("leDate"), loadslipTable.get("confirmDate"), loadslipTable.get("sendForBarcodeDate"), loadslipTable.get("totTyres"), loadslipTable.get("totTubes"), loadslipTable.get("totFlaps"),
        loadslipTable.get("totValve"), loadslipTable.get("totPctr"), loadslipTable.get("totQty"), loadslipTable.get("grn"), loadslipTable.get("status"), loadslipTable.get("tteUtil"), loadslipTable.get("weightUtil"), shipmentJoin.get("stopType"), criteriaBuilder.selectCase().when(shipmentJoin.get("actualTruckType").isNull(), shipmentJoin.get("truckType")).otherwise(shipmentJoin.get("actualTruckType")).alias("truckType"),
        shipmentJoin.get("truckNumber"), shipmentJoin.get("servprov"), shipmentJoin.get("freightAvailability"), shipmentJoin.get("startTime").alias("gatedOutDate"), loadslipTable.get("insertDate").alias("createdDate"),
        loadslipTable.get("tte").alias("tteQty"), loadslipTable.get("itemCategory").alias("itemCategory"), loadslipTable.get("releaseDate").alias("releaseDate"), loadslipTable.get("loadslipType").alias("type"),
        loadslipTable.get("grnDate").alias("grnDate"), loadslipTable.get("lrDate").alias("lrDate"), loadslipTable.get("dropSeq").alias("dropSeq"), loadslipTable.get("volumeUtil").alias("volumeUtil"), loadslipTable.get("integrationStatus"), loadslipTable.get("integrationMsg"), loadslipTable.get("comments").alias("comments"),
        loadslipTable.get("ditQty").alias("ditQty"), loadslipTable.get("shortQty").alias("shortQty"), loadslipTable.get("updateUser").alias("updateUser"), loadslipTable.get("insertUser").alias("insertUser"), loadslipTable.get("eWayBillNo").alias("eWayBillNo"), loadslipTable.get("sapInvValue").alias("sapInvValue"),
        loadslipTable.get("sapInvWeight").alias("sapInvWeight"), loadslipTable.get("otherQty").alias("otherQty"), shipmentJoin.get("transhipment").alias("transhipment"), shipmentJoin.get("containerNum").alias("containerNum"),
        loadslipTable.get("weight").alias("totWeight"), loadslipTable.get("weightUom").alias("weightUom"), shipmentJoin.get("destCountry").alias("destCountry"),
        loadslipTable.get("marketSegment").alias("marketSegment"), shipmentJoin.get("ftTripId").alias("FT_TRIP_ID"), loadslipTable.get("customInvoiceNumber").alias("CUSTOM_INV_NUMBER"), loadslipTable.get("trackingConsentStatus").alias("trackingConsentStatus"), loadslipTable.get("consentPhoneTelecom").alias("consentPhoneTelecom")).distinct(true);

    criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
    criteriaQuery.orderBy(criteriaBuilder.desc(loadslipTable.get("insertDate")));

  /*  CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    Root<LoadslipMetaData> iRoot = countQuery.from(criteriaQuery.getResultType());
    countQuery.select(criteriaBuilder.count(iRoot));
    Predicate restriction = criteriaQuery.getRestriction();
    if (restriction != null) {
      countQuery.where(restriction);
    }
    Long count = entityManager.createQuery(countQuery).getSingleResult();*/

    TypedQuery<LoadslipMetaData> typedQuery = entityManager.createQuery(criteriaQuery);
/*    int count = typedQuery.getResultList().size();
    typedQuery.setFirstResult(loadslipFilterDto.getIndex() * loadslipFilterDto.getPageLength());
    typedQuery.setMaxResults(loadslipFilterDto.getPageLength());*/
    return typedQuery.getResultList();
  }

  @Override
  public List<LoadslipMetaData> noInvoiceLoadslipFilter(ApplicationUser loggedInUser,List<String> statuses,LoadslipFilterDto loadslipFilterDto) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<LoadslipMetaData> criteriaQuery = criteriaBuilder.createQuery(LoadslipMetaData.class);
    Root<Loadslip> loadslipTable = criteriaQuery.from(Loadslip.class);



    Join<Loadslip, Shipment> shipmentJoin = loadslipTable.join("shipment", JoinType.INNER);
    shipmentJoin.on(criteriaBuilder.equal(loadslipTable.get("shipment").get("shipmentId"), shipmentJoin.get("shipmentId")));

    Join<Loadslip, LoadslipDetail> loadslipDetailJoin = loadslipTable.join("loadslipDetails", JoinType.LEFT);
    loadslipDetailJoin.on(criteriaBuilder.and(criteriaBuilder.equal(loadslipDetailJoin.get("loadslip").get("loadslipId"), loadslipTable.get("loadslipId"))));

    loadslipFilterDto.setLoadslipsWithItems(false);
    List<Predicate> predicates = getPredicatesForLoadslips( loggedInUser, loadslipFilterDto,  criteriaBuilder, loadslipTable,  shipmentJoin, loadslipDetailJoin, null,  statuses);
  /*  criteriaQuery.multiselect(loadslipTable.get("loadslipId"), shipmentJoin.get("shipmentId"), loadslipTable.get("qty"), loadslipTable.get("sourceLoc"), loadslipTable.get("destLoc"), shipmentJoin.get("truckNumber"), criteriaBuilder.selectCase().when(shipmentJoin.get("actualTruckType").isNull(), shipmentJoin.get("truckType")).otherwise(shipmentJoin.get("actualTruckType")).alias("truckType"),
        shipmentJoin.get("servprov"), shipmentJoin.get("freightAvailability"),loadslipTable.get("loadslipType").alias("type"),loadslipTable.get("dropSeq").alias("dropSeq"), shipmentJoin.get("transhipment").alias("transhipment"),shipmentJoin.get("stopType"),
        loadslipTable.get("lsprintDate"), loadslipTable.get("arrivedDate"), loadslipTable.get("lsDate"), loadslipTable.get("leDate"),
        loadslipTable.get("insertDate").alias("createdDate"),loadslipTable.get("updateUser").alias("updateUser"), loadslipTable.get("insertUser").alias("insertUser"),loadslipTable.get("comments").alias("comments"),
        shipmentJoin.get("containerNum")).distinct(true);*/
    criteriaQuery.multiselect(loadslipTable.get("loadslipId"), shipmentJoin.get("shipmentId"), loadslipTable.get("qty"), loadslipTable.get("sourceLoc"), loadslipTable.get("destLoc"), loadslipTable.get("stoSoNum"),
        loadslipTable.get("delivery"), loadslipTable.get("sapInvoice"), loadslipTable.get("sapInvoiceDate"), loadslipTable.get("lrNum"), loadslipTable.get("lsprintDate"), loadslipTable.get("arrivedDate"), loadslipTable.get("lsDate"), loadslipTable.get("leDate"), loadslipTable.get("confirmDate"), loadslipTable.get("sendForBarcodeDate"), loadslipTable.get("totTyres"), loadslipTable.get("totTubes"), loadslipTable.get("totFlaps"),
        loadslipTable.get("totValve"), loadslipTable.get("totPctr"), loadslipTable.get("totQty"), loadslipTable.get("grn"), loadslipTable.get("status"), loadslipTable.get("tteUtil"), loadslipTable.get("weightUtil"), shipmentJoin.get("stopType"), criteriaBuilder.selectCase().when(shipmentJoin.get("actualTruckType").isNull(), shipmentJoin.get("truckType")).otherwise(shipmentJoin.get("actualTruckType")).alias("truckType"),
        shipmentJoin.get("truckNumber"), shipmentJoin.get("servprov"), shipmentJoin.get("freightAvailability"), shipmentJoin.get("startTime").alias("gatedOutDate"), loadslipTable.get("insertDate").alias("createdDate"),
        loadslipTable.get("tte").alias("tteQty"), loadslipTable.get("itemCategory").alias("itemCategory"), loadslipTable.get("releaseDate").alias("releaseDate"), loadslipTable.get("loadslipType").alias("type"),
        loadslipTable.get("grnDate").alias("grnDate"), loadslipTable.get("lrDate").alias("lrDate"), loadslipTable.get("dropSeq").alias("dropSeq"), loadslipTable.get("volumeUtil").alias("volumeUtil"), loadslipTable.get("integrationStatus"), loadslipTable.get("integrationMsg"), loadslipTable.get("comments").alias("comments"),
        loadslipTable.get("ditQty").alias("ditQty"), loadslipTable.get("shortQty").alias("shortQty"), loadslipTable.get("updateUser").alias("updateUser"), loadslipTable.get("insertUser").alias("insertUser"), loadslipTable.get("eWayBillNo").alias("eWayBillNo"), loadslipTable.get("sapInvValue").alias("sapInvValue"),
        loadslipTable.get("sapInvWeight").alias("sapInvWeight"), loadslipTable.get("otherQty").alias("otherQty"), shipmentJoin.get("transhipment").alias("transhipment"), shipmentJoin.get("containerNum").alias("containerNum"),
        loadslipTable.get("weight").alias("totWeight"), loadslipTable.get("weightUom").alias("weightUom"), shipmentJoin.get("destCountry").alias("destCountry"),
        loadslipTable.get("marketSegment").alias("marketSegment"), shipmentJoin.get("ftTripId").alias("FT_TRIP_ID"), loadslipTable.get("customInvoiceNumber").alias("CUSTOM_INV_NUMBER")
            , loadslipTable.get("trackingConsentStatus").alias("trackingConsentStatus"), loadslipTable.get("consentPhoneTelecom").alias("consentPhoneTelecom")).distinct(true);

    criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
//    criteriaQuery.orderBy(criteriaBuilder.desc(loadslipTable.get("insertDate")));
    TypedQuery<LoadslipMetaData> typedQuery = entityManager.createQuery(criteriaQuery);
    int count = typedQuery.getResultList().size();
    /*typedQuery.setFirstResult(loadslipFilterDto.getIndex() * loadslipFilterDto.getPageLength());
    typedQuery.setMaxResults(loadslipFilterDto.getPageLength());*/
    return typedQuery.getResultList();
  }

 /* @Override
  public Page<LoadslipMetaData> filterLoadslipsMovement(ApplicationUser loggedInUser, List<String> statuses, LoadslipFilterDto loadslipFilterDto) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<LoadslipMetaData> criteriaQuery = criteriaBuilder.createQuery(LoadslipMetaData.class);
    Root<Loadslip> loadslipTable = criteriaQuery.from(Loadslip.class);

    Root<TruckReport> truckReportRoot = criteriaQuery.from(TruckReport.class);
    Predicate truckPredicate= criteriaBuilder.equal(truckReportRoot.get("shipmentID"),loadslipTable.get("shipment").get("shipmentId"));

    Join<Loadslip, Shipment> shipmentJoin = loadslipTable.join("shipment", JoinType.INNER);

    shipmentJoin.on(criteriaBuilder.equal(loadslipTable.get("shipment").get("shipmentId"), shipmentJoin.get("shipmentId")));

    //Join del-inv-header
    Join<Loadslip, LoadslipDetail> loadslipDetailJoin = loadslipTable.join("loadslipDetails", JoinType.INNER);
    loadslipDetailJoin.on(criteriaBuilder.and(criteriaBuilder.equal(loadslipDetailJoin.get("loadslip").get("loadslipId"), loadslipTable.get("loadslipId"))));

    Join<Loadslip, LoadslipDetailBom> loadslipDetailBomJoin = loadslipTable.join("loadslipDetailBoms", JoinType.INNER);
    loadslipDetailBomJoin.on(criteriaBuilder.and(criteriaBuilder.equal(loadslipDetailBomJoin.get("loadslip").get("loadslipId"), loadslipTable.get("loadslipId"))));


    List<Predicate> predicates= getPredicatesForLoadslips(loggedInUser, loadslipFilterDto, criteriaBuilder, loadslipTable, shipmentJoin, loadslipDetailJoin, loadslipDetailBomJoin, statuses);
    predicates.add(truckPredicate);
    criteriaQuery.multiselect(loadslipTable.get("loadslipId"), shipmentJoin.get("shipmentId"), loadslipTable.get("qty"), loadslipTable.get("sourceLoc"), loadslipTable.get("destLoc"), loadslipTable.get("stoSoNum"),
        loadslipTable.get("delivery"), loadslipTable.get("sapInvoice"), loadslipTable.get("sapInvoiceDate"), loadslipTable.get("lrNum"), loadslipTable.get("lsprintDate"), loadslipTable.get("arrivedDate"), loadslipTable.get("lsDate"), loadslipTable.get("leDate"), loadslipTable.get("confirmDate"), loadslipTable.get("sendForBarcodeDate"), loadslipTable.get("totTyres"), loadslipTable.get("totTubes"), loadslipTable.get("totFlaps"),
        loadslipTable.get("totValve"), loadslipTable.get("totPctr"), loadslipTable.get("totQty"), loadslipTable.get("grn"), loadslipTable.get("status"), loadslipTable.get("tteUtil"), loadslipTable.get("weightUtil"), shipmentJoin.get("stopType"), criteriaBuilder.selectCase().when(shipmentJoin.get("actualTruckType").isNull(), shipmentJoin.get("truckType")).otherwise(shipmentJoin.get("actualTruckType")).alias("truckType"),
        shipmentJoin.get("truckNumber"), shipmentJoin.get("servprov"), shipmentJoin.get("freightAvailability"), shipmentJoin.get("startTime").alias("gatedOutDate"), loadslipTable.get("insertDate").alias("createdDate"),
        loadslipTable.get("tte").alias("tteQty"), loadslipTable.get("itemCategory").alias("itemCategory"), loadslipTable.get("releaseDate").alias("releaseDate"), loadslipTable.get("loadslipType").alias("type"),
        loadslipTable.get("grnDate").alias("grnDate"), loadslipTable.get("lrDate").alias("lrDate"), loadslipTable.get("dropSeq").alias("dropSeq"), loadslipTable.get("volumeUtil").alias("volumeUtil"), loadslipTable.get("integrationStatus"), loadslipTable.get("integrationMsg"), loadslipTable.get("comments").alias("comments"),
        loadslipTable.get("ditQty").alias("ditQty"), loadslipTable.get("shortQty").alias("shortQty"), loadslipTable.get("updateUser").alias("updateUser"), loadslipTable.get("insertUser").alias("insertUser"),truckReportRoot.get("gateInDate").alias("gateInDate"),truckReportRoot.get("gateOutDate").alias("gateOutDate"),truckReportRoot.get("reportDate").alias("reportDate"),truckReportRoot.get("inWeight").alias("inWeight"),
        truckReportRoot.get("outWeight").alias("outWeight"),truckReportRoot.get("driverName").alias("driverName"),truckReportRoot.get("driverMobile").alias("driverMobile"),truckReportRoot.get("driverLicense").alias("driverLicense"), loadslipTable.get("eWayBillNo").alias("eWayBillNo"), loadslipTable.get("sapInvValue").alias("sapInvValue"), loadslipTable.get("sapInvWeight").alias("sapInvWeight")).distinct(true);

    criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
    criteriaQuery.orderBy(criteriaBuilder.desc(loadslipTable.get("insertDate")));

  *//*  CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    Root<LoadslipMetaData> iRoot = countQuery.from(criteriaQuery.getResultType());
    countQuery.select(criteriaBuilder.count(iRoot));
    Predicate restriction = criteriaQuery.getRestriction();
    if (restriction != null) {
      countQuery.where(restriction);
    }
    Long count = entityManager.createQuery(countQuery).getSingleResult();*//*
    TypedQuery<LoadslipMetaData> typedQuery = entityManager.createQuery(criteriaQuery);
    int count = typedQuery.getResultList().size();
    typedQuery.setFirstResult(loadslipFilterDto.getIndex() * loadslipFilterDto.getPageLength());
    typedQuery.setMaxResults(loadslipFilterDto.getPageLength());
    return new PageImpl<>(typedQuery.getResultList(), PageRequest.of(loadslipFilterDto.getIndex(), loadslipFilterDto.getPageLength()), count);
  }*/


  @Override
  public Page<LoadslipMetaData> filterLoadslipsMovement(ApplicationUser loggedInUser, List<String> statuses, LoadslipFilterDto loadslipFilterDto) {

    StringBuilder sb = setQueryForFilterLoadslipsMovement(loggedInUser, loadslipFilterDto);

    Query q = entityManager.unwrap(Session.class).createNativeQuery(sb.toString())
        .addScalar("loadslipId", StandardBasicTypes.STRING).addScalar("shipmentId", StandardBasicTypes.STRING).addScalar("qty", StandardBasicTypes.INTEGER).addScalar("sourceLoc", StandardBasicTypes.STRING).addScalar("destLoc", StandardBasicTypes.STRING).addScalar("stoSoNum", StandardBasicTypes.STRING)

        .addScalar("delivery", StandardBasicTypes.STRING).addScalar("sapInvoice", StandardBasicTypes.STRING).addScalar("sapInvoiceDate", StandardBasicTypes.DATE).addScalar("lrNum", StandardBasicTypes.STRING).addScalar("lsPrintDate", StandardBasicTypes.DATE).addScalar("bayArrivedDate", StandardBasicTypes.DATE).addScalar("loadingStartDate", StandardBasicTypes.DATE).addScalar("loadingEndDate", StandardBasicTypes.DATE).addScalar("confirmedDate", StandardBasicTypes.DATE)

        .addScalar("sendForBarcodeDate", StandardBasicTypes.DATE).addScalar("totTyres", StandardBasicTypes.INTEGER).addScalar("totTubes", StandardBasicTypes.INTEGER).addScalar("totTubes", StandardBasicTypes.INTEGER).addScalar("totFlaps", StandardBasicTypes.INTEGER).addScalar("totValve", StandardBasicTypes.INTEGER).addScalar("totPctr", StandardBasicTypes.INTEGER).addScalar("totQty", StandardBasicTypes.INTEGER)

        .addScalar("grn", StandardBasicTypes.STRING).addScalar("status", StandardBasicTypes.STRING).addScalar("tteUtil", StandardBasicTypes.DOUBLE).addScalar("weightUtil", StandardBasicTypes.DOUBLE).addScalar("stopType", StandardBasicTypes.STRING).addScalar("truckType", StandardBasicTypes.STRING).addScalar("truckNumber", StandardBasicTypes.STRING).addScalar("servprov", StandardBasicTypes.STRING)

        .addScalar("freightAvailability", StandardBasicTypes.STRING).addScalar("gatedOutDate", StandardBasicTypes.DATE).addScalar("createdDate", StandardBasicTypes.DATE).addScalar("tteQty", StandardBasicTypes.DOUBLE).addScalar("itemCategory", StandardBasicTypes.STRING).addScalar("releaseDate", StandardBasicTypes.DATE).addScalar("type", StandardBasicTypes.STRING)

        .addScalar("grnDate", StandardBasicTypes.DATE).addScalar("lrDate", StandardBasicTypes.DATE).addScalar("dropSeq", StandardBasicTypes.INTEGER).addScalar("volumeUtil", StandardBasicTypes.DOUBLE).addScalar("integrationStatus", StandardBasicTypes.STRING).addScalar("integrationMsg", StandardBasicTypes.STRING).addScalar("comments", StandardBasicTypes.STRING).addScalar("ditQty", StandardBasicTypes.INTEGER).addScalar("containerNum",StandardBasicTypes.STRING)

        .addScalar("shortQty", StandardBasicTypes.INTEGER).addScalar("updateUser", StandardBasicTypes.STRING).addScalar("insertUser", StandardBasicTypes.STRING).addScalar("gateInDate", StandardBasicTypes.DATE).addScalar("gateOutDate", StandardBasicTypes.DATE).addScalar("reportDate", StandardBasicTypes.DATE).addScalar("inWeight", StandardBasicTypes.DOUBLE).addScalar("outWeight", StandardBasicTypes.DOUBLE).addScalar("invoiceCnt",StandardBasicTypes.INTEGER)

        .addScalar("driverName", StandardBasicTypes.STRING).addScalar("driverMobile", StandardBasicTypes.STRING).addScalar("driverLicense", StandardBasicTypes.STRING).addScalar("eWayBillNo", StandardBasicTypes.STRING).addScalar("sapInvValue", StandardBasicTypes.INTEGER).addScalar("sapInvWeight", StandardBasicTypes.INTEGER).addScalar("otherQty", StandardBasicTypes.STRING).addScalar("transhipment", StandardBasicTypes.STRING).addScalar("loadslipcomments", StandardBasicTypes.STRING)
				.addScalar("destCountry",StandardBasicTypes.STRING).addScalar("totalWeight",StandardBasicTypes.DOUBLE).addScalar("weightUom",StandardBasicTypes.STRING).addScalar("marketSegment", StandardBasicTypes.STRING)
        .addScalar("ftTripId", StandardBasicTypes.STRING).addScalar("customInvoiceNumber", StandardBasicTypes.STRING);

//    PaginationViewBean pageViewBean = new PaginationViewBean();
    int totalElement = q.getResultList().size();
    q.setFirstResult((loadslipFilterDto.getIndex() * loadslipFilterDto.getPageLength()));
    q.setMaxResults(loadslipFilterDto.getPageLength());
    List<LoadslipMetaData> loadslipMetaData = ((NativeQuery) q)
        .setResultTransformer(new AliasToBeanResultTransformer(LoadslipMetaData.class)).getResultList();

   /* TypedQuery<LoadslipMetaData> typedQuery = entityManager.createQuery(criteriaQuery);
    int count = typedQuery.getResultList().size();
    typedQuery.setFirstResult(loadslipFilterDto.getIndex() * loadslipFilterDto.getPageLength());
    typedQuery.setMaxResults(loadslipFilterDto.getPageLength());*/
    return new PageImpl<>(loadslipMetaData, PageRequest.of(loadslipFilterDto.getIndex(), loadslipFilterDto.getPageLength()), totalElement);
  }

  private StringBuilder setQueryForFilterLoadslipsMovement(ApplicationUser loggedInUser, LoadslipFilterDto loadslipFilterDto) {
    StringBuilder status = new StringBuilder();
    int count = 1;
    for (String status1 : loadslipFilterDto.getStatus()) {
      status.append("'");
      status.append(status1);
      status.append("'");
      if (count != loadslipFilterDto.getStatus().size()) {
        status.append(",");
      }
      count++;
    }
    /*DP_REP, L1_MGR, L2_MGR*/
   if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())){
     StringBuilder sb = new StringBuilder("  select distinct ls.*, tr.gatein_date as gateInDate, tr.gateout_date as gateOutDate,tr.reporting_date as reportDate,tr.in_weight as inWeight,tr.out_weight as outWeight,tr.driver_name as driverName,tr.driver_mobile as driverMobile,tr.driver_license as driverLicense,tr.COMMENTS as comments  from ( " +
         "   select lp.LOADSLIP_ID as loadslipId,lp.SHIPMENT_ID as shipmentId,lp.qty as qty,lp.SOURCE_LOC as sourceLoc,lp.DEST_LOC as destLoc, lp.STO_SO_NUM as stoSoNum,lp.DELIVERY as delivery, lp.SAP_INVOICE as sapInvoice, " +
         "   lp.SAP_INVOICE_DATE as sapInvoiceDate,lp.LR_NUM as lrNum, lp.lsprint_date as lsPrintDate,lp.arrived_date as bayArrivedDate,lp.LS_DATE as loadingStartDate ,lp.LE_DATE as loadingEndDate, lp.CONFIRM_DATE as confirmedDate,  " +
         "   lp.send_for_barcode_date  as sendForBarcodeDate,lp.TOT_TYRES as totTyres, lp.TOT_TUBES as totTubes, lp.TOT_FLAPS as totFlaps,lp.TOT_VALVE as totValve,lp.tot_pctr as totPctr,lp.tot_qty as totQty, " +
         "   lp.GRN as grn,lp.STATUS as status,lp.TTE_UTIL as tteUtil,lp.WEIGHT_UTIL as weightUtil,shipment.STOP_TYPE as stopType,(CASE WHEN shipment.ACTUAL_TRUCK_TYPE IS NULL THEN shipment.TRUCK_TYPE ELSE shipment.ACTUAL_TRUCK_TYPE  END) as truckType, " +
         "   shipment.TRUCK_NUMBER as truckNumber,shipment.SERVPROV as servprov,shipment.FRT_AVAIL_FLAG as freightAvailability,shipment.START_TIME as gatedOutDate, lp.INSERT_DATE as createdDate,lp.TTE_QTY as tteQty,  " +
         "   lp.ITEM_CATEGORY as itemCategory,lp.RELEASE_DATE as releaseDate,lp.LOADSLIP_TYPE as type,lp.grn_date as grnDate,lp.lr_date as lrDate,lp.drop_seq as dropSeq,lp.volume_util as volumeUtil,lp.INT_STATUS as integrationStatus , " +
         "   lp.INT_MESSAGE as integrationMsg,lp.COMMENTS as loadslipcomments,lp.DIT_QTY as ditQty,lp.SHORT_QTY as shortQty ,lp.UPDATE_USER as updateUser, lp.INSERT_USER as insertUser,lp.E_WAY_BILL_NO as eWayBillNo ,lp.SAP_INV_VALUE as sapInvValue , " +
         "   lp.SAP_INV_WEIGHT as sapInvWeight ,lp.other as otherQty ,shipment.TRANSHIPMENT as transhipment ,(CASE WHEN lp.LOADSLIP_TYPE in('FGS_EXP', 'JIT_OEM') THEN DHI.dhi_count  ELSE LIH.dhi_count END) as invoiceCnt, shipment.CONTAINER_NUM AS containerNum," +
         "   shipment.DEST_COUNTRY as destCountry,lp.WEIGHT as totalWeight, lp.WEIGHT_UOM as weightUom, lp.MKT_SEG as marketSegment, shipment.ft_trip_id as ftTripId, lp.CUSTOM_INV_NUMBER as customInvoiceNumber  " +
         "  from LOADSLIP lp INNER JOIN Shipment shipment on lp.shipment_id = shipment.shipment_id  " +
         "  left join (select distinct count(1) as dhi_count, dih.loadslip_id from del_inv_header dih group by dih.loadslip_id ) DHI on lp.loadslip_id = DHI.loadslip_id " +
         "  left join (select distinct count(1) as dhi_count, dih.loadslip_id from loadslip_inv_header dih group by dih.loadslip_id) LIH on lp.loadslip_id = LIH.loadslip_id ) ls left join  truck_reporting tr on ls.shipmentId = tr.SHIPMENT_ID and tr.reporting_location = ls.sourceLoc  left join LOADSLIP_DETAIL_BOM ldb on ldb.LOADSLIP_ID=ls.loadslipId   " +
         "  left join loadslip_detail ld on ld.LOADSLIP_ID=ls.loadslipId where  ( (ls.status in  (" + status + " ) and ls.type not in ( 'FGS_EXP', 'JIT_OEM')) OR (ls.status in(" + status + " ) and ls.type  in ( 'FGS_EXP', 'JIT_OEM'))) ");
         /*if(!StringUtils.isEmpty(loadslipFilterDto.getSource())){
      sb.append(" ls.sourceLoc = '" +loggedInUser.getPlantCode()+"'" );
    }*/
      if (!StringUtils.isEmpty(loadslipFilterDto.getLoadslipId())) {
//        sb.append("and ls.loadslipId = '" + loadslipFilterDto.getLoadslipId() + "'");
        sb.append("and LOWER(ls.loadslipId) LIKE '%" + loadslipFilterDto.getLoadslipId().toLowerCase() + "%'");
      }
   /* if (!StringUtils.isEmpty(loadslipFilterDto.getStopType())) {
      sb.append("  ld.STOP_TYPE = '" +  loadslipFilterDto.getStopType() + "'" );
    }*/
      if (!StringUtils.isEmpty(loadslipFilterDto.getItemId())) {
        sb.append("and   ld.ITEM_ID = '" + loadslipFilterDto.getItemId() + "'");
      }
      if (!StringUtils.isEmpty(loadslipFilterDto.getDestination())) {
        sb.append("and   ls.destLoc = '" + loadslipFilterDto.getDestination() + "'");
      }
      if (!StringUtils.isEmpty(loadslipFilterDto.getShipmentId())) {
        sb.append("and   ls.shipmentId = '" + loadslipFilterDto.getShipmentId() + "'");
      }
      if (loadslipFilterDto.getTruckType() != null && loadslipFilterDto.getTruckType().size()>0) {
        sb.append("and   ls.truckType in (" + Utility.join(loadslipFilterDto.getTruckType()) + ") ");
      }
    /*if (!StringUtils.isEmpty(loadslipFilterDto.getTranshipment())) {
      sb.append("  shipment.TRANSHIPMENT = '" +loadslipFilterDto.getTranshipment() +"'");
    }*/
     if (loadslipFilterDto.getLsStatus()!=null && loadslipFilterDto.getLsStatus().size()>0) {
       //sb.append("and   ls.status = '" + loadslipFilterDto.getLsStatus() + "'");
       sb.append(("and ls.status  in (" + Utility.join(loadslipFilterDto.getLsStatus()) + ") "));
     }
      if (!StringUtils.isEmpty(loadslipFilterDto.getInvoice())) {
        sb.append("and   ls.sapInvoice = '" + loadslipFilterDto.getInvoice() + "'");
      }
      if (!StringUtils.isEmpty(loadslipFilterDto.getStopType())) {
        sb.append("and   ls.stopType = '" + loadslipFilterDto.getStopType() + "'");
      }
      if (!StringUtils.isEmpty(loadslipFilterDto.getFromCreatedDate()) && !StringUtils.isEmpty(loadslipFilterDto.getToCreatedDate())) {
/*
      sb.append("  ls.createdDate   >='" +DateUtils.formatDate(DateUtils.setTimeToMidnight(DateUtils.formatDate(loadslipFilterDto.getFromCreatedDate(),PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) +"' and ls.createdDate <= '"+DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(loadslipFilterDto.getToCreatedDate(),PLAN_RECORD_DATE_FORMAT)),Constants.DATE_FORMATE_WITH_HYPHEN)+"'");
*/
        sb.append("and  ls.createdDate   >=TO_DATE('" + loadslipFilterDto.getFromCreatedDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
        sb.append(" and ls.createdDate <= TO_DATE('" + DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(loadslipFilterDto.getToCreatedDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      }

    /* if (!StringUtils.isEmpty(loadslipFilterDto.getMarketSegment())) {
       Map<String, String> marketSegMap = Utility.deriveTubeAndFlapBatchCodes();
       //Here tube and flap batch codes are same based on MarketSegment
       String tubeOrFlapBatchCode = marketSegMap.get(loadslipFilterDto.getMarketSegment());
       sb.append("and   ldb.TUBE_BATCH = '" + tubeOrFlapBatchCode + "'");
     }*/

    if (loadslipFilterDto.getMarketSegment() != null && loadslipFilterDto.getMarketSegment().size()>0){
      sb.append(" and ls.marketSegment  in (" + Utility.join(loadslipFilterDto.getMarketSegment()) + ") ");
    }

      if (!StringUtils.isEmpty(loadslipFilterDto.getTruckNumber())) {
        sb.append("and   ls.truckNumber = '" + loadslipFilterDto.getTruckNumber() + "'");
      }

      if (!StringUtils.isEmpty(loadslipFilterDto.getContainerNum())) {
        sb.append("and   ls.containerNum = '" + loadslipFilterDto.getContainerNum() + "'");
      }
      if (!StringUtils.isEmpty(loadslipFilterDto.getSource())){
        sb.append("and   ls.sourceLoc = '" + loadslipFilterDto.getSource() + "'");
      }

		 if (!StringUtils.isEmpty(loadslipFilterDto.getDestCountry())) {
			 sb.append("and   ls.destCountry = '" + loadslipFilterDto.getDestCountry() + "'");
		 }

		 if (!StringUtils.isEmpty(loadslipFilterDto.getInsertUser())){
       sb.append("and   ls.insertUser = '" + loadslipFilterDto.getInsertUser() + "'");
     }

     if (!StringUtils.isEmpty(loadslipFilterDto.getTransporter())) {
       sb.append("and   ls.servprov = '" + loadslipFilterDto.getTransporter() + "'");
     }
     if(loadslipFilterDto.getItemCategories() != null && !loadslipFilterDto.getItemCategories().isEmpty()){
       sb.append(" AND ls.itemCategory IN ("+Utility.join(loadslipFilterDto.getItemCategories())+") ");
     }

     sb.append("  ORDER BY ls.createdDate DESC");

     return sb;
   } else{
     StringBuilder sb = new StringBuilder("  select distinct ls.*, tr.gatein_date as gateInDate, tr.gateout_date as gateOutDate,tr.reporting_date as reportDate,tr.in_weight as inWeight,tr.out_weight as outWeight,tr.driver_name as driverName,tr.driver_mobile as driverMobile,tr.driver_license as driverLicense,tr.COMMENTS as comments  from ( " +
         "   select lp.LOADSLIP_ID as loadslipId,lp.SHIPMENT_ID as shipmentId,lp.qty as qty,lp.SOURCE_LOC as sourceLoc,lp.DEST_LOC as destLoc, lp.STO_SO_NUM as stoSoNum,lp.DELIVERY as delivery, lp.SAP_INVOICE as sapInvoice, " +
         "   lp.SAP_INVOICE_DATE as sapInvoiceDate,lp.LR_NUM as lrNum, lp.lsprint_date as lsPrintDate,lp.arrived_date as bayArrivedDate,lp.LS_DATE as loadingStartDate ,lp.LE_DATE as loadingEndDate, lp.CONFIRM_DATE as confirmedDate,  " +
         "   lp.send_for_barcode_date  as sendForBarcodeDate,lp.TOT_TYRES as totTyres, lp.TOT_TUBES as totTubes, lp.TOT_FLAPS as totFlaps,lp.TOT_VALVE as totValve,lp.tot_pctr as totPctr,lp.tot_qty as totQty, " +
         "   lp.GRN as grn,lp.STATUS as status,lp.TTE_UTIL as tteUtil,lp.WEIGHT_UTIL as weightUtil,shipment.STOP_TYPE as stopType,(CASE WHEN shipment.ACTUAL_TRUCK_TYPE IS NULL THEN shipment.TRUCK_TYPE ELSE shipment.ACTUAL_TRUCK_TYPE  END) as truckType, " +
         "   shipment.TRUCK_NUMBER as truckNumber,shipment.SERVPROV as servprov,shipment.FRT_AVAIL_FLAG as freightAvailability,shipment.START_TIME as gatedOutDate, lp.INSERT_DATE as createdDate,lp.TTE_QTY as tteQty,  " +
         "   lp.ITEM_CATEGORY as itemCategory,lp.RELEASE_DATE as releaseDate,lp.LOADSLIP_TYPE as type,lp.grn_date as grnDate,lp.lr_date as lrDate,lp.drop_seq as dropSeq,lp.volume_util as volumeUtil,lp.INT_STATUS as integrationStatus , " +
         "   lp.INT_MESSAGE as integrationMsg,lp.COMMENTS as loadslipcomments,lp.DIT_QTY as ditQty,lp.SHORT_QTY as shortQty ,lp.UPDATE_USER as updateUser, lp.INSERT_USER as insertUser,lp.E_WAY_BILL_NO as eWayBillNo ,lp.SAP_INV_VALUE as sapInvValue , " +
         "   lp.SAP_INV_WEIGHT as sapInvWeight ,lp.other as otherQty ,shipment.TRANSHIPMENT as transhipment ,(CASE WHEN lp.LOADSLIP_TYPE in('FGS_EXP', 'JIT_OEM') THEN DHI.dhi_count  ELSE LIH.dhi_count END) as invoiceCnt ,shipment.CONTAINER_NUM AS containerNum," +
         "   shipment.DEST_COUNTRY as destCountry,lp.WEIGHT as totalWeight, lp.WEIGHT_UOM as weightUom, lp.MKT_SEG as marketSegment,shipment.ft_trip_id as ftTripId, lp.CUSTOM_INV_NUMBER as customInvoiceNumber   " +
         "  from LOADSLIP lp INNER JOIN Shipment shipment on lp.shipment_id = shipment.shipment_id  and ( (lp.STATUS in  (" + status + " ) and lp.LOADSLIP_TYPE not in ( 'FGS_EXP', 'JIT_OEM')) OR (lp.STATUS in ( " + status + " ) and lp.LOADSLIP_TYPE  in ( 'FGS_EXP', 'JIT_OEM'))) " +
         "  left join (select distinct count(1) as dhi_count, dih.loadslip_id from del_inv_header dih group by dih.loadslip_id ) DHI on lp.loadslip_id = DHI.loadslip_id " +
         "  left join (select distinct count(1) as dhi_count, dih.loadslip_id from loadslip_inv_header dih group by dih.loadslip_id) LIH on lp.loadslip_id = LIH.loadslip_id ) ls left join  truck_reporting tr on ls.shipmentId = tr.SHIPMENT_ID AND ( tr.reporting_location = '"+loggedInUser.getPlantCode()+"' OR tr.reporting_location IS NULL ) left join LOADSLIP_DETAIL_BOM ldb on ldb.LOADSLIP_ID=ls.loadslipId   " +
         "  left join loadslip_detail ld on ld.LOADSLIP_ID=ls.loadslipId where ls.sourceLoc ='" + loggedInUser.getPlantCode() + "'  ");
         /*if(!StringUtils.isEmpty(loadslipFilterDto.getSource())){
      sb.append(" ls.sourceLoc = '" +loggedInUser.getPlantCode()+"'" );
    }*/
      if (!StringUtils.isEmpty(loadslipFilterDto.getLoadslipId())) {
//        sb.append("and ls.loadslipId = '" + loadslipFilterDto.getLoadslipId() + "'");
        sb.append("and LOWER(ls.loadslipId) LIKE '%" + loadslipFilterDto.getLoadslipId().toLowerCase() + "%'");
      }
   /* if (!StringUtils.isEmpty(loadslipFilterDto.getStopType())) {
      sb.append("  ld.STOP_TYPE = '" +  loadslipFilterDto.getStopType() + "'" );
    }*/
      if (!StringUtils.isEmpty(loadslipFilterDto.getItemId())) {
        sb.append("and   ld.ITEM_ID = '" + loadslipFilterDto.getItemId() + "'");
      }
      if (!StringUtils.isEmpty(loadslipFilterDto.getDestination())) {
        sb.append("and   ls.destLoc = '" + loadslipFilterDto.getDestination() + "'");
      }
      if (!StringUtils.isEmpty(loadslipFilterDto.getShipmentId())) {
        sb.append("and   ls.shipmentId = '" + loadslipFilterDto.getShipmentId() + "'");
      }
     if (loadslipFilterDto.getTruckType() != null && loadslipFilterDto.getTruckType().size()>0) {
       sb.append("and   ls.truckType in (" + Utility.join(loadslipFilterDto.getTruckType()) + ") ");
     }
    /*if (!StringUtils.isEmpty(loadslipFilterDto.getTranshipment())) {
      sb.append("  shipment.TRANSHIPMENT = '" +loadslipFilterDto.getTranshipment() +"'");
    }*/
      if (loadslipFilterDto.getLsStatus()!=null && loadslipFilterDto.getLsStatus().size()>0) {
        //sb.append("and   ls.status = '" + loadslipFilterDto.getLsStatus() + "'");
        sb.append(("and ls.status  in (" + Utility.join(loadslipFilterDto.getLsStatus()) + ") "));
      }
      if (!StringUtils.isEmpty(loadslipFilterDto.getInvoice())) {
        sb.append("and   ls.sapInvoice = '" + loadslipFilterDto.getInvoice() + "'");
      }
      if (!StringUtils.isEmpty(loadslipFilterDto.getStopType())) {
        sb.append("and   ls.stopType = '" + loadslipFilterDto.getStopType() + "'");
      }
      if (!StringUtils.isEmpty(loadslipFilterDto.getFromCreatedDate()) && !StringUtils.isEmpty(loadslipFilterDto.getToCreatedDate())) {
/*
      sb.append("  ls.createdDate   >='" +DateUtils.formatDate(DateUtils.setTimeToMidnight(DateUtils.formatDate(loadslipFilterDto.getFromCreatedDate(),PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) +"' and ls.createdDate <= '"+DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(loadslipFilterDto.getToCreatedDate(),PLAN_RECORD_DATE_FORMAT)),Constants.DATE_FORMATE_WITH_HYPHEN)+"'");
*/
        sb.append("and  ls.createdDate   >=TO_DATE('" + loadslipFilterDto.getFromCreatedDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
        sb.append(" and ls.createdDate <= TO_DATE('" + DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(loadslipFilterDto.getToCreatedDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      }

     /*if (!StringUtils.isEmpty(loadslipFilterDto.getMarketSegment())) {
       Map<String, String> marketSegMap = Utility.deriveTubeAndFlapBatchCodes();
       //Here tube and flap batch codes are same based on MarketSegment
       String tubeOrFlapBatchCode = marketSegMap.get(loadslipFilterDto.getMarketSegment());
       sb.append("and   ldb.TUBE_BATCH = '" + tubeOrFlapBatchCode + "'");
     }
*/
     if (loadslipFilterDto.getMarketSegment() != null && loadslipFilterDto.getMarketSegment().size()>0){
       sb.append(" and ls.marketSegment  in (" + Utility.join(loadslipFilterDto.getMarketSegment()) + ") ");
     }

     if (!StringUtils.isEmpty(loadslipFilterDto.getTruckNumber())) {
        sb.append("and   ls.truckNumber = '" + loadslipFilterDto.getTruckNumber() + "'");
      }

      if (!StringUtils.isEmpty(loadslipFilterDto.getContainerNum())) {
        sb.append("and   ls.containerNum = '" + loadslipFilterDto.getContainerNum() + "'");
      }

     if (!StringUtils.isEmpty(loadslipFilterDto.getDestCountry())) {
       sb.append("and   ls.destCountry = '" + loadslipFilterDto.getDestCountry() + "'");
     }
     if (!StringUtils.isEmpty(loadslipFilterDto.getInsertUser())){
       sb.append("and   ls.insertUser = '" + loadslipFilterDto.getInsertUser() + "'");
     }

     if (!StringUtils.isEmpty(loadslipFilterDto.getTransporter())) {
       sb.append("and   ls.servprov = '" + loadslipFilterDto.getTransporter() + "'");
     }

     if(loadslipFilterDto.getItemCategories() != null && !loadslipFilterDto.getItemCategories().isEmpty()){
       sb.append(" AND ls.itemCategory IN ("+Utility.join(loadslipFilterDto.getItemCategories())+") ");
     }
     sb.append("  ORDER BY ls.createdDate DESC");

     return sb;
   }
  }


  private List<Predicate> getPredicatesForLoadslips(ApplicationUser loggedInsuer, LoadslipFilterDto loadslipFilterDto, CriteriaBuilder criteriaBuilder, Root<Loadslip> loadslipTable, Join<Loadslip, Shipment> shipmentJoin, Join<Loadslip, LoadslipDetail> loadslipDetailJoin, Join<Loadslip, LoadslipDetailBom> loadslipDetailBomJoin, List<String> statuses) {
    List<Predicate> predicates = new ArrayList<>();

    /*DP_REP, L1_MGR, L2_MGR*/
    if (!UserRole.getDPREPAccessRoles().contains(loggedInsuer.getRole())) {
      predicates.add(criteriaBuilder.equal(loadslipTable.get("sourceLoc"), loggedInsuer.getPlantCode()));
    } else {
      /*Since user DP_REP can select any source location */
      if (!StringUtils.isEmpty(loadslipFilterDto.getSource())) {
        predicates.add(criteriaBuilder.equal(loadslipTable.get("sourceLoc"), loadslipFilterDto.getSource()));
      }
    }
    if (!StringUtils.isEmpty(loadslipFilterDto.getLoadslipId())) {
//      predicates.add(criteriaBuilder.equal(loadslipTable.get("loadslipId"), loadslipFilterDto.getLoadslipId()));
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(loadslipTable.get("loadslipId")), "%" + loadslipFilterDto.getLoadslipId().toLowerCase() + "%"));
    }
    if (!StringUtils.isEmpty(loadslipFilterDto.getStopType())) {
      predicates.add(criteriaBuilder.equal(shipmentJoin.get("stopType"), loadslipFilterDto.getStopType()));
    }
    if(loadslipFilterDto.isLoadslipsWithItems()) {
      if (!StringUtils.isEmpty(loadslipFilterDto.getItemId())) {
        predicates.add(criteriaBuilder.equal(loadslipDetailJoin.get("loadslipDetailId").get("itemId"), loadslipFilterDto.getItemId()));
      }
     /* if (!StringUtils.isEmpty(loadslipFilterDto.getMarketSegment())) {
        Map<String, String> marketSegMap = Utility.deriveTubeAndFlapBatchCodes();
        //Here tube and flap batch codes are same based on MarketSegment
        String tubeOrFlapBatchCode = marketSegMap.get(loadslipFilterDto.getMarketSegment());
        predicates.add(criteriaBuilder.equal(loadslipDetailBomJoin.get("tubeBatch"), tubeOrFlapBatchCode));
      }*/
    }else {
      predicates.add(criteriaBuilder.isNull(loadslipDetailJoin.get("loadslipDetailId").get("loadslipId")));
    }
    if (!StringUtils.isEmpty(loadslipFilterDto.getDestination())) {
      predicates.add(criteriaBuilder.equal(loadslipTable.get("destLoc"), loadslipFilterDto.getDestination()));
    }
    if (loadslipFilterDto.getTruckType() != null && loadslipFilterDto.getTruckType().size()>0) {
      Predicate p1 = criteriaBuilder.and(criteriaBuilder.isNull(shipmentJoin.get("actualTruckType")),
              criteriaBuilder.and(shipmentJoin.get("truckType").in(loadslipFilterDto.getTruckType()))
      );
      Predicate p2 = criteriaBuilder.and(criteriaBuilder.isNotNull(shipmentJoin.get("actualTruckType")),
              criteriaBuilder.and(shipmentJoin.get("actualTruckType").in(loadslipFilterDto.getTruckType()))
      );

      predicates.add(criteriaBuilder.or(p1, p2));
    }
    if (!StringUtils.isEmpty(loadslipFilterDto.getTranshipment())) {
      predicates.add(criteriaBuilder.equal(shipmentJoin.get("transhipment"), loadslipFilterDto.getTranshipment()));
    }
    if (!StringUtils.isEmpty(loadslipFilterDto.getInvoice())) {
      predicates.add(criteriaBuilder.equal(loadslipTable.get("sapInvoice"), loadslipFilterDto.getInvoice()));
    }
    if (!StringUtils.isEmpty(loadslipFilterDto.getShipmentId())) {
      predicates.add(criteriaBuilder.equal(shipmentJoin.get("shipmentId"), loadslipFilterDto.getShipmentId()));
    }
    if (loadslipFilterDto.getMarketSegment() != null && loadslipFilterDto.getMarketSegment().size()>0){
      predicates.add(criteriaBuilder.and(loadslipTable.get("marketSegment").in(loadslipFilterDto.getMarketSegment())));
    }

  /*  if (loadslipFilterDto.getCreatedDate() != null && (DateUtils.isDateValid(loadslipFilterDto.getCreatedDate(), PLAN_RECORD_DATE_FORMAT))) {
      predicates.add(criteriaBuilder.and(
          criteriaBuilder.greaterThanOrEqualTo(loadslipTable.get("insertDate"), DateUtils.atStartOfDay(DateUtils.formatDate(loadslipFilterDto.getCreatedDate(), PLAN_RECORD_DATE_FORMAT))),
          criteriaBuilder.lessThanOrEqualTo(loadslipTable.get("insertDate"), DateUtils.atEndOfDay(DateUtils.formatDate(loadslipFilterDto.getCreatedDate(), PLAN_RECORD_DATE_FORMAT)))));
    }*/

    //FromDispatch Date and ToDispatchDate
    if (!StringUtils.isEmpty(loadslipFilterDto.getFromCreatedDate()) && !StringUtils.isEmpty(loadslipFilterDto.getToCreatedDate())) {
      predicates.add(criteriaBuilder.and(
              criteriaBuilder.greaterThanOrEqualTo(loadslipTable.get("insertDate"),
                      DateUtils.atStartOfDay(DateUtils.formatDate(loadslipFilterDto.getFromCreatedDate(),
                              PLAN_RECORD_DATE_FORMAT))),
              criteriaBuilder.lessThanOrEqualTo(loadslipTable.get("insertDate"), DateUtils.atEndOfDay(
                      DateUtils.formatDate(loadslipFilterDto.getToCreatedDate(), PLAN_RECORD_DATE_FORMAT)))));
    }

    /*( (lp.STATUS in ?2 and lp.LOADSLIP_TYPE not in ( 'FGS_EXP', 'JIT_OEM')) OR (lp.STATUS in ?3 and lp.LOADSLIP_TYPE  in ( 'FGS_EXP', 'JIT_OEM')))
    Predicate or = criteriaBuilder.or(and1, and2, and3);
*/
    if (statuses != null && !statuses.isEmpty()) {

      List<String> typeList = new ArrayList<>();
      typeList.add(Constants.DelInvType.FGS_EXP.name());
      typeList.add(Constants.DelInvType.JIT_OEM.name());

      List<Constants.LoadslipStatus> loadslipStatus = loadslipFilterDto.getStatus().parallelStream().map(Constants.LoadslipStatus::valueOf).collect(Collectors.toList());
      List<Constants.LoadslipStatus> statusList = statuses.parallelStream().map(Constants.LoadslipStatus::valueOf).collect(Collectors.toList());
      //Predicate predicatetmp = loadslipTable.get("status").in(statusList).not(criteriaBuilder.in(loadslipTable.get("loadslipType"));
      Predicate nonExport = criteriaBuilder.and(loadslipTable.get("status").in(statusList), criteriaBuilder.not(loadslipTable.get("loadslipType").in(typeList)));
      Predicate export = criteriaBuilder.and(loadslipTable.get("status").in(loadslipStatus), loadslipTable.get("loadslipType").in(typeList));

      Predicate and = criteriaBuilder.or(nonExport, export);
      predicates.add(and);
      //predicates.add(loadslipTable.get("status").in(statusList));
    }

    if (loadslipFilterDto.getLsStatus()!=null && loadslipFilterDto.getLsStatus().size()>0) {
      List<Constants.LoadslipStatus> statusList = loadslipFilterDto.getLsStatus().parallelStream().map(Constants.LoadslipStatus::valueOf).collect(Collectors.toList());
      predicates.add(criteriaBuilder.and(loadslipTable.get("status").in(statusList)));//loadslipTable.get("status"), Constants.LoadslipStatus.valueOf(Utility.join(loadslipFilterDto.getLsStatus()))
    }

    if (!StringUtils.isEmpty(loadslipFilterDto.getTruckNumber())) {
      predicates.add(criteriaBuilder.equal(shipmentJoin.get("truckNumber"), loadslipFilterDto.getTruckNumber()));
    }

    if (!StringUtils.isEmpty(loadslipFilterDto.getContainerNum())) {
      predicates.add(criteriaBuilder.equal(shipmentJoin.get("containerNum"), loadslipFilterDto.getContainerNum()));
    }

    if(!StringUtils.isEmpty(loadslipFilterDto.getDestCountry())){
      predicates.add(criteriaBuilder.equal(shipmentJoin.get("destCountry"), loadslipFilterDto.getDestCountry()));
    }

    if(!StringUtils.isEmpty(loadslipFilterDto.getTransporter())){
      predicates.add(criteriaBuilder.equal(shipmentJoin.get("servprov"), loadslipFilterDto.getTransporter()));
    }
    if (loadslipFilterDto.getItemCategories() != null && !loadslipFilterDto.getItemCategories().isEmpty()){
      predicates.add(criteriaBuilder.and(loadslipTable.get("itemCategory").in(loadslipFilterDto.getItemCategories())));
    }

    return predicates;
  }



  private List<Predicate> getPredicatesForExportTracker(ApplicationUser loggedInsuer, ExportTrackerFilter exportTrackerFilter, CriteriaBuilder criteriaBuilder, Root<TruckReport> truckReportRoot, Root<Shipment> shipmentRoot) {
    List<Predicate> predicates = new ArrayList<>();

    //For export Type
    predicates.add(criteriaBuilder.equal(shipmentRoot.get("shipmentType"), Constants.DelInvType.FGS_EXP.name()));
    predicates.add(criteriaBuilder.equal(shipmentRoot.get("shipmentId"), truckReportRoot.get("shipmentID")));
    predicates.add(criteriaBuilder.equal(truckReportRoot.get("sourceLocation"), truckReportRoot.get("reportLocation")));


    if (UserRole.getDPREPAccessRoles().contains(loggedInsuer.getRole())){
      if (!StringUtils.isEmpty(exportTrackerFilter.getSourceLoc())) {
        predicates.add(criteriaBuilder.equal(truckReportRoot.get("sourceLocation"), exportTrackerFilter.getSourceLoc()));
      }
    }else {
      if (!StringUtils.isEmpty(exportTrackerFilter.getSourceLoc())) {
        predicates.add(criteriaBuilder.equal(truckReportRoot.get("sourceLocation"), exportTrackerFilter.getSourceLoc()));
      }else {
        predicates.add(criteriaBuilder.equal(truckReportRoot.get("sourceLocation"), loggedInsuer.getPlantCode()));
      }
    }

    if (!StringUtils.isEmpty(exportTrackerFilter.getDestLoc())) {
      predicates.add(criteriaBuilder.equal(truckReportRoot.get("destinationLocation"), exportTrackerFilter.getDestLoc()));
    }

    if (!StringUtils.isEmpty(exportTrackerFilter.getShipmentId())) {
      predicates.add(criteriaBuilder.equal(truckReportRoot.get("shipmentID"), exportTrackerFilter.getShipmentId()));
    }


    if (!StringUtils.isEmpty(exportTrackerFilter.getContainerNum())) {
      predicates.add(criteriaBuilder.equal(shipmentRoot.get("containerNum"), exportTrackerFilter.getContainerNum()));
    }


    if (!StringUtils.isEmpty(exportTrackerFilter.getTruckNumber())) {
      predicates.add(criteriaBuilder.equal(shipmentRoot.get("truckNumber"), exportTrackerFilter.getTruckNumber()));
    }

    /*Commented this code because we are displaying TRUCK_TYPE in frontend  and applying filter on ACTUAL_TRUCK_TYPE.
     * So now changing the logic to TRUCK_TYPE (As discussed with Hari)*/

    /*if (!StringUtils.isEmpty(exportTrackerFilter.getTruckType())) {
      predicates.add(criteriaBuilder.equal(truckReportRoot.get("actualTruckType"), exportTrackerFilter.getTruckType()));
    }*/

    if (exportTrackerFilter.getTruckType() != null && exportTrackerFilter.getTruckType().size()>0) {
      predicates.add(criteriaBuilder.and(shipmentRoot.get("truckType").in(exportTrackerFilter.getTruckType())));
      //predicates.add(truckReportRoot.<String>get("truckType").in(exportTrackerFilter.getTruckType()));
    }

    if (!StringUtils.isEmpty(exportTrackerFilter.getTransporter())) {
      predicates.add(criteriaBuilder.equal(truckReportRoot.get("servprov"), exportTrackerFilter.getTransporter()));
    }


    return predicates;
  }


  private List<Predicate> getPredicatesForTrucks(TruckReportFilterDto truckReportFilterDto, CriteriaBuilder criteriaBuilder, Root<TruckReport> root, ApplicationUser loggedInUser,
                                                 List<Constants.TruckReportStatus> truckReportStatuses) {
    List<Predicate> predicates = new ArrayList<>();

    if (!StringUtils.isEmpty(truckReportFilterDto.getSource())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("sourceLocation")), "%" + truckReportFilterDto.getSource().toLowerCase() + "%"));
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getDestination())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("destinationLocation")), "%" + truckReportFilterDto.getDestination().toLowerCase() + "%"));
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getTransporter())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("servprov")), "%" + truckReportFilterDto.getTransporter().toLowerCase() + "%"));
    }
    if (truckReportFilterDto.getTruckType() !=null && truckReportFilterDto.getTruckType().size()>0) {
      //predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("reportedTruckType")), "%" + truckReportFilterDto.getTruckType().toLowerCase() + "%"));
      predicates.add(criteriaBuilder.and(root.get("reportedTruckType").in(truckReportFilterDto.getTruckType())));
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getTruckNumber())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("truck").get("truckNumber")), "%" + truckReportFilterDto.getTruckNumber().toLowerCase() + "%"));
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getIndentID())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("indentSummary").get("indentId")), "%" + truckReportFilterDto.getIndentID().toLowerCase() + "%"));
    }
    if (truckReportStatuses != null && !truckReportStatuses.isEmpty()) {
      predicates.add(root.<String>get("status").in(truckReportStatuses));
    }
    return predicates;
  }


  @Override
  public List<ExportTrackerDto> filterExportTracker(ApplicationUser loggedInUser, ExportTrackerFilter exportTrackerFilter) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ExportTrackerDto> criteriaQuery = criteriaBuilder.createQuery(ExportTrackerDto.class);
    Root<TruckReport> truckReportTable = criteriaQuery.from(TruckReport.class);
    Root<Shipment> shipmentRoot = criteriaQuery.from(Shipment.class);

    truckReportTable.alias("tr");
    shipmentRoot.alias("s");
    //truckReportTable.join
    List<Predicate> predicates = getPredicatesForExportTracker(loggedInUser, exportTrackerFilter, criteriaBuilder, truckReportTable, shipmentRoot);
    Expression<String> groupByShipment = shipmentRoot.get("containerNum").as(String.class);
    criteriaQuery.multiselect(
            shipmentRoot.get("containerNum"),
            shipmentRoot.get("shipmentId"),
            shipmentRoot.get("truckNumber"),
            shipmentRoot.get("actualTruckType"),
            shipmentRoot.get("truckType"),
            truckReportTable.get("sourceLocation"),
            truckReportTable.get("reportLocation"),
            truckReportTable.get("destinationLocation"),
            truckReportTable.get("reportDate"),
            truckReportTable.get("gateInDate"),
            truckReportTable.get("gateOutDate"),
            shipmentRoot.get("gateInDateCfs"),
            shipmentRoot.get("gateOutDateCfs"),
            shipmentRoot.get("gateInDatePort"),
            shipmentRoot.get("shippedOnboardDate"),
            shipmentRoot.get("vesselDepartPolDate"),
            shipmentRoot.get("vesselArrivePodDate"));
    // criteriaQuery.groupBy(groupByShipment);
    criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
    TypedQuery<ExportTrackerDto> typedQuery = entityManager.createQuery(criteriaQuery);
    return typedQuery.getResultList();
  }


  public Page<MTItem> getMTItem(MtItemFilterDto itemFilterDto, String itemId, String description, String classification, String type, String group, String category, String tte, String loadfactor) {
    if(tte !=null && !tte.equals("")&& tte.startsWith("0")){
      tte = tte.substring(1,tte.length());
    }
    //System.out.println("******BEFORE CALLING sql STATEMENT:  "+ tte);
    List<MTItem> itemsList = new ArrayList<MTItem>();
    Page<MTItem> itemsPageList;
    //Query q = entityManager.createNativeQuery("select I.item_id as id, I.item_classification as classification, I.item_description as description, I.item_type as type, I.item_group as \"group\" ,I.item_category as category, I.tte as tte, I.load_factor as loadfactor from MT_ITEM I where LOWER(I.item_id) like LOWER(concat(:itemId,'%')) ").setParameter("itemId",itemId);

    Query q = entityManager.createNativeQuery(
            "SELECT\n" +
                    "    i.item_id AS id,\n" +
                    "    i.item_classification AS classification,\n" +
                    "    i.item_description AS description,\n" +
                    "    i.item_type AS type,\n" +
                    "    i.item_group AS \"group\",\n" +
                    "    i.tte AS tte,\n" +
                    "    i.load_factor AS loadfactor,\n" +
                    "    i.gross_wt AS grosswt,\n" +
                    "    i.gross_wt_uom AS grosswtuom,\n" +
                    "    i.net_wt AS netwt,\n" +
                    "    i.net_wt_uom AS netwtuom,\n" +
                    "    i.volume AS volume,\n" +
                    "    i.vol_uom AS voluom,\n" +
                    "    i.length AS \"length\",\n" +
                    "    i.len_uom AS lenuom,\n" +
                    "    i.width AS width,\n" +
                    "    i.wd_uom AS wduom,\n" +
                    "    i.height AS height,\n" +
                    "    i.ht_uom AS htuom,\n" +
                    "    i.diameter AS diameter,\n" +
                    "    i.dm_uom AS dmuom,\n" +
                    "    i.insert_user AS insertuser,\n" +
                    "    i.insert_date AS insertdate,\n" +
                    "    i.update_user AS updateuser,\n" +
                    "    i.update_date AS updatedate,\n" +
                    "    i.item_category AS category\n" +
                    "FROM mt_item i\n" +
                    "WHERE \n" +
                    "nvl(upper(i.item_id),'-') like '%'||upper(:itemId)||'%' \n" +
                    "and nvl(upper(i.item_classification),'-') like '%'||upper(:classification)||'%' \n" +
                    "and nvl(upper(i.item_description),'-') like '%'||upper(:description)||'%' \n" +
                    "and nvl(upper(i.item_type),'-') like '%'||upper(:type)||'%' \n" +
                    "and nvl(upper(i.item_group),'-') like '%'||upper(:group)||'%' \n" +
                    "and nvl(upper(i.item_category),'-') like '%'||upper(:category)||'%' \n" +
                    "and nvl(upper(i.tte),'-') like '%'||upper(:tte)||'%' \n" +
                    "and nvl(upper(i.load_factor),'-') like '%'||upper(:loadfactor)||'%' order by i.item_id,i.item_type")
            .setParameter("itemId",itemId)
            .setParameter("classification",classification)
            .setParameter("description",description)
            .setParameter("type",type)
            .setParameter("group",group)
            .setParameter("category",category)
            .setParameter("tte",tte)
            .setParameter("loadfactor",loadfactor)
            .setFirstResult(itemFilterDto.getIndex()*itemFilterDto.getPageLength())
            .setMaxResults(itemFilterDto.getPageLength());
    // System.out.println("query = "+q.toString());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT\n" +
                    "    count(i.item_id) \n" +
                    "FROM mt_item i\n" +
                    "WHERE \n" +
                    "nvl(upper(i.item_id),'-') like '%'||upper(:itemId)||'%' \n" +
                    "and nvl(upper(i.item_classification),'-') like '%'||upper(:classification)||'%' \n" +
                    "and nvl(upper(i.item_description),'-') like '%'||upper(:description)||'%' \n" +
                    "and nvl(upper(i.item_type),'-') like '%'||upper(:type)||'%' \n" +
                    "and nvl(upper(i.item_group),'-') like '%'||upper(:group)||'%' \n" +
                    "and nvl(upper(i.item_category),'-') like '%'||upper(:category)||'%' \n" +
                    "and nvl(upper(i.tte),'-') like '%'||upper(:tte)||'%' \n" +
                    "and nvl(upper(i.load_factor),'-') like '%'||upper(:loadfactor)||'%' " )
            .setParameter("itemId",itemId)
            .setParameter("classification",classification)
            .setParameter("description",description)
            .setParameter("type",type)
            .setParameter("group",group)
            .setParameter("category",category)
            .setParameter("tte",tte)
            .setParameter("loadfactor",loadfactor);

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    System.out.println("******************Count Value:" + Integer.valueOf(countObject.get(0).toString()) );


    List<Object> res = q.getResultList();

    Iterator it = res.iterator();
    while(it.hasNext()){
      Object[] line = (Object[]) it.next();
      MTItem item = new MTItem();
      item.setId(line[0].toString());
      item.setId((line[0]!=null)?line[0].toString():"");
      item.setClassification((line[1]!=null)?line[1].toString():"");
      item.setDescription((line[2]!=null)?line[2].toString():"");
      item.setType(ItemType.toString((line[3]!=null)?line[3].toString():""));
      item.setGroup((line[4]!=null)?line[4].toString():"");
      item.setTte(Double.valueOf((line[5]!=null)?line[5].toString():"0"));
      item.setLoadFactor(Double.valueOf((line[6]!=null)?line[6].toString():"0"));
      item.setGrossWt((Double.parseDouble((line[7]!=null)?line[7].toString():"0")));
      item.setGrossWtUom((line[8]!=null)?line[8].toString():"");
      item.setNetWt(Double.valueOf((line[9]!=null)?line[9].toString():"0"));
      item.setNetWtUom((line[10]!=null)?line[10].toString():"");
      item.setVolume(Double.valueOf((line[11]!=null)?line[11].toString():"0"));
      item.setVolUom((line[12]!=null)?line[12].toString():"");
      item.setLength(Integer.valueOf((line[13]!=null)?line[13].toString():"0"));
      item.setLenUom((line[14]!=null)?line[14].toString():"");
      item.setWidth(Integer.valueOf((line[15]!=null)?line[15].toString():"0"));
      item.setWdUom((line[16]!=null)?line[16].toString():"");
      item.setHeight(Integer.valueOf((line[17]!=null)?line[17].toString():"0"));
      item.setHtUom((line[18]!=null)?line[18].toString():"");
      item.setDiameter(Integer.valueOf((line[19]!=null)?line[19].toString():"0"));
      item.setDmUom((line[20]!=null)?line[20].toString():"");
      item.setInsertUser((line[21]!=null)?line[21].toString():"");
      item.setUpdateUser((line[23]!=null)?line[23].toString():"");
      //  itemBean.setInsertDate((Date) line[6]);
      //  itemBean.setUpdateDate((Date) line[8]);
      try {
        // item.setInsertDate(DateUtils.formatDate(line[22].toString(), Constants.ORA_DATE_FORMAT));
        //   item.setUpdateDate(DateUtils.formatDate(line[24].toString(),Constants.ORA_DATE_FORMAT));
        item.setInsertDate((Date) line[22]);
        item.setUpdateDate((Date) line[24]);
      }catch(Exception e){
        item.setInsertDate(null);
        item.setUpdateDate(null);
      }
      item.setCategory((line[25]!=null)?line[25].toString():"");
      itemsList.add(item);
    }
    itemsPageList = new PageImpl<MTItem>(itemsList);
    return new PageImpl<MTItem>(itemsList,PageRequest.of(itemFilterDto.getIndex(), itemFilterDto.getPageLength()), count);
    // return new PageImpl<MTItem>(itemsList);
  }


  public Page<MTOeBom> getFilteredMTOeBom(MTOeBomFilterDto mtOeBomFilterDto){
    List<MTOeBom> mtOeBomList  = new ArrayList<MTOeBom>();
    System.out.println("******BEFORE CALLING sql STATEMENT:  "+new Gson().toJson(mtOeBomFilterDto));
    Query q = entityManager.createNativeQuery("SELECT\n" +
            "f.SALES_SKU\tas salesSku,\n" +
            "f.ITEM_ID\tas itemId,\n" +
            "f.COMP_QTY\tas compQty,\n" +
            "f.ITEM_SEQ\tas itemSeq,\n" +
            "f.OE_CODE\tas oeCode,\n" +
            "f.INSERT_USER\tas insertUser,\n" +
            "f.INSERT_DATE\tas insertDate,\n" +
            "f.UPDATE_USER\tas updateUser,\n" +
            "f.UPDATE_DATE\tas updateDate\n" +
            "FROM mt_item_oe_bom f\n" +
            "WHERE \n" +
            "upper (nvl(f.SALES_SKU,'-'))like '%' || upper(:salesSku)||'%' \n" +
            "and upper (nvl(f.ITEM_ID,'-'))like '%' || upper(:itemId)||'%' \n" +
            "and upper (nvl(f.OE_CODE,'-')) like '%' || upper(:oeCode)||'%' order by f.SALES_SKU,f.ITEM_ID, f.COMP_QTY")
            .setParameter("salesSku",mtOeBomFilterDto.getSalesSku())
            .setParameter("itemId",mtOeBomFilterDto.getItemId())
            .setParameter("oeCode",mtOeBomFilterDto.getOeCode())
            .setFirstResult(mtOeBomFilterDto.getIndex()*mtOeBomFilterDto.getPageLength())
            .setMaxResults(mtOeBomFilterDto.getPageLength());
    List<Object[]> res = q.getResultList();

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(f.ITEM_ID) \n" +
                    "FROM mt_item_oe_bom f\n" +
                    "WHERE \n" +
                    "upper (nvl(f.SALES_SKU,'-'))like '%' || upper(:salesSku)||'%' \n" +
                    "and upper (nvl(f.ITEM_ID,'-'))like '%' || upper(:itemId)||'%' \n" +
                    "and upper (nvl(f.OE_CODE,'-')) like '%' || upper(:oeCode)||'%'")
            .setParameter("salesSku",mtOeBomFilterDto.getSalesSku())
            .setParameter("itemId",mtOeBomFilterDto.getItemId())
            .setParameter("oeCode",mtOeBomFilterDto.getOeCode());
    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    Iterator it = res.iterator();
    MTOeBom mtOeBom;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      mtOeBom = new MTOeBom();
      mtOeBom.setSalesSku(((line[0]!=null)?line[0].toString():"0"));
      mtOeBom.setItemId((line[1]!=null)?line[1].toString():"");
      mtOeBom.setCompQty(Integer.valueOf((line[2]!=null)?line[2].toString():""));
      mtOeBom.setItemSeq(Integer.valueOf((line[3]!=null)?line[3].toString():""));
      mtOeBom.setOeCode((line[4]!=null)?line[4].toString():"");
      mtOeBom.setInsertUser((line[5]!=null)?line[5].toString():"");
      mtOeBom.setUpdateUser((line[7]!=null)?line[7].toString():"");
      try {
        mtOeBom.setInsertDate((Date) line[6]);
        mtOeBom.setUpdateDate((Date) line[8]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting effective date");
        mtOeBom.setInsertDate(null);
      }
      mtOeBomList.add(mtOeBom);
    }
    return new PageImpl<MTOeBom>(mtOeBomList,PageRequest.of(mtOeBomFilterDto.getIndex(), mtOeBomFilterDto.getPageLength()), count);
  }



  public Page<MTRepBom> getFilteredMTRepBom(MTRepBomFilterDto mtRepBomFilterDto){
    List<MTRepBom> mtRepBomList  = new ArrayList<MTRepBom>();
    Query q = entityManager.createNativeQuery("SELECT\n" +
            "f.SALES_SKU\tas salesSku,\n" +
            "f.ITEM_ID\tas itemId,\n" +
            "f.COMP_QTY\tas compQty,\n" +
            "f.ITEM_SEQ\tas itemSeq,\n" +
            "f.INSERT_USER\tas insertUser,\n" +
            "f.INSERT_DATE\tas insertDate,\n" +
            "f.UPDATE_USER\tas updateUser,\n" +
            "f.UPDATE_DATE\tas updateDate \n" +
            "FROM mt_item_rep_bom f\n" +
            "WHERE " +
            "upper(f.SALES_SKU)like '%' || upper(:salesSku)||'%' \n" +
            "and upper (f.ITEM_ID) like '%' || upper(:itemId)||'%' order by f.SALES_SKU,f.ITEM_ID, f.COMP_QTY")
//            " ((lower(nvl(f.SALES_SKU,'-')) = lower(nvl(:salesSku,'-'))) OR (f.SALES_SKU is not null AND :salesSku is null))\n" +
//            "  AND ((lower(nvl(f.ITEM_ID,'-')) = lower(nvl(:itemId,'-'))) OR (f.ITEM_ID is not null AND :itemId is null))")
            // "  AND f.id > 16262 order by f.EFFECTIVE_DATE desc")  //15823
            .setParameter("salesSku",mtRepBomFilterDto.getSalesSku())
            .setParameter("itemId",mtRepBomFilterDto.getItemId())
            .setFirstResult(mtRepBomFilterDto.getIndex()*mtRepBomFilterDto.getPageLength())
            .setMaxResults(mtRepBomFilterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT\n" +
                    "count(f.item_id) \n" +
                    "FROM mt_item_rep_bom f\n" +
                    "WHERE " +
                    "upper(f.SALES_SKU)like '%' || upper(:salesSku)||'%' \n" +
                    "and upper (f.ITEM_ID) like '%' || upper(:itemId)||'%'")
            .setParameter("salesSku",mtRepBomFilterDto.getSalesSku())
            .setParameter("itemId",mtRepBomFilterDto.getItemId());
    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();

    Iterator it = res.iterator();
    MTRepBom mtRepBom;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      mtRepBom = new MTRepBom();
      mtRepBom.setSalesSku(((line[0]!=null)?line[0].toString():"0"));
      mtRepBom.setItemId((line[1]!=null)?line[1].toString():"");
      mtRepBom.setCompQty(Integer.valueOf((line[2]!=null)?line[2].toString():""));
      mtRepBom.setItemSeq(Integer.valueOf((line[3]!=null)?line[3].toString():""));
      mtRepBom.setInsertUser(((line[4]!=null)?line[4].toString():""));
      //mtRepBom.setInsertDate(((line[6]!=null)?line[6].toString():"");
      mtRepBom.setUpdateUser(((line[6]!=null)?line[6].toString():""));
      try {
        mtRepBom.setInsertDate((Date) line[5]);
        mtRepBom.setUpdateDate((Date)line[7]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting effective date");
        mtRepBom.setInsertDate(null);
      }
      mtRepBomList.add(mtRepBom);
    }
    return new PageImpl<MTRepBom>(mtRepBomList,PageRequest.of(mtRepBomFilterDto.getIndex(), mtRepBomFilterDto.getPageLength()), count);
  }


  public Page<MTPlantItem> getFilteredMTPlantItem(MTPlantItemFilterDto mtPlantItemFilterDto) {
    List<MTPlantItem> mtPlantItemList = new ArrayList<MTPlantItem>();
    Query q = entityManager.createNativeQuery(
            "" +
                    "SELECT \n" +
                    "f.PLANT_CODE\tas plantCode,\n" +
                    "f.EFFECTIVE_DATE\tas effectiveDate,\n" +
                    "f.ITEM_ID\tas itemId,\n" +
                    "f.WEIGHT\tas weight,\n" +
                    "f.WEIGHT_UOM\tas weightUom,\n" +
                    "f.INSERT_USER\tas insertUser,\n" +
                    "f.INSERT_DATE\tas insertDate,\n" +
                    "f.UPDATE_USER\tas updateUser,\n" +
                    "f.UPDATE_DATE\tas updateDate\n" +
                    "FROM mt_item_plant_weight f \n" +
                    "WHERE " +
                    "upper(f.PLANT_CODE) like '%' || upper(:plantCode)||'%'\n" +
                    "and upper(f.ITEM_ID) like '%'||upper(:itemId)||'%'\n" +
                    "AND ((nvl(trunc(f.EFFECTIVE_DATE),sysdate+1) = nvl(to_date(:effectiveDate,'DD-MM-RR'),sysdate+1)) " +
                    "OR (f.EFFECTIVE_DATE is not null AND :effectiveDate is null)) order by f.PLANT_CODE, f.ITEM_ID, f.EFFECTIVE_DATE desc")
            .setParameter("plantCode", mtPlantItemFilterDto.getPlantCode())
            .setParameter("effectiveDate", mtPlantItemFilterDto.getEffectiveDate())
            .setParameter("itemId", mtPlantItemFilterDto.getItemId())
            .setFirstResult(mtPlantItemFilterDto.getIndex() * mtPlantItemFilterDto.getPageLength())
            .setMaxResults(mtPlantItemFilterDto.getPageLength());
    // "  AND ((nvl(trunc(f.EFFECTIVE_DATE),sysdate+1) = nvl(to_date(:effectiveDate,'DD-MM-RR'),sysdate+1)) OR (f.EFFECTIVE_DATE is not null AND :effectiveDate is null))\n" +
    Query countQuery = entityManager.createNativeQuery(
            "SELECT\n" +
                    "    count(f.item_id) \n" +
                    "FROM mt_item_plant_weight f\n" +
                    "WHERE " +
                    "upper(f.PLANT_CODE) like '%' || upper(:plantCode)||'%'\n" +
                    "and upper(f.ITEM_ID) like '%'||upper(:itemId)||'%'\n" +
                    "AND ((nvl(trunc(f.EFFECTIVE_DATE),sysdate+1) = nvl(to_date(:effectiveDate,'DD-MM-RR'),sysdate+1)) OR (f.EFFECTIVE_DATE is not null AND :effectiveDate is null))")
            .setParameter("plantCode",mtPlantItemFilterDto.getPlantCode())
            .setParameter("itemId",mtPlantItemFilterDto.getItemId())
            .setParameter("effectiveDate",mtPlantItemFilterDto.getEffectiveDate());
    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    MTPlantItem mtPlantItem;
    Object[] line;
    while (it.hasNext()) {
      line = (Object[]) it.next();
      mtPlantItem = new MTPlantItem();
      mtPlantItem.setPlantCode(((line[0] != null) ? line[0].toString() : "0"));
      mtPlantItem.setItemId((line[2] != null) ? line[2].toString() : "");
      mtPlantItem.setWeight(Double.valueOf((line[3] != null) ? line[3].toString() : ""));
      mtPlantItem.setWeightUom((line[4] != null) ? line[4].toString() : "");
      mtPlantItem.setInsertUser((line[5] != null) ? line[5].toString() : "");
      mtPlantItem.setUpdateUser((line[7] != null) ? line[7].toString() : "");
      try {
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) line[1]);
        cal.add(Calendar.DATE, 1);
        mtPlantItem.setEffectiveDate(cal.getTime());
        mtPlantItem.setInsertDate((Date) line[6]);
        mtPlantItem.setUpdateDate((Date) line[8]);
      } catch (Exception e) {
        System.out.println("*********** Exception while formatting effective date");
        mtPlantItem.setInsertDate(null);
      }
      mtPlantItemList.add(mtPlantItem);
    }
    return new PageImpl<MTPlantItem>(mtPlantItemList,PageRequest.of(mtPlantItemFilterDto.getIndex(), mtPlantItemFilterDto.getPageLength()), count);
  }





  public Page<Freight> filterFreights(FreightFilterDto freightFilterDto, String transporterSapCode, String servprov, String sourceLoc, String sourceDesc, String destLoc, String destDesc, String truckType, String condition1,
                                      String effectiveDate, String expiryDate, String ttDays, String baseFreight, String status, String fromInsertDate, String toInsertDate, String expiryDateFilterType, String fromExpiryDate, String toExpiryDate){
    System.out.println("*** BEFORE CALLING Query:  "+new Gson().toJson(freightFilterDto));
    List<Freight> freightsList  = new ArrayList<Freight>();
    Page<Freight> freightsPageList ;

    StringBuilder mainQuery = new StringBuilder("SELECT\n" +
        "f.ID\tas \"id\",\n" +
        "f.TRANSPORTER_SAP_CODE\tas transporterSapCode,\n" +
        "f.SERVPROV\tas servprov,\n" +
        "f.SOURCE_LOC\tas sourceLoc,\n" +
        "f.SOURCE_DESC\tas sourceDesc,\n" +
        "f.DEST_LOC\tas destLoc,\n" +
        "f.DEST_DESC\tas destDesc,\n" +
        "f.LANE_CODE\tas laneCode,\n" +
        "f.TRUCK_TYPE\tas truckType,\n" +
        "f.CONDITION1\tas condition1,\n" +
        "f.CONDITION2\tas condition2,\n" +
        "f.EFFECTIVE_DATE\tas effectiveDate,\n" +
        "f.EXPIRY_DATE\tas expiryDate,\n" +
        "f.TT_DAYS\tas ttDays,\n" +
        "f.BASE_FREIGHT\tas baseFreight,\n" +
        "f.BASE_FREIGHT_UOM\tas baseFreightUom,\n" +
        "f.BASIS\tas basis,\n" +
        "f.MIN_VALUE\tas minValue,\n" +
        "f.MIN_VALUE_UOM\tas minValueUom,\n" +
        "f.APPROVAL1_USER\tas approval1User,\n" +
        "f.APPROVAL1_DATE\tas approval1Date,\n" +
        "f.APPROVAL2_USER\tas approval2User,\n" +
        "f.APPROVAL2_DATE\tas approval2Date,\n" +
        "f.STATUS\tas status,\n" +
        "f.INSERT_USER\tas insertUser,\n" +
        "f.INSERT_DATE\tas insertDate,\n" +
        "f.UPDATE_USER\tas updateUser,\n" +
        "f.UPDATE_DATE\tas updateDate,\n" +
        "f.PREVIOUS_RATE\tas previousRate,\n" +
        "f.DIFF\tas diff,\n" +
        "to_char(f.PERCENTILE,'FM999999999999999990.09999')\tas percentile,\n" +
        "f.TRANSPORT_MODE\tas transportMode, \n" +
        "f.RATE_TYPE\tas rateType, \n" +
        "f.LOADING\tas loading, \n" +
        "f.UNLOADING\tas unloading, \n" +
        "f.OTHERS1\tas others1, \n" +
        "f.OTHERS1_CODE\tas others1Code, \n" +
        "f.OTHERS2\tas others2, \n" +
        "f.OTHERS2_CODE\tas others2Code, \n" +
        "f.OTHERS3\tas others3, \n" +
        "f.OTHERS3_CODE\tas others3Code, \n" +
        "f.DISTANCE \tas distance, \n" +
        "f.TOTAL_EXPENSE \tas totalExpense, \n" +
        "f.PAYABLE_TRANSPORTER \tas payableTransporter, \n" +
        "f.SOURCE_TYPE \tas sourceType \n, " +
        "f.REMARKS as remarks " +
        "FROM freight f\n" +
        "WHERE " +
        "((lower(nvl(f.TRANSPORTER_SAP_CODE,'-')) = lower(nvl(:transporterSapCode,'-'))) OR (f.TRANSPORTER_SAP_CODE is not null AND :transporterSapCode is null))\n" +
        "  AND ((lower(nvl(f.SERVPROV,'-')) = lower(nvl(:servprov,'-'))) OR (f.SERVPROV is not null AND :servprov is null))\n" +
        "  AND ((lower(nvl(f.SOURCE_LOC,'-')) = lower(nvl(:sourceLoc,'-'))) OR (f.SOURCE_LOC is not null AND :sourceLoc is null))\n" +
        "  AND ((lower(nvl(f.SOURCE_DESC,'-')) = lower(nvl(:sourceDesc,'-'))) OR (f.SOURCE_DESC is not null AND :sourceDesc is null))\n" +
        "  AND ((lower(nvl(f.DEST_LOC,'-')) = lower(nvl(:destLoc,'-'))) OR (f.DEST_LOC is not null AND :destLoc is null))\n" +
        "  AND ((lower(nvl(f.DEST_DESC,'-')) = lower(nvl(:destDesc,'-'))) OR (f.DEST_DESC is not null AND :destDesc is null))\n" +
        "  AND ((lower(nvl(f.TRUCK_TYPE,'-')) = lower(nvl(:truckType,'-'))) OR (f.TRUCK_TYPE is not null AND :truckType is null))\n" +
        "  AND ((lower(nvl(f.condition1,'-')) = lower(nvl(:condition1,'-'))) OR (f.condition1 is not null AND :condition1 is null))\n" +
        "  AND ((nvl(trunc(f.EFFECTIVE_DATE),sysdate+1) = nvl(to_date(:effectiveDate,'DD-MM-RR'),sysdate+1)) OR (f.EFFECTIVE_DATE is not null AND :effectiveDate is null))\n") ;

    if (!StringUtils.isEmpty(expiryDateFilterType)){
      if (("IS_NULL").equals(expiryDateFilterType)){
        /*IS NULL*/
        mainQuery.append(" AND f.EXPIRY_DATE IS NULL ");
      }else if (("BETWEEN").equals(expiryDateFilterType)){
        /*BETWEEN FILTER*/
        if (!StringUtils.isEmpty(fromExpiryDate) && !StringUtils.isEmpty(toExpiryDate)){
          /*IF BOTH DATES NOT NULL*/
          mainQuery.append(" AND (f.EXPIRY_DATE >= to_date(:fromExpiryDate,'dd-MM-yyyy') AND f.EXPIRY_DATE <= to_date(:toExpiryDate,'dd-MM-yyyy'))  ");
        }else {
          /*WHEN From And To Date is Empty then displaying all the records with expiry date as not null*/
          mainQuery.append(" AND f.EXPIRY_DATE IS NOT NULL ");
        }
      }
    }

    /*AND ((lower(trunc(f.EXPIRY_DATE)) = lower(to_date(:expiryDate,'dd-MM-yyyy')) AND :expiryDate is NOT null) OR (:expiryDate is null AND f.EXPIRY_DATE IS NULL))*/
    mainQuery.append( " " +
        "        AND ((nvl(trunc(f.INSERT_DATE),sysdate+1) >= nvl(to_date(:fromInsertDate,'dd-MM-yyyy'),sysdate+1)) OR (f.INSERT_DATE is not null AND :fromInsertDate is null))" +
        "          AND ((nvl(trunc(f.INSERT_DATE),sysdate+1) <= nvl(to_date(:toInsertDate,'dd-MM-yyyy'),sysdate+1)) OR (f.INSERT_DATE is not null AND :toInsertDate is null))" +
//        "          AND (((lower(nvl(trunc(f.INSERT_DATE),sysdate+1)) >= lower(nvl(to_date(:fromInsertDate,'DD-MM-RR'),sysdate+1))) AND (lower(nvl(trunc(f.INSERT_DATE),sysdate+1)) <= lower(nvl(to_date(:toInsertDate,'DD-MM-RR'),sysdate+1)))) OR (f.EXPIRY_DATE is not null AND (:fromInsertDate is null AND :toInsertDate is null)))" +
        "          AND ((to_char(nvl(f.TT_DAYS,0),'99.99') = to_char(nvl(:ttDays,0),'99.99')) OR (f.TT_DAYS is not null AND :ttDays is null))" +
        "         AND ((to_char(nvl(f.BASE_FREIGHT,0),'9999999.99') = to_char(nvl(:baseFreight,0),'9999999.99')) OR (f.BASE_FREIGHT is not null AND :baseFreight is null))" +
        "          AND ((lower(nvl(f.status,'Unapproved')) = lower(nvl(:status,'Unapproved'))) OR (f.status is not null AND :status is null))" +
        "          order by f.EFFECTIVE_DATE desc");

    Query q = entityManager.createNativeQuery(mainQuery.toString())
            .setParameter("transporterSapCode",transporterSapCode)
            .setParameter("servprov",servprov)
            .setParameter("sourceLoc",sourceLoc)
            .setParameter("sourceDesc",sourceDesc)
            .setParameter("destLoc",destLoc)
            .setParameter("destDesc",destDesc)
            .setParameter("truckType",truckType)
            .setParameter("condition1",condition1)
            .setParameter("effectiveDate",effectiveDate)
//            .setParameter("expiryDate",expiryDate)
            .setParameter("ttDays",ttDays)
            .setParameter("baseFreight",baseFreight)
            .setParameter("status",status)
            .setParameter("fromInsertDate",fromInsertDate)
            .setParameter("toInsertDate",toInsertDate)
            .setFirstResult(freightFilterDto.getIndex()*freightFilterDto.getPageLength())
            .setMaxResults(freightFilterDto.getPageLength());

    if (!StringUtils.isEmpty(expiryDateFilterType)){
     if (("BETWEEN").equals(expiryDateFilterType)){
        /*BETWEEN FILTER*/
        if (!StringUtils.isEmpty(fromExpiryDate) && !StringUtils.isEmpty(toExpiryDate)){
          /*IF BOTH DATES NOT NULL*/
          q.setParameter("fromExpiryDate",fromExpiryDate)
              .setParameter("toExpiryDate",toExpiryDate);
        }
      }
    }

    StringBuilder countQuery = new StringBuilder("SELECT\n" +
        "    count(f.TRANSPORTER_SAP_CODE) \n" +
        "FROM freight f\n" +
        "WHERE " +
        "((lower(nvl(f.TRANSPORTER_SAP_CODE,'-')) = lower(nvl(:transporterSapCode,'-'))) OR (f.TRANSPORTER_SAP_CODE is not null AND :transporterSapCode is null))\n" +
        "  AND ((lower(nvl(f.SERVPROV,'-')) = lower(nvl(:servprov,'-'))) OR (f.SERVPROV is not null AND :servprov is null))\n" +
        "  AND ((lower(nvl(f.SOURCE_LOC,'-')) = lower(nvl(:sourceLoc,'-'))) OR (f.SOURCE_LOC is not null AND :sourceLoc is null))\n" +
        "  AND ((lower(nvl(f.SOURCE_DESC,'-')) = lower(nvl(:sourceDesc,'-'))) OR (f.SOURCE_DESC is not null AND :sourceDesc is null))\n" +
        "  AND ((lower(nvl(f.DEST_LOC,'-')) = lower(nvl(:destLoc,'-'))) OR (f.DEST_LOC is not null AND :destLoc is null))\n" +
        "  AND ((lower(nvl(f.DEST_DESC,'-')) = lower(nvl(:destDesc,'-'))) OR (f.DEST_DESC is not null AND :destDesc is null))\n" +
        "  AND ((lower(nvl(f.TRUCK_TYPE,'-')) = lower(nvl(:truckType,'-'))) OR (f.TRUCK_TYPE is not null AND :truckType is null))\n" +
        "  AND ((lower(nvl(f.condition1,'-')) = lower(nvl(:condition1,'-'))) OR (f.condition1 is not null AND :condition1 is null))\n" +
        "  AND ((nvl(trunc(f.EFFECTIVE_DATE),sysdate+1) = nvl(to_date(:effectiveDate,'DD-MM-RR'),sysdate+1)) OR (f.EFFECTIVE_DATE is not null AND :effectiveDate is null))\n" );
    /*"  AND ((lower(trunc(f.EXPIRY_DATE)) = lower(to_date(:expiryDate,'dd-MM-yyyy')) AND :expiryDate is NOT null) OR (:expiryDate is null AND f.EXPIRY_DATE IS NULL))\n" +*/

    if (!StringUtils.isEmpty(expiryDateFilterType)){
      if (("IS_NULL").equals(expiryDateFilterType)){
        /*IS NULL*/
        countQuery.append(" AND f.EXPIRY_DATE IS NULL  ");
      }else if (("BETWEEN").equals(expiryDateFilterType)){
        /*BETWEEN FILTER*/
        if (!StringUtils.isEmpty(fromExpiryDate) && !StringUtils.isEmpty(toExpiryDate)){
          /*IF BOTH DATES NOT NULL*/
          countQuery.append(" AND (f.EXPIRY_DATE >= to_date(:fromExpiryDate,'dd-MM-yyyy') AND f.EXPIRY_DATE <= to_date(:toExpiryDate,'dd-MM-yyyy'))  ");
        }else {
          /*WHEN From And To Date is Empty then displaying all the records with expiry date as not null*/
          countQuery.append(" AND f.EXPIRY_DATE IS NOT NULL ");
        }
      }
    }/*else{
     *//*BOTH*//*
      mainQuery.append(" AND ((lower(trunc(f.EXPIRY_DATE)) = lower(to_date(:expiryDate,'dd-MM-yyyy')) AND :expiryDate is NOT null) OR (:expiryDate is null AND f.EXPIRY_DATE IS NULL)) ");
    }*/

        countQuery.append("  AND ((nvl(trunc(f.INSERT_DATE),sysdate+1) >= nvl(to_date(:fromInsertDate,'dd-MM-yyyy'),sysdate+1)) OR (f.INSERT_DATE is not null AND :fromInsertDate is null))\n" +
        "  AND ((nvl(trunc(f.INSERT_DATE),sysdate+1) <= nvl(to_date(:toInsertDate,'dd-MM-yyyy'),sysdate+1)) OR (f.INSERT_DATE is not null AND :toInsertDate is null))\n" +
        /*"  AND (((lower(nvl(trunc(f.INSERT_DATE),sysdate+1)) >= lower(nvl(to_date(:fromInsertDate,'DD-MM-RR'),sysdate+1))) AND (lower(nvl(trunc(f.INSERT_DATE),sysdate+1)) <= lower(nvl(to_date(:toInsertDate,'DD-MM-RR'),sysdate+1)))) OR (f.EXPIRY_DATE is not null AND (:fromInsertDate is null AND :toInsertDate is null)))\n" +*/
        "  AND ((to_char(nvl(f.TT_DAYS,0),'99.99') = to_char(nvl(:ttDays,0),'99.99')) OR (f.TT_DAYS is not null AND :ttDays is null))\n" + "   AND ((to_char(nvl(f.BASE_FREIGHT,0),'9999999.99') = to_char(nvl(:baseFreight,0),'9999999.99')) OR (f.BASE_FREIGHT is not null AND :baseFreight is null))\n" +
        "  AND ((lower(nvl(f.status,'Unapproved')) = lower(nvl(:status,'Unapproved'))) OR (f.status is not null AND :status is null))");

    Query countQ = entityManager.createNativeQuery(countQuery.toString())
            //  "  order by f.EFFECTIVE_DATE desc")
            .setParameter("transporterSapCode",transporterSapCode)
            .setParameter("servprov",servprov)
            .setParameter("sourceLoc",sourceLoc)
            .setParameter("sourceDesc",sourceDesc)
            .setParameter("destLoc",destLoc)
            .setParameter("destDesc",destDesc)
            .setParameter("truckType",truckType)
            .setParameter("condition1",condition1)
            .setParameter("effectiveDate",effectiveDate)
//            .setParameter("expiryDate",expiryDate)
            .setParameter("ttDays",ttDays)
            .setParameter("baseFreight",baseFreight)
            .setParameter("fromInsertDate",fromInsertDate)
            .setParameter("toInsertDate",toInsertDate)
            .setParameter("status",status);

    if (!StringUtils.isEmpty(expiryDateFilterType)){
      if (("BETWEEN").equals(expiryDateFilterType)){
        /*BETWEEN FILTER*/
        if (!StringUtils.isEmpty(fromExpiryDate) && !StringUtils.isEmpty(toExpiryDate)){
          /*IF BOTH DATES NOT NULL*/
          countQ.setParameter("fromExpiryDate",fromExpiryDate)
              .setParameter("toExpiryDate",toExpiryDate);
        }
      }
    }


    List<Object> countObject = countQ.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    //  System.out.println("******************Count Value:" + Integer.valueOf(countObject.get(0).toString()) );
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    while(it.hasNext()) {
      Object[] line = (Object[]) it.next();
      Freight freight = new Freight();
      freight.setId(Double.valueOf((line[0]!=null)?line[0].toString():"0"));
      freight.setTransporterSapCode((line[1]!=null)?line[1].toString():"");
      freight.setServprov((line[2]!=null)?line[2].toString():"");
      freight.setSourceLoc((line[3]!=null)?line[3].toString():"");
      freight.setSourceDesc((line[4]!=null)?line[4].toString():"");
      freight.setDestLoc((line[5]!=null)?line[5].toString():"");
      freight.setDestDesc((line[6]!=null)?line[6].toString():"");
      freight.setLaneCode((line[7]!=null)?line[7].toString():"");
      freight.setTruckType((line[8]!=null)?line[8].toString():"");
      freight.setCondition1((line[9]!=null)?line[9].toString():"");
      freight.setCondition2((line[10]!=null)?line[10].toString():"");
      try {
//        Calendar cal = Calendar.getInstance();
//        cal.setTime((Date) line[11]);
//        cal.add(Calendar.DATE, 1);
//        freight.setEffectiveDate(cal.getTime());
        freight.setEffectiveDate((Date) line[11]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting effective date");
        freight.setEffectiveDate(null);
      }
      try {
        freight.setExpiryDate((Date)line[12]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting setExpiryDate date");
        freight.setExpiryDate(null);
      }
      freight.setTtDays(Double.valueOf((line[13]!=null)?line[13].toString():"0"));
      freight.setBaseFreight(Double.valueOf((line[14]!=null)?line[14].toString().trim():"0"));
      freight.setBaseFreightUom((line[15]!=null)?line[15].toString():"");
      freight.setBasis((line[16]!=null)?line[16].toString():"");
      freight.setMinValue(Double.valueOf((line[17]!=null)?line[17].toString():"0"));
      freight.setMinValueUom((line[17]!=null)?line[17].toString():"");
      freight.setMinValueUom((line[18]!=null)?line[18].toString():"");
      freight.setApproval1User((line[19]!=null)?line[19].toString():"");
      try {
//        freight.setApproval1Date(DateUtils.formatDate(line[20].toString(), Constants.ORA_DATE_FORMAT));
        freight.setApproval1Date(line[20] != null ? (Date) line[20] : null);
      }catch(Exception e){
        freight.setApproval1Date(null);
      }
      freight.setApproval2User((line[21]!=null)?line[21].toString():"");
      try {
//        freight.setApproval2Date((DateUtils.formatDate(line[22].toString(), Constants.ORA_DATE_FORMAT)));
        freight.setApproval2Date(line[22] != null ? (Date) line[22] : null);

      }catch(Exception e){
        freight.setApproval2Date(null);
      }
      freight.setStatus((line[23]!=null)?line[23].toString():"");
      freight.setInsertUser((line[24]!=null)?line[24].toString():"");
      try {
//        freight.setInsertDate(DateUtils.formatDate(line[25].toString(), Constants.ORA_DATE_FORMAT));
        freight.setInsertDate(line[25] != null ? (Date) line[25] : null);
      }catch(Exception e){
        freight.setInsertDate(null);
      }
      freight.setUpdateUser((line[26]!=null)?line[26].toString():"");
      try {
//        freight.setUpdateDate(DateUtils.formatDate(line[27].toString(), Constants.ORA_DATE_FORMAT));
        freight.setUpdateDate(line[27] != null ? (Date) line[27] : null);
      }catch(Exception e){
        freight.setUpdateDate(null);
      }
      freight.setPreviousRate(Double.valueOf((line[28]!=null)?line[28].toString():"0"));
      freight.setDiff((line[29]!=null)?line[29].toString():"0");
      freight.setPercentile((line[30]!=null)?line[30].toString():"0");
      freight.setTransportMode((line[31]!=null)?line[31].toString():"");
      freight.setRateType((line[32]!=null)?line[32].toString():"");
      freight.setLoading(Double.valueOf((line[33]!=null)?line[33].toString():"0"));
      freight.setUnLoading(Double.valueOf((line[34]!=null)?line[34].toString():"0"));
      freight.setOthers1(Double.valueOf((line[35]!=null)?line[35].toString():"0"));
      freight.setOthers1Code((line[36]!=null)?line[36].toString():"");
      freight.setOthers2(Double.valueOf((line[37]!=null)?line[37].toString():"0"));
      freight.setOthers2Code((line[38]!=null)?line[38].toString():"");
      freight.setOthers3(Double.valueOf((line[39]!=null)?line[39].toString():"0"));
      freight.setOthers3Code((line[40]!=null)?line[40].toString():"");

      freight.setDistance(Double.valueOf((line[41]!=null)?line[41].toString():"0"));
      freight.setTotalExpense(Double.valueOf((line[42]!=null)?line[42].toString():"0"));
      freight.setPayableTransporter(Double.valueOf((line[43]!=null)?line[43].toString():"0"));
      freight.setSourceType((line[44]!=null)?line[44].toString():"");
      freight.setRemarks((line[45]!=null)?line[45].toString():"");

      freightsList.add(freight);
    }
    freightsPageList = new PageImpl<Freight>(freightsList);
    return new PageImpl<Freight>(freightsList,PageRequest.of(freightFilterDto.getIndex(), freightFilterDto.getPageLength()), count);
  }



 //"upper(nvl(location_id,'-')) like '%'|| upper(:locationId)||'%' \n" +
 //        "and upper (nvl(bay_id,'-')) like '%'|| upper(:bayId)||'%' \n" +
 //        "and upper (nvl(bay_status,'-')) like '%'|| upper(:bayStatus)||'%' order by location_id, bay_id")
  public Page<MTTransporter> filterTransporters(TransporterFilterDto transporterFilterDto) {
    List<MTTransporter> transportersList = new ArrayList<MTTransporter>();
    Page<MTTransporter> transportersPageList;
    Query q = entityManager.createNativeQuery("select " +
            "  t.transporter_id as \"id\",\n" +
            "      t.transporter_desc as description,\n" +
            "      t.servprov as servprov,\n" +
            "      t.transporter_address as address,\n" +
            "      t.city as city,\n" +
            "      t.state as state,\n" +
            "      t.postal_code  as postalCode,\n" +
            "      t.country as country,\n" +
            "      t.is_active as isActive,\n" +
            "      t.insert_user as insertUser,\n" +
            "      t.insert_date as insertDate,\n" +
            "      t.update_user as updateUser,\n" +
            "      t.update_date as updateDate,\n" +
            "      t.industry_key as industryKey,\n" +
            "      t.state_code as stateCode,\n" +
            "      t.gst_no as gstNo,\n" +
            "      t.gst_state as gstState,\n" +
            "      t.pan_no as panNo\n" +
            "from MT_TRANSPORTER t \n" +
            "where " +
            "upper (nvl(t.transporter_id,'-')) like '%'||upper(:transporterId)||'%' \n" +
            "and upper (nvl(t.transporter_desc,'-')) like '%'||upper(:transporterDesc)||'%' \n" +
            "and upper (nvl(t.servprov,'-')) like '%'||upper(:servProv)||'%' order by t.transporter_id")
            .setParameter("transporterId", transporterFilterDto.getTransporterId())
            .setParameter("transporterDesc", transporterFilterDto.getTransporterDesc())
            .setParameter("servProv", transporterFilterDto.getServprov())
            .setFirstResult(transporterFilterDto.getIndex()*transporterFilterDto.getPageLength())
            .setMaxResults(transporterFilterDto.getPageLength());
    List<Object[]> res = q.getResultList();

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(t.transporter_id) \n" +
                    "FROM MT_TRANSPORTER t\n" +
                    "where " +
                    "upper (nvl(t.transporter_id,'-')) like '%'||upper(:transporterId)||'%' \n" +
                    "and upper (nvl(t.transporter_desc,'-')) like '%'||upper(:transporterDesc)||'%' \n" +
                    "and upper (nvl(t.servprov,'-')) like '%'||upper(:servProv)||'%' ")
            .setParameter("transporterId", transporterFilterDto.getTransporterId())
            .setParameter("transporterDesc", transporterFilterDto.getTransporterDesc())
            .setParameter("servProv", transporterFilterDto.getServprov());
          //  .setFirstResult(transporterFilterDto.getIndex()*transporterFilterDto.getPageLength())
          //  .setMaxResults(transporterFilterDto.getPageLength());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());

    Iterator it = res.iterator();
    while (it.hasNext()) {
      Object[] line = (Object[]) it.next();
      MTTransporter transporter = new MTTransporter();
      transporter.setId((line[0] != null) ? line[0].toString() : "");
      transporter.setDescription((line[1] != null) ? line[1].toString() : "");
      transporter.setServprov((line[2] != null) ? line[2].toString() : "");
      transporter.setAddress((line[3] != null) ? line[3].toString() : "");
      transporter.setCity((line[4] != null) ? line[4].toString() : "");
      transporter.setState((line[5] != null) ? line[5].toString() : "");
      transporter.setPostalCode((line[6] != null) ? line[6].toString() : "");
      transporter.setCountry((line[7] != null) ? line[7].toString() : "");
      transporter.setIsActive((line[8] != null) ? line[8].toString() : "");
      transporter.setInsertUser((line[9] != null) ? line[9].toString() : "");
      try{
        transporter.setInsertDate((Date)line[10]);
        // transporter.setInsertDate(DateUtils.formatDate(line[10].toString(), Constants.ORA_DATE_FORMAT));
      }catch(Exception e){
        transporter.setInsertDate(null);
      }
      transporter.setUpdateUser((line[11] != null) ? line[11].toString() : "");
      try{
        transporter.setUpdateDate((Date)line[12]);
        // transporter.setUpdateDate(DateUtils.formatDate(line[12].toString(), Constants.ORA_DATE_FORMAT));
      }catch(Exception e){
        transporter.setUpdateDate(null);
      }
      transporter.setIndustryKey((line[13] != null) ? line[13].toString() : "");
      transporter.setStateCode((line[14] != null) ? line[14].toString() : "");
      transporter.setGstNo((line[15] != null) ? line[15].toString() : "");
      transporter.setGstState((line[16] != null) ? line[16].toString() : "");
      transporter.setPanNo((line[17] != null) ? line[17].toString() : "");
      transportersList.add(transporter);
    }
    transportersPageList= new PageImpl<MTTransporter>(transportersList);
    // return transportersPageList;
    return new PageImpl<MTTransporter>(transportersList,PageRequest.of(transporterFilterDto.getIndex(), transporterFilterDto.getPageLength()), count);
    //return new PageImpl<MTTransporter>(transportersPageList,PageRequest.of(transporterFilterDto.getIndex(), transporterFilterDto.getPageLength()), count);
  }


  public Page<LocationScan> getLocationScan(LocationScanFilterDto filterDto) {
    List<LocationScan> itemList = new ArrayList<LocationScan>();
    Page<LocationScan> itemPageList;
    Query q = entityManager.createNativeQuery("SELECT\n" +
            "f.LOCATION_ID\tas locationId,\n" +
            "f.SCANNABLE\tas scannable,\n" +
            "f.ITEM_CATEGORY\tas itemCategory,\n" +
            "f.INSERT_USER\tas insertUser,\n" +
            "f.UPDATE_USER\tas updateUser,\n" +
            "f.INSERT_DATE\tas insertDate,\n" +
            "f.UPDATE_DATE\tas updateDate \n" +
            "from LOCATION_SCAN f \n" +
            "where \n" +
            "upper(f.LOCATION_ID)like '%' || upper(:locationId)||'%' \n" +
            "and upper (f.ITEM_CATEGORY) like '%' || upper(:itemCategory)||'%' \n" +
            "and upper (f.SCANNABLE) like '%' || upper(:scannable)||'%' order by f.LOCATION_ID")
//            "WHERE ((lower(nvl(f.LOCATION_ID,'-')) = lower(nvl(:locationId,'-'))) OR (f.LOCATION_ID is not null AND :locationId is null))\n" +
//            " AND ((lower(nvl(f.ITEM_CATEGORY,'-')) = lower(nvl(:itemCategory,'-'))) OR (f.ITEM_CATEGORY is not null AND :itemCategory is null))\n" +
//            " AND ((lower(nvl(f.SCANNABLE,'-')) = lower(nvl(:scannable,'-'))) OR (f.SCANNABLE is not null AND :scannable is null))")
            .setParameter("locationId", filterDto.getLocationId())
            .setParameter("scannable", filterDto.getScannable())
            .setParameter("itemCategory", filterDto.getItemCategory())
            .setFirstResult(filterDto.getIndex() * filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(f.LOCATION_ID) \n" +
                    "FROM LOCATION_SCAN f\n" +
                    "WHERE " +
                    "upper(f.LOCATION_ID)like '%' || upper(:locationId)||'%' \n" +
                    "and upper (f.ITEM_CATEGORY) like '%' || upper(:itemCategory)||'%' \n" +
                    "and upper (f.SCANNABLE) like '%' || upper(:scannable)||'%'")
            .setParameter("locationId", filterDto.getLocationId())
            .setParameter("scannable", filterDto.getScannable())
            .setParameter("itemCategory", filterDto.getItemCategory());

    List<Object> countObject = countQuery.getResultList();
    int count = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    LocationScan itemBean;
    Object[] line;
    while (it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new LocationScan();
      itemBean.setLocationId(((line[0] != null) ? line[0].toString() : "0"));
      itemBean.setScannable((line[1] != null) ? line[1].toString() : "");
      itemBean.setItemCategory((line[2] != null) ? line[2].toString() : "");
      itemBean.setInsertUser(((line[3] != null) ? line[3].toString() : ""));
      itemBean.setUpdateUser(((line[4] != null) ? line[4].toString() : ""));
      try {
        itemBean.setInsertDate((line[5] != null) ? (Date) line[5] : null);
        itemBean.setUpdateDate((line[6] != null) ? (Date) line[6] : null);
      } catch (Exception e) {
        System.out.println("*********** Exception while formatting effective date");
        itemBean.setInsertDate(null);
      }
      itemList.add(itemBean);
    }
    return new PageImpl<LocationScan>(itemList, PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }


  public Page<MtBatchCodes> getMtBatchCodes(MtBatchCodesFilterDto filterDto) {
    System.out.println(" Index  "+filterDto.getIndex() );
    List<MtBatchCodes> itemsList = new ArrayList<MtBatchCodes>();
    Query q = entityManager.createNativeQuery("select " +
            "mbc.batch_code,\n" +
            "mbc.category,\n" +
            "mbc.plant_code,\n" +
            "mbc.batch_description,\n" +
            "mbc.insert_user,\n" +
            "mbc.update_user,\n" +
            "mbc.insert_date,\n" +
            "mbc.update_date, \n" +
            "mbc.bc_id \n" +
            "from mt_batch_codes mbc \n" +
            "where \n" +
            "upper (nvl(mbc.batch_code,'-')) like '%'||upper(:batchCode)||'%'\n" +
            "and upper (nvl(mbc.category,'-')) like '%'||upper(:category)||'%'\n" +
            "and nvl(mbc.plant_code,0) = nvl(:plantCode, nvl(mbc.plant_code,0)) order by mbc.batch_code asc")
            .setParameter("batchCode", filterDto.getBatchCode())
            .setParameter("category", filterDto.getCategory())
            .setParameter("plantCode", filterDto.getPlantCode())
            .setFirstResult(filterDto.getIndex() * filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT\n" +
                    "count(mbc.BC_ID) \n" +
                    "from MT_BATCH_CODES mbc \n" +
                    "WHERE " +
                    "upper (nvl(mbc.batch_code,'-')) like '%'||upper(:batchCode)||'%'\n" +
                    "and upper (nvl(mbc.category,'-')) like '%'||upper(:category)||'%'\n" +
                    "and nvl(mbc.plant_code,0) = nvl(:plantCode, nvl(mbc.plant_code,0))")
            .setParameter("batchCode", filterDto.getBatchCode())
            .setParameter("category", filterDto.getCategory())
            .setParameter("plantCode", filterDto.getPlantCode());

    List<Object> countObject = countQuery.getResultList();
    int count = Integer.valueOf(countObject.get(0).toString());
    System.out.println(" count  "+count );
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    MtBatchCodes itemBean;
    Object[] line;
    while (it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new MtBatchCodes();
      itemBean.setBatchCode(((line[0] != null) ? line[0].toString() : "0"));
      itemBean.setCategory((line[1] != null) ? line[1].toString() : "");
      itemBean.setPlantCode((line[2] != null) ? line[2].toString() : "");
      itemBean.setBatchDescription((line[3] != null) ? line[3].toString() : "");
      itemBean.setInsertUser(((line[4] != null) ? line[4].toString() : ""));
      itemBean.setUpdateUser(((line[5] != null) ? line[5].toString() : ""));
      try {
        itemBean.setInsertDate((line[6] != null) ? (Date) line[6] : null);
        itemBean.setUpdateDate((line[7] != null) ? (Date) line[7] : null);
      } catch (Exception e) {
        System.out.println("*********** Exception while formatting effective date");
        itemBean.setInsertDate(null);
      }
      itemBean.setBcId(Double.valueOf((line[8] != null) ? line[8].toString() : "0"));
      itemsList.add(itemBean);
    }
    return new PageImpl<MtBatchCodes>(itemsList, PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }



  public Page<MtTruckType> getMtTruckType(MtTruckTypeFilterDto filterDto){
    List<MtTruckType> itemsList  = new ArrayList<MtTruckType>();
    System.out.println("BEFORE CALLING sql STATEMENT:  "+new Gson().toJson(filterDto));
    Query q = entityManager.createNativeQuery("SELECT\n" +
            "f.TRUCK_TYPE\tas truckType,\n" +
            "f.TRUCK_DESC\tas truckDesc,\n" +
            "f.LOAD_FACTOR\tas loadFactor,\n" +
            "f.TTE_CAPACITY\tas tteCapacity,\n" +
            "f.GROSS_WT\tas grossWt,\n" +
            "f.GROSS_WT_UOM\tas grossWtUom,\n" +
            "f.GROSS_VOL\tas grossVol,\n" +
            "f.GROSS_VOL_UOM\tas grossVolUom,\n" +
            "f.VARIANT1\tas variant1,\n" +
            "f.VARIANT2\tas variant2, \n" +
            "f.TT_ID\tas ttId, \n" +
            "f.INSERT_USER\tas insertUser,\n" +
            "f.UPDATE_USER\tas updateUser,\n" +
            "f.INSERT_DATE\tas insertDate,\n" +
            "f.UPDATE_DATE\tas updateDate \n" +
            "from MT_TRUCK_TYPE f\n" +
            "WHERE " +
            "nvl(upper(f.TRUCK_TYPE),'-') like '%' || upper(:truckType)||'%' \n" +
            "and nvl(upper (f.TRUCK_DESC),'-') like '%' || upper(:truckDesc)||'%' \n" +
            "and nvl(upper(f.LOAD_FACTOR),'-')like '%' || upper(:loadFactor)||'%' \n" +
            "and nvl(upper (f.TTE_CAPACITY),'-') like '%' || upper(:tteCapacity)||'%' \n" +
            "and nvl(upper (f.VARIANT1),'-') like '%' || upper(:variant1)||'%' order by f.TRUCK_TYPE ")
            .setParameter("truckType",filterDto.getTruckType())
            .setParameter("truckDesc",filterDto.getTruckDesc())
            .setParameter("loadFactor",filterDto.getLoadFactor())
            .setParameter("tteCapacity",filterDto.getTteCapacity())
            .setParameter("variant1",filterDto.getVariant1())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT\n" +
                    "    count(f.TRUCK_TYPE) \n" +
                    "from MT_TRUCK_TYPE f\n" +
                    "WHERE " +
                    "nvl(upper(f.TRUCK_TYPE),'-') like '%' || upper(:truckType)||'%' \n" +
                    "and nvl(upper (f.TRUCK_DESC),'-') like '%' || upper(:truckDesc)||'%' \n" +
                    "and nvl(upper(f.LOAD_FACTOR),'-')like '%' || upper(:loadFactor)||'%' \n" +
                    "and nvl(upper (f.TTE_CAPACITY),'-') like '%' || upper(:tteCapacity)||'%' \n" +
                    "and nvl(upper (f.VARIANT1),'-') like '%' || upper(:variant1)||'%'")
            .setParameter("truckType",filterDto.getTruckType())
            .setParameter("truckDesc",filterDto.getTruckDesc())
            .setParameter("loadFactor",filterDto.getLoadFactor())
            .setParameter("tteCapacity",filterDto.getTteCapacity())
            .setParameter("variant1",filterDto.getVariant1());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());

    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    MtTruckType itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new MtTruckType();
      itemBean.setTruckType(((line[0]!=null)?line[0].toString():""));
      itemBean.setTruckDesc(((line[1]!=null)?line[1].toString():""));
      itemBean.setLoadFactor(Double.valueOf((line[2]!=null)?line[2].toString():"0"));
      itemBean.setTteCapacity(Double.valueOf((line[3]!=null)?line[3].toString():"0"));
      itemBean.setGrossWt(Double.valueOf((line[4]!=null)?line[4].toString():"0"));
      itemBean.setGrossWtUom(((line[5]!=null)?line[5].toString():""));
      itemBean.setGrossVol(Double.valueOf((line[6]!=null)?line[6].toString():"0"));
      itemBean.setGrossVolUom(((line[7]!=null)?line[7].toString():""));
      itemBean.setVariant1(((line[8]!=null)?line[8].toString():""));
      itemBean.setVariant2(((line[9]!=null)?line[9].toString():""));
      itemBean.setTtId(Double.valueOf((line[10]!=null)?line[10].toString():"0"));
      itemBean.setInsertUser(((line[11] != null) ? line[11].toString() : ""));
      itemBean.setUpdateUser(((line[12] != null) ? line[12].toString() : ""));
      try {
        itemBean.setInsertDate((line[13] != null) ? (Date) line[13] : null);
        itemBean.setUpdateDate((line[14] != null) ? (Date) line[14] : null);
      } catch (Exception e) {
        System.out.println("*********** Exception while formatting effective date");
        itemBean.setInsertDate(null);
      }
      itemsList.add(itemBean);
    }
    return new PageImpl<MtTruckType>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }


  public Page<MtSapTruckType> getMtSapTruckType(MtSapTruckTypeFilterDto filterDto) {
    List<MtSapTruckType> itemsList = new ArrayList<MtSapTruckType>();
    Query q = entityManager.createNativeQuery("SELECT\n" +
            "f.SAP_TRUCK_TYPE\tas sapTruckType,\n" +
            "f.SAP_TRUCK_TYPE_DESC\tas sapTruckTypeDesc,\n" +
            "f.OPS_TRUCK_TYPE\tas opsTruckType,\n" +
            "f.OPS_VARIANT_1\tas opsVariant1, \n" +
            "f.STT_ID\tas sttId, \n" +
            "f.INSERT_USER\tas insertUser,\n" +
            "f.UPDATE_USER\tas updateUser,\n" +
            "f.INSERT_DATE\tas insertDate,\n" +
            "f.UPDATE_DATE\tas updateDate \n" +
            "from MT_SAP_TRUCK_TYPE f \n" +
            "WHERE \n" +
            "nvl(upper(f.SAP_TRUCK_TYPE),'-')like '%' || upper(:sapTruckType)||'%'   \n" +
            "and nvl(upper (f.SAP_TRUCK_TYPE_DESC),'-') like '%' || upper(:sapTruckTypeDesc)||'%' \n" +
            "and nvl(upper (f.OPS_TRUCK_TYPE),'-') like '%' || upper(:opsTruckType)||'%' \n" +
            "and nvl(upper (f.OPS_VARIANT_1),'-') like '%' || upper(:opsVariant1)||'%' order by f.SAP_TRUCK_TYPE")
            .setParameter("sapTruckType", filterDto.getSapTruckType())
            .setParameter("sapTruckTypeDesc", filterDto.getSapTruckTypeDesc())
            .setParameter("opsTruckType", filterDto.getOpsTruckType())
            .setParameter("opsVariant1", filterDto.getOpsVariant1())
            .setFirstResult(filterDto.getIndex() * filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT\n" +
                    "    count(f.SAP_TRUCK_TYPE) \n" +
                    "from MT_SAP_TRUCK_TYPE f\n" +
                    "WHERE \n" +
                    "nvl(upper(f.SAP_TRUCK_TYPE),'-')like '%' || upper(:sapTruckType)||'%'   \n" +
                    "and nvl(upper (f.SAP_TRUCK_TYPE_DESC),'-') like '%' || upper(:sapTruckTypeDesc)||'%' \n" +
                    "and nvl(upper (f.OPS_TRUCK_TYPE),'-') like '%' || upper(:opsTruckType)||'%' \n" +
                    "and nvl(upper (f.OPS_VARIANT_1),'-') like '%' || upper(:opsVariant1)||'%'")
//                    "((lower(nvl(f.SAP_TRUCK_TYPE,'-')) = lower(nvl(:sapTruckType,'-'))) OR (f.SAP_TRUCK_TYPE is not null AND :sapTruckType is null))\n" +
//                    " AND ((lower(nvl(f.SAP_TRUCK_TYPE_DESC,'-')) = lower(nvl(:sapTruckTypeDesc,'-'))) OR (f.SAP_TRUCK_TYPE_DESC is not null AND :sapTruckTypeDesc is null))\n" +
//                    " AND ((lower(nvl(f.OPS_TRUCK_TYPE,'-')) = lower(nvl(:opsTruckType,'-'))) OR (f.OPS_TRUCK_TYPE is not null AND :opsTruckType is null))\n" +
//                    " AND ((lower(nvl(f.OPS_VARIANT_1,'-')) = lower(nvl(:opsVariant1,'-'))) OR (f.OPS_VARIANT_1 is not null AND :opsVariant1 is null))")
            .setParameter("sapTruckType", filterDto.getSapTruckType())
            .setParameter("sapTruckTypeDesc", filterDto.getSapTruckTypeDesc())
            .setParameter("opsTruckType", filterDto.getOpsTruckType())
            .setParameter("opsVariant1", filterDto.getOpsVariant1());

    List<Object> countObject = countQuery.getResultList();
    int count = Integer.valueOf(countObject.get(0).toString());
    System.out.println("******************Count Value:" + Integer.valueOf(countObject.get(0).toString()));
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    MtSapTruckType itemBean;
    Object[] line;
    while (it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new MtSapTruckType();
      itemBean.setSapTruckType(((line[0] != null) ? line[0].toString() : "0"));
      itemBean.setSapTruckTypeDesc((line[1] != null) ? line[1].toString() : "");
      itemBean.setOpsTruckType((line[2] != null) ? line[2].toString() : "");
      itemBean.setOpsVariant1((line[3] != null) ? line[3].toString() : "");
      itemBean.setSttId(Double.valueOf((line[4]!=null)?line[4].toString():"0"));
      itemBean.setInsertUser(((line[5] != null) ? line[5].toString() : ""));
      itemBean.setUpdateUser(((line[6] != null) ? line[6].toString() : ""));
      try {
        itemBean.setInsertDate((line[7] != null) ? (Date) line[7] : null);
        itemBean.setUpdateDate((line[8] != null) ? (Date) line[8] : null);
      } catch (Exception e) {
        System.out.println("*********** Exception while formatting effective date");
        itemBean.setInsertDate(null);
      }
      itemsList.add(itemBean);
    }
    return new PageImpl<MtSapTruckType>(itemsList, PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }





  public Page<MtMaterialGroup> getMtMaterialGroup(MtMaterialGroupFilterDto filterDto){

    List<MtMaterialGroup> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("SELECT \n" +
            "f.MATERIAL_GROUP_ID\tas materialGroupId,\n" +
            "f.DESCRIPTION_1\tas description1,\n" +
            "f.DESCRIPTION_2\tas description2,\n" +
            "f.SCM_GROUP\tas scmGroup, \n" +
            "f.MG_ID\tas mgId, \n" +
            "f.INSERT_USER AS insertuser,\n" +
            "f.INSERT_DATE AS insertdate,\n" +
            "f.UPDATE_USER AS updateuser,\n" +
            "f.UPDATE_DATE AS updatedate \n" +
            "FROM MT_MATERIAL_GROUP f \n" +
            "WHERE " +
            " nvl(upper(f.MATERIAL_GROUP_ID),'-') like '%' || upper(:materialGroupId)||'%'" +
            "and nvl(upper (f.SCM_GROUP),'-') like '%' || upper(:scmGroup)||'%' order by f.MATERIAL_GROUP_ID ")
//            "((lower(nvl(f.MATERIAL_GROUP_ID,'-')) = lower(nvl(:materialGroupId,'-'))) OR (f.MATERIAL_GROUP_ID is not null AND :materialGroupId is null))\n" +
//            " AND ((lower(nvl(f.SCM_GROUP,'-')) = lower(nvl(:scmGroup,'-'))) OR (f.SCM_GROUP is not null AND :scmGroup is null))")
//            "WHERE ((lower(nvl(f.MATERIAL_GROUP_ID,'-')) = lower(nvl(:materialGroupId,'-'))) OR (f.MATERIAL_GROUP_ID is not null AND :materialGroupId is null))\n" +
//            "AND((lower(nvl(f.SCM_GROUP,'-')) = lower(nvl(:scmGroup,'-'))) OR (f.SCM_GROUP is not null AND :scmGroup is null))")
            .setParameter("materialGroupId",filterDto.getMaterialGroupId())
            .setParameter("scmGroup",filterDto.getScmGroup())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(f.MATERIAL_GROUP_ID) \n" +
                    "FROM MT_MATERIAL_GROUP f\n" +
                    "WHERE \n" +
                    "nvl(upper(f.MATERIAL_GROUP_ID),'-') like '%' || upper(:materialGroupId)||'%' \n" +
                    "and nvl(upper(f.SCM_GROUP),'-') like '%' || upper(:scmGroup)||'%'" )
            .setParameter("materialGroupId",filterDto.getMaterialGroupId())
            .setParameter("scmGroup",filterDto.getScmGroup());
    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    MtMaterialGroup itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new MtMaterialGroup();
      itemBean.setMaterialGroupId((line[0]!=null)?line[0].toString():"0");
      itemBean.setDescription_1((line[1]!=null)?line[1].toString():"");
      itemBean.setDescription_2((line[2]!=null)?line[2].toString():"");
      itemBean.setScmGroup((line[3]!=null)?line[3].toString():"");
      itemBean.setMgId(Double.valueOf((line[4]!=null)?line[4].toString():"0"));
      itemBean.setInsertUser((line[5]!=null)?line[5].toString():"");
      itemBean.setUpdateUser((line[7]!=null)?line[7].toString():"");
      try {
        itemBean.setInsertDate((Date) line[6]);
        itemBean.setUpdateDate((Date) line[8]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting Insert date");
        itemBean.setInsertDate(null);
      }

      itemsList.add(itemBean);
    }
    return new PageImpl<MtMaterialGroup>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }



  public Page<OrderTypeLookup> getOrderTypeLookup(OrderTypeLookupFilterDto filterDto){
    List<OrderTypeLookup> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "order_type,\n" +
            "movement_type,\n" +
            "market_segment,\n" +
            "sap_order_type,\n" +
            "sap_doc_type,\n" +
            "insert_user,\n" +
            "insert_date,\n" +
            "update_user,\n" +
            "update_date, \n" +
            "bom_type \n" +
            "from " +
            "order_type_lookup " +
            "where " +
            "upper(order_type) like '%'||upper(:orderType) ||'%'\n" +
            "and upper(movement_type) like '%'||upper(:movementType) ||'%'\n" +
            "and upper(market_segment) like '%'||upper(:marketSegment) ||'%'\n" +
            "and upper(sap_order_type) like '%'||upper(:sapOrderType) ||'%'\n" +
            "and upper(sap_doc_type) like '%'||upper(:sapDocType) ||'%' \n" +
            "and ((upper(bom_type) like '%'||upper(:bomType) ||'%') OR bom_type is null) \n ")
            .setParameter("orderType",filterDto.getOrderType())
            .setParameter("movementType",filterDto.getMovementType())
            .setParameter("marketSegment",filterDto.getMarketSegment())
            .setParameter("sapOrderType",filterDto.getSapOrderType())
            .setParameter("sapDocType",filterDto.getSapDocType())
            .setParameter("bomType",filterDto.getBomType())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(order_type) \n" +
                    "FROM order_type_lookup \n" +
                    "where " +
                    "upper(order_type) like '%' || upper(:orderType)||'%'\n" +
                    "and upper(movement_type) like '%' || upper(:movementType)||'%'\n" +
                    "and upper(market_segment) like '%' || upper(:marketSegment)||'%'\n" +
                    "and upper(sap_order_type) like '%' || upper(:sapOrderType)||'%'\n" +
                    "and upper(sap_doc_type) like '%'||upper(:sapDocType) ||'%' \n" +
                    "and upper(bom_type) like '%'||upper(:bomType) ||'%' \n ")
            .setParameter("orderType", filterDto.getOrderType())
            .setParameter("movementType", filterDto.getMovementType())
            .setParameter("marketSegment", filterDto.getMarketSegment())
            .setParameter("sapOrderType", filterDto.getOrderType())
            .setParameter("sapDocType", filterDto.getSapDocType())
            .setParameter("bomType",filterDto.getBomType());
    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    OrderTypeLookup itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new OrderTypeLookup();
      itemBean.setOrderType((line[0]!=null)?line[0].toString():"0");
      itemBean.setMovementType((line[1]!=null)?line[1].toString():"");
      itemBean.setMarketSegment((line[2]!=null)?line[2].toString():"");
      itemBean.setSapOrderType((line[3]!=null)?line[3].toString():"");
      itemBean.setSapDocType((line[4]!=null)?line[4].toString():"0");
      itemBean.setInsertUser((line[5]!=null)?line[5].toString():"");
      itemBean.setUpdateUser((line[7]!=null)?line[7].toString():"");
      itemBean.setBomType((line[9]!=null)?line[9].toString():"");
      try {
        itemBean.setInsertDate((Date) line[6]);
        itemBean.setUpdateDate((Date) line[8]);
      }catch(Exception e){
        itemBean.setInsertDate(null);
      }
      itemsList.add(itemBean);
    }
    return new PageImpl<OrderTypeLookup>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }

  public Page<MTValve> getMTValve(MTValveFilterDto filterDto){
    List<MTValve> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "item_id, \n" +
            "item_description, \n" +
            "item_category, \n" +
            "batch_code, \n" +
            "insert_user, \n" +
            "insert_date, \n" +
            "update_user, \n" +
            "update_date, \n" +
            "valve_id \n" +
            "from mt_valve \n" +
            "where " +
            "upper(item_id) like '%'|| upper(:itemId)||'%' \n" +
            "and upper(item_description) like '%'|| upper(:itemDesc)||'%' \n" +
            "and upper(item_category) like '%'|| upper(:itemCategory)||'%' \n" +
            "and upper(batch_code) like '%'|| upper(:batchCode)||'%'")
            .setParameter("itemId",filterDto.getItemId())
            .setParameter("itemDesc",filterDto.getItemDescription())
            .setParameter("itemCategory",filterDto.getItemCategory())
            .setParameter("batchCode",filterDto.getBatchCode())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(item_id) \n" +
                    "FROM mt_valve \n" +
                    "where " +
                    "upper(item_id) like '%' || upper(:itemId)||'%' \n" +
                    "and upper(item_description) like '%'|| upper(:itemDesc)||'%' \n" +
                    "and upper(item_category) like '%'|| upper(:itemCategory)||'%' \n" +
                    "and upper(batch_code) like '%'|| upper(:batchCode)||'%'")
            .setParameter("itemId",filterDto.getItemId())
            .setParameter("itemDesc",filterDto.getItemDescription())
            .setParameter("itemCategory",filterDto.getItemCategory())
            .setParameter("batchCode",filterDto.getBatchCode());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    MTValve itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new MTValve();
      itemBean.setItemId((line[0]!=null)?line[0].toString():"0");
      itemBean.setItemDescription((line[1]!=null)?line[1].toString():"");
      itemBean.setItemCategory((line[2]!=null)?line[2].toString():"");
      itemBean.setBatchCode((line[3]!=null)?line[3].toString():"");

      itemBean.setInsertUser((line[4]!=null)?line[4].toString():"");
      itemBean.setUpdateUser((line[6]!=null)?line[6].toString():"");
      try {
        itemBean.setInsertDate((Date) line[5]);
        itemBean.setUpdateDate((Date) line[7]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting Insert date");
        itemBean.setInsertDate(null);
      }
      itemBean.setValveId(Double.valueOf((line[8]!=null)?line[8].toString():"0"));
      itemsList.add(itemBean);
    }
    return new PageImpl<MTValve>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }



  public Page<MtScac> getServProList(MtScacFilterDto filterDto){
    System.out.println(new Gson().toJson(filterDto));
    List<MtScac> itemsList  = new ArrayList<>();
    Page<MtScac> itemPageList ;
    Query q = entityManager.createNativeQuery("select " +
            "scac, \n" +
            "company_name \n" +
            "from mt_scac \n" +
            "where " +
            "upper(scac) like '%'|| upper(:scac)||'%' \n" +
            "and upper(company_name) like '%'|| upper(:companyName)||'%' order by scac asc")
            .setParameter("scac",filterDto.getScac())
            .setParameter("companyName",filterDto.getCompanyName());

    Query countQuery = entityManager.createNativeQuery(
            "select " +
                    "count(scac) \n" +
                    "from mt_scac \n" +
                    "where " +
                    "upper(scac) like '%'|| upper(:scac)||'%' \n" +
                    "and upper(company_name) like '%'|| upper(:companyName)||'%'")
            .setParameter("scac",filterDto.getScac())
            .setParameter("companyName",filterDto.getCompanyName());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());

    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    MtScac itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new MtScac();
      itemBean.setScac((line[0]!=null)?line[0].toString():"0");
      itemBean.setCompanyName((line[1]!=null)?line[1].toString():"");
      itemsList.add(itemBean);
    }
    itemPageList = new PageImpl<MtScac>(itemsList);
   // return itemPageList;
    return new PageImpl<MtScac>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }





  @Override
  public Page<TrucksMetaData> filterTrucksMovement(ApplicationUser loggedInUser, TruckReportFilterDto truckReportFilterDto) {

    StringBuilder sb = setQueryForFilterTrucksMovement(loggedInUser, truckReportFilterDto);

    Query q = entityManager.unwrap(Session.class).createNativeQuery(sb.toString())
            .addScalar("truckNumber", StandardBasicTypes.STRING).addScalar("containerNum",StandardBasicTypes.STRING).addScalar("driverName", StandardBasicTypes.STRING)
            .addScalar("driverMobile", StandardBasicTypes.STRING).addScalar("driverLicense", StandardBasicTypes.STRING).addScalar("loadslipId", StandardBasicTypes.STRING)
            .addScalar("shipmentId", StandardBasicTypes.STRING).addScalar("gateInDate", StandardBasicTypes.DATE).addScalar("gateOutDate", StandardBasicTypes.DATE)
            .addScalar("reportDate", StandardBasicTypes.DATE).addScalar("inWeight", StandardBasicTypes.DOUBLE).addScalar("outWeight", StandardBasicTypes.DOUBLE)
            .addScalar("truckType", StandardBasicTypes.STRING).addScalar("servprov", StandardBasicTypes.STRING).addScalar("qty", StandardBasicTypes.INTEGER)
            .addScalar("sourceLoc", StandardBasicTypes.STRING).addScalar("destLoc", StandardBasicTypes.STRING).addScalar("stoSoNum", StandardBasicTypes.STRING)
            .addScalar("delivery", StandardBasicTypes.STRING).addScalar("sapInvoice", StandardBasicTypes.STRING).addScalar("sapInvoiceDate", StandardBasicTypes.DATE)
            .addScalar("lrNum", StandardBasicTypes.STRING).addScalar("lsPrintDate", StandardBasicTypes.DATE).addScalar("bayArrivedDate", StandardBasicTypes.DATE)
            .addScalar("loadingStartDate", StandardBasicTypes.DATE).addScalar("loadingEndDate", StandardBasicTypes.DATE).addScalar("confirmedDate", StandardBasicTypes.DATE)
            .addScalar("sendForBarcodeDate", StandardBasicTypes.DATE).addScalar("totTyres", StandardBasicTypes.INTEGER).addScalar("totTubes", StandardBasicTypes.INTEGER)
            .addScalar("totFlaps", StandardBasicTypes.INTEGER).addScalar("totValve", StandardBasicTypes.INTEGER).addScalar("totPctr", StandardBasicTypes.INTEGER)
            .addScalar("totQty", StandardBasicTypes.INTEGER).addScalar("grn", StandardBasicTypes.STRING).addScalar("status", StandardBasicTypes.STRING)
            .addScalar("tteUtil", StandardBasicTypes.DOUBLE).addScalar("weightUtil", StandardBasicTypes.DOUBLE).addScalar("loadslipcomments", StandardBasicTypes.STRING)
            .addScalar("freightAvailability", StandardBasicTypes.STRING).addScalar("transhipment", StandardBasicTypes.STRING).addScalar("updateUser", StandardBasicTypes.STRING)
            .addScalar("insertUser", StandardBasicTypes.STRING).addScalar("eWayBillNo", StandardBasicTypes.STRING).addScalar("integrationStatus", StandardBasicTypes.STRING)
            .addScalar("integrationMsg", StandardBasicTypes.STRING).addScalar("itemCategory", StandardBasicTypes.STRING).addScalar("gatedOutDate", StandardBasicTypes.DATE)
            .addScalar("releaseDate", StandardBasicTypes.DATE).addScalar("type", StandardBasicTypes.STRING).addScalar("sapInvValue", StandardBasicTypes.INTEGER)
            .addScalar("sapInvWeight", StandardBasicTypes.INTEGER).addScalar("otherQty", StandardBasicTypes.STRING).addScalar("createdDate", StandardBasicTypes.DATE)

        .addScalar("grnDate", StandardBasicTypes.DATE).addScalar("lrDate", StandardBasicTypes.DATE)
        .addScalar("repGiHrs", StandardBasicTypes.DOUBLE).addScalar("repGoHrs",StandardBasicTypes.DOUBLE).addScalar("giGoHrs",StandardBasicTypes.DOUBLE)
        .addScalar("giRelHrs", StandardBasicTypes.DOUBLE).addScalar("loRelHrs",StandardBasicTypes.DOUBLE).addScalar("relGoHrs",StandardBasicTypes.DOUBLE)

        .addScalar("comments", StandardBasicTypes.STRING).addScalar("indentId",StandardBasicTypes.STRING).addScalar("transporterSapCode",StandardBasicTypes.STRING)
				.addScalar("stopType", StandardBasicTypes.STRING).addScalar("ditQty", StandardBasicTypes.INTEGER).addScalar("shortQty", StandardBasicTypes.INTEGER)
				.addScalar("tteQty", StandardBasicTypes.DOUBLE).addScalar("volumeUtil", StandardBasicTypes.DOUBLE).addScalar("dropSeq", StandardBasicTypes.INTEGER)
				.addScalar("bayStatus", StandardBasicTypes.STRING).addScalar("destCountry",StandardBasicTypes.STRING).addScalar("loadslipStatus",StandardBasicTypes.STRING)
				.addScalar("reportingLoc",StandardBasicTypes.STRING).addScalar("isFreightAvailable",StandardBasicTypes.STRING).addScalar("rejectionCode",StandardBasicTypes.STRING)
				.addScalar("rejectionStatus",StandardBasicTypes.STRING).addScalar("actualTruckType",StandardBasicTypes.STRING).addScalar("marketSegment", StandardBasicTypes.STRING)
        .addScalar("isPuc",StandardBasicTypes.STRING).addScalar("isInsurance",StandardBasicTypes.STRING).addScalar("isSeatBelt",StandardBasicTypes.STRING).addScalar("isFirstAid",StandardBasicTypes.STRING)
        .addScalar("isFireExtenguisher",StandardBasicTypes.BOOLEAN).addScalar("isEmergencyCard",StandardBasicTypes.STRING).addScalar("isSparKArrestor",StandardBasicTypes.STRING)
        .addScalar("isFitnessCert",StandardBasicTypes.STRING).addScalar("bsNorms", StandardBasicTypes.STRING).addScalar("fuelType", StandardBasicTypes.STRING).addScalar("IndentCreationDate",StandardBasicTypes.DATE).addScalar("reportingDateAtDest",StandardBasicTypes.DATE)
            .addScalar("truckCapacity", StandardBasicTypes.DOUBLE).addScalar("truckGrossVehicleWt",StandardBasicTypes.DOUBLE).addScalar("truckUnladenWt",StandardBasicTypes.DOUBLE).addScalar("unloadingDateAtDest",StandardBasicTypes.DATE).addScalar("stdTT", StandardBasicTypes.DOUBLE).addScalar("delayedDays", StandardBasicTypes.INTEGER)
        .addScalar("ftTripId", StandardBasicTypes.STRING).addScalar("customInvNumber", StandardBasicTypes.STRING).addScalar("indentCategory", StandardBasicTypes.STRING);

//    Replaced With Akshay's query on 11/10/2019 uncomment till 2413
//    StringBuilder countQuery = getCountQueryForTruckMovement(truckReportFilterDto, loggedInUser);
    StringBuilder countQuery = new StringBuilder("select count(*) from ("+sb+")");
    List<Object> objectList = entityManager.createNativeQuery(countQuery.toString()).getResultList();
    int totalElement = Integer.valueOf(objectList.get(0).toString());

//    PaginationViewBean pageViewBean = new PaginationViewBean();
//    int totalElement = q.getResultList().size();
    q.setFirstResult((truckReportFilterDto.getIndex() * truckReportFilterDto.getPageLength()));
    q.setMaxResults(truckReportFilterDto.getPageLength());
    List<TrucksMetaData> loadslipMetaData = ((NativeQuery) q)
        .setResultTransformer(new AliasToBeanResultTransformer(TrucksMetaData.class)).getResultList();

   /* TypedQuery<LoadslipMetaData> typedQuery = entityManager.createQuery(criteriaQuery);
    int count = typedQuery.getResultList().size();
    typedQuery.setFirstResult(loadslipFilterDto.getIndex() * loadslipFilterDto.getPageLength());
    typedQuery.setMaxResults(loadslipFilterDto.getPageLength());*/
    return new PageImpl<>(loadslipMetaData, PageRequest.of(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength()), totalElement);
  }

  private StringBuilder getCountQueryForTruckMovement(TruckReportFilterDto truckReportFilterDto, ApplicationUser loggedInUser) {
    StringBuilder countQurey = new StringBuilder("select count(*) from truck_reporting tr \n" +
        "LEFT JOIN shipment shipment on shipment.shipment_id = tr.shipment_id \n" +
        "LEFT JOIN loadslip lp on lp.shipment_id = tr.shipment_id AND (lp.status != 'CANCELLED' OR lp.status IS NULL ) AND ((lp.source_loc = tr.reporting_location OR lp.source_loc IS NULL)OR lp.dest_loc = tr.reporting_location)\n" +
        "LEFT JOIN indent_summary ins on ins.indent_id = tr.indent_id ");

    /*Right join with loadslip_details is only applied when filter with itemId is performed*/
    if (!StringUtils.isEmpty(truckReportFilterDto.getItemId())){
      countQurey.append(" RIGHT JOIN (select distinct loadslip.shipment_id as shipmentId from loadslip " +
          "                   RIGHT JOIN (select distinct loadslip_id as loadslipId from loadslip_detail where item_id = '"+truckReportFilterDto.getItemId()+"') ld on ld.loadslipId = loadslip.loadslip_id) lds " +
          "     on lds.shipmentId = lp.shipment_id ");
    }

    countQurey.append(" WHERE tr.gate_control_code IS NOT NULL ");

    if (!UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())){
//      NOT DPREP user
      countQurey.append(" AND tr.reporting_location = '"+loggedInUser.getPlantCode()+"' ");
    }

    if (!StringUtils.isEmpty(truckReportFilterDto.getLoadslipId())) {
      countQurey.append("and lp.loadslip_id = '" + truckReportFilterDto.getLoadslipId() + "'");
    }

    if (!StringUtils.isEmpty(truckReportFilterDto.getDestination())) {
      countQurey.append("and   lp.dest_Loc = '" + truckReportFilterDto.getDestination() + "'");
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getShipmentID())) {
      countQurey.append("and   lp.shipment_id = '" + truckReportFilterDto.getShipmentID() + "'");
    }
    if (truckReportFilterDto.getTruckType() !=null && truckReportFilterDto.getTruckType().size()>0) {
      countQurey.append("and   tr.truck_type in (" + Utility.join(truckReportFilterDto.getTruckType()) + ") ");
    }

    if (!StringUtils.isEmpty(truckReportFilterDto.getFromReportDate()) && !StringUtils.isEmpty(truckReportFilterDto.getToReportDate())) {
/*
      sb.append("  ls.createdDate   >='" +DateUtils.formatDate(DateUtils.setTimeToMidnight(DateUtils.formatDate(loadslipFilterDto.getFromCreatedDate(),PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) +"' and ls.createdDate <= '"+DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(loadslipFilterDto.getToCreatedDate(),PLAN_RECORD_DATE_FORMAT)),Constants.DATE_FORMATE_WITH_HYPHEN)+"'");
*/
      countQurey.append("and  tr.reporting_date   >=TO_DATE('" + truckReportFilterDto.getFromReportDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      countQurey.append(" and tr.reporting_date <= TO_DATE('" + DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(truckReportFilterDto.getToReportDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
    }

    if (!StringUtils.isEmpty(truckReportFilterDto.getFromGateInDate()) && !StringUtils.isEmpty(truckReportFilterDto.getToGateInDate())) {
/*
      sb.append("  ls.createdDate   >='" +DateUtils.formatDate(DateUtils.setTimeToMidnight(DateUtils.formatDate(loadslipFilterDto.getFromCreatedDate(),PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) +"' and ls.createdDate <= '"+DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(loadslipFilterDto.getToCreatedDate(),PLAN_RECORD_DATE_FORMAT)),Constants.DATE_FORMATE_WITH_HYPHEN)+"'");
*/
      countQurey.append("and  tr.gatein_date   >=TO_DATE('" + truckReportFilterDto.getFromGateInDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      countQurey.append(" and tr.gatein_date <= TO_DATE('" + DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(truckReportFilterDto.getToGateInDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
    }

    if (!StringUtils.isEmpty(truckReportFilterDto.getFromGateOutDate()) && !StringUtils.isEmpty(truckReportFilterDto.getToGateOutDate())) {
/*
      sb.append("  ls.createdDate   >='" +DateUtils.formatDate(DateUtils.setTimeToMidnight(DateUtils.formatDate(loadslipFilterDto.getFromCreatedDate(),PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) +"' and ls.createdDate <= '"+DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(loadslipFilterDto.getToCreatedDate(),PLAN_RECORD_DATE_FORMAT)),Constants.DATE_FORMATE_WITH_HYPHEN)+"'");
*/
      countQurey.append("and  tr.gateout_date   >=TO_DATE('" + truckReportFilterDto.getFromGateOutDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      countQurey.append(" and tr.gateout_date <= TO_DATE('" + DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(truckReportFilterDto.getToGateOutDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
    }

//			if (!StringUtils.isEmpty(truckReportFilterDto.getMarketSegment())) {
//				Map<String, String> marketSegMap = Utility.deriveTubeAndFlapBatchCodes();
//				//Here tube and flap batch codes are same based on MarketSegment
//				String tubeOrFlapBatchCode = marketSegMap.get(truckReportFilterDto.getMarketSegment());
//				sb.append("and   ldb.TUBE_BATCH = '" + tubeOrFlapBatchCode + "'");
//			}

    if (truckReportFilterDto.getMarketSegment() != null && truckReportFilterDto.getMarketSegment().size()>0){
      countQurey.append(" and lp.mkt_seg in (" + Utility.join(truckReportFilterDto.getMarketSegment()) + ") ");
    }

    if (!StringUtils.isEmpty(truckReportFilterDto.getTruckNumber())) {
      countQurey.append("and   tr.truck_Number = '" + truckReportFilterDto.getTruckNumber() + "'");
    }

    if (!StringUtils.isEmpty(truckReportFilterDto.getContainerNum())) {
      countQurey.append("and   tr.container_num = '" + truckReportFilterDto.getContainerNum() + "'");
    }

    if (!StringUtils.isEmpty(truckReportFilterDto.getSource())) {
      countQurey.append("and   tr.source_loc = '" + truckReportFilterDto.getSource() + "'");
    }

    if (truckReportFilterDto.getStatuses()!=null && truckReportFilterDto.getStatuses().size()>0) {
      // sb.append("and   ts.status IN ('" + truckReportFilterDto.getStatus() + "'");
      countQurey.append(("and tr.status  in (" + Utility.join(truckReportFilterDto.getStatuses()) + ") "));
    }

    if (truckReportFilterDto.getBayStatus() != null && truckReportFilterDto.getBayStatus().size()>0) {
      countQurey.append("and   tr.bay_status  in (" + Utility.join(truckReportFilterDto.getBayStatus()) + ") ");
    }

    if (!StringUtils.isEmpty(truckReportFilterDto.getInvoice())) {
      countQurey.append("and   lp.sap_invoice = '" + truckReportFilterDto.getInvoice() + "'");
    }

    if (!StringUtils.isEmpty(truckReportFilterDto.getStopType())){
      countQurey.append("and   shipment.stop_type = '" + truckReportFilterDto.getStopType() + "'");
    }

    if (!StringUtils.isEmpty(truckReportFilterDto.getTransporter())){
      countQurey.append("and   tr.servprov = '" + truckReportFilterDto.getTransporter() + "'");
    }

    if (!StringUtils.isEmpty(truckReportFilterDto.getDestCountry())){
      countQurey.append("and   tr.dest_country = '" + truckReportFilterDto.getDestCountry() + "'");
    }

    if (!StringUtils.isEmpty(truckReportFilterDto.getIndentID())){
      countQurey.append("and   tr.indent_id = '" + truckReportFilterDto.getIndentID() + "'");
    }

    if (truckReportFilterDto.getRejection() != null && truckReportFilterDto.getRejection().size()>0){
      countQurey.append("and   tr.rej_status in (" + Utility.join(truckReportFilterDto.getRejection()) + ") ");
    }

    return countQurey;
  }


  public StringBuilder setQueryForFilterTrucksMovement(ApplicationUser loggedInUser,TruckReportFilterDto truckReportFilterDto) {

    StringBuilder status = new StringBuilder();
    int count = 1;
    for (String status1 : truckReportFilterDto.getStatusList()) {
      status.append("'");
      status.append(status1);
      status.append("'");
      if (count != truckReportFilterDto.getStatusList().size()) {
        status.append(",");
      }
      count++;
    }

    /*DP_REP, L1_MGR, L2_MGR*/
    if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())){
      /*Delayed days Calculation
       * 1. When report-date-Dest is available then (report-date-Dest) - (gate-out-at-source + tt_days)
       * 2. ELSE if loadslip-id is not null and truck is not REJECTED and (current-date) - (gate-out-at-source + tt_days) >0 (i.e., current date should be greater than (gate-out-at-source + tt-days))
       * NOTE: when loadslip is not assaigned to the truck OR truck is REJECTED then delayed days is shown as null
       * 3. Other wise show null*/

      /*Replaced with Akshay's Query on 11/10/2019 uncomment till 2572-2594*/

      /*   StringBuilder sb = new StringBuilder("select DISTINCT ts.* from (select tr.TRUCK_NUMBER as truckNumber,tr.driver_name as driverName,tr.container_num as containerNum,tr.indent_id AS indentId,tr.TRANSPORTER_SAP_CODE AS transporterSapCode,lp.status AS loadslipStatus,tr.REJECTION_CODE AS rejectionCode,REJ_STATUS AS rejectionStatus, " +
          "    tr.driver_mobile as driverMobile,tr.driver_license as driverLicense,isu.FRT_AVAIL_FLAG AS isFreightAvailable,REP_GI_HRS AS repGiHrs, REP_GO_HRS AS repGoHrs,GI_GO_HRS AS giGoHrs,GI_REL_HRS AS giRelHrs,LO_REL_HRS AS loRelHrs,REL_GO_HRS AS relGoHrs ,lp.LOADSLIP_ID as loadslipId,lp.SHIPMENT_ID as shipmentId,tr.gatein_date as gateInDate, tr.gateout_date as gateOutDate,tr.reporting_date as reportDate,tr.reporting_location AS reportingLoc, " +
          "tr.is_puc AS ispuc,tr.is_insurance AS isinsurance,tr.is_seatbelt AS isseatbelt ,tr.is_first_aid AS isfirstaid,tr.is_fire_extenguisher AS isfireextenguisher,tr.is_emergency_card AS isemergencycard,tr.is_spark_arrestor AS issparkarrestor, tr.is_fitness_cert AS isfitnesscert,"+
          "    tr.in_weight as inWeight,tr.out_weight as outWeight,tr.COMMENTS as comments,tr.actual_truck_type AS actualTruckType,tr.truck_type AS truckType,tr.SERVPROV as servprov, " +
          "    lp.qty as qty,tr.SOURCE_LOC as sourceLoc,(CASE WHEN lp.loadslip_id IS NULL THEN isu.dest_loc ELSE lp.dest_loc END) as destLoc, lp.STO_SO_NUM as stoSoNum,lp.DELIVERY as delivery, lp.SAP_INVOICE as sapInvoice,    lp.SAP_INVOICE_DATE as sapInvoiceDate,lp.LR_NUM as lrNum, lp.lsprint_date as lsPrintDate, " +
          "    lp.arrived_date as bayArrivedDate,lp.LS_DATE as loadingStartDate ,lp.LE_DATE as loadingEndDate, lp.CONFIRM_DATE as confirmedDate,lp.send_for_barcode_date  as sendForBarcodeDate,lp.TOT_TYRES as totTyres, " +
          "    lp.TOT_TUBES as totTubes, lp.TOT_FLAPS as totFlaps,lp.TOT_VALVE as totValve,lp.tot_pctr as totPctr,lp.tot_qty as totQty,lp.GRN as grn,tr.STATUS as status,lp.TTE_UTIL as tteUtil,lp.WEIGHT_UTIL as weightUtil,lp.COMMENTS as loadslipcomments, " +
          "    shipment.FRT_AVAIL_FLAG as freightAvailability,shipment.TRANSHIPMENT as transhipment,tr.UPDATE_USER as updateUser, tr.INSERT_USER as insertUser,lp.E_WAY_BILL_NO as eWayBillNo ,lp.INT_STATUS as integrationStatus,lp.INT_MESSAGE as integrationMsg, " +
          "    lp.ITEM_CATEGORY as itemCategory,shipment.START_TIME as gatedOutDate,lp.RELEASE_DATE as releaseDate,lp.LOADSLIP_TYPE as type, " +
          "    lp.SAP_INV_VALUE as sapInvValue ,    lp.SAP_INV_WEIGHT as sapInvWeight ,lp.other as otherQty,lp.INSERT_DATE as createdDate,lp.grn_date as grnDate,lp.lr_date as lrDate , " +
					"    shipment.stop_type  AS stopType, lp.dit_qty  AS ditQty,  lp.short_qty AS shortQty,  lp.tte_qty  AS tteQty, lp.volume_util AS volumeUtil, lp.drop_seq  AS dropSeq ,tr.bay_status AS bayStatus,tr.DEST_COUNTRY as destCountry, lp.mkt_seg as marketSegment,isu.insert_date AS indentCreationDate," +
          "    lp.grn_reporting_date  AS reportingDateAtDest, lp.grn_unloading_date  AS unloadingDateAtDest, tr.tt_days  AS stdTT, shipment.ft_trip_id as ftTripId, " +
          "    (CASE WHEN lp.grn_reporting_date IS NOT NULL THEN " +
          "          Abs(( ( To_date(lp.grn_reporting_date, 'DD-MM-YYYY') - To_date(tr.gateout_date, 'DD-MM-YYYY') ) - ( CASE WHEN tr.tt_days IS NULL THEN 0 ELSE tr.tt_days END) )) " +
          "     ELSE " +
          "          (CASE WHEN (lp.loadslip_id IS NOT NULL AND tr.rej_status != 'REJECTED') AND " +
          "              (((TO_DATE((SELECT SYSDATE FROM DUAL),'DD-MM-YYYY')) -(TO_DATE(tr.gateout_date,'DD-MM-YYYY')) -( CASE WHEN tr.tt_days IS NULL THEN 0 ELSE tr.tt_days END)) > 0) " +
          "                THEN Abs(((TO_DATE((SELECT SYSDATE FROM DUAL),'DD-MM-YYYY')) -(TO_DATE(tr.gateout_date,'DD-MM-YYYY')) -( CASE WHEN tr.tt_days IS NULL THEN 0 ELSE tr.tt_days END))) " +
          "           ELSE NULL END) " +
          "     END) AS delayedDays  " +
          " from truck_reporting tr " +
          "left join loadslip lp on ( lp.shipment_id = tr.shipment_id OR lp.shipment_id IS NULL ) and ( (lp.source_loc = tr.reporting_location  OR lp.source_loc IS NULL) or (tr.reporting_location=lp.dest_loc) )  AND ( lp.status NOT IN( 'CANCELLED' ) OR lp.status IS NULL ) " +
          "left join Shipment shipment on tr.shipment_id = shipment.shipment_id LEFT JOIN indent_summary isu ON isu.indent_id = tr.indent_id  ) ts " );*/

			/*Removed this two because loadslip_detail_bom ldb is never used in query and loadslip_details ld is replaced with RIGHT JOIN as it is not working in COUNT query*/
      /*" LEFT JOIN loadslip_detail_bom ldb  ON ldb.loadslip_id = ts.loadslipid " +
					"         LEFT JOIN loadslip_detail ld  ON ld.loadslip_id = ts.loadslipid "*/

      /*Right join with loadslip_details is only applied when filter with itemId is performed*/

      /*Replaced With Akshay's query on 11/10/2019 uncomment till 2603-2609*/
      /*if (!StringUtils.isEmpty(truckReportFilterDto.getItemId())){
        sb.append(" RIGHT JOIN (select distinct loadslip.shipment_id as shipmentId from loadslip " +
            "                   RIGHT JOIN (select distinct loadslip_id as loadslipId from loadslip_detail where item_id = '"+truckReportFilterDto.getItemId()+"') ld on ld.loadslipId = loadslip.loadslip_id) lds " +
            "     on lds.shipmentId = ts.shipmentid ");
      }

      sb.append("   where ts.truckNumber is not null and ts.status in("+status+")  ");*/

      StringBuilder sb = new StringBuilder("SELECT DISTINCT ts.*\n" +
          "FROM   (SELECT tr.truck_number          AS truckNumber,\n" +
          "               tr.driver_name           AS driverName,\n" +
          "               tr.container_num         AS containerNum,\n" +
          "               tr.indent_id             AS indentId,\n" +
          "               tr.transporter_sap_code  AS transporterSapCode,\n" +
          "               lp.status                AS loadslipStatus,\n" +
          "               tr.rejection_code        AS rejectionCode,\n" +
          "               rej_status               AS rejectionStatus,\n" +
          "               tr.driver_mobile         AS driverMobile,\n" +
          "               tr.driver_license        AS driverLicense,\n" +
          "               isu.frt_avail_flag       AS isFreightAvailable,\n" +
          "               rep_gi_hrs               AS repGiHrs,\n" +
          "               rep_go_hrs               AS repGoHrs,\n" +
          "               gi_go_hrs                AS giGoHrs,\n" +
          "               gi_rel_hrs               AS giRelHrs,\n" +
          "               lo_rel_hrs               AS loRelHrs,\n" +
          "               rel_go_hrs               AS relGoHrs,\n" +
          "               lp.loadslip_id           AS loadslipId,\n" +
          "               lp.shipment_id           AS shipmentId,\n" +
          "               tr.gatein_date           AS gateInDate,\n" +
          "               tr.gateout_date          AS gateOutDate,\n" +
          "               tr.reporting_date        AS reportDate,\n" +
          "               tr.reporting_location    AS reportingLoc,\n" +
          "               tr.is_puc                AS ispuc,\n" +
          "               tr.is_insurance          AS isinsurance,\n" +
          "               tr.is_seatbelt           AS isseatbelt,\n" +
          "               tr.is_first_aid          AS isfirstaid,\n" +
          "               tr.is_fire_extenguisher  AS isfireextenguisher,\n" +
          "               tr.is_emergency_card     AS isemergencycard,\n" +
          "               tr.is_spark_arrestor     AS issparkarrestor,\n" +
          "               tr.is_fitness_cert       AS isfitnesscert,\n" +
          "               tr.truck_capacity         AS truckCapacity, \n"  +
              "  tr.gross_vehicle_wt as truckgrossVehicleWt, \n" +
              "  tr.unladen_wt as truckunladenWt, \n" +
              "  tr.bs_norms as bsNorms, \n" +
              "  tr.fuel_type as fuelType, \n" +
              "               tr.in_weight             AS inWeight,\n" +
          "               tr.out_weight            AS outWeight,\n" +
          "               tr.comments              AS comments,\n" +
          "               tr.actual_truck_type     AS actualTruckType,\n" +
          "               tr.truck_type            AS truckType,\n" +
          "               tr.servprov              AS servprov,\n" +
          "               lp.qty                   AS qty,\n" +
          "               tr.source_loc            AS sourceLoc,\n" +
          "               ( CASE\n" +
          "                   WHEN lp.loadslip_id IS NULL THEN isu.dest_loc\n" +
          "                   ELSE lp.dest_loc\n" +
          "                 END )                  AS destLoc,\n" +
          "               lp.sto_so_num            AS stoSoNum,\n" +
          "               lp.delivery              AS delivery,\n" +
          "               lp.sap_invoice           AS sapInvoice,\n" +
          "               lp.sap_invoice_date      AS sapInvoiceDate,\n" +
          "               lp.lr_num                AS lrNum,\n" +
          "               lp.lsprint_date          AS lsPrintDate,\n" +
          "               lp.arrived_date          AS bayArrivedDate,\n" +
          "               lp.ls_date               AS loadingStartDate,\n" +
          "               lp.le_date               AS loadingEndDate,\n" +
          "               lp.confirm_date          AS confirmedDate,\n" +
          "               lp.send_for_barcode_date AS sendForBarcodeDate,\n" +
          "               lp.tot_tyres             AS totTyres,\n" +
          "               lp.tot_tubes             AS totTubes,\n" +
          "               lp.tot_flaps             AS totFlaps,\n" +
          "               lp.tot_valve             AS totValve,\n" +
          "               lp.tot_pctr              AS totPctr,\n" +
          "               lp.tot_qty               AS totQty,\n" +
          "               lp.grn                   AS grn,\n" +
          "               tr.status                AS status,\n" +
          "               lp.tte_util              AS tteUtil,\n" +
          "               lp.weight_util           AS weightUtil,\n" +
          "               lp.comments              AS loadslipcomments,\n" +
          "               shipment.frt_avail_flag  AS freightAvailability,\n" +
          "               shipment.transhipment    AS transhipment,\n" +
          "               tr.update_user           AS updateUser,\n" +
          "               tr.insert_user           AS insertUser,\n" +
          "               lp.e_way_bill_no         AS eWayBillNo,\n" +
          "               lp.int_status            AS integrationStatus,\n" +
          "               lp.int_message           AS integrationMsg,\n" +
          "               lp.item_category         AS itemCategory,\n" +
          "             shipment.start_time      AS gatedOutDate,\n" +
          "               lp.release_date          AS releaseDate,\n" +
          "               lp.loadslip_type         AS type,\n" +
          "               lp.sap_inv_value         AS sapInvValue,\n" +
          "               lp.sap_inv_weight        AS sapInvWeight,\n" +
          "               lp.other                 AS otherQty,\n" +
          "               lp.insert_date           AS createdDate,\n" +
          "               lp.grn_date              AS grnDate,\n" +
          "               lp.lr_date               AS lrDate,\n" +
          "               shipment.stop_type       AS stopType,\n" +
          "               lp.dit_qty               AS ditQty,\n" +
          "               lp.short_qty             AS shortQty,\n" +
          "               lp.tte_qty               AS tteQty,\n" +
          "               lp.volume_util           AS volumeUtil,\n" +
          "               lp.drop_seq              AS dropSeq,\n" +
          "               tr.bay_status            AS bayStatus,\n" +
          "               tr.dest_country          AS destCountry,\n" +
          "               lp.mkt_seg               AS marketSegment,\n" +
          "               isu.insert_date          AS indentCreationDate,\n" +
          "               lp.grn_reporting_date    AS reportingDateAtDest,\n" +
          "               lp.grn_unloading_date    AS unloadingDateAtDest,\n" +
          "               tr.tt_days               AS stdTT,\n" +
          "               shipment.ft_trip_id      AS ftTripId,\n" +
          "               lp.custom_inv_number   AS customInvNumber,\n" +
          "    tr.TRACKING_CONSENT_STATUS as trackingConsentStatus,\n"+
              "  tr.CONSENT_PHONE_TELECOM as consentPhoneTelecom,\n" +
          "                ( CASE " +
          "                      WHEN lp.grn_reporting_date IS NOT NULL THEN Abs(( (" +
          "                      To_date(to_char(lp.grn_reporting_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') - " +
          "                      To_date(to_char(tr.gateout_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') ) - ( CASE " +
          "                      WHEN tr.tt_days IS NULL THEN 0 " +
          "                      ELSE tr.tt_days END ) )) " +
          "                 ELSE ( CASE WHEN ( lp.loadslip_id IS NOT NULL " +
          "                                   AND tr.rej_status != 'REJECTED' ) " +
          "                                   AND ( ( ( To_date((SELECT to_char(sysdate, 'DD-MM-YYYY') FROM   dual), 'DD-MM-YYYY') ) " +
          "       - ( " +
          "               To_date(to_char(tr.gateout_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') ) - ( " +
          "               CASE " +
          "                 WHEN tr.tt_days IS NULL THEN 0 " +
          "                 ELSE tr.tt_days " +
          "               END ) ) > 0 ) THEN Abs(( ( " +

          "       To_date((SELECT to_char(sysdate, 'DD-MM-YYYY')  " +
          "       FROM   dual), 'DD-MM-YYYY') ) - ( " +
          "       To_date(to_char(tr.gateout_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') ) - ( " +
          "       CASE " +
          "       WHEN tr.tt_days IS NULL THEN 0 " +
          "       ELSE tr.tt_days " +
          "       END ) )) " +
          "       ELSE NULL " +
          "       END ) " +
          "       END )                  AS delayedDays," +
    /*      "               ( CASE\n" +
          "                      WHEN lp.grn_reporting_date IS NOT NULL THEN Abs(( (\n" +
          "                      To_date(lp.grn_reporting_date, 'DD-MM-YYYY') -\n" +
          "                      To_date(tr.gateout_date, 'DD-MM-YYYY') ) - ( CASE\n" +
          "                      WHEN tr.tt_days IS NULL THEN 0\n" +
          "                      ELSE tr.tt_days END ) ))\n" +
          "                 ELSE ( CASE WHEN ( lp.loadslip_id IS NOT NULL\n" +
          "                                   AND tr.rej_status != 'REJECTED' )\n" +
          "                                   AND ( ( ( To_date((SELECT sysdate  FROM   dual), 'DD-MM-YYYY') )\n" +
          "       - (\n" +
          "               To_date(tr.gateout_date, 'DD-MM-YYYY') ) - (\n" +
          "               CASE\n" +
          "                 WHEN tr.tt_days IS NULL THEN 0\n" +
          "                 ELSE tr.tt_days\n" +
          "               END ) ) > 0 ) THEN Abs(( (\n" +
          "       To_date((SELECT sysdate \n" +
          "       FROM   dual), 'DD-MM-YYYY') ) - (\n" +
          "       To_date(tr.gateout_date, 'DD-MM-YYYY') ) - (\n" +
          "       CASE\n" +
          "       WHEN tr.tt_days IS NULL THEN 0\n" +
          "       ELSE tr.tt_days\n" +
          "       END ) ))\n" +
          "       ELSE NULL\n" +
          "       END )\n" +
          "       END )                  AS delayedDays ," +*/

          "       isu.item_category      AS indentCategory " +
          "       FROM   truck_reporting tr,loadslip lp,shipment shipment,\n" +
          "       indent_summary isu      \n" +
          "       where tr.shipment_id=lp.shipment_id(+)\n" +
          "       and tr.shipment_id = shipment.shipment_id(+)\n" +
          "       and tr.indent_id= isu.indent_id(+) ) ts, " +
          "       loadslip_detail ld " +
          "       WHERE  ts.trucknumber IS NOT NULL " +
          "      AND ((ts.rejectionStatus = 'REJECTED' OR ts.status = 'GATED_IN') OR (ts.rejectionStatus != 'REJECTED' AND Nvl(ts.loadslipstatus, 'X') <> 'CANCELLED')) " +
//          "       and nvl(ts.loadslipStatus,'X') <> 'CANCELLED' " +
          "       AND ts.loadslipid = ld.loadslip_id(+) and ts.status in("+status+")");

      if (!StringUtils.isEmpty(truckReportFilterDto.getLoadslipId())) {
        sb.append("and ts.loadslipId = '" + truckReportFilterDto.getLoadslipId() + "'");
      }

     /* if (!StringUtils.isEmpty(truckReportFilterDto.getItemId())) {
        sb.append("and   ld.ITEM_ID = '" + truckReportFilterDto.getItemId() + "'");
      }*/
      if (!StringUtils.isEmpty(truckReportFilterDto.getDestination())) {
        sb.append("and   ts.destLoc = '" + truckReportFilterDto.getDestination() + "'");
      }
      if (!StringUtils.isEmpty(truckReportFilterDto.getShipmentID())) {
        sb.append("and   ts.shipmentId = '" + truckReportFilterDto.getShipmentID() + "'");
      }
      if (truckReportFilterDto.getTruckType() !=null && truckReportFilterDto.getTruckType().size()>0) {
        sb.append("and   ts.truckType in (" + Utility.join(truckReportFilterDto.getTruckType()) + ") ");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getFromReportDate()) && !StringUtils.isEmpty(truckReportFilterDto.getToReportDate())) {
/*
      sb.append("  ls.createdDate   >='" +DateUtils.formatDate(DateUtils.setTimeToMidnight(DateUtils.formatDate(loadslipFilterDto.getFromCreatedDate(),PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) +"' and ls.createdDate <= '"+DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(loadslipFilterDto.getToCreatedDate(),PLAN_RECORD_DATE_FORMAT)),Constants.DATE_FORMATE_WITH_HYPHEN)+"'");
*/
        sb.append("and  ts.reportDate   >=TO_DATE('" + truckReportFilterDto.getFromReportDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
        sb.append(" and ts.reportDate <= TO_DATE('" + DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(truckReportFilterDto.getToReportDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getFromGateInDate()) && !StringUtils.isEmpty(truckReportFilterDto.getToGateInDate())) {
/*
      sb.append("  ls.createdDate   >='" +DateUtils.formatDate(DateUtils.setTimeToMidnight(DateUtils.formatDate(loadslipFilterDto.getFromCreatedDate(),PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) +"' and ls.createdDate <= '"+DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(loadslipFilterDto.getToCreatedDate(),PLAN_RECORD_DATE_FORMAT)),Constants.DATE_FORMATE_WITH_HYPHEN)+"'");
*/
        sb.append("and  ts.gateInDate   >=TO_DATE('" + truckReportFilterDto.getFromGateInDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
        sb.append(" and ts.gateInDate <= TO_DATE('" + DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(truckReportFilterDto.getToGateInDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getFromGateOutDate()) && !StringUtils.isEmpty(truckReportFilterDto.getToGateOutDate())) {
/*
      sb.append("  ls.createdDate   >='" +DateUtils.formatDate(DateUtils.setTimeToMidnight(DateUtils.formatDate(loadslipFilterDto.getFromCreatedDate(),PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) +"' and ls.createdDate <= '"+DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(loadslipFilterDto.getToCreatedDate(),PLAN_RECORD_DATE_FORMAT)),Constants.DATE_FORMATE_WITH_HYPHEN)+"'");
*/
        sb.append("and  ts.gateOutDate   >=TO_DATE('" + truckReportFilterDto.getFromGateOutDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
        sb.append(" and ts.gateOutDate <= TO_DATE('" + DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(truckReportFilterDto.getToGateOutDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      }

//			if (!StringUtils.isEmpty(truckReportFilterDto.getMarketSegment())) {
//				Map<String, String> marketSegMap = Utility.deriveTubeAndFlapBatchCodes();
//				//Here tube and flap batch codes are same based on MarketSegment
//				String tubeOrFlapBatchCode = marketSegMap.get(truckReportFilterDto.getMarketSegment());
//				sb.append("and   ldb.TUBE_BATCH = '" + tubeOrFlapBatchCode + "'");
//			}

      if (truckReportFilterDto.getMarketSegment() != null && truckReportFilterDto.getMarketSegment().size()>0){
        sb.append(" and ts.marketSegment in (" + Utility.join(truckReportFilterDto.getMarketSegment()) + ") ");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getTruckNumber())) {
//        sb.append("and   ts.truckNumber = '" + truckReportFilterDto.getTruckNumber() + "'");
        sb.append("and   LOWER(ts.truckNumber) LIKE '" + "%"+truckReportFilterDto.getTruckNumber().toLowerCase()+ "%" + "'");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getContainerNum())) {
        sb.append("and   ts.containerNum = '" + truckReportFilterDto.getContainerNum() + "'");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getInsertUser())) {
        sb.append("and   ts.insertUser = '" + truckReportFilterDto.getInsertUser() + "'");
      }


      if (!StringUtils.isEmpty(truckReportFilterDto.getSource())) {
        sb.append("and   ts.sourceLoc = '" + truckReportFilterDto.getSource() + "'");
      }

      if (truckReportFilterDto.getStatuses()!=null && truckReportFilterDto.getStatuses().size()>0) {
       // sb.append("and   ts.status IN ('" + truckReportFilterDto.getStatus() + "'");
        sb.append(("and ts.status  in (" + Utility.join(truckReportFilterDto.getStatuses()) + ") "));
      }

      if (truckReportFilterDto.getBayStatus() != null && truckReportFilterDto.getBayStatus().size()>0) {
        sb.append("and   ts.bayStatus  in (" + Utility.join(truckReportFilterDto.getBayStatus()) + ") ");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getInvoice())) {
        sb.append("and   ts.sapInvoice = '" + truckReportFilterDto.getInvoice() + "'");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getStopType())){
        sb.append("and   ts.stopType = '" + truckReportFilterDto.getStopType() + "'");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getTransporter())){
        sb.append("and   ts.servprov = '" + truckReportFilterDto.getTransporter() + "'");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getDestCountry())){
        sb.append("and   ts.destCountry = '" + truckReportFilterDto.getDestCountry() + "'");
      }

			if (!StringUtils.isEmpty(truckReportFilterDto.getIndentID())){
				sb.append("and   ts.indentId = '" + truckReportFilterDto.getIndentID() + "'");
			}

			if (truckReportFilterDto.getRejection() != null && truckReportFilterDto.getRejection().size()>0){
				sb.append("and   ts.rejectionstatus in (" + Utility.join(truckReportFilterDto.getRejection()) + ") ");
			}

			if (!truckReportFilterDto.getIndentCategoryList().isEmpty()){
			  sb.append(" AND ts.indentCategory IN ("+Utility.join(truckReportFilterDto.getIndentCategoryList())+") ");
      }

			sb.append("  order by ts.reportDate desc");

      return sb;
    } else{
      /*Delayed days Calculation
       * 1. When report-date-Dest is available then (report-date-Dest) - (gate-out-at-source + tt_days)
       * 2. ELSE if loadslip-id is not null and truck is not REJECTED and (current-date) - (gate-out-at-source + tt_days) >0 (i.e., current date should be greater than (gate-out-at-source + tt-days))
       * NOTE: when loadslip is not assaigned to the truck OR truck is REJECTED then delayed days is shown as null
       * 3. Other wise show null*/

      /*Replaced with Akshay's query on 11/10/2019 uncomment till 2713-2739*/
     /* StringBuilder sb = new StringBuilder("select DISTINCT ts.* from (select tr.TRUCK_NUMBER as truckNumber,tr.driver_name as driverName,tr.container_num as containerNum,tr.indent_id AS indentId,tr.TRANSPORTER_SAP_CODE AS transporterSapCode,lp.status AS loadslipStatus , tr.REJECTION_CODE AS rejectionCode,REJ_STATUS AS rejectionStatus," +
          "    tr.driver_mobile as driverMobile,tr.driver_license as driverLicense,isu.FRT_AVAIL_FLAG AS isFreightAvailable,REP_GI_HRS AS repGiHrs, REP_GO_HRS AS repGoHrs,GI_GO_HRS AS giGoHrs,GI_REL_HRS AS giRelHrs,LO_REL_HRS AS loRelHrs,REL_GO_HRS AS relGoHrs ,lp.LOADSLIP_ID as loadslipId,lp.SHIPMENT_ID as shipmentId,tr.gatein_date as gateInDate, tr.gateout_date as gateOutDate,tr.reporting_date as reportDate,tr.reporting_location AS reportingLoc, " +
          "tr.is_puc AS ispuc,tr.is_insurance AS isinsurance,tr.is_seatbelt AS isseatbelt ,tr.is_first_aid AS isfirstaid,tr.is_fire_extenguisher AS isfireextenguisher,tr.is_emergency_card AS isemergencycard,tr.is_spark_arrestor AS issparkarrestor, tr.is_fitness_cert AS isfitnesscert,"+
          "    tr.in_weight as inWeight,tr.out_weight as outWeight,tr.COMMENTS as comments,tr.actual_truck_type AS actualTruckType,tr.truck_type AS truckType,tr.SERVPROV as servprov, " +
          "    lp.qty as qty,tr.SOURCE_LOC as sourceLoc,(CASE WHEN lp.loadslip_id IS NULL THEN isu.dest_loc ELSE lp.dest_loc END) as destLoc, lp.STO_SO_NUM as stoSoNum,lp.DELIVERY as delivery, lp.SAP_INVOICE as sapInvoice,    lp.SAP_INVOICE_DATE as sapInvoiceDate,lp.LR_NUM as lrNum, lp.lsprint_date as lsPrintDate, " +
          "    lp.arrived_date as bayArrivedDate,lp.LS_DATE as loadingStartDate ,lp.LE_DATE as loadingEndDate, lp.CONFIRM_DATE as confirmedDate,lp.send_for_barcode_date  as sendForBarcodeDate,lp.TOT_TYRES as totTyres," +
          "    lp.TOT_TUBES as totTubes, lp.TOT_FLAPS as totFlaps,lp.TOT_VALVE as totValve,lp.tot_pctr as totPctr,lp.tot_qty as totQty,lp.GRN as grn,tr.STATUS as status,lp.TTE_UTIL as tteUtil,lp.WEIGHT_UTIL as weightUtil,lp.COMMENTS as loadslipcomments," +
          "    shipment.FRT_AVAIL_FLAG as freightAvailability,shipment.TRANSHIPMENT as transhipment,tr.UPDATE_USER as updateUser, tr.INSERT_USER as insertUser,lp.E_WAY_BILL_NO as eWayBillNo ,lp.INT_STATUS as integrationStatus,lp.INT_MESSAGE as integrationMsg, " +
          "    lp.ITEM_CATEGORY as itemCategory,shipment.START_TIME as gatedOutDate,lp.RELEASE_DATE as releaseDate,lp.LOADSLIP_TYPE as type, " +
          "    lp.SAP_INV_VALUE as sapInvValue ,    lp.SAP_INV_WEIGHT as sapInvWeight ,lp.other as otherQty,lp.INSERT_DATE as createdDate,lp.grn_date as grnDate,lp.lr_date as lrDate," +
					"    shipment.stop_type  AS stopType, lp.dit_qty  AS ditQty,  lp.short_qty AS shortQty,  lp.tte_qty  AS tteQty, lp.volume_util AS volumeUtil, lp.drop_seq  AS dropSeq ,tr.bay_status AS bayStatus,tr.DEST_COUNTRY as destCountry, lp.mkt_seg as marketSegment,isu.insert_date AS indentCreationDate ," +
          "    lp.grn_reporting_date  AS reportingDateAtDest, lp.grn_unloading_date  AS unloadingDateAtDest, tr.tt_days  AS stdTT,shipment.ft_trip_id as ftTripId, " +
          "    (CASE WHEN lp.grn_reporting_date IS NOT NULL THEN " +
          "          Abs(( ( To_date(lp.grn_reporting_date, 'DD-MM-YYYY') - To_date(tr.gateout_date, 'DD-MM-YYYY') ) - ( CASE WHEN tr.tt_days IS NULL THEN 0 ELSE tr.tt_days END) )) " +
          "     ELSE " +
          "          (CASE WHEN (lp.loadslip_id IS NOT NULL AND tr.rej_status != 'REJECTED') AND " +
          "              (((TO_DATE((SELECT SYSDATE FROM DUAL),'DD-MM-YYYY')) -(TO_DATE(tr.gateout_date,'DD-MM-YYYY')) -( CASE WHEN tr.tt_days IS NULL THEN 0 ELSE tr.tt_days END)) > 0) " +
          "                THEN Abs(((TO_DATE((SELECT SYSDATE FROM DUAL),'DD-MM-YYYY')) -(TO_DATE(tr.gateout_date,'DD-MM-YYYY')) -( CASE WHEN tr.tt_days IS NULL THEN 0 ELSE tr.tt_days END))) " +
          "           ELSE NULL END ) " +
          "     END) AS delayedDays " +
          " from truck_reporting tr " +
          "left join loadslip lp on ( lp.shipment_id = tr.shipment_id OR lp.shipment_id IS NULL ) and ( (lp.source_loc = tr.reporting_location  OR lp.source_loc IS NULL) or (tr.reporting_location=lp.dest_loc) )  AND ( lp.status NOT IN( 'CANCELLED' ) OR lp.status IS NULL ) LEFT JOIN indent_summary isu ON isu.indent_id = tr.indent_id " +
          "left join Shipment shipment on tr.shipment_id = shipment.shipment_id where tr.reporting_location='"+loggedInUser.getPlantCode()+ "') ts " );*/

      /*Removed these two because loadslip_detail_bom ldb is never used in query and loadslip_details ld is replaced with RIGHT JOIN as it is not working in COUNT query*/
      /*" LEFT JOIN loadslip_detail_bom ldb  ON ldb.loadslip_id = ts.loadslipid " +
					"         LEFT JOIN loadslip_detail ld  ON ld.loadslip_id = ts.loadslipid   "*/

      /*Right join with loadslip_details is only applied when filter with itemId is performed*/
      /*Replaced with Akshay's query on 11/10/2019 uncomment till 2744-2749*/
      /*if (!StringUtils.isEmpty(truckReportFilterDto.getItemId())){
        sb.append(" RIGHT JOIN (select distinct loadslip.shipment_id as shipmentId from loadslip " +
            "                   RIGHT JOIN (select distinct loadslip_id as loadslipId from loadslip_detail where item_id = '"+truckReportFilterDto.getItemId()+"') ld on ld.loadslipId = loadslip.loadslip_id) lds " +
            "     on lds.shipmentId = ts.shipmentid ");
      }
      sb.append(" where ts.truckNumber is not null ");*/

         /*if(!StringUtils.isEmpty(loadslipFilterDto.getSource())){
      sb.append(" ls.sourceLoc = '" +loggedInUser.getPlantCode()+"'" );
    }*/

      StringBuilder sb = new StringBuilder("SELECT DISTINCT ts.*\n" +
          "FROM   (SELECT tr.truck_number          AS truckNumber,\n" +
          "               tr.driver_name           AS driverName,\n" +
          "               tr.container_num         AS containerNum,\n" +
          "               tr.indent_id             AS indentId,\n" +
          "               tr.transporter_sap_code  AS transporterSapCode,\n" +
          "               lp.status                AS loadslipStatus,\n" +
          "               tr.rejection_code        AS rejectionCode,\n" +
          "               rej_status               AS rejectionStatus,\n" +
          "               tr.driver_mobile         AS driverMobile,\n" +
          "               tr.driver_license        AS driverLicense,\n" +
          "               isu.frt_avail_flag       AS isFreightAvailable,\n" +
          "               rep_gi_hrs               AS repGiHrs,\n" +
          "               rep_go_hrs               AS repGoHrs,\n" +
          "               gi_go_hrs                AS giGoHrs,\n" +
          "               gi_rel_hrs               AS giRelHrs,\n" +
          "               lo_rel_hrs               AS loRelHrs,\n" +
          "               rel_go_hrs               AS relGoHrs,\n" +
          "               lp.loadslip_id           AS loadslipId,\n" +
          "               lp.shipment_id           AS shipmentId,\n" +
          "               tr.gatein_date           AS gateInDate,\n" +
          "               tr.gateout_date          AS gateOutDate,\n" +
          "               tr.reporting_date        AS reportDate,\n" +
          "               tr.reporting_location    AS reportingLoc,\n" +
          "               tr.is_puc                AS ispuc,\n" +
          "               tr.is_insurance          AS isinsurance,\n" +
          "               tr.is_seatbelt           AS isseatbelt,\n" +
          "               tr.is_first_aid          AS isfirstaid,\n" +
          "               tr.is_fire_extenguisher  AS isfireextenguisher,\n" +
          "               tr.is_emergency_card     AS isemergencycard,\n" +
          "               tr.is_spark_arrestor     AS issparkarrestor,\n" +
          "               tr.is_fitness_cert       AS isfitnesscert,\n" +
              "  tr.truck_capacity as truckCapacity, \n"  +
              "  tr.gross_vehicle_wt as truckgrossVehicleWt, \n" +
              "  tr.unladen_wt as truckunladenWt, \n" +
              "  tr.bs_norms as bsNorms, \n" +
              "  tr.fuel_type as fuelType, \n" +
              "               tr.in_weight             AS inWeight,\n" +
          "               tr.out_weight            AS outWeight,\n" +
          "               tr.comments              AS comments,\n" +
          "               tr.actual_truck_type     AS actualTruckType,\n" +
          "               tr.truck_type            AS truckType,\n" +
          "               tr.servprov              AS servprov,\n" +
          "               lp.qty                   AS qty,\n" +
          "               tr.source_loc            AS sourceLoc,\n" +
          "               ( CASE\n" +
          "                   WHEN lp.loadslip_id IS NULL THEN isu.dest_loc\n" +
          "                   ELSE lp.dest_loc\n" +
          "                 END )                  AS destLoc,\n" +
          "               lp.sto_so_num            AS stoSoNum,\n" +
          "               lp.delivery              AS delivery,\n" +
          "               lp.sap_invoice           AS sapInvoice,\n" +
          "               lp.sap_invoice_date      AS sapInvoiceDate,\n" +
          "               lp.lr_num                AS lrNum,\n" +
          "               lp.lsprint_date          AS lsPrintDate,\n" +
          "               lp.arrived_date          AS bayArrivedDate,\n" +
          "               lp.ls_date               AS loadingStartDate,\n" +
          "               lp.le_date               AS loadingEndDate,\n" +
          "               lp.confirm_date          AS confirmedDate,\n" +
          "               lp.send_for_barcode_date AS sendForBarcodeDate,\n" +
          "               lp.tot_tyres             AS totTyres,\n" +
          "               lp.tot_tubes             AS totTubes,\n" +
          "               lp.tot_flaps             AS totFlaps,\n" +
          "               lp.tot_valve             AS totValve,\n" +
          "               lp.tot_pctr              AS totPctr,\n" +
          "               lp.tot_qty               AS totQty,\n" +
          "               lp.grn                   AS grn,\n" +
          "               tr.status                AS status,\n" +
          "               lp.tte_util              AS tteUtil,\n" +
          "               lp.weight_util           AS weightUtil,\n" +
          "               lp.comments              AS loadslipcomments,\n" +
          "               shipment.frt_avail_flag  AS freightAvailability,\n" +
          "               shipment.transhipment    AS transhipment,\n" +
          "               tr.update_user           AS updateUser,\n" +
          "               tr.insert_user           AS insertUser,\n" +
          "               lp.e_way_bill_no         AS eWayBillNo,\n" +
          "               lp.int_status            AS integrationStatus,\n" +
          "               lp.int_message           AS integrationMsg,\n" +
          "               lp.item_category         AS itemCategory,\n" +
          "             shipment.start_time      AS gatedOutDate,\n" +
          "               lp.release_date          AS releaseDate,\n" +
          "               lp.loadslip_type         AS type,\n" +
          "               lp.sap_inv_value         AS sapInvValue,\n" +
          "               lp.sap_inv_weight        AS sapInvWeight,\n" +
          "               lp.other                 AS otherQty,\n" +
          "               lp.insert_date           AS createdDate,\n" +
          "               lp.grn_date              AS grnDate,\n" +
          "               lp.lr_date               AS lrDate,\n" +
          "               shipment.stop_type       AS stopType,\n" +
          "               lp.dit_qty               AS ditQty,\n" +
          "               lp.short_qty             AS shortQty,\n" +
          "               lp.tte_qty               AS tteQty,\n" +
          "               lp.volume_util           AS volumeUtil,\n" +
          "               lp.drop_seq              AS dropSeq,\n" +
          "               tr.bay_status            AS bayStatus,\n" +
          "               tr.dest_country          AS destCountry,\n" +
          "               lp.mkt_seg               AS marketSegment,\n" +
          "               isu.insert_date          AS indentCreationDate,\n" +
          "               lp.grn_reporting_date    AS reportingDateAtDest,\n" +
          "               lp.grn_unloading_date    AS unloadingDateAtDest,\n" +
          "               tr.tt_days               AS stdTT,\n" +
          "               shipment.ft_trip_id      AS ftTripId,\n" +
          "               lp.custom_inv_number   AS customInvNumber,\n" +
          "               ( CASE\n" +
          "                      WHEN lp.grn_reporting_date IS NOT NULL THEN Abs(( (\n" +
          "                      To_date(lp.grn_reporting_date, 'DD-MM-YYYY') -\n" +
          "                      To_date(tr.gateout_date, 'DD-MM-YYYY') ) - ( CASE\n" +
          "                      WHEN tr.tt_days IS NULL THEN 0\n" +
          "                      ELSE tr.tt_days END ) ))\n" +
          "                 ELSE ( CASE WHEN ( lp.loadslip_id IS NOT NULL\n" +
          "                                   AND tr.rej_status != 'REJECTED' )\n" +
          "                                   AND ( ( ( To_date((SELECT sysdate  FROM   dual), 'DD-MM-YYYY') )\n" +
          "       - (\n" +
          "               To_date(tr.gateout_date, 'DD-MM-YYYY') ) - (\n" +
          "               CASE\n" +
          "                 WHEN tr.tt_days IS NULL THEN 0\n" +
          "                 ELSE tr.tt_days\n" +
          "               END ) ) > 0 ) THEN Abs(( (\n" +
          "       To_date((SELECT sysdate \n" +
          "       FROM   dual), 'DD-MM-YYYY') ) - (\n" +
          "       To_date(tr.gateout_date, 'DD-MM-YYYY') ) - (\n" +
          "       CASE\n" +
          "       WHEN tr.tt_days IS NULL THEN 0\n" +
          "       ELSE tr.tt_days\n" +
          "       END ) ))\n" +
          "       ELSE NULL\n" +
          "       END )\n" +
          "       END )                  AS delayedDays," +
          "       isu.item_category      AS indentCategory \n" +
          "       FROM   truck_reporting tr,loadslip lp,shipment shipment,\n" +
          "       indent_summary isu      \n" +
          "       where tr.shipment_id=lp.shipment_id(+)\n" +
          "       and tr.shipment_id = shipment.shipment_id(+)\n" +
          "       and tr.indent_id= isu.indent_id(+) AND tr.reporting_location = '"+loggedInUser.getPlantCode()+"') ts, " +
          "       loadslip_detail ld " +
          "       WHERE  ts.trucknumber IS NOT NULL " +
          "      AND ((ts.rejectionStatus = 'REJECTED' OR ts.status = 'GATED_IN') OR (ts.rejectionStatus != 'REJECTED' AND Nvl(ts.loadslipstatus, 'X') <> 'CANCELLED')) " +
//          "and nvl(ts.loadslipStatus,'X') <> 'CANCELLED' " +
          "        AND ts.loadslipid = ld.loadslip_id(+) ");


      if (!StringUtils.isEmpty(truckReportFilterDto.getLoadslipId())) {
        sb.append("and ts.loadslipId = '" + truckReportFilterDto.getLoadslipId() + "'");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getItemId())) {
        sb.append("and   ld.ITEM_ID = '" + truckReportFilterDto.getItemId() + "'");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getDestination())) {
        sb.append("and   ts.destLoc = '" + truckReportFilterDto.getDestination() + "'");
      }
      if (!StringUtils.isEmpty(truckReportFilterDto.getShipmentID())) {
        sb.append("and   ts.shipmentId = '" + truckReportFilterDto.getShipmentID() + "'");
      }
      if (truckReportFilterDto.getTruckType() !=null && truckReportFilterDto.getTruckType().size()>0) {
        sb.append("and   ts.truckType in (" + Utility.join(truckReportFilterDto.getTruckType()) + ") ");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getFromReportDate()) && !StringUtils.isEmpty(truckReportFilterDto.getToReportDate())) {
/*
      sb.append("  ls.createdDate   >='" +DateUtils.formatDate(DateUtils.setTimeToMidnight(DateUtils.formatDate(loadslipFilterDto.getFromCreatedDate(),PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) +"' and ls.createdDate <= '"+DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(loadslipFilterDto.getToCreatedDate(),PLAN_RECORD_DATE_FORMAT)),Constants.DATE_FORMATE_WITH_HYPHEN)+"'");
*/
        sb.append("and  ts.reportDate   >=TO_DATE('" + truckReportFilterDto.getFromReportDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
        sb.append(" and ts.reportDate <= TO_DATE('" + DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(truckReportFilterDto.getToReportDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getFromGateInDate()) && !StringUtils.isEmpty(truckReportFilterDto.getToGateInDate())) {
/*
      sb.append("  ls.createdDate   >='" +DateUtils.formatDate(DateUtils.setTimeToMidnight(DateUtils.formatDate(loadslipFilterDto.getFromCreatedDate(),PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) +"' and ls.createdDate <= '"+DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(loadslipFilterDto.getToCreatedDate(),PLAN_RECORD_DATE_FORMAT)),Constants.DATE_FORMATE_WITH_HYPHEN)+"'");
*/
        sb.append("and  ts.gateInDate   >=TO_DATE('" + truckReportFilterDto.getFromGateInDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
        sb.append(" and ts.gateInDate <= TO_DATE('" + DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(truckReportFilterDto.getToGateInDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getFromGateOutDate()) && !StringUtils.isEmpty(truckReportFilterDto.getToGateOutDate())) {
/*
      sb.append("  ls.createdDate   >='" +DateUtils.formatDate(DateUtils.setTimeToMidnight(DateUtils.formatDate(loadslipFilterDto.getFromCreatedDate(),PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) +"' and ls.createdDate <= '"+DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(loadslipFilterDto.getToCreatedDate(),PLAN_RECORD_DATE_FORMAT)),Constants.DATE_FORMATE_WITH_HYPHEN)+"'");
*/
        sb.append("and  ts.gateOutDate   >=TO_DATE('" + truckReportFilterDto.getFromGateOutDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
        sb.append(" and ts.gateOutDate <= TO_DATE('" + DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(truckReportFilterDto.getToGateOutDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      }

//      if (!StringUtils.isEmpty(truckReportFilterDto.getMarketSegment())) {
//        Map<String, String> marketSegMap = Utility.deriveTubeAndFlapBatchCodes();
//        //Here tube and flap batch codes are same based on MarketSegment
//        String tubeOrFlapBatchCode = marketSegMap.get(truckReportFilterDto.getMarketSegment());
//        sb.append("and   ldb.TUBE_BATCH = '" + tubeOrFlapBatchCode + "'");
//      }

      if (truckReportFilterDto.getMarketSegment() != null && truckReportFilterDto.getMarketSegment().size()>0){
        sb.append(" and ts.marketSegment in (" + Utility.join(truckReportFilterDto.getMarketSegment()) + ") ");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getTruckNumber())) {
//        sb.append("and   ts.truckNumber = '" + truckReportFilterDto.getTruckNumber() + "'");
        sb.append("and   LOWER(ts.truckNumber) LIKE '" + "%"+truckReportFilterDto.getTruckNumber().toLowerCase()+ "%" + "'");

      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getContainerNum())) {
        sb.append("and   ts.containerNum = '" + truckReportFilterDto.getContainerNum() + "'");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getInsertUser())) {
        sb.append("and   ts.insertUser = '" + truckReportFilterDto.getInsertUser() + "'");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getSource())) {
        sb.append("and   ts.sourceLoc = '" + truckReportFilterDto.getSource() + "'");
      }

      if (truckReportFilterDto.getStatuses()!=null && truckReportFilterDto.getStatuses().size()>0) {
        //sb.append("and   ts.status = '" + truckReportFilterDto.getStatus() + "'");
        sb.append(("and ts.status  in (" + Utility.join(truckReportFilterDto.getStatuses()) + ") "));
      }

      if (truckReportFilterDto.getBayStatus() != null && truckReportFilterDto.getBayStatus().size()>0) {
        sb.append("and   ts.bayStatus  in (" + Utility.join(truckReportFilterDto.getBayStatus()) + ") ");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getInvoice())) {
        sb.append("and   ts.sapInvoice = '" + truckReportFilterDto.getInvoice() + "'");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getStopType())){
        sb.append("and   ts.stopType = '" + truckReportFilterDto.getStopType() + "'");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getTransporter())){
        sb.append("and   ts.servprov = '" + truckReportFilterDto.getTransporter() + "'");
      }

      if (!StringUtils.isEmpty(truckReportFilterDto.getDestCountry())){
        sb.append("and   ts.destCountry = '" + truckReportFilterDto.getDestCountry() + "'");
      }

			if (!StringUtils.isEmpty(truckReportFilterDto.getIndentID())){
				sb.append("and   ts.indentId = '" + truckReportFilterDto.getIndentID() + "'");
			}

      if (truckReportFilterDto.getRejection() != null && truckReportFilterDto.getRejection().size()>0){
        sb.append("and   ts.rejectionstatus in (" + Utility.join(truckReportFilterDto.getRejection()) + ") ");
      }

      if (!truckReportFilterDto.getIndentCategoryList().isEmpty()){
        sb.append(" AND ts.indentCategory IN ("+Utility.join(truckReportFilterDto.getIndentCategoryList())+") ");
      }


      sb.append("  order by ts.reportDate desc");

      return sb;
    }
  }


  public Page<MtElr> getMtElr(MtElrFilterDto filterDto){
    List<MtElr> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "location_id, \n" +
            "servprov, \n" +
            "elr_flag, \n" +
            "insert_user, \n" +
            "insert_date, \n" +
            "update_user, \n" +
            "update_date \n" +
            "from mt_elr \n" +
            "where " +
            "upper(location_id) like '%'|| upper(:locationId)||'%' \n" +
            "and upper(servprov) like '%'|| upper(:servprov)||'%' \n" +
            "and upper(elr_flag) like '%'|| upper(:elrFlag)||'%' order by location_id, elr_flag")
            .setParameter("locationId",filterDto.getLocationId())
            .setParameter("servprov",filterDto.getServprov())
            .setParameter("elrFlag",filterDto.getElrFlag())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(location_id) \n" +
                    "FROM mt_elr \n" +
                    "where " +
                    "upper(location_id) like '%'|| upper(:locationId)||'%' \n" +
                    "and upper(servprov) like '%'|| upper(:servprov)||'%' \n" +
                    "and upper(elr_flag) like '%'|| upper(:elrFlag)||'%'")
            .setParameter("locationId",filterDto.getLocationId())
            .setParameter("servprov",filterDto.getServprov())
            .setParameter("elrFlag",filterDto.getElrFlag());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    MtElr itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new MtElr();
      itemBean.setLocationId((line[0]!=null)?line[0].toString():"0");
      itemBean.setServprov((line[1]!=null)?line[1].toString():"");
      itemBean.setElrFlag((line[2]!=null)?line[2].toString():"");
      itemBean.setInsertUser((line[3]!=null)?line[3].toString():"");
      itemBean.setUpdateUser((line[5]!=null)?line[5].toString():"");
      try {
        itemBean.setInsertDate((Date) line[4]);
        itemBean.setUpdateDate((Date) line[6]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting Insert date");
        itemBean.setInsertDate(null);
      }
      itemsList.add(itemBean);
    }
    return new PageImpl<MtElr>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }


  public Page<MtExcessWaitingLocLimit> getMtExcessWaitingLocLimit(MtExcessWaitingLocLimitFilterDto filterDto){
    List<MtExcessWaitingLocLimit> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "reporting_loc, \n" +
            "excess_time, \n" +
            "insert_user, \n" +
            "insert_date, \n" +
            "update_user, \n" +
            "update_date \n" +
            "from MT_EXCESS_WAITING_LOC_LIMIT \n" +
            "where " +
            "upper(reporting_loc) like '%'|| upper(:reportingLoc)||'%' \n" +
            "and upper(excess_time) like '%'|| upper(:excessTime)||'%' order by reporting_loc asc")
            .setParameter("reportingLoc",filterDto.getReportingLoc())
            .setParameter("excessTime",filterDto.getExcessTime())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(reporting_loc) \n" +
                    "FROM MT_EXCESS_WAITING_LOC_LIMIT \n" +
                    "where " +
                    "upper(reporting_loc) like '%'|| upper(:reportingLoc)||'%' \n" +
                    "and upper(excess_time) like '%'|| upper(:excessTime)||'%'")
            .setParameter("reportingLoc",filterDto.getReportingLoc())
            .setParameter("excessTime",filterDto.getExcessTime());
    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    MtExcessWaitingLocLimit itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new MtExcessWaitingLocLimit();
      itemBean.setReportingLoc((line[0]!=null)?line[0].toString():"0");
      itemBean.setExcessTime(Double.valueOf((line[1] != null) ? line[1].toString() : "0"));
      itemBean.setInsertUser((line[2]!=null)?line[2].toString():"");
      itemBean.setUpdateUser((line[4]!=null)?line[4].toString():"");
      try {
        itemBean.setInsertDate((Date) line[3]);
        itemBean.setUpdateDate((Date) line[5]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting Insert date");
        itemBean.setInsertDate(null);
      }
      itemsList.add(itemBean);
    }
    return new PageImpl<MtExcessWaitingLocLimit>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }


  public Page<MtExcessWaitingRepLimit> getMtExcessWaitingRepLimit(MtExcessWaitingRepLimitFilterDto filterDto){
    List<MtExcessWaitingRepLimit> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "reporting_loc, \n" +
            "excess_time, \n" +
            "insert_user, \n" +
            "insert_date, \n" +
            "update_user, \n" +
            "update_date \n" +
            "from MT_EXCESS_WAITING_REP_LIMIT \n" +
            "where " +
            "upper(reporting_loc) like '%'|| upper(:reportingLoc)||'%' \n" +
            "and upper(excess_time) like '%'|| upper(:excessTime)||'%' order by reporting_loc asc")
            .setParameter("reportingLoc",filterDto.getReportingLoc())
            .setParameter("excessTime",filterDto.getExcessTime())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(reporting_loc) \n" +
                    "FROM MT_EXCESS_WAITING_REP_LIMIT \n" +
                    "where " +
                    "upper(reporting_loc) like '%'|| upper(:reportingLoc)||'%' \n" +
                    "and upper(excess_time) like '%'|| upper(:excessTime)||'%'")
            .setParameter("reportingLoc",filterDto.getReportingLoc())
            .setParameter("excessTime",filterDto.getExcessTime());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    MtExcessWaitingRepLimit itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new MtExcessWaitingRepLimit();
      itemBean.setReportingLoc((line[0]!=null)?line[0].toString():"0");
      itemBean.setExcessTime(Double.valueOf((line[1] != null) ? line[1].toString() : "0"));
      itemBean.setInsertUser((line[2]!=null)?line[2].toString():"");
      itemBean.setUpdateUser((line[4]!=null)?line[4].toString():"");
      try {
        itemBean.setInsertDate((Date) line[3]);
        itemBean.setUpdateDate((Date) line[5]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting Insert date");
        itemBean.setInsertDate(null);
      }
      itemsList.add(itemBean);
    }
    return new PageImpl<MtExcessWaitingRepLimit>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }



  public Page<MTLocationBayEntity> getMtLocationBay(MtLocationBayFilterDto filterDto){
    List<MTLocationBayEntity> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "location_id, \n" +
            "bay_id, \n" +
            "bay_desc, \n" +
            "bay_status, \n" +
            "insert_user, \n" +
            "insert_date, \n" +
            "update_user, \n" +
            "update_date, \n" +
            "lb_id \n" +
            "from MT_LOCATION_BAY \n" +
            "where " +
            "upper(nvl(location_id,'-')) like '%'|| upper(:locationId)||'%' \n" +
            "and upper (nvl(bay_id,'-')) like '%'|| upper(:bayId)||'%' \n" +
            "and upper (nvl(bay_status,'-')) like '%'|| upper(:bayStatus)||'%' order by location_id, bay_id")
            .setParameter("locationId",filterDto.getLocationId())
            .setParameter("bayId",filterDto.getBayId())
            .setParameter("bayStatus",filterDto.getBayStatus())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(location_id) \n" +
                    "FROM MT_LOCATION_BAY \n" +
                    "where " +
                    "upper(nvl(location_id,'-')) like '%'|| upper(:locationId)||'%' \n" +
                    "and upper (nvl(bay_id,'-')) like '%'|| upper(:bayId)||'%' \n" +
                    "and upper (nvl(bay_status,'-')) like '%'|| upper(:bayStatus)||'%'")
            .setParameter("locationId",filterDto.getLocationId())
            .setParameter("bayId",filterDto.getBayId())
            .setParameter("bayStatus",filterDto.getBayStatus());
    // .setFirstResult(filterDto.getIndex()*filterDto.getPageLength());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    MTLocationBayEntity itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new MTLocationBayEntity();
      itemBean.setLocationId((line[0]!=null)?line[0].toString():"0");
      itemBean.setBayId((line[1]!=null)?line[1].toString():"");
      itemBean.setBayDescription((line[2]!=null)?line[2].toString():"");
      itemBean.setBayStatus((line[3]!=null)?line[3].toString():"");
      itemBean.setInsertUser((line[4]!=null)?line[4].toString():"");
      itemBean.setUpdateUser((line[6]!=null)?line[6].toString():"");
      itemBean.setLbId(Integer.valueOf((line[8]!=null)?line[8].toString():"0"));
      try {
        itemBean.setInsertDate((Date) line[5]);
        itemBean.setUpdateDate((Date) line[7]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting Insert date");
        itemBean.setInsertDate(null);
      }
      itemsList.add(itemBean);
    }
    return new PageImpl<MTLocationBayEntity>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }



  public Page<MTLocation> getMtLocation(MtLocationFilterDto filterDto){
    System.out.println("BEFORE CALLING sql STATEMENT:  "+new Gson().toJson(filterDto));
    List<MTLocation> itemsList  = new ArrayList<>();
    Query q = null;
    Query countQuery =null;
    if(!filterDto.isExcelExport()){
      q = entityManager.createNativeQuery("select " +
              "location_id, \n" +
              "location_desc, \n" +
              "location_type, \n" +
              "location_address, \n" +
              "city, \n" +
              "state, \n" +
              "postal_code, \n" +
              "country, \n" +
              "is_active, \n" +
              "lat, \n" +
              "lon, \n" +
              "state_code, \n" +
              "gst_no, \n" +
              "gst_state, \n" +
              "pan_no, \n" +
              "ft_access_key, \n" +
              "location_class, \n" +
              "linked_plant, \n" +
              "insert_user, \n" +
              "insert_date, \n" +
              "update_user, \n" +
              "update_date, " +
              "email_id\n" +
              "from mt_location \n" +
              "where " +
              "upper(location_id) like '%'|| upper(:id)||'%' \n" +
              "and upper (nvl(location_desc,'-')) like '%'|| upper(:description)||'%' \n" +
              "and upper (nvl(location_type,'-'))  like '%'|| upper(:type)||'%' \n" +
              "and upper (nvl(city,'-'))  like '%'|| upper(:city)||'%' \n" +
              "and upper (nvl(state,'-')) like '%'|| upper(:state)||'%' \n" +
              "and upper(nvl(country,'-'))  like '%'|| upper(:country)||'%' \n" +
              "and upper(nvl(state_code,0)) like '%'|| upper(:stateCode)||'%' \n" +
              "and upper(nvl(gst_no,'-')) like '%'|| upper(:gstNum)||'%' \n" +
              "and upper(nvl(gst_state,0)) like '%'|| upper(:gstState)||'%' \n" +
              "and upper(nvl(ft_access_key,'-')) like '%'|| upper(:ftAccessKey)||'%' \n" +
              "and upper(nvl(linked_plant,'-'))  like '%'|| upper(:linkedPlant)||'%' \n" +
              "and upper(nvl(location_class,'-'))  like '%'|| upper(:locationClass)||'%' order by location_id")
              .setParameter("id",filterDto.getId())
              .setParameter("description",filterDto.getLocationDesc())
              .setParameter("type",filterDto.getLocationType())
              .setParameter("city",filterDto.getCity())
              .setParameter("state",filterDto.getState())
              .setParameter("country",filterDto.getCountry())
              .setParameter("stateCode",filterDto.getStateCode())
              .setParameter("gstState",filterDto.getGstState())
              .setParameter("gstNum",filterDto.getGstNo())
              .setParameter("ftAccessKey",filterDto.getFtAccessKey())
              .setParameter("linkedPlant",filterDto.getLinkedPlant())
              .setParameter("locationClass",filterDto.getLocationClass())
              .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
              .setMaxResults(filterDto.getPageLength());

      countQuery = entityManager.createNativeQuery(
              "SELECT \n" +
                      "count(location_id) \n" +
                      "FROM mt_location \n" +
                      "where " +
                      "upper(location_id) like '%'|| upper(:id)||'%' \n" +
                      "and upper (nvl(location_desc,'-')) like '%'|| upper(:description)||'%' \n" +
                      "and upper (nvl(location_type,'-'))  like '%'|| upper(:type)||'%' \n" +
                      "and upper (nvl(city,'-'))  like '%'|| upper(:city)||'%' \n" +
                      "and upper (nvl(state,'-')) like '%'|| upper(:state)||'%' \n" +
                      "and upper(nvl(country,'-'))  like '%'|| upper(:country)||'%' \n" +
                      "and upper(nvl(state_code,0)) like '%'|| upper(:stateCode)||'%' \n" +
                      "and upper(nvl(gst_no,'-')) like '%'|| upper(:gstNum)||'%' \n" +
                      "and upper(nvl(gst_state,0)) like '%'|| upper(:gstState)||'%' \n" +
                      "and upper(nvl(ft_access_key,'-')) like '%'|| upper(:ftAccessKey)||'%' \n" +
                      "and upper(nvl(linked_plant,'-'))  like '%'|| upper(:linkedPlant)||'%' \n" +
                      "and upper(nvl(location_class,'-'))  like '%'|| upper(:locationClass)||'%'")
              .setParameter("id",filterDto.getId())
              .setParameter("description",filterDto.getLocationDesc())
              .setParameter("type",filterDto.getLocationType())
              .setParameter("city",filterDto.getCity())
              .setParameter("state",filterDto.getState())
              .setParameter("country",filterDto.getCountry())
              .setParameter("stateCode",filterDto.getStateCode())
              .setParameter("gstState",filterDto.getGstState())
              .setParameter("gstNum",filterDto.getGstNo())
              .setParameter("ftAccessKey",filterDto.getFtAccessKey())
              .setParameter("linkedPlant",filterDto.getLinkedPlant())
              .setParameter("locationClass",filterDto.getLocationClass());
      // .setFirstResult(filterDto.getIndex()*filterDto.getPageLength());

      List<Object> countObject = countQuery.getResultList();
      int count  = Integer.valueOf(countObject.get(0).toString());
      System.out.println("******************Count Value:" + Integer.valueOf(countObject.get(0).toString()) );
      List<Object[]> res = q.getResultList();
      Iterator it = res.iterator();
      MTLocation  itemBean;
      Object[] line;
      while(it.hasNext()) {
        line = (Object[]) it.next();
        itemBean = new MTLocation();
        itemBean.setId((line[0]!=null)?line[0].toString():"0");
        itemBean.setDescription((line[1]!=null)?line[1].toString():"");
        itemBean.setType((line[2]!=null)?line[2].toString():"");
        itemBean.setAddress((line[3]!=null)?line[3].toString():"");
        itemBean.setCity((line[4]!=null)?line[4].toString():"");
        itemBean.setState((line[5]!=null)?line[5].toString():"");
        itemBean.setPostalCode((line[6]!=null)?line[6].toString():"");
        itemBean.setCountry((line[7]!=null)?line[7].toString():"");
        itemBean.setIsActive((line[8]!=null)?line[8].toString():"");
        itemBean.setLatitude(Double.valueOf((line[9]!=null)?line[9].toString():"0"));
        itemBean.setLongitude(Double.valueOf((line[10]!=null)?line[10].toString():"0"));
        itemBean.setStateCode(Integer.valueOf((line[11]!=null)?line[11].toString():"0"));
        itemBean.setGstNum((line[12]!=null)?line[12].toString():"");
        itemBean.setGstState((line[13]!=null)?line[13].toString():"");
        itemBean.setPanNum((line[14]!=null)?line[14].toString():"");
        itemBean.setFtAccessKey((line[15]!=null)?line[15].toString():"");
        itemBean.setLocationClass((line[16]!=null)?line[16].toString():"");
        itemBean.setLinkedPlant((line[17]!=null)?line[17].toString():"");
        itemBean.setInsertUser((line[18]!=null)?line[18].toString():"");
        itemBean.setUpdateUser((line[20]!=null)?line[20].toString():"");
        itemBean.setEmailID(line[22]!= null ? line[22].toString():"");

        try {
          itemBean.setInsertDate((Date) line[19]);
          itemBean.setUpdateDate((Date) line[21]);
        }catch(Exception e){
          System.out.println("*********** Exception while formatting Insert date");
          itemBean.setInsertDate(null);
        }
        itemsList.add(itemBean);
      }

      return new PageImpl<MTLocation>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);

    }
    else{
      q = entityManager.createNativeQuery("select " +
              "location_id, \n" +
              "location_desc, \n" +
              "location_type, \n" +
              "location_address, \n" +
              "city, \n" +
              "state, \n" +
              "postal_code, \n" +
              "country, \n" +
              "is_active, \n" +
              "lat, \n" +
              "lon, \n" +
              "state_code, \n" +
              "gst_no, \n" +
              "gst_state, \n" +
              "pan_no, \n" +
              "ft_access_key, \n" +
              "location_class, \n" +
              "linked_plant \n" +
              "insert_user, \n" +
              "insert_date, \n" +
              "update_user, \n" +
              "update_date, " +
              "email_id " +
              "from mt_location \n" +
              "where " +
              "upper(location_id) like '%'|| upper(:id)||'%' \n" +
              "and upper (nvl(location_desc,'-')) like '%'|| upper(:description)||'%' \n" +
              "and upper (nvl(location_type,'-'))  like '%'|| upper(:type)||'%' \n" +
              "and upper (nvl(city,'-'))  like '%'|| upper(:city)||'%' \n" +
              "and upper (nvl(state,'-')) like '%'|| upper(:state)||'%' \n" +
              "and upper(nvl(country,'-'))  like '%'|| upper(:country)||'%' \n" +
              "and upper(nvl(state_code,0)) like '%'|| upper(:stateCode)||'%' \n" +
              "and upper(nvl(gst_no,'-')) like '%'|| upper(:gstNum)||'%' \n" +
              "and upper(nvl(gst_state,0)) like '%'|| upper(:gstState)||'%' \n" +
              "and upper(nvl(ft_access_key,'-')) like '%'|| upper(:ftAccessKey)||'%' \n" +
              "and upper(nvl(linked_plant,'-'))  like '%'|| upper(:linkedPlant)||'%' \n" +
              "and upper(nvl(location_class,'-'))  like '%'|| upper(:locationClass)||'%' order by location_id")
              .setParameter("id",filterDto.getId())
              .setParameter("description",filterDto.getLocationDesc())
              .setParameter("type",filterDto.getLocationType())
              .setParameter("city",filterDto.getCity())
              .setParameter("state",filterDto.getState())
              .setParameter("country",filterDto.getCountry())
              .setParameter("stateCode",filterDto.getStateCode())
              .setParameter("gstState",filterDto.getGstState())
              .setParameter("gstNum",filterDto.getGstNo())
              .setParameter("ftAccessKey",filterDto.getFtAccessKey())
              .setParameter("linkedPlant",filterDto.getLinkedPlant())
              .setParameter("locationClass",filterDto.getLocationClass());

      List<Object[]> res = q.getResultList();
      Iterator it = res.iterator();
      MTLocation  itemBean;
      Object[] line;
      while(it.hasNext()) {
        line = (Object[]) it.next();
        itemBean = new MTLocation();
        itemBean.setId((line[0]!=null)?line[0].toString():"0");
        itemBean.setDescription((line[1]!=null)?line[1].toString():"");
        itemBean.setType((line[2]!=null)?line[2].toString():"");
        itemBean.setAddress((line[3]!=null)?line[3].toString():"");
        itemBean.setCity((line[4]!=null)?line[4].toString():"");
        itemBean.setState((line[5]!=null)?line[5].toString():"");
        itemBean.setPostalCode((line[6]!=null)?line[6].toString():"");
        itemBean.setCountry((line[7]!=null)?line[7].toString():"");
        itemBean.setIsActive((line[8]!=null)?line[8].toString():"");
        itemBean.setLatitude(Double.valueOf((line[9]!=null)?line[9].toString():"0"));
        itemBean.setLongitude(Double.valueOf((line[10]!=null)?line[10].toString():"0"));
        itemBean.setStateCode(Integer.valueOf((line[11]!=null)?line[11].toString():"0"));
        itemBean.setGstNum((line[12]!=null)?line[12].toString():"");
        itemBean.setGstState((line[13]!=null)?line[13].toString():"");
        itemBean.setPanNum((line[14]!=null)?line[14].toString():"");
        itemBean.setFtAccessKey((line[15]!=null)?line[15].toString():"");
        itemBean.setLocationClass((line[16]!=null)?line[16].toString():"");
        itemBean.setLinkedPlant((line[17]!=null)?line[17].toString():"");
        itemBean.setInsertUser((line[18]!=null)?line[18].toString():"");
        itemBean.setUpdateUser((line[19]!=null)?line[19].toString():"");
        itemBean.setEmailID(line[21]!= null ? line[21].toString():"");
        try {
          itemBean.setInsertDate((Date) line[18]);
          itemBean.setUpdateDate((Date) line[20]);
        }catch(Exception e){
          System.out.println("*********** Exception while formatting Insert date");
          itemBean.setInsertDate(null);
        }
        itemsList.add(itemBean);
      }
      return new PageImpl<MTLocation>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), 0);
    }

  }














  public Page<CtOtmFreightBasis> getCtOtmFreightBasis(CtOtmFreightBasisFilterDto filterDto){
    List<CtOtmFreightBasis> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "basis, \n" +
            "description, \n" +
            "in_paas, \n" +
            "in_otm, \n" +
            "otm_basis \n" +
            "from ct_otm_freight_basis \n" +
            "where " +
            " upper(basis) like '%'|| upper(:basis)||'%'")
            .setParameter("basis",filterDto.getBasis())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(basis) \n" +
                    "FROM ct_otm_freight_basis \n" +
                    "where " +
                    " upper(basis) like '%'|| upper(:basis)||'%'")
            .setParameter("basis",filterDto.getBasis());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    CtOtmFreightBasis itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new CtOtmFreightBasis();
      itemBean.setBasis((line[0]!=null)?line[0].toString():"0");
      itemBean.setDescription((line[1]!=null)?line[1].toString():"");
      itemBean.setInPaas((line[2]!=null)?line[2].toString():"");
      itemBean.setInOtm((line[3]!=null)?line[3].toString():"");
      itemBean.setOtmBasis((line[4]!=null)?line[4].toString():"");
      itemsList.add(itemBean);
    }
    return new PageImpl<CtOtmFreightBasis>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }

  public Page<CtUom> getCtUom(CtUomFilterDto filterDto){
    List<CtUom> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "item_id, \n" +
            "item_description, \n" +
            "item_category, \n" +
            "batch_code, \n" +
            "insert_user, \n" +
            "insert_date, \n" +
            "update_user, \n" +
            "update_date, \n" +
            "valve_id \n" +
            "from mt_valve \n" +
            "where " +
            "upper(item_id) like '%'|| upper(:itemId)||'%' \n" +
            "and upper(item_description) like '%'|| upper(:itemDesc)||'%' \n" +
            "and upper(item_category) like '%'|| upper(:itemCategory)||'%' \n" +
            "and upper(batch_code) like '%'|| upper(:batchCode)||'%'")
            .setParameter("itemId",filterDto.getItemId())
            .setParameter("itemDesc",filterDto.getItemDescription())
            .setParameter("itemCategory",filterDto.getItemCategory())
            .setParameter("batchCode",filterDto.getBatchCode())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(item_id) \n" +
                    "FROM mt_valve \n" +
                    "where " +
                    "upper(item_id) like '%' || upper(:itemId)||'%' \n" +
                    "and upper(item_description) like '%'|| upper(:itemDesc)||'%' \n" +
                    "and upper(item_category) like '%'|| upper(:itemCategory)||'%' \n" +
                    "and upper(batch_code) like '%'|| upper(:batchCode)||'%'")
            .setParameter("itemId",filterDto.getItemId())
            .setParameter("itemDesc",filterDto.getItemDescription())
            .setParameter("itemCategory",filterDto.getItemCategory())
            .setParameter("batchCode",filterDto.getBatchCode())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    CtUom itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new CtUom();
      itemBean.setItemId((line[0]!=null)?line[0].toString():"0");
      itemBean.setItemDescription((line[1]!=null)?line[1].toString():"");
      itemBean.setItemCategory((line[2]!=null)?line[2].toString():"");
      itemBean.setBatchCode((line[3]!=null)?line[3].toString():"");

      itemBean.setInsertUser((line[4]!=null)?line[4].toString():"");
      itemBean.setUpdateUser((line[6]!=null)?line[6].toString():"");
      try {
        itemBean.setInsertDate((Date) line[5]);
        itemBean.setUpdateDate((Date) line[7]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting Insert date");
        itemBean.setInsertDate(null);
      }
      itemBean.setValveId(Double.valueOf((line[8]!=null)?line[8].toString():"0"));
      itemsList.add(itemBean);
    }
    return new PageImpl<CtUom>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }


  public Page<CtUomMap> getCtUomMap(CtUomMapFilterDto filterDto){
    List<CtUomMap> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "item_id, \n" +
            "item_description, \n" +
            "item_category, \n" +
            "batch_code, \n" +
            "insert_user, \n" +
            "insert_date, \n" +
            "update_user, \n" +
            "update_date, \n" +
            "valve_id \n" +
            "from mt_valve \n" +
            "where " +
            "upper(item_id) like '%'|| upper(:itemId)||'%' \n" +
            "and upper(item_description) like '%'|| upper(:itemDesc)||'%' \n" +
            "and upper(item_category) like '%'|| upper(:itemCategory)||'%' \n" +
            "and upper(batch_code) like '%'|| upper(:batchCode)||'%'")
            .setParameter("itemId",filterDto.getItemId())
            .setParameter("itemDesc",filterDto.getItemDescription())
            .setParameter("itemCategory",filterDto.getItemCategory())
            .setParameter("batchCode",filterDto.getBatchCode())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(item_id) \n" +
                    "FROM mt_valve \n" +
                    "where " +
                    "upper(item_id) like '%' || upper(:itemId)||'%' \n" +
                    "and upper(item_description) like '%'|| upper(:itemDesc)||'%' \n" +
                    "and upper(item_category) like '%'|| upper(:itemCategory)||'%' \n" +
                    "and upper(batch_code) like '%'|| upper(:batchCode)||'%'")
            .setParameter("itemId",filterDto.getItemId())
            .setParameter("itemDesc",filterDto.getItemDescription())
            .setParameter("itemCategory",filterDto.getItemCategory())
            .setParameter("batchCode",filterDto.getBatchCode())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    CtUomMap itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new CtUomMap();
      itemBean.setItemId((line[0]!=null)?line[0].toString():"0");
      itemBean.setItemDescription((line[1]!=null)?line[1].toString():"");
      itemBean.setItemCategory((line[2]!=null)?line[2].toString():"");
      itemBean.setBatchCode((line[3]!=null)?line[3].toString():"");

      itemBean.setInsertUser((line[4]!=null)?line[4].toString():"");
      itemBean.setUpdateUser((line[6]!=null)?line[6].toString():"");
      try {
        itemBean.setInsertDate((Date) line[5]);
        itemBean.setUpdateDate((Date) line[7]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting Insert date");
        itemBean.setInsertDate(null);
      }
      itemBean.setValveId(Double.valueOf((line[8]!=null)?line[8].toString():"0"));
      itemsList.add(itemBean);
    }
    return new PageImpl<CtUomMap>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }

  public Page<MtIncoterms> getMtIncoterms(MtIncotermsFilterDto filterDto){
    List<MtIncoterms> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "item_id, \n" +
            "item_description, \n" +
            "item_category, \n" +
            "batch_code, \n" +
            "insert_user, \n" +
            "insert_date, \n" +
            "update_user, \n" +
            "update_date, \n" +
            "valve_id \n" +
            "from mt_valve \n" +
            "where " +
            "upper(item_id) like '%'|| upper(:itemId)||'%' \n" +
            "and upper(item_description) like '%'|| upper(:itemDesc)||'%' \n" +
            "and upper(item_category) like '%'|| upper(:itemCategory)||'%' \n" +
            "and upper(batch_code) like '%'|| upper(:batchCode)||'%'")
            .setParameter("itemId",filterDto.getItemId())
            .setParameter("itemDesc",filterDto.getItemDescription())
            .setParameter("itemCategory",filterDto.getItemCategory())
            .setParameter("batchCode",filterDto.getBatchCode())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(item_id) \n" +
                    "FROM mt_valve \n" +
                    "where " +
                    "upper(item_id) like '%' || upper(:itemId)||'%' \n" +
                    "and upper(item_description) like '%'|| upper(:itemDesc)||'%' \n" +
                    "and upper(item_category) like '%'|| upper(:itemCategory)||'%' \n" +
                    "and upper(batch_code) like '%'|| upper(:batchCode)||'%'")
            .setParameter("itemId",filterDto.getItemId())
            .setParameter("itemDesc",filterDto.getItemDescription())
            .setParameter("itemCategory",filterDto.getItemCategory())
            .setParameter("batchCode",filterDto.getBatchCode())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    MtIncoterms itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new MtIncoterms();
      itemBean.setItemId((line[0]!=null)?line[0].toString():"0");
      itemBean.setItemDescription((line[1]!=null)?line[1].toString():"");
      itemBean.setItemCategory((line[2]!=null)?line[2].toString():"");
      itemBean.setBatchCode((line[3]!=null)?line[3].toString():"");

      itemBean.setInsertUser((line[4]!=null)?line[4].toString():"");
      itemBean.setUpdateUser((line[6]!=null)?line[6].toString():"");
      try {
        itemBean.setInsertDate((Date) line[5]);
        itemBean.setUpdateDate((Date) line[7]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting Insert date");
        itemBean.setInsertDate(null);
      }
      itemBean.setValveId(Double.valueOf((line[8]!=null)?line[8].toString():"0"));
      itemsList.add(itemBean);
    }
    return new PageImpl<MtIncoterms>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }


  public Page<MTTruck> getMtTruck(MtTruckFilterDto filterDto){
    List<MTTruck> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "item_id, \n" +
            "item_description, \n" +
            "item_category, \n" +
            "batch_code, \n" +
            "insert_user, \n" +
            "insert_date, \n" +
            "update_user, \n" +
            "update_date, \n" +
            "valve_id \n" +
            "from mt_valve \n" +
            "where " +
            "nvl(upper(item_id),'-') like '%'|| upper(:itemId)||'%' \n" +
            "and nvl(upper(item_description),'-') like '%'|| upper(:itemDesc)||'%' \n" +
            "and nvl(upper(item_category),'-') like '%'|| upper(:itemCategory)||'%' \n" +
            "and nvl(upper(batch_code),'-') like '%'|| upper(:batchCode)||'%'")
            .setParameter("itemId",filterDto.getItemId())
            .setParameter("itemDesc",filterDto.getItemDescription())
            .setParameter("itemCategory",filterDto.getItemCategory())
            .setParameter("batchCode",filterDto.getBatchCode())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(item_id) \n" +
                    "FROM mt_valve \n" +
                    "where " +
                    "nvl(upper(item_id),'-') like '%'|| upper(:itemId)||'%' \n" +
                    "and nvl(upper(item_description),'-') like '%'|| upper(:itemDesc)||'%' \n" +
                    "and nvl(upper(item_category),'-') like '%'|| upper(:itemCategory)||'%' \n" +
                    "and nvl(upper(batch_code),'-') like '%'|| upper(:batchCode)||'%'")
            .setParameter("itemId",filterDto.getItemId())
            .setParameter("itemDesc",filterDto.getItemDescription())
            .setParameter("itemCategory",filterDto.getItemCategory())
            .setParameter("batchCode",filterDto.getBatchCode());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    MTTruck itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new MTTruck();
//      itemBean.setItemId((line[0]!=null)?line[0].toString():"0");
//      itemBean.setItemDescription((line[1]!=null)?line[1].toString():"");
//      itemBean.setItemCategory((line[2]!=null)?line[2].toString():"");
//      itemBean.setBatchCode((line[3]!=null)?line[3].toString():"");

      itemBean.setInsertUser((line[4]!=null)?line[4].toString():"");
      itemBean.setUpdateUser((line[6]!=null)?line[6].toString():"");
      try {
        itemBean.setInsertDate((Date) line[5]);
        itemBean.setUpdateDate((Date) line[7]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting Insert date");
        itemBean.setInsertDate(null);
      }
      itemsList.add(itemBean);
    }
    return new PageImpl<MTTruck>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }


  public Page<MtContact> getMtContact(MtContactFilterDto filterDto){
    List<MtContact> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "item_id, \n" +
            "item_description, \n" +
            "item_category, \n" +
            "batch_code, \n" +
            "insert_user, \n" +
            "insert_date, \n" +
            "update_user, \n" +
            "update_date, \n" +
            "valve_id \n" +
            "from mt_valve \n" +
            "where " +
            "upper(item_id) like '%'|| upper(:itemId)||'%' \n" +
            "and upper(item_description) like '%'|| upper(:itemDesc)||'%' \n" +
            "and upper(item_category) like '%'|| upper(:itemCategory)||'%' \n" +
            "and upper(batch_code) like '%'|| upper(:batchCode)||'%'")
            .setParameter("itemId",filterDto.getItemId())
            .setParameter("itemDesc",filterDto.getItemDescription())
            .setParameter("itemCategory",filterDto.getItemCategory())
            .setParameter("batchCode",filterDto.getBatchCode())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(item_id) \n" +
                    "FROM mt_valve \n" +
                    "where " +
                    "upper(item_id) like '%' || upper(:itemId)||'%' \n" +
                    "and upper(item_description) like '%'|| upper(:itemDesc)||'%' \n" +
                    "and upper(item_category) like '%'|| upper(:itemCategory)||'%' \n" +
                    "and upper(batch_code) like '%'|| upper(:batchCode)||'%'")
            .setParameter("itemId",filterDto.getItemId())
            .setParameter("itemDesc",filterDto.getItemDescription())
            .setParameter("itemCategory",filterDto.getItemCategory())
            .setParameter("batchCode",filterDto.getBatchCode())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    MtContact itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new MtContact();
      itemBean.setItemId((line[0]!=null)?line[0].toString():"0");
      itemBean.setItemDescription((line[1]!=null)?line[1].toString():"");
      itemBean.setItemCategory((line[2]!=null)?line[2].toString():"");
      itemBean.setBatchCode((line[3]!=null)?line[3].toString():"");

      itemBean.setInsertUser((line[4]!=null)?line[4].toString():"");
      itemBean.setUpdateUser((line[6]!=null)?line[6].toString():"");
      try {
        itemBean.setInsertDate((Date) line[5]);
        itemBean.setUpdateDate((Date) line[7]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting Insert date");
        itemBean.setInsertDate(null);
      }
      itemBean.setValveId(Double.valueOf((line[8]!=null)?line[8].toString():"0"));
      itemsList.add(itemBean);
    }
    return new PageImpl<MtContact>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }



  public Page<MTCustomer> getMtCustomer(MtCustomerFilterDto filterDto){
    List<MTCustomer> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "item_id, \n" +
            "item_description, \n" +
            "item_category, \n" +
            "batch_code, \n" +
            "insert_user, \n" +
            "insert_date, \n" +
            "update_user, \n" +
            "update_date, \n" +
            "valve_id \n" +
            "from mt_valve \n" +
            "where " +
            "upper(item_id) like '%'|| upper(:itemId)||'%' \n" +
            "and upper(item_description) like '%'|| upper(:itemDesc)||'%' \n" +
            "and upper(item_category) like '%'|| upper(:itemCategory)||'%' \n" +
            "and upper(batch_code) like '%'|| upper(:batchCode)||'%'")
            .setParameter("itemId",filterDto.getItemId())
            .setParameter("itemDesc",filterDto.getItemDescription())
            .setParameter("itemCategory",filterDto.getItemCategory())
            .setParameter("batchCode",filterDto.getBatchCode())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(item_id) \n" +
                    "FROM mt_valve \n" +
                    "where " +
                    "upper(item_id) like '%' || upper(:itemId)||'%' \n" +
                    "and upper(item_description) like '%'|| upper(:itemDesc)||'%' \n" +
                    "and upper(item_category) like '%'|| upper(:itemCategory)||'%' \n" +
                    "and upper(batch_code) like '%'|| upper(:batchCode)||'%'")
            .setParameter("itemId",filterDto.getItemId())
            .setParameter("itemDesc",filterDto.getItemDescription())
            .setParameter("itemCategory",filterDto.getItemCategory())
            .setParameter("batchCode",filterDto.getBatchCode());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    MTCustomer itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new MTCustomer();
//      itemBean.setItemId((line[0]!=null)?line[0].toString():"0");
//      itemBean.setItemDescription((line[1]!=null)?line[1].toString():"");
//      itemBean.setItemCategory((line[2]!=null)?line[2].toString():"");
//      itemBean.setBatchCode((line[3]!=null)?line[3].toString():"");
//
//      itemBean.setInsertUser((line[4]!=null)?line[4].toString():"");
//      itemBean.setUpdateUser((line[6]!=null)?line[6].toString():"");
//      try {
//        itemBean.setInsertDate((Date) line[5]);
//        itemBean.setUpdateDate((Date) line[7]);
//      }catch(Exception e){
//        System.out.println("*********** Exception while formatting Insert date");
//        itemBean.setInsertDate(null);
//      }
      itemsList.add(itemBean);
    }
    return new PageImpl<MTCustomer>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }




  public Page<MtCustomerShipTo> getMtCustomerShipTo(MtCustomerShipToFilterDto filterDto){
    List<MtCustomerShipTo> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "item_id, \n" +
            "item_description, \n" +
            "item_category, \n" +
            "batch_code, \n" +
            "insert_user, \n" +
            "insert_date, \n" +
            "update_user, \n" +
            "update_date, \n" +
            "valve_id \n" +
            "from mt_valve \n" +
            "where " +
            "upper(item_id) like '%'|| upper(:itemId)||'%' \n" +
            "and upper(item_description) like '%'|| upper(:itemDesc)||'%' \n" +
            "and upper(item_category) like '%'|| upper(:itemCategory)||'%' \n" +
            "and upper(batch_code) like '%'|| upper(:batchCode)||'%'")
            .setParameter("itemId",filterDto.getItemId())
            .setParameter("itemDesc",filterDto.getItemDescription())
            .setParameter("itemCategory",filterDto.getItemCategory())
            .setParameter("batchCode",filterDto.getBatchCode())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(item_id) \n" +
                    "FROM mt_valve \n" +
                    "where " +
                    "upper(item_id) like '%' || upper(:itemId)||'%' \n" +
                    "and upper(item_description) like '%'|| upper(:itemDesc)||'%' \n" +
                    "and upper(item_category) like '%'|| upper(:itemCategory)||'%' \n" +
                    "and upper(batch_code) like '%'|| upper(:batchCode)||'%'")
            .setParameter("itemId",filterDto.getItemId())
            .setParameter("itemDesc",filterDto.getItemDescription())
            .setParameter("itemCategory",filterDto.getItemCategory())
            .setParameter("batchCode",filterDto.getBatchCode());
           // .setFirstResult(filterDto.getIndex()*filterDto.getPageLength());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    MtCustomerShipTo itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new MtCustomerShipTo();
      itemBean.setItemId((line[0]!=null)?line[0].toString():"0");
      itemBean.setItemDescription((line[1]!=null)?line[1].toString():"");
      itemBean.setItemCategory((line[2]!=null)?line[2].toString():"");
      itemBean.setBatchCode((line[3]!=null)?line[3].toString():"");

      itemBean.setInsertUser((line[4]!=null)?line[4].toString():"");
      itemBean.setUpdateUser((line[6]!=null)?line[6].toString():"");
      try {
        itemBean.setInsertDate((Date) line[5]);
        itemBean.setUpdateDate((Date) line[7]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting Insert date");
        itemBean.setInsertDate(null);
      }
      itemBean.setValveId(Double.valueOf((line[8]!=null)?line[8].toString():"0"));
      itemsList.add(itemBean);
    }
    return new PageImpl<MtCustomerShipTo>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }

  // USER_ID, STATUS, USER_ROLE_ID, PLANT_CODE, FIRST_NAME, LAST_NAME, EMAIL_ID
  public Page<UserEntity> getUser(UserFilterDto filterDto){
    System.out.println("BEFORE CALLING sql STATEMENT---:  "+new Gson().toJson(filterDto));
    List<UserEntity> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "user_id, \n" +
            "password, \n" +
            "status, \n" +
            "user_role_id, \n" +
            "plant_code, \n" +
            "first_name, \n" +
            "last_name, \n" +
            "email_id, \n" +
            "last_login_date, \n" +
            "insert_user, \n" +
            "insert_date, \n" +
            "update_user, \n" +
            "update_date \n" +
            "from um_user \n" +
            "where " +
            "nvl(upper(user_id),'-') like '%'||upper(:userId)||'%' \n" +
            "and nvl(upper(status),'-') like '%'||upper(:status)||'%' \n" +
            "and nvl(upper(user_role_id),'-') like '%'||upper(:role)||'%' \n" +
            "and nvl(upper(plant_code),'-') like '%'||upper(:plantCode)||'%' \n" +
            "and nvl(upper(first_name),'-') like '%'||upper(:firstName)||'%' \n" +
            "and nvl(upper(last_name),'-') like '%'||upper(:lastName)||'%' \n" +
            "and nvl(upper(email_id),'-') like '%'||upper(:email)||'%' order by user_id")
            .setParameter("userId",filterDto.getUserId())
            .setParameter("status",filterDto.getStatus())
            .setParameter("role",filterDto.getRoleId())
            .setParameter("plantCode",filterDto.getPlantCode())
            .setParameter("firstName",filterDto.getFirstName())
            .setParameter("lastName",filterDto.getLastName())
            .setParameter("email",filterDto.getEmailId())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(user_id) \n" +
                    "FROM um_user \n" +
                    "where " +
                    "nvl(upper(user_id),'-') like '%'||upper(:userId)||'%' \n" +
                    "and nvl(upper(status),'-') like '%'||upper(:status)||'%' \n" +
                    "and nvl(upper(user_role_id),'-') like '%'||upper(:role)||'%' \n" +
                    "and nvl(upper(plant_code),'-') like '%'||upper(:plantCode)||'%' \n" +
                    "and nvl(upper(first_name),'-') like '%'||upper(:firstName)||'%' \n" +
                    "and nvl(upper(last_name),'-') like '%'||upper(:lastName)||'%' \n" +
                    "and nvl(upper(email_id),'-') like '%'||upper(:email)||'%'")
            .setParameter("userId",filterDto.getUserId())
            .setParameter("status",filterDto.getStatus())
            .setParameter("role",filterDto.getRoleId())
            .setParameter("plantCode",filterDto.getPlantCode())
            .setParameter("firstName",filterDto.getFirstName())
            .setParameter("lastName",filterDto.getLastName())
            .setParameter("email",filterDto.getEmailId());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    UserEntity itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new UserEntity();
      itemBean.setUserId((line[0]!=null)?line[0].toString():"0");
      itemBean.setPassword((line[1]!=null)?line[1].toString():"");
      itemBean.setStatus((line[2]!=null)?line[2].toString():"");
      itemBean.setRoleId((line[3]!=null)?line[3].toString():"");
      itemBean.setPlantCode((line[4]!=null)?line[4].toString():"");
      itemBean.setFirstName((line[5]!=null)?line[5].toString():"");
      itemBean.setLastName((line[6]!=null)?line[6].toString():"");
      itemBean.setEmailId((line[7]!=null)?line[7].toString():"");
      itemBean.setInsertUser((line[9]!=null)?line[9].toString():"");
      itemBean.setUpdateUser((line[11]!=null)?line[11].toString():"");
      try {
        itemBean.setLastLoginDate((Date) line[8]);
        itemBean.setInsertDate((Date) line[10]);
        itemBean.setUpdateDate((Date) line[12]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting Insert date");
        itemBean.setInsertDate(null);
      }
      itemsList.add(itemBean);
    }
    return new PageImpl<UserEntity>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }


  public int updatePassword(UserFilterDto filterDto){
    int i = 0;
    EntityTransaction tx = null;
    try{
      tx = entityManager.getTransaction();
      Query q = entityManager.createNativeQuery("update " +
              "um_user \n" +
              "set password='"+filterDto.getPassword() +"' "+
              "where " +
              "user_id ='"+filterDto.getUserId() +"' ");
      i = q.executeUpdate();
      tx.commit();
      return i;
    }catch (Exception e){
      e.printStackTrace();
    }
    return i;
  }


  public Page<UmUserAssociationEntity> getUserAssociation(UserAssociationFilterDto filterDto){
    //System.out.println("******BEFORE CALLING sql STATEMENT:  "+new Gson().toJson(filterDto));
    List<UmUserAssociationEntity> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "user_id, \n" +
            "association_identifier, \n" +
            "association_value, \n" +
            "insert_user, \n" +
            "insert_date, \n" +
            "update_user, \n" +
            "update_date, \n" +
            "ua_id \n" +
            "from um_user_association \n" +
            "where " +
            "nvl(upper(user_id),'-') like '%'||upper(:userId)||'%' \n" +
            "and nvl(upper(association_identifier),'-') like '%'||upper(:associationIdentifier)||'%' \n" +
            "and nvl(upper(association_value),'-') like '%'||upper(:associationValue)||'%' order by user_id \n" )
            .setParameter("userId",filterDto.getUserId())
            .setParameter("associationIdentifier",filterDto.getAssociationIdentifier())
            .setParameter("associationValue",filterDto.getAssociationValue())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(user_id) \n" +
                    "FROM um_user_association \n" +
                    "where " +
                    "nvl(upper(user_id),'-') like '%'||upper(:userId)||'%' \n" +
                    "and nvl(upper(association_identifier),'-') like '%'||upper(:associationIdentifier)||'%' \n" +
                    "and nvl(upper(association_value),'-') like '%'||upper(:associationValue)||'%' \n" )
            .setParameter("userId",filterDto.getUserId())
            .setParameter("associationIdentifier",filterDto.getAssociationIdentifier())
            .setParameter("associationValue",filterDto.getAssociationValue());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    UmUserAssociationEntity itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new UmUserAssociationEntity();
      itemBean.setUserId((line[0]!=null)?line[0].toString():"0");
      itemBean.setAssociationIdentifier((line[1]!=null)?line[1].toString():"");
      itemBean.setAssociationValue((line[2]!=null)?line[2].toString():"");
      itemBean.setInsertUser((line[3]!=null)?line[3].toString():"");
      itemBean.setUpdateUser((line[5]!=null)?line[5].toString():"");
      itemBean.setUaId(Integer.valueOf((line[7]!=null)?line[7].toString():"0"));
      //itemBean.setUaId(Double.valueOf(line[7]!=null)?line[7].toString():"");
      try {
        itemBean.setInsertDate((Date) line[4]);
        itemBean.setUpdateDate((Date) line[6]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting Insert date");
        itemBean.setInsertDate(null);
      }

      itemsList.add(itemBean);
    }
    return new PageImpl<UmUserAssociationEntity>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }




  public Page<UmUserRole> getUserRole(UserRoleFilterDto filterDto){
    List<UmUserRole> itemsList  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "user_role_id,  \n" +
            "description, \n" +
            "insert_user, \n" +
            "insert_date, \n" +
            "update_user, \n" +
            "update_date \n" +
            "from um_user_role \n" +
            "where " +
            "nvl(upper(user_role_id),'-') like '%'||upper(:userRoleId)||'%' \n" +
            "and nvl(upper(description),'-') like '%'||upper(:description)||'%' \n" )
            .setParameter("userRoleId",filterDto.getUserRoleId())
            .setParameter("description",filterDto.getDescription())
            .setFirstResult(filterDto.getIndex()*filterDto.getPageLength())
            .setMaxResults(filterDto.getPageLength());

    Query countQuery = entityManager.createNativeQuery(
            "SELECT \n" +
                    "count(user_role_id) \n" +
                    "FROM um_user_role \n" +
                    "where " +
                    "nvl(upper(user_role_id),'-') like '%'||upper(:userRoleId)||'%' \n" +
                    "and nvl(upper(description),'-') like '%'||upper(:description)||'%' \n" )
                     .setParameter("userRoleId",filterDto.getUserRoleId())
                     .setParameter("description",filterDto.getDescription());

    List<Object> countObject = countQuery.getResultList();
    int count  = Integer.valueOf(countObject.get(0).toString());
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    UmUserRole itemBean;
    Object[] line;
    while(it.hasNext()) {
      line = (Object[]) it.next();
      itemBean = new UmUserRole();
      itemBean.setUserRoleId((line[0]!=null)?line[0].toString():"0");
      itemBean.setDescription((line[1]!=null)?line[1].toString():"");
      itemBean.setInsertUser((line[2]!=null)?line[2].toString():"");
      itemBean.setUpdateUser((line[4]!=null)?line[4].toString():"");
      try {
        itemBean.setInsertDate((Date) line[3]);
        itemBean.setUpdateDate((Date) line[5]);
      }catch(Exception e){
        System.out.println("*********** Exception while formatting Insert date");
        itemBean.setInsertDate(null);
      }
      itemsList.add(itemBean);
    }
    return new PageImpl<UmUserRole>(itemsList,PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count);
  }


  @Override
  public DispatchPlanFilterDto filterDisplanPlanInfo(DispatchPlanFilterDto filterDto, ApplicationUser applicationUser, Optional<MTLocation> mtLocation){

    /*Getting all the EXT_WAREHOUSE Locs*/
    List<String> extWarehouseLocs = utilityService.getLinkedExtWareHouse("1007");

    StringBuilder dispatchPlanQuery = setQueryForDispatchPlan(filterDto, applicationUser, mtLocation, extWarehouseLocs);

    StringBuilder countQueryForDispatchPlans = setCountQueryForDispatchPlans(filterDto, applicationUser, mtLocation, extWarehouseLocs);

    Query q = entityManager.unwrap(Session.class).createNativeQuery(dispatchPlanQuery.toString())
        .addScalar("sourceLocation", StandardBasicTypes.STRING).addScalar("lineNumber",StandardBasicTypes.INTEGER).addScalar("volumeUom", StandardBasicTypes.STRING)
        .addScalar("tempDispatchDate", StandardBasicTypes.DATE).addScalar("planId", StandardBasicTypes.LONG).addScalar("id", StandardBasicTypes.LONG)
        .addScalar("destinationLocation",StandardBasicTypes.STRING).addScalar("itemId", StandardBasicTypes.STRING).addScalar("itemDescription",StandardBasicTypes.STRING)
        .addScalar("category", StandardBasicTypes.STRING).addScalar("tte",StandardBasicTypes.DOUBLE).addScalar("batchCode", StandardBasicTypes.STRING)
        .addScalar("quantity", StandardBasicTypes.INTEGER).addScalar("priority", StandardBasicTypes.INTEGER).addScalar("status", StandardBasicTypes.STRING)
        .addScalar("appStatusEnum", Utility.getCustomEnumType(Constants.DispatchPlanItemAppStatus.class.getName())).addScalar("marketSegment", StandardBasicTypes.STRING).addScalar("approvedQuantity", StandardBasicTypes.INTEGER)
        .addScalar("unapprovedQuantity", StandardBasicTypes.INTEGER).addScalar("deletedApprQuantity", StandardBasicTypes.INTEGER).addScalar("deletedUnApprQuantity", StandardBasicTypes.INTEGER)
        .addScalar("availableQuantity", StandardBasicTypes.INTEGER).addScalar("totalAvailableQuantity", StandardBasicTypes.INTEGER).addScalar("reservedQuantity", StandardBasicTypes.INTEGER)
        .addScalar("dispatchedQuantity", StandardBasicTypes.INTEGER).addScalar("insertUser", StandardBasicTypes.STRING).addScalar("updateUser", StandardBasicTypes.STRING)
        .addScalar("destinationDescription", StandardBasicTypes.STRING).addScalar("comments", StandardBasicTypes.STRING).addScalar("tempInsertDate", StandardBasicTypes.DATE)
        .addScalar("tempUpdateDate", StandardBasicTypes.DATE).addScalar("weight", StandardBasicTypes.DOUBLE).addScalar("weightUom", StandardBasicTypes.STRING)
        .addScalar("volume", StandardBasicTypes.DOUBLE).addScalar("loaded", StandardBasicTypes.INTEGER);

    List<Object> objectList = entityManager.createNativeQuery(countQueryForDispatchPlans.toString()).getResultList();
    int count = Integer.valueOf(objectList.get(0).toString());
    
    q.setFirstResult(filterDto.getIndex()*filterDto.getPageLength());
    q.setMaxResults(filterDto.getPageLength());
    List<DispatchPlanItemDto> dispatchPlanItemDtos = ((NativeQuery) q)
        .setResultTransformer(new AliasToBeanResultTransformer(DispatchPlanItemDto.class)).getResultList();

    /*Removing the data with source = 1007 and dest_loc = EXT_WAREHOUSE*/
//Commented This because now checking the condition in the query itself
//    if (mtLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equalsIgnoreCase(mtLocation.get().getLocationClass()) && filterDto.getIsViewPlans()) {
//      dispatchPlanItemDtos = dispatchPlanItemDtos.stream().parallel().filter(dp -> !(dp.getSourceLocation().equals("1007") && extWarehouseLocs.contains(dp.getDestinationLocation())))
//          .collect(Collectors.toList());
//    }
    /*Getting STANDARD_FTL truck Details*/
    MTTruckTypeInfo mtTruckTypeInfo = truckTypeInfoRepository.findOneByType("STANDARD_FTL").parallelStream().filter(mt -> StringUtils.isEmpty(mt.getVariant1())).findFirst().get();

    for (DispatchPlanItemDto dispatchPlanItemDto : dispatchPlanItemDtos) {
      if (!StringUtils.isEmpty(dispatchPlanItemDto.getTempDispatchDate()))
        dispatchPlanItemDto.setDispatchDate(DateUtils.formatDate(dispatchPlanItemDto.getTempDispatchDate(), PLAN_RECORD_DATE_FORMAT));
      if (!StringUtils.isEmpty(dispatchPlanItemDto.getTempInsertDate()))
        dispatchPlanItemDto.setInsertDate(DateUtils.formatDate(dispatchPlanItemDto.getTempInsertDate(), Constants.DATE_TIME_FORMAT));
      if (!StringUtils.isEmpty(dispatchPlanItemDto.getTempUpdateDate()))
        dispatchPlanItemDto.setUpdateDate(DateUtils.formatDate(dispatchPlanItemDto.getTempUpdateDate(), Constants.DATE_TIME_FORMAT));
//      if (Status.OPEN.name().equals(dispatchPlanItemDto.getStatus())) {
//        dispatchPlanItemDto.setPlanAge(ChronoUnit.DAYS.between(Instant.ofEpochMilli(DateUtils.setTimeToMidnight(dispatchPlanItemDto.getTempInsertDate()).getTime()),
//            Instant.ofEpochMilli(DateUtils.setTimeToMidnight(new Date()).getTime())) + "");
//      } else {
//        if (!StringUtils.isEmpty(dispatchPlanItemDto.getTempUpdateDate()))
//          dispatchPlanItemDto.setPlanAge(ChronoUnit.DAYS.between(Instant.ofEpochMilli(DateUtils.setTimeToMidnight(dispatchPlanItemDto.getTempInsertDate()).getTime()),
//              Instant.ofEpochMilli(DateUtils.setTimeToMidnight(dispatchPlanItemDto.getTempUpdateDate()).getTime())) + "");
//      }

      Long age = 0l;
      if(Constants.Status.OPEN.name().equals(dispatchPlanItemDto.getStatus())) {
        age = ChronoUnit.DAYS.between(Instant.ofEpochMilli(DateUtils.setTimeToMidnight(dispatchPlanItemDto.getTempDispatchDate()).getTime()),
            Instant.ofEpochMilli(DateUtils.setTimeToMidnight(new Date()).getTime())) ;
      }else {
        age = ChronoUnit.DAYS.between(Instant.ofEpochMilli(DateUtils.setTimeToMidnight(dispatchPlanItemDto.getTempDispatchDate()).getTime()),
            Instant.ofEpochMilli( dispatchPlanItemDto.getTempUpdateDate() != null ?  DateUtils.setTimeToMidnight(dispatchPlanItemDto.getTempUpdateDate()).getTime() : DateUtils.setTimeToMidnight(new Date()).getTime()));
      }
      dispatchPlanItemDto.setPlanAge(age >= 0 ? age +"" : 0 +"");

      dispatchPlanItemDto.setAppStatus(Utility.camelCase(dispatchPlanItemDto.getAppStatusEnum().getStatus()));
      dispatchPlanItemDto.setApprovalQuantity(0);
      dispatchPlanItemDto.setUnapprovalQuantity(0);
      dispatchPlanItemDto.setDeleteApprQuantity(0);
      dispatchPlanItemDto.setDeleteUnApprQuantity(0);
      dispatchPlanItemDto.setLoaded(dispatchPlanItemDto.getLoaded() !=null ? dispatchPlanItemDto.getLoaded() : 0);
      dispatchPlanItemDto.setDispatchedQuantity(dispatchPlanItemDto.getDispatchedQuantity() != null ? dispatchPlanItemDto.getDispatchedQuantity() : 0);
      dispatchPlanItemDto.calculateDispatchPlanBOMValues(dispatchPlanItemDto, applicationUser, mtTruckTypeInfo);
    }

    DispatchPlanFilterDto dispatchPlanFilterDto = new DispatchPlanFilterDto(dispatchPlanItemDtos, count);
    return dispatchPlanFilterDto;
  }

  private StringBuilder setCountQueryForDispatchPlans(DispatchPlanFilterDto filterDto, ApplicationUser applicationUser, Optional<MTLocation> mtLocation, List<String> extWarehouseLocs) {
    StringBuilder countQuery = new StringBuilder("select count(*) FROM   dispatch_plan dp " +
        "        LEFT JOIN disp_plan d " +
        "        ON dp.dispatch_plan_id = d.dispatch_plan_id " +
        "        LEFT JOIN dispatch_plan_bom dispBom " +
        "        ON dp.dispatch_plan_id = dispbom.dispatch_plan_id  and dp.line_num = dispbom.line_num " +
        "        LEFT JOIN MT_ITEM mt " +
        "               ON mt.item_id = dp.item_id where dp.id is not null ");
    StringBuilder whereClause = setWhereClauseForDispatchPlans(filterDto, applicationUser, mtLocation, extWarehouseLocs);

    countQuery.append(" "+whereClause+" ");
    return countQuery;
  }

  private StringBuilder setQueryForDispatchPlan(DispatchPlanFilterDto filterDto, ApplicationUser applicationUser, Optional<MTLocation> mtLocation, List<String> extWarehouseLocs) {
  /*  String appStatus = null;
    if (!StringUtils.isEmpty(filterDto.getStatus())){
      appStatus = "'"+filterDto.getStatus()+"'";
    }else {
      appStatus = Utility.join(filterDto.getStatuses().parallelStream().map(s -> s.name()).collect(Collectors.toList()));
    }*/
    StringBuilder mainQuery = null;
    mainQuery = new StringBuilder("select dps.* from(SELECT dp.app_status       AS appStatusEnum, " +
        "       dp.approved_qty     AS approvedQuantity, " +
        "       dp.avail_qty        AS availableQuantity," +
        "       dp.batch_code       AS batchCode, " +
        "       dp.item_category    AS category, " +
        "       dp.comments         AS comments, " +
        "       dp.deleted_qty      AS deletedApprQuantity, " +
        "       dp.dest_description AS destinationDescription," +
        "       dp.dest_loc         AS destinationLocation," +
        "       dp.dispatch_date    AS tempDispatchDate, " +
        "       dp.dispatched_qty   AS dispatchedQuantity, " +
        "       dp.id               AS id, " +
        "       dp.insert_date      AS tempInsertDate, " +
        "       dp.insert_user      AS insertUser, " +
        "       dp.item_description AS itemDescription, " +
        "       dp.item_id          AS itemId, " +
        "       dp.line_num         AS lineNumber, " +
        "       dp.market_segment   AS marketSegment, " +
        "       d.dispatch_plan_id AS planId, " +
        "       dp.priority         AS priority," +
        "       dp.quantity         AS quantity, " +
        "       dp.reserved_qty     as reservedQuantity, " +
        "       dp.source_loc       AS sourceLocation, " +
        "       dp.status           AS status, " +
        "       dp.tot_avail_qty    AS totalAvailableQuantity, " +
        "       dp.tte              AS tte, " +
        "       dp.unapp_qty        AS unapprovedQuantity, " +
        "       dp.unapp_del_qty    AS deletedUnApprQuantity, " +
        "       dp.update_date      AS tempUpdateDate, " +
        "       dp.update_user      AS updateUser," +
        "       dp.weight           AS weight," +
        "       dp.weight_uom       AS weightUom," +
        "       dp.volume           AS volume," +
        "       dp.volume_uom       AS volumeUom, " +
        "       dp.loaded_qty       AS loaded FROM   dispatch_plan dp " +
        "       LEFT JOIN disp_plan d " +
        "              ON dp.dispatch_plan_id = d.dispatch_plan_id " +
        "        LEFT JOIN MT_ITEM mt " +
        "               ON mt.item_id = dp.item_id" +
        "       where dp.id is not null" );

    StringBuilder whereClause = setWhereClauseForDispatchPlans(filterDto, applicationUser, mtLocation, extWarehouseLocs);

    mainQuery.append(" "+whereClause+" ");

    mainQuery.append(" ORDER BY dp.dispatch_date DESC ) dps");

    return mainQuery;

  }

  private StringBuilder setWhereClauseForDispatchPlans(DispatchPlanFilterDto filterDto, ApplicationUser applicationUser, Optional<MTLocation> mtLocation, List<String> extWarehouseLocs) {
    StringBuilder mainQuery = new StringBuilder();
    /*String appStatus = null;
    if (!StringUtils.isEmpty(filterDto.getStatus())){
      appStatus = "'"+filterDto.getStatus()+"'";
    }else {
      appStatus = Utility.join(filterDto.getStatuses().parallelStream().map(s -> s.name()).collect(Collectors.toList()));
    }*/

    if (mtLocation.isPresent() && (LocationType.EXT_WAREHOUSE.name()).equals(mtLocation.get().getLocationClass()) && filterDto.getIsViewPlans()) {
      /*if (!StringUtils.isEmpty(filterDto.getSourceCode())) {
//        predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("sourceLocation")), "%" + filterDto.getSourceCode().toLowerCase() + "%"));
        mainQuery.append(" and (dp.source_loc LIKE '%" + filterDto.getSourceCode() + "%' AND " +
            "(dp.source_loc = '" + applicationUser.getPlantCode() + "' OR  dp.source_loc = '" + mtLocation.get().getLinkedPlant() + "'))");
      } else  {
        mainQuery.append(" AND dp.source_loc IN ('" + applicationUser.getPlantCode() +"','"+mtLocation.get().getLinkedPlant()+"') ");
      }*/
      if (filterDto.getIsViewPlans()){
        /*For EXT_WAREHOUSE in View-plans page he can also view LINKED_PLANT loc plans.*/
        if (filterDto.getSourceCodeList() != null && !filterDto.getSourceCodeList().isEmpty()){
          mainQuery.append(" AND dp.source_loc IN ("+Utility.join(filterDto.getSourceCodeList())+") ");
        }else{
          /*In View Plans by befault EXT_WAREHOUSE & LINKED_PLANT loc plans will be displayed*/
          mainQuery.append(" AND dp.source_loc IN ('" + applicationUser.getPlantCode() +"','"+mtLocation.get().getLinkedPlant()+"') ");
        }
      }else {
        /*For Approve/Modify plans page he can only view the plans created at that perticular LOC*/
        mainQuery.append(" AND dp.source_loc IN ('" + applicationUser.getPlantCode() + "') ");
      }
      if (!StringUtils.isEmpty(filterDto.getInsertUser())){
        mainQuery.append(" AND dp.insert_user = '" + filterDto.getInsertUser() + "' ");
      }
/*      This will not fetch the plans with source 1007 and destionation = EXT_WAREHOUSE*/
      mainQuery.append(" AND (dp.source_loc != '1007' OR (dp.source_loc = '1007' AND dp.dest_loc NOT IN("+Utility.join(extWarehouseLocs)+"))) ");

    } else {
      /*DP_REP, L1_MGR, L2_MGR*/
      if (UserRole.getDPREPAccessRoles().contains(applicationUser.getRole())) {
        if (!StringUtils.isEmpty(filterDto.getSourceCode()) && !filterDto.getIsViewPlans()) {
          mainQuery.append(" AND dp.source_loc = '" + filterDto.getSourceCode() + "' ");
        }
        if (filterDto.getSourceCodeList() != null && !filterDto.getSourceCodeList().isEmpty()){
          mainQuery.append(" AND dp.source_loc IN ("+Utility.join(filterDto.getSourceCodeList())+") ");
        }
        if (!StringUtils.isEmpty(filterDto.getInsertUser())) {
          mainQuery.append(" AND dp.insert_user = '" + filterDto.getInsertUser() + "' ");
        }
      } else {
        // NOT DP_REP ROLE
        if (!StringUtils.isEmpty(filterDto.getInsertUser())) {
          mainQuery.append(" AND dp.insert_user = '" + filterDto.getInsertUser() + "' ");
        }
        mainQuery.append(" AND dp.source_loc = '" + applicationUser.getPlantCode() + "' ");
      }
    }
    if (filterDto.getStatus() != null && filterDto.getStatus().size()>0){
      mainQuery.append(" and dp.app_status IN (" + Utility.join(filterDto.getStatus()) + ") ");
    }

    if (filterDto.getPlanStatus() != null && filterDto.getPlanStatus().size()>0) {
      mainQuery.append(" AND dp.status IN (" + Utility.join(filterDto.getPlanStatus()) + ")");
    }
    if (filterDto.getPlanId() != null && filterDto.getPlanId() != 0)
      mainQuery.append(" AND d.dispatch_plan_id = '" + filterDto.getPlanId() + "' ");
    if (!StringUtils.isEmpty(filterDto.getDestinationCode()))
      mainQuery.append(" AND dp.dest_loc ='" + filterDto.getDestinationCode() + "' ");
    if (filterDto.getMarketSegment() != null && filterDto.getMarketSegment().size()>0)
      mainQuery.append(" AND dp.market_segment IN (" + Utility.join(filterDto.getMarketSegment()) + ")");
    if (!StringUtils.isEmpty(filterDto.getMaterialCode()))
      mainQuery.append(" AND dp.item_id LIKE '%" + filterDto.getMaterialCode() + "%' ");
    if (!StringUtils.isEmpty(filterDto.getMaterialDescription()))
      mainQuery.append(" AND dp.item_description LIKE '%" + filterDto.getMaterialDescription() + "%' ");
    if (filterDto.getMaterialGroup() != null && filterDto.getMaterialGroup().size() > 0) {
      mainQuery.append(" AND dp.item_category IN (" + Utility.join(filterDto.getMaterialGroup()) + ")");
    }

    /*Serach With updateUser*/
    if (!StringUtils.isEmpty(filterDto.getUpdateUser())) {
      mainQuery.append(" AND dp.update_user = '" + filterDto.getUpdateUser() + "' ");
    }

    //FromDispatch Date and ToDispatchDate
    if (!StringUtils.isEmpty(filterDto.getFromDispatchDate()) && !StringUtils.isEmpty(filterDto.getToDispatchDate())) {
      mainQuery.append(" and dp.dispatch_date >=TO_DATE('" + filterDto.getFromDispatchDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
//      mainQuery.append(" and dp.dispatch_date <= TO_DATE('" + DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(filterDto.getToDispatchDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      mainQuery.append(" and dp.dispatch_date < =TO_DATE('" + filterDto.getToDispatchDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");

    }

//    Priority Filter
    if (filterDto.getPriorityList() != null && !filterDto.getPriorityList().isEmpty()){
      mainQuery.append(" AND ( ");
      if (filterDto.getPriorityList().contains("ISNULL")){
        filterDto.getPriorityList().remove("ISNULL");
        mainQuery.append(" dp.priority is null ");
        if (!filterDto.getPriorityList().isEmpty()){
          mainQuery.append(" OR ");
        }
      }
      if (!filterDto.getPriorityList().isEmpty()){
        mainQuery.append(" dp.priority IN ("+Utility.joinInt(filterDto.getPriorityList().stream().map(p -> Integer.valueOf(p)).collect(Collectors.toList()))+") ");
      }

      mainQuery.append(" ) ");
    }

    if (!StringUtils.isEmpty(filterDto.getItemClassification())){
      mainQuery.append(" AND mt.item_classification = '"+filterDto.getItemClassification()+"' ");
    }
    return mainQuery;
  }












  public List<String> getServPros(){
    List<String> list  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "scac \n" +
            "from mt_scac order by scac asc") ;
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    String line;
    while(it.hasNext()) {
      line = (String) it.next();
      list.add(line);
    }
    return list;
  }

  public List<String> getLocationClasses(){
    List<String> list  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "distinct location_class \n" +
            "from mt_location order by location_class asc") ;
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    String line;
    while(it.hasNext()) {
      line = (String) it.next();
      if(line != null && !line.equals("") ){
        list.add(line);
      }
    }
    return list;
  }

  public List<String> getLocationIdsList(){
    List<String> list  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "location_id \n" +
            "from mt_location order by location_id asc") ;
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    String line;
    while(it.hasNext()) {
      line = (String) it.next();
      list.add(line);
    }
    return list;
  }

  public List<String> getUserIdsList(){
    List<String> list  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "user_id \n" +
            "from um_user order by user_id asc") ;
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    String line;
    while(it.hasNext()) {
      line = (String) it.next();
      list.add(line);
    }
    return list;
  }


  public List<String> getUserRoleIdsList(){
    List<String> list  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select " +
            "user_role_id \n" +
            "from um_user_role order by user_role_id asc") ;
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    String line;
    while(it.hasNext()) {
      line = (String) it.next();
      list.add(line);
    }
    return list;
  }


    public List<String> getDistinctScmGroupList(){
        List<String> list  = new ArrayList<>();
        Query q = entityManager.createNativeQuery("select distinct " +
                "scm_group \n" +
                "from mt_material_group order by scm_group asc") ;
        List<Object[]> res = q.getResultList();
        Iterator it = res.iterator();
        String line;
        while(it.hasNext()) {
                line = (String) it.next();
                if(line != null ){
                    list.add(line);
                }
        }
        return list;
    }




  public List<String> getPaasTruckTypeList(){
    List<String> list  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select distinct " +
            "truck_type \n" +
            "from mt_truck_type order by truck_type asc") ;
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    String line;
    while(it.hasNext()) {
      line = (String) it.next();
      if(line != null ){
        list.add(line);
      }
    }
    return list;
  }



  public List<String> getTruckTypeList(){
    List<String> list  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select distinct " +
            "truck_type \n" +
            "from mt_truck_type order by truck_type asc") ;
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    String line;
    while(it.hasNext()) {
      line = (String) it.next();
      if(line != null ){
        list.add(line);
      }
    }
    return list;
  }



  public List<String> getVariantsList(){
    List<String> list  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select distinct " +
            "ops_variant_1 \n" +
            "from mt_sap_truck_type order by ops_variant_1 asc") ;
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    String line;
    while(it.hasNext()) {
      line = (String) it.next();
      if(line != null ){
        list.add(line);
      }
    }
    return list;
  }

  public List<String> getItemGroupsList(){
    List<String> list  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select distinct " +
            "item_group \n" +
            "from mt_item order by item_group asc") ;
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    String line;
    while(it.hasNext()) {
      line = (String) it.next();
      if(line != null ){
        list.add(line);
      }
    }
    return list;
  }

  public List<String> getbatchCategoryList(){
    List<String> list  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select distinct " +
            "category \n" +
            "from mt_batch_codes order by category asc") ;
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    String line;
    while(it.hasNext()) {
      line = (String) it.next();
      if(line != null ){
        list.add(line);
      }
    }
    return list;
  }

  public List<String> getbatchCodesForValves(){
    List<String> list  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select distinct " +
            "batch_code \n" +
            "from mt_batch_codes where category ='Valve' order by batch_code asc");
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    String line;
    while(it.hasNext()) {
      line = (String) it.next();
      if(line != null && !line.equals("")){
        list.add(line);
      }
    }
    return list;
  }

  public List<String> getItemIdList(){
    List<String> list  = new ArrayList<>();
    Query q = entityManager.createNativeQuery("select distinct " +
            "item_id \n" +
            "from mt_item order by item_id asc") ;
    List<Object[]> res = q.getResultList();
    Iterator it = res.iterator();
    String line;
    while(it.hasNext()) {
      line = (String) it.next();
      if(line != null ){
        list.add(line);
      }
    }
    System.out.println("******Size:  "+list.size());
    return list;
  }

  @Override
  public Page<MTTruckDedicated> getFilteredMTTruckDedicated(TruckDedicatedFilterDto truckDedicatedFilterDto, ApplicationUser applicationUser) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery criteriaQuery= criteriaBuilder.createQuery(MTTruckDedicated.class);
    Root<MTTruckDedicated> root = criteriaQuery.from(MTTruckDedicated.class);
    root.alias("mtTruckDedicated");

    List<Predicate> predicates = getPredicatesForMTTruckDedicated(truckDedicatedFilterDto, applicationUser, criteriaBuilder, root);

    criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
    criteriaQuery.orderBy(criteriaBuilder.desc(root.get("insertDate")));

    CriteriaQuery countQuery = criteriaBuilder.createQuery(Long.class);
    Root<MTTruckDedicated> iRoot = countQuery.from(MTTruckDedicated.class);
    iRoot.alias("mtTruckDedicated");
    countQuery.select(criteriaBuilder.count(iRoot));
    Predicate restriction = criteriaQuery.getRestriction();
    if (restriction != null ){
      countQuery.where(restriction);
    }

    Object singleResult = entityManager.createQuery(countQuery).getSingleResult();
    Long count = singleResult != null ? (Long) singleResult : 0L ;

    TypedQuery<MTTruckDedicated> typedQuery = entityManager.createQuery(criteriaQuery);
    typedQuery.setFirstResult(truckDedicatedFilterDto.getIndex() * truckDedicatedFilterDto.getPageLength());
    typedQuery.setMaxResults(truckDedicatedFilterDto.getPageLength());
    List<MTTruckDedicated> mtTruckDedicateds = typedQuery.getResultList();

    return new PageImpl<>(mtTruckDedicateds, PageRequest.of(truckDedicatedFilterDto.getIndex(), truckDedicatedFilterDto.getPageLength(), Sort.Direction.DESC, "insertDate"), count);
  }

  private List<Predicate> getPredicatesForMTTruckDedicated(TruckDedicatedFilterDto truckDedicatedFilterDto, ApplicationUser applicationUser, CriteriaBuilder criteriaBuilder, Root<MTTruckDedicated> root) {
    List<Predicate> predicates = new ArrayList<>();

    if (!StringUtils.isEmpty(truckDedicatedFilterDto.getServprov())){
      predicates.add(criteriaBuilder.equal(root.get("servProv"), truckDedicatedFilterDto.getServprov()));
    }
    if (!StringUtils.isEmpty(truckDedicatedFilterDto.getSourceLoc())){
      predicates.add(criteriaBuilder.equal(root.get("sourceLoc"), truckDedicatedFilterDto.getSourceLoc()));
    }
    if (!StringUtils.isEmpty(truckDedicatedFilterDto.getDestLoc())){
      predicates.add(criteriaBuilder.equal(root.get("destLoc"), truckDedicatedFilterDto.getDestLoc()));
    }
    if (truckDedicatedFilterDto.getTruckType() != null && !truckDedicatedFilterDto.getTruckType().isEmpty()){
      predicates.add(criteriaBuilder.and(root.get("truckType").in(truckDedicatedFilterDto.getTruckType())));
    }
    if (!StringUtils.isEmpty(truckDedicatedFilterDto.getTruckNumber())){
      predicates.add(criteriaBuilder.equal(root.get("truckNumber"), truckDedicatedFilterDto.getTruckNumber()));
    }
    if (!StringUtils.isEmpty(truckDedicatedFilterDto.getFromExpiryDate()) && !StringUtils.isEmpty(truckDedicatedFilterDto.getToExpiryDate())){
      predicates.add(criteriaBuilder.and((criteriaBuilder.greaterThanOrEqualTo(root.get("expiryDate"), DateUtils.formatDate(truckDedicatedFilterDto.getFromExpiryDate(), PLAN_RECORD_DATE_FORMAT))),
          (criteriaBuilder.lessThanOrEqualTo(root.get("expiryDate"), DateUtils.formatDate(truckDedicatedFilterDto.getToExpiryDate(), PLAN_RECORD_DATE_FORMAT)))));
    }

    return predicates;
  }


  @Override
  public Page<TruckReport> filterIntransitTrucksInfo(TruckReportFilterDto truckReportFilterDto, List<Constants.TruckReportStatus> truckReportStatuses, ApplicationUser applicationUser){
    String mainQuery = getIntransitTrucksQuery(truckReportFilterDto, truckReportStatuses, applicationUser);

    String countQuery = getCountQueryForIntransitTrucks(truckReportFilterDto, truckReportStatuses, applicationUser);
/* Utility.getCustomEnumType(Constants.DispatchPlanItemAppStatus.class.getName())*/
    Query q = entityManager.unwrap(Session.class).createNativeQuery(mainQuery).addScalar("gateControlCode", StandardBasicTypes.STRING).addScalar("type", Utility.getCustomEnumType(Constants.SystemType.class.getName()))
        .addScalar("transporterSapCode", StandardBasicTypes.STRING).addScalar("containerNum", StandardBasicTypes.STRING).addScalar("containerCode", StandardBasicTypes.STRING)
        .addScalar("driverName", StandardBasicTypes.STRING).addScalar("driverMobile",StandardBasicTypes.STRING).addScalar("driverLicense", StandardBasicTypes.STRING)
        .addScalar("servprov",StandardBasicTypes.STRING).addScalar("truckType", StandardBasicTypes.STRING).addScalar("reportLocation",StandardBasicTypes.STRING)
        .addScalar("sourceLocation", StandardBasicTypes.STRING).addScalar("destinationLocation", StandardBasicTypes.STRING).addScalar("reportDate", StandardBasicTypes.DATE)
        .addScalar("gateInDate", StandardBasicTypes.DATE).addScalar("gateOutDate",StandardBasicTypes.DATE).addScalar("destGeofenceDate", StandardBasicTypes.DATE)
        .addScalar("dereportDate", StandardBasicTypes.DATE).addScalar("waitTimeHrs", StandardBasicTypes.DOUBLE).addScalar("tTHrs", StandardBasicTypes.DOUBLE)
        .addScalar("status", Utility.getCustomEnumType(Constants.TruckReportStatus.class.getName())).addScalar("rejectedStatus", Utility.getCustomEnumType(Constants.TruckReportStatus.class.getName()))
        .addScalar("inWeight", StandardBasicTypes.DOUBLE).addScalar("outWeight", StandardBasicTypes.DOUBLE).addScalar("netWeight", StandardBasicTypes.DOUBLE)
        .addScalar("bay", StandardBasicTypes.STRING).addScalar("bayStatus", Utility.getCustomEnumType(Constants.BayStatus.class.getName())).addScalar("rejectionCode", StandardBasicTypes.STRING)
        .addScalar("actualTruckType", StandardBasicTypes.STRING).addScalar("refCode", StandardBasicTypes.STRING).addScalar("insertUser", StandardBasicTypes.STRING)
        .addScalar("updateUser", StandardBasicTypes.STRING).addScalar("insertDate", StandardBasicTypes.DATE).addScalar("updateDate", StandardBasicTypes.DATE)
        .addScalar("shipmentID", StandardBasicTypes.STRING).addScalar("EtaDest", StandardBasicTypes.DATE).addScalar("reportedTruckType", StandardBasicTypes.STRING)
        .addScalar("activity", StandardBasicTypes.STRING).addScalar("comments",StandardBasicTypes.STRING).addScalar("destCountry", StandardBasicTypes.STRING)
        .addScalar("gpsProvider", StandardBasicTypes.STRING).addScalar("gpsEnabled", StandardBasicTypes.STRING).addScalar("truckNumber", StandardBasicTypes.STRING)
        .addScalar("indentId", StandardBasicTypes.STRING).addScalar("isPuc", StandardBasicTypes.STRING).addScalar("isInsurance", StandardBasicTypes.STRING)
        .addScalar("isSeatBelt", StandardBasicTypes.STRING).addScalar("isFirstAid", StandardBasicTypes.STRING).addScalar("isFireExtenguisher", StandardBasicTypes.STRING)
        .addScalar("isEmergencyCard", StandardBasicTypes.STRING).addScalar("isSparKArrestor", StandardBasicTypes.STRING).addScalar("truckCapacity", StandardBasicTypes.DOUBLE).addScalar("truckGrossVehicleWt",StandardBasicTypes.DOUBLE).addScalar("truckUnladenWt",StandardBasicTypes.DOUBLE)
            .addScalar("indentCategory", StandardBasicTypes.STRING).addScalar("isFitnessCert", StandardBasicTypes.STRING).addScalar("bsNorms", StandardBasicTypes.STRING).addScalar("fuelType", StandardBasicTypes.STRING)
            ;

    List<Object> objectList = entityManager.createNativeQuery(countQuery).getResultList();
    int count = Integer.valueOf(objectList.get(0).toString());

    q.setFirstResult(truckReportFilterDto.getIndex()*truckReportFilterDto.getPageLength());
    q.setMaxResults(truckReportFilterDto.getPageLength());
    List<TruckReport> truckReportList = ((NativeQuery) q)
        .setResultTransformer(new AliasToBeanResultTransformer(TruckReport.class)).getResultList();

    return new PageImpl<>(truckReportList, new PageRequest(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength()), count);

  }

  private String getCountQueryForIntransitTrucks(TruckReportFilterDto truckReportFilterDto, List<Constants.TruckReportStatus> truckReportStatuses, ApplicationUser applicationUser) {
    StringBuilder stringBuilder = new StringBuilder("select count(*) from (select tr.gate_Control_Code, ROW_NUMBER() OVER (PARTITION BY tr.shipment_id ORDER BY tr.insert_date ASC) rn   from truck_reporting tr " +
        "              left join mt_truck mt on mt.truck_Number=tr.truck_number " +
        "              left join ct_Gps ct on ct.gps_Provider=mt.gps_Provider " +
        "              left join Indent_Summary i on i.indent_Id = tr.indent_id ");

    stringBuilder.append("  RIGHT JOIN (  SELECT sh.shipment_id as  shipId  from ((SELECT DISTINCT st.shipment_id as shipmentId from shared_truck  st WHERE st.status = 'OPEN' ");

    if (!UserRole.getDPREPAccessRoles().contains(applicationUser.getRole())){
      stringBuilder.append(" AND st.plant_code = '"+applicationUser.getPlantCode()+"' ");
    }

    stringBuilder.append(" ) UNION  (SELECT DISTINCT s.shipment_id from shipment_stop s  WHERE s.activity = 'D' ");

    if (!UserRole.getDPREPAccessRoles().contains(applicationUser.getRole())){
      stringBuilder.append(" AND s.location_id = '"+applicationUser.getPlantCode()+"' ");
    }
    stringBuilder.append(")) a, shipment  sh where sh.shipment_id = a.shipmentId and sh.status = 'INTRANSIT') sharedData on sharedData.shipId = tr.shipment_id WHERE tr.status IN ("+
        Utility.join(truckReportStatuses.parallelStream().map(Constants.TruckReportStatus::name).collect(Collectors.toList()))+") ");

    setWhereClauseForIntransitTrucks(stringBuilder, truckReportFilterDto, applicationUser);
    stringBuilder.append(" ORDER BY tr.REPORTING_DATE DESC) ab where ab.rn=1 ");

    return stringBuilder.toString();
  }

  private String getIntransitTrucksQuery(TruckReportFilterDto truckReportFilterDto, List<Constants.TruckReportStatus> truckReportStatuses, ApplicationUser applicationUser) {
    StringBuilder mainQuery = new StringBuilder("select * from (select tr.GATE_CONTROL_CODE as gateControlCode,tr.TYPE as type,tr.TRANSPORTER_SAP_CODE as transporterSapCode,tr.CONTAINER_NUM as containerNum, " +
        "      tr.CONTAINER_CODE as containerCode,tr.DRIVER_NAME as driverName,tr.DRIVER_MOBILE as driverMobile,tr.DRIVER_LICENSE as driverLicense,tr.SERVPROV as servprov, " +
        "      tr.TRUCK_TYPE as truckType,tr.REPORTING_LOCATION as reportLocation,tr.SOURCE_LOC as sourceLocation,tr.DEST_LOC as destinationLocation,tr.REPORTING_DATE as reportDate, " +
        "      tr.GATEIN_DATE as gateInDate,tr.GATEOUT_DATE as gateOutDate ,tr.DEST_GEOFENCEIN_DATE as destGeofenceDate,tr.DEREPORTING_DATE as dereportDate,tr.REP_GI_HRS as waitTimeHrs, " +
        "      tr.GI_GO_HRS as tTHrs,tr.STATUS as status,tr.REJ_STATUS as rejectedStatus,tr.IN_WEIGHT as inWeight,tr.OUT_WEIGHT as outWeight,tr.NET_WEIGHT as netWeight ,tr.BAY as bay, " +
        "      tr.IS_PUC as isPuc, tr.IS_INSURANCE as isInsurance,tr.IS_SEATBELT as isSeatBelt,tr.IS_FIRST_AID as isFirstAid,tr.IS_FIRE_EXTENGUISHER as isFireExtenguisher, " +
        "      tr.IS_EMERGENCY_CARD as isEmergencyCard,tr.IS_SPARK_ARRESTOR as isSparKArrestor,tr.IS_FITNESS_CERT as isFitnessCert, " +
        "      tr.BAY_STATUS as bayStatus,tr.REJECTION_CODE as rejectionCode,tr.ACTUAL_TRUCK_TYPE as actualTruckType,tr.REF_CODE as refCode,tr.INSERT_USER as insertUser,tr.UPDATE_USER as updateUser, " +
        "      tr.INSERT_DATE as insertDate,tr.UPDATE_DATE as updateDate,tr.Shipment_ID as shipmentID,tr.ETA_DESTINATION as EtaDest,tr.REPORTED_TRUCK_TYPE as reportedTruckType,tr.ACTIVITY as activity, " +
        "      tr.COMMENTS as comments,mt.TRUCK_NUMBER as truckNumber,mt.GPS_ENABLED as gpsEnabled,ct.GPS_PROVIDER as gpsProvider,i.INDENT_ID as indentId, i.item_category as indentCategory, tr.DEST_COUNTRY as destCountry, " +
        "       ROW_NUMBER() OVER (PARTITION BY tr.shipment_id ORDER BY tr.insert_date ASC) rn " +
        "       from truck_reporting tr " +
        "      left join mt_truck mt on mt.truck_Number=tr.truck_number " +
        "       left join ct_Gps ct on ct.gps_Provider=mt.gps_Provider  " +
        "      left join Indent_Summary i on i.indent_Id = tr.indent_id " );

    mainQuery.append("  RIGHT JOIN (  SELECT sh.shipment_id as  shipId  from ((SELECT DISTINCT st.shipment_id as shipmentId from shared_truck  st WHERE st.status = 'OPEN' ");

    if (!UserRole.getDPREPAccessRoles().contains(applicationUser.getRole())){
      mainQuery.append(" AND st.plant_code = '"+applicationUser.getPlantCode()+"' ");
    }

    mainQuery.append(" ) UNION  (SELECT DISTINCT s.shipment_id from shipment_stop s  WHERE s.activity = 'D' ");

    if (!UserRole.getDPREPAccessRoles().contains(applicationUser.getRole())){
      mainQuery.append(" AND s.location_id = '"+applicationUser.getPlantCode()+"' ");
    }
    mainQuery.append(")) a, shipment  sh where sh.shipment_id = a.shipmentId and sh.status = 'INTRANSIT') sharedData on sharedData.shipId = tr.shipment_id WHERE tr.status IN ("+
        Utility.join(truckReportStatuses.parallelStream().map(Constants.TruckReportStatus::name).collect(Collectors.toList()))+") ");
    setWhereClauseForIntransitTrucks(mainQuery, truckReportFilterDto, applicationUser);
    mainQuery.append(" ORDER BY tr.REPORTING_DATE DESC ) ab where ab.rn = 1 ");

    return  mainQuery.toString();
  }

  private void setWhereClauseForIntransitTrucks(StringBuilder mainQuery, TruckReportFilterDto truckReportFilterDto, ApplicationUser applicationUser) {

    if (!StringUtils.isEmpty(truckReportFilterDto.getSource())) {
      mainQuery.append(" AND tr.SOURCE_LOC = '"+truckReportFilterDto.getSource()+"' ");
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getDestination())) {
      mainQuery.append(" AND tr.DEST_LOC = '"+truckReportFilterDto.getDestination()+"' ");
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getTransporter())) {
      mainQuery.append(" AND tr.SERVPROV = '"+truckReportFilterDto.getTransporter()+"' ");
    }
    if (truckReportFilterDto.getTruckType() !=null && truckReportFilterDto.getTruckType().size()>0) {
      mainQuery.append("AND ((tr.ACTUAL_TRUCK_TYPE IS NULL AND tr.TRUCK_TYPE IN ("+Utility.join(truckReportFilterDto.getTruckType())+")) " +
          "OR (tr.ACTUAL_TRUCK_TYPE IS NOT NULL AND tr.ACTUAL_TRUCK_TYPE IN ("+Utility.join(truckReportFilterDto.getTruckType())+"))) ");
    }
    if (!StringUtils.isEmpty(truckReportFilterDto.getTruckNumber())) {
      mainQuery.append(" AND tr.TRUCK_NUMBER = '"+truckReportFilterDto.getTruckNumber()+"' ");
    }

    // In Intransit Menu,trucks report location should not equal to User LoggedIn plant code i.e here truckReportFilterDto.getReportLocation()
    /*DP_REP, L1_MGR, L2_MGR*/
    if (!StringUtils.isEmpty(truckReportFilterDto.getReportLocation()) && (!UserRole.getDPREPAccessRoles().contains(applicationUser.getRole()))) {
      mainQuery.append(" AND tr.REPORTING_LOCATION != '"+truckReportFilterDto.getReportLocation()+"' ");
    }

    if (!StringUtils.isEmpty(truckReportFilterDto.getShipmentID())) {
      mainQuery.append(" AND tr.shipment_id = '"+truckReportFilterDto.getShipmentID()+"' ");
    }
  }

  @Override
  public Page<MTCustomer> filterMTCustomer(MtCustomerFilterDto mtCustomerFilterDto, ApplicationUser applicationUser) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<MTCustomer> criteriaQuery = criteriaBuilder.createQuery(MTCustomer.class);
    Root<MTCustomer> root = criteriaQuery.from(MTCustomer.class);
    root.alias("mtCustomer");

    List<Predicate> predicates = getPredicatedForMTCustomer(mtCustomerFilterDto, applicationUser, criteriaBuilder, root);

    criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
    criteriaQuery.orderBy(criteriaBuilder.desc(root.get("insertDate")));

    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    Root<MTCustomer> iRoot = countQuery.from(criteriaQuery.getResultType());
    iRoot.alias("mtCustomer");
    countQuery.select(criteriaBuilder.count(iRoot));
    Predicate restriction = criteriaQuery.getRestriction();
    if (restriction != null) {
      countQuery.where(restriction);
    }
    Long count = entityManager.createQuery(countQuery).getSingleResult();

    TypedQuery<MTCustomer> typedQuery = entityManager.createQuery(criteriaQuery);
    typedQuery.setFirstResult(mtCustomerFilterDto.getIndex() * mtCustomerFilterDto.getPageLength());
    typedQuery.setMaxResults(mtCustomerFilterDto.getPageLength());
    List<MTCustomer> mtCustomerList = typedQuery.getResultList();

    return new PageImpl<>(mtCustomerList, PageRequest.of(mtCustomerFilterDto.getIndex(), mtCustomerFilterDto.getPageLength()), count);
  }


  private List<Predicate> getPredicatedForMTCustomer(MtCustomerFilterDto mtCustomerFilterDto, ApplicationUser applicationUser, CriteriaBuilder criteriaBuilder, Root<MTCustomer> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (!StringUtils.isEmpty(mtCustomerFilterDto.getCustId())){
      predicates.add(criteriaBuilder.equal(root.get("id"), mtCustomerFilterDto.getCustId()));
    }
   /* if (!StringUtils.isEmpty(mtCustomerFilterDto.getCountry())){
      predicates.add(criteriaBuilder.equal(root.get("country"), mtCustomerFilterDto.getCountry()));
    }*/
//    if (!StringUtils.isEmpty(mtCustomerFilterDto.getIsActive())){
//      predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("isActive"), mtCustomerFilterDto.getIsActive()), criteriaBuilder.equal(root.get("isActive"), null)));
//    }else{
//      predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("isActive"), "Y"), criteriaBuilder.equal(root.get("isActive"), null)));
//    }

    if (!StringUtils.isEmpty(mtCustomerFilterDto.getCustName())){
      predicates.add(criteriaBuilder.and(criteriaBuilder.isNotNull(root.get("customerName")), criteriaBuilder.equal(root.get("customerName"), mtCustomerFilterDto.getCustName())));
    }
    if (!StringUtils.isEmpty(mtCustomerFilterDto.getCity())){
      predicates.add(criteriaBuilder.and(criteriaBuilder.isNotNull(root.get("city")), criteriaBuilder.equal(root.get("city"), mtCustomerFilterDto.getCity())));
    }
    if (!StringUtils.isEmpty(mtCustomerFilterDto.getState())){
      predicates.add(criteriaBuilder.and(criteriaBuilder.isNotNull(root.get("state")), criteriaBuilder.equal(root.get("state"), mtCustomerFilterDto.getState())));
    }
    if (!StringUtils.isEmpty(mtCustomerFilterDto.getCustAcctGrp())){
      predicates.add(criteriaBuilder.and(criteriaBuilder.isNotNull(root.get("custAcctGRP")), criteriaBuilder.equal(root.get("custAcctGRP"), mtCustomerFilterDto.getCustAcctGrp())));
    }
    if (mtCustomerFilterDto.getCustTypes() != null && !mtCustomerFilterDto.getCustTypes().isEmpty()){
      predicates.add(criteriaBuilder.and(criteriaBuilder.isNotNull(root.get("customerType")), root.<String>get("customerType").in(mtCustomerFilterDto.getCustTypes())));
    }
    if (mtCustomerFilterDto.getStateCodes() != null && !mtCustomerFilterDto.getStateCodes().isEmpty()){
      predicates.add(criteriaBuilder.and(criteriaBuilder.isNotNull(root.get("stateCode")), root.<String>get("stateCode").in(mtCustomerFilterDto.getStateCodes())));
    }



    if (!StringUtils.isEmpty(mtCustomerFilterDto.getFromInsertDate()) && !StringUtils.isEmpty(mtCustomerFilterDto.getToInsertDate())){
      predicates.add(criteriaBuilder.and((criteriaBuilder.greaterThanOrEqualTo(root.get("insertDate"), DateUtils.formatDate(mtCustomerFilterDto.getFromInsertDate(), PLAN_RECORD_DATE_FORMAT))),
          (criteriaBuilder.lessThanOrEqualTo(root.get("insertDate"), DateUtils.formatDate(mtCustomerFilterDto.getToInsertDate(), PLAN_RECORD_DATE_FORMAT)))));
    }

    return predicates;

  }


  @Override
  public ApiResponse getClsData(ClsDetailsFilterDto filterDto, ApplicationUser loggedInUser) {
    StringBuilder mainQuery = getClsDataQuery(filterDto, loggedInUser);

    StringBuilder countQuery = getContQueryForClsData(filterDto, loggedInUser);

    Query q = entityManager.unwrap(Session.class).createNativeQuery(mainQuery.toString())
        .addScalar("shipmentId", StandardBasicTypes.STRING).addScalar("loadslipId", StandardBasicTypes.STRING)
        .addScalar("gateouDate", StandardBasicTypes.DATE).addScalar("sourceLoc", StandardBasicTypes.STRING)
        .addScalar("destLoc", StandardBasicTypes.STRING).addScalar("containerNum", StandardBasicTypes.STRING)
        .addScalar("stosoNum", StandardBasicTypes.STRING).addScalar("delivery", StandardBasicTypes.STRING)
        .addScalar("sapInvoice", StandardBasicTypes.STRING).addScalar("sapInvoiceDate", StandardBasicTypes.DATE)
        .addScalar("customInvNumber", StandardBasicTypes.STRING).addScalar("piNo", StandardBasicTypes.STRING)
        .addScalar("customerName", StandardBasicTypes.STRING).addScalar("preInvNo", StandardBasicTypes.STRING)
        .addScalar("incoterm", StandardBasicTypes.STRING).addScalar("pol", StandardBasicTypes.STRING)
        .addScalar("pod", StandardBasicTypes.STRING).addScalar("billingParty", StandardBasicTypes.STRING)
        .addScalar("shippingLine", StandardBasicTypes.STRING).addScalar("contPickDate", StandardBasicTypes.DATE)
        .addScalar("stuffingDate", StandardBasicTypes.DATE).addScalar("bookingNum", StandardBasicTypes.STRING)
        .addScalar("postInvNo", StandardBasicTypes.STRING).addScalar("cha", StandardBasicTypes.STRING)
        .addScalar("plannedVessel", StandardBasicTypes.STRING).addScalar("vesselDepartPolDate", StandardBasicTypes.DATE)
        .addScalar("shippinBill", StandardBasicTypes.STRING).addScalar("shippingBillDate", StandardBasicTypes.DATE)
        .addScalar("gateInDate", StandardBasicTypes.DATE).addScalar("customsExamDate", StandardBasicTypes.DATE)
        .addScalar("gateoutDateCfs", StandardBasicTypes.DATE)
        .addScalar("gateinDatePort", StandardBasicTypes.DATE).addScalar("actualVessel", StandardBasicTypes.STRING)
        .addScalar("shippedOnboardDate", StandardBasicTypes.DATE).addScalar("eportRemark", StandardBasicTypes.STRING)
        .addScalar("isSyncOtm", StandardBasicTypes.STRING).addScalar("status", StandardBasicTypes.STRING)
        .addScalar("insertDate", StandardBasicTypes.DATE).addScalar("cofd", StandardBasicTypes.STRING)
        .addScalar("etaPod", StandardBasicTypes.DATE).addScalar("forwarder", StandardBasicTypes.STRING)
        .addScalar("invAmount", StandardBasicTypes.STRING).addScalar("leoDate", StandardBasicTypes.DATE)
        .addScalar("paymentTerms", StandardBasicTypes.STRING);

    List<Object> objectList = entityManager.createNativeQuery(countQuery.toString()).getResultList();
    int count = Integer.parseInt(objectList.get(0).toString());

    q.setFirstResult(filterDto.getIndex()*filterDto.getPageLength());
    q.setMaxResults(filterDto.getPageLength());
    List<ClsDataDto> clsDataDtos = ((NativeQuery) q)
        .setResultTransformer(new AliasToBeanResultTransformer(ClsDataDto.class)).getResultList();

    clsDataDtos = clsDataDtos.parallelStream().map(this::setDateToString).collect(Collectors.toList());

    return new ApiResponse(HttpStatus.OK, "", new PageImpl<>(clsDataDtos, PageRequest.of(filterDto.getIndex(), filterDto.getPageLength()), count));
  }

  public ClsDataDto setDateToString(ClsDataDto clsDataDto){
    clsDataDto.setGateouDateStr(clsDataDto.getGateouDate() != null ? DateUtils.formatDate(clsDataDto.getGateouDate(), Constants.DATE_TIME_FORMAT) : null);
    clsDataDto.setSapInvoiceDateStr(clsDataDto.getSapInvoiceDate() != null ? DateUtils.formatDate(clsDataDto.getSapInvoiceDate(), Constants.DATE_TIME_FORMAT) : null);
    clsDataDto.setContPickDateStr(clsDataDto.getContPickDate() != null ? DateUtils.formatDate(clsDataDto.getContPickDate(), Constants.DATE_TIME_FORMAT) : null);
    clsDataDto.setStuffingDateStr(clsDataDto.getStuffingDate() != null ? DateUtils.formatDate(clsDataDto.getStuffingDate(), Constants.DATE_TIME_FORMAT) : null);
    clsDataDto.setVesselDepartPolDateStr(clsDataDto.getVesselDepartPolDate() != null ? DateUtils.formatDate(clsDataDto.getVesselDepartPolDate(), Constants.DATE_TIME_FORMAT) : null);
    clsDataDto.setShippingBillDateStr(clsDataDto.getShippingBillDate() != null ? DateUtils.formatDate(clsDataDto.getShippingBillDate(), Constants.DATE_TIME_FORMAT) : null);
    clsDataDto.setGateInDateStr(clsDataDto.getGateInDate() != null ? DateUtils.formatDate(clsDataDto.getGateInDate(), Constants.DATE_TIME_FORMAT) : null);
    clsDataDto.setCustomsExamDateStr(clsDataDto.getCustomsExamDate() != null ? DateUtils.formatDate(clsDataDto.getCustomsExamDate(), Constants.DATE_TIME_FORMAT) : null);
    clsDataDto.setGateoutDateCfsStr(clsDataDto.getGateoutDateCfs() != null ? DateUtils.formatDate(clsDataDto.getGateoutDateCfs(), Constants.DATE_TIME_FORMAT) : null);
    clsDataDto.setGateinDatePortStr(clsDataDto.getGateinDatePort() != null ? DateUtils.formatDate(clsDataDto.getGateinDatePort(), Constants.DATE_TIME_FORMAT) : null);
    clsDataDto.setShippedOnboardDateStr(clsDataDto.getShippedOnboardDate() != null ? DateUtils.formatDate(clsDataDto.getShippedOnboardDate(), Constants.DATE_TIME_FORMAT) : null);
    clsDataDto.setInsertDateStr(clsDataDto.getInsertDate() != null ? DateUtils.formatDate(clsDataDto.getInsertDate(), Constants.DATE_TIME_FORMAT) : null);
    clsDataDto.setEtaPodStr(clsDataDto.getEtaPod() != null ? DateUtils.formatDate(clsDataDto.getEtaPod(), Constants.DATE_TIME_FORMAT) : null);
    clsDataDto.setLeoDateStr(clsDataDto.getLeoDate() != null ? DateUtils.formatDate(clsDataDto.getLeoDate(), Constants.DATE_TIME_FORMAT) : null);

    return clsDataDto;
  }
  private StringBuilder getContQueryForClsData(ClsDetailsFilterDto filterDto, ApplicationUser loggedInUser) {
    StringBuilder countQuery = new StringBuilder("select count(*) from shipment a, loadslip b, shipment_export c, truck_reporting d " +
        " where a.shipment_id = b.shipment_id " +
        " and a.shipment_id = c.shipment_id(+) " +
        " and a.shipment_id = d.shipment_id " +
        " and b.source_loc = d.reporting_location " +
        " and d.gateout_date is not null " +
        " and b.loadslip_type='FGS_EXP' ");

    setWhereClauseForClsData(countQuery, filterDto, loggedInUser);

    return countQuery;
  }

  private StringBuilder getClsDataQuery(ClsDetailsFilterDto filterDto, ApplicationUser loggedInUser) {
    StringBuilder mainQuery = new StringBuilder("select     distinct     a.shipment_id AS shipmentId, " +
        "                b.loadslip_id AS loadslipId, " +
        "                d.gateout_date AS gateouDate, " +
        "                b.source_loc AS sourceLoc, " +
        "                b.dest_loc AS destLoc, " +
        "                a.container_num AS containerNum, " +
        "                b.sto_so_num AS stosoNum, " +
        "                b.delivery AS delivery, " +
        "                b.sap_invoice AS sapInvoice, " +
        "                b.sap_invoice_date AS sapInvoiceDate, " +
        "                b.custom_inv_number AS customInvNumber, " +
        "                c.pi_no AS piNo, " +
        "                c.customer_name AS customerName , " +
        "                c.pre_inv_no AS preInvNo, " +
        "                c.inco_term AS incoterm, " +
        "                c.pol AS pol, " +
        "                c.pod AS pod, " +
        "                c.billing_party AS billingParty, " +
        "                c.shipping_line AS shippingLine, " +
        "                c.cont_pick_date AS contPickDate, " +
        "                c.stuffing_date AS stuffingDate, " +
        "                c.booking_num AS bookingNum, " +
        "                c.post_inv_no AS postInvNo, " +
        "                c.cha AS cha, " +
        "                c.planned_vessel AS plannedVessel, " +
        "                c.vessel_depart_pol_date AS vesselDepartPolDate, " +
        "                c.shipping_bill AS shippinBill, " +
        "                c.shipping_bill_date AS shippingBillDate, " +
        "                c.gatein_date_cfs AS gateInDate, " +
        "                c.customs_exam_date AS customsExamDate, " +
        "                c.gateout_date_cfs AS gateoutDateCfs, " +
        "                c.gatein_date_port AS gateinDatePort, " +
        "                c.actual_vessel AS actualVessel, " +
        "                c.shipped_onboard_date AS shippedOnboardDate, " +
        "                c.export_remarks AS eportRemark, " +
        "                nvl(c.is_sync_otm,'N') AS isSyncOtm, " +
        "                (case when a.status is null then 'CREATED' else a.status end) as status, " +
        "                a.insert_date AS insertDate, " +
        "                c.COFD as cofd, " +
        "                c.ETA_POD As etaPod, " +
        "                c.FORWARDER as forwarder, " +
        "                c.INV_AMOUNT As invAmount, " +
        "                c.LEO_DATE AS leoDate, " +
        "                c.PAYMENT_TERMS AS paymentTerms  " +
        /*" from shipment a, loadslip b, shipment_export c, truck_reporting d " +
        " where a.shipment_id = b.shipment_id " +
        " and a.shipment_id = c.shipment_id(+) " +
        " and a.shipment_id = d.shipment_id " +
        " and b.source_loc = d.reporting_location " +
        " and d.gateout_date is not null " +
        " and b.loadslip_type='FGS_EXP' " +*/

        " from shipment a, loadslip b,loadslip_detail b1, shipment_export c, truck_reporting d " +
        " where a.shipment_id = b.shipment_id " +
        " and b.loadslip_id = b1.loadslip_id " +
        " and ltrim(b1.invoice_number,'0') = c.sap_invoice(+) " +
        " and a.shipment_id = c.shipment_id(+) " +
        " and a.shipment_id = d.shipment_id " +
        " and b.source_loc = d.reporting_location " +
        " and d.gateout_date is not null " +
        " and b.loadslip_type='FGS_EXP' ");

    setWhereClauseForClsData(mainQuery, filterDto, loggedInUser);
    mainQuery.append(" ORDER BY a.insert_date DESC");

    return mainQuery;
  }

  private void setWhereClauseForClsData(StringBuilder mainQuery, ClsDetailsFilterDto filterDto, ApplicationUser loggedInUser) {
    if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole()) || UserRole.FPL.equals(loggedInUser.getRole())){
      if (!StringUtils.isEmpty(filterDto.getSourceLoc())){
        mainQuery.append(" AND b.source_loc = '"+filterDto.getSourceLoc()+"' ");
      }
    }else{
      mainQuery.append(" AND b.source_loc = '"+loggedInUser.getPlantCode()+"' ");
    }

    if (!StringUtils.isEmpty(filterDto.getShipmentId())){
      mainQuery.append(" AND a.shipment_id = '"+filterDto.getShipmentId()+"' ");
    }

    if (!StringUtils.isEmpty(filterDto.getFromInsertDate()) && !StringUtils.isEmpty(filterDto.getToInsertDate())){
      mainQuery.append(" and a.insert_date >= TO_DATE('" + filterDto.getFromInsertDate() + "','" + PLAN_RECORD_DATE_FORMAT + "')");
      mainQuery.append(" and a.insert_date <= TO_DATE('" + DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(filterDto.getToInsertDate(), PLAN_RECORD_DATE_FORMAT)),
          Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
//      mainQuery.append(" and a.insert_date <= TO_DATE('" + filterDto.getToInsertDate() + "','" + PLAN_RECORD_DATE_FORMAT + "')");
    }

    if (!StringUtils.isEmpty(filterDto.getFromShipmentOnboardDate()) && !StringUtils.isEmpty(filterDto.getToShipmentOnboardDate())){
      mainQuery.append(" and c.shipped_onboard_date >= TO_DATE('" + filterDto.getFromShipmentOnboardDate() + "','" + PLAN_RECORD_DATE_FORMAT + "')");
      mainQuery.append(" and c.shipped_onboard_date < TO_DATE('" + DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(filterDto.getToShipmentOnboardDate(), PLAN_RECORD_DATE_FORMAT)),
          Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
//      mainQuery.append(" and c.shipped_onboard_date <= TO_DATE('" + filterDto.getToShipmentOnboardDate() + "','" + PLAN_RECORD_DATE_FORMAT + "')");
    }

    if (!StringUtils.isEmpty(filterDto.getFromGateOutDate()) && !StringUtils.isEmpty(filterDto.getToGateOutDate())){
      mainQuery.append(" and d.gateout_date >= TO_DATE('" + filterDto.getFromGateOutDate() + "','" + PLAN_RECORD_DATE_FORMAT + "')");
      mainQuery.append(" and d.gateout_date <= TO_DATE('" + DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(filterDto.getToGateOutDate(), PLAN_RECORD_DATE_FORMAT)),
          Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
//      mainQuery.append(" and d.gateout_date <= TO_DATE('" + filterDto.getToGateOutDate() + "','" + PLAN_RECORD_DATE_FORMAT + "')");
    }

    if (!StringUtils.isEmpty(filterDto.getFromSapInvDate()) && !StringUtils.isEmpty(filterDto.getToSapInvDate())){
      mainQuery.append(" and  b.sap_invoice_date >= TO_DATE('" + filterDto.getFromSapInvDate() + "','" + PLAN_RECORD_DATE_FORMAT + "')");
      mainQuery.append(" and  b.sap_invoice_date < TO_DATE('" + DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(filterDto.getToSapInvDate(), PLAN_RECORD_DATE_FORMAT)),
          Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
//      mainQuery.append(" and d.gateout_date <= TO_DATE('" + filterDto.getToSapInvDate() + "','" + PLAN_RECORD_DATE_FORMAT + "')");
    }
  }


}
