package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MTPlantItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@NoArgsConstructor
public class MTPlantItemFilterDto {

    private String plantCode;
    private String effectiveDate;
    private String itemId;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<MTPlantItemInfoDto> itemsList = new ArrayList<>();

    public MTPlantItemFilterDto(Page<MTPlantItem> pageableFreight){
        System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(MTPlantItemInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }

    public MTPlantItemFilterDto(MTPlantItemFilterDto mtPlantItemFilterDto, Page<MTPlantItem> pageableFreight){
        this.itemId = mtPlantItemFilterDto.getItemId();
        this.plantCode = mtPlantItemFilterDto.getPlantCode();
        this.effectiveDate = mtPlantItemFilterDto.getEffectiveDate();
        this.itemsList = pageableFreight.stream().parallel().map(MTPlantItemInfoDto::new).collect(Collectors.toList());
        // System.out.println("itemId= "+mtOeBoms.get(0).getItemId());
        this.total = pageableFreight.getTotalElements();
    }


}
