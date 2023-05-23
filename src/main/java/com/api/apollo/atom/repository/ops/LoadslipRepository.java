package com.api.apollo.atom.repository.ops;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.ops.Loadslip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LoadslipRepository extends JpaRepository<Loadslip, String> {

  @Query(nativeQuery = true, value = "select atl_business_flow_pkg.GET_ORDER_TYPE(?1,?2,?3) from dual")
  String findOrderType(String source, String destination, String itemId);

  List<Loadslip> findAllBySourceLocAndStatusIn(String plantCode, List<Constants.LoadslipStatus> status);

  @Query(nativeQuery = true, value = "(select DHI.dhi_count as invoiceCnt, lp.volume_util as volumeUtil, lp.drop_seq as dropSeq, lp.grn_date as grnDate, lp.lr_date as lrDate, lp.lsprint_date as lsPrintDate,lp.arrived_date as bayArrivedDate,  lp.ls_date as loadingStartDate, lp.le_date as loadingEndDate,lp.confirm_date as confirmedDate, lp.release_date as releasedDate,    lp.send_for_barcode_date  as sendForBarcodeDate," +
      " lp .LOADSLIP_TYPE as type, lp.LOADSLIP_ID as loadslipId, lp.SHIPMENT_ID as shipmentId, lp.qty as qty, lp.SOURCE_LOC as sourceLoc, lp.DEST_LOC as destLoc, lp.STO_SO_NUM as stoSoNum, lp.DELIVERY as delivery,lp.SAP_INVOICE as sapInvoice, lp.SAP_INVOICE_DATE as sapInvoiceDate, lp.LR_NUM as lrNum, lp.TOT_TYRES as totTyres, " + "lp.TOT_TUBES as totTubes, lp.TOT_FLAPS as totFlaps, lp.TOT_VALVE as totValve, lp.tot_pctr as totPctr,lp.tot_qty as totQty,lp.GRN as grn, " +
      "lp.STATUS as status, lp.TTE_QTY as tteQty, lp.WEIGHT as TOT_WEIGHT, lp.WEIGHT_UOM as WEIGHT_UOM, lp.ITEM_CATEGORY as itemCategory, lp.TTE_UTIL as tteUtil, lp.WEIGHT_UTIL as weightUtil, lp.INT_STATUS,lp.INT_MESSAGE, shipment.STOP_TYPE as stopType, shipment.FRT_AVAIL_FLAG as freightAvailability,lp.UPDATE_USER as updateUser,lp.INSERT_USER as insertUser, lp.OTHER as otherQty, " +
      "(CASE WHEN shipment.ACTUAL_TRUCK_TYPE IS NULL THEN shipment.TRUCK_TYPE ELSE shipment.ACTUAL_TRUCK_TYPE  END) as truckType, shipment.TRUCK_NUMBER as truckNumber, shipment.SERVPROV as servprov, shipment.START_TIME as gatedOutDate, " +
      "lp.INSERT_DATE as createdDate, lp.MKT_SEG as MKT_SEG, lp.RELEASE_DATE as releaseDate,lp.COMMENTS as comments, lp.DIT_QTY as ditQty, lp.SHORT_QTY as shortQty ,lp.E_WAY_BILL_NO as eWayBillNo ,lp.SAP_INV_VALUE as sapInvValue ," +
      "lp.SAP_INV_WEIGHT as sapInvWeight,shipment.TRANSHIPMENT as transhipment, shipment.DEST_COUNTRY as destCountry, shipment.CONTAINER_NUM as containerNum, shipment.FT_TRIP_ID as FT_TRIP_ID, lp.CUSTOM_INV_NUMBER as CUSTOM_INV_NUMBER   " +
      ",lp.tracking_consent_status as trackingConsentStatus, lp.consent_Phone_Telecom as consentPhoneTelecom " +
          ", lp.go_approval_reason as goApprovalReason " +
      "from Loadslip lp INNER JOIN Shipment shipment on lp.shipment_id = shipment.shipment_id and lp.SOURCE_LOC = ?1 and ( (lp.STATUS in ?2 and lp.LOADSLIP_TYPE not in ( 'FGS_EXP', 'JIT_OEM')) OR (lp.STATUS in ?3 and lp.LOADSLIP_TYPE  in ( 'FGS_EXP', 'JIT_OEM'))) LEFT JOIN (select distinct count(1) as dhi_count, dih.loadslip_id from del_inv_header dih group by dih.loadslip_id ) DHI on lp.LOADSLIP_ID = DHI.loadslip_id) ORDER BY createdDate DESC",
      countQuery = "select count(lp.LOADSLIP_ID) from Loadslip lp INNER JOIN Shipment shipment on lp.shipment_id = shipment.shipment_id and lp.SOURCE_LOC = ?1 and ( (lp.STATUS in ?2 and lp.LOADSLIP_TYPE not in ( 'FGS_EXP', 'JIT_OEM')) OR (lp.STATUS in ?3 and lp.LOADSLIP_TYPE  in ( 'FGS_EXP', 'JIT_OEM'))) LEFT JOIN (select distinct count(1) as dhi_count, dih.loadslip_id from del_inv_header dih group by dih.loadslip_id ) DHI on lp.LOADSLIP_ID = DHI.loadslip_id")
  Page<Map<String, Object>> getLoadSlipsBySourceLocAndStatus(String plantCode, List<String> status, List<String> expoStatus, Pageable pageable);

  /*For DP_REP displaying all the loadslips*/
  @Query(nativeQuery = true, value = "(select DHI.dhi_count as invoiceCnt, lp.volume_util as volumeUtil, lp.drop_seq as dropSeq, lp.grn_date as grnDate, lp.lr_date as lrDate, lp.lsprint_date as lsPrintDate,lp.arrived_date as bayArrivedDate,  lp.ls_date as loadingStartDate, lp.le_date as loadingEndDate,lp.confirm_date as confirmedDate, lp.release_date as releasedDate,    lp.send_for_barcode_date  as sendForBarcodeDate," +
      " lp .LOADSLIP_TYPE as type, lp.LOADSLIP_ID as loadslipId, lp.SHIPMENT_ID as shipmentId, lp.qty as qty, lp.SOURCE_LOC as sourceLoc, lp.DEST_LOC as destLoc, lp.STO_SO_NUM as stoSoNum, lp.DELIVERY as delivery,lp.SAP_INVOICE as sapInvoice, lp.SAP_INVOICE_DATE as sapInvoiceDate, lp.LR_NUM as lrNum, lp.TOT_TYRES as totTyres, " + "lp.TOT_TUBES as totTubes, lp.TOT_FLAPS as totFlaps, lp.TOT_VALVE as totValve, lp.tot_pctr as totPctr,lp.tot_qty as totQty,lp.GRN as grn, " +
      "lp.STATUS as status, lp.TTE_QTY as tteQty, lp.weight as TOT_WEIGHT, lp.WEIGHT_UOM as WEIGHT_UOM, lp.ITEM_CATEGORY as itemCategory, lp.TTE_UTIL as tteUtil, lp.WEIGHT_UTIL as weightUtil, lp.INT_STATUS,lp.INT_MESSAGE, shipment.STOP_TYPE as stopType, shipment.FRT_AVAIL_FLAG as freightAvailability,lp.UPDATE_USER as updateUser,lp.INSERT_USER as insertUser, lp.OTHER as otherQty, " +
      "(CASE WHEN shipment.ACTUAL_TRUCK_TYPE IS NULL THEN shipment.TRUCK_TYPE ELSE shipment.ACTUAL_TRUCK_TYPE  END) as truckType, shipment.TRUCK_NUMBER as truckNumber, shipment.SERVPROV as servprov, shipment.START_TIME as gatedOutDate, lp.INSERT_DATE as createdDate, lp.RELEASE_DATE as releaseDate,lp.COMMENTS as comments, lp.DIT_QTY as ditQty, lp.SHORT_QTY as shortQty ,lp.E_WAY_BILL_NO as eWayBillNo ,lp.SAP_INV_VALUE as sapInvValue ," +
      "lp.SAP_INV_WEIGHT as sapInvWeight,lp.MKT_SEG as MKT_SEG,shipment.TRANSHIPMENT as transhipment, shipment.DEST_COUNTRY as destCountry, shipment.CONTAINER_NUM as containerNum, shipment.FT_TRIP_ID as FT_TRIP_ID, lp.CUSTOM_INV_NUMBER as CUSTOM_INV_NUMBER  " +
          ",lp.tracking_consent_status as trackingConsentStatus, lp.consent_Phone_Telecom as consentPhoneTelecom "  +
          ", lp.go_approval_reason as goApprovalReason " +
          "from Loadslip lp INNER JOIN Shipment shipment on lp.shipment_id = shipment.shipment_id and  ( (lp.STATUS in ?1 and lp.LOADSLIP_TYPE not in ( 'FGS_EXP', 'JIT_OEM')) OR (lp.STATUS in ?2 and lp.LOADSLIP_TYPE  in ( 'FGS_EXP', 'JIT_OEM'))) LEFT JOIN (select distinct count(1) as dhi_count, dih.loadslip_id from del_inv_header dih group by dih.loadslip_id ) DHI on lp.LOADSLIP_ID = DHI.loadslip_id) ORDER BY createdDate DESC",
      countQuery = "select count(lp.LOADSLIP_ID) from Loadslip lp INNER JOIN Shipment shipment on lp.shipment_id = shipment.shipment_id and  ( (lp.STATUS in ?1 and lp.LOADSLIP_TYPE not in ( 'FGS_EXP', 'JIT_OEM')) OR (lp.STATUS in ?2 and lp.LOADSLIP_TYPE  in ( 'FGS_EXP', 'JIT_OEM'))) LEFT JOIN (select distinct count(1) as dhi_count, dih.loadslip_id from del_inv_header dih group by dih.loadslip_id ) DHI on lp.LOADSLIP_ID = DHI.loadslip_id")
  Page<Map<String, Object>> getLoadSlipsByStatusForDPREP(List<String> status, List<String> expoStatus, Pageable pageable);


  @Query(nativeQuery = true, value = "(select distinct ls.*, tr.gatein_date as gateInDate,tr.gateout_date as gateOutDate,tr.reporting_date as reportDate,tr.in_weight as inWeight,tr.out_weight as outWeight,tr.driver_name as driverName, " +
      "tr.driver_mobile as driverMobile,tr.driver_license as driverLicense,tr.COMMENTS as comments  from ( " +
      "select distinct (CASE WHEN lp.LOADSLIP_TYPE in('FGS_EXP', 'JIT_OEM') THEN DHI.dhi_count  ELSE LIH.dhi_count END) as invoiceCnt, lp.volume_util as volumeUtil, lp.drop_seq as dropSeq, lp.grn_date as grnDate, lp.lr_date as lrDate, lp.lsprint_date as lsPrintDate,lp.arrived_date as bayArrivedDate,  lp.ls_date as loadingStartDate, " +
      "lp.le_date as loadingEndDate,lp.confirm_date as confirmedDate, lp.release_date as releasedDate,    lp.send_for_barcode_date  as sendForBarcodeDate,lp .LOADSLIP_TYPE as type, lp.LOADSLIP_ID as loadslipId, " +
      "lp.SHIPMENT_ID as shipmentId, lp.qty as qty, lp.SOURCE_LOC as sourceLoc, lp.DEST_LOC as destLoc, lp.STO_SO_NUM as stoSoNum, lp.DELIVERY as delivery,lp.SAP_INVOICE as sapInvoice, lp.SAP_INVOICE_DATE as sapInvoiceDate, " +
      "lp.LR_NUM as lrNum, lp.TOT_TYRES as totTyres,lp.TOT_TUBES as totTubes, lp.TOT_FLAPS as totFlaps, lp.TOT_VALVE as totValve, lp.tot_pctr as totPctr,lp.tot_qty as totQty,lp.GRN as grn, lp.STATUS as status, " +
      "lp.TTE_QTY as tteQty, lp.ITEM_CATEGORY as itemCategory, lp.MKT_SEG as MKT_SEG, lp.TTE_UTIL as tteUtil, lp.WEIGHT_UTIL as weightUtil, lp.INT_STATUS,lp.INT_MESSAGE, shipment.STOP_TYPE as stopType, shipment.FRT_AVAIL_FLAG as freightAvailability, " +
      "lp.UPDATE_USER as updateUser,lp.INSERT_USER as insertUser,(CASE WHEN shipment.ACTUAL_TRUCK_TYPE IS NULL THEN shipment.TRUCK_TYPE ELSE shipment.ACTUAL_TRUCK_TYPE  END) as truckType, " +
      "shipment.TRUCK_NUMBER as truckNumber, shipment.SERVPROV as servprov, shipment.START_TIME as gatedOutDate, lp.INSERT_DATE as createdDate, lp.RELEASE_DATE as releaseDate,lp.COMMENTS as loadslipcomments, lp.DIT_QTY as ditQty, " +
      "lp.SHORT_QTY as shortQty ,lp.E_WAY_BILL_NO as eWayBillNo ,lp.SAP_INV_VALUE as sapInvValue ,lp.SAP_INV_WEIGHT as sapInvWeight ,lp.other as otherQty ,shipment.TRANSHIPMENT as transhipment ," +
      "shipment.CONTAINER_NUM AS containerNum,shipment.DEST_COUNTRY as destCountry,lp.WEIGHT as TOT_WEIGHT, lp.WEIGHT_UOM as WEIGHT_UOM, shipment.ft_trip_id as FT_TRIP_ID, lp.CUSTOM_INV_NUMBER as CUSTOM_INV_NUMBER " +
          ",lp.tracking_consent_status as trackingConsentStatus, lp.consent_Phone_Telecom as consentPhoneTelecom "  +
          ", lp.go_approval_reason as goApprovalReason " +
          "from Loadslip lp INNER JOIN Shipment shipment on lp.shipment_id = shipment.shipment_id  and ( (lp.STATUS in ?1 and lp.LOADSLIP_TYPE not in ( 'FGS_EXP', 'JIT_OEM')) OR (lp.STATUS in ?2 and lp.LOADSLIP_TYPE  in ( 'FGS_EXP', 'JIT_OEM'))) " +
      "left join (select distinct count(1) as dhi_count, dih.loadslip_id from del_inv_header dih group by dih.loadslip_id ) DHI on lp.loadslip_id = DHI.loadslip_id  " +
      "left join (select distinct count(1) as dhi_count, dih.loadslip_id from loadslip_inv_header dih group by dih.loadslip_id) LIH on lp.loadslip_id = LIH.loadslip_id ) ls left join  truck_reporting tr on ls.shipmentId = tr.SHIPMENT_ID and tr.reporting_location = ls.sourceLoc  ORDER BY ls.createdDate DESC)"
      ,countQuery = "select count(*)  from ( select lp.SHIPMENT_ID as shipmentId,lp.source_loc AS sourceLoc from Loadslip lp INNER JOIN Shipment shipment on lp.shipment_id = shipment.shipment_id  and ( (lp.STATUS in ?1 and lp.LOADSLIP_TYPE not in ( 'FGS_EXP', 'JIT_OEM'))  " +
      "OR (lp.STATUS in ?2 and lp.LOADSLIP_TYPE  in ( 'FGS_EXP', 'JIT_OEM'))) LEFT JOIN (select distinct count(1) as dhi_count, dih.loadslip_id from del_inv_header dih group by dih.loadslip_id ) DHI on lp.LOADSLIP_ID = DHI.loadslip_id ) ls  left join truck_reporting tr on ls.shipmentId = tr.SHIPMENT_ID and tr.reporting_location = ls.sourceLoc   ")
  Page<Map<String, Object>> getLoadSlipsMovementBySourceLocAndStatusDprep(List<String> status, List<String> expoStatus, Pageable pageable);

  @Query(nativeQuery = true, value = "(select distinct ls.*, tr.gatein_date as gateInDate,tr.gateout_date as gateOutDate,tr.reporting_date as reportDate,tr.in_weight as inWeight,tr.out_weight as outWeight,tr.driver_name as driverName, " +
      "tr.driver_mobile as driverMobile,tr.driver_license as driverLicense,tr.COMMENTS as comments  from ( " +
      "select distinct (CASE WHEN lp.LOADSLIP_TYPE in('FGS_EXP', 'JIT_OEM') THEN DHI.dhi_count  ELSE LIH.dhi_count END) as invoiceCnt, lp.volume_util as volumeUtil, lp.drop_seq as dropSeq, lp.grn_date as grnDate, lp.lr_date as lrDate, lp.lsprint_date as lsPrintDate,lp.arrived_date as bayArrivedDate,  lp.ls_date as loadingStartDate, " +
      "lp.le_date as loadingEndDate,lp.confirm_date as confirmedDate, lp.release_date as releasedDate,    lp.send_for_barcode_date  as sendForBarcodeDate,lp .LOADSLIP_TYPE as type, lp.LOADSLIP_ID as loadslipId, " +
      "lp.SHIPMENT_ID as shipmentId, lp.qty as qty, lp.SOURCE_LOC as sourceLoc, lp.DEST_LOC as destLoc, lp.STO_SO_NUM as stoSoNum, lp.DELIVERY as delivery,lp.SAP_INVOICE as sapInvoice, lp.SAP_INVOICE_DATE as sapInvoiceDate, " +
      "lp.LR_NUM as lrNum, lp.TOT_TYRES as totTyres,lp.TOT_TUBES as totTubes, lp.TOT_FLAPS as totFlaps, lp.TOT_VALVE as totValve, lp.tot_pctr as totPctr,lp.tot_qty as totQty,lp.GRN as grn, lp.STATUS as status, " +
      "lp.TTE_QTY as tteQty, lp.ITEM_CATEGORY as itemCategory, lp.MKT_SEG as MKT_SEG, lp.TTE_UTIL as tteUtil, lp.WEIGHT_UTIL as weightUtil, lp.INT_STATUS,lp.INT_MESSAGE, shipment.STOP_TYPE as stopType, shipment.FRT_AVAIL_FLAG as freightAvailability, " +
      "lp.UPDATE_USER as updateUser,lp.INSERT_USER as insertUser,(CASE WHEN shipment.ACTUAL_TRUCK_TYPE IS NULL THEN shipment.TRUCK_TYPE ELSE shipment.ACTUAL_TRUCK_TYPE  END) as truckType, " +
      "shipment.TRUCK_NUMBER as truckNumber, shipment.SERVPROV as servprov, shipment.START_TIME as gatedOutDate, lp.INSERT_DATE as createdDate, lp.RELEASE_DATE as releaseDate,lp.COMMENTS as loadslipcomments, lp.DIT_QTY as ditQty, " +
      "lp.SHORT_QTY as shortQty ,lp.E_WAY_BILL_NO as eWayBillNo ,lp.SAP_INV_VALUE as sapInvValue ,lp.SAP_INV_WEIGHT as sapInvWeight ,lp.other as otherQty ,shipment.TRANSHIPMENT as transhipment ," +
      "shipment.CONTAINER_NUM AS containerNum ,shipment.DEST_COUNTRY as destCountry,lp.WEIGHT as TOT_WEIGHT, lp.WEIGHT_UOM as WEIGHT_UOM, shipment.ft_trip_id as FT_TRIP_ID, lp.CUSTOM_INV_NUMBER as CUSTOM_INV_NUMBER " +
          ",lp.tracking_consent_status as trackingConsentStatus, lp.consent_Phone_Telecom as consentPhoneTelecom "  +
          ", lp.go_approval_reason as goApprovalReason " +
          "from Loadslip lp INNER JOIN Shipment shipment on lp.shipment_id = shipment.shipment_id and lp.SOURCE_LOC =?1 and ( (lp.STATUS in ?2 and lp.LOADSLIP_TYPE not in ( 'FGS_EXP', 'JIT_OEM')) OR (lp.STATUS in ?3 and lp.LOADSLIP_TYPE  in ( 'FGS_EXP', 'JIT_OEM'))) " +
      "left join (select distinct count(1) as dhi_count, dih.loadslip_id from del_inv_header dih group by dih.loadslip_id ) DHI on lp.loadslip_id = DHI.loadslip_id " +
      "left join (select distinct count(1) as dhi_count, dih.loadslip_id from loadslip_inv_header dih group by dih.loadslip_id) LIH on lp.loadslip_id = LIH.loadslip_id ) ls left join  truck_reporting tr  ON ls.shipmentid = tr.shipment_id AND (tr.reporting_location =?1 OR tr.reporting_location IS NULL))ORDER BY ls.createdDate desc "
      ,countQuery = "select count(*)  from ( select lp.SHIPMENT_ID as shipmentId from Loadslip lp INNER JOIN Shipment shipment on lp.shipment_id = shipment.shipment_id  and lp.SOURCE_LOC = ?1 and ( (lp.STATUS in ?2 and lp.LOADSLIP_TYPE not in ( 'FGS_EXP', 'JIT_OEM'))  " +
      "OR (lp.STATUS in ?3 and lp.LOADSLIP_TYPE  in ( 'FGS_EXP', 'JIT_OEM'))) LEFT JOIN (select distinct count(1) as dhi_count, dih.loadslip_id from del_inv_header dih group by dih.loadslip_id ) DHI on lp.LOADSLIP_ID = DHI.loadslip_id ) ls  left join truck_reporting tr on ls.shipmentId = tr.SHIPMENT_ID  and (tr.reporting_location=?1 or tr.reporting_location is null) ")
  Page<Map<String, Object>> getLoadSlipsMovementBySourceLocAndStatus(String plantCode, List<String> status, List<String> expoStatus, Pageable pageable);

  @Query("SELECT ls.qty FROM Loadslip ls WHERE ls.loadslipId in ?1")
  List<Integer> findQtyByLoadslipIdIn(List<String> slipDetailIds);

  @Query(nativeQuery = true, value = "SELECT Sum(tube_qty)  AS tube_qty,Sum(flap_qty)  AS flap_qty,Sum(valve_qty) AS valve_qty " +
      "FROM   loadslip_detail_bom WHERE  line_no IN(SELECT line_no FROM   loadslip_detail  WHERE  loadslip_id = ?1  AND scannable = 'N')  AND loadslip_id = ?1")
  Map<String, Object> findFubeQtyFlapQtyValveQtySum(String loadslipId);


  List<Loadslip> findAllByShipmentShipmentIdAndStatusNot(String shipmentID, Constants.LoadslipStatus status);

  List<Loadslip> findAllByShipmentShipmentId(String shipmentID);

  List<Loadslip> findAllByShipmentShipmentIdAndSourceLocAndStatusNot(String shipmentID, String sourceLoc, Constants.LoadslipStatus status);

  List<Loadslip> findAllByShipmentShipmentIdAndDestLocAndStatusNot(String shipmentID, String destLoc, Constants.LoadslipStatus status);

  List<Loadslip> findAllByShipmentShipmentIdInAndDestLocAndStatusNot(List<String> shipmentIds, String destLoc, Constants.LoadslipStatus status);

  List<Loadslip> findAllByShipmentShipmentIdInAndStatusNot(List<String> shipmentIds, Constants.LoadslipStatus status);


  @Transactional
  @Modifying
  @Query(nativeQuery = true, value = "delete from loadslip_line_detail where loadslip_id = ?1")
  void deleteLoadslipLineDetail(String loadslipId);

  @Transactional
  @Modifying
  @Query(nativeQuery = true, value = "delete from loadslip_detail where loadslip_id = ?1")
  void deleteLoadslipByLoadslipId(String loadslipID);

  @Transactional
  @Modifying
  @Query(nativeQuery = true, value = "delete from loadslip_detail where loadslip_id = ?1 and item_id not in = ?2")
  void deleteLoadslipDetail(String loadslipId, List<String> itemId);

  Optional<Loadslip> findByLoadslipIdAndStatusNot(String loadslipID, Constants.LoadslipStatus status);

  Optional<Loadslip> findByLoadslipId(String loadslipID);


  List<Loadslip> findByShipmentShipmentId(String shipmentId);


  @Query(value = "select * from (select nvl(sum(invoice_quantity),0) as tot_inv_qty,nvl(sum(loadslip_quantity),0) tot_ls_qty from table(atl_business_flow_pkg.loadslip_dashboard(?1)))", nativeQuery = true)
  Map<String, Object> checkEqualityInvoiceQtyAndLoadedQty(String loadslipId);

  //view loadslip invoice data procedure
//   @Query(value = "select * from table(atl_business_flow_pkg.loadslip_dashboard(?1))",nativeQuery = true)
  @Query(value = "select * from table(atl_business_flow_pkg.loadslip_dashboard_ui(?1))", nativeQuery = true)
  List<Map<String, Object>> getLoadslipInvoiceData(String loadslipId);


  @Query(value = "select l.loadslipId as loadslipId, l.loadslipType as loadslipType, l.stoSoNum as stoSoNum, ld.scannedQty as scannedQty from Loadslip l " +
      " join LoadslipDetail ld on ld.loadslip = l.loadslipId " +
      " where l.loadslipId IN (?1)")
  List<Map<String, Object>> findAllLoadslipDetails(List<String> loadslipIds);

  @Query(nativeQuery = true, value ="select l.SHIPMENT_ID as shipmentId,l.COMMENTS as comments, l.lr_num as lrNum, l.lr_Date as lrDate, " +
      " l.tot_tyres as totalTyres, l.tot_tubes as totalTubes, l.tot_flaps as totalFlap, l.tot_valve as totalValve, l.tot_pctr as totalPctr, l.other as other," +
      " l.tot_qty as totalQty, l.sap_invoice as sapInv from LOADSLIP l where l.SHIPMENT_ID IN (?1) AND l.status != 'CANCELLED'")
  List<Map<String,Object>> findAllShipmentIdIn(List<String> shipmentIds);


  List<Loadslip> findCommentsByShipmentShipmentId(String shipmentId);

  @Query(nativeQuery = true,value = " select distinct lp.source_loc as sourceLoc, lp.dest_loc as destLoc from Loadslip lp where lp.source_loc IN (?1) AND lp.status NOT IN (?2)")
  List<Map<String,Object>> findAllBySourceLocIn(List<String> sourceList, List<String> completedLoadslipStatus);

  @Query(value = "select new com.api.apollo.atom.entity.ops.Loadslip(l.loadslipId, l.status) from Loadslip l where l.loadslipId IN (?1)")
  List<Loadslip> findAllByLoadslipIdIn(List<String> loadslipIdList);

  @Query(nativeQuery = true, value = "select " +
      "case " +
      "when mt_elr.elr_flag = 'N' then 'N'" +
      "when mt_elr.elr_flag = 'Y' then 'Y'" +
      "else 'N'" +
      "end as elr from" +
      " MT_ELR , (select ls.source_loc, ship.servprov from loadslip ls, shipment ship where ls.shipment_id = ship.shipment_id and ls.loadslip_id = ?1) " +
      "elr_data where MT_ELR.location_id = elr_data.source_loc and MT_ELR.servprov = elr_data.servprov")
  String getElrFlag(String loadslipId);

  @Query(value = "select distinct itemCategory from Loadslip where itemCategory is not null ORDER BY itemCategory ASC")
  List<String> getDistinctCategory();
}
