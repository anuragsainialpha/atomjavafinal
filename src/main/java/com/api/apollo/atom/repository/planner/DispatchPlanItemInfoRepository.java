package com.api.apollo.atom.repository.planner;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.Constants.DispatchPlanItemAppStatus;
import com.api.apollo.atom.constant.Constants.Status;
import com.api.apollo.atom.entity.plan.DispatchPlanItemInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DispatchPlanItemInfoRepository extends JpaRepository<DispatchPlanItemInfo, Long> {

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query("update DispatchPlanItemInfo planInfo set planInfo.appStatus =:appStatus where planInfo.id in :ids")
  void approveByDispatchPlanItemIds(@Param("appStatus") DispatchPlanItemAppStatus appStatus, @Param("ids") List<Long> ids);

  @Modifying
  @Transactional
  @Query("delete from DispatchPlanItemInfo t where id = ?1")
  void deleteById(Long id);

  List<DispatchPlanItemInfo> findAllByIdIn(List<Long> ids);

  @Modifying
  @Transactional
  @Query(value = "delete from dispatch_plan_bom where line_num = ?1 and dispatch_plan_id = ?2", nativeQuery = true)
  void deleteDispatchPlanBom(int id, Long planId);

  Page<DispatchPlanItemInfo> findAllByInsertUser(String userId, Pageable pageable);

  Page<DispatchPlanItemInfo> findAllByInsertUserAndAppStatus(String userId, DispatchPlanItemAppStatus appStatus, Pageable pageable);

  Page<DispatchPlanItemInfo> findAllByInsertUserAndAppStatusIn(String userId, List<DispatchPlanItemAppStatus> appStatuses, Pageable pageable);

  Page<DispatchPlanItemInfo> findAllBySourceLocationInAndAppStatusIn(List<String> sourceLocList, List<DispatchPlanItemAppStatus> appStatuses, Pageable pageable);

  Page<DispatchPlanItemInfo> findAllByInsertUserInAndAppStatusInOrSourceLocation(List<String> userId, List<DispatchPlanItemAppStatus> appStatuses, String source, Pageable pageable);

  Page<DispatchPlanItemInfo> findAllByAppStatusInAndSourceLocation(List<DispatchPlanItemAppStatus> appStatuses, String source, Pageable pageable);

  /*displaying all record for user with role DP_REP*/
  Page<DispatchPlanItemInfo> findAllByAppStatusIn(List<DispatchPlanItemAppStatus> appStatuses, Pageable pageable);

  Long countByDispatchPlanId(Long id);

  @Query("select distinct d.destinationLocation from DispatchPlanItemInfo d where d.status= ?1 AND d.sourceLocation = ?2 AND d.availableQuantity > 0 ORDER BY d.destinationLocation ASC ")
  List<String> findOpenDestinationsOrderByDest(Status status, String sourceLoc);

  @Query("select distinct d.destinationLocation from DispatchPlanItemInfo d where d.status= ?1 AND d.sourceLocation IN (?2) AND d.availableQuantity > 0 ORDER BY d.destinationLocation ASC ")
  List<String> findOpenDestinationsInOrderByDest(Status status, List<String> sourceLoc);

  /*Getting open destinations with description*/
  @Query("select distinct d.destinationLocation as dest_loc, d.destinationDescription as dest_desc from DispatchPlanItemInfo d where d.status= ?1 AND d.sourceLocation IN (?2) AND " +
      " d.availableQuantity > 0  ORDER BY d.destinationLocation ASC ")
  List<Map<String, Object>> findOpenDestinationsAndDescriptionInOrderByDest(Status status, List<String> sourceLoc);

  //Currently not using following query, Maintaining just for backup purpose
  @Query(nativeQuery = true, value = "SELECT DISTINCT a.item_id, a.item_description, a.batch_code, a.sum_qty, a.min_priority, a.tte, a.market_segment, a.item_category, bom.tube_code, bom.tube_desc, bom.flap_code, bom.flap_desc, bom.valve_code, bom.valve_desc, bom.tube_comp_qty, bom.flap_comp_qty, bom.valve_comp_qty, bom.weight, bom.volume "
      + "FROM (SELECT DISTINCT dp.item_id, dp.item_description, dp.batch_code, Sum(dp.avail_qty) sum_qty, Min(dp.priority) min_priority, dp.tte, dp.market_segment, dp.item_category FROM dispatch_plan dp "
      + "WHERE dest_loc = ?1 AND avail_qty > 0 AND status = 'OPEN' AND ( app_status = 'APPROVED' OR app_status = 'APPROVED_PART' ) GROUP BY item_id, item_description, batch_code,tte, market_segment, item_category) a "
      + "LEFT OUTER JOIN dispatch_plan_bom bom ON a.item_id = bom.item_id")
  List<Map<String, Object>> findApprovedPlansByDestination(String destLoc);

  @Query(nativeQuery = true, value = "select distinct a.item_id, a.item_description, a.batch_code, a.sum_qty, a.min_priority, a.tte, a.market_segment, a.item_category, bom.tube_code, bom.tube_desc, bom.flap_code, bom.flap_desc, bom.valve_code, bom.valve_desc, bom.tube_comp_qty, bom.flap_comp_qty, bom.valve_comp_qty, bom.weight, bom.volume "
      + " from (select distinct dp.item_id, dp.item_description, dp.batch_code, sum(dp.avail_qty) sum_qty, min(dp.priority) min_priority, dp.tte, dp.market_segment, dp.item_category from dispatch_plan dp where source_loc = ?1 and dest_loc = ?2 and avail_qty > 0 and status = 'OPEN' and ( app_status = 'APPROVED' or app_status = 'APPROVED_PART' ) "
      + "group by item_id, item_description, batch_code, tte, market_segment, item_category ) a, table(atl_business_flow_pkg.get_item_bom(a.item_id,?1,?2)) bom")
  List<Map<String, Object>> findApprovedPlansByDestination(String source, String dest);

  @Query(nativeQuery = true, value = "select distinct a.item_category from (select distinct dp.item_id, dp.item_category from dispatch_plan dp where source_loc = ?1 and dest_loc = ?2 and avail_qty > 0 and status = 'OPEN' and ( app_status = 'APPROVED' or app_status = 'APPROVED_PART' ) " +
      "  ) a, table(atl_business_flow_pkg.get_item_bom(a.item_id,?1,?2)) bom")
  List<String> findApprovedPlansByDestinationToGetDistictCategory(String source, String dest);

  @Query(nativeQuery = true, value = "select distinct a.item_id, a.item_description, a.batch_code, a.sum_qty, a.min_priority, a.tte, a.market_segment, a.item_category, bom.tube_code, bom.tube_desc, bom.flap_code, bom.flap_desc, bom.valve_code, bom.valve_desc, bom.tube_comp_qty, bom.flap_comp_qty, bom.valve_comp_qty, bom.weight, bom.volume "
      + " from (select distinct dp.item_id, dp.item_description, dp.batch_code, sum(dp.avail_qty) sum_qty, min(dp.priority) min_priority, dp.tte, dp.market_segment, dp.item_category from dispatch_plan dp where source_loc = ?1 and dest_loc = ?2 and avail_qty > 0 and status = 'OPEN' and ( app_status = 'APPROVED' or app_status = 'APPROVED_PART' ) "
      + "group by item_id, item_description, batch_code, tte, market_segment, item_category ) a, table(atl_business_flow_pkg.get_item_bom(a.item_id,?1,?2)) bom  where a.item_category in(?3) ")
  List<Map<String, Object>> findApprovedPlansByDestinationAndItemCategory(String source, String dest ,List<String> itemCategories);

	@Query(nativeQuery = true, value = "select distinct a.item_id, a.item_description, a.batch_code, a.sum_qty, a.min_priority, a.tte, a.market_segment, a.item_category, bom.tube_code, bom.tube_desc, bom.flap_code, bom.flap_desc, bom.valve_code, bom.valve_desc, bom.tube_comp_qty, bom.flap_comp_qty, bom.valve_comp_qty, bom.weight, bom.volume "
			+ " from (select distinct dp.item_id, dp.item_description, dp.batch_code, sum(dp.avail_qty) sum_qty, min(dp.priority) min_priority, dp.tte, dp.market_segment, dp.item_category from dispatch_plan dp where source_loc = ?1 and dest_loc = ?2 and avail_qty > 0 and status = 'OPEN' and ( app_status = 'APPROVED' or app_status = 'APPROVED_PART' ) "
			+ " AND (item_id,batch_code) in ((select item_id,batch_code from dispatch_plan where dest_loc = ?2) minus (select distinct item_id,batch_code from loadslip_detail where loadslip_id = ?4 and batch_code is not null)) "
			+ " group by item_id, item_description, batch_code, tte, market_segment, item_category ) a, table(atl_business_flow_pkg.get_item_bom(a.item_id,?1,?2)) bom where a.item_category in(?3)")
	List<Map<String, Object>> findApprovedPlansByDestinationExcludingCurrentLoadslipItemsAndItemCategory(String plantCode, String destLoc,List<String> itemCategories, String loadslipId);

  //Currently not using following query, Maintaining just for backup purpose
  @Query(nativeQuery = true, value = "SELECT DISTINCT a.item_id, a.item_description, a.batch_code, a.sum_qty, a.min_priority, a.tte, a.market_segment, a.item_category, bom.tube_code, bom.tube_desc, bom.flap_code, bom.flap_desc, bom.valve_code, bom.valve_desc, bom.tube_comp_qty, bom.flap_comp_qty, bom.valve_comp_qty, bom.weight, bom.volume "
      + "FROM (SELECT DISTINCT dp.item_id, dp.item_description, dp.batch_code, Sum(dp.avail_qty) sum_qty, Min(dp.priority) min_priority, dp.tte, dp.market_segment, dp.item_category FROM dispatch_plan dp "
      + "WHERE dest_loc = ?1 and item_id in(?2) AND status = 'OPEN' AND ( app_status = 'APPROVED' OR app_status = 'APPROVED_PART' ) GROUP BY item_id, item_description, batch_code,tte, market_segment, item_category) a "
      + "LEFT OUTER JOIN dispatch_plan_bom bom ON a.item_id = bom.item_id")
  List<Map<String, Object>> findApprovedPlansByDestination(String destLoc, List<String> itemIds);


  @Query(nativeQuery = true, value = "select distinct a.item_id, a.item_description, a.batch_code, a.sum_qty, a.min_priority, a.tte, a.market_segment, a.item_category, bom.tube_code, bom.tube_desc, bom.flap_code, bom.flap_desc, bom.valve_code, bom.valve_desc, bom.tube_comp_qty, bom.flap_comp_qty, bom.valve_comp_qty, bom.weight, bom.volume "
      + " from (select distinct dp.item_id, dp.item_description, dp.batch_code, sum(dp.avail_qty) sum_qty, min(dp.priority) min_priority, dp.tte, dp.market_segment, dp.item_category from dispatch_plan dp where source_loc = ?1 and dest_loc = ?2 and item_id in(?3)  and status = 'OPEN' and ( app_status = 'APPROVED' or app_status = 'APPROVED_PART' ) "
      + "group by item_id, item_description, batch_code, tte, market_segment, item_category ) a, table(atl_business_flow_pkg.get_item_bom(a.item_id,?1,?2)) bom")
  List<Map<String, Object>> findApprovedPlansByDestination(String source, String dest, List<String> itemIds);


  List<DispatchPlanItemInfo> findAllBySourceLocationAndDestinationLocationAndItemIdInAndStatusOrderByDispatchDateAsc(String plantCode, String destination, List<String> itemIds,Constants.Status status);

  List<DispatchPlanItemInfo> findAllBySourceLocationAndDestinationLocationAndItemIdInAndAvailableQuantityGreaterThanOrderByDispatchDateAsc(String plantCode, String destination, List<String> itemIds, Integer availableQty);

  List<DispatchPlanItemInfo> findAllBySourceLocationAndDestinationLocationAndBatchCodeInAndItemIdInOrderByDispatchDateAsc(String plantCode, String destination, List<String> batchCodes, List<String> itemIds);

  List<DispatchPlanItemInfo> findAllBySourceLocationAndDestinationLocationAndBatchCodeAndItemIdAndStatusOrderByDispatchDateAsc(String plantCode, String destination, String batchCode, String itemId, Constants.Status status);

  List<DispatchPlanItemInfo> findAllBySourceLocationAndDestinationLocationAndItemIdAndStatusOrderByDispatchDateAsc(String plantCode, String destination, String itemId,Constants.Status status);

  @Query(nativeQuery = true, value = "select distinct a.item_id, a.item_description, a.batch_code, a.sum_qty, a.min_priority, a.tte, a.market_segment, a.item_category, bom.tube_code, bom.tube_desc, bom.flap_code, bom.flap_desc, bom.valve_code, bom.valve_desc, bom.tube_comp_qty, bom.flap_comp_qty, bom.valve_comp_qty, bom.weight, bom.volume "
      + " from (select distinct dp.item_id, dp.item_description, dp.batch_code, sum(dp.avail_qty) sum_qty, min(dp.priority) min_priority, dp.tte, dp.market_segment, dp.item_category from dispatch_plan dp where source_loc = ?1 and dest_loc = ?2 and avail_qty > 0 and status = 'OPEN' and ( app_status = 'APPROVED' or app_status = 'APPROVED_PART' ) "
      + " AND (item_id,batch_code) in ((select item_id,batch_code from dispatch_plan where dest_loc = ?2) minus (select distinct item_id,batch_code from loadslip_detail where loadslip_id = ?3 and batch_code is not null)) "
      + " group by item_id, item_description, batch_code, tte, market_segment, item_category ) a, table(atl_business_flow_pkg.get_item_bom(a.item_id,?1,?2)) bom ")
  List<Map<String, Object>> findApprovedPlansByDestinationExcludingCurrentLoadslipItems(String plantCode, String destLoc, String loadslipId);

  @Query(nativeQuery = true, value = "select dps.* from(SELECT dp.app_status       AS appStatus, " +
      "       dp.approved_qty     AS approvedQuantity, " +
      "       dp.avail_qty        AS availableQuantity, " +
      "       dp.batch_code       AS batchCode, " +
      "       dp.item_category    AS category, " +
      "       dp.comments         AS comments, " +
      "       dp.deleted_qty      AS deletedApprQuantity, " +
      "       dp.dest_description AS destinationDescription, " +
      "       dp.dest_loc         AS destinationLocation, " +
      "       dp.dispatch_date    AS dispatchDate, " +
      "       dp.dispatched_qty   AS dispatchedQuantity, " +
      "       dp.id               AS id, " +
      "       dp.insert_date      AS insertDate, " +
      "       dp.insert_user      AS insertUser, " +
      "       dp.item_description AS itemDescription, " +
      "       dp.item_id          AS itemId, " +
      "       dp.line_num         AS lineNumber, " +
      "       dp.market_segment   AS marketSegment, " +
      "       d.dispatch_plan_id AS planId, " +
      "       dp.priority         AS priority, " +
      "       dp.quantity         AS quantity, " +
      "       dp.reserved_qty     as reservedQuantity, " +
      "       dp.source_loc       AS sourceLocation, " +
      "       dp.status           AS status, " +
      "       dp.tot_avail_qty    AS totalAvailableQuantity, " +
      "       dp.tte              AS tte,  " +
      "       dp.unapp_qty        AS unapprovedQuantity, " +
      "       dp.unapp_del_qty    AS deletedUnApprQuantity,  " +
      "       dp.update_date      AS updateDate,  " +
      "       dp.update_user      AS updateUser, " +
      "       dp.weight           AS weight, " +
      "       dp.weight_uom       AS weightUom, " +
      "       dp.volume           AS volume, " +
      "       dp.volume_uom       AS volumeUom, " +
      "       dp.loaded_qty       AS loadedQty " +
      " FROM   dispatch_plan dp " +
      "       LEFT JOIN disp_plan d " +
      "              ON dp.dispatch_plan_id = d.dispatch_plan_id " +
      "       where dp.app_status IN (?1) and dp.source_loc = ?2 ORDER BY dp.dispatch_date DESC ) dps",
      countQuery = "select count(*) FROM   dispatch_plan dp " +
          "LEFT JOIN disp_plan d ON dp.dispatch_plan_id = d.dispatch_plan_id " +
          "where dp.app_status IN (?1)  and dp.source_loc = ?2")
  Page<Map<String, Object>> findAllPlansByAppStatusInAndSourceLocationNRM(List<String> appStatuses, String source, Pageable pageable);

  /*For DP_REP*/
  @Query(nativeQuery = true, value = "select dps.* from(SELECT dp.app_status       AS appStatus, " +
      "       dp.approved_qty     AS approvedQuantity, " +
      "       dp.avail_qty        AS availableQuantity, " +
      "       dp.batch_code       AS batchCode, " +
      "       dp.item_category    AS category, " +
      "       dp.comments         AS comments, " +
      "       dp.deleted_qty      AS deletedApprQuantity, " +
      "       dp.dest_description AS destinationDescription, " +
      "       dp.dest_loc         AS destinationLocation, " +
      "       dp.dispatch_date    AS dispatchDate, " +
      "       dp.dispatched_qty   AS dispatchedQuantity, " +
      "       dp.id               AS id, " +
      "       dp.insert_date      AS insertDate, " +
      "       dp.insert_user      AS insertUser, " +
      "       dp.item_description AS itemDescription, " +
      "       dp.item_id          AS itemId, " +
      "       dp.line_num         AS lineNumber, " +
      "       dp.market_segment   AS marketSegment, " +
      "       d.dispatch_plan_id AS planId, " +
      "       dp.priority         AS priority, " +
      "       dp.quantity         AS quantity, " +
      "       dp.reserved_qty     as reservedQuantity, " +
      "       dp.source_loc       AS sourceLocation, " +
      "       dp.status           AS status, " +
      "       dp.tot_avail_qty    AS totalAvailableQuantity, " +
      "       dp.tte              AS tte, " +
      "       dp.unapp_qty        AS unapprovedQuantity, " +
      "       dp.unapp_del_qty    AS deletedUnApprQuantity, " +
      "       dp.update_date      AS updateDate,  " +
      "       dp.update_user      AS updateUser, " +
      "       dp.weight           AS weight, " +
      "       dp.weight_uom       AS weightUom, " +
      "       dp.volume           AS volume, " +
      "       dp.volume_uom       AS volumeUom, " +
      "       dp.loaded_qty       AS loadedQty " +
      " FROM   dispatch_plan dp " +
      "       LEFT JOIN disp_plan d " +
      "              ON dp.dispatch_plan_id = d.dispatch_plan_id " +
      "       where dp.app_status IN (?1) ORDER BY dp.dispatch_date DESC ) dps",
      countQuery = "select count(*) FROM   dispatch_plan dp " +
          "LEFT JOIN disp_plan d ON dp.dispatch_plan_id = d.dispatch_plan_id " +
          " where dp.app_status IN (?1) ")
  Page<Map<String, Object>> findAllPlansByAppStatusInDPREP(List<String> appStatuses, Pageable pageable);

  /*For EXT_WAREHouse*/
  @Query(nativeQuery = true, value = "select dps.* from(SELECT dp.app_status       AS appStatus, " +
      "       dp.approved_qty     AS approvedQuantity, " +
      "       dp.avail_qty        AS availableQuantity, " +
      "       dp.batch_code       AS batchCode, " +
      "       dp.item_category    AS category, " +
      "       dp.comments         AS comments, " +
      "       dp.deleted_qty      AS deletedApprQuantity, " +
      "       dp.dest_description AS destinationDescription, " +
      "       dp.dest_loc         AS destinationLocation, " +
      "       dp.dispatch_date    AS dispatchDate, " +
      "       dp.dispatched_qty   AS dispatchedQuantity, " +
      "       dp.id               AS id, " +
      "       dp.insert_date      AS insertDate, " +
      "       dp.insert_user      AS insertUser, " +
      "       dp.item_description AS itemDescription, " +
      "       dp.item_id          AS itemId, " +
      "       dp.line_num         AS lineNumber, " +
      "       dp.market_segment   AS marketSegment, " +
      "       d.dispatch_plan_id AS planId, " +
      "       dp.priority         AS priority, " +
      "       dp.quantity         AS quantity, " +
      "       dp.reserved_qty     as reservedQuantity, " +
      "       dp.source_loc       AS sourceLocation, " +
      "       dp.status           AS status, " +
      "       dp.tot_avail_qty    AS totalAvailableQuantity, " +
      "       dp.tte              AS tte, " +
      "       dp.unapp_qty        AS unapprovedQuantity, " +
      "       dp.unapp_del_qty    AS deletedUnApprQuantity, " +
      "       dp.update_date      AS updateDate, " +
      "       dp.update_user      AS updateUser, " +
      "       dp.weight           AS weight," +
      "       dp.weight_uom       AS weightUom," +
      "       dp.volume           AS volume," +
      "       dp.volume_uom       AS volumeUom," +
      "       dp.loaded_qty       AS loadedQty " +
      " FROM   dispatch_plan dp " +
      "       LEFT JOIN disp_plan d " +
      "              ON dp.dispatch_plan_id = d.dispatch_plan_id" +
      "       where dp.source_loc IN (?1) AND  dp.app_status IN (?2) ORDER BY dp.dispatch_date DESC ) dps ",
      countQuery = "select count(*) FROM   dispatch_plan dp " +
          "LEFT JOIN disp_plan d ON dp.dispatch_plan_id = d.dispatch_plan_id " +
          "where dp.source_loc IN (?1) AND  dp.app_status IN (?2) " +
          "AND (dp.source_loc != '1007' OR (dp.source_loc = '1007' AND dp.dest_loc NOT IN(?1)))")
  Page<Map<String, Object>> findAllPlansBySourceLocationInAndAppStatusInEXT(List<String> sourceLocList, List<String> appStatuses, List<String> extWarehouseLocs, Pageable pageable);


  @Query(nativeQuery = true, value = "select dispbom.dispatch_plan_id as dispatchPlanId, dispBom.line_num as lineNumber, dispbom.weight as weight, " +
      "dispBom.volume as volume from dispatch_plan_bom dispBom where dispbom.dispatch_plan_id IN (?1)")
  List<Map<String, Object>> findDispatchPlanBomByPlanIdIn(List<String> dispatchPlanIds);

  @Query(value = "select distinct dp.priority from DispatchPlanItemInfo dp where dp.priority is not null ORDER BY dp.priority ASC ")
  List<Integer> getPriorityList();

  @Query(value = "select distinct dp.priority from DispatchPlanItemInfo dp where dp.priority is not null AND dp.sourceLocation = ?1  ORDER BY dp.priority ASC ")
  List<Integer> getPriorityListForSource(String plantCode);

  @Query(value = "select distinct sourceLocation  from DispatchPlanItemInfo where status = 'OPEN' ORDER BY sourceLocation ASC")
  List<String> findDistinctSourceLoc();
}
