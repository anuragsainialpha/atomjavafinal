
package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.MtExcessWaitingRepLimit;
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
public class MtExcessWaitingRepLimitInfoDto {

    @JsonProperty("id")
    @SerializedName(value = "id")
    private Double id;

    @JsonProperty("reportingLoc")
    @SerializedName(value = "reportingLoc")
    private String reportingLoc;

    @JsonProperty("excessTime")
    @SerializedName(value = "excessTime")
    private Double excessTime;

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


    public MtExcessWaitingRepLimitInfoDto(MtExcessWaitingRepLimit mtExcessWaitingLocLimit) {
        this.reportingLoc=mtExcessWaitingLocLimit.getReportingLoc();
        this.excessTime=mtExcessWaitingLocLimit.getExcessTime();
        this.insertUser = mtExcessWaitingLocLimit.getInsertUser();
        this.updateUser = mtExcessWaitingLocLimit.getUpdateUser();
        this.insertDate = mtExcessWaitingLocLimit.getInsertDate();
        this.updateDate = mtExcessWaitingLocLimit.getUpdateDate();
    }
}
