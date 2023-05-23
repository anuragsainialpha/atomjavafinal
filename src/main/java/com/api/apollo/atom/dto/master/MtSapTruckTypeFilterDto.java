package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MTOeBom;
import com.api.apollo.atom.entity.master.MtSapTruckType;
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
public class MtSapTruckTypeFilterDto {

    private String sapTruckType;
    private String sapTruckTypeDesc;
    private String opsTruckType;
    private String opsVariant1;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<MtSapTruckTypeInfoDto> itemsList = new ArrayList<>();

    public MtSapTruckTypeFilterDto(Page<MtSapTruckType> pageableFreight){
        System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(MtSapTruckTypeInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }

    public MtSapTruckTypeFilterDto(MtSapTruckTypeFilterDto filterDto, Page<MtSapTruckType> pageableItem){
        this.itemsList = pageableItem.stream().parallel().map(MtSapTruckTypeInfoDto::new).collect(Collectors.toList());
        this.total = pageableItem.getTotalElements();
    }
}
