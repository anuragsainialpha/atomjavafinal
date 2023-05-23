package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.OrderTypeLookup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class OrderTypeLookupFilterDto {

    private String orderType;
    private String movementType;
    private String marketSegment;
    private String sapOrderType;
    private String sapDocType;
    private String bomType;
    private String insertUser;
    private Date insertDate;
    private String updateUser;
    private Date updateDate;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<OrderTypeLookupInfoDto> itemsList = new ArrayList<>();

    public OrderTypeLookupFilterDto(Page<OrderTypeLookup> pageableFreight){
        //System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(OrderTypeLookupInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }


    public OrderTypeLookupFilterDto(OrderTypeLookupFilterDto OrderTypeLookupFilterDto, Page<OrderTypeLookup> pageableItem){
        this.itemsList = pageableItem.stream().parallel().map(OrderTypeLookupInfoDto::new).collect(Collectors.toList());
        // System.out.println("itemId= "+mtOeBoms.get(0).getItemId());
        this.total = pageableItem.getTotalElements();
    }

}
