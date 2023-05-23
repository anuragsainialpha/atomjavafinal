package com.api.apollo.atom.repository.ops;

import com.api.apollo.atom.constant.Constants.TruckReportStatus;
import com.api.apollo.atom.entity.ops.TruckReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TruckReportRepository extends JpaRepository<TruckReport, String> {

  List<TruckReport> findByIndentSummaryId(String indentId);

  @Query(value = "select tr from TruckReport tr where tr.indentSummary.indentId In (?1)")
  List<TruckReport> findByIndentSummaryIdIn(List<String> indentIds);

  List<TruckReport> findByTruckTruckNumberAndStatusAndReportLocation(String truckNumber, TruckReportStatus truckReportStatus, String reportLocation);

  List<TruckReport> findByTruckTruckNumberAndStatusIn(String truckNumber, List<TruckReportStatus> statuses);

  boolean existsByTruckTruckNumberAndReportLocationAndStatusAndRejectedStatus(String truckNumber, String location, TruckReportStatus status, TruckReportStatus rejectedStatus);

  boolean existsByTruckTruckNumberAndStatusInAndReportLocation(String truckNumber, List<TruckReportStatus> status, String reportLocation);

  @Query(nativeQuery = true, value = "SELECT tr.truck_number,tr.truck_type,tr.actual_truck_type, tr.servprov  FROM truck_reporting tr WHERE tr.status = ?1 AND tr.status <> ?2 AND tr.source_loc = ?3 AND tr.dest_loc = ?4 AND tr.REPORTING_LOCATION = ?5")
  List<Map<String, String>> findTrucksByStatusAndStatusNotAndSourceAndDestLocAndReportingLoc(String status, String status2, String sourceLoc, String destLoc, String reportLoc);

  /*To get reported trucks with shipTo locations*/
  @Query(nativeQuery = true, value = "SELECT tr.truck_number,tr.truck_type,tr.actual_truck_type, tr.servprov,isu.item_category  FROM truck_reporting tr LEFT JOIN indent_summary isu on isu.indent_id =tr.indent_id  WHERE tr.status = ?1 AND tr.status <> ?2 AND tr.source_loc = ?3 AND tr.dest_loc IN (?4) AND tr.REPORTING_LOCATION = ?5")
  List<Map<String, String>> findTrucksByStatusAndStatusNotAndSourceAndDestLocInAndReportingLoc(String status, String status2, String sourceLoc, List<String> destLocList, String reportLoc);

	/*To get reported trucks with shipTo locations and itemCategory*/
	@Query(nativeQuery = true, value = "SELECT tr.truck_number,tr.truck_type,tr.actual_truck_type, tr.servprov  FROM truck_reporting tr LEFT JOIN indent_summary ids on ids.indent_id=tr.indent_id WHERE tr.status = ?1 AND tr.status <> ?2 AND tr.source_loc = ?3 AND tr.dest_loc IN (?4) AND tr.REPORTING_LOCATION = ?5 and (ids.item_category=?6 or ids.item_category is null)")
	List<Map<String, String>> findTrucksByStatusAndStatusNotAndSourceAndDestLocInAndReportingLocAndItemCategory(String status, String status2, String sourceLoc, List<String> destLocList, String reportLoc, String itemCategory);


	@Query(nativeQuery = true, value = "SELECT tr.truck_number,tr.truck_type, tr.container_num , tr.dest_loc, tr.servprov,c.country_name as countryName," +
      " (CASE WHEN (select mt.location_desc from mt_location mt where mt.location_id = tr.dest_loc) is not null THEN (select mt.location_desc from mt_location mt where mt.location_id = tr.dest_loc)\n" +
      "ELSE (select ct.cust_name from mt_customer ct where ct.cust_id = tr.dest_loc)\n" +
      "END ) AS dest_desc, i.item_category" +
      " FROM truck_reporting tr " +
      " left join ct_country c on c.country_code = tr.dest_country " +
      " LEFT JOIN INDENT_SUMMARY i on tr.indent_id = i.indent_id " +
      " WHERE tr.status = ?1 AND tr.status <> ?2 AND tr.source_loc = ?3 AND tr.dest_loc = ?4 AND tr.REPORTING_LOCATION = ?5 and tr.container_num is not null")
  List<Map<String, String>> findTrucksByStatusAndStatusNotAndSourceAndDestLocAndReportingLocWithContainerNo(String status, String status2, String sourceLoc, String destLoc, String reportLoc);

  @Query(nativeQuery = true, value = "SELECT tr.truck_number,tr.truck_type, tr.container_num , tr.dest_loc, tr.servprov,c.country_name as countryName," +
      " (CASE WHEN (select mt.location_desc from mt_location mt where mt.location_id = tr.dest_loc) is not null THEN (select mt.location_desc from mt_location mt where mt.location_id = tr.dest_loc) " +
      " ELSE (select ct.cust_name from mt_customer ct where ct.cust_id = tr.dest_loc) " +
      " END ) AS dest_desc, i.item_category" +
      " FROM truck_reporting tr " +
      " left join ct_country c on c.country_code = tr.dest_country  " +
      " LEFT JOIN INDENT_SUMMARY i on tr.indent_id = i.indent_id " +
      " WHERE tr.status = ?1 AND tr.status <> ?2 AND tr.source_loc = ?3 AND tr.REPORTING_LOCATION = ?4 and tr.container_num is not null")
  List<Map<String, String>> findTrucksByStatusAndStatusNotAndSourceAndDestLocAndReportingLocWithContainerNoNotDestLoc(String status, String status2, String sourceLoc, String reportLoc);

  @Query(nativeQuery = true, value = "SELECT tr.truck_number,tr.truck_type, tr.container_num , tr.dest_loc, tr.servprov," +
      " (CASE WHEN (select mt.location_desc from mt_location mt where mt.location_id = tr.dest_loc) is not null THEN (select mt.location_desc from mt_location mt where mt.location_id = tr.dest_loc)\n" +
      "ELSE (select ct.cust_name from mt_customer ct where ct.cust_id = tr.dest_loc)\n" +
      "END ) AS dest_desc, i.item_category,c.country_name as countryName" +
      " FROM truck_reporting tr " +
      " left join ct_country c on c.country_code = tr.dest_country" +
      " LEFT JOIN INDENT_SUMMARY i on tr.indent_id = i.indent_id " +
      " WHERE tr.status = ?1 AND tr.status <> ?2 AND tr.source_loc = ?3 AND tr.dest_loc = ?4 AND tr.REPORTING_LOCATION = ?5")
  List<Map<String, String>> findTrucksByStatusAndStatusNotAndSourceAndDestLocAndReportingLocWithContainerNoAll(String status, String status2, String sourceLoc, String destLoc, String reportLoc);

  @Query(nativeQuery = true, value = "SELECT tr.truck_number,tr.truck_type, tr.container_num , tr.dest_loc, tr.servprov, " +
      " (CASE WHEN (select mt.location_desc from mt_location mt where mt.location_id = tr.dest_loc) is not null THEN (select mt.location_desc from mt_location mt where mt.location_id = tr.dest_loc) " +
      " ELSE (select ct.cust_name from mt_customer ct where ct.cust_id = tr.dest_loc) " +
      " END ) AS dest_desc, i.item_category,c.country_name as countryName" +
      "  FROM truck_reporting tr " +
      " left join ct_country c on c.country_code = tr.dest_country" +
      " LEFT JOIN INDENT_SUMMARY i on tr.indent_id = i.indent_id " +
      "WHERE tr.status = ?1 AND tr.status <> ?2 AND tr.source_loc = ?3 AND tr.REPORTING_LOCATION = ?4")
  List<Map<String, String>> findTrucksByStatusAndStatusNotAndSourceAndDestLocAndReportingLocWithContainerNoNotDestLocAll(String status, String status2, String sourceLoc, String reportLoc);


  Page<TruckReport> findAllByReportLocationAndStatusInOrderByReportDateAsc(String source, List<TruckReportStatus> status, Pageable pageable);

  //considering only activity when we get type is d
  Page<TruckReport> findAllByReportLocationAndStatusInAndActivityOrderByReportDateAsc(String source, List<TruckReportStatus> status, String activity, Pageable pageable);

  @Query(nativeQuery = true, value ="select tr.GATE_CONTROL_CODE as GATE_CONTROL_CODE,tr.TYPE as TYPE,tr.TRANSPORTER_SAP_CODE as TRANSPORTER_SAP_CODE,tr.CONTAINER_NUM as CONTAINER_NUM," +
      "tr.CONTAINER_CODE as CONTAINER_CODE,tr.DRIVER_NAME as DRIVER_NAME,tr.DRIVER_MOBILE as DRIVER_MOBILE,tr.DRIVER_LICENSE as DRIVER_LICENSE,tr.SERVPROV as SERVPROV," +
      "tr.TRUCK_TYPE as TRUCK_TYPE,tr.REPORTING_LOCATION as REPORTING_LOCATION,tr.SOURCE_LOC as SOURCE_LOC,tr.DEST_LOC as DEST_LOC,tr.REPORTING_DATE as REPORTING_DATE," +
      "tr.GATEIN_DATE as GATEIN_DATE,tr.GATEOUT_DATE as GATEOUT_DATE ,tr.DEST_GEOFENCEIN_DATE as DEST_GEOFENCEIN_DATE,tr.DEREPORTING_DATE as DEREPORTING_DATE,tr.REP_GI_HRS as REP_GI_HRS," +
      "tr.GI_GO_HRS as GI_GO_HRS,tr.STATUS as STATUS,tr.REJ_STATUS as REJ_STATUS,tr.IN_WEIGHT as IN_WEIGHT,tr.OUT_WEIGHT as OUT_WEIGHT,tr.NET_WEIGHT as NET_WEIGHT ,tr.BAY as BAY," +
      "tr.IS_PUC as IS_PUC, tr.IS_INSURANCE as IS_INSURANCE,tr.IS_SEATBELT as IS_SEATBELT,tr.IS_FIRST_AID as IS_FIRST_AID,tr.IS_FIRE_EXTENGUISHER as IS_FIRE_EXTENGUISHER," +
      "tr.IS_EMERGENCY_CARD as IS_EMERGENCY_CARD,tr.IS_SPARK_ARRESTOR as IS_SPARK_ARRESTOR,tr.IS_FITNESS_CERT as IS_FITNESS_CERT,"+
       "tr.TRUCK_CAPACITY as TRUCK_CAPACITY, tr.fuel_type as FUEL_TYPE, tr.bs_norms as BS_NORMS, " +
          "tr.BAY_STATUS as BAY_STATUS,tr.REJECTION_CODE as REJECTION_CODE,tr.ACTUAL_TRUCK_TYPE as ACTUAL_TRUCK_TYPE,tr.REF_CODE as REF_CODE,tr.INSERT_USER as INSERT_USER,tr.UPDATE_USER as UPDATE_USER," +
      "tr.INSERT_DATE as INSERT_DATE,tr.UPDATE_DATE as UPDATE_DATE,tr.Shipment_ID as Shipment_ID,tr.ETA_DESTINATION as ETA_DESTINATION,tr.REPORTED_TRUCK_TYPE as REPORTED_TRUCK_TYPE,tr.ACTIVITY as ACTIVITY, sh.FT_TRIP_ID as FT_TRIP_ID," +
      "tr.COMMENTS as COMMENTS,mt.TRUCK_NUMBER as TRUCK_NUMBER,mt.GPS_ENABLED as GPS_ENABLED,ct.GPS_PROVIDER as GPS_PROVIDER,i.INDENT_ID as INDENT_ID , tr.E_WAY_BILL_NO as E_WAY_BILL_NO ,tr.TRACKING_CONSENT_STATUS as TRACKING_CONSENT_STATUS," +
          " tr.CONSENT_PHONE_TELECOM as CONSENT_PHONE_TELECOM, " +
      " tr.SAP_INV_VALUE as SAP_INV_VALUE ,tr.SAP_INV_WEIGHT as SAP_INV_WEIGHT, tr.DEST_COUNTRY as DEST_COUNTRY, i.item_category as indentCategory from Truck_Reporting tr " +
      "left join MT_Truck mt on mt.truck_Number=tr.truck_number" +
      " left join ct_gps ct on ct.gps_Provider=mt.gps_Provider " +
      "left join Indent_Summary i on i.indent_Id = tr.indent_id" +
      " left join Shipment sh on tr.shipment_ID = sh.shipment_Id " +
      "  where  tr.REPORTING_LOCATION = ?1 and tr.status IN (?2) and (tr.activity = ?3)",
      countQuery = "select count(tr.gate_Control_Code) from Truck_Reporting tr left join MT_Truck mt on mt.truck_Number = tr.truck_number left join ct_gps gps on gps.gps_Provider = mt.gps_Provider left join Indent_Summary i on i.indent_Id = tr.indent_id where tr.REPORTING_LOCATION = ?1 and tr.status IN (?2) and (tr.activity = ?3) ")
  Page<Map<String, Object>> findAByReportLocationAndStatusInAndActivityOrderByReportDateAsc(String source, List<String> status, String activity, Pageable pageable);

  /*For User With Role DP_REP we are not considering reportedLocation*/
  @Query(nativeQuery = true, value = "select tr.GATE_CONTROL_CODE as GATE_CONTROL_CODE,tr.TYPE as TYPE,tr.TRANSPORTER_SAP_CODE as TRANSPORTER_SAP_CODE,tr.CONTAINER_NUM as CONTAINER_NUM," +
      "tr.CONTAINER_CODE as CONTAINER_CODE,tr.DRIVER_NAME as DRIVER_NAME,tr.DRIVER_MOBILE as DRIVER_MOBILE,tr.DRIVER_LICENSE as DRIVER_LICENSE,tr.SERVPROV as SERVPROV," +
      "tr.TRUCK_TYPE as TRUCK_TYPE,tr.REPORTING_LOCATION as REPORTING_LOCATION,tr.SOURCE_LOC as SOURCE_LOC,tr.DEST_LOC as DEST_LOC,tr.REPORTING_DATE as REPORTING_DATE," +
      "tr.GATEIN_DATE as GATEIN_DATE,tr.GATEOUT_DATE as GATEOUT_DATE ,tr.DEST_GEOFENCEIN_DATE as DEST_GEOFENCEIN_DATE,tr.DEREPORTING_DATE as DEREPORTING_DATE,tr.REP_GI_HRS as REP_GI_HRS," +
      "tr.GI_GO_HRS as GI_GO_HRS,tr.STATUS as STATUS,tr.REJ_STATUS as REJ_STATUS,tr.IN_WEIGHT as IN_WEIGHT,tr.OUT_WEIGHT as OUT_WEIGHT,tr.NET_WEIGHT as NET_WEIGHT ,tr.BAY as BAY," +
      "tr.IS_PUC as IS_PUC, tr.IS_INSURANCE as IS_INSURANCE,tr.IS_SEATBELT as IS_SEATBELT,tr.IS_FIRST_AID as IS_FIRST_AID,tr.IS_FIRE_EXTENGUISHER as IS_FIRE_EXTENGUISHER," +
      "tr.IS_EMERGENCY_CARD as IS_EMERGENCY_CARD,tr.IS_SPARK_ARRESTOR as IS_SPARK_ARRESTOR,tr.IS_FITNESS_CERT as IS_FITNESS_CERT,"+
          "tr.TRUCK_CAPACITY as TRUCK_CAPACITY,  tr.fuel_type as FUEL_TYPE, tr.bs_norms as BS_NORMS," +
          "tr.BAY_STATUS as BAY_STATUS,tr.REJECTION_CODE as REJECTION_CODE,tr.ACTUAL_TRUCK_TYPE as ACTUAL_TRUCK_TYPE,tr.REF_CODE as REF_CODE,tr.INSERT_USER as INSERT_USER,tr.UPDATE_USER as UPDATE_USER," +
      "tr.INSERT_DATE as INSERT_DATE,tr.UPDATE_DATE as UPDATE_DATE,tr.Shipment_ID as Shipment_ID,tr.ETA_DESTINATION as ETA_DESTINATION,tr.REPORTED_TRUCK_TYPE as REPORTED_TRUCK_TYPE,tr.ACTIVITY as ACTIVITY, sh.FT_TRIP_ID as FT_TRIP_ID," +
      "tr.COMMENTS as COMMENTS,mt.TRUCK_NUMBER as TRUCK_NUMBER,mt.GPS_ENABLED as GPS_ENABLED,ct.GPS_PROVIDER as GPS_PROVIDER,i.INDENT_ID as INDENT_ID , tr.E_WAY_BILL_NO as E_WAY_BILL_NO ," +
      " tr.SAP_INV_VALUE as SAP_INV_VALUE ,tr.SAP_INV_WEIGHT as SAP_INV_WEIGHT, tr.DEST_COUNTRY as DEST_COUNTRY, i.item_category as indentCategory, tr.TRACKING_CONSENT_STATUS as TRACKING_CONSENT_STATUS," +
          " tr.CONSENT_PHONE_TELECOM as CONSENT_PHONE_TELECOM from Truck_Reporting tr " +
      "left join MT_Truck mt on mt.truck_Number=tr.truck_number" +
      " left join ct_gps ct on ct.gps_Provider=mt.gps_Provider " +
      "left join Indent_Summary i on i.indent_Id = tr.indent_id" +
      " left join Shipment sh on tr.shipment_ID = sh.shipment_Id " +
      "  where tr.status IN (?1) and (tr.activity = ?2)",
      countQuery = "select count(tr.gate_Control_Code) from Truck_Reporting tr left join MT_Truck mt on mt.truck_Number = tr.truck_number left join ct_gps gps on gps.gps_Provider = mt.gps_Provider left join Indent_Summary i on i.indent_Id = tr.indent_id  where tr.status IN (?1) and (tr.activity = ?2)")
  Page<Map<String, Object>> findAByReportLocationAndStatusInAndActivityOrderByReportDateAscDPREP(List<String> status, String activity, Pageable pageable);

  //considering activity and null when we get activity type is p
  Page<TruckReport> findAllByReportLocationAndStatusInAndActivityOrActivityIsNullOrderByReportDateAsc(String source, List<TruckReportStatus> status, String activity, Pageable pageable);

  @Query(value = "select tr from TruckReport tr where tr.reportLocation = ?1 and tr.status IN (?2) and (tr.activity is null or tr.activity = ?3)")
  Page<TruckReport> findTruckReportsWithActivityP(String source, List<String> status, String activity, Pageable pageable);

  @Query(nativeQuery = true, value = "select tr.GATE_CONTROL_CODE as GATE_CONTROL_CODE,tr.TYPE as TYPE,tr.TRANSPORTER_SAP_CODE as TRANSPORTER_SAP_CODE,tr.CONTAINER_NUM as CONTAINER_NUM," +
      "tr.CONTAINER_CODE as CONTAINER_CODE,tr.DRIVER_NAME as DRIVER_NAME,tr.DRIVER_MOBILE as DRIVER_MOBILE,tr.DRIVER_LICENSE as DRIVER_LICENSE,tr.SERVPROV as SERVPROV," +
      "tr.TRUCK_TYPE as TRUCK_TYPE,tr.REPORTING_LOCATION as REPORTING_LOCATION,tr.SOURCE_LOC as SOURCE_LOC,tr.DEST_LOC as DEST_LOC,tr.REPORTING_DATE as REPORTING_DATE," +
      "tr.GATEIN_DATE as GATEIN_DATE,tr.GATEOUT_DATE as GATEOUT_DATE ,tr.DEST_GEOFENCEIN_DATE as DEST_GEOFENCEIN_DATE,tr.DEREPORTING_DATE as DEREPORTING_DATE,tr.REP_GI_HRS as REP_GI_HRS," +
      "tr.GI_GO_HRS as GI_GO_HRS,tr.STATUS as STATUS,tr.REJ_STATUS as REJ_STATUS,tr.IN_WEIGHT as IN_WEIGHT,tr.OUT_WEIGHT as OUT_WEIGHT,tr.NET_WEIGHT as NET_WEIGHT ,tr.BAY as BAY," +
      "tr.IS_PUC as IS_PUC, tr.IS_INSURANCE as IS_INSURANCE,tr.IS_SEATBELT as IS_SEATBELT,tr.IS_FIRST_AID as IS_FIRST_AID,tr.IS_FIRE_EXTENGUISHER as IS_FIRE_EXTENGUISHER," +
      "tr.IS_EMERGENCY_CARD as IS_EMERGENCY_CARD,tr.IS_SPARK_ARRESTOR as IS_SPARK_ARRESTOR,tr.IS_FITNESS_CERT as IS_FITNESS_CERT,"+
          "tr.TRUCK_CAPACITY as TRUCK_CAPACITY,  tr.fuel_type as FUEL_TYPE, tr.bs_norms as BS_NORMS," +
          "tr.BAY_STATUS as BAY_STATUS,tr.REJECTION_CODE as REJECTION_CODE,tr.ACTUAL_TRUCK_TYPE as ACTUAL_TRUCK_TYPE,tr.REF_CODE as REF_CODE,tr.INSERT_USER as INSERT_USER,tr.UPDATE_USER as UPDATE_USER," +
      "tr.INSERT_DATE as INSERT_DATE,tr.UPDATE_DATE as UPDATE_DATE,tr.Shipment_ID as Shipment_ID,tr.ETA_DESTINATION as ETA_DESTINATION,tr.REPORTED_TRUCK_TYPE as REPORTED_TRUCK_TYPE,tr.ACTIVITY as ACTIVITY, sh.FT_TRIP_ID as FT_TRIP_ID," +
      "tr.COMMENTS as COMMENTS,mt.TRUCK_NUMBER as TRUCK_NUMBER,mt.GPS_ENABLED as GPS_ENABLED,ct.GPS_PROVIDER as GPS_PROVIDER,i.INDENT_ID as INDENT_ID , tr.E_WAY_BILL_NO as E_WAY_BILL_NO ," +
      " tr.SAP_INV_VALUE as SAP_INV_VALUE ,tr.SAP_INV_WEIGHT as SAP_INV_WEIGHT, tr.DEST_COUNTRY as DEST_COUNTRY, i.item_category as indentCategory," +
          "tr.TRACKING_CONSENT_STATUS as TRACKING_CONSENT_STATUS, tr.CONSENT_PHONE_TELECOM as CONSENT_PHONE_TELECOM from Truck_Reporting tr " +
      "left join MT_Truck mt on mt.truck_Number=tr.truck_number" +
      " left join ct_gps ct on ct.gps_Provider=mt.gps_Provider " +
      "left join Indent_Summary i on i.indent_Id = tr.indent_id" +
      " left join Shipment sh on tr.shipment_ID = sh.shipment_Id " +
      "  where  tr.REPORTING_LOCATION = ?1 and tr.status IN (?2) and (tr.activity is null or tr.activity = ?3) ",
      countQuery = "select count(tr.gate_Control_Code) from Truck_Reporting tr left join MT_Truck mt on mt.truck_Number = tr.truck_number left join ct_gps gps on gps.gps_Provider = mt.gps_Provider left join Indent_Summary i on i.indent_Id = tr.indent_id where  tr.REPORTING_LOCATION = ?1 and tr.status IN (?2) and (tr.activity is null or tr.activity = ?3)")
  Page<Map<String, Object>> findTruckReportsWithActivityPNative(String source, List<String> status, String activity, Pageable pageable);

  /*For User With Role DP_REP we are not considering reportedLocation*/
  @Query(nativeQuery = true, value = "select tr.GATE_CONTROL_CODE as GATE_CONTROL_CODE,tr.TYPE as TYPE,tr.TRANSPORTER_SAP_CODE as TRANSPORTER_SAP_CODE,tr.CONTAINER_NUM as CONTAINER_NUM," +
      "tr.CONTAINER_CODE as CONTAINER_CODE,tr.DRIVER_NAME as DRIVER_NAME,tr.DRIVER_MOBILE as DRIVER_MOBILE,tr.DRIVER_LICENSE as DRIVER_LICENSE,tr.SERVPROV as SERVPROV," +
      "tr.TRUCK_TYPE as TRUCK_TYPE,tr.REPORTING_LOCATION as REPORTING_LOCATION,tr.SOURCE_LOC as SOURCE_LOC,tr.DEST_LOC as DEST_LOC,tr.REPORTING_DATE as REPORTING_DATE," +
      "tr.GATEIN_DATE as GATEIN_DATE,tr.GATEOUT_DATE as GATEOUT_DATE ,tr.DEST_GEOFENCEIN_DATE as DEST_GEOFENCEIN_DATE,tr.DEREPORTING_DATE as DEREPORTING_DATE,tr.REP_GI_HRS as REP_GI_HRS," +
      "tr.GI_GO_HRS as GI_GO_HRS,tr.STATUS as STATUS,tr.REJ_STATUS as REJ_STATUS,tr.IN_WEIGHT as IN_WEIGHT,tr.OUT_WEIGHT as OUT_WEIGHT,tr.NET_WEIGHT as NET_WEIGHT ,tr.BAY as BAY," +
      "tr.IS_PUC as IS_PUC, tr.IS_INSURANCE as IS_INSURANCE,tr.IS_SEATBELT as IS_SEATBELT,tr.IS_FIRST_AID as IS_FIRST_AID,tr.IS_FIRE_EXTENGUISHER as IS_FIRE_EXTENGUISHER," +
      "tr.IS_EMERGENCY_CARD as IS_EMERGENCY_CARD,tr.IS_SPARK_ARRESTOR as IS_SPARK_ARRESTOR,tr.IS_FITNESS_CERT as IS_FITNESS_CERT,"+
          "tr.TRUCK_CAPACITY as TRUCK_CAPACITY,  tr.fuel_type as FUEL_TYPE, tr.bs_norms as BS_NORMS," +
          "tr.BAY_STATUS as BAY_STATUS,tr.REJECTION_CODE as REJECTION_CODE,tr.ACTUAL_TRUCK_TYPE as ACTUAL_TRUCK_TYPE,tr.REF_CODE as REF_CODE,tr.INSERT_USER as INSERT_USER,tr.UPDATE_USER as UPDATE_USER," +
      "tr.INSERT_DATE as INSERT_DATE,tr.UPDATE_DATE as UPDATE_DATE,tr.Shipment_ID as Shipment_ID,tr.ETA_DESTINATION as ETA_DESTINATION,tr.REPORTED_TRUCK_TYPE as REPORTED_TRUCK_TYPE,tr.ACTIVITY as ACTIVITY, sh.FT_TRIP_ID as FT_TRIP_ID," +
      "tr.COMMENTS as COMMENTS,mt.TRUCK_NUMBER as TRUCK_NUMBER,mt.GPS_ENABLED as GPS_ENABLED,ct.GPS_PROVIDER as GPS_PROVIDER,i.INDENT_ID as INDENT_ID , tr.E_WAY_BILL_NO as E_WAY_BILL_NO ," +
      " tr.SAP_INV_VALUE as SAP_INV_VALUE ,tr.SAP_INV_WEIGHT as SAP_INV_WEIGHT, tr.DEST_COUNTRY as DEST_COUNTRY, i.item_category as indentCategory, tr.TRACKING_CONSENT_STATUS as TRACKING_CONSENT_STATUS, tr.CONSENT_PHONE_TELECOM as CONSENT_PHONE_TELECOM from Truck_Reporting tr " +
      "left join MT_Truck mt on mt.truck_Number=tr.truck_number" +
      " left join ct_gps ct on ct.gps_Provider=mt.gps_Provider " +
      "left join Indent_Summary i on i.indent_Id = tr.indent_id" +
      " left join Shipment sh on tr.shipment_ID = sh.shipment_Id " +
      "  where  tr.status IN (?1) and (tr.activity is null or tr.activity = ?2)",
      countQuery = "select count(tr.gate_Control_Code) from Truck_Reporting tr left join MT_Truck mt on mt.truck_Number = tr.truck_number left join ct_gps gps on gps.gps_Provider = mt.gps_Provider left join Indent_Summary i on i.indent_Id = tr.indent_id where  tr.status IN (?1) and (tr.activity is null or tr.activity = ?2)")
  Page<Map<String, Object>> findTruckReportsWithActivityPNativeDPREP(List<String> status, String activity, Pageable pageable);


  Page<TruckReport> findAllByReportLocationAndStatusInAndActivityNotOrderByReportDateAsc(String source, List<TruckReportStatus> status, String activity, Pageable pageable);


  Page<TruckReport> findAllByDestinationLocationAndStatusInAndReportLocation(String source, List<TruckReportStatus> status, String reportLoc, Pageable pageable);

  Optional<TruckReport> findOneByGateControlCodeAndIndentSummaryIndentIdAndStatus(String gateControlCode, String indentId, TruckReportStatus status);

  Optional<TruckReport> findOneByGateControlCodeAndIndentSummaryIndentIdAndStatusAndTruckTruckNumber(String gateControlCode, String indentId, TruckReportStatus status, String truckNumber);

  Optional<TruckReport> findOneByGateControlCodeAndIndentSummaryIndentIdAndTruckTruckNumber(String gateControlCode, String indentId,  String truckNumber);


  Optional<TruckReport> findOneByGateControlCodeAndIndentSummaryIndentId(String gateControlCode, String indentId);

  @Query("select T.sourceLocation from TruckReport T")
  List<String> getPlantSouce();

  @Query("select new com.api.apollo.atom.entity.ops.TruckReport(T.waitTimeHrs,T.status,T.gateInDate,T.reportDate) from TruckReport T where T.sourceLocation=?1")
  List<TruckReport> findAllBySourceLocation(String source);

  List<TruckReport> findAllByStatusAndDestinationLocation(TruckReportStatus status, String sourceLoc);

  Optional<TruckReport> findOneByGateControlCodeAndTruckTruckNumber(String gateControlCode, String truckNumber);

  @Query("SELECT tr FROM TruckReport tr WHERE tr.destinationLocation = ?1 AND tr.status in ?2 AND tr.refCode is NULL")
  Page<TruckReport> findAllByDestinationLocationAndStatusInAndRefCodeIsNull(String plantCode, List<TruckReportStatus> truckStatuses, PageRequest pageble);

  @Transactional
  @Modifying
  @Query(nativeQuery = true, value = "UPDATE truck_reporting SET shipment_id = NULL WHERE shipment_id = ?1")
  void updateTruckReporting(String shipmentId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE truck_reporting SET tracking_consent_status = 'RETRYING' WHERE shipment_id = ?1")
    void updateTruckReportConsentByShipmentIdRetry(String shipmentId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE truck_reporting SET tracking_consent_status = 'RETRYING' WHERE gate_control_code = ?1")
    void updateTruckReportConsentByGateControlCodeRetry(String gateControlCode);

  Optional<TruckReport> findOneByShipmentID(String shipmentId);

  Optional<TruckReport> findByShipmentIDAndReportLocation(String shipmentId, String plantCode);

  List<TruckReport> findAllByShipmentID(String shipmentId);

  Optional<TruckReport> findByTruckTruckNumberAndShipmentIDAndReportLocation(String truckNumber, String shipmentId, String plantCode);

  Optional<TruckReport> findByTruckTruckNumberAndReportLocationAndStatus(String truckNumber, String repoLoc, TruckReportStatus status);

  Page<TruckReport> findAllByShipmentIDInAndStatusInOrderByReportDateAsc(List<String> shipmentIds, List<TruckReportStatus> truckStatuses, Pageable pageble);

  Page<TruckReport> findAllByShipmentIDInAndStatusInAndReportLocationNotOrderByReportDateAsc(List<String> shipmentIds, List<TruckReportStatus> truckStatuses, String reportLocation, Pageable pageble);

  @Query(nativeQuery = true, value = "select * from (select tr.GATE_CONTROL_CODE as GATE_CONTROL_CODE,tr.TYPE as TYPE,tr.TRANSPORTER_SAP_CODE as TRANSPORTER_SAP_CODE,tr.CONTAINER_NUM as CONTAINER_NUM," +
      "tr.CONTAINER_CODE as CONTAINER_CODE,tr.DRIVER_NAME as DRIVER_NAME,tr.DRIVER_MOBILE as DRIVER_MOBILE,tr.DRIVER_LICENSE as DRIVER_LICENSE,tr.SERVPROV as SERVPROV," +
      "tr.TRUCK_TYPE as TRUCK_TYPE,tr.REPORTING_LOCATION as REPORTING_LOCATION,tr.SOURCE_LOC as SOURCE_LOC,tr.DEST_LOC as DEST_LOC,tr.REPORTING_DATE as REPORTING_DATE," +
      "tr.GATEIN_DATE as GATEIN_DATE,tr.GATEOUT_DATE as GATEOUT_DATE ,tr.DEST_GEOFENCEIN_DATE as DEST_GEOFENCEIN_DATE,tr.DEREPORTING_DATE as DEREPORTING_DATE,tr.REP_GI_HRS as REP_GI_HRS," +
      "tr.GI_GO_HRS as GI_GO_HRS,tr.STATUS as STATUS,tr.REJ_STATUS as REJ_STATUS,tr.IN_WEIGHT as IN_WEIGHT,tr.OUT_WEIGHT as OUT_WEIGHT,tr.NET_WEIGHT as NET_WEIGHT ,tr.BAY as BAY," +
      "tr.IS_PUC as IS_PUC, tr.IS_INSURANCE as IS_INSURANCE,tr.IS_SEATBELT as IS_SEATBELT,tr.IS_FIRST_AID as IS_FIRST_AID,tr.IS_FIRE_EXTENGUISHER as IS_FIRE_EXTENGUISHER," +
      "tr.IS_EMERGENCY_CARD as IS_EMERGENCY_CARD,tr.IS_SPARK_ARRESTOR as IS_SPARK_ARRESTOR,tr.IS_FITNESS_CERT as IS_FITNESS_CERT,"+
          "tr.TRUCK_CAPACITY as TRUCK_CAPACITY,  tr.fuel_type as FUEL_TYPE, tr.bs_norms as BS_NORMS," +
          "tr.BAY_STATUS as BAY_STATUS,tr.REJECTION_CODE as REJECTION_CODE,tr.ACTUAL_TRUCK_TYPE as ACTUAL_TRUCK_TYPE,tr.REF_CODE as REF_CODE,tr.INSERT_USER as INSERT_USER,tr.UPDATE_USER as UPDATE_USER," +
      "tr.INSERT_DATE as INSERT_DATE,tr.UPDATE_DATE as UPDATE_DATE,tr.Shipment_ID as Shipment_ID,tr.ETA_DESTINATION as ETA_DESTINATION,tr.REPORTED_TRUCK_TYPE as REPORTED_TRUCK_TYPE,tr.ACTIVITY as ACTIVITY," +
      "tr.COMMENTS as COMMENTS,mt.TRUCK_NUMBER as TRUCK_NUMBER,mt.GPS_ENABLED as GPS_ENABLED,ct.GPS_PROVIDER as GPS_PROVIDER,i.INDENT_ID as INDENT_ID, tr.DEST_COUNTRY as DEST_COUNTRY," +
      " ROW_NUMBER() OVER (PARTITION BY tr.shipment_id ORDER BY tr.insert_date ASC) rn,  " +
          "tr.TRACKING_CONSENT_STATUS as TRACKING_CONSENT_STATUS, tr.CONSENT_PHONE_TELECOM as CONSENT_PHONE_TELECOM" +
      " from truck_reporting tr " +
      "left join mt_truck mt on mt.truck_Number=tr.truck_number" +
      " left join ct_Gps ct on ct.gps_Provider=mt.gps_Provider " +
      "left join Indent_Summary i on i.indent_Id = tr.indent_id " +
      " RIGHT JOIN (  SELECT sh.shipment_id as  shipId  from ((SELECT DISTINCT st.shipment_id as shipmentId from shared_truck  st WHERE st.plant_code = ?1 AND st.status = 'OPEN') " +
      "            UNION " +
      "    (SELECT DISTINCT s.shipment_id from shipment_stop s  WHERE s.location_id = ?1 and s.activity = 'D')) a, shipment  sh where sh.shipment_id = a.shipmentId and sh.status = 'INTRANSIT') sharedData on sharedData.shipId = tr.shipment_id " +
      " WHERE tr.reporting_location != ?1 AND tr.status IN (?2) ORDER BY tr.REPORTING_DATE DESC) ab where ab.rn = 1  "
      /*"  where  tr.shipmentID IN (?1) and tr.status IN (?2) and (tr.reportLocation != ?3)"*/,
      countQuery = "select count(*) from (select tr.gate_Control_Code, ROW_NUMBER() OVER (PARTITION BY tr.shipment_id ORDER BY tr.insert_date ASC) rn   from truck_reporting tr " +
          "    left join mt_truck mt on mt.truck_Number=tr.truck_number " +
          "    left join ct_Gps ct on ct.gps_Provider=mt.gps_Provider " +
          "    left join Indent_Summary i on i.indent_Id = tr.indent_id " +
          "    RIGHT JOIN (  SELECT sh.shipment_id as  shipId  from ((SELECT DISTINCT st.shipment_id as shipmentId from shared_truck  st WHERE st.plant_code = ?1 AND st.status = 'OPEN') " +
          "            UNION " +
          "    (SELECT DISTINCT s.shipment_id from shipment_stop s  WHERE s.location_id = ?1 and s.activity = 'D')) a, shipment  sh where sh.shipment_id = a.shipmentId and sh.status = 'INTRANSIT') sharedData on sharedData.shipId = tr.shipment_id " +
          "    WHERE tr.reporting_location != ?1 AND tr.status IN (?2) ORDER BY tr.REPORTING_DATE DESC) ab where ab.rn=1 ")
  Page<Map<String, Object>> findByShipmentIDInAndStatusInAndReportLocationNotOrderByReportDateAsc( String reportingLoc, List<String> truckReportStatuses, Pageable pageble);

  /*OLD WHERE CLAUSE*/
      /*" where tr.shipment_id IN (  SELECT sh.shipment_id  from ((SELECT DISTINCT st.shipment_id as shipmentId from shared_truck  st WHERE st.status = 'OPEN') \n" +
      " UNION " +
      " (SELECT DISTINCT s.shipment_id from shipment_stop s  WHERE s.activity = 'D')) a, shipment  sh where sh.shipment_id = a.shipmentId and sh.status = 'INTRANSIT') AND " +
      " tr.shipment_id IN (select distinct t.shipment_id from truck_reporting t where  t.status = 'INTRANSIT'  )  AND tr.status IN (?1) ",*/
  /*"  where  tr.shipmentID IN (?1) and tr.status IN (?2) "*/

  /*Old Count Query*/
/*  countQuery = "select count(tr.gate_control_code) from truck_reporting tr left join mt_truck mt on mt.truck_Number=tr.truck_number left join ct_Gps ct on ct.gps_Provider=mt.gps_Provider left join Indent_Summary i on i.indent_Id = tr.indent_id " +
      " where tr.shipment_id IN (  SELECT sh.shipment_id  from ((SELECT DISTINCT st.shipment_id as shipmentId from shared_truck  st WHERE st.plant_code = ?1 AND st.status = 'OPEN') \n" +
      " UNION " +
      " (SELECT DISTINCT s.shipment_id from shipment_stop s  WHERE s.location_id = ?1 and s.activity = 'D')) a, shipment  sh where sh.shipment_id = a.shipmentId and sh.status = 'INTRANSIT') AND " +
      " tr.shipment_id IN (select distinct t.shipment_id from truck_reporting t where  t.status = 'INTRANSIT'  )  AND tr.reporting_location != ?1 AND tr.status IN (?2)")*/

  /*Intransit trucks forDP_REP*/
  @Query(nativeQuery = true, value = "select * from (select tr.GATE_CONTROL_CODE as GATE_CONTROL_CODE,tr.TYPE as TYPE,tr.TRANSPORTER_SAP_CODE as TRANSPORTER_SAP_CODE,tr.CONTAINER_NUM as CONTAINER_NUM," +
      "tr.CONTAINER_CODE as CONTAINER_CODE,tr.DRIVER_NAME as DRIVER_NAME,tr.DRIVER_MOBILE as DRIVER_MOBILE,tr.DRIVER_LICENSE as DRIVER_LICENSE,tr.SERVPROV as SERVPROV," +
      "tr.TRUCK_TYPE as TRUCK_TYPE,tr.REPORTING_LOCATION as REPORTING_LOCATION,tr.SOURCE_LOC as SOURCE_LOC,tr.DEST_LOC as DEST_LOC,tr.REPORTING_DATE as REPORTING_DATE," +
      "tr.GATEIN_DATE as GATEIN_DATE,tr.GATEOUT_DATE as GATEOUT_DATE ,tr.DEST_GEOFENCEIN_DATE as DEST_GEOFENCEIN_DATE,tr.DEREPORTING_DATE as DEREPORTING_DATE,tr.REP_GI_HRS as REP_GI_HRS," +
      "tr.GI_GO_HRS as GI_GO_HRS,tr.STATUS as STATUS,tr.REJ_STATUS as REJ_STATUS,tr.IN_WEIGHT as IN_WEIGHT,tr.OUT_WEIGHT as OUT_WEIGHT,tr.NET_WEIGHT as NET_WEIGHT ,tr.BAY as BAY," +
      "tr.IS_PUC as IS_PUC, tr.IS_INSURANCE as IS_INSURANCE,tr.IS_SEATBELT as IS_SEATBELT,tr.IS_FIRST_AID as IS_FIRST_AID,tr.IS_FIRE_EXTENGUISHER as IS_FIRE_EXTENGUISHER," +
      "tr.IS_EMERGENCY_CARD as IS_EMERGENCY_CARD,tr.IS_SPARK_ARRESTOR as IS_SPARK_ARRESTOR,tr.IS_FITNESS_CERT as IS_FITNESS_CERT,"+
          "tr.TRUCK_CAPACITY as TRUCK_CAPACITY,  tr.fuel_type as FUEL_TYPE, tr.bs_norms as BS_NORMS," +
          "tr.BAY_STATUS as BAY_STATUS,tr.REJECTION_CODE as REJECTION_CODE,tr.ACTUAL_TRUCK_TYPE as ACTUAL_TRUCK_TYPE,tr.REF_CODE as REF_CODE,tr.INSERT_USER as INSERT_USER,tr.UPDATE_USER as UPDATE_USER," +
      "tr.INSERT_DATE as INSERT_DATE,tr.UPDATE_DATE as UPDATE_DATE,tr.Shipment_ID as Shipment_ID,tr.ETA_DESTINATION as ETA_DESTINATION,tr.REPORTED_TRUCK_TYPE as REPORTED_TRUCK_TYPE,tr.ACTIVITY as ACTIVITY," +
      "tr.COMMENTS as COMMENTS,mt.TRUCK_NUMBER as TRUCK_NUMBER,mt.GPS_ENABLED as GPS_ENABLED,ct.GPS_PROVIDER as GPS_PROVIDER,i.INDENT_ID as INDENT_ID, tr.DEST_COUNTRY as DEST_COUNTRY, " +
      " ROW_NUMBER() OVER (PARTITION BY tr.shipment_id ORDER BY tr.insert_date ASC) rn " +
      " tr.TRACKING_CONSENT_STATUS as TRACKING_CONSENT_STATUS, tr.CONSENT_PHONE_TELECOM as CONSENT_PHONE_TELECOM from truck_reporting tr " +
      "left join mt_truck mt on mt.truck_Number=tr.truck_number" +
      " left join ct_Gps ct on ct.gps_Provider=mt.gps_Provider " +
      "left join Indent_Summary i on i.indent_Id = tr.indent_id " +
      " RIGHT JOIN (  SELECT sh.shipment_id as  shipId  from ((SELECT DISTINCT st.shipment_id as shipmentId from shared_truck  st WHERE st.status = 'OPEN') \n" +
      "               UNION " +
      "               (SELECT DISTINCT s.shipment_id from shipment_stop s  WHERE s.activity = 'D')) a, shipment  sh where sh.shipment_id = a.shipmentId and sh.status = 'INTRANSIT') sharedData " +
      "     on sharedData.shipId = tr.shipment_id  " +
      " WHERE tr.status IN (?1) ORDER BY tr.REPORTING_DATE DESC ) ab where ab.rn = 1" ,
      countQuery = "select count(*) from (select tr.gate_Control_Code, ROW_NUMBER() OVER (PARTITION BY tr.shipment_id ORDER BY tr.insert_date ASC) rn   from truck_reporting tr " +
          "      left join mt_truck mt on mt.truck_Number=tr.truck_number " +
          "      left join ct_Gps ct on ct.gps_Provider=mt.gps_Provider " +
          "      left join Indent_Summary i on i.indent_Id = tr.indent_id" +
          "      RIGHT JOIN (SELECT sh.shipment_id as  shipId  from ((SELECT DISTINCT st.shipment_id as shipmentId from shared_truck  st WHERE st.status = 'OPEN') " +
          "                  UNION " +
          "      (SELECT DISTINCT s.shipment_id from shipment_stop s  WHERE s.activity = 'D')) a, shipment  sh where sh.shipment_id = a.shipmentId and sh.status = 'INTRANSIT') sharedData on sharedData.shipId = tr.shipment_id " +
          "       WHERE tr.status IN (?1) ORDER BY tr.REPORTING_DATE DESC) ab where ab.rn=1")
  Page<Map<String, Object>> findByShipmentIDInAndStatusInOrderByReportDateAsc(List<String> truckStatuses, Pageable pageble);

  @Query(nativeQuery = true, value = "SELECT REPORTING_LOCATION FROM TRUCK_REPORTING WHERE Shipment_ID = ?1")
  List<String> findReportLocationByShipmentID(String shipmentID);

  @Query(nativeQuery = true, value = "SELECT REPORTING_LOCATION as reportLoc, SHIPMENT_ID as shipmentId FROM TRUCK_REPORTING WHERE Shipment_ID IN (?1)")
  List<Map<String, String>> findReportLocationByShipmentIDIn(List<String> shipmentIDs);

  TruckReport findByGateControlCode(String gateControlCode);

  Optional<TruckReport> findFirst1ByTruckTruckNumberOrderByReportDateDesc(String truckNumber);

  boolean existsByTruckTruckNumberIgnoreCaseAndReportLocationAndStatusNotIn(String truckNum, String reportLoc, List<TruckReportStatus> status);


  /*Delayed days Calculation
   * 1. When report-date-Dest is available then (report-date-Dest) - (gate-out-at-source + tt_days)
   * 2. ELSE if loadslip-id is not null and truck is not REJECTED and (current-date) - (gate-out-at-source + tt_days) >0 (i.e., current date should be greater than (gate-out-at-source + tt-days))
   * NOTE: when loadslip is not assaigned to the truck OR truck is REJECTED then delayed days is shown as null
   * 3. Other wise show null*/

  @Query(nativeQuery = true ,value = "SELECT DISTINCT ts.* from (SELECT tr.truck_number    AS truckNumber, \n" +
      "       tr.container_num         AS containerNum, \n" +
      "       tr.driver_name           AS driverName, \n" +
      "       tr.driver_mobile         AS driverMobile, \n" +
      "       tr.driver_license        AS driverLicense, \n" +
			"       isu.FRT_AVAIL_FLAG          AS isFreightAvailable, \n" +
      "       tr.REP_GI_HRS            AS repGiHrs, \n" +
      "       tr.REP_GO_HRS            AS repGoHrs, \n" +
      "       tr.GI_GO_HRS             AS giGoHrs, \n" +
      "       tr.GI_REL_HRS            AS giRelHrs, \n" +
      "       tr.LO_REL_HRS            AS loRelHrs, \n" +
      "       tr.REL_GO_HRS            AS relGoHrs, "+
     /* "       tr.tt_hrs                   AS totalTurnAroundTime, \n" +
      "       tr.wait_time_hrs            AS totalwaittimefromReportedtoGateIn, \n" +
      "       tr.release_time_hrs         AS timeFromFGSCalltoRelease, \n" +*/
      "       lp.loadslip_id           AS loadslipId, \n" +
      "       lp.shipment_id           AS shipmentId, \n" +
      "       tr.gatein_date           AS gateInDate, \n" +
      "       tr.gateout_date          AS gateOutDate, \n" +
      "       tr.reporting_date        AS reportDate, \n" +
			"       tr.reporting_location        AS reportingLoc,\n"+
      "       tr.in_weight             AS inWeight, \n" +
      "       tr.out_weight            AS outWeight, \n" +
      "       tr.comments              AS comments, \n" +

      "      tr.is_puc AS ispuc, \n" +
      "      tr.is_insurance AS isinsurance, \n" +
      "       tr.is_seatbelt AS isseatbelt, \n" +
      "       tr.is_first_aid AS isfirstaid, \n" +
      "       tr.is_fire_extenguisher AS isfireextenguisher, \n" +
      "       tr.is_emergency_card AS isemergencycard, \n" +
      "       tr.is_spark_arrestor AS issparkarrestor, \n" +
      "      tr.is_fitness_cert AS isfitnesscert, \n" +
          "  tr.truck_capacity as truckCapacity, \n" +
          "  tr.gross_vehicle_wt as truckgrossVehicleWt, \n" +
          "  tr.unladen_wt as truckunladenWt, \n" +
          "  tr.bs_norms as bsNorms, \n" +
          "  tr.fuel_type as fuelType, \n" +
     /* "       ( CASE \n" +
      "           WHEN tr.actual_truck_type IS NULL THEN tr.truck_type \n" +
      "           ELSE tr.actual_truck_type \n" +
      "         END )                  AS truckType, \n" +*/
			"       tr.actual_truck_type     AS actualTruckType,"+
			"       tr.truck_type            AS truckType,"+
      "       tr.servprov        AS servprov, \n" +
      "       lp.qty                   AS qty, \n" +
      "       tr.source_loc            AS sourceLoc, \n" +
      "       (CASE WHEN lp.loadslip_id IS NULL THEN isu.dest_loc ELSE lp.dest_loc END)             AS destLoc, \n" +
      "       lp.sto_so_num            AS stoSoNum, \n" +
      "       lp.delivery              AS delivery, \n" +
      "       lp.sap_invoice           AS sapInvoice, \n" +
      "       lp.sap_invoice_date      AS sapInvoiceDate, \n" +
      "       lp.lr_num                AS lrNum, \n" +
      "       lp.lsprint_date          AS lsPrintDate, \n" +
      "       lp.arrived_date          AS bayArrivedDate, \n" +
      "       lp.ls_date               AS loadingStartDate, \n" +
      "       lp.le_date               AS loadingEndDate, \n" +
      "       lp.confirm_date          AS confirmedDate, \n" +
      "       lp.send_for_barcode_date AS sendForBarcodeDate, \n" +
      "       lp.tot_tyres             AS totTyres, \n" +
      "       lp.tot_tubes             AS totTubes, \n" +
      "       lp.tot_flaps             AS totFlaps, \n" +
      "       lp.tot_valve             AS totValve, \n" +
      "       lp.tot_pctr              AS totPctr, \n" +
      "       lp.tot_qty               AS totQty, \n" +
      "       lp.grn                   AS grn, \n" +
      "       tr.status                AS status, \n" +
      "       lp.tte_util              AS tteUtil, \n" +
      "       lp.weight_util           AS weightUtil, \n" +
      "       lp.comments              AS loadslipcomments, \n" +
      "       shipment.frt_avail_flag  AS freightAvailability, \n" +
      "       shipment.transhipment    AS transhipment, \n" +
      "       tr.update_user           AS updateUser, \n" +
      "       tr.insert_user           AS insertUser, \n" +
      "       lp.e_way_bill_no         AS eWayBillNo, \n" +
      "       lp.int_status            AS integrationStatus, \n" +
      "       lp.int_message           AS integrationMsg, \n" +
      "       lp.item_category         AS itemCategory, \n" +
      "       shipment.start_time      AS gatedOutDate, \n" +
      "       lp.release_date          AS releaseDate, \n" +
      "       lp.loadslip_type         AS type, \n" +
      "       lp.sap_inv_value         AS sapInvValue, \n" +
      "       lp.sap_inv_weight        AS sapInvWeight, \n" +
      "       lp.other                 AS otherQty, \n" +
      "       lp.insert_date           AS createdDate, \n" +
      "       lp.grn_date              AS grnDate, \n" +
      "       lp.lr_date               AS lrDate, \n " +
			"     shipment.stop_type             AS stopType,\n" +
			"               lp.dit_qty               AS ditQty,\n" +
			"               lp.short_qty             AS shortQty,\n" +
			"               lp.tte_qty               AS tteQty,\n" +
			"               lp.volume_util           AS volumeUtil,\n" +
			"               lp.drop_seq              AS dropSeq , "+
			"               tr.bay_status            AS bayStatus, "+
      "               tr.DEST_COUNTRY    AS destCountry,"+
			"               tr.indent_id                AS indentId,"+
			"               tr.TRANSPORTER_SAP_CODE  AS transporterSapCode,"+
			"               lp.status                AS loadslipStatus,"+
			"               tr.REJECTION_CODE        AS rejectioncode,"+
			"               tr.REJ_STATUS            AS rejectionstatus," +
      "               lp.MKT_SEG               AS marketSegment, "+
			"               isu.insert_date          AS indentCreationDate,"+
			"               lp.grn_reporting_date    AS reportingDateAtDest,\n" +
			"               lp.grn_unloading_date    AS unloadingDateAtDest,\n" +
			"               tr.tt_days               AS stdTT, \n" +
      "               shipment.ft_trip_id      AS ftTripId, " +
      "               lp.custom_inv_number   AS customInvNumber, " +
			"               (CASE WHEN lp.grn_reporting_date IS NOT NULL THEN \n" +
      "                     Abs(( ( To_date(lp.grn_reporting_date, 'DD-MM-YYYY') - To_date(tr.gateout_date, 'DD-MM-YYYY') ) - ( CASE WHEN tr.tt_days IS NULL THEN 0 ELSE tr.tt_days END) ))\n" +
      "                ELSE " +
      "                     (CASE WHEN (lp.loadslip_id IS NOT NULL AND tr.rej_status != 'REJECTED') AND " +
      "                         (((TO_DATE((SELECT SYSDATE FROM DUAL),'DD-MM-YYYY')) -(TO_DATE(tr.gateout_date,'DD-MM-YYYY')) -( CASE WHEN tr.tt_days IS NULL THEN 0 ELSE tr.tt_days END)) > 0) \n" +
      "                           THEN Abs(((TO_DATE((SELECT SYSDATE FROM DUAL),'DD-MM-YYYY')) -(TO_DATE(tr.gateout_date,'DD-MM-YYYY')) -( CASE WHEN tr.tt_days IS NULL THEN 0 ELSE tr.tt_days END)))\n" +
      "                      ELSE NULL END) " +
      "               END) AS delayedDays "+
      " FROM   truck_reporting tr \n" +
      "       LEFT JOIN loadslip lp \n" +
      "              ON ( lp.shipment_id = tr.shipment_id OR lp.shipment_id IS NULL ) and ( (lp.source_loc = tr.reporting_location  OR lp.source_loc IS NULL) or (tr.reporting_location=lp.dest_loc) ) AND ( lp.status NOT IN( 'CANCELLED' ) OR lp.status IS NULL ) \n" +
          "left join Shipment shipment ON tr.shipment_id = shipment.shipment_id LEFT JOIN indent_summary isu ON isu.indent_id = tr.indent_id where  tr.truck_number is not null and tr.status in(?1) ) ts order by ts.reportDate desc",
  countQuery = "SELECT  count(*)   FROM   (SELECT DISTINCT tr.truck_number AS truckNumber,lp.loadslip_id AS loadslipId,tr.reporting_date AS reportDate FROM   truck_reporting tr \n" +
			"      LEFT JOIN loadslip lp ON ( lp.shipment_id = tr.shipment_id OR lp.shipment_id IS NULL ) and ( (lp.source_loc = tr.reporting_location  OR lp.source_loc IS NULL) or (tr.reporting_location=lp.dest_loc) )  AND ( lp.status NOT IN( 'CANCELLED' ) OR lp.status IS NULL ) LEFT JOIN shipment shipment ON tr.shipment_id = shipment.shipment_id LEFT JOIN indent_summary isu ON isu.indent_id = tr.indent_id \n" +
			"      WHERE tr.truck_number IS NOT NULL  AND tr.status in(?1)  ) ts")
  Page<Map<String, Object>> getTrucksMovementBySourceLocDPREP(List<String> statuses ,Pageable pageable);


  /*Delayed days Calculation
   * 1. When report-date-Dest is available then (report-date-Dest) - (gate-out-at-source + tt_days)
   * 2. ELSE if loadslip-id is not null and truck is not REJECTED and (current-date) - (gate-out-at-source + tt_days) >0 (i.e., current date should be greater than (gate-out-at-source + tt-days))
   * NOTE: when loadslip is not assaigned to the truck OR truck is REJECTED then delayed days is shown as null
   * 3. Other wise show null*/

  @Query(nativeQuery = true ,value = "SELECT DISTINCT ts.* from (SELECT  tr.truck_number    AS truckNumber, \n" +
      "       tr.container_num         AS containerNum, \n" +
      "       tr.driver_name           AS driverName, \n" +
      "       tr.driver_mobile         AS driverMobile, \n" +
      "       tr.driver_license        AS driverLicense, \n" +
      "       isu.FRT_AVAIL_FLAG          AS isFreightAvailable, \n" +
      "       tr.REP_GI_HRS            AS repGiHrs, \n" +
      "       tr.REP_GO_HRS            AS repGoHrs, \n" +
      "       tr.GI_GO_HRS             AS giGoHrs, \n" +
      "       tr.GI_REL_HRS            AS giRelHrs, \n" +
      "       tr.LO_REL_HRS            AS loRelHrs, \n" +
      "       tr.REL_GO_HRS            AS relGoHrs, "+

      "      tr.is_puc AS ispuc, \n" +
      "      tr.is_insurance AS isinsurance, \n" +
      "       tr.is_seatbelt AS isseatbelt, \n" +
      "       tr.is_first_aid AS isfirstaid, \n" +
      "       tr.is_fire_extenguisher AS isfireextenguisher, \n" +
      "       tr.is_emergency_card AS isemergencycard, \n" +
      "       tr.is_spark_arrestor AS issparkarrestor, \n" +
      "      tr.is_fitness_cert AS isfitnesscert, \n" +
          "  tr.truck_capacity as truckCapacity, \n"  +
          "  tr.gross_vehicle_wt as truckgrossVehicleWt, \n" +
          "  tr.unladen_wt as truckunladenWt, \n" +
          "  tr.bs_norms as bsNorms, \n" +
          "  tr.fuel_type as fuelType, \n" +
     /* "       tr.tt_hrs                   AS tt_hrs, \n" +
      "       tr.wait_time_hrs            AS waitTimeHrs, \n" +
      "       tr.release_time_hrs         AS releaseTimeHrs, \n" +*/
      "       lp.loadslip_id           AS loadslipId, \n" +
      "       lp.shipment_id           AS shipmentId, \n" +
      "       tr.gatein_date           AS gateInDate, \n" +
      "       tr.gateout_date          AS gateOutDate, \n" +
      "       tr.reporting_date        AS reportDate, \n" +
			"       tr.reporting_location        AS reportingLoc,\n"+
      "       tr.in_weight             AS inWeight, \n" +
      "       tr.out_weight            AS outWeight, \n" +
      "       tr.comments              AS comments, \n" +
     /* "       ( CASE \n" +
      "           WHEN tr.actual_truck_type IS NULL THEN tr.truck_type \n" +
      "           ELSE tr.actual_truck_type \n" +
      "         END )                  AS truckType, \n" +*/
      "       tr.actual_truck_type     AS actualTruckType,"+
			"       tr.truck_type            AS truckType,"+
      "       tr.servprov        AS servprov, \n" +
      "       lp.qty                   AS qty, \n" +
      "       tr.source_loc            AS sourceLoc, \n" +
      "       (CASE WHEN lp.loadslip_id IS NULL THEN isu.dest_loc ELSE lp.dest_loc END)            AS destLoc, \n" +
      "       lp.sto_so_num            AS stoSoNum, \n" +
      "       lp.delivery              AS delivery, \n" +
      "       lp.sap_invoice           AS sapInvoice, \n" +
      "       lp.sap_invoice_date      AS sapInvoiceDate, \n" +
      "       lp.lr_num                AS lrNum, \n" +
      "       lp.lsprint_date          AS lsPrintDate, \n" +
      "       lp.arrived_date          AS bayArrivedDate, \n" +
      "       lp.ls_date               AS loadingStartDate, \n" +
      "       lp.le_date               AS loadingEndDate, \n" +
      "       lp.confirm_date          AS confirmedDate, \n" +
      "       lp.send_for_barcode_date AS sendForBarcodeDate, \n" +
      "       lp.tot_tyres             AS totTyres, \n" +
      "       lp.tot_tubes             AS totTubes, \n" +
      "       lp.tot_flaps             AS totFlaps, \n" +
      "       lp.tot_valve             AS totValve, \n" +
      "       lp.tot_pctr              AS totPctr, \n" +
      "       lp.tot_qty               AS totQty, \n" +
      "       lp.grn                   AS grn, \n" +
      "       tr.status                AS status, \n" +
      "       lp.tte_util              AS tteUtil, \n" +
      "       lp.weight_util           AS weightUtil, \n" +
      "       lp.comments              AS loadslipcomments, \n" +
      "       shipment.frt_avail_flag  AS freightAvailability, \n" +
      "       shipment.transhipment    AS transhipment, \n" +
      "       tr.update_user           AS updateUser, \n" +
      "       tr.insert_user           AS insertUser, \n" +
      "       lp.e_way_bill_no         AS eWayBillNo, \n" +
      "       lp.int_status            AS integrationStatus, \n" +
      "       lp.int_message           AS integrationMsg, \n" +
      "       lp.item_category         AS itemCategory, \n" +
      "       shipment.start_time      AS gatedOutDate, \n" +
      "       lp.release_date          AS releaseDate, \n" +
      "       lp.loadslip_type         AS type, \n" +
      "       lp.sap_inv_value         AS sapInvValue, \n" +
      "       lp.sap_inv_weight        AS sapInvWeight, \n" +
      "       lp.other                 AS otherQty, \n" +
      "       lp.insert_date           AS createdDate, \n" +
      "       lp.grn_date              AS grnDate, \n" +
      "       lp.lr_date               AS lrDate, \n " +
			" shipment.stop_type             AS stopType,\n" +
      " shipment.ft_trip_id            AS ftTripId, " +
			"               lp.dit_qty               AS ditQty,\n" +
			"               lp.short_qty             AS shortQty,\n" +
			"               lp.tte_qty               AS tteQty,\n" +
			"               lp.volume_util           AS volumeUtil,\n" +
			"               lp.drop_seq              AS dropSeq , \n"+
			"               tr.bay_status            AS bayStatus,"+
      "               tr.DEST_COUNTRY    AS destCountry,"+
			"               tr.indent_id             AS indentId,"+
			"               tr.TRANSPORTER_SAP_CODE  AS transporterSapCode,"+
			"               lp.status                AS loadslipStatus,"+
			"               tr.REJECTION_CODE        AS rejectioncode,"+
			"               tr.REJ_STATUS            AS rejectionstatus," +
      "               lp.MKT_SEG               AS marketSegment,"+
			"               isu.insert_date          AS indentCreationDate,"+
			"               lp.grn_reporting_date    AS reportingDateAtDest,\n" +
			"               lp.grn_unloading_date    AS unloadingDateAtDest,\n" +
			"               tr.tt_days               AS stdTT, " +
      "               lp.custom_inv_number   AS customInvNumber, " +
			"               (CASE WHEN lp.grn_reporting_date IS NOT NULL THEN \n" +
      "                     Abs(( ( To_date(lp.grn_reporting_date, 'DD-MM-YYYY') - To_date(tr.gateout_date, 'DD-MM-YYYY') ) - ( CASE WHEN tr.tt_days IS NULL THEN 0 ELSE tr.tt_days END) ))\n" +
      "                ELSE " +
      "                     (CASE WHEN (lp.loadslip_id IS NOT NULL AND tr.rej_status != 'REJECTED') AND " +
      "                         (((TO_DATE((SELECT SYSDATE FROM DUAL),'DD-MM-YYYY')) -(TO_DATE(tr.gateout_date,'DD-MM-YYYY')) -( CASE WHEN tr.tt_days IS NULL THEN 0 ELSE tr.tt_days END)) > 0) \n" +
      "                           THEN Abs(((TO_DATE((SELECT SYSDATE FROM DUAL),'DD-MM-YYYY')) -(TO_DATE(tr.gateout_date,'DD-MM-YYYY')) -( CASE WHEN tr.tt_days IS NULL THEN 0 ELSE tr.tt_days END)))\n" +
      "                      ELSE NULL END)\n" +
      "                END) AS delayedDays "+
      " FROM   truck_reporting tr \n" +
      "       LEFT JOIN loadslip lp \n" +
      "              ON ( lp.shipment_id = tr.shipment_id OR lp.shipment_id IS NULL ) and ( (lp.source_loc = tr.reporting_location  OR lp.source_loc IS NULL) or (tr.reporting_location=lp.dest_loc) )  AND ( lp.status NOT IN( 'CANCELLED' ) OR lp.status IS NULL )\n" +
      "left join Shipment shipment  ON tr.shipment_id = shipment.shipment_id LEFT JOIN indent_summary isu ON isu.indent_id = tr.indent_id WHERE tr.truck_number is not null and tr.reporting_location= ?1 and tr.status in(?2) ) ts  order by ts.reportDate desc" ,
  countQuery = "SELECT Count(*) \n" +
			"FROM   (SELECT DISTINCT tr.truck_number   AS truckNumber, 	lp.loadslip_id    AS loadslipId,tr.reporting_date AS reportDate  FROM   truck_reporting tr LEFT JOIN loadslip lp on ( lp.shipment_id = tr.shipment_id OR lp.shipment_id IS NULL ) and ( (lp.source_loc = tr.reporting_location  OR lp.source_loc IS NULL) or (tr.reporting_location=lp.dest_loc) )  AND ( lp.status NOT IN( 'CANCELLED' ) OR lp.status IS NULL )\n" +
			"               LEFT JOIN shipment shipment ON tr.shipment_id = shipment.shipment_id  LEFT JOIN indent_summary isu  ON isu.indent_id = tr.indent_id  WHERE  tr.truck_number IS NOT NULL \n" +
			"               AND tr.reporting_location = ?1   AND tr.status IN(?2) ) ts ")
  Page<Map<String, Object>> getTrucksMovementBySourceLoc(String plantCode ,List<String> statuses, Pageable pageable);

	@Query(nativeQuery = true, value = "SELECT DISTINCT isu.item_category  FROM truck_reporting tr LEFT JOIN indent_summary isu on isu.indent_id =tr.indent_id  WHERE tr.truck_number= ?1  and  tr.status in(?2) and tr.reporting_location = ?3 ")
	String findTruckIndentCatgory(String truckNumber, List<String> status, String sourceLoc);

	@Query(nativeQuery = true, value = "SELECT DISTINCT isu.item_category  FROM truck_reporting tr LEFT JOIN indent_summary isu on isu.indent_id =tr.indent_id  WHERE tr.truck_number= ?1  and isu.indent_id=?2 ")
	String findTruckIndentCatgoryByShipmentId(String truckNumber, String indentId);

	@Query(nativeQuery = true, value = "select distinct t.shipment_id from truck_reporting t where  t.status = 'INTRANSIT' AND t.reporting_location != ?1 ")
	List<String> findDistinctIntransitShipmentsId(String reportLocation);

  @Query(nativeQuery = true, value = "select distinct t.shipment_id from truck_reporting t where  t.status = 'INTRANSIT' ")
  List<String> findDistinctIntransitShipmentsIdForDprep();
}
