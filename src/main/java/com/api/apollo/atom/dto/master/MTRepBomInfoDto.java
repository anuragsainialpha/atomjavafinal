package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.MTRepBom;
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
public class MTRepBomInfoDto {

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


    public MTRepBomInfoDto(MTRepBom mTRepBom) {
        this.salesSku = mTRepBom.getSalesSku();
        this.itemId = mTRepBom.getItemId();
        this.compQty = mTRepBom.getCompQty();
        this.itemSeq = mTRepBom.getItemSeq();
        this.insertUser = mTRepBom.getInsertUser();
        this.updateUser = mTRepBom.getUpdateUser();
        this.insertDate = mTRepBom.getInsertDate();
        this.updateDate = mTRepBom.getUpdateDate();
    }
}
