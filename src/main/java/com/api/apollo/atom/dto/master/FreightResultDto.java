package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants.FreightStatus;
import com.api.apollo.atom.entity.master.FreightTemp;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import javax.persistence.StoredProcedureQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class FreightResultDto {

    private Integer totalRecords;
    private Integer totalErrorRecords;
    private String message;
    private FreightStatus status;

    private List<FreightTempInfoDto> errorsRecords = new ArrayList<>();

    public FreightResultDto(StoredProcedureQuery storedProcedure, Page<FreightTemp> pageableFreightErrors, Integer totalRecords ) {
        try {
            this.totalRecords = totalRecords;
                    /*= storedProcedure.getOutputParameterValue("p_tot_records") != null
                    ? (Integer) storedProcedure.getOutputParameterValue("p_tot_records")
                    : null;*/
            this.errorsRecords = pageableFreightErrors.stream().parallel().map(FreightTempInfoDto::new).collect(Collectors.toList());

            this.totalErrorRecords = errorsRecords.size();

            /*
            Clob err = (Clob) storedProcedure.getOutputParameterValue("p_error_out");
            Long len = err.length();
            String substring = err.getSubString(1,  len.intValue());
            System.out.println("error result - "+substring);
            this.errorRec = storedProcedure.getOutputParameterValue("p_error_out") != null
                    ? substring
                    : null;
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void FreightResultDto(){

    }
    public FreightResultDto(StoredProcedureQuery storedProcedure, Integer totalRecords ){
        this.totalRecords = totalRecords;

    }


}
