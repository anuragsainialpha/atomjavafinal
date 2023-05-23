package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.MtSapTruckType;
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
public class MtSapTruckTypeInfoDto {

    @JsonProperty("sapTruckType")
    @SerializedName(value = "sapTruckType")
    private String sapTruckType;

    @JsonProperty("sapTruckTypeDesc")
    @SerializedName(value = "sapTruckTypeDesc")
    private String sapTruckTypeDesc;

    @JsonProperty("opsTruckType")
    @SerializedName(value = "opsTruckType")
    private String opsTruckType;

    @JsonProperty("opsVariant1")
    @SerializedName(value = "opsVariant1")
    private String opsVariant1;

    @JsonProperty("sttId")
    @SerializedName(value = "sttId")
    private Double sttId;

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

    public MtSapTruckTypeInfoDto(MtSapTruckType mtSapTruckType) {
        this.sapTruckType = mtSapTruckType.getSapTruckType();
        this.sapTruckTypeDesc = mtSapTruckType.getSapTruckTypeDesc();
        this.opsTruckType = mtSapTruckType.getOpsTruckType();
        this.opsVariant1 = mtSapTruckType.getOpsVariant1();
        this.sttId = mtSapTruckType.getSttId();
        this.insertUser = mtSapTruckType.getInsertUser();
        this.updateUser = mtSapTruckType.getUpdateUser();
        this.insertDate = mtSapTruckType.getInsertDate();
        this.updateDate = mtSapTruckType.getUpdateDate();
    }
}
