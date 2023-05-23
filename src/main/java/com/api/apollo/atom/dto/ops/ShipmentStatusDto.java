package com.api.apollo.atom.dto.ops;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ShipmentStatusDto {

  public String shipmentWithNReportLoc;

  public Double shipmentWithN;

  public Double shipmentWithY;

  public Double cummShipmentWithN;

  public Double cummShipmentWithY;

  public Double total;

  public Double cummTotal;

  public ShipmentStatusDto() {}

  public ShipmentStatusDto(Double shipmentWithFrt, Double shipmentWithoutFrt) {
    this.shipmentWithN = shipmentWithFrt;
    this.shipmentWithY = shipmentWithoutFrt;
  }

}
