package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MtElr;
import com.api.apollo.atom.entity.master.MtTruckType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import javax.persistence.Column;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class MtElrFilterDto {


    private String locationId;
    private String servprov;
    private String elrFlag;


    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<MtElrInfoDto> itemsList = new ArrayList<>();

    public MtElrFilterDto(Page<MtElr> pageableFreight){
        System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(MtElrInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }


    public MtElrFilterDto(MtElrFilterDto filterDto, Page<MtElr> pageableItem){
        this.itemsList = pageableItem.stream().parallel().map(MtElrInfoDto::new).collect(Collectors.toList());
        this.total = pageableItem.getTotalElements();
    }
}
