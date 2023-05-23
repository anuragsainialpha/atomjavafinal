package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.MtElr;
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
public class MtElrInfoDto {

    @JsonProperty("locationId")
    @SerializedName(value = "locationId")
    private String locationId;

    @JsonProperty("servprov")
    @SerializedName(value = "servprov")
    private String servprov;

    @JsonProperty("elrFlag")
    @SerializedName(value = "elrFlag")
    private String elrFlag;

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




    public MtElrInfoDto(MtElr mtElr) {
        this.locationId=mtElr.getLocationId();
        this.servprov=mtElr.getServprov();
        this.elrFlag=mtElr.getElrFlag();
        this.insertUser = mtElr.getInsertUser();
        this.updateUser = mtElr.getUpdateUser();
        this.insertDate = mtElr.getInsertDate();
        this.updateDate = mtElr.getUpdateDate();

    }
}
