package com.api.apollo.atom.entity.ops;

import com.api.apollo.atom.constant.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SharedTruckInfo {

  private String shipmentId;

  private String truckNumber;

  private String truckType;

  private String actualTruckType;

  private String variant1;

  private String variant2;

  private String containerNum;

  private String type;

  private boolean allowCreateLS = true;

  public SharedTruckInfo(String shipmentId, String truckNumber, String truckType, String actualTruckType, String variant1, String variant2, String containerNum, String type) {
    this.shipmentId = shipmentId;
    this.truckNumber = truckNumber;
    this.truckType = truckType;
    this.actualTruckType = actualTruckType;
    this.variant1 = variant1;
    this.variant2 = variant2;
    this.containerNum = containerNum;
    this.type = type;
  }
}
