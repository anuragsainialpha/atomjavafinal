
package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.LocationType;
import com.api.apollo.atom.entity.master.MTLocation;
import com.api.apollo.atom.entity.master.MTLocation;
import com.api.apollo.atom.entity.master.MtTruckType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class MtLocationFilterDto {


    private String id;
    private String locationDesc;
    private String locationType;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String isActive;
    private Double lat;
    private Double lon;
    private Integer stateCode;
    private String gstNo;
    private String gstState;
    private String panNo;
    private String locationClass;
    private String linkedPlant;
    private String ftAccessKey;
    private boolean excelExport;

    private String emailID;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<MtLocationInfoDto> itemsList = new ArrayList<>();

    public MtLocationFilterDto(Page<MTLocation> pageableFreight){
        System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(MtLocationInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }


    public MtLocationFilterDto(MtLocationFilterDto filterDto, Page<MTLocation> pageableItem){
        this.itemsList = pageableItem.stream().parallel().map(MtLocationInfoDto::new).collect(Collectors.toList());
        this.total = pageableItem.getTotalElements();
    }

    public MtLocationFilterDto(MtLocationFilterDto filterDto, List<MTLocation> list){
        this.itemsList = list.stream().parallel().map(MtLocationInfoDto::new).collect(Collectors.toList());
    }


}
