package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.MTOeBom;
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
public class MTOeBomInfoDto {

    @JsonProperty("salesSku")
    @SerializedName(value = "salesSku")
    private String salesSku;

    @JsonProperty("itemId")
    @SerializedName(value = "itemId")
    private String itemId;

    @JsonProperty("compQty")
    @SerializedName(value = "compQty")
    private Integer compQty;

    @JsonProperty("itemSeq")
    @SerializedName(value = "itemSeq")
    private Integer itemSeq;

    @JsonProperty("oeCode")
    @SerializedName(value = "oeCode")
    private String oeCode;

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


    public MTOeBomInfoDto(MTOeBom mTOeBom) {
        this.salesSku = mTOeBom.getSalesSku();
        this.itemId = mTOeBom.getItemId();
        this.compQty = mTOeBom.getCompQty();
        this.itemSeq = mTOeBom.getItemSeq();
        this.oeCode = mTOeBom.getOeCode();
        this.insertUser = mTOeBom.getInsertUser();
        this.updateUser = mTOeBom.getUpdateUser();
        this.insertDate = mTOeBom.getInsertDate();
        this.updateDate = mTOeBom.getUpdateDate();
    }

}
