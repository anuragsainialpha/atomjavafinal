package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MtMaterialGroup;
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
public class MtMaterialGroupFilterDto {
    private String materialGroupId;
    private String description_1;
    private String description_2;
    private String scmGroup;
    private String insertUser;
    private Date insertDate;
    private String updateUser;
    private Date updateDate;
    private Double mgId;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<MtMaterialGroupInfoDto> mtMaterialGroupList = new ArrayList<>();

    public MtMaterialGroupFilterDto(Page<MtMaterialGroup> pageableFreight){
        System.out.println("****** SIZE: "+this.mtMaterialGroupList.size());
        this.mtMaterialGroupList = pageableFreight.stream().parallel().map(MtMaterialGroupInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }


    public MtMaterialGroupFilterDto(MtMaterialGroupFilterDto MtMaterialGroupFilterDto, Page<MtMaterialGroup> pageableItem){
        this.mtMaterialGroupList = pageableItem.stream().parallel().map(MtMaterialGroupInfoDto::new).collect(Collectors.toList());
        // System.out.println("itemId= "+mtOeBoms.get(0).getItemId());
        this.total = pageableItem.getTotalElements();
    }
}
