package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MTCustomer;
//import com.api.apollo.atom.entity.master.MtCustomerType;
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
public class MtCustomerFilterDto {


    private String itemId;
    private String itemDescription;
    private String itemCategory;
    private String batchCode;

    private String custId;

    private String custName;

    private String city;

    private String country;

    private String isActive = "Y";

    private String fromInsertDate;

    private String toInsertDate;

    private String state;

    private List<String> stateCodes;

    private String custAcctGrp;

    private List<String> custTypes;





    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<MtCustomerInfoDto> itemsList = new ArrayList<>();

    public MtCustomerFilterDto(Page<MTCustomer> pageableFreight){
        System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(MtCustomerInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }


    public MtCustomerFilterDto(MtCustomerFilterDto filterDto, Page<MTCustomer> pageableItem){
        this.itemsList = pageableItem.stream().parallel().map(MtCustomerInfoDto::new).collect(Collectors.toList());
        this.total = pageableItem.getTotalElements();
    }
}
