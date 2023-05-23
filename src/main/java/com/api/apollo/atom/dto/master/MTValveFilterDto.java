package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MTValve;
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
public class MTValveFilterDto {


    private String itemId;
    private String itemDescription;
    private String itemCategory;
    private String batchCode;


    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<MTValveInfoDto> itemsList = new ArrayList<>();

    public MTValveFilterDto(Page<MTValve> pageableFreight){
        System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(MTValveInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }


    public MTValveFilterDto(MTValveFilterDto filterDto, Page<MTValve> pageableItem){
        this.itemsList = pageableItem.stream().parallel().map(MTValveInfoDto::new).collect(Collectors.toList());
        this.total = pageableItem.getTotalElements();
    }
}
