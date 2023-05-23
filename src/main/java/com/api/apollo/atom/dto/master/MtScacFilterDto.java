package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MtScac;
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
public class MtScacFilterDto {
    private String scac;
    private String companyName;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.PAGE_LIMIT;
    private long total;

    public List<MtScacInfoDto> itemsList = new ArrayList<>();

    public MtScacFilterDto(Page<MtScac> pageableFreight){
        System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(MtScacInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }


    public MtScacFilterDto(MtScacFilterDto filterDto, Page<MtScac> pageableItem){
        this.itemsList = pageableItem.stream().parallel().map(MtScacInfoDto::new).collect(Collectors.toList());
        this.total = pageableItem.getTotalElements();
    }
}
