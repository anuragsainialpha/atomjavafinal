

package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MtCustomerShipTo;
//import com.api.apollo.atom.entity.master.MtCustomerShipToType;
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
public class MtCustomerShipToFilterDto {


    private String itemId;
    private String itemDescription;
    private String itemCategory;
    private String batchCode;


    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<MtCustomerShipToInfoDto> itemsList = new ArrayList<>();

    public MtCustomerShipToFilterDto(Page<MtCustomerShipTo> pageableFreight){
        System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(MtCustomerShipToInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }


    public MtCustomerShipToFilterDto(MtCustomerShipToFilterDto filterDto, Page<MtCustomerShipTo> pageableItem){
        this.itemsList = pageableItem.stream().parallel().map(MtCustomerShipToInfoDto::new).collect(Collectors.toList());
        this.total = pageableItem.getTotalElements();
    }
}
