package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MTRepBom;
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
public class MTRepBomFilterDto {

    private String salesSku;
    private String itemId;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<MTRepBomInfoDto> mtRepBoms = new ArrayList<>();

    public MTRepBomFilterDto(Page<MTRepBom> pageableFreight){
        System.out.println("****** SIZE: "+this.mtRepBoms.size());
        this.mtRepBoms = pageableFreight.stream().parallel().map(MTRepBomInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }

    public MTRepBomFilterDto(MTRepBomFilterDto mtRepBomFilterDto, Page<MTRepBom> pageableFreight){
        this.itemId = mtRepBomFilterDto.getItemId();
        this.salesSku = mtRepBomFilterDto.getSalesSku();
        this.mtRepBoms = pageableFreight.stream().parallel().map(MTRepBomInfoDto::new).collect(Collectors.toList());
        // System.out.println("itemId= "+mtOeBoms.get(0).getItemId());
        this.total = pageableFreight.getTotalElements();
    }

}
