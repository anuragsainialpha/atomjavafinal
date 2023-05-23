package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MTOeBom;
import com.api.apollo.atom.entity.master.MtTruckType;
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
public class MtTruckTypeFilterDto {

    private String truckType;
    private Double loadFactor;
    private String truckDesc;
    private Double tteCapacity;
    private String variant1;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<MtTruckTypeInfoDto> itemsList = new ArrayList<>();

    public MtTruckTypeFilterDto(Page<MtTruckType> pageableFreight){
        System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(MtTruckTypeInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }


    public MtTruckTypeFilterDto(MtTruckTypeFilterDto filterDto, Page<MtTruckType> pageableItem){
        this.itemsList = pageableItem.stream().parallel().map(MtTruckTypeInfoDto::new).collect(Collectors.toList());
        this.total = pageableItem.getTotalElements();
    }

}
