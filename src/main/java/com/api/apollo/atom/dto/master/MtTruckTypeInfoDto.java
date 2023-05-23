package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.MtTruckType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class MtTruckTypeInfoDto {

    @JsonProperty("truckType")
    @SerializedName(value = "truckType")
    private String truckType;

    @JsonProperty("loadFactor")
    @SerializedName(value = "loadFactor")
    private Double loadFactor;

    @JsonProperty("truckDesc")
    @SerializedName(value = "truckDesc")
    private String truckDesc;

    @JsonProperty("tteCapacity")
    @SerializedName(value = "tteCapacity")
    private Double tteCapacity;

    @JsonProperty("grossWt")
    @SerializedName(value = "grossWt")
    private Double grossWt;

    @JsonProperty("grossWtUom")
    @SerializedName(value = "grossWtUom")
    private String grossWtUom;

    @JsonProperty("grossVol")
    @SerializedName(value = "grossVol")
    private Double grossVol;

    @JsonProperty("grossVolUom")
    @SerializedName(value = "grossVolUom")
    private String grossVolUom;

    @JsonProperty("variant1")
    @SerializedName(value = "variant1")
    private String variant1;

    @JsonProperty("variant2")
    @SerializedName(value = "variant2")
    private String variant2;

    @JsonProperty("insertUser")
    @SerializedName(value = "insertUser")
    private String insertUser;

    @JsonProperty("updateUser")
    @SerializedName(value = "updateUser")
    private String updateUser;

    @JsonProperty("insertDate")
    @SerializedName(value = "insertDate")
    private Date insertDate;

    @JsonProperty("updateDate")
    @SerializedName(value = "updateDate")
    private Date updateDate;

    @JsonProperty("ttId")
    @SerializedName(value = "ttId")
    private Double ttId;


    public MtTruckTypeInfoDto(MtTruckType mtTruckType) {
        this.truckType = mtTruckType.getTruckType();
        this.loadFactor = mtTruckType.getLoadFactor();
        this.truckDesc = mtTruckType.getTruckDesc();
        this.tteCapacity = mtTruckType.getTteCapacity();
        this.grossWt = mtTruckType.getGrossWt();
        this.grossWtUom = mtTruckType.getGrossWtUom();
        this.grossVol = mtTruckType.getGrossVol();
        this.grossVolUom = mtTruckType.getGrossVolUom();
        this.variant1 = mtTruckType.getVariant1();
        this.variant2 = mtTruckType.getVariant2();
        this.ttId=mtTruckType.getTtId();

        this.insertUser = mtTruckType.getInsertUser();
        this.updateUser = mtTruckType.getUpdateUser();
        this.insertDate = mtTruckType.getInsertDate();
        this.updateDate = mtTruckType.getUpdateDate();
    }
}
