package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.util.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrucksMetaData {



	private Object truckNumber;
	private Object containerNum;
	private Object driverName;
	private Object driverMobile;
	private Object driverLicense;
	private Object trackingConsentStatus;
	private Object consentPhoneTelecom;
	private Object loadslipId;
	private Object shipmentId;
	private Object gateInDate;
	private Object gateOutDate;
	private Object reportDate;
	private Object inWeight;

	private Object outWeight;
	private Object comments;
	private Object truckType;
	private Object servprov;
	private Object qty;
	private Object sourceLoc;

	private Object destLoc;
	private Object stoSoNum;
	private Object delivery;
	private Object sapInvoice;
	private Object sapInvoiceDate;
	private Object lrNum;

	private Object lsPrintDate;
	private Object bayArrivedDate;
	private Object loadingStartDate;
	private Object loadingEndDate;
	private Object confirmedDate;
	private Object sendForBarcodeDate;

	private Object totTyres;
	private Object totTubes;
	private Object totFlaps;
	private Object totValve;
	private Object totPctr;
	private Object totQty;

	private Object grn;
	private Object status;
	private Object tteUtil;
	private Object weightUtil;
	private Object loadslipcomments;
	private Object freightAvailability;

	private Object transhipment;
	private Object updateUser;
	private Object insertUser;
	private Object eWayBillNo;
	private Object sapInvValue;
	private Object sapInvWeight;

	private Object otherQty;
	private Object createdDate;
	private Object grnDate;
	private Object lrDate;
	private Object netWeight;
	private Object diffInvWeight;

	private Object integrationStatus;
	private Object integrationMsg;
	private Object itemCategory;
	private Object gatedOutDate;
	private Object type;
	private Object releaseDate;
	private String destDis;
	private Boolean weightDiffStatus= Boolean.FALSE;

	private Object callRelHrs;
	private Object repGiHrs;
	private Object repGoHrs;
	private Object giGoHrs;
	private Object giRelHrs;
	private Object loRelHrs;
	private Object relGoHrs;
	private Object indentId;
	private Object transporterSapCode;
	private Object loadslipStatus;
	private Object reportingLoc;
	private Object isFreightAvailable;
	private Object rejectionCode;
	private Object rejectionDesc;
	private Object rejectionStatus;
	private Object actualTruckType;

	//truck report flags
	private  Object isPuc ;
	private Object isInsurance ;
	private Object isSeatBelt;
	private Object isFirstAid ;
	private Object  isFireExtenguisher ;
	private Object isEmergencyCard;
	private Object isSparKArrestor;
	private Object isFitnessCert;

	private Object truckCapacity;
	private Object truckGrossVehicleWt;
	private Object truckUnladenWt;
	private Object bsNorms;
	private Object fuelType;

	private boolean canLSCancelled = true;

	private Object stopType;
	//private Object destCountryName;
	//private Object actualArrivalDate;
	private Object ditQty;
	private Object shortQty;
	private Object tteQty;
	private Object volumeUtil;
	private Object dropSeq;
	private Object bayStatus;

	private Object destCountry;
	private Object destCountryName;
	private Object totalTat;
	private Object marketSegment;
	private Object indentCreationDate;
	private Object reportingDateAtDest;
	private Object unloadingDateAtDest;
	private Object stdTT;
	private Object delayedDays;
	private Object ftTripId;
	private Object customInvNumber;
	private Object indentCategory;



	public TrucksMetaData(Map<String, Object> trucksMap) {
		this.loadslipId = trucksMap.get("loadslipId");
		this.shipmentId = trucksMap.get("shipmentId");
		this.qty = trucksMap.get("qty");
		this.sourceLoc = trucksMap.get("sourceLoc");
		this.destLoc = trucksMap.get("destLoc");
		this.stoSoNum = trucksMap.get("stoSoNum");
		this.delivery = trucksMap.get("delivery");
		this.sapInvoice = trucksMap.get("sapInvoice");
		this.sapInvoiceDate = trucksMap.get("sapInvoiceDate") != null ? DateUtils.formatDate((((Date) trucksMap.get("sapInvoiceDate"))), Constants.DATE_TIME_FORMAT) : null;
		this.lrNum = trucksMap.get("lrNum");
		this.lsPrintDate = trucksMap.get("lsPrintDate") != null ? DateUtils.formatDate((((Date) trucksMap.get("lsPrintDate"))), Constants.DATE_TIME_FORMAT) : null;
		this.bayArrivedDate = trucksMap.get("bayArrivedDate") != null ? DateUtils.formatDate((((Date) trucksMap.get("bayArrivedDate"))), Constants.DATE_TIME_FORMAT) : null;
		this.loadingStartDate = trucksMap.get("loadingStartDate") != null ? DateUtils.formatDate((((Date) trucksMap.get("loadingStartDate"))), Constants.DATE_TIME_FORMAT) : null;
		this.loadingEndDate = trucksMap.get("loadingEndDate") != null ? DateUtils.formatDate((((Date) trucksMap.get("loadingEndDate"))), Constants.DATE_TIME_FORMAT) : null;
		this.confirmedDate = trucksMap.get("confirmedDate") != null ? DateUtils.formatDate((((Date) trucksMap.get("confirmedDate"))), Constants.DATE_TIME_FORMAT) : null;
		this.sendForBarcodeDate = trucksMap.get("sendForBarcodeDate") != null ? DateUtils.formatDate((((Date) trucksMap.get("sendForBarcodeDate"))), Constants.DATE_TIME_FORMAT) : null;
		this.totTyres = trucksMap.get("totTyres");
		this.totTubes = trucksMap.get("totTubes");
		this.totFlaps = trucksMap.get("totFlaps");
		this.totValve = trucksMap.get("totValve");
		this.totPctr = trucksMap.get("totPctr");
		this.totQty = trucksMap.get("totQty");
		this.grn = trucksMap.get("grn");
		this.status = trucksMap.get("status");
		this.tteUtil = trucksMap.get("tteUtil");
		this.weightUtil = trucksMap.get("weightUtil");

		this.stopType = trucksMap.get("stopType");

		this.truckType = trucksMap.get("truckType");
		this.truckNumber = trucksMap.get("truckNumber");
		this.servprov = trucksMap.get("servprov");
		this.gatedOutDate = trucksMap.get("gatedOutDate") != null ? DateUtils.formatDate((((Date) trucksMap.get("gatedOutDate"))), Constants.DATE_TIME_FORMAT) : null;
		this.createdDate = trucksMap.get("createdDate") != null ? DateUtils.formatDate((((Date) trucksMap.get("createdDate"))), Constants.DATE_TIME_FORMAT) : null;
		this.tteQty = trucksMap.get("tteQty");
		this.itemCategory = trucksMap.get("itemCategory");
		//this.invoiceCnt = trucksMap.get("invoiceCnt");
		this.releaseDate = trucksMap.get("releaseDate") != null ? DateUtils.formatDate((((Date) trucksMap.get("releaseDate"))), Constants.DATE_TIME_FORMAT) : null;
		this.type = trucksMap.get("type");
		this.grnDate = trucksMap.get("grnDate") != null ? DateUtils.formatDate((((Date) trucksMap.get("grnDate"))), Constants.DATE_TIME_FORMAT) : null;
		this.lrDate = trucksMap.get("lrDate") != null ? DateUtils.formatDate((((Date) trucksMap.get("lrDate"))), Constants.DATE_TIME_FORMAT) : null;
		this.volumeUtil = trucksMap.get("volumeUtil");
		this.dropSeq = trucksMap.get("dropSeq");
		this.integrationStatus = trucksMap.get("INT_STATUS");
		this.integrationMsg = trucksMap.get("INT_MESSAGE");
		this.freightAvailability = trucksMap.get("freightAvailability");
		this.comments = trucksMap.get("comments");
		this.ditQty = trucksMap.get("ditQty");
		this.shortQty = trucksMap.get("shortQty");
		this.updateUser = trucksMap.get("updateUser");
		this.insertUser = trucksMap.get("insertUser");
		this.gateInDate = trucksMap.get("gateInDate") != null ? DateUtils.formatDate((((Date) trucksMap.get("gateInDate"))), Constants.DATE_TIME_FORMAT) : null;
		this.gateOutDate = trucksMap.get("gateOutDate") != null ? DateUtils.formatDate((((Date) trucksMap.get("gateOutDate"))), Constants.DATE_TIME_FORMAT) : null;
		this.reportDate = trucksMap.get("reportDate") != null ? DateUtils.formatDate((((Date) trucksMap.get("reportDate"))), Constants.DATE_TIME_FORMAT) : null;
		this.inWeight = trucksMap.get("inWeight");
		this.outWeight = trucksMap.get("outWeight");
		this.driverName = trucksMap.get("driverName");
		this.driverMobile = trucksMap.get("driverMobile");
		this.driverLicense = trucksMap.get("driverLicense");
		this.eWayBillNo = trucksMap.get("eWayBillNo");
		this.sapInvValue = trucksMap.get("sapInvValue");
		this.sapInvWeight = trucksMap.get("sapInvWeight");
		this.trackingConsentStatus = trucksMap.get("trackingConsentStatus");
		this.consentPhoneTelecom = trucksMap.get("consentPhoneTelecom");
		this.otherQty = (trucksMap.get("otherQty") != null && Integer.parseInt(trucksMap.get("otherQty").toString()) == 0) ? null : trucksMap.get("otherQty");
		this.transhipment = trucksMap.get("transhipment");
		if (!StringUtils.isEmpty(trucksMap.get("outWeight")) && !StringUtils.isEmpty(trucksMap.get("inWeight")))
			this.netWeight = Math.abs((Double.valueOf(trucksMap.get("outWeight").toString()) - Double.valueOf(trucksMap.get("inWeight").toString())));
		this.loadslipcomments = trucksMap.get("loadslipcomments");
		if (!StringUtils.isEmpty(this.netWeight) && !StringUtils.isEmpty(this.sapInvWeight))
			this.diffInvWeight = Math.abs((Double.valueOf(this.netWeight.toString())) - (Double.valueOf(this.sapInvWeight.toString())));
		this.containerNum = trucksMap.get("containerNum");

		//this.callRelHrs=trucksMap.get("callRelHrs");
		this.repGiHrs=trucksMap.get("repGiHrs");
		this.repGoHrs=trucksMap.get("repGoHrs");
		this.giGoHrs=trucksMap.get("tTHrs");
		this.giRelHrs=trucksMap.get("giRelHrs");
		this.loRelHrs=trucksMap.get("loRelHrs");
		this.relGoHrs=trucksMap.get("relGoHrs");

		this.bayStatus=trucksMap.get("bayStatus");
		this.destCountry = trucksMap.get("destCountry");
		this.indentId = trucksMap.get("indentId");
		this.transporterSapCode = trucksMap.get("transporterSapCode");
		this.loadslipStatus = trucksMap.get("loadslipStatus");
		this.reportingLoc = trucksMap.get("reportingLoc");
		this.isFreightAvailable = trucksMap.get("isFreightAvailable");
		this.rejectionCode = trucksMap.get("rejectionCode");
		this.rejectionStatus = trucksMap.get("rejectionStatus");
		this.actualTruckType = trucksMap.get("actualTruckType");
		this.marketSegment = trucksMap.get("marketSegment");

		this.isPuc = trucksMap.get("isPuc") !=null ?(trucksMap.get("isPuc").equals("N")? false:true) :true;
		this.isInsurance = trucksMap.get("isInsurance")!=null ?(trucksMap.get("isInsurance").equals("N")? false:true) :true;
		this.isSeatBelt = trucksMap.get("isSeatBelt")!=null ?(trucksMap.get("isSeatBelt").equals("N")? false:true) :true;
		this.isFirstAid = trucksMap.get("isFirstAid")!=null ?(trucksMap.get("isFirstAid").equals("N")? false:true) :true;
		this.isFireExtenguisher = trucksMap.get("isFireExtenguisher")!=null ?(trucksMap.get("isFireExtenguisher").equals("N")? false:true) :true;
		this.isEmergencyCard = trucksMap.get("isEmergencyCard")!=null ?(trucksMap.get("isEmergencyCard").equals("N")? false:true) :true;
		this.isSparKArrestor = trucksMap.get("isSparKArrestor")!=null ?(trucksMap.get("isSparKArrestor").equals("N")? false:true) :true;
		this.isFitnessCert = trucksMap.get("isFitnessCert")!=null ?(trucksMap.get("isFitnessCert").equals("N")? false:true) :true;
		this.truckCapacity = trucksMap.get("truckCapacity");
		this.bsNorms = trucksMap.get("bsNorms");
		this.fuelType = trucksMap.get("fuelType");
		this.truckGrossVehicleWt = trucksMap.get("truckGrossVehicleWt");
		this.truckUnladenWt = trucksMap.get("truckUnladenWt");
		this.indentCreationDate = trucksMap.get("indentCreationDate") != null ? DateUtils.formatDate((((Date) trucksMap.get("indentCreationDate"))), Constants.DATE_TIME_FORMAT) : null;
		this.reportingDateAtDest = trucksMap.get("reportingDateAtDest") != null ? DateUtils.formatDate((((Date) trucksMap.get("reportingDateAtDest"))), Constants.DATE_TIME_FORMAT) : null;
		this.unloadingDateAtDest = trucksMap.get("unloadingDateAtDest") != null ? DateUtils.formatDate((((Date) trucksMap.get("unloadingDateAtDest"))), Constants.DATE_TIME_FORMAT) : null;
		this.stdTT = trucksMap.get("stdTT");
		this.delayedDays = trucksMap.get("delayedDays");
		this.ftTripId = trucksMap.get("ftTripId");
		this.customInvNumber = trucksMap.get("customInvNumber");
	}



}
