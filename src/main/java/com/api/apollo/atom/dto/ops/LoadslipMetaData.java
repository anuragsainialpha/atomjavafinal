package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.util.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.Map;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoadslipMetaData {

  // These fields will be fetched based on the sequence they mentioned here
  private Object loadslipId;
  private Object shipmentId;
  private Object qty;
  private Object sourceLoc;
  private Object destLoc;
  private Object stoSoNum;
  private Object delivery;
  private Object sapInvoice;
  private Object sapInvoiceDate;
  private Object lrNum;
  private Object lrDate;
  private Object lsPrintDate;
  private Object bayArrivedDate;
  private Object loadingStartDate;
  private Object loadingEndDate;
  private Object confirmedDate;
  private Object releasedDate;
  private Object sendForBarcodeDate;
  private Object totTyres;
  private Object totTubes;
  private Object totFlaps;
  private Object totValve;
  private Object totPctr;
  private Object totQty;
  private Object grn;
  private Object grnDate;
  private Object status;
  private Object tteUtil;
  private Object weightUtil;
  private Object stopType;
  private Object truckType;
  private Object truckNumber;
  private Object servprov;
  private Object gatedOutDate;
  private Object createdDate;
  private Object tteQty;
  private Object itemCategory;
  private Object invoiceCnt;
  private Object releaseDate;
  private Object type;
  private Object dropSeq;
  private Object volumeUtil;
  private String destDis;
  private Object integrationStatus;
  private Object integrationMsg;
  private boolean canLSCancelled = true;
  private Object freightAvailability;
  private Object comments;
  private Object ditQty;
  private Object shortQty;
  private Object updateUser;
  private Object insertUser;
  private Object gateInDate;
  private Object gateOutDate;
  private Object reportDate;
  private Object inWeight;
  private Object outWeight;
  private Object driverName;
  private Object driverMobile;
  private Object driverLicense;
  private Object eWayBillNo;
  private Object sapInvValue;
  private Object sapInvWeight;
  private Object otherQty;
  private Object transhipment;
  private Object netWeight;
  private Object loadslipcomments;
  private Object diffInvWeight;
  private Boolean weightDiffStatus= Boolean.FALSE;
  private Object containerNum;
  private Object totalWeight;
  private Object weightUom;
  private Object destCountry;
  private Object destCountryName;

  private Object scannedQty;
  private Object loadslipType;
  private Object marketSegment;

  private Object ftTripId;

  private Object customInvoiceNumber;
  private Object trackingConsentStatus;
  private Object consentPhoneTelecom;
  private Object goApprovalReason;


  public LoadslipMetaData(Object loadslipId, Object shipmentId, Object qty, Object sourceLoc, Object destLoc, Object stoSoNum, Object delivery, Object sapInvoice, Object sapInvoiceDate, Object lrNum, Object lsPrintDate,
                          Object bayArrivedDate, Object loadingStartDate, Object loadingEndDate, Object confirmedDate, Object sendForBarcodeDate, Object totTyres, Object totTubes, Object totFlaps, Object totValve, Object totPctr, Object totQty,
                          Object grn, Object status, Object tteUtil, Object weightUtil, Object stopType, Object truckType, Object truckNumber, Object servprov, Object freightAvailability, Object gatedOutDate,
                          Object createdDate, Object tteQty, Object itemCategory, Object releaseDate, Object type, Object grnDate, Object lrDate, Object dropSeq, Object volumeUtil, Object intStatus, Object intMsg, Object comments,
                          Object ditQty,Object shortQty,Object updateUser,Object insertUser,Object eWayBillNo,Object sapInvValue,Object sapInvWeight, Object otherQty,Object transhipment,Object containerNum, Object totWeight,
                          Object weightUom, Object destCountry, Object marketSegment, Object ftTripId, Object customInvoiceNumber, Object trackingConsentStatus, Object consentPhoneTelecom) {
    this.loadslipId = loadslipId;
    this.shipmentId = shipmentId;
    this.qty = qty;
    this.sourceLoc = sourceLoc;
    this.destLoc = destLoc;
    this.stoSoNum = stoSoNum;
    this.delivery = delivery;
    this.sapInvoice = sapInvoice;
    this.sapInvoiceDate = sapInvoiceDate;
    this.lrNum = lrNum;
    this.lsPrintDate = lsPrintDate != null ?  DateUtils.formatDate((((java.util.Date)lsPrintDate)), Constants.DATE_TIME_FORMAT) : null;
    this.bayArrivedDate = bayArrivedDate != null ?  DateUtils.formatDate((((java.util.Date)bayArrivedDate)), Constants.DATE_TIME_FORMAT) : null;
    this.loadingStartDate = loadingStartDate != null ?  DateUtils.formatDate((((java.util.Date)loadingStartDate)), Constants.DATE_TIME_FORMAT) : null;
    this.loadingEndDate = loadingEndDate != null ?  DateUtils.formatDate((((java.util.Date)loadingEndDate)), Constants.DATE_TIME_FORMAT) : null;
    this.confirmedDate = confirmedDate != null ?  DateUtils.formatDate((((java.util.Date)confirmedDate)), Constants.DATE_TIME_FORMAT) : null;
    this.sendForBarcodeDate = sendForBarcodeDate != null ?  DateUtils.formatDate((((java.util.Date)sendForBarcodeDate)), Constants.DATE_TIME_FORMAT) : null;
    this.totTyres = totTyres;
    this.totTubes = totTubes;
    this.totFlaps = totFlaps;
    this.totValve = totValve;
    this.totPctr = totPctr;
    this.totQty = totQty;
    this.grn = grn;
    this.status = status;
    this.tteUtil = tteUtil;
    this.weightUtil = weightUtil;
    this.stopType = stopType;
    this.truckType = truckType;
    this.truckNumber = truckNumber;
    this.servprov = servprov;
    this.freightAvailability = freightAvailability;
    this.gatedOutDate = gatedOutDate;
    this.createdDate = createdDate;
    this.tteQty = tteQty;
    this.itemCategory = itemCategory;
    this.releaseDate = releaseDate != null ?  DateUtils.formatDate((((java.util.Date)releaseDate)), Constants.DATE_TIME_FORMAT) : null;
    this.type = type;
    this.grnDate = grnDate != null ?  DateUtils.formatDate((((java.util.Date)grnDate)), Constants.DATE_TIME_FORMAT) : null;
    this.lrDate = lrDate;
    this.dropSeq = dropSeq;
    this.volumeUtil = volumeUtil;
    this.integrationStatus = intStatus;
    this.integrationMsg = intMsg;
    this.comments = comments;
    this.ditQty = ditQty;
    this.shortQty = shortQty;
    this.updateUser=updateUser;
    this.insertUser = insertUser;
    this.eWayBillNo=eWayBillNo;
    this.sapInvValue=sapInvValue;
    this.sapInvWeight=sapInvWeight;
    this.otherQty = (otherQty != null && Integer.parseInt(otherQty.toString())== 0) ?  null : otherQty;
    this.transhipment=transhipment;
    this.containerNum=containerNum;
    this.totalWeight = totWeight;
    this.weightUom = weightUom;
    this.destCountry = destCountry;
    this.marketSegment = marketSegment;
    this.ftTripId =ftTripId;
    this.customInvoiceNumber = customInvoiceNumber;
    this.trackingConsentStatus = trackingConsentStatus;
    this.consentPhoneTelecom = consentPhoneTelecom;
    this.goApprovalReason = goApprovalReason;
  }

  /*for view loadslip movement */
  public LoadslipMetaData(Object loadslipId, Object shipmentId, Object qty, Object sourceLoc, Object destLoc, Object stoSoNum, Object delivery, Object sapInvoice, Object sapInvoiceDate, Object lrNum, Object lsPrintDate,
                          Object bayArrivedDate,Object loadingStartDate,Object loadingEndDate, Object confirmedDate,Object sendForBarcodeDate, Object totTyres, Object totTubes, Object totFlaps, Object totValve, Object totPctr, Object totQty,
                          Object grn, Object status, Object tteUtil, Object weightUtil, Object stopType, Object truckType, Object truckNumber, Object servprov, Object freightAvailability, Object gatedOutDate,
                          Object createdDate, Object tteQty, Object itemCategory, Object releaseDate, Object type, Object grnDate, Object lrDate, Object dropSeq, Object volumeUtil, Object intStatus, Object intMsg, Object comments,
                          Object ditQty,Object shortQty,Object updateUser,Object insertUser,Object gateInDate,Object gateOutDate,Object reportDate,Object inWeight,Object outWeight,Object driverName,Object driverMobile,Object driverLicense,
                          Object eWayBillNo,Object sapInvValue,Object sapInvWeight, Object otherQty, Object customInvoiceNumber
                        , Object trackingConsentStatus, Object consentPhoneTelecom,Object goApprovalReason) {
    this.loadslipId = loadslipId;
    this.shipmentId = shipmentId;
    this.qty = qty;
    this.sourceLoc = sourceLoc;
    this.destLoc = destLoc;
    this.stoSoNum = stoSoNum;
    this.delivery = delivery;
    this.sapInvoice = sapInvoice;
    this.sapInvoiceDate = sapInvoiceDate;
    this.lrNum = lrNum;
    this.lsPrintDate = lsPrintDate != null ?  DateUtils.formatDate((((java.util.Date)lsPrintDate)), Constants.DATE_TIME_FORMAT) : null;
    this.bayArrivedDate = bayArrivedDate != null ?  DateUtils.formatDate((((java.util.Date)bayArrivedDate)), Constants.DATE_TIME_FORMAT) : null;
    this.loadingStartDate = loadingStartDate != null ?  DateUtils.formatDate((((java.util.Date)loadingStartDate)), Constants.DATE_TIME_FORMAT) : null;
    this.loadingEndDate = loadingEndDate != null ?  DateUtils.formatDate((((java.util.Date)loadingEndDate)), Constants.DATE_TIME_FORMAT) : null;
    this.confirmedDate = confirmedDate != null ?  DateUtils.formatDate((((java.util.Date)confirmedDate)), Constants.DATE_TIME_FORMAT) : null;
    this.sendForBarcodeDate = sendForBarcodeDate != null ?  DateUtils.formatDate((((java.util.Date)sendForBarcodeDate)), Constants.DATE_TIME_FORMAT) : null;
    this.totTyres = totTyres;
    this.totTubes = totTubes;
    this.totFlaps = totFlaps;
    this.totValve = totValve;
    this.totPctr = totPctr;
    this.totQty = totQty;
    this.grn = grn;
    this.status = status;
    this.tteUtil = tteUtil;
    this.weightUtil = weightUtil;
    this.stopType = stopType;
    this.truckType = truckType;
    this.truckNumber = truckNumber;
    this.servprov = servprov;
    this.freightAvailability = freightAvailability;
    this.gatedOutDate = gatedOutDate;
    this.createdDate = createdDate;
    this.tteQty = tteQty;
    this.itemCategory = itemCategory;
    this.releaseDate = releaseDate != null ?  DateUtils.formatDate((((java.util.Date)releaseDate)), Constants.DATE_TIME_FORMAT) : null;
    this.type = type;
    this.grnDate = grnDate != null ?  DateUtils.formatDate((((java.util.Date)grnDate)), Constants.DATE_TIME_FORMAT) : null;
    this.lrDate = lrDate;
    this.dropSeq = dropSeq;
    this.volumeUtil = volumeUtil;
    this.integrationStatus = intStatus;
    this.integrationMsg = intMsg;
    this.comments = comments;
    this.ditQty = ditQty;
    this.shortQty = shortQty;
    this.updateUser=updateUser;
    this.insertUser = insertUser;
    this.gateInDate= gateInDate != null ?  DateUtils.formatDate((((java.util.Date)gateInDate)), Constants.DATE_TIME_FORMAT) : null;
    this.gateOutDate= gateOutDate != null ?  DateUtils.formatDate((((java.util.Date)gateOutDate)), Constants.DATE_TIME_FORMAT) : null;
    this.reportDate= reportDate != null ?  DateUtils.formatDate((((java.util.Date)reportDate)), Constants.DATE_TIME_FORMAT) : null;
    this.inWeight=inWeight;
    this.outWeight=outWeight;
    this.driverName=driverName;
    this.driverMobile=driverMobile;
    this.driverLicense=driverLicense;
    this.insertUser = insertUser;
    this.eWayBillNo=eWayBillNo;
    this.sapInvValue=sapInvValue;
    this.sapInvWeight=sapInvWeight;
    this.otherQty = (otherQty != null && Integer.parseInt(otherQty.toString())== 0) ?  null : otherQty;
    this.customInvoiceNumber = customInvoiceNumber;
    this.trackingConsentStatus = trackingConsentStatus;
    this.consentPhoneTelecom = consentPhoneTelecom;
    this.goApprovalReason = goApprovalReason;
  }

  public LoadslipMetaData(Map<String, Object> loadslipMap) {
    this.loadslipId = loadslipMap.get("loadslipId");
    this.shipmentId = loadslipMap.get("shipmentId");
    this.qty = loadslipMap.get("qty");
    this.sourceLoc = loadslipMap.get("sourceLoc");
    this.destLoc = loadslipMap.get("destLoc");
    this.stoSoNum = loadslipMap.get("stoSoNum");
    this.delivery = loadslipMap.get("delivery");
    this.sapInvoice = loadslipMap.get("sapInvoice");
    this.sapInvoiceDate = loadslipMap.get("sapInvoiceDate") != null ? DateUtils.formatDate((((java.util.Date) loadslipMap.get("sapInvoiceDate"))), Constants.DATE_TIME_FORMAT) : null;
    this.lrNum = loadslipMap.get("lrNum");
    this.lsPrintDate = loadslipMap.get("lsPrintDate") != null ? DateUtils.formatDate((((java.util.Date) loadslipMap.get("lsPrintDate"))), Constants.DATE_TIME_FORMAT) : null;
    this.bayArrivedDate = loadslipMap.get("bayArrivedDate") != null ? DateUtils.formatDate((((java.util.Date) loadslipMap.get("bayArrivedDate"))), Constants.DATE_TIME_FORMAT) : null;
    this.loadingStartDate = loadslipMap.get("loadingStartDate") != null ? DateUtils.formatDate((((java.util.Date) loadslipMap.get("loadingStartDate"))), Constants.DATE_TIME_FORMAT) : null;
    this.loadingEndDate = loadslipMap.get("loadingEndDate") != null ? DateUtils.formatDate((((java.util.Date) loadslipMap.get("loadingEndDate"))), Constants.DATE_TIME_FORMAT) : null;
    this.confirmedDate = loadslipMap.get("confirmedDate") != null ? DateUtils.formatDate((((java.util.Date) loadslipMap.get("confirmedDate"))), Constants.DATE_TIME_FORMAT) : null;
    this.sendForBarcodeDate = loadslipMap.get("sendForBarcodeDate") != null ? DateUtils.formatDate((((java.util.Date) loadslipMap.get("sendForBarcodeDate"))), Constants.DATE_TIME_FORMAT) : null;
    this.totTyres = loadslipMap.get("totTyres");
    this.totTubes = loadslipMap.get("totTubes");
    this.totFlaps = loadslipMap.get("totFlaps");
    this.totValve = loadslipMap.get("totValve");
    this.totPctr = loadslipMap.get("totPctr");
    this.totQty = loadslipMap.get("totQty");
    this.grn = loadslipMap.get("grn");
    this.status = loadslipMap.get("status");
    this.tteUtil = loadslipMap.get("tteUtil");
    this.weightUtil = loadslipMap.get("weightUtil");
    this.stopType = loadslipMap.get("stopType");
    this.truckType = loadslipMap.get("truckType");
    this.truckNumber = loadslipMap.get("truckNumber");
    this.servprov = loadslipMap.get("servprov");
    this.gatedOutDate = loadslipMap.get("gatedOutDate") != null ? DateUtils.formatDate((((java.util.Date) loadslipMap.get("gatedOutDate"))), Constants.DATE_TIME_FORMAT) : null;
    this.createdDate = loadslipMap.get("createdDate") != null ? DateUtils.formatDate((((java.util.Date) loadslipMap.get("createdDate"))), Constants.DATE_TIME_FORMAT) : null;
    this.tteQty = loadslipMap.get("tteQty");
    this.itemCategory = loadslipMap.get("itemCategory");
    this.invoiceCnt = loadslipMap.get("invoiceCnt");
    this.releaseDate = loadslipMap.get("releaseDate") != null ? DateUtils.formatDate((((java.util.Date) loadslipMap.get("releaseDate"))), Constants.DATE_TIME_FORMAT) : null;
    this.type = loadslipMap.get("type");
    this.grnDate = loadslipMap.get("grnDate") != null ? DateUtils.formatDate((((java.util.Date) loadslipMap.get("grnDate"))), Constants.DATE_TIME_FORMAT) : null;
    this.lrDate = loadslipMap.get("lrDate") != null ? DateUtils.formatDate((((java.util.Date) loadslipMap.get("lrDate"))), Constants.DATE_TIME_FORMAT) : null;
    this.volumeUtil = loadslipMap.get("volumeUtil");
    this.dropSeq = loadslipMap.get("dropSeq");
    this.integrationStatus = loadslipMap.get("INT_STATUS");
    this.integrationMsg = loadslipMap.get("INT_MESSAGE");
    this.freightAvailability = loadslipMap.get("freightAvailability");
    this.comments = loadslipMap.get("comments");
    this.ditQty = loadslipMap.get("ditQty");
    this.shortQty = loadslipMap.get("shortQty");
    this.updateUser = loadslipMap.get("updateUser");
    this.insertUser = loadslipMap.get("insertUser");
    this.gateInDate= loadslipMap.get("gateInDate") != null ? DateUtils.formatDate((((java.util.Date) loadslipMap.get("gateInDate"))), Constants.DATE_TIME_FORMAT) : null;
    this.gateOutDate=loadslipMap.get("gateOutDate") != null ? DateUtils.formatDate((((java.util.Date) loadslipMap.get("gateOutDate"))), Constants.DATE_TIME_FORMAT) : null;
    this.reportDate=loadslipMap.get("reportDate") != null ? DateUtils.formatDate((((java.util.Date) loadslipMap.get("reportDate"))), Constants.DATE_TIME_FORMAT) : null;
    this.inWeight=loadslipMap.get("inWeight");
    this.outWeight=loadslipMap.get("outWeight");
    this.driverName=loadslipMap.get("driverName");
    this.driverMobile=loadslipMap.get("driverMobile");
    this.driverLicense=loadslipMap.get("driverLicense");
    this.eWayBillNo= loadslipMap.get("eWayBillNo");
    this.sapInvValue= loadslipMap.get("sapInvValue");
    this.sapInvWeight= loadslipMap.get("sapInvWeight");
    this.otherQty = (loadslipMap.get("otherQty") != null && Integer.parseInt(loadslipMap.get("otherQty").toString())== 0) ?  null : loadslipMap.get("otherQty");
    this.transhipment=loadslipMap.get("transhipment");
    if (!StringUtils.isEmpty(loadslipMap.get("outWeight")) && !StringUtils.isEmpty(loadslipMap.get("inWeight")))
      this.netWeight = Math.abs((Double.valueOf(loadslipMap.get("outWeight").toString()) - Double.valueOf(loadslipMap.get("inWeight").toString())));
    this.loadslipcomments=loadslipMap.get("loadslipcomments");
    if (!StringUtils.isEmpty(this.netWeight) && !StringUtils.isEmpty(this.sapInvWeight))
      this.diffInvWeight = Math.abs((Double.valueOf(this.netWeight.toString())) - (Double.valueOf(this.sapInvWeight.toString())));
    this.containerNum = loadslipMap.get("containerNum");
    this.totalWeight = !StringUtils.isEmpty(loadslipMap.get("TOT_WEIGHT")) ? loadslipMap.get("TOT_WEIGHT") : null;
    this.weightUom = loadslipMap.get("WEIGHT_UOM");
    this.destCountry = loadslipMap.get("destCountry");
    this.marketSegment = loadslipMap.get("MKT_SEG");
    this.ftTripId = loadslipMap.get("FT_TRIP_ID");
    this.customInvoiceNumber = loadslipMap.get("CUSTOM_INV_NUMBER");
    this.trackingConsentStatus = loadslipMap.get("TRACKING_CONSENT_STATUS");
    this.consentPhoneTelecom = loadslipMap.get("CONSENT_PHONE_TELECOM");
    this.goApprovalReason = loadslipMap.get("goApprovalReason");
  }
  public LoadslipMetaData(Object loadslipId, Object shipmentId, Object qty, Object sourceLoc, Object destLoc,Object truckNumber,Object truckType,Object servprov,Object freightAvailability,Object type,Object dropSeq,Object transhipment,
                         Object stopType, Object lsPrintDate, Object bayArrivedDate, Object loadingStartDate, Object loadingEndDate,
                          Object createdDate,Object updateUser,Object insertUser,Object comments, Object containerNum
          , Object trackingConsentStatus, Object consentPhoneTelecom, Object goApprovalReason) {
    this.loadslipId = loadslipId;
    this.shipmentId = shipmentId;
    this.qty = qty;
    this.sourceLoc = sourceLoc;
    this.destLoc = destLoc;
    this.truckNumber = truckNumber;
    this.truckType = truckType;
    this.servprov = servprov;
    this.freightAvailability = freightAvailability;
    this.type = type;
    this.dropSeq = dropSeq;
    this.transhipment = transhipment;
    this.stopType = stopType;

    this.lsPrintDate = lsPrintDate != null ? DateUtils.formatDate((((java.util.Date) lsPrintDate)), Constants.DATE_TIME_FORMAT) : null;
    this.bayArrivedDate = bayArrivedDate != null ? DateUtils.formatDate((((java.util.Date) bayArrivedDate)), Constants.DATE_TIME_FORMAT) : null;
    this.loadingStartDate = loadingStartDate != null ? DateUtils.formatDate((((java.util.Date) loadingStartDate)), Constants.DATE_TIME_FORMAT) : null;
    this.loadingEndDate = loadingEndDate != null ? DateUtils.formatDate((((java.util.Date) loadingEndDate)), Constants.DATE_TIME_FORMAT) : null;

    this.createdDate = createdDate;
    this.updateUser = updateUser;
    this.insertUser = insertUser;
    this.comments = comments;
    this.containerNum = containerNum;
    this.trackingConsentStatus = trackingConsentStatus;
    this.consentPhoneTelecom = consentPhoneTelecom;
    this.goApprovalReason = goApprovalReason;
  }


}
