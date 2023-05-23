package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants.MTREPBOMFreightStatus;

import javax.persistence.StoredProcedureQuery;

public class MTRepBomResultsDto {

    private Integer totalRecords;
    private Integer totalErrorRecords;

    private Long id;
    private String message;
    private MTREPBOMFreightStatus status;

    public MTRepBomResultsDto() {
    }

    public MTRepBomResultsDto(StoredProcedureQuery storedProcedure) {
        this.totalRecords = storedProcedure.getOutputParameterValue("p_tot_records") != null
                ? (Integer) storedProcedure.getOutputParameterValue("p_tot_records")
                : null;
    }
}
