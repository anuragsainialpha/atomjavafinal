package com.api.apollo.atom.entity.ops;

import com.api.apollo.atom.constant.Constants.BayStatus;
import com.api.apollo.atom.constant.Constants.SystemType;
import com.api.apollo.atom.constant.Constants.TruckReportStatus;
import com.api.apollo.atom.entity.master.MTTruck;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "TRUCK_REPORTING")
@Getter
@Setter
@NoArgsConstructor
public class TruckReport {

  @Id
  @Column(name = "GATE_CONTROL_CODE")
  private String gateControlCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "INDENT_ID", referencedColumnName = "INDENT_ID")
  @JsonBackReference
  private IndentSummary indentSummary;

  @Column(name = "TYPE")
  @Enumerated(EnumType.STRING)
  private SystemType type;

  @Column(name = "TRANSPORTER_SAP_CODE")
  private String transporterSapCode;

  @Column(name = "CONTAINER_NUM")
  private String containerNum;

  @Column(name = "CONTAINER_CODE")
  private String containerCode;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "TRUCK_NUMBER", referencedColumnName = "TRUCK_NUMBER")
  private MTTruck truck;

  @Column(name = "DRIVER_NAME")
  private String driverName;

  @Column(name = "DRIVER_MOBILE")
  private String driverMobile;

  @Column(name = "DRIVER_LICENSE")
  private String driverLicense;

  @Column(name = "SERVPROV")
  private String servprov;

  @Column(name = "TRUCK_TYPE")
  private String truckType;

  @Column(name = "REPORTING_LOCATION")
  private String reportLocation;

  @Column(name = "SOURCE_LOC")
  private String sourceLocation;

  @Column(name = "DEST_LOC")
  private String destinationLocation;

  @Column(name = "REPORTING_DATE")
  private Date reportDate;

  @Column(name = "GATEIN_DATE")
  private Date gateInDate;
  @Column(name = "GATEOUT_DATE")
  private Date gateOutDate;
  @Column(name = "DEST_GEOFENCEIN_DATE")
  private Date destGeofenceDate;
  @Column(name = "DEREPORTING_DATE")
  private Date dereportDate;

  @Column(name = "REP_GI_HRS")
  private Double waitTimeHrs;

  @Column(name = "GI_GO_HRS")
  private Double tTHrs;

  @Column(name = "STATUS")
  @Enumerated(EnumType.STRING)
  private TruckReportStatus status;

  @Column(name = "REJ_STATUS")
  @Enumerated(EnumType.STRING)
  private TruckReportStatus rejectedStatus;

  @Column(name = "IN_WEIGHT")
  private Double inWeight;

  @Column(name = "OUT_WEIGHT")
  private Double outWeight;

  @Column(name = "NET_WEIGHT")
  private Double netWeight;

  @Column(name = "BAY")
  private String bay;

  @Column(name = "BAY_STATUS")
  @Enumerated(EnumType.STRING)
  private BayStatus bayStatus;

  @Column(name = "REJECTION_CODE")
  private String rejectionCode;

  @Column(name = "ACTUAL_TRUCK_TYPE")
  private String actualTruckType;

  @Column(name = "REF_CODE")
  private String refCode;

  @Column(name = "INSERT_USER")
  private String insertUser;

  @Column(name = "UPDATE_USER")
  private String updateUser;

  @Column(name = "INSERT_DATE")
  private Date insertDate = new Date();

  @Column(name = "UPDATE_DATE")
  private Date updateDate;

  @Column(name = "Shipment_ID")
  private String shipmentID;

  @Column(name = "ETA_DESTINATION")
  private Date EtaDest;

  @Column(name = "REPORTED_TRUCK_TYPE")
  private String reportedTruckType;

  @Transient
  private String loadslipUSDate;

  @Transient
  private String loadslipUEDate;

  @Transient
  private String releaseDate;

  @Transient
  private boolean truckReported;

  @Column(name = "ACTIVITY")
  private  String activity;

  @Column(name = "COMMENTS")
  private  String comments;

  @Column(name = "DEST_COUNTRY")
  private String destCountry;

  @Transient
  private String gpsProvider;

  @Transient
  private String gpsEnabled;

  @Transient
  private String truckNumber;

  @Transient
  private String indentId;

  @Transient
  private String loadslipComments;

  @Column(name = "E_WAY_BILL_NO")
  private String eWayBillNo;

  @Column(name = "SAP_INV_VALUE")
  private Integer sapInvValue;

  @Column(name = "SAP_INV_WEIGHT")
  private Integer sapInvWeight;

  @Transient
  private String ctDiffWt;

  @Transient
  private String rejectionDesc;

  @Transient
  private String destCountryName;

  @Transient
  private String ftTripId;


  @Column(name = "REP_GO_HRS")
  private Double repGoHrs;

  @Column(name = "GI_REL_HRS")
  private Double giRelHrs;

  @Column(name = "LO_REL_HRS")
  private Double loRelHrs;

  @Column(name = "REL_GO_HRS")
  private Double relGoHrs;

  @Column(name = "IS_PUC")
  private String isPuc = "Y";

  @Column(name = "IS_INSURANCE")
  private String isInsurance = "Y";

  @Column(name = "IS_SEATBELT")
  private String isSeatBelt = "Y";

  @Column(name = "IS_FIRST_AID")
  private String isFirstAid = "Y";

  @Column(name = "IS_FIRE_EXTENGUISHER")
  private String  isFireExtenguisher = "Y";

  @Column(name = "IS_EMERGENCY_CARD")
  private String isEmergencyCard = "Y";

  @Column(name = "IS_SPARK_ARRESTOR")
  private String isSparKArrestor = "Y";

  @Column(name = "IS_FITNESS_CERT")
  private String isFitnessCert = "Y";

  @Column(name = "TT_DAYS")
  private Double ttDays;

  //added new columns in the truck_reporting for consent
  @Column(name = "TRACKING_CONSENT_STATUS")
  private String trackingConsentStatus;

  //added new columns in the truck_reporting for consent
  @Column(name = "CONSENT_PHONE_TELECOM")
  private String consentPhoneTelecom;

  //April 2023 - Change
  @Column(name = "TRUCK_CAPACITY")
  private Double truckCapacity;

  @Column(name = "GROSS_VEHICLE_WT")
  private Double truckGrossVehicleWt;


  @Column(name = "UNLADEN_WT")
  private Double truckUnladenWt;

  @Column(name = "BS_NORMS")
  private String bsNorms;

  @Column(name = "FUEL_TYPE")
  private String fuelType;


  @Transient
  private Integer totTyres;

  @Transient
  private Integer totTubes;

  @Transient
  private Integer totFlaps;

  @Transient
  private Integer totValve;

  @Transient
  private Integer totPctr;

  @Transient
  private Integer otherQty;

  @Transient
  private Integer totQty;

  @Transient
  private String lrNum;

  @Transient
  private Date lrDate;

  @Transient
  private String indentCategory;

  @Transient
  private String sapInvoice;


  public TruckReport(Double waitTimeHrs, TruckReportStatus status, Date gateInDate, Date reportDate) {
    this.reportDate = reportDate;
    this.gateInDate = gateInDate;
    this.waitTimeHrs = waitTimeHrs;
    this.status = status;
  }

  /*Used only for Optimizing trucks-info api*/
  public TruckReport(Map<String,Object> mapTruckReport){
    this.gateControlCode = mapTruckReport.get("GATE_CONTROL_CODE") != null ? (String) mapTruckReport.get("GATE_CONTROL_CODE") : null;
    this.type = mapTruckReport.get("TYPE") != null ? SystemType.valueOf(mapTruckReport.get("TYPE").toString()) : null;
    this.transporterSapCode = mapTruckReport.get("TRANSPORTER_SAP_CODE") != null ?(String) mapTruckReport.get("TRANSPORTER_SAP_CODE") : null;
    this.containerNum = mapTruckReport.get("CONTAINER_NUM") != null ?(String) mapTruckReport.get("CONTAINER_NUM") : null;
    this.containerCode = mapTruckReport.get("CONTAINER_CODE") != null ? (String) mapTruckReport.get("CONTAINER_CODE") : null;
    this.driverName = mapTruckReport.get("DRIVER_NAME") != null ? (String) mapTruckReport.get("DRIVER_NAME") : null;
    this.driverMobile = mapTruckReport.get("DRIVER_MOBILE") != null ? (String) mapTruckReport.get("DRIVER_MOBILE") : null;
    this.driverLicense = mapTruckReport.get("DRIVER_LICENSE") != null ? (String) mapTruckReport.get("DRIVER_LICENSE") : null;
    this.servprov =mapTruckReport.get("SERVPROV") != null ? (String) mapTruckReport.get("SERVPROV") : null;
    this.truckType = mapTruckReport.get("TRUCK_TYPE") != null ?(String) mapTruckReport.get("TRUCK_TYPE") : null;
    this.reportLocation = mapTruckReport.get("REPORTING_LOCATION") != null ?(String) mapTruckReport.get("REPORTING_LOCATION") : null;
    this.sourceLocation = mapTruckReport.get("SOURCE_LOC") != null ? mapTruckReport.get("SOURCE_LOC").toString() : null;
    this.destinationLocation = mapTruckReport.get("DEST_LOC") != null ? (String) mapTruckReport.get("DEST_LOC") : null;
    this.reportDate = mapTruckReport.get("REPORTING_DATE") != null ? (Date) mapTruckReport.get("REPORTING_DATE") : null;
    this.gateInDate = mapTruckReport.get("GATEIN_DATE") != null ? (Date) mapTruckReport.get("GATEIN_DATE") :null;
    this.gateOutDate =mapTruckReport.get("GATEOUT_DATE") != null ? (Date) mapTruckReport.get("GATEOUT_DATE") : null;
    this.destGeofenceDate = mapTruckReport.get("DEST_GEOFENCEIN_DATE") != null ? (Date) mapTruckReport.get("DEST_GEOFENCEIN_DATE") : null;
    this.dereportDate =mapTruckReport.get("DEREPORTING_DATE")!= null ? (Date) mapTruckReport.get("DEREPORTING_DATE") : null;
    this.waitTimeHrs = mapTruckReport.get("REP_GI_HRS") != null ? ((BigDecimal)mapTruckReport.get("REP_GI_HRS")).doubleValue() : null;
    this.tTHrs = mapTruckReport.get("GI_GO_HRS") != null ? ((BigDecimal) mapTruckReport.get("GI_GO_HRS")).doubleValue() : null;
    this.status =mapTruckReport.get("STATUS") != null ? TruckReportStatus.valueOf(mapTruckReport.get("STATUS").toString()) : null;
    this.rejectedStatus = mapTruckReport.get("REJ_STATUS") !=null ? TruckReportStatus.valueOf(mapTruckReport.get("REJ_STATUS").toString()) : null;
    this.inWeight = mapTruckReport.get("IN_WEIGHT") != null ? ((BigDecimal) mapTruckReport.get("IN_WEIGHT")).doubleValue() : null;
    this.outWeight = mapTruckReport.get("OUT_WEIGHT") != null ? ((BigDecimal) mapTruckReport.get("OUT_WEIGHT")).doubleValue() : null;
    this.netWeight = mapTruckReport.get("NET_WEIGHT") != null ? ((BigDecimal) mapTruckReport.get("NET_WEIGHT")).doubleValue() : null;
    this.bay = mapTruckReport.get("BAY") != null ? (String) mapTruckReport.get("BAY")  : null;
    this.bayStatus = mapTruckReport.get("BAY_STATUS") != null ? BayStatus.valueOf(mapTruckReport.get("BAY_STATUS").toString()) : null;
    this.rejectionCode =mapTruckReport.get("REJECTION_CODE") != null ? (String) mapTruckReport.get("REJECTION_CODE") : null;
    this.actualTruckType = mapTruckReport.get("ACTUAL_TRUCK_TYPE") != null ? (String) mapTruckReport.get("ACTUAL_TRUCK_TYPE") : null;
    this.refCode =mapTruckReport.get("REF_CODE") != null ?  (String) mapTruckReport.get("REF_CODE") : null;
    this.insertUser = mapTruckReport.get("INSERT_USER") != null ? (String) mapTruckReport.get("INSERT_USER") : null;
    this.updateUser = mapTruckReport.get("UPDATE_USER") != null ? (String) mapTruckReport.get("UPDATE_USER") : null;
    this.insertDate =mapTruckReport.get("INSERT_DATE")!= null ? (Date) mapTruckReport.get("INSERT_DATE") : null;
    this.updateDate =mapTruckReport.get("UPDATE_DATE")!= null ? (Date) mapTruckReport.get("UPDATE_DATE") : null;
    this.shipmentID = mapTruckReport.get("Shipment_ID") != null ?(String) mapTruckReport.get("Shipment_ID") : null;
    this.EtaDest = mapTruckReport.get("ETA_DESTINATION") != null ?(Date) mapTruckReport.get("ETA_DESTINATION") : null;
    this.reportedTruckType =mapTruckReport.get("REPORTED_TRUCK_TYPE") != null ? (String) mapTruckReport.get("REPORTED_TRUCK_TYPE") : null;
    this.activity = mapTruckReport.get("ACTIVITY") != null ?(String) mapTruckReport.get("ACTIVITY") : null;
    this.comments = mapTruckReport.get("COMMENTS") != null ?(String) mapTruckReport.get("COMMENTS") : null;
    this.gpsEnabled = mapTruckReport.get("GPS_ENABLED") !=null ?(String) mapTruckReport.get("GPS_ENABLED") : null;
    this.gpsProvider = mapTruckReport.get("GPS_PROVIDER") != null ?(String) mapTruckReport.get("GPS_PROVIDER") : null;
    this.truckNumber = mapTruckReport.get("TRUCK_NUMBER") != null ?(String) mapTruckReport.get("TRUCK_NUMBER") : null;
    this.indentId =mapTruckReport.get("INDENT_ID") != null ? (String) mapTruckReport.get("INDENT_ID") : null;
    this.eWayBillNo = mapTruckReport.get("E_WAY_BILL_NO") != null ?(String) mapTruckReport.get("E_WAY_BILL_NO") : null;
    this.sapInvValue = mapTruckReport.get("SAP_INV_VALUE") != null ?((BigDecimal) mapTruckReport.get("SAP_INV_VALUE")).intValue() : null;
    this.sapInvWeight = mapTruckReport.get("SAP_INV_WEIGHT") != null ?((BigDecimal) mapTruckReport.get("SAP_INV_WEIGHT")).intValue() : null;
    this.destCountry = mapTruckReport.get("DEST_COUNTRY") != null ?(String) mapTruckReport.get("DEST_COUNTRY") : null;
    this.isPuc = mapTruckReport.get("IS_PUC") != null ?(String)mapTruckReport.get("IS_PUC") : null;
    this.isInsurance =mapTruckReport.get("IS_INSURANCE") != null ? (String)mapTruckReport.get("IS_INSURANCE") : null;
    this.isSeatBelt = mapTruckReport.get("IS_SEATBELT") != null ?(String)mapTruckReport.get("IS_SEATBELT") : null;
    this.isFirstAid = mapTruckReport.get("IS_FIRST_AID") != null ?(String)mapTruckReport.get("IS_FIRST_AID") : null;
    this.isFireExtenguisher = mapTruckReport.get("IS_FIRE_EXTENGUISHER") != null ? (String)mapTruckReport.get("IS_FIRE_EXTENGUISHER") : null;
    this.isEmergencyCard = mapTruckReport.get("IS_EMERGENCY_CARD") != null ?(String)mapTruckReport.get("IS_EMERGENCY_CARD") : null;
    this.isSparKArrestor =mapTruckReport.get("IS_SPARK_ARRESTOR") != null ? (String)mapTruckReport.get("IS_SPARK_ARRESTOR") : null;
    this.isFitnessCert = mapTruckReport.get("IS_FITNESS_CERT") != null ? (String)mapTruckReport.get("IS_FITNESS_CERT") : null;
    this.truckCapacity = mapTruckReport.get("TRUCK_CAPACITY") != null ? ((BigDecimal) mapTruckReport.get("TRUCK_CAPACITY")).doubleValue() : null;
    this.truckGrossVehicleWt = mapTruckReport.get("GROSS_VEHICLE_WT") != null ? ((BigDecimal) mapTruckReport.get("GROSS_VEHICLE_WT")).doubleValue() : null;
    this.truckUnladenWt = mapTruckReport.get("UNLADEN_WT") != null ? ((BigDecimal) mapTruckReport.get("UNLADEN_WT")).doubleValue() : null;
    this.ftTripId = mapTruckReport.get("FT_TRIP_ID") != null ? mapTruckReport.get("FT_TRIP_ID").toString() : null;
    this.indentCategory = mapTruckReport.get("indentCategory") != null ? mapTruckReport.get("indentCategory").toString() : null;
    this.trackingConsentStatus = mapTruckReport.get("TRACKING_CONSENT_STATUS") != null ? mapTruckReport.get("TRACKING_CONSENT_STATUS").toString() : null;
    this.consentPhoneTelecom = mapTruckReport.get("CONSENT_PHONE_TELECOM") != null ? mapTruckReport.get("CONSENT_PHONE_TELECOM").toString() : null;
    this.bsNorms = mapTruckReport.get("BS_NORMS") != null ? mapTruckReport.get("BS_NORMS").toString() : null;
    this.fuelType = mapTruckReport.get("FUEL_TYPE") != null ? mapTruckReport.get("FUEL_TYPE").toString() : null;

  }
}
