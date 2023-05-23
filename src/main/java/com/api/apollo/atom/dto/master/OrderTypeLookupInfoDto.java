package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.OrderTypeLookup;
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
public class OrderTypeLookupInfoDto {

    @JsonProperty("orderType")
    @SerializedName(value = "orderType")
    private String orderType;

    @JsonProperty("movementType")
    @SerializedName(value = "movementType")
    private String movementType;

    @JsonProperty("marketSegment")
    @SerializedName(value = "marketSegment")
    private String marketSegment;

    @JsonProperty("sapOrderType")
    @SerializedName(value = "sapOrderType")
    private String sapOrderType;

    @JsonProperty("sapDocType")
    @SerializedName(value = "sapDocType")
    private String sapDocType;

    @JsonProperty("bomType")
    @SerializedName(value = "bomType")
    private String bomType;

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


    public OrderTypeLookupInfoDto(OrderTypeLookup ordertypelookup) {
        this.orderType = ordertypelookup.getOrderType();
        this.movementType = ordertypelookup.getMovementType();
        this.marketSegment = ordertypelookup.getMarketSegment();
        this.sapOrderType = ordertypelookup.getSapOrderType();
        this.sapDocType = ordertypelookup.getSapDocType();
        this.bomType = ordertypelookup.getBomType();
        this.insertUser = ordertypelookup.getInsertUser();
        this.insertDate = ordertypelookup.getInsertDate();
        this.updateUser = ordertypelookup.getUpdateUser();
        this.updateDate = ordertypelookup.getUpdateDate();
    }
}
