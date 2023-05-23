package com.api.apollo.atom.dto.master;


import com.api.apollo.atom.entity.master.MTTransporter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class TransporterInfoDto {
    @JsonProperty("id")
    @SerializedName(value = "id")
    private String id;

    @JsonProperty("description")
    @SerializedName(value = "description")
    private String description;

    @JsonProperty("servprov")
    @SerializedName(value = "servprov")
    private String servprov;

    @JsonProperty("address")
    @SerializedName(value = "address")
    private String address;

    @JsonProperty("city")
    @SerializedName(value = "city")
    private String city;

    @JsonProperty("state")
    @SerializedName(value = "state")
    private String state;

    @JsonProperty("postalCode")
    @SerializedName(value = "postalCode")
    private String postalCode;

    @JsonProperty("country")
    @SerializedName(value = "country")
    private String country;

    @JsonProperty("isActive")
    @SerializedName(value = "isActive")
    private String isActive;

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

    @JsonProperty("industryKey")
    @SerializedName(value = "industryKey")
    private String industryKey;

    @JsonProperty("stateCode")
    @SerializedName(value = "stateCode")
    private String stateCode;

    @JsonProperty("gstNo")
    @SerializedName(value = "gstNo")
    private String gstNo;

    @JsonProperty("gstState")
    @SerializedName(value = "gstState")
    private String gstState;

    @JsonProperty("panNo")
    @SerializedName(value = "panNo")
    private String panNo;

    public TransporterInfoDto(MTTransporter transporter){
        this.id = transporter.getId();
        this.description = transporter.getDescription();
        this.servprov = transporter.getServprov();
        this.address = transporter.getAddress();
        this.city = transporter.getCity();
        this.state = transporter.getState();
        this.postalCode  = transporter.getPostalCode();
        this.country = transporter.getCountry();
        this.isActive = transporter.getIsActive();
        this.insertUser = transporter.getInsertUser();
        this.insertDate = transporter.getInsertDate();
        this.updateUser = transporter.getUpdateUser();
        this.updateDate  = transporter.getUpdateDate();
        this.industryKey = transporter.getIndustryKey();
        this.stateCode = transporter.getStateCode();
        this.gstNo = transporter.getGstNo();
        this.gstState = transporter.getGstState();
        this.panNo = transporter.getPanNo();

    }
}
