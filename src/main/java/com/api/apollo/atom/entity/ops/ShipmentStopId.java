package com.api.apollo.atom.entity.ops;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentStopId implements Serializable {
  private static final long serialVersionUID = 1L;

  @Column(name = "SHIPMENT_ID", nullable = false)
  private String shipmentId;

  @Column(name = "STOP_NUM", nullable = false)
  private int stopNum;

  @Column(name = "LOADSLIP_ID", nullable = false)
  private String loadslipId;
}
