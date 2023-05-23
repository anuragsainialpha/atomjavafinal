package com.api.apollo.atom.service.impl;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.LocationType;
import com.api.apollo.atom.constant.UserRole;
import com.api.apollo.atom.dto.ops.*;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.master.MTLocation;
import com.api.apollo.atom.repository.ops.ShipmentStopRepository;
import com.api.apollo.atom.service.DashboardService;
import com.api.apollo.atom.util.DateUtils;
import com.api.apollo.atom.util.Utility;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.api.apollo.atom.constant.Constants.PLAN_RECORD_DATE_FORMAT;

@Service
public class DashboardServiceImpl implements DashboardService {

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private ShipmentStopRepository shipmentStopRepository;

  @Override
  public List<String> getShipmentSourceList(){
    return shipmentStopRepository.findDistinctSourceLoc();
  }

  @Override
  public PlanAgeing getPlanAgeing(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation) {
    StringBuilder query1 = getPlanAgeingQueryWithWhereClause(dashboardFilterDto, applicationUser, false, optionalMTLocation);
    StringBuilder query2 = getPlanAgeingQueryWithWhereClause(dashboardFilterDto, applicationUser, true, optionalMTLocation);

    StringBuilder mainQuery = new StringBuilder("With query1 AS ("+query1+"), query2 AS ("+query2+") " +
        "SELECT query1.lessThan2 as lessThan2, query1.lessThan2Count as lessThan2Count, query1.lessThan7 as lessThan7, query1.lessThan7Count as lessThan7Count, query1.greaterThan7 as greaterThan7, query1.greaterThan7Count as greaterThan7Count, " +
        "(CASE WHEN query2.lessThan2 is not null THEN query2.lessThan2 ELSE 0 END) as cummLessThan2, " +
        " (CASE WHEN query2.lessThan2Count is not null THEN query2.lessThan2Count ELSE 0 END) as cummLessThan2Count, " +
        " (CASE WHEN query2.lessThan7 is not null THEN query2.lessThan7 ELSE 0 END) as cummLessThan7, " +
        " (CASE WHEN query2.lessThan7Count is not null THEN query2.lessThan7Count ELSE 0 END) as cummLessThan7Count, " +
        " (CASE WHEN query2.greaterThan7 is not null THEN query2.greaterThan7 ELSE 0 END) as cummGreaterThan7, " +
        " (CASE WHEN query2.greaterThan7Count is not null THEN query2.greaterThan7Count ELSE 0 END) as cummGreaterThan7Count " +
        "FROM query1, query2 ");

    Query query = entityManager.unwrap(Session.class).createNativeQuery(mainQuery.toString())
        .addScalar("lessThan2", StandardBasicTypes.DOUBLE).addScalar("lessThan2Count", StandardBasicTypes.DOUBLE).addScalar("lessThan7", StandardBasicTypes.DOUBLE)
        .addScalar("lessThan7Count", StandardBasicTypes.DOUBLE).addScalar("greaterThan7", StandardBasicTypes.DOUBLE).addScalar("greaterThan7Count", StandardBasicTypes.DOUBLE)
        .addScalar("cummLessThan2", StandardBasicTypes.DOUBLE).addScalar("cummLessThan2Count", StandardBasicTypes.DOUBLE).addScalar("cummLessThan7", StandardBasicTypes.DOUBLE)
        .addScalar("cummLessThan7Count", StandardBasicTypes.DOUBLE).addScalar("cummGreaterThan7", StandardBasicTypes.DOUBLE).addScalar("cummGreaterThan7Count", StandardBasicTypes.DOUBLE);

//    Object[] planAgeingObj = (Object[]) query.getResultList().get(0);
    List<PlanAgeing> planAgeingList = ((NativeQuery) query)
        .setResultTransformer(new AliasToBeanResultTransformer(PlanAgeing.class)).getResultList();

//    return new DashboardDataDto().new PlanAgeing(planAgeingObj[0] != null ? ((BigDecimal)planAgeingObj[0]).doubleValue() : 0,
//        planAgeingObj[1] != null ? ((BigDecimal)planAgeingObj[1]).doubleValue() : 0,
//        planAgeingObj[2] != null ? ((BigDecimal)planAgeingObj[2]).doubleValue() : 0,
//        planAgeingObj[3] != null ? ((BigDecimal)planAgeingObj[3]).doubleValue() : 0,
//        planAgeingObj[4] != null ? ((BigDecimal)planAgeingObj[4]).doubleValue() : 0,
//        planAgeingObj[5] != null ? ((BigDecimal)planAgeingObj[5]).doubleValue() : 0);

    return (planAgeingList != null && !planAgeingList.isEmpty()) ? planAgeingList.get(0) : null;
  }

  private StringBuilder getPlanAgeingQueryWithWhereClause(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, boolean isCummData, Optional<MTLocation> optionalMTLocation) {
    StringBuilder mainQuery = new StringBuilder(" select SUM(CASE WHEN (((select SYSDATE from dual) - (dp.insert_date)) >= "+dashboardFilterDto.getFirstLineStart()+" AND ((select SYSDATE from dual) - (dp.insert_date)) <= "+(dashboardFilterDto.getFirstLineEnd() + 1)+")  THEN (dp.tot_avail_qty + dp.reserved_qty + dp.loaded_qty) ELSE 0 END) AS lessThan2, " +
        "        COUNT(CASE WHEN (((select SYSDATE from dual) - (dp.insert_date)) >= "+dashboardFilterDto.getFirstLineStart()+" AND ((select SYSDATE from dual) - (dp.insert_date)) <= "+(dashboardFilterDto.getFirstLineEnd() + 1)+" )  THEN dp.dispatch_plan_id END) as lessThan2Count, " +
        "        SUM(CASE WHEN ((select SYSDATE from dual) - (dp.insert_date)) >= "+dashboardFilterDto.getSecondLineStart()+" AND ((select SYSDATE from dual) - (dp.insert_date)) <= "+(dashboardFilterDto.getSecondLineEnd() + 1)+"  THEN (dp.tot_avail_qty + dp.reserved_qty + dp.loaded_qty) ELSE 0 END) as lessThan7, " +
        "        COUNT (CASE WHEN ((select SYSDATE from dual) - (dp.insert_date)) >= "+dashboardFilterDto.getSecondLineStart()+" AND ((select SYSDATE from dual) - (dp.insert_date)) <= "+(dashboardFilterDto.getSecondLineEnd() + 1)+"  THEN dp.dispatch_plan_id END) as lessThan7Count," +
        "        Sum(CASE WHEN ((select SYSDATE from dual) - (dp.insert_date)) >= "+dashboardFilterDto.getThirdLine()+"  THEN  (dp.tot_avail_qty + dp.reserved_qty + dp.loaded_qty) ELSE 0 END) as greaterThan7, " +
        "        COUNT (CASE WHEN ((select SYSDATE from dual) - (dp.insert_date)) >= "+dashboardFilterDto.getThirdLine()+"  THEN dp.dispatch_plan_id END) as greaterThan7Count" +
        "    from DISPATCH_PLAN dp where dp.dispatch_plan_id IS NOT NULL ");
  /*  if (UserRole.getDPREPAccessRoles().contains(applicationUser.getRole()) ){
      if (!StringUtils.isEmpty(dashboardFilterDto.getSourceLoc())){
        mainQuery.append(" AND dp.source_loc = '"+dashboardFilterDto.getSourceLoc()+"' ");
      }
    } else {
      mainQuery.append(" AND dp.source_loc = '" + applicationUser.getPlantCode() + "' ");
    }*/
    if (this.isExtWarehouse(optionalMTLocation)){
      if (!StringUtils.isEmpty(dashboardFilterDto.getSourceLoc())) {
        mainQuery.append(" and (dp.source_loc = '" + dashboardFilterDto.getSourceLoc() + "' AND " +
            "(dp.source_loc = '" + applicationUser.getPlantCode() + "' OR  dp.source_loc = '" + optionalMTLocation.get().getLinkedPlant() + "'))");
      } else {
        mainQuery.append(" AND dp.source_loc IN ('" + applicationUser.getPlantCode() +"','"+optionalMTLocation.get().getLinkedPlant()+"') ");
      }
    }else {
      if (UserRole.getDPREPAccessRoles().contains(applicationUser.getRole())) {
        if (!StringUtils.isEmpty(dashboardFilterDto.getSourceLoc())) {
          mainQuery.append(" AND dp.source_loc = '" + dashboardFilterDto.getSourceLoc() + "' ");
        }
      } else {
        mainQuery.append(" AND dp.source_loc = '" + applicationUser.getPlantCode() + "' ");
      }
    }

    if (!StringUtils.isEmpty(dashboardFilterDto.getDestination())){
      mainQuery.append(" AND dp.dest_loc = '" + dashboardFilterDto.getDestination() + "' ");
    }
    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      mainQuery.append(" AND dp.item_category IN ("+ Utility.join(dashboardFilterDto.getMaterialGroup())+") ");
    }
    if (isCummData) {
      Date today = new Date();
      mainQuery.append("and  dp.dispatch_date   >=TO_DATE('" + DateUtils.getStartOfTheMonth(today) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      mainQuery.append(" and dp.dispatch_date < TO_DATE('" +
          DateUtils.formatDate(DateUtils.setNextDayStart(today), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
    } else {
      if (!StringUtils.isEmpty(dashboardFilterDto.getFromDispatchDateAgeing()) && !StringUtils.isEmpty(dashboardFilterDto.getToDispatchDateAgeing())){
        mainQuery.append("and  dp.dispatch_date   >=TO_DATE('" + dashboardFilterDto.getFromDispatchDateAgeing() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
        mainQuery.append(" and dp.dispatch_date < TO_DATE('" +
            DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(dashboardFilterDto.getToDispatchDateAgeing(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      }
    }
    if (dashboardFilterDto.getMarketSegments() != null && !dashboardFilterDto.getMarketSegments().isEmpty()){
      mainQuery.append(" AND dp.market_segment IN ("+Utility.join(dashboardFilterDto.getMarketSegments())+") ");
    }
    if (!StringUtils.isEmpty(dashboardFilterDto.getPlanStatus())){
      mainQuery.append(" AND dp.status = '"+dashboardFilterDto.getPlanStatus()+"' ");
    }/*else {
      mainQuery.append(" AND dp.status = 'OPEN' ");
    }*/
    return mainQuery;
  }

  @Override
  public List<ShipmentStatusDto> getShipmentStatusData(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation) {

    StringBuilder query1 = getSubQueryForShipmentStatusWithN(dashboardFilterDto, applicationUser, false, optionalMTLocation);

    StringBuilder query2 = getSubQueryForShipmentStatusWithN(dashboardFilterDto, applicationUser, true, optionalMTLocation);

    StringBuilder mainQuery = new StringBuilder("select * from ( WITH " +
        "QUERY1 as ("+query1+"), " +
        "QUERY2 as ("+query2+") " +
        "select QUERY1.shipmentWithN as shipmentWithN ,QUERY1.reportLoc as shipmentWithNReportLoc,QUERY1.shipmentWithY as shipmentWithY, QUERY1.total as total,  " +
        " (CASE WHEN QUERY2.shipmentWithN is not null THEN QUERY2.shipmentWithN ELSE 0 END) as cummShipmentWithN, " +
        " (CASE WHEN QUERY2.shipmentWithY is not null THEN QUERY2.shipmentWithY ELSE 0 END) as cummShipmentWithY, " +
        " (CASE WHEN QUERY2.total is not null THEN QUERY2.total ELSE 0 END) as cummTotal  " +
        " from QUERY1 LEFT JOIN QUERY2 ON QUERY1.reportLoc = QUERY2.reportLoc )ab  ORDER BY ab.shipmentWithNReportLoc ASC ");

    Query query = entityManager.unwrap(Session.class).createNativeQuery(mainQuery.toString()).addScalar("shipmentWithNReportLoc", StandardBasicTypes.STRING)
        .addScalar("shipmentWithN", StandardBasicTypes.DOUBLE).addScalar("shipmentWithY", StandardBasicTypes.DOUBLE).addScalar("total", StandardBasicTypes.DOUBLE)
        .addScalar("cummShipmentWithN", StandardBasicTypes.DOUBLE).addScalar("cummShipmentWithY", StandardBasicTypes.DOUBLE).addScalar("cummTotal", StandardBasicTypes.DOUBLE);

    List<ShipmentStatusDto> shipmentStatusList = ((NativeQuery) query)
        .setResultTransformer(new AliasToBeanResultTransformer(ShipmentStatusDto.class)).getResultList();

    if (dashboardFilterDto.getSourceList() != null && !dashboardFilterDto.getSourceList().isEmpty()){
      shipmentStatusList = setEmptyValuesToNullShipmentStatus(shipmentStatusList, dashboardFilterDto.getSourceList()).parallelStream().
          sorted(Comparator.comparing(shipment -> shipment.getShipmentWithNReportLoc())).collect(Collectors.toList());
    }
    return shipmentStatusList;

  }

  private StringBuilder getSubQueryForShipmentStatusWithN(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, boolean isCummData, Optional<MTLocation> optionalMTLocation) {
    StringBuilder subQuery = new StringBuilder("select COUNT(CASE WHEN abc.flag = 'N' THEN abc.shipmentId END) as shipmentWithN , COUNT(CASE WHEN abc.flag = 'Y' THEN abc.shipmentId END) as shipmentWithY, abc.reportLoc, COUNT(abc.shipmentId) as total  " +
        " from (select sh.shipment_id as shipmentId, sh.frt_avail_flag as flag ,tr.reporting_location as reportLoc ,ROW_NUMBER() OVER (PARTITION BY tr.shipment_id ORDER BY tr.insert_date ASC) as rn " +
        " from truck_reporting tr  " +
        " LEFT JOIN shipment sh on tr.shipment_id = sh.shipment_id " +
        " where (sh.status is NULL OR sh.status != 'CANCELLED')  ");
    setWhereClauseForShipmentStatus(subQuery, dashboardFilterDto, applicationUser, isCummData, optionalMTLocation);
    subQuery.append(" )abc where abc.rn = 1 GROUP BY abc.reportLoc ");

    return subQuery;
  }

  private void setWhereClauseForShipmentStatus(StringBuilder subQuery, DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, boolean isCummData, Optional<MTLocation> optionalMTLocation) {
    if (isCummData){
      Date today = new Date();
//      For Cumm date filter will be from start of the month to current date
      subQuery.append(" and sh.insert_date   >=TO_DATE('" + DateUtils.getStartOfTheMonth(today) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      subQuery.append(" and sh.insert_date < TO_DATE('" +
          DateUtils.formatDate(DateUtils.setNextDayStart(today), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
    }else {
      if (!StringUtils.isEmpty(dashboardFilterDto.getFromDispatchDate()) && !StringUtils.isEmpty(dashboardFilterDto.getToDispatchDate())){
        subQuery.append(" and sh.insert_date   >=TO_DATE('" + dashboardFilterDto.getFromDispatchDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
        subQuery.append(" and sh.insert_date < TO_DATE('" +
            DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(dashboardFilterDto.getToDispatchDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      }
    }

    if (Constants.isDPREPRole(applicationUser.getRole())){
      if (dashboardFilterDto.getSourceList() != null && !dashboardFilterDto.getSourceList().isEmpty()){
        subQuery.append(" AND tr.reporting_location IN ("+Utility.join(dashboardFilterDto.getSourceList())+") ");
      }
    }else {
      subQuery.append(" AND tr.reporting_location IN ('"+applicationUser.getPlantCode()+"') ");
    }
    if (!StringUtils.isEmpty(dashboardFilterDto.getDestination())){
      subQuery.append(" AND tr.dest_loc = '"+dashboardFilterDto.getDestination()+"' ");
    }
   /* if (!StringUtils.isEmpty(dashboardFilterDto.getTransporter())){
      subQuery.append(" AND sh.servprov = '"+dashboardFilterDto.getTransporter()+"' ");
    }*/
   if (dashboardFilterDto.getTransporterList() != null && !dashboardFilterDto.getTransporterList().isEmpty()){
     subQuery.append(" AND sh.servprov IN ("+Utility.join(dashboardFilterDto.getTransporterList())+") ");
   }
  }


  @Transactional
  private List<ShipmentStatusDto> setEmptyValuesToNullShipmentStatus(List<ShipmentStatusDto> shipmentStatuses, List<String> sourceList) {
    List<ShipmentStatusDto> shipmentStatusArrayList = new ArrayList<>();
//    When No records fount
    if (shipmentStatuses == null ||shipmentStatuses.isEmpty()){
      sourceList.parallelStream().forEach(source -> {
        ShipmentStatusDto shipmentStatus = new ShipmentStatusDto();
        shipmentStatus.setShipmentWithNReportLoc(source);
        shipmentStatusArrayList.add(shipmentStatus);
      });
      return shipmentStatusArrayList;
    }

    sourceList.parallelStream().forEach(source -> {
      Optional<ShipmentStatusDto> optionalShipmentStatus = shipmentStatuses.parallelStream().filter(shipment -> shipment.getShipmentWithNReportLoc().equals(source)).findFirst();
      if (optionalShipmentStatus.isPresent()) {
        shipmentStatusArrayList.add(optionalShipmentStatus.get());
      } else {
        ShipmentStatusDto shipmentStatus = new ShipmentStatusDto();
        shipmentStatus.setShipmentWithNReportLoc(source);
        shipmentStatusArrayList.add(shipmentStatus);
      }
    });
    return shipmentStatusArrayList;
  }

  @Override
  public OpenPlanDataDto getOpenPlansData(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation) {
    StringBuilder query1 = null;
    if ( optionalMTLocation != null && optionalMTLocation.isPresent() && LocationType.RDC.name().equals(optionalMTLocation.get().getType())){
      //      For RDC we will consider quantity while calculating the truck Count
      query1 = new StringBuilder("select SUM(dp.quantity) availQtySum , SUM(dp.tte * dp.quantity) tteSum, COUNT(*) as total, " +
          "SUM(dp.approved_qty) as totalApprovedQty, SUM(dp.reserved_qty + dp.loaded_qty) as totalReserved " +
          " from dispatch_plan dp, mt_item item " +
          " where item.item_id = dp.item_id  AND item.item_classification ='TYRE' ");
    }else {
//      For FGS we will consider available quantity while calculating the truck Count
      query1 = new StringBuilder("select SUM(dp.avail_qty) availQtySum , SUM(dp.tte * dp.avail_qty) tteSum, COUNT(*) as total, " +
          " SUM(dp.approved_qty) as totalApprovedQty, SUM(dp.reserved_qty + dp.loaded_qty) as totalReserved   " +
          " from dispatch_plan dp, mt_item item " +
          " where item.item_id = dp.item_id  AND item.item_classification ='TYRE' ");
    }

    if (UserRole.getDPREPAccessRoles().contains(applicationUser.getRole()) ){
      if (!StringUtils.isEmpty(dashboardFilterDto.getSourceLoc())){
        query1.append(" AND dp.source_loc = '"+dashboardFilterDto.getSourceLoc()+"' ");
      }
    }else {
      query1.append(" AND dp.source_loc = '"+applicationUser.getPlantCode()+"' ");
    }

    if (!StringUtils.isEmpty(dashboardFilterDto.getDestination())){
      query1.append(" AND dp.dest_loc = '"+dashboardFilterDto.getDestination()+"' ");
    }
    if (!StringUtils.isEmpty(dashboardFilterDto.getFromDispatchDate()) && !StringUtils.isEmpty(dashboardFilterDto.getToDispatchDate())){
      query1.append("and  dp.dispatch_date   >=TO_DATE('" + dashboardFilterDto.getFromDispatchDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      query1.append(" and dp.dispatch_date < TO_DATE('" +
          DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(dashboardFilterDto.getToDispatchDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
    }
    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      query1.append(" AND dp.item_category IN ("+Utility.join(dashboardFilterDto.getMaterialGroup())+") ");
    }
    query1.append(" AND dp.status = 'OPEN' ");


    StringBuilder query2 = new StringBuilder("select TTE_CAPACITY tteCapacity from mt_truck_type where truck_type='STANDARD_FTL' AND variant1 is NULL");

    StringBuilder mainQuery = new StringBuilder("WITH query1 as ("+query1.toString()+"), " +
        "query2 as ("+query2.toString()+") " +
        "select availQtySum,tteSum,tteCapacity, ((tteSum)/tteCapacity) as truckCount, total,totalApprovedQty,totalReserved from query1, query2");
    Query query = entityManager.createNativeQuery(mainQuery.toString());

    Object[] openPlanObject = (Object[]) query.getResultList().get(0);

    return new OpenPlanDataDto(openPlanObject[0] != null? ((BigDecimal)openPlanObject[0]).doubleValue() : 0,
        openPlanObject[1] != null ? ((BigDecimal)openPlanObject[1]).doubleValue() : 0,
        openPlanObject[3]!= null ? ((BigDecimal)openPlanObject[3]).doubleValue() : 0,
        openPlanObject[4]!= null ? ((BigDecimal)openPlanObject[4]).intValue() : 0,
        openPlanObject[5]!= null ? ((BigDecimal)openPlanObject[5]).doubleValue() : 0,
        openPlanObject[6]!= null ? ((BigDecimal)openPlanObject[6]).doubleValue() : 0);
  }


  @Override
  public List<IndentStatusDto> getIndentStatusWithCumm(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, boolean isPlacementStatus, Optional<MTLocation> optionalMTLocation){
    StringBuilder query1 = getQuery1ForIndentStatus(dashboardFilterDto, applicationUser, false, optionalMTLocation);

    StringBuilder query2 = getQuery1ForIndentStatus(dashboardFilterDto, applicationUser, true, optionalMTLocation);

    StringBuilder mainQuery = new StringBuilder("WITH " +
        "query1 AS ("+query1+"), " +
        "query2 AS ("+query2+") " +
        "SELECT query1.totalNetIndented as totalNetIndented, query1.totalReported as totalReported, query1.totalRejected as totalRejected, query1.totalNetBalance as totalNetBalance, query1.total as total, " );
//   When searched with categpry we have to group the result
    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      mainQuery.append(" query1.itemCategory as itemCategory, ");
    }
    if (isPlacementStatus){
      mainQuery.append(" query1.totalNetPlaced as totalNetPlaced, ((query1.totalNetPlaced / query1.totalNetIndented) * 100) as placementPercentage, ");
    }

    mainQuery.append("(CASE WHEN query2.totalNetIndented is not null THEN query2.totalNetIndented ELSE 0 END) as cummTotalNetIndented, " +
        "(CASE WHEN query2.totalReported is not null THEN query2.totalReported ELSE 0 END) as cummTotalReported, " +
        "(CASE WHEN query2.totalRejected is not null THEN query2.totalRejected ELSE 0 END) as cummTotalRejected, " +
        "(CASE WHEN query2.totalNetBalance is not null THEN query2.totalNetBalance ELSE 0 END) as cummTotalNetBalance, " +
        "(CASE WHEN query2.total is not null THEN query2.total ELSE 0 END) as cummTotal ");

//   When searched with categpry we have to group the result
    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      mainQuery.append(", (CASE WHEN query2.itemCategory is not null THEN query2.itemCategory ELSE null END) as cummItemCategory  ");
    }
    if (isPlacementStatus){
      mainQuery.append(", (CASE WHEN query2.totalNetIndented is not null THEN query2.totalNetIndented ELSE 0 END) as cummTotalNetPlaced, " +
          "(CASE WHEN query2.totalNetPlaced is not null AND query2.totalNetIndented is not null THEN ((query2.totalNetPlaced / query2.totalNetIndented) * 100) ELSE 0 END) as cummPlacementPercentage ");
    }
    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      mainQuery.append(" FROM query1 LEFT JOIN query2 ON query2.itemCategory = query1.itemCategory ");
    }else {
      mainQuery.append(" FROM query1, query2 ");
    }
    //   When searched with categpry we have to group the result
//    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
//      mainQuery.append(" WHERE query2.itemCategory = query1.itemCategory ");
//    }

    Query query = getNativeQueryWithScalar(mainQuery, dashboardFilterDto, isPlacementStatus);


    List<IndentStatusDto> indentStatusDtos = ((NativeQuery) query)
        .setResultTransformer(new AliasToBeanResultTransformer(IndentStatusDto.class)).getResultList();

//    Setting Null values when no records fount for a perticular category
    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      return setEmptyValuesToNull(indentStatusDtos, dashboardFilterDto.getMaterialGroup());
    }

    return indentStatusDtos;
  }

  @Transactional
  private List<IndentStatusDto> setEmptyValuesToNull(List<IndentStatusDto> indentStatusDtos, List<String> materialGroup) {
    List<IndentStatusDto> indentStatusDtoList = new ArrayList<>();
//    When No records fount
    if (indentStatusDtos == null ||indentStatusDtos.isEmpty()){
      materialGroup.parallelStream().forEach(catergory -> {
        IndentStatusDto indentStatusDto = new IndentStatusDto();
        indentStatusDto.setCummItemCategory(catergory);
        indentStatusDtoList.add(indentStatusDto);
      });
      return indentStatusDtoList;
    }

    materialGroup.parallelStream().forEach(category -> {
      Optional<IndentStatusDto> optionalIndentStatusDto = indentStatusDtos.parallelStream().filter(indent -> indent.getItemCategory().equals(category)).findFirst();
      if (optionalIndentStatusDto != null && optionalIndentStatusDto.isPresent()) {
        indentStatusDtoList.add(optionalIndentStatusDto.get());
      } else {
        IndentStatusDto indentStatusDto = new IndentStatusDto();
        indentStatusDto.setItemCategory(category);
        indentStatusDtoList.add(indentStatusDto);
      }
    });
    return indentStatusDtoList;
  }

  private Query getNativeQueryWithScalar(StringBuilder mainQuery, DashboardFilterDto dashboardFilterDto, boolean isPlacementStaus) {
    Query query = null;
    if (!isPlacementStaus){
//      Indent Status
      if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
        //   When searched with categpry we will get two extra fields itemCategory & cummItemCategory
        query = entityManager.unwrap(Session.class).createNativeQuery(mainQuery.toString())
            .addScalar("totalNetIndented", StandardBasicTypes.DOUBLE).addScalar("totalReported", StandardBasicTypes.DOUBLE).addScalar("totalRejected", StandardBasicTypes.DOUBLE)
            .addScalar("totalNetBalance", StandardBasicTypes.DOUBLE).addScalar("total", StandardBasicTypes.INTEGER).addScalar("cummTotalNetIndented", StandardBasicTypes.DOUBLE)
            .addScalar("cummTotalReported", StandardBasicTypes.DOUBLE).addScalar("cummTotalRejected", StandardBasicTypes.DOUBLE).addScalar("cummTotalNetBalance", StandardBasicTypes.DOUBLE)
            .addScalar("cummTotal", StandardBasicTypes.INTEGER).addScalar("itemCategory", StandardBasicTypes.STRING).addScalar("cummItemCategory", StandardBasicTypes.STRING);
      }else {
        query = entityManager.unwrap(Session.class).createNativeQuery(mainQuery.toString())
            .addScalar("totalNetIndented", StandardBasicTypes.DOUBLE).addScalar("totalReported", StandardBasicTypes.DOUBLE).addScalar("totalRejected", StandardBasicTypes.DOUBLE)
            .addScalar("totalNetBalance", StandardBasicTypes.DOUBLE).addScalar("total", StandardBasicTypes.INTEGER).addScalar("cummTotalNetIndented", StandardBasicTypes.DOUBLE)
            .addScalar("cummTotalReported", StandardBasicTypes.DOUBLE).addScalar("cummTotalRejected", StandardBasicTypes.DOUBLE).addScalar("cummTotalNetBalance", StandardBasicTypes.DOUBLE)
            .addScalar("cummTotal", StandardBasicTypes.INTEGER);
      }
    }else {
//       PlacementStatus
      if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
        query = entityManager.unwrap(Session.class).createNativeQuery(mainQuery.toString())
            .addScalar("totalNetIndented", StandardBasicTypes.DOUBLE).addScalar("totalReported", StandardBasicTypes.DOUBLE).addScalar("totalRejected", StandardBasicTypes.DOUBLE)
            .addScalar("totalNetBalance", StandardBasicTypes.DOUBLE).addScalar("total", StandardBasicTypes.INTEGER).addScalar("cummTotalNetIndented", StandardBasicTypes.DOUBLE)
            .addScalar("cummTotalReported", StandardBasicTypes.DOUBLE).addScalar("cummTotalRejected", StandardBasicTypes.DOUBLE).addScalar("cummTotalNetBalance", StandardBasicTypes.DOUBLE)
            .addScalar("cummTotal", StandardBasicTypes.INTEGER).addScalar("itemCategory", StandardBasicTypes.STRING).addScalar("cummItemCategory", StandardBasicTypes.STRING)
            .addScalar("totalNetPlaced", StandardBasicTypes.DOUBLE).addScalar("placementPercentage", StandardBasicTypes.DOUBLE).addScalar("cummTotalNetPlaced", StandardBasicTypes.DOUBLE).addScalar("cummPlacementPercentage", StandardBasicTypes.DOUBLE);
      }else {
        query = entityManager.unwrap(Session.class).createNativeQuery(mainQuery.toString())
            .addScalar("totalNetIndented", StandardBasicTypes.DOUBLE).addScalar("totalReported", StandardBasicTypes.DOUBLE).addScalar("totalRejected", StandardBasicTypes.DOUBLE)
            .addScalar("totalNetBalance", StandardBasicTypes.DOUBLE).addScalar("total", StandardBasicTypes.INTEGER).addScalar("cummTotalNetIndented", StandardBasicTypes.DOUBLE)
            .addScalar("cummTotalReported", StandardBasicTypes.DOUBLE).addScalar("cummTotalRejected", StandardBasicTypes.DOUBLE).addScalar("cummTotalNetBalance", StandardBasicTypes.DOUBLE)
            .addScalar("cummTotal", StandardBasicTypes.INTEGER).addScalar("totalNetPlaced", StandardBasicTypes.DOUBLE).addScalar("placementPercentage", StandardBasicTypes.DOUBLE)
            .addScalar("cummTotalNetPlaced", StandardBasicTypes.DOUBLE).addScalar("cummPlacementPercentage", StandardBasicTypes.DOUBLE);
      }
    }
    return query;
  }

  private StringBuilder getQuery1ForIndentStatus(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, boolean isQueryForCumm, Optional<MTLocation> optionalMTLocation) {
    StringBuilder query1 = new StringBuilder("SELECT SUM(i.net_requested) as totalNetIndented, " +
        "SUM(i.reported) as totalReported, SUM(i.rejected) as totalRejected, SUM(i.net_balance) as totalNetBalance, " +
        "count(*) as total, SUM(i.net_Placed) as totalNetPlaced ");
    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      query1.append(", i.item_category as itemCategory ");
    }

    query1.append(" FROM indent_summary i WHERE i.indent_id IS NOT NULL ");
    setWhereClauseForIndentStatus(query1,dashboardFilterDto, applicationUser, isQueryForCumm, optionalMTLocation);
    query1.append(" AND i.status != 'CANCELLED'");

    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      query1.append(" GROUP BY i.item_category ");
    }

    return query1;
  }

  private void setWhereClauseForIndentStatus(StringBuilder query1, DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, boolean isQueryForCumm, Optional<MTLocation> optionalMTLocation) {
    if (isQueryForCumm){
      Date today = new Date();
//      For Cumm date filter will be from start of the month to current date
      query1.append("and  i.dispatch_date   >=TO_DATE('" + DateUtils.getStartOfTheMonth(today) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      query1.append(" and i.dispatch_date < TO_DATE('" +
          DateUtils.formatDate(DateUtils.setNextDayStart(today), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
    }else {
      if (!StringUtils.isEmpty(dashboardFilterDto.getFromDispatchDate()) && !StringUtils.isEmpty(dashboardFilterDto.getToDispatchDate())){
        query1.append("and  i.dispatch_date   >=TO_DATE('" + dashboardFilterDto.getFromDispatchDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
        query1.append(" and i.dispatch_date < TO_DATE('" +
            DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(dashboardFilterDto.getToDispatchDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      }
    }
    if (UserRole.getDPREPAccessRoles().contains(applicationUser.getRole()) ){
      if (!StringUtils.isEmpty(dashboardFilterDto.getSourceLoc())){
        query1.append(" AND i.source_loc = '"+dashboardFilterDto.getSourceLoc()+"' ");
      }
    }else {
      query1.append(" AND i.source_loc = '"+applicationUser.getPlantCode()+"' ");
    }

    if (!StringUtils.isEmpty(dashboardFilterDto.getDestination())){
      query1.append(" AND i.dest_loc = '"+dashboardFilterDto.getDestination()+"' ");
    }
    /*if (!StringUtils.isEmpty(dashboardFilterDto.getTransporter())){
      query1.append(" AND i.servprov = '"+dashboardFilterDto.getTransporter()+"' ");
    }*/
    if (dashboardFilterDto.getTransporterList() != null && !dashboardFilterDto.getTransporterList().isEmpty()){
      query1.append(" AND i.servprov IN ("+Utility.join(dashboardFilterDto.getTransporterList())+") ");
    }
    if (dashboardFilterDto.getTruckTypes() != null && !dashboardFilterDto.getTruckTypes().isEmpty()){
      query1.append(" AND i.truck_type IN ("+Utility.join(dashboardFilterDto.getTruckTypes())+") ");
    }
    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      query1.append("AND i.item_category IN ("+Utility.join(dashboardFilterDto.getMaterialGroup())+") ");
    }
  }

  @Override
  public PlanAgeing getDelayedDaysData(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation) {
    StringBuilder query1 = getQuerysForDeleayedDays(dashboardFilterDto, applicationUser, false, optionalMTLocation);

    StringBuilder query2 = getQuerysForDeleayedDays(dashboardFilterDto, applicationUser, true, optionalMTLocation);

    StringBuilder mainQuery = new StringBuilder("WITH query1 AS ("+query1+"), query2 AS ("+query2+") " +
        "SELECT query1.lessThan2 as lessThan2, query1.lessThan7 as lessThan7, query1.greaterThan7 as greaterThan7, " +
        " (CASE WHEN query2.lessThan2 is not null THEN query2.lessThan2 ELSE 0 END) as cummLessThan2, " +
        " (CASE WHEN query2.lessThan7 is not null THEN query2.lessThan7 ELSE 0 END) as cummLessThan7, " +
        " (CASE WHEN query2.greaterThan7 is not null THEN query2.greaterThan7 ELSE 0 END) as cummGreaterThan7 " +
        " FROM query1, query2 ") ;

    String sqlString;
    Query query = entityManager.unwrap(Session.class).createNativeQuery(mainQuery.toString())
        .addScalar("lessThan2", StandardBasicTypes.DOUBLE).addScalar("lessThan7", StandardBasicTypes.DOUBLE).addScalar("greaterThan7", StandardBasicTypes.DOUBLE)
        .addScalar("cummLessThan2", StandardBasicTypes.DOUBLE).addScalar("cummLessThan7", StandardBasicTypes.DOUBLE).addScalar("cummGreaterThan7", StandardBasicTypes.DOUBLE);

    List<PlanAgeing> planAgeingList =  ((NativeQuery) query)
        .setResultTransformer(new AliasToBeanResultTransformer(PlanAgeing.class)).getResultList();
    if (planAgeingList != null && !planAgeingList.isEmpty()){
      return planAgeingList.get(0);
    }
    return null;
  }

  @Override
  public List<PlanUploadDto> getPlanUploadData(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation) {
    StringBuilder query1 = getQuery1ForPlanUpload(dashboardFilterDto, applicationUser, false, optionalMTLocation);

    StringBuilder query2 = getQuery1ForPlanUpload(dashboardFilterDto, applicationUser, true, optionalMTLocation);

    StringBuilder mainQuery = new StringBuilder("WITH query1 as ("+query1+"), query2 as ("+query2+") " +
        "SELECT query1.plPlans as uploadedBy3PL, query1.not3PlPlans as uploadedByOthers, query1.total as total, ((query1.plPlans/query1.total) * 100) as percentage3PL," +
        " ((query1.not3PlPlans/query1.total) * 100) as percentageOthers, query1.sourceLoc as sourceLoc, " +
        " (CASE WHEN query2.plPlans is not null THEN query2.plPlans ELSE 0 END) as cummUploadedBy3PL, " +
        " (CASE WHEN query2.not3PlPlans is not null THEN query2.not3PlPlans ELSE 0 END) as cummUploadedByOthers, " +
        " (CASE WHEN query2.total is not null THEN query2.total ELSE 0 END) as cummTotal, " +
        " (CASE WHEN query2.plPlans is not null AND query2.total IS NOT NULL THEN query2.plPlans ELSE 0 END) as cummPercentage3PL, " +
        " (CASE WHEN query2.not3PlPlans is not null AND query2.total IS NOT NULL THEN ((query2.not3PlPlans/query2.total) * 100) ELSE 0 END) as cummPercentageOthers " +
        " FROM query1 LEFT JOIN query2  ON query1.sourceLoc = query2.sourceLoc");

    Query query = entityManager.unwrap(Session.class).createNativeQuery(mainQuery.toString())
        .addScalar("uploadedBy3PL", StandardBasicTypes.DOUBLE).addScalar("uploadedByOthers", StandardBasicTypes.DOUBLE).addScalar("total", StandardBasicTypes.DOUBLE)
        .addScalar("percentage3PL", StandardBasicTypes.DOUBLE).addScalar("percentageOthers", StandardBasicTypes.DOUBLE).addScalar("sourceLoc", StandardBasicTypes.STRING)
        .addScalar("cummUploadedBy3PL", StandardBasicTypes.DOUBLE).addScalar("cummUploadedByOthers", StandardBasicTypes.DOUBLE).addScalar("cummTotal", StandardBasicTypes.DOUBLE)
        .addScalar("cummPercentage3PL", StandardBasicTypes.DOUBLE).addScalar("cummPercentageOthers", StandardBasicTypes.DOUBLE);

    List<PlanUploadDto> planUploadDtoList = ((NativeQuery) query)
        .setResultTransformer(new AliasToBeanResultTransformer(PlanUploadDto.class)).getResultList();

    if (dashboardFilterDto.getSourceList() != null && !dashboardFilterDto.getSourceList().isEmpty()){
      planUploadDtoList = setEmptyValuesToNullPlanUpload(planUploadDtoList, dashboardFilterDto.getSourceList()).parallelStream().sorted(Comparator.comparing(p -> p.getSourceLoc())).collect(Collectors.toList());
    }

    return planUploadDtoList.parallelStream().sorted(Comparator.comparing(p -> p.getSourceLoc())).collect(Collectors.toList());
  }

  private List<PlanUploadDto> setEmptyValuesToNullPlanUpload(List<PlanUploadDto> planUploadDtoList, List<String> sourceList) {
    List<PlanUploadDto> dtoList = new ArrayList<>();
    if (planUploadDtoList == null || planUploadDtoList.isEmpty()){
      sourceList.parallelStream().forEach(source -> {
        PlanUploadDto planUploadDto = new PlanUploadDto();
        planUploadDto.setSourceLoc(source);
        dtoList.add(planUploadDto);
      });
      return dtoList;
    }

    sourceList.parallelStream().forEach(source -> {
      Optional<PlanUploadDto> optionalPlanUploadDto = planUploadDtoList.parallelStream().filter(plan -> plan.getSourceLoc().equals(source)).findFirst();
      if (optionalPlanUploadDto.isPresent()) {
        dtoList.add(optionalPlanUploadDto.get());
      } else {
        PlanUploadDto planUploadDto = new PlanUploadDto();
        planUploadDto.setSourceLoc(source);
        dtoList.add(planUploadDto);
      }
    });

    return dtoList;
  }

  private StringBuilder getQuery1ForPlanUpload(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, boolean isCummData, Optional<MTLocation> optionalMTLocation) {
    StringBuilder mainQuery = new StringBuilder("select COUNT(CASE WHEN abc.insert_user LIKE '3PL%' THEN abc.id END) as plPlans, " +
        " COUNT( CASE WHEN abc.insert_user NOT LIKE '3PL%' THEN abc.id END ) as not3PlPlans, count(abc.id) as total, abc.sourceLoc as sourceLoc " +
        "  from (select dp.source_loc as sourceLoc, dp.id, dp.insert_user from dispatch_plan dp WHERE dp.id is not null " );
    setWhereClauseForPlanUpload(mainQuery,dashboardFilterDto, applicationUser, isCummData, optionalMTLocation);

    mainQuery.append(" ) abc GROUP BY abc.sourceLoc ");

    return mainQuery;

  }

  private void setWhereClauseForPlanUpload(StringBuilder mainQuery, DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, boolean isCummData, Optional<MTLocation> optionalMTLocation) {
    if (isCummData){
      Date today = new Date();
//      For Cumm date filter will be from start of the month to current date
      mainQuery.append(" and  dp.dispatch_date   >=TO_DATE('" + DateUtils.getStartOfTheMonth(today) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      mainQuery.append(" and dp.dispatch_date <TO_DATE('" +
          DateUtils.formatDate(DateUtils.setNextDayStart(today), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
    }else {
      if (!StringUtils.isEmpty(dashboardFilterDto.getFromDispatchDate()) && !StringUtils.isEmpty(dashboardFilterDto.getToDispatchDate())){
        mainQuery.append(" and  dp.dispatch_date   >=TO_DATE('" + dashboardFilterDto.getFromDispatchDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
        mainQuery.append(" and dp.dispatch_date < TO_DATE('" +
            DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(dashboardFilterDto.getToDispatchDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      }
    }

   /* if (Constants.isDPREPRole(applicationUser.getRole()) ){
      if (dashboardFilterDto.getSourceList() != null && !dashboardFilterDto.getSourceList().isEmpty()){
        mainQuery.append(" AND dp.source_loc IN ("+Utility.join(dashboardFilterDto.getSourceList())+") ");
      }
    }else {
      mainQuery.append(" AND dp.source_loc IN ('"+applicationUser.getPlantCode()+"') ");
    }*/
    if (this.isExtWarehouse(optionalMTLocation)){
      if (dashboardFilterDto.getSourceList() != null && !dashboardFilterDto.getSourceList().isEmpty()) {
        mainQuery.append(" and (dp.source_loc IN (" + Utility.join(dashboardFilterDto.getSourceList()) + ") AND " +
            "(dp.source_loc = '" + applicationUser.getPlantCode() + "' OR  dp.source_loc = '" + optionalMTLocation.get().getLinkedPlant() + "'))");
      } else {
        mainQuery.append(" AND dp.source_loc IN ('" + applicationUser.getPlantCode() +"','"+optionalMTLocation.get().getLinkedPlant()+"') ");
      }
    }else {
      if (UserRole.getDPREPAccessRoles().contains(applicationUser.getRole())) {
        if (dashboardFilterDto.getSourceList() != null && !dashboardFilterDto.getSourceList().isEmpty()) {
          mainQuery.append(" AND dp.source_loc IN (" + Utility.join(dashboardFilterDto.getSourceList()) + ") ");
        }
      } else {
        mainQuery.append(" AND dp.source_loc = '" + applicationUser.getPlantCode() + "' ");
      }
    }


    if (!StringUtils.isEmpty(dashboardFilterDto.getDestination())){
      mainQuery.append(" AND dp.dest_loc = '"+dashboardFilterDto.getDestination()+"' ");
    }

    if (dashboardFilterDto.getMarketSegments() != null && !dashboardFilterDto.getMarketSegments().isEmpty()){
      mainQuery.append(" AND dp.market_segment IN ("+Utility.join(dashboardFilterDto.getMarketSegments())+") ");
    }

    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty())
    {
      mainQuery.append(" AND dp.item_category IN ("+Utility.join(dashboardFilterDto.getMaterialGroup())+") ");
    }

    if (!StringUtils.isEmpty(dashboardFilterDto.getPlanStatus())){
      mainQuery.append(" AND dp.status = '"+dashboardFilterDto.getPlanStatus()+"'");
    }

  }

  private StringBuilder getQuerysForDeleayedDays(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, boolean isCummData, Optional<MTLocation> optionalMTLocation) {
    StringBuilder subQuery = new StringBuilder("select COUNT(CASE WHEN abc.delayedDays >= "+dashboardFilterDto.getFirstLineStart()+" AND  abc.delayedDays <= "+(dashboardFilterDto.getFirstLineEnd() + 1)+" THEN abc.shipmentId END) as lessThan2, " +
        "COUNT(CASE WHEN abc.delayedDays >= "+dashboardFilterDto.getSecondLineStart()+" AND  abc.delayedDays <= "+(dashboardFilterDto.getSecondLineEnd() + 1)+" THEN abc.shipmentId END) as lessThan7, " +
        "COUNT(CASE WHEN abc.delayedDays >= "+dashboardFilterDto.getThirdLine()+" THEN abc.shipmentId END) as greaterThan7 " +
        " from " +
        " (select sh.shipment_id as shipmentId,(CASE WHEN lp.grn_reporting_date IS NOT NULL THEN " +
        "                Abs(( ( To_date(lp.grn_reporting_date, 'DD-MM-YYYY') - To_date(tr.gateout_date, 'DD-MM-YYYY') ) - ( CASE WHEN tr.tt_days IS NULL THEN 0 ELSE tr.tt_days END) ))" +
        "            ELSE " +
        "                (CASE WHEN (lp.loadslip_id IS NOT NULL AND tr.rej_status != 'REJECTED') AND " +
        "                                (((TO_DATE((SELECT SYSDATE FROM DUAL),'DD-MM-YYYY')) -(TO_DATE(tr.gateout_date,'DD-MM-YYYY')) -( CASE WHEN tr.tt_days IS NULL THEN 0 ELSE tr.tt_days END)) > 0) " +
        "                        THEN Abs(((TO_DATE((SELECT SYSDATE FROM DUAL),'DD-MM-YYYY')) -(TO_DATE(tr.gateout_date,'DD-MM-YYYY')) -( CASE WHEN tr.tt_days IS NULL THEN 0 ELSE tr.tt_days END)))" +
        "                    ELSE NULL END) " +
        "            END) AS delayedDays, ROW_NUMBER() OVER (PARTITION BY tr.shipment_id ORDER BY tr.insert_date ASC) as rn " +
        "            from truck_reporting tr " +
        "            RIGHT JOIN loadslip lp  ON lp.shipment_id = tr.shipment_id" +
        "            RIGHT JOIN shipment sh ON tr.shipment_id = sh.shipment_id  WHERE sh.shipment_id is not null ");

    setWhereClauseForDelayedDays(subQuery, dashboardFilterDto, applicationUser, isCummData, optionalMTLocation);
    subQuery.append(" ) abc where abc.rn = 1");

    return subQuery;
  }

  private void setWhereClauseForDelayedDays(StringBuilder subQuery, DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, boolean isCummData, Optional<MTLocation> optionalMTLocation) {
    if (isCummData){
      Date today = new Date();
//      For Cumm date filter will be from start of the month to current date
      subQuery.append(" and  sh.insert_date   >=TO_DATE('" + DateUtils.getStartOfTheMonth(today) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      subQuery.append(" and sh.insert_date < TO_DATE('" +
          DateUtils.formatDate(DateUtils.setNextDayStart(today), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
    }else {
      if (!StringUtils.isEmpty(dashboardFilterDto.getFromDispatchDateAgeing()) && !StringUtils.isEmpty(dashboardFilterDto.getToDispatchDateAgeing())){
        subQuery.append(" and  sh.insert_date   >=TO_DATE('" + dashboardFilterDto.getFromDispatchDateAgeing() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
        subQuery.append(" and sh.insert_date < TO_DATE('" +
            DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(dashboardFilterDto.getToDispatchDateAgeing(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      }
    }

    if (Constants.isDPREPRole(applicationUser.getRole()) ){
      if (!StringUtils.isEmpty(dashboardFilterDto.getSourceLoc())){
        subQuery.append(" AND tr.reporting_location = '"+dashboardFilterDto.getSourceLoc()+"' ");
      }
    }else {
      subQuery.append(" AND tr.reporting_location = '"+applicationUser.getPlantCode()+"' ");
    }

    if (!StringUtils.isEmpty(dashboardFilterDto.getDestination())){
      subQuery.append(" AND tr.dest_loc = '"+dashboardFilterDto.getDestination()+"' ");
    }

    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      subQuery.append(" AND lp.item_category IN ("+Utility.join(dashboardFilterDto.getMaterialGroup())+") ");
    }
    if (dashboardFilterDto.getMarketSegments() != null && !dashboardFilterDto.getMarketSegments().isEmpty()){
      subQuery.append(" AND lp.mkt_seg IN ("+Utility.join(dashboardFilterDto.getMarketSegments())+") ");
    }
    if (dashboardFilterDto.getShipmentStatusList() != null && !dashboardFilterDto.getShipmentStatusList().isEmpty()){
      subQuery.append(" AND sh.status IN ("+Utility.join(dashboardFilterDto.getShipmentStatusList())+") ");
    }/*else {
      subQuery.append(" AND sh.status IN ('INTRANSIT') ");
    }*/
  }

  @Override
  public List<PlansDataDto> getOpenPlansDetails(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation) {
    StringBuilder query1 = getQuery1OpenPlansData(dashboardFilterDto, applicationUser, optionalMTLocation, false, optionalMTLocation);

    StringBuilder query2 = getQuery1OpenPlansData(dashboardFilterDto, applicationUser, optionalMTLocation, true, optionalMTLocation);

    StringBuilder mainQuery = new StringBuilder("SELECT * FROM ( ");
    mainQuery.append(" WITH query1 AS ("+query1+"), query2 AS ("+query2+"), query3 as (SELECT tte_capacity as tteCapacity from mt_truck_type where truck_type = 'STANDARD_FTL' AND variant1 IS NULL ) ");
    mainQuery.append("select query1.totalPlanQty as totalPlanQty, " +
        "query1.totalAvailQty as totalAvailQty, " +
        "query1.totalTotAvailQty as totalTotAvailQty, " +
        "query1.totalReservedQty as totalReservedQty, " +
        "query1.total as total, " +
        "query1.totalTTE as  totalTTE, " +
        "(CASE WHEN query2.totalPlanQty IS NOT NULL THEN query2.totalPlanQty ELSE 0 END) as cummTotalPlanQty, " +
        "(CASE WHEN query2.totalAvailQty IS NOT NULL THEN query2.totalAvailQty ELSE 0 END) as cummTotalAvailQty, " +
        "(CASE WHEN query2.totalTotAvailQty IS NOT NULL THEN query2.totalTotAvailQty ELSE 0 END) as cummTotalTotAvailQty, " +
        "(CASE WHEN query2.totalReservedQty IS NOT NULL THEN query2.totalReservedQty ELSE 0 END) as cummTotalReservedQty, " +
        "(CASE WHEN query2.total IS NOT NULL THEN query2.total ELSE 0 END) as cummTotal, " +
        "(CASE WHEN query2.totalTTE IS NOT NULL THEN query2.totalTTE ELSE 0 END) as cummTotalTTE, " +
        "(CASE WHEN query1.totalTTE IS NOT NULL THEN (query1.totalTTE / query3.tteCapacity ) ELSE 0 END ) as truckCount," +
        "(CASE WHEN query2.totalTTE IS NOT NULL THEN (query2.totalTTE / query3.tteCapacity ) ELSE 0 END ) as cummTruckCount ");
    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      mainQuery.append(", query1.itemCategory as itemCategory from query3, query1 LEFT JOIN query2 ON query1.itemCategory = query2.itemCategory ");
    }else{
      mainQuery.append(" FROM query1, query2, query3 ");
    }
    mainQuery.append(" ) abc ");
    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      mainQuery.append("  order by abc.itemCategory desc ");
    }

    Query query = null;

    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      query = entityManager.unwrap(Session.class).createNativeQuery(mainQuery.toString())
          .addScalar("totalPlanQty", StandardBasicTypes.DOUBLE).addScalar("totalAvailQty", StandardBasicTypes.DOUBLE).addScalar("totalTotAvailQty", StandardBasicTypes.DOUBLE)
          .addScalar("totalReservedQty", StandardBasicTypes.DOUBLE).addScalar("total", StandardBasicTypes.DOUBLE).addScalar("totalTTE", StandardBasicTypes.DOUBLE)
          .addScalar("cummTotalPlanQty", StandardBasicTypes.DOUBLE).addScalar("cummTotalAvailQty", StandardBasicTypes.DOUBLE).addScalar("cummTotalTotAvailQty", StandardBasicTypes.DOUBLE)
          .addScalar("cummTotalReservedQty", StandardBasicTypes.DOUBLE).addScalar("cummTotal", StandardBasicTypes.DOUBLE).addScalar("cummTotalTTE", StandardBasicTypes.DOUBLE)
          .addScalar("truckCount", StandardBasicTypes.DOUBLE).addScalar("cummTruckCount", StandardBasicTypes.DOUBLE).addScalar("itemCategory", StandardBasicTypes.STRING);
    }else{
      query = entityManager.unwrap(Session.class).createNativeQuery(mainQuery.toString())
          .addScalar("totalPlanQty", StandardBasicTypes.DOUBLE).addScalar("totalAvailQty", StandardBasicTypes.DOUBLE).addScalar("totalTotAvailQty", StandardBasicTypes.DOUBLE)
          .addScalar("totalReservedQty", StandardBasicTypes.DOUBLE).addScalar("total", StandardBasicTypes.DOUBLE).addScalar("totalTTE", StandardBasicTypes.DOUBLE)
          .addScalar("cummTotalPlanQty", StandardBasicTypes.DOUBLE).addScalar("cummTotalAvailQty", StandardBasicTypes.DOUBLE).addScalar("cummTotalTotAvailQty", StandardBasicTypes.DOUBLE)
          .addScalar("cummTotalReservedQty", StandardBasicTypes.DOUBLE).addScalar("cummTotal", StandardBasicTypes.DOUBLE).addScalar("cummTotalTTE", StandardBasicTypes.DOUBLE)
          .addScalar("truckCount", StandardBasicTypes.DOUBLE).addScalar("cummTruckCount", StandardBasicTypes.DOUBLE);
    }
    List<PlansDataDto> plansDataDtos =  ((NativeQuery) query)
        .setResultTransformer(new AliasToBeanResultTransformer(PlansDataDto.class)).getResultList();

    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      plansDataDtos = setEmptyValuesToZeroPlans(plansDataDtos, dashboardFilterDto.getMaterialGroup());
    }
    return plansDataDtos;
  }

  private List<PlansDataDto> setEmptyValuesToZeroPlans(List<PlansDataDto> plansDataDtos, List<String> materialGroup) {
    if (plansDataDtos == null || plansDataDtos.isEmpty()){
      materialGroup.parallelStream().forEach(group -> {
        PlansDataDto plansDataDto = new PlansDataDto();
        plansDataDto.setItemCategory(group);
        plansDataDtos.add(plansDataDto);
      });
      return plansDataDtos;
    }

    materialGroup.stream().forEach(group -> {
      Optional<PlansDataDto> optionalPlansDataDto = plansDataDtos.parallelStream().filter(plan -> plan.getItemCategory().equals(group)).findFirst();
      if (!optionalPlansDataDto.isPresent()){
        PlansDataDto plansDataDto = new PlansDataDto();
        plansDataDto.setItemCategory(group);
        plansDataDtos.add(plansDataDto);
      }
    });
    return plansDataDtos;
  }

  private StringBuilder getQuery1OpenPlansData(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation, boolean isCummData, Optional<MTLocation> mtLocation) {
    StringBuilder subQuery = new StringBuilder("select SUM(dp.quantity) as totalPlanQty, " +
        "SUM(dp.avail_qty) as totalAvailQty, SUM(dp.tot_avail_qty) as totalTotAvailQty, " +
        "SUM(dp.reserved_qty + dp.loaded_qty) as totalReservedQty, count(*) as total ");
    if (optionalMTLocation!= null && optionalMTLocation.isPresent() && LocationType.RDC.name().equals(optionalMTLocation.get().getType())){
      subQuery.append(", SUM((dp.quantity - dp.deleted_qty) * dp.tte) AS totalTTE");
    }else{
      subQuery.append(",SUM(dp.avail_qty * dp.tte) AS totalTTE ");
    }
    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      subQuery.append(", dp.item_Category AS itemCategory ");
    }
    subQuery.append(" FROM dispatch_plan dp WHERE dp.id IS NOT NULL ");
    setWhereClauseForOpenPlans(subQuery, dashboardFilterDto, applicationUser, optionalMTLocation, isCummData, optionalMTLocation);
    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      subQuery.append(" GROUP BY dp.item_Category ");
    }

    return subQuery;
  }

  private void setWhereClauseForOpenPlans(StringBuilder subQuery, DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation, boolean isCummData, Optional<MTLocation> mtLocation) {
    if (isCummData){
      Date today = new Date();
//      For Cumm date filter will be from start of the month to current date
      subQuery.append(" and  dp.dispatch_date   >=TO_DATE('" + DateUtils.getStartOfTheMonth(today) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      subQuery.append(" and dp.dispatch_date < TO_DATE('" +
          DateUtils.formatDate(DateUtils.setNextDayStart(today), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
    }else {
      if (!StringUtils.isEmpty(dashboardFilterDto.getFromDispatchDate()) && !StringUtils.isEmpty(dashboardFilterDto.getToDispatchDate())){
        subQuery.append(" and  dp.dispatch_date   >=TO_DATE('" + dashboardFilterDto.getFromDispatchDate() + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
        subQuery.append(" and dp.dispatch_date < TO_DATE('" +
            DateUtils.formatDate(DateUtils.setNextDayStart(DateUtils.formatDate(dashboardFilterDto.getToDispatchDate(), PLAN_RECORD_DATE_FORMAT)), Constants.DATE_FORMATE_WITH_HYPHEN) + "','" + Constants.DATE_FORMATE_WITH_HYPHEN + "')");
      }
    }

/*    if (this.isExtWarehouse(optionalMTLocation)){
      if (!StringUtils.isEmpty(dashboardFilterDto.getSourceLoc())) {
        subQuery.append(" and (dp.source_loc LIKE '%" + dashboardFilterDto.getSourceLoc() + "%' AND " +
            "(dp.source_loc = '" + applicationUser.getPlantCode() + "' OR  dp.source_loc = '" + optionalMTLocation.get().getLinkedPlant() + "'))");
      } else {
        subQuery.append(" AND dp.source_loc IN ('" + applicationUser.getPlantCode() +"','"+optionalMTLocation.get().getLinkedPlant()+"') ");
      }
    }else {
      if (UserRole.getDPREPAccessRoles().contains(applicationUser.getRole())) {
        if (!StringUtils.isEmpty(dashboardFilterDto.getSourceLoc())) {
          subQuery.append(" AND dp.source_loc = '" + dashboardFilterDto.getSourceLoc() + "' ");
        }
      } else {
        subQuery.append(" AND dp.source_loc = '" + applicationUser.getPlantCode() + "' ");
      }
    }*/
    if (this.isExtWarehouse(optionalMTLocation)){
      if (dashboardFilterDto.getSourceList() != null && !dashboardFilterDto.getSourceList().isEmpty()) {
        subQuery.append(" and (dp.source_loc IN (" + Utility.join(dashboardFilterDto.getSourceList()) + ") AND " +
            "(dp.source_loc = '" + applicationUser.getPlantCode() + "' OR  dp.source_loc = '" + optionalMTLocation.get().getLinkedPlant() + "'))");
      } else {
        subQuery.append(" AND dp.source_loc IN ('" + applicationUser.getPlantCode() +"','"+optionalMTLocation.get().getLinkedPlant()+"') ");
      }
    }else {
      if (UserRole.getDPREPAccessRoles().contains(applicationUser.getRole())) {
        if (dashboardFilterDto.getSourceList() != null && !dashboardFilterDto.getSourceList().isEmpty()) {
          subQuery.append(" AND dp.source_loc IN (" + Utility.join(dashboardFilterDto.getSourceList()) + ") ");
        }
      } else {
        subQuery.append(" AND dp.source_loc = '" + applicationUser.getPlantCode() + "' ");
      }
    }

    if (!StringUtils.isEmpty(dashboardFilterDto.getDestination())){
      subQuery.append(" AND dp.dest_loc = '"+dashboardFilterDto.getDestination()+"' ");
    }

    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      subQuery.append(" AND dp.item_category IN ("+Utility.join(dashboardFilterDto.getMaterialGroup())+") ");
    }
    if (dashboardFilterDto.getMarketSegments() != null && !dashboardFilterDto.getMarketSegments().isEmpty()){
      subQuery.append(" AND dp.market_segment IN ("+Utility.join(dashboardFilterDto.getMarketSegments())+") ");
    }
    if (!StringUtils.isEmpty(dashboardFilterDto.getPlanStatus())){
      subQuery.append(" AND dp.status = '"+dashboardFilterDto.getPlanStatus()+"' ");
    }
    if (dashboardFilterDto.getAppStatusList() != null && !dashboardFilterDto.getAppStatusList().isEmpty()){
      subQuery.append(" AND dp.app_status IN ("+Utility.join(dashboardFilterDto.getAppStatusList())+") ");
    }
  }

  @Override
  public List<PlansDataDto> getDispatchedPlanDetails(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation) {
    StringBuilder query1 = getQuery1ForDispathedPlans(dashboardFilterDto, applicationUser, optionalMTLocation, false, optionalMTLocation);
    StringBuilder query2 = getQuery1ForDispathedPlans(dashboardFilterDto, applicationUser, optionalMTLocation, true, optionalMTLocation);

    StringBuilder mainQuery = new StringBuilder("SELECT * FROM (WITH query1 AS ("+query1+"), query2 AS ("+query2+"), query3 AS (SELECT tte_capacity as tteCapacity from mt_truck_type where truck_type = 'STANDARD_FTL' AND variant1 is null ) " +
        "SELECT query1.totalDispatchedQty AS totalDispatchedQty, query1.total AS total, (query1.totalTTe / query3.tteCapacity) as truckCount, query1.totalTTe as totalTTe, " +
        "(CASE WHEN query2.totalDispatchedQty IS NOT NULL THEN query2.totalDispatchedQty ELSE 0 END) AS cummTotalDispatchedQty," +
        "(CASE WHEN query2.total IS NOT NULL THEN query2.total ELSE 0 END) AS cummTotal, " +
        "(CASE WHEN query2.totalTTe IS NOT NULL THEN query2.totalTTe ELSE 0 END) AS cummTotalTTe, " +
        "(CASE WHEN query2.totalTTe IS NOT NULL THEN (query2.totalTTe/ query3.tteCapacity) ELSE 0 END) AS cummTruckCount ");
    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      mainQuery.append(", query1.itemCategory as itemCategory FROM query3, query1 LEFT JOIN query2 ON query1.itemCategory = query2.itemCategory ) abc ORDER BY abc.itemCategory desc");
    }else{
      mainQuery.append(" FROM query1, query2, query3 )");
    }

    Query query = null;
    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      query = entityManager.unwrap(Session.class).createNativeQuery(mainQuery.toString())
          .addScalar("totalDispatchedQty", StandardBasicTypes.DOUBLE).addScalar("total", StandardBasicTypes.DOUBLE)
          .addScalar("totalTTE", StandardBasicTypes.DOUBLE).addScalar("truckCount", StandardBasicTypes.DOUBLE)
          .addScalar("cummTotalDispatchedQty", StandardBasicTypes.DOUBLE).addScalar("cummTotal", StandardBasicTypes.DOUBLE)
          .addScalar("cummTotalTTE", StandardBasicTypes.DOUBLE).addScalar("cummTruckCount", StandardBasicTypes.DOUBLE)
          .addScalar("itemCategory", StandardBasicTypes.STRING);
    }else{
      query = entityManager.unwrap(Session.class).createNativeQuery(mainQuery.toString())
          .addScalar("totalDispatchedQty", StandardBasicTypes.DOUBLE).addScalar("total", StandardBasicTypes.DOUBLE)
          .addScalar("totalTTE", StandardBasicTypes.DOUBLE).addScalar("truckCount", StandardBasicTypes.DOUBLE)
          .addScalar("cummTotalDispatchedQty", StandardBasicTypes.DOUBLE).addScalar("cummTotal", StandardBasicTypes.DOUBLE)
          .addScalar("cummTotalTTE", StandardBasicTypes.DOUBLE).addScalar("cummTruckCount", StandardBasicTypes.DOUBLE);
    }

    List<PlansDataDto> plansDataDtos =  ((NativeQuery) query)
        .setResultTransformer(new AliasToBeanResultTransformer(PlansDataDto.class)).getResultList();
    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      plansDataDtos = setEmptyValuesToZeroPlans(plansDataDtos, dashboardFilterDto.getMaterialGroup());
    }
    return plansDataDtos;
  }

  private StringBuilder getQuery1ForDispathedPlans(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation, boolean isCummData, Optional<MTLocation> mtLocation) {

    StringBuilder subQuery1 = new StringBuilder("SELECT SUM(dp.dispatched_qty) as totalDispatchedQty, count(*) as total, SUM(dp.dispatched_qty * dp.tte) as totalTTe ");
    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      subQuery1.append(", dp.item_category as itemCategory ");
    }
    subQuery1.append(" FROM dispatch_plan dp WHERE dp.id IS NOT NULL ");
    setWhereClauseForOpenPlans(subQuery1, dashboardFilterDto, applicationUser, optionalMTLocation, isCummData, optionalMTLocation);
    subQuery1.append(" AND dp.dispatched_qty > 0 " );

    if (dashboardFilterDto.getMaterialGroup() != null && !dashboardFilterDto.getMaterialGroup().isEmpty()){
      subQuery1.append(" GROUP BY dp.item_category ");
    }

    return subQuery1;

  }

  private boolean isExtWarehouse(Optional<MTLocation> optionalMTLocation){
    if (optionalMTLocation!= null && optionalMTLocation.isPresent()){
      if (!StringUtils.isEmpty(optionalMTLocation.get().getLocationClass()) && LocationType.EXT_WAREHOUSE.name().equals(optionalMTLocation.get().getLocationClass())){
        return true;
      }
    }
    return false;
  }


}
