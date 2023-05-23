package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.CtOtmFreightBasis;
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
public class CtOtmFreightBasisInfoDto {

    @JsonProperty("basis")
    @SerializedName(value = "basis")
    private String basis;

    @JsonProperty("description")
    @SerializedName(value = "description")
    private String description;

    @JsonProperty("inPaas")
    @SerializedName(value = "inPaas")
    private String inPaas;

    @JsonProperty("inOtm")
    @SerializedName(value = "inOtm")
    private String inOtm;

    @JsonProperty("otmBasis")
    @SerializedName(value = "otmBasis")
    private String otmBasis;




    public CtOtmFreightBasisInfoDto(CtOtmFreightBasis CtOtmFreightBasis) {
        this.basis=CtOtmFreightBasis.getBasis();
        this.description = CtOtmFreightBasis.getDescription();
        this.inPaas = CtOtmFreightBasis.getInPaas();
        this.inOtm = CtOtmFreightBasis.getInOtm();
        this.otmBasis = CtOtmFreightBasis.getOtmBasis();

    }
}
