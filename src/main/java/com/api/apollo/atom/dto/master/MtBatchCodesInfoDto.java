package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.MtBatchCodes;
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
public class MtBatchCodesInfoDto {

    @JsonProperty("batchCode")
    @SerializedName(value = "batchCode")
    private String batchCode;

    @JsonProperty("category")
    @SerializedName(value = "category")
    private String category;

    @JsonProperty("plantCode")
    @SerializedName(value = "plantCode")
    private String plantCode;

    @JsonProperty("batchDescription")
    @SerializedName(value = "batchDescription")
    private String batchDescription;

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

    @JsonProperty("bcId")
    @SerializedName(value = "bcId")
    private Double bcId;

    public MtBatchCodesInfoDto(MtBatchCodes mtBatchCodes) {
        this.batchCode = mtBatchCodes.getBatchCode();
        this.category = mtBatchCodes.getCategory();
        this.plantCode = mtBatchCodes.getPlantCode();
        this.batchDescription = mtBatchCodes.getBatchDescription();

        this.insertUser = mtBatchCodes.getInsertUser();
        this.updateUser = mtBatchCodes.getUpdateUser();
        this.insertDate = mtBatchCodes.getInsertDate();
        this.updateDate = mtBatchCodes.getUpdateDate();
        this.bcId =mtBatchCodes.getBcId();
    }
}
