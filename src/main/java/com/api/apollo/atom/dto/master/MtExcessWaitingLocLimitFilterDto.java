
package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MtExcessWaitingLocLimit;
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
public class MtExcessWaitingLocLimitFilterDto {


    private String reportingLoc;
    private Double excessTime;


    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<MtExcessWaitingLocLimitInfoDto> itemsList = new ArrayList<>();

    public MtExcessWaitingLocLimitFilterDto(Page<MtExcessWaitingLocLimit> pageableFreight){
        System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(MtExcessWaitingLocLimitInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }


    public MtExcessWaitingLocLimitFilterDto(MtExcessWaitingLocLimitFilterDto filterDto, Page<MtExcessWaitingLocLimit> pageableItem){
        this.itemsList = pageableItem.stream().parallel().map(MtExcessWaitingLocLimitInfoDto::new).collect(Collectors.toList());
        this.total = pageableItem.getTotalElements();
    }
}
