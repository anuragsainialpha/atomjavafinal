
package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.UmUserAssociation;
//import com.api.apollo.atom.entity.UserAssociationType;
import com.api.apollo.atom.entity.master.UmUserAssociationEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class UserAssociationFilterDto {


    private String userId;
    private String associationIdentifier;
    private String associationValue;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<UserAssociationInfoDto> itemsList = new ArrayList<>();

    public UserAssociationFilterDto(Page<UmUserAssociationEntity> pageableFreight){
        System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(UserAssociationInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }

    public UserAssociationFilterDto(UserAssociationFilterDto filterDto, Page<UmUserAssociationEntity> pageableItem){
        this.itemsList = pageableItem.stream().parallel().map(UserAssociationInfoDto::new).collect(Collectors.toList());
        this.total = pageableItem.getTotalElements();
    }
}
