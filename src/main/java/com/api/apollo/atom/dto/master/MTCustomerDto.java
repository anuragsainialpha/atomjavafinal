package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MTCustomer;
import com.api.apollo.atom.util.DateUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Setter
@Getter
@NoArgsConstructor
public class MTCustomerDto {

  private String id;

  private String customerName;

  private String custAcctGRP;

  private String address;

  private String city;

  private String state;

  private String postalCode;

  private String country;

  private String isActive;

  private Double latitude;

  private Double longitude;

  private String deliveryTerms;

  private String insertUser;

  private String insertDate;

  private String updateUser;

  private String updateDate;

  private String customerType;

  private String gstNum;

  private String panNum;

  private String stateCode;

  private String gstCode;

  public MTCustomerDto(MTCustomer mtCustomer){
    this.id = mtCustomer.getId();
    this.address = mtCustomer.getAddress();
    this.city = mtCustomer.getCity();
    this.country = mtCustomer.getCountry();
    this.custAcctGRP = mtCustomer.getCustAcctGRP();
    this.customerName = mtCustomer.getCustomerName();
    this.customerType = mtCustomer.getCustomerType();
    this.deliveryTerms = mtCustomer.getDeliveryTerms();
    this.gstCode = mtCustomer.getGstCode();
    this.gstNum = mtCustomer.getGstNum();
    this.insertDate = mtCustomer.getInsertDate() != null ? DateUtils.formatDate(mtCustomer.getInsertDate(), Constants.DATE_TIME_FORMAT) : null;
    this.insertUser = mtCustomer.getInsertUser();
    this.isActive = mtCustomer.getIsActive();
    this.latitude = mtCustomer.getLatitude();
    this.longitude = mtCustomer.getLongitude();
    this.panNum = mtCustomer.getPanNum();
    this.postalCode = mtCustomer.getPostalCode();
    this.state = mtCustomer.getState();
    this.stateCode = mtCustomer.getStateCode();
    this.updateDate = mtCustomer.getUpdateDate() != null ? DateUtils.formatDate(mtCustomer.getUpdateDate(), Constants.DATE_TIME_FORMAT) : null;
    this.updateUser = mtCustomer.getUpdateUser();


  }

}
