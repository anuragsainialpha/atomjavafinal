
package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.CtUomMap;
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
public class CtUomMapInfoDto {

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


    public CtUomMapInfoDto(CtUomMap CtUomMap) {

        this.insertUser = CtUomMap.getInsertUser();
        this.updateUser = CtUomMap.getUpdateUser();
        this.insertDate = CtUomMap.getInsertDate();
        this.updateDate = CtUomMap.getUpdateDate();
    }
}
