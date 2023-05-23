package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TruckDedicatedFilterDto {

  private Long id;

  private String servprov;

  private String sourceLoc;

  private String destLoc;

  private List<String> truckType;

  private String truckNumber;

  private String fromExpiryDate;

  private String toExpiryDate;

  private String insertUser;

  private String updateUser;

  private int index = Constants.PAGE_INDEX;

  private int pageLength = Constants.ADMIN_PAGE_LIMIT;

  public boolean isFilterService(){
    return !StringUtils.isEmpty(this.servprov) || !StringUtils.isEmpty(this.sourceLoc) || !StringUtils.isEmpty(this.destLoc) ||
        (this.truckType != null && !this.truckType.isEmpty()) || !StringUtils.isEmpty(this.truckNumber) || (!StringUtils.isEmpty(this.toExpiryDate) && !StringUtils.isEmpty(this.fromExpiryDate));
  }

}
