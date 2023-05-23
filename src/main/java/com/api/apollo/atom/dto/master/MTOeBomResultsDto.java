package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants.MTOEBOMStatus;

import javax.persistence.StoredProcedureQuery;

public class MTOeBomResultsDto {

    private Integer totalRecords;
    private Integer totalErrorRecords;

    private Long id;
    private String message;
    private MTOEBOMStatus status;

    public MTOeBomResultsDto() {
    }

    public MTOeBomResultsDto(StoredProcedureQuery storedProcedure) {
        this.totalRecords = storedProcedure.getOutputParameterValue("p_tot_records") != null
                ? (Integer) storedProcedure.getOutputParameterValue("p_tot_records")
                : null;
    }

}
