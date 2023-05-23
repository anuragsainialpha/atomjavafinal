package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.FreightTemp;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import javax.persistence.StoredProcedureQuery;


import javax.persistence.StoredProcedureQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ResultsDto {

    private Integer totalRecords;
    private Integer totalErrorRecords;
    private String message;

    public void ResultsDto(){

    }

    public ResultsDto(StoredProcedureQuery storedProcedure, String message, Integer totalRecords ) {
        try {
            this.totalRecords = totalRecords;
            this.message = message;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultsDto(StoredProcedureQuery storedProcedure, Integer totalRecords ){
        this.totalRecords = totalRecords;

    }




}
