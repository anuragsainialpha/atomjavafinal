package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MTOeBom;
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
public class MTOeBomFilterDto {

    private String salesSku;
    private String itemId;
    private String oeCode;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<MTOeBomInfoDto> mtOeBoms = new ArrayList<>();

    public MTOeBomFilterDto(Page<MTOeBom> pageableFreight){
        System.out.println("****** SIZE: "+this.mtOeBoms.size());
        this.mtOeBoms = pageableFreight.stream().parallel().map(MTOeBomInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }


    public MTOeBomFilterDto(MTOeBomFilterDto mTOeBomFilterDto, Page<MTOeBom> pageableFreight){
        this.itemId = mTOeBomFilterDto.getItemId();
        this.oeCode = mTOeBomFilterDto.getOeCode();
        this.oeCode = mTOeBomFilterDto.getSalesSku();
        this.mtOeBoms = pageableFreight.stream().parallel().map(MTOeBomInfoDto::new).collect(Collectors.toList());
        // System.out.println("itemId= "+mtOeBoms.get(0).getItemId());
        this.total = pageableFreight.getTotalElements();
    }

}
