
package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.CtUom;
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
public class CtUomInfoDto {

    @JsonProperty("itemId")
    @SerializedName(value = "itemId")
    private String itemId;

    @JsonProperty("itemDescription")
    @SerializedName(value = "itemDescription")
    private String itemDescription;

    @JsonProperty("itemCategory")
    @SerializedName(value = "itemCategory")
    private String itemCategory;

    @JsonProperty("batchCode")
    @SerializedName(value = "batchCode")
    private String batchCode;

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


    public CtUomInfoDto(CtUom CtUom) {
        this.itemId=CtUom.getItemId();
        this.itemDescription = CtUom.getItemDescription();
        this.itemCategory = CtUom.getItemCategory();
        this.batchCode = CtUom.getBatchCode();
        this.insertUser = CtUom.getInsertUser();
        this.updateUser = CtUom.getUpdateUser();
        this.insertDate = CtUom.getInsertDate();
        this.updateDate = CtUom.getUpdateDate();
        this.valveId = CtUom.getValveId();
    }
}