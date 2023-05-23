

package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.UserActiveType;
import com.api.apollo.atom.constant.UserRole;
import com.api.apollo.atom.entity.ApplicationUser;
//import com.api.apollo.atom.entity.master.UserType;
import com.api.apollo.atom.entity.master.UmUserRole;
import com.api.apollo.atom.entity.master.UserEntity;
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
public class UserFilterDto {

    private String userId;
    private String status;
    private String roleId;
    private String password;
    private String plantCode;
    private String firstName;
    private String lastName;
    private String emailId;
    private Date lastLoginDate;



    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<UserInfoDto> itemsList = new ArrayList<>();

    public UserFilterDto(Page<UserEntity> pageableFreight){
        System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(UserInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }


    public UserFilterDto(UserFilterDto filterDto, Page<UserEntity> pageableItem){
        this.itemsList = pageableItem.stream().parallel().map(UserInfoDto::new).collect(Collectors.toList());
        this.total = pageableItem.getTotalElements();
    }


    public UserFilterDto(UserFilterDto filterDto, List<UserEntity> list){
        this.itemsList = list.stream().parallel().map(UserInfoDto::new).collect(Collectors.toList());
    }
}
