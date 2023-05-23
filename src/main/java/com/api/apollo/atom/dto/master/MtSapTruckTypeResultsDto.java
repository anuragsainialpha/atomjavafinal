package com.api.apollo.atom.dto.master;

import javax.persistence.StoredProcedureQuery;

public class MtSapTruckTypeResultsDto {
    private Integer totalRecords;
    private Integer totalErrorRecords;

    private Long id;
    private String message;

    public MtSapTruckTypeResultsDto() {
    }

    public MtSapTruckTypeResultsDto(StoredProcedureQuery storedProcedure) {
        this.totalRecords = storedProcedure.getOutputParameterValue("p_tot_records") != null
                ? (Integer) storedProcedure.getOutputParameterValue("p_tot_records")
                : null;

    }
}
