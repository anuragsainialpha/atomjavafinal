package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MTTruckDedicated;
import com.api.apollo.atom.util.DateUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class MTTruckDedicatedDto {

  private Long id;

  private String servprov;

  private String sourceLoc;

  private String sourceDesc;

  private String destDesc;

  private String destLoc;

  private String truckType;

  private String truckNumber;

  private String expiryDate;

  private String insertUser;

  private String updateUser;

  private String insertDate;

  private String updateDate;

  public MTTruckDedicatedDto(MTTruckDedicated mtTruckDedicated){
    this.id = mtTruckDedicated.getId();
    this.servprov = mtTruckDedicated.getServProv();
    this.sourceLoc = mtTruckDedicated.getSourceLoc();
    this.destLoc = mtTruckDedicated.getDestLoc();
    this.sourceDesc = mtTruckDedicated.getSourceDesc();
    this.destDesc = mtTruckDedicated.getDestDesc();
    this.truckType = mtTruckDedicated.getTruckType();
    this.truckNumber = mtTruckDedicated.getTruckNumber();
    this.expiryDate = mtTruckDedicated.getExpiryDate() != null ? DateUtils.formatDate(mtTruckDedicated.getExpiryDate(), Constants.PLAN_RECORD_DATE_FORMAT) : null;
    this.insertUser = mtTruckDedicated.getInsertUser();
    this.updateUser = mtTruckDedicated.getUpdateUser();
    this.insertDate = mtTruckDedicated.getInsertDate() != null ? DateUtils.formatDate(mtTruckDedicated.getInsertDate(), Constants.DATE_TIME_FORMAT) : null;
    this.updateDate = mtTruckDedicated.getUpdateDate() != null ? DateUtils.formatDate(mtTruckDedicated.getUpdateDate(), Constants.DATE_TIME_FORMAT) : null;
  }

}
