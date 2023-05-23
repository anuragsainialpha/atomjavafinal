package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.LocationScan;
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
public class LocationScanFilterDto {

    private String locationId;
    private String scannable;
    private String itemCategory;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<LocationScanInfoDto> locationScanList = new ArrayList<>();

    public LocationScanFilterDto(Page<LocationScan> pageableFreight){
        System.out.println("****** SIZE: "+this.locationScanList.size());
        this.locationScanList = pageableFreight.stream().parallel().map(LocationScanInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }


    public LocationScanFilterDto(LocationScanFilterDto locationScanFilterDto, Page<LocationScan> pageableItem){
        this.locationScanList = pageableItem.stream().parallel().map(LocationScanInfoDto::new).collect(Collectors.toList());
        // System.out.println("itemId= "+mtOeBoms.get(0).getItemId());
        this.total = pageableItem.getTotalElements();
    }
}
