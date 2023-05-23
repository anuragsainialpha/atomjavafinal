package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.Freight;
import com.api.apollo.atom.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import java.util.Date;

//@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class FreightInfoDto {

    @JsonProperty("id")
    @SerializedName(value = "id")
    private Double id;

    @JsonProperty("transporterSapCode")
    @SerializedName(value = "transporterSapCode")
    private String transporterSapCode;

    @JsonProperty("servprov")
    @SerializedName(value = "servprov")
    private String servprov;

    @JsonProperty("sourceLoc")
    @SerializedName(value = "sourceLoc")
    private String sourceLoc;

    @JsonProperty("sourceDesc")
    @SerializedName(value = "sourceDesc")
    private String sourceDesc;

    @JsonProperty("destLoc")
    @SerializedName(value = "destLoc")
    private String destLoc;

    @JsonProperty("destDesc")
    @SerializedName(value = "destDesc")
    private String destDesc;

    @JsonProperty("laneCode")
    @SerializedName(value = "laneCode")
    private String laneCode;

    @JsonProperty("truckType")
    @SerializedName(value = "truckType")
    private String truckType;

    @JsonProperty("variant1")
    @SerializedName(value = "condition1")
    private String condition1;

    @JsonProperty("variant2")
    @SerializedName(value = "condition2")
    private String condition2;

    @JsonProperty("effectiveDate")
//    @JsonFormat(timezone = "IST")
    @SerializedName(value = "effectiveDate")
    private Date effectiveDate;

    @JsonProperty("expiryDate")
//    @JsonFormat(timezone = "IST")
    @SerializedName(value = "expiryDate")
    private Date expiryDate;

    @JsonProperty("ttDays")
    @SerializedName(value = "ttDays")
    private Double ttDays;

    @JsonProperty("baseFreight")
    @SerializedName(value = "baseFreight")
    private Double baseFreight;

    @JsonProperty("baseFreightUom")
    @SerializedName(value = "baseFreightUom")
    private String baseFreightUom;

    @JsonProperty("basis")
    @SerializedName(value = "basis")
    private String basis;

    @JsonProperty("minValue")
    @SerializedName(value = "minValue")
    private Double minValue;

    @JsonProperty("minValueUom")
    @SerializedName(value = "minValueUom")
    private String minValueUom;

    @JsonProperty("approval1User")
    @SerializedName(value = "approval1User")
    private String approval1User;

    @JsonProperty("approval1Date")
    @SerializedName(value = "approval1Date")
    private Date approval1Date;

    @JsonProperty("approval2User")
    @SerializedName(value = "approval2User")
    private String approval2User;

    @JsonProperty("approval2Date")
    @SerializedName(value = "approval2Date")
    private Date approval2Date;

    @JsonProperty("status")
    @SerializedName(value = "status")
    private String status;

    @JsonProperty("insertUser")
    @SerializedName(value = "insertUser")
    private String insertUser;

    @JsonProperty("insertDate")
    @SerializedName(value = "insertDate")
    private Date insertDate;

    @JsonProperty("updateUser")
    @SerializedName(value = "updateUser")
    private String updateUser;

    @JsonProperty("updateDate")
    @SerializedName(value = "updateDate")
    private Date updateDate;

    @JsonProperty("transportMode")
    @SerializedName(value = "transportMode")
    private String transportMode;


    @JsonProperty("previousRate")
    @SerializedName(value = "previousRate")
    private Double previousRate;

    @JsonProperty("diff")
    @SerializedName(value = "diff")
    private String diff;

    @JsonProperty("percentile")
    @SerializedName(value = "percentile")
    private String percentile;

    @JsonProperty("rateRecordId")
    @SerializedName(value = "rateRecordId")
    private String rateRecordId;


    @JsonProperty("rateType")
    @SerializedName(value = "rateType")
    private String rateType;

    @JsonProperty("loading")
    @SerializedName(value = "loading")
    private Double loading;

    @JsonProperty("unloading")
    @SerializedName(value = "unloading")
    private Double unloading;


    @JsonProperty("others1")
    @SerializedName(value = "others1")
    private Double others1;

    @JsonProperty("others1Code")
    @SerializedName(value = "others1Code")
    private String others1Code;

    @JsonProperty("others2")
    @SerializedName(value = "others2")
    private Double others2;

    @JsonProperty("others2Code")
    @SerializedName(value = "others2Code")
    private String others2Code;

    @JsonProperty("others3")
    @SerializedName(value = "others3")
    private Double others3;

    @JsonProperty("others3Code")
    @SerializedName(value = "others3Code")
    private String others3Code;

    @JsonProperty("distance")
    @SerializedName(value = "distance")
    private Double distance;

    @JsonProperty("totExpense")
    @SerializedName(value = "totExpense")
    private Double totalExpense;

    @JsonProperty("payTransporter")
    @SerializedName(value = "payTransporter")
    private Double payableTransporter;

    @JsonProperty("sourceType")
    @SerializedName(value = "sourceType")
    private String sourceType;

    @JsonProperty("remarks")
    @SerializedName(value = "remarks")
    private String remarks;

    private String expiryDateString;

    private Boolean isUpdateExpiryDate = false;

    private String selectedExpiryDate;

    private boolean showSaveButton = false;





    public FreightInfoDto(Freight freight) {
        this.id=freight.getId();
        this.transporterSapCode = freight.getTransporterSapCode();
        this.servprov = freight.getServprov();
        this.sourceLoc = freight.getSourceLoc();
        this.sourceDesc = freight.getSourceDesc();
        this.destLoc = freight.getDestLoc();
        this.destDesc = freight.getDestDesc();
        this.laneCode = freight.getLaneCode();
        this.truckType = freight.getTruckType();
        this.condition1 = freight.getCondition1();
        this.condition2 = freight.getCondition2();
        this.effectiveDate = freight.getEffectiveDate();
        this.expiryDate = freight.getExpiryDate();
/*        this.effectiveDate = freight.getEffectiveDate() != null ? DateUtils.formatDateToUTC(DateUtils.formatDate(freight.getEffectiveDate(), Constants.DATE_FORMATE_WITH_HYPHEN), Constants.DATE_FORMATE_WITH_HYPHEN) : null;
        this.expiryDate = freight.getExpiryDate() != null ? DateUtils.formatDateToUTC(DateUtils.formatDate(freight.getExpiryDate(), Constants.DATE_FORMATE_WITH_HYPHEN), Constants.DATE_FORMATE_WITH_HYPHEN) : null;*/
        this.ttDays = freight.getTtDays();
        this.baseFreight = freight.getBaseFreight();
        this.baseFreightUom = freight.getBaseFreightUom();
        this.basis = freight.getBasis();
        this.minValue = freight.getMinValue();
        this.minValueUom = freight.getMinValueUom();
        this.approval1User = freight.getApproval1User();
        this.approval1Date = freight.getApproval1Date();
        this.approval2User = freight.getApproval2User();
        this.approval2Date = freight.getApproval2Date();
        this.status = freight.getStatus();
        this.insertUser = freight.getInsertUser();
        this.insertDate = freight.getInsertDate();
        this.updateUser = freight.getUpdateUser();
        this.updateDate = freight.getUpdateDate();
        this.transportMode = freight.getTransportMode();
        this.previousRate = freight.getPreviousRate();
        this.diff = freight.getDiff();
        this.percentile = freight.getPercentile();
        this.rateRecordId = freight.getRateRecordId();

        this.rateType = freight.getRateType();
        this.loading= freight.getLoading();
        this.unloading= freight.getUnLoading();
        this.others1= freight.getOthers1();
        this.others1Code= freight.getOthers1Code();
        this.others2= freight.getOthers2();
        this.others2Code= freight.getOthers2Code();
        this.others3= freight.getOthers3();
        this.others3Code= freight.getOthers3Code();

        this.distance= freight.getDistance();
        this.totalExpense= freight.getTotalExpense();
        this.payableTransporter= freight.getPayableTransporter();
        this.sourceType= freight.getSourceType();
        this.remarks = freight.getRemarks();


    }
}
