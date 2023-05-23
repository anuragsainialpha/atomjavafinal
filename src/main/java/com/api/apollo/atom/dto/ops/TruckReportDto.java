package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.Constants.BayStatus;
import com.api.apollo.atom.constant.Constants.TruckReportStatus;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.master.MTTruck;
import com.api.apollo.atom.entity.ops.IndentDetails;
import com.api.apollo.atom.entity.ops.TruckReport;
import com.api.apollo.atom.entity.ops.TruckReportSummary;
import com.api.apollo.atom.util.DateUtils;
import com.api.apollo.atom.util.Utility;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@NoArgsConstructor
public class TruckReportDto {

  private String source;
  private String truckNumber;
  private String truckType;
  private String driverName;
  private String driverContact;
  private String driverLicense;
  private boolean gpsEnabled;
  private String gpsProvider;
  private String indentId;
  private String gateControlCode;
  private String containerNum;
  private String destination;
  private String destDis;
  private String transporter;
  private String shipmentId;
  private String reportedDate;
  private String gateInDate;
  private String gateOutDate;
  private String bayAssigned;
  private String rejectionStatus;
  private String status;
  private String editTruckStatus;
  private String loadslipUSDate;
  private String loadslipUEDate;
  private String releaseDate;
  private String EtaDest;
  private String tTHrs;
  private Double inWeight;
  private Double outWeight;
  private Double netWeight;
  private String reportLocation;
  private String trucksLoadingIn;
  private Integer excessWaitingLocCount;
  private String trackingConsentStatus;
  private String consentPhoneTelecom;

  private Long excessWaitingTime;

  private Long excessReportingWaitingTime;

  //truck report flags
  private boolean isPuc = Boolean.TRUE;
  private boolean isInsurance = Boolean.TRUE;
  private boolean isSeatBelt = Boolean.TRUE;
  private boolean isFirstAid = Boolean.TRUE;
  private boolean  isFireExtenguisher = Boolean.TRUE;
  private boolean isEmergencyCard = Boolean.TRUE;
  private boolean isSparKArrestor = Boolean.TRUE;
  private boolean isFitnessCert = Boolean.TRUE;

  //April 2023
  private Double truckCapacity;
  private Double truckGrossVehicleWt;
  private Double truckUnladenWt;
  private String bsNorms;
  private String fuelType;

  private boolean isGateIn = Boolean.FALSE;
  private boolean isGateOut = Boolean.FALSE;
  private String callStatus;
  private String bayStatus;

  private Integer reportedTrucksCount;
  private Integer gatedInTrucksCount;
  private Integer watingMoreThen3HTrucks;
  private Date editGateInDate;
  private Date editReportedDate;
  private boolean truckReported;
  private boolean intransitTruck;

  private String comments;

  private String activity;

  private String updateUser;

  private String insertUser;

  private String loadslipComments;

  private String eWayBillNo;

  private Integer sapInvValue;

  private Integer sapInvWeight;

 // List<CTRejectionDto> rejectionCodes = new ArrayList<>();

  private Double diffInvWeight;

  private String rejectionCode;

  private String rejectionDesc;

  private Boolean weightDiffStatus= Boolean.FALSE;

  private Boolean inWeightStatus= Boolean.FALSE;

  private Boolean outWeightStatus= Boolean.FALSE;

  private String destCountry;

  private String destCountryName;

  private String ftTripId;

  private String lrNum;

  private String lrDate;

  private Integer totTyre;

  private Integer totTube;

  private Integer totValve;

  private Integer totPctr;

  private Integer otherQty;

  private Integer totQty;

  private Integer totFlap;

  private String indentCategory;

  private String sapInvoice;


  public String validateTruckReport() {
    List<String> errorMessages = new ArrayList<String>();
    if (StringUtils.isEmpty(this.truckNumber))
      errorMessages.add("truck number");
    if (StringUtils.isEmpty(this.truckType))
      errorMessages.add("truck type");
    if (StringUtils.isEmpty(this.driverName))
      errorMessages.add("driver name");
    if (StringUtils.isEmpty(this.driverContact))
      errorMessages.add("driver contact");
    if (StringUtils.isEmpty(this.driverLicense))
      errorMessages.add("driver license");
    if (this.gpsEnabled && StringUtils.isEmpty(this.gpsProvider))
      errorMessages.add("gps provider");
    if (StringUtils.isEmpty(this.indentId))
      errorMessages.add("indent id");
    return errorMessages.stream().collect(Collectors.joining(","));
  }

  public String validateReportedTruck() {
    List<String> errorMessages = new ArrayList<String>();
    if (StringUtils.isEmpty(this.gateControlCode))
      errorMessages.add("Gate control code");
    if (StringUtils.isEmpty(this.status))
      errorMessages.add("Status");
    if (StringUtils.isEmpty(this.indentId))
      errorMessages.add("indent id");
    return errorMessages.stream().collect(Collectors.joining(","));
  }

  public String validateTruckBayAssign() {
    List<String> errorMessages = new ArrayList<String>();
    if (StringUtils.isEmpty(this.gateControlCode))
      errorMessages.add("Gate control code");
    if (StringUtils.isEmpty(this.status))
      errorMessages.add("Status");
    if (StringUtils.isEmpty(this.indentId))
      errorMessages.add("indent id");
/*		if (StringUtils.isEmpty(this.bayAssigned))
			errorMessages.add("Bay id");*/
    return errorMessages.stream().collect(Collectors.joining(","));
  }

  public String validateToRejectTruck() {
    List<String> errorMessages = new ArrayList<String>();
    if (StringUtils.isEmpty(this.gateControlCode))
      errorMessages.add("Gate control code");
    if (StringUtils.isEmpty(this.status))
      errorMessages.add("Status");
    if (StringUtils.isEmpty(this.truckNumber)) {
      errorMessages.add("truckNumber");
    }
    return errorMessages.stream().collect(Collectors.joining(","));
  }

  public TruckReportDto(IndentDetails indentDetails) {
    MTTruck truck = indentDetails.getTruck();
    this.truckNumber = truck != null ? truck.getTruckNumber() : null;
    this.truckType = truck != null && truck.getTtId()!= null ? truck.getTtId().getType() : null;
    this.driverName = indentDetails.getDriverName();
    this.driverContact = indentDetails.getDriverMobile();
    this.driverLicense = indentDetails.getDriverLicense();
    this.gpsEnabled = indentDetails.getGpsEnabled().equals("Y");
    this.gpsProvider = indentDetails.getGpsProvider();
    this.containerNum = indentDetails.getContainerNum();
    this.indentId = indentDetails.getIndent().getIndentId();
    this.updateUser = indentDetails.getUpdateUser();
    this.insertUser = indentDetails.getInsertUser();
  }

  /*Optimized getIndents() API*/
  public TruckReportDto(IndentDetails indentDetails, String dummyVariable) {
    this.truckNumber = indentDetails.getTruckNumber();
    this.truckType = indentDetails.getTruckType();
    this.driverName = indentDetails.getDriverName();
    this.driverContact = indentDetails.getDriverMobile();
    this.driverLicense = indentDetails.getDriverLicense();
    this.gpsEnabled = indentDetails.getGpsEnabled().equals("Y");
    this.gpsProvider = indentDetails.getGpsProvider();
    this.containerNum = indentDetails.getContainerNum();
    this.indentId = indentDetails.getIndentId();
    this.updateUser = indentDetails.getUpdateUser();
    this.insertUser = indentDetails.getInsertUser();
  }

  public TruckReportDto(TruckReport truckReport) {
    MTTruck truck = truckReport.getTruck();
    this.gpsEnabled = truck.getGpsEnabled().equals("Y");
    this.gpsProvider = truck.getGpsProvider() != null ? truck.getGpsProvider().getGpsProvider() : "";
    this.truckNumber = truck.getTruckNumber();
    this.indentId = truckReport.getIndentSummary().getIndentId();
//    this.indentCategory = truckReport.getIndentSummary().getCategory();
    setTruckDetails(truckReport);
  }

  public TruckReportDto(IndentDetails indentDetails, TruckReport truckReport, ApplicationUser loggedInUser) {
    this(truckReport);
    if (truckReport.getStatus() != null && truckReport.getBayStatus() != null && (truckReport.getStatus().equals(TruckReportStatus.REPORTED) || truckReport.getStatus().equals(TruckReportStatus.GATED_IN)) /*&& !StringUtils.isEmpty(truckReport.getBay())*/ && truckReport.getBayStatus().equals(BayStatus.CALL)) {
      this.callStatus = loggedInUser.getRole().isRoleGateSecurity() ? Constants.GAT_CALL_STATUS : Constants.PLT_CALL_STATUS;
      this.bayStatus = truckReport.getBayStatus() != null ? truckReport.getBayStatus().name() : null;
    }

  }

  /*Constructor with dummy variable
  * used for optimizing trucks-info*/
  public TruckReportDto(TruckReport truckReport,String ctDiffWt) {
    this.gpsEnabled = truckReport.getGpsEnabled() != null ? (truckReport.getGpsEnabled().equals("Y")) : null;
    this.gpsProvider = truckReport.getGpsProvider() != null ? truckReport.getGpsProvider() : "";
    this.truckNumber = truckReport.getTruckNumber();
    this.indentId = truckReport.getIndentId();
    this.trackingConsentStatus = truckReport.getTrackingConsentStatus();
    this.consentPhoneTelecom = truckReport.getConsentPhoneTelecom();
    setTruckDetails(truckReport);
  }

  private void setTruckDetails(TruckReport truckReport) {
    this.driverName = truckReport.getDriverName();
    this.driverContact = truckReport.getDriverMobile();
    this.driverLicense = truckReport.getDriverLicense();
    this.gateControlCode = truckReport.getGateControlCode();
    this.tTHrs = truckReport.getTTHrs() != null ? truckReport.getTTHrs().toString() : null;
    if (truckReport.getEtaDest() != null) {
      this.EtaDest = DateUtils.formatDate(truckReport.getEtaDest(), Constants.PLAN_RECORD_DATE_FORMAT);
    }
    this.activity = truckReport.getActivity();


    this.source = truckReport.getSourceLocation();
    this.destination = truckReport.getDestinationLocation();
    this.truckType =  truckReport.getActualTruckType() != null ? truckReport.getActualTruckType() :truckReport.getTruckType();
//    this.truckType = truckReport.getReportedTruckType() != null ? truckReport.getReportedTruckType() : "";
//    this.transporter = Utility.camelCase(truck.getServprov());
    this.transporter = Utility.camelCase(truckReport.getServprov());
    this.reportedDate = DateUtils.formatDate(truckReport.getReportDate(), Constants.DATE_TIME_FORMAT);
    if (truckReport.getGateInDate() != null) {
      this.gateInDate = DateUtils.formatDate(truckReport.getGateInDate(), Constants.DATE_TIME_FORMAT);
      this.isGateIn = Boolean.TRUE;
    }
    if (truckReport.getGateOutDate() != null) {
      this.gateOutDate = DateUtils.formatDate(truckReport.getGateOutDate(), Constants.DATE_TIME_FORMAT);
      this.isGateOut = Boolean.TRUE;
    }
    this.bayAssigned = truckReport.getBay();
    this.status = Utility.camelCase(truckReport.getStatus().name());
    this.rejectionStatus = Utility.camelCase(truckReport.getRejectedStatus().name());
    this.inWeight = truckReport.getInWeight();
    this.outWeight = truckReport.getOutWeight();
    if (!StringUtils.isEmpty(truckReport.getOutWeight()) && !StringUtils.isEmpty(truckReport.getInWeight()))
      this.netWeight = Math.abs((truckReport.getOutWeight() - truckReport.getInWeight()));
    this.inWeightStatus = (!StringUtils.isEmpty(this.inWeight) && this.inWeight !=0.0) ? true : false;
    this.outWeightStatus = (!StringUtils.isEmpty(this.outWeight) && this.outWeight !=0.0) ? true : false;

    this.reportLocation = truckReport.getReportLocation();
    // For RDC Inventory trucks
    if (truckReport.getLoadslipUSDate() != null) {
      this.loadslipUSDate = truckReport.getLoadslipUSDate();
    }
    if (truckReport.getLoadslipUEDate() != null) {
      this.loadslipUEDate = truckReport.getLoadslipUEDate();
    }
    if (truckReport.getReleaseDate() != null) {
      // this.releaseDate = truckReport.getReleaseDate();
    }
    this.containerNum = truckReport.getContainerNum();
    // Truck Reporting is having Ref_code(not empty and not null) then it is intransit truck(Incomimg truck) else Shipment truck
    this.intransitTruck = !StringUtils.isEmpty(truckReport.getRefCode()) /*&& !StringUtils.isEmpty(truckReport.getActivity()) && truckReport.getActivity().equals("D")*/;
    this.shipmentId = truckReport.getShipmentID();
    this.insertUser = truckReport.getInsertUser();
    this.updateUser = truckReport.getUpdateUser();
    this.loadslipComments = truckReport.getLoadslipComments();
    this.eWayBillNo=truckReport.getEWayBillNo();
    this.sapInvValue=truckReport.getSapInvValue();
    this.sapInvWeight=truckReport.getSapInvWeight();
    if (!StringUtils.isEmpty(this.netWeight) && this.netWeight !=0.0 && !StringUtils.isEmpty(this.sapInvWeight))
      this.diffInvWeight =Math.abs((this.netWeight) - (this.sapInvWeight));
    this.rejectionCode=truckReport.getRejectionCode();
    if (!StringUtils.isEmpty(truckReport.getCtDiffWt()))
      this.weightDiffStatus = (this.diffInvWeight != null ? this.diffInvWeight : 0.0) > (Double.valueOf(truckReport.getCtDiffWt()));
    this.rejectionDesc=truckReport.getRejectionDesc();
    this.destCountry = truckReport.getDestCountry();
    this.destCountryName = truckReport.getDestCountryName();

    this.isPuc = !StringUtils.isEmpty(truckReport.getIsPuc()) && truckReport.getIsPuc().equalsIgnoreCase("Y");
    this.isInsurance =  !StringUtils.isEmpty(truckReport.getIsInsurance()) && truckReport.getIsInsurance().equalsIgnoreCase("Y");
    this.isSeatBelt =  !StringUtils.isEmpty(truckReport.getIsSeatBelt()) && truckReport.getIsSeatBelt().equalsIgnoreCase("Y");
    this.isFirstAid =  !StringUtils.isEmpty(truckReport.getIsFirstAid()) && truckReport.getIsFirstAid().equalsIgnoreCase("Y");
    this.isFireExtenguisher =  !StringUtils.isEmpty(truckReport.getIsFireExtenguisher())&& truckReport.getIsFireExtenguisher().equalsIgnoreCase("Y");
    this.isEmergencyCard =  !StringUtils.isEmpty(truckReport.getIsEmergencyCard())&& truckReport.getIsEmergencyCard().equalsIgnoreCase("Y");
    this.isSparKArrestor = !StringUtils.isEmpty(truckReport.getIsSparKArrestor())&& truckReport.getIsSparKArrestor().equalsIgnoreCase("Y");
    this.isFitnessCert = !StringUtils.isEmpty(truckReport.getIsFitnessCert())&& truckReport.getIsFitnessCert().equalsIgnoreCase("Y");
    //April 2023
    this.truckCapacity = truckReport.getTruckCapacity();
    this.bsNorms = truckReport.getBsNorms();
    this.fuelType = truckReport.getFuelType();
    this.truckGrossVehicleWt = truckReport.getTruckGrossVehicleWt();
    this.truckUnladenWt = truckReport.getTruckUnladenWt();
    this.ftTripId = truckReport.getFtTripId();
    this.totFlap = truckReport.getTotFlaps();
    this.totPctr = truckReport.getTotPctr();
    this.totQty = truckReport.getTotQty();
    this.totTube = truckReport.getTotTubes();
    this.totTyre = truckReport.getTotTyres();
    this.totValve = truckReport.getTotValve();
    this.otherQty = truckReport.getOtherQty();
    this.lrNum = truckReport.getLrNum();
    this.lrDate = truckReport.getLrDate() != null ? DateUtils.formatDate(truckReport.getLrDate(), Constants.DATE_TIME_FORMAT) : null;
    this.indentCategory = truckReport.getIndentSummary() != null ? truckReport.getIndentSummary().getCategory() : truckReport.getIndentCategory();
    this.sapInvoice = truckReport.getSapInvoice();
    this.trackingConsentStatus = truckReport.getTrackingConsentStatus();
    this.consentPhoneTelecom = truckReport.getConsentPhoneTelecom();
  }

  public TruckReportDto(TruckReport truckReport, ApplicationUser loggedInUser) {
    this(truckReport);
    if (truckReport.getBayStatus() != null) {
      this.bayStatus = truckReport.getBayStatus().name();
      this.callStatus = loggedInUser.getRole().isRoleGateSecurity() ? Constants.GAT_CALL_STATUS : Constants.PLT_CALL_STATUS;
    }
    this.truckReported = truckReport.isTruckReported();
    this.comments = truckReport.getComments();
    this.trackingConsentStatus = truckReport.getTrackingConsentStatus();
    this.consentPhoneTelecom = truckReport.getConsentPhoneTelecom();
    this.bsNorms = truckReport.getBsNorms();
    this.fuelType = truckReport.getFuelType();
  }

  /*Optimized Truck->Inventory->Shipping/Receiving*/
  public TruckReportDto(TruckReport truckReport, ApplicationUser loggedInUser,String didNotUse) {
    this(truckReport,didNotUse);
    if (truckReport.getBayStatus() != null) {
      this.bayStatus = truckReport.getBayStatus().name();
      this.callStatus = loggedInUser.getRole().isRoleGateSecurity() ? Constants.GAT_CALL_STATUS : Constants.PLT_CALL_STATUS;
    }
    this.truckReported = truckReport.isTruckReported();
    this.comments = truckReport.getComments();
    this.trackingConsentStatus = truckReport.getTrackingConsentStatus();
    this.consentPhoneTelecom = truckReport.getConsentPhoneTelecom();
    this.bsNorms = truckReport.getBsNorms();
    this.fuelType = truckReport.getFuelType();
  }

  public TruckReportDto(Integer reportedTrucksCount, Integer gatedInTrucksCount, Integer watingMoreThen3HTrucks) {
    this.reportedTrucksCount = reportedTrucksCount;
    this.gatedInTrucksCount = gatedInTrucksCount;
    this.watingMoreThen3HTrucks = watingMoreThen3HTrucks;
  }

  public TruckReportDto(TruckReportSummary truckReportSummary) {
    this.reportedTrucksCount = truckReportSummary.getReportedCount();
    this.gatedInTrucksCount = truckReportSummary.getGatedInCount();
    this.watingMoreThen3HTrucks = truckReportSummary.getExcessWaitingLocCount();
    this.excessWaitingLocCount = truckReportSummary.getExcessWaitingCount();
    this.trucksLoadingIn = truckReportSummary.getTrucksLoadingIn();


  }

//  OLD CONSTRUCTOR LINES
  /*this.driverName = truckReport.getDriverName();
    this.driverContact = truckReport.getDriverMobile();
    this.driverLicense = truckReport.getDriverLicense();
    this.gateControlCode = truckReport.getGateControlCode();
    this.tTHrs = truckReport.getTTHrs() != null ? truckReport.getTTHrs().toString() : null;
    if (truckReport.getEtaDest() != null)
      this.EtaDest = DateUtils.formatDate(truckReport.getEtaDest(), Constants.PLAN_RECORD_DATE_FORMAT);
    this.activity = truckReport.getActivity();


    this.source = truckReport.getSourceLocation();
    this.destination = truckReport.getDestinationLocation();
    this.truckType = truckReport.getActualTruckType() != null ? truckReport.getActualTruckType() : truckReport.getTruckType();
//    this.truckType = truckReport.getReportedTruckType() != null ? truckReport.getReportedTruckType() : "";
//    this.transporter = Utility.camelCase(truck.getServprov());
    this.transporter = Utility.camelCase(truckReport.getServprov());
    this.reportedDate = DateUtils.formatDate(truckReport.getReportDate(), Constants.DATE_TIME_FORMAT);
    if (truckReport.getGateInDate() != null) {
      this.gateInDate = DateUtils.formatDate(truckReport.getGateInDate(), Constants.DATE_TIME_FORMAT);
      this.isGateIn = Boolean.TRUE;
    }
    if (truckReport.getGateOutDate() != null) {
      this.gateOutDate = DateUtils.formatDate(truckReport.getGateOutDate(), Constants.DATE_TIME_FORMAT);
      this.isGateOut = Boolean.TRUE;
    }
    this.bayAssigned = truckReport.getBay();
    this.status = Utility.camelCase(truckReport.getStatus().name());
    this.rejectionStatus = Utility.camelCase(truckReport.getRejectedStatus().name());
    this.inWeight = truckReport.getInWeight();
    this.outWeight = truckReport.getOutWeight();
    if (!StringUtils.isEmpty(truckReport.getOutWeight()) && !StringUtils.isEmpty(truckReport.getInWeight()))
      this.netWeight = Math.abs((truckReport.getOutWeight() - truckReport.getInWeight()));
    this.inWeightStatus = (!StringUtils.isEmpty(this.inWeight) && this.inWeight !=0.0) ? true : false;
    this.outWeightStatus = (!StringUtils.isEmpty(this.outWeight) && this.outWeight !=0.0) ? true : false;
    this.reportLocation = truckReport.getReportLocation();
    // For RDC Inventory trucks
    if (truckReport.getLoadslipUSDate() != null) {
      this.loadslipUSDate = truckReport.getLoadslipUSDate();
    }
    if (truckReport.getLoadslipUEDate() != null) {
      this.loadslipUEDate = truckReport.getLoadslipUEDate();
    }
    if (truckReport.getReleaseDate() != null) {
      // this.releaseDate = truckReport.getReleaseDate();
    }
    this.containerNum = truckReport.getContainerNum();
    // Truck Reporting is having Ref_code(not empty and not null) then it is intransit truck(Incomimg truck) else Shipment truck
    this.intransitTruck = !StringUtils.isEmpty(truckReport.getRefCode()) *//*!StringUtils.isEmpty(truckReport.getActivity()) && truckReport.getActivity().equals("D")*//*;
    this.shipmentId = truckReport.getShipmentID();
    this.bayStatus = truckReport.getBayStatus() != null ? truckReport.getBayStatus().name() : null;
    this.insertUser =truckReport.getInsertUser();
    this.updateUser=truckReport.getUpdateUser();
    this.loadslipComments = truckReport.getLoadslipComments();
    this.comments = truckReport.getComments();
    this.eWayBillNo=truckReport.getEWayBillNo();
    this.sapInvValue=truckReport.getSapInvValue();
    this.sapInvWeight=truckReport.getSapInvWeight();
    if (!StringUtils.isEmpty(this.netWeight) && this.netWeight !=0.0 && !StringUtils.isEmpty(this.sapInvWeight))
      this.diffInvWeight =Math.abs((this.netWeight) - (this.sapInvWeight));

    this.rejectionCode=truckReport.getRejectionCode();
    if (!StringUtils.isEmpty(truckReport.getCtDiffWt()))
      this.weightDiffStatus = (this.diffInvWeight != null ? this.diffInvWeight : 0.0) > (Double.valueOf(truckReport.getCtDiffWt()));
    this.rejectionDesc=truckReport.getRejectionDesc();
    this.destCountry = truckReport.getDestCountry();
    this.destCountryName = truckReport.getDestCountryName();

    this.isPuc = !StringUtils.isEmpty(truckReport.getIsPuc()) && truckReport.getIsPuc().equalsIgnoreCase("Y");
    this.isInsurance =  !StringUtils.isEmpty(truckReport.getIsInsurance()) && truckReport.getIsInsurance().equalsIgnoreCase("Y");
    this.isSeatBelt =  !StringUtils.isEmpty(truckReport.getIsSeatBelt()) && truckReport.getIsSeatBelt().equalsIgnoreCase("Y");
    this.isFirstAid =  !StringUtils.isEmpty(truckReport.getIsFirstAid()) && truckReport.getIsFirstAid().equalsIgnoreCase("Y");
    this.isFireExtenguisher =  !StringUtils.isEmpty(truckReport.getIsFireExtenguisher())&& truckReport.getIsFireExtenguisher().equalsIgnoreCase("Y");
    this.isEmergencyCard =  !StringUtils.isEmpty(truckReport.getIsEmergencyCard())&& truckReport.getIsEmergencyCard().equalsIgnoreCase("Y");
    this.isSparKArrestor = !StringUtils.isEmpty(truckReport.getIsSparKArrestor())&& truckReport.getIsSparKArrestor().equalsIgnoreCase("Y");
    this.isFitnessCert = !StringUtils.isEmpty(truckReport.getIsFitnessCert())&& truckReport.getIsFitnessCert().equalsIgnoreCase("Y");
    this.ftTripId = truckReport.getFtTripId();*/
}


