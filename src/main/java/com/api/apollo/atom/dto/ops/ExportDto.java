package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.entity.ops.DelInvHeader;
import com.api.apollo.atom.entity.ops.DelInvLine;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
@Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExportDto {
    private List<DelInvHeader> delInvHeaders = new ArrayList<>();
    private List<DelInvLine> itemListForInvoice = new ArrayList<>();
    private List<Map<String,String>> gatedInTrucks = new ArrayList<>();
    private List<String> exportInvoiceList = new ArrayList<>();
    private List<ExportInfoDto> exportInfoDtoList = new ArrayList<>();
    private Map<String, String> marketSegmentMap = new HashMap<>();
    private LoadSlipUtilizationDto loadSlipUtilizationDto;
}


