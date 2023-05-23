package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.MTPlantItem;
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
public class MTPlantItemInfoDto {

    @JsonProperty("plantCode")
    @SerializedName(value = "plantCode")
    private String plantCode;

    @JsonProperty("effectiveDate")
    @SerializedName(value = "effectiveDate")
    private Date effectiveDate;

    @JsonProperty("itemId")
    @SerializedName(value = "itemId")
    private String itemId;

    @JsonProperty("weight")
    @SerializedName(value = "weight")
    private Double weight;

    @JsonProperty("weightUom")
    @SerializedName(value = "weightUom")
    private String weightUom;

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


    public MTPlantItemInfoDto(MTPlantItem mTPlantItem) {
        this.plantCode = mTPlantItem.getPlantCode();
        this.effectiveDate = mTPlantItem.getEffectiveDate();
        this.itemId = mTPlantItem.getItemId();
        this.weight = mTPlantItem.getWeight();
        this.weightUom = mTPlantItem.getWeightUom();
        this.insertUser = mTPlantItem.getInsertUser();
        this.updateUser = mTPlantItem.getUpdateUser();
        this.insertDate = mTPlantItem.getInsertDate();
        this.updateDate = mTPlantItem.getUpdateDate();
    }
}
