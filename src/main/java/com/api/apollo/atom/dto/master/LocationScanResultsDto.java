package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;

import javax.persistence.StoredProcedureQuery;

public class LocationScanResultsDto {

    private Integer totalRecords;
    private Integer totalErrorRecords;

    private Long id;
    private String message;

    public LocationScanResultsDto() {
    }

    public LocationScanResultsDto(StoredProcedureQuery storedProcedure) {
        this.totalRecords = storedProcedure.getOutputParameterValue("p_tot_records") != null
                ? (Integer) storedProcedure.getOutputParameterValue("p_tot_records")
                : null;
    }



}
