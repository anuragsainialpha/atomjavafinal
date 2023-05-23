

package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MTLocationBay;
import com.api.apollo.atom.entity.master.MTLocationBay;
import com.api.apollo.atom.entity.master.MTLocationBayEntity;
import com.api.apollo.atom.entity.master.MtTruckType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
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
public class MtLocationBayFilterDto {

    private String locationId;
    private String bayId;
    private String bayStatus;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<MtLocationBayInfoDto> itemsList = new ArrayList<>();

    public MtLocationBayFilterDto(Page<MTLocationBayEntity> pageableFreight){
        System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(MtLocationBayInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }


    public MtLocationBayFilterDto(MtLocationBayFilterDto filterDto, Page<MTLocationBayEntity> pageableItem){
        this.itemsList = pageableItem.stream().parallel().map(MtLocationBayInfoDto::new).collect(Collectors.toList());
        this.total = pageableItem.getTotalElements();
    }
}
