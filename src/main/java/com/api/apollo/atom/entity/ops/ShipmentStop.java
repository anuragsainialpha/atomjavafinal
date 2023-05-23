package com.api.apollo.atom.entity.ops;

import com.api.apollo.atom.entity.ApplicationUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "SHIPMENT_STOP")
@Getter
@Setter
@NoArgsConstructor
public class ShipmentStop {

  @EmbeddedId
  @AttributeOverrides({
      @AttributeOverride(name = "shipmentId", column = @Column(name = "SHIPMENT_ID", nullable = false)),
      @AttributeOverride(name = "stopNum", column = @Column(name = "STOP_NUM", nullable = false)),
      @AttributeOverride(name = "loadslipId", column = @Column(name = "LOADSLIP_ID", nullable = false))})
  private ShipmentStopId shipmentStopId;

  @Column(name = "LOCATION_ID")
  private String locationId;

  @Column(name = "ACTIVITY")
  private String activity;

  @Column(name = "ACTUAL_ARRIVAL_DATE")
  private String actualArrivalDate;

  @Column(name = "INSERT_USER", nullable = false)
  private String insertUser;

  @Column(name = "INSERT_DATE", nullable = false)
  private Date insertDate;

  @Column(name = "UPDATE_USER")
  private String updateUser;

  @Column(name = "UPDATE_DATE")
  private Date updateDate;

  public ShipmentStop(ShipmentStopId shipmentStopId, String location, String activityType, ApplicationUser loggedInUser, Date insertDate) {
    this.shipmentStopId = shipmentStopId;
    this.locationId = location;
    this.activity = activityType;
    this.insertUser = loggedInUser.getUserId();
    this.insertDate = insertDate;
  }

}
