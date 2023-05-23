
package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.MTLocation;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

//@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class MtLocationInfoDto {

    @JsonProperty("locationId")
    @SerializedName(value = "locationId")
    private String id;

    @JsonProperty("locationDesc")
    @SerializedName(value = "locationDesc")
    private String description;

    @JsonProperty("locationType")
    @SerializedName(value = "locationType")
    private String type;


    @JsonProperty("locationAddress")
    @SerializedName(value = "locationAddress")
    private String locationAddress;



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

    @JsonProperty("lat")
    @SerializedName(value = "lat")
    private Double lat;

    @JsonProperty("lon")
    @SerializedName(value = "long")
    private Double lon;

    @JsonProperty("stateCode")
    @SerializedName(value = "stateCode")
    private Integer stateCode;

    @JsonProperty("gstNo")
    @SerializedName(value = "gstNo")
    private String gstNum;


    @JsonProperty("gstState")
    @SerializedName(value = "gstState")
    private String gstState;


    @JsonProperty("panNo")
    @SerializedName(value = "panNo")
    private String panNum;

    @JsonProperty("ftAccessKey")
    @SerializedName(value = "ftAccessKey")
    private String ftAccessKey;


    @JsonProperty("locationClass")
    @SerializedName(value = "locationClass")
    private String locationClass;

    @JsonProperty("linkedPlant")
    @SerializedName(value = "linkedPlant")
    private String linkedPlant;

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

    @JsonProperty("emailID")
    @SerializedName(value = "emailID")
    private String emailID;




    public MtLocationInfoDto(MTLocation mtLocation) {
        this.id=mtLocation.getId();
        this.description=mtLocation.getDescription();
        this.type=mtLocation.getType();
        this.locationAddress=mtLocation.getAddress();
        this.city=mtLocation.getCity();
        this.state=mtLocation.getState();
        this.postalCode=mtLocation.getPostalCode();
        this.country=mtLocation.getCountry();
        this.isActive=mtLocation.getIsActive();
        this.lat=mtLocation.getLatitude();
        this.lon=mtLocation.getLongitude();
        this.stateCode=mtLocation.getStateCode();
        this.gstNum=mtLocation.getGstNum();
        this.state=mtLocation.getState();
        this.gstState = mtLocation.getGstState();
        this.panNum=mtLocation.getPanNum();
        this.ftAccessKey=mtLocation.getFtAccessKey();
        this.locationClass=mtLocation.getLocationClass();
        this.linkedPlant=mtLocation.getLinkedPlant();
        this.insertUser = mtLocation.getInsertUser();
        this.updateUser = mtLocation.getUpdateUser();
        this.insertDate = mtLocation.getInsertDate();
        this.updateDate = mtLocation.getUpdateDate();
        this.emailID = mtLocation.getEmailID();
    }
}
