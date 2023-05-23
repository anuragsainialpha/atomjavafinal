package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.MtScac;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class MtScacInfoDto {

    @JsonProperty("ID")
    @SerializedName(value = "ID")
    private String scac;

    @JsonProperty("Name")
    @SerializedName(value = "Name")
    private String companyName;

    public MtScacInfoDto(MtScac mtScac) {
        this.scac = mtScac.getScac();
        this.companyName = mtScac.getCompanyName();
    }
}
