package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.Freight;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class FreightFilterDto {

    private String transporterSapCode;
    private String servprov;
    private String sourceLoc;
    private String sourceDesc;
    private String destLoc;
    private String destDesc;
    private String truckType;
    private String condition1;
    private String effectiveDate;
    private String expiryDate;
    private String ttDays;
    private String baseFreight;
    private String status;
    private String transportMode;

    private Double previousRate;
    private String diff;
    private String percentile;
    private String rateRecordId;

    private String fromInsertDate;

    private String toInsertDate;

    private String expiryDateFilterType;

    private String fromExpiryDate;

    private String toExpiryDate;


    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.PAGE_LIMIT;
    private long total;

    public List<FreightInfoDto> freights = new ArrayList<>();

    public FreightFilterDto(Page<Freight> pageableFreight){
        System.out.println("****** "+this.freights.size());
        this.freights = pageableFreight.stream().parallel().map(FreightInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }

    public FreightFilterDto(FreightFilterDto freightFilterDto, Page<Freight> pageableFreight){


        this.transportMode = freightFilterDto.getTransportMode();
        this.previousRate = freightFilterDto.getPreviousRate();
        this.diff = freightFilterDto.getDiff();
        this.percentile = freightFilterDto.getPercentile();
        //   this.rateRecordId = freightFilterDto.getRateRecordId();

        this.transporterSapCode = freightFilterDto.getTransporterSapCode();
        this.servprov = freightFilterDto.getServprov();
        this.sourceLoc = freightFilterDto.getSourceLoc();
        this.sourceDesc = freightFilterDto.getSourceDesc();
        this.destLoc = freightFilterDto.getDestLoc();
        this.destDesc = freightFilterDto.getDestDesc();
        this.truckType = freightFilterDto.getTruckType();
        this.condition1 = freightFilterDto.getCondition1();
        this.effectiveDate = freightFilterDto.getEffectiveDate();
        this.expiryDate = freightFilterDto.getExpiryDate();
        this.ttDays = freightFilterDto.getTtDays();
        this.baseFreight = freightFilterDto.getBaseFreight();
        this.status = freightFilterDto.getStatus();
        this.transportMode = freightFilterDto.getTransportMode();
        this.freights = pageableFreight.stream().parallel().map(FreightInfoDto::new).collect(Collectors.toList());
        // System.out.println("effective date from freights = "+freights.get(0).getEffectiveDate());
        // System.out.println("insert date from freights = "+freights.get(0).getInsertDate());
        this.total = pageableFreight.getTotalElements();
    }

}
