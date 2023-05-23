package com.api.apollo.atom.dto.master;

import javax.persistence.StoredProcedureQuery;

public class MtValveResultsDto {

    private Integer totalRecords;
    private Integer totalErrorRecords;

    private Long id;
    private String message;
   // private MTREPBOMFreightStatus status;

    public MtValveResultsDto() {
    }

    public MtValveResultsDto(StoredProcedureQuery storedProcedure) {
        this.totalRecords = storedProcedure.getOutputParameterValue("p_tot_records") != null
                ? (Integer) storedProcedure.getOutputParameterValue("p_tot_records")
                : null;
    }
}
