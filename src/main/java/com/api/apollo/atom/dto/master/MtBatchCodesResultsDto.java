package com.api.apollo.atom.dto.master;

import javax.persistence.StoredProcedureQuery;

public class MtBatchCodesResultsDto {

    private Integer totalRecords;
    private Integer totalErrorRecords;

    private Long id;
    private String message;

    public MtBatchCodesResultsDto() {
    }

    public MtBatchCodesResultsDto(StoredProcedureQuery storedProcedure) {
        this.totalRecords = storedProcedure.getOutputParameterValue("p_tot_records") != null
                ? (Integer) storedProcedure.getOutputParameterValue("p_tot_records")
                : null;
    }
}
