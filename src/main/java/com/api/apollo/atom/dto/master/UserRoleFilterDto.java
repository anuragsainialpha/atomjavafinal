
package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.UmUserRole;
//import com.api.apollo.atom.entity.master.UserRoleType;
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
public class UserRoleFilterDto {

    private String userRoleId;
    private String description;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<UserRoleInfoDto> itemsList = new ArrayList<>();

    public UserRoleFilterDto(Page<UmUserRole> pageableFreight){
        System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(UserRoleInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }


    public UserRoleFilterDto(UserRoleFilterDto filterDto, Page<UmUserRole> pageableItem){
        this.itemsList = pageableItem.stream().parallel().map(UserRoleInfoDto::new).collect(Collectors.toList());
        this.total = pageableItem.getTotalElements();
    }

    public UserRoleFilterDto(UserRoleFilterDto filterDto, List<UmUserRole> list){
        this.itemsList = list.stream().parallel().map(UserRoleInfoDto::new).collect(Collectors.toList());
    }





}