
package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.MTLocationBay;
import com.api.apollo.atom.entity.master.MTLocationBayEntity;
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
public class MtLocationBayInfoDto {

    @JsonProperty("locationId")
    @SerializedName(value = "locationId")
    private String locationId;

    @JsonProperty("bayId")
    @SerializedName(value = "bayId")
    private String bayId;

    @JsonProperty("bayDescription")
    @SerializedName(value = "bayDescription")
    private String bayDescription;

//    @JsonProperty("baySite")
//    @SerializedName(value = "baySite")
//    private String baySite;

    @JsonProperty("bayStatus")
    @SerializedName(value = "bayStatus")
    private String bayStatus;

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

    @JsonProperty("valveId")
    @SerializedName(value = "valveId")
    private Double valveId;


    @JsonProperty("ftAccessKey")
    @SerializedName(value = "ftAccessKey")
    private String ftAccessKey;

    @JsonProperty("lbId")
    @SerializedName(value = "lbId")
    private Integer lbId;


    public MtLocationBayInfoDto(MTLocationBayEntity mtLocationBay) {
        this.locationId=mtLocationBay.getLocationId();
        this.bayId=mtLocationBay.getBayId();
        this.bayDescription=mtLocationBay.getBayDescription();
        this.bayStatus=mtLocationBay.getBayStatus();
        this.insertUser = mtLocationBay.getInsertUser();
        this.updateUser = mtLocationBay.getUpdateUser();
        this.insertDate = mtLocationBay.getInsertDate();
        this.updateDate = mtLocationBay.getUpdateDate();
        this.lbId=mtLocationBay.getLbId();
    }
}
