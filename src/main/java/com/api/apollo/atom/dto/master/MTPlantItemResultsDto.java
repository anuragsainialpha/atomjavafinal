package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants.MTPLANTITEMStatus;

import javax.persistence.StoredProcedureQuery;

public class MTPlantItemResultsDto {

    private Integer totalRecords;
    private Integer totalErrorRecords;

    private Long id;
    private String message;
    private MTPLANTITEMStatus status;

    public MTPlantItemResultsDto() {
    }

    public MTPlantItemResultsDto(StoredProcedureQuery storedProcedure) {
        this.totalRecords = storedProcedure.getOutputParameterValue("p_tot_records") != null
                ? (Integer) storedProcedure.getOutputParameterValue("p_tot_records")
                : null;
    }
}
