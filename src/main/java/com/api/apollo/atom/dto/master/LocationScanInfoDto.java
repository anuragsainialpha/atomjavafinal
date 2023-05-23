package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.LocationScan;
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
public class LocationScanInfoDto {

    @JsonProperty("locationId")
    @SerializedName(value = "locationId")
    private String locationId;

    @JsonProperty("scannable")
    @SerializedName(value = "scannable")
    private String scannable;

    @JsonProperty("itemCategory")
    @SerializedName(value = "itemCategory")
    private String itemCategory;

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

    public LocationScanInfoDto(LocationScan locationScan) {
        this.locationId = locationScan.getLocationId();
        this.scannable = locationScan.getScannable();
        this.itemCategory = locationScan.getItemCategory();
        this.insertUser = locationScan.getInsertUser();
        this.updateUser = locationScan.getUpdateUser();
        this.insertDate = locationScan.getInsertDate();
        this.updateDate = locationScan.getUpdateDate();
    }
}
