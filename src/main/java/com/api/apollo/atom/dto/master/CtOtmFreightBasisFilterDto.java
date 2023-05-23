package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.CtOtmFreightBasis;
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
public class CtOtmFreightBasisFilterDto {


    private String basis;
    private String description;
    private String inPaas;
    private String inOtm;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<CtOtmFreightBasisInfoDto> itemsList = new ArrayList<>();

    public CtOtmFreightBasisFilterDto(Page<CtOtmFreightBasis> pageableFreight){
        System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(CtOtmFreightBasisInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }


    public CtOtmFreightBasisFilterDto(CtOtmFreightBasisFilterDto filterDto, Page<CtOtmFreightBasis> pageableItem){
        this.itemsList = pageableItem.stream().parallel().map(CtOtmFreightBasisInfoDto::new).collect(Collectors.toList());
        this.total = pageableItem.getTotalElements();
    }
}
