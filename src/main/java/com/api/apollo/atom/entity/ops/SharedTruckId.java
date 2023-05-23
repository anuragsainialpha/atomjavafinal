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
public class SharedTruckId implements Serializable {

  private static final long serialVersionUID = 1L;

  @Column(name = "SHIPMENT_ID")
  private String shipmentId;

  @Column(name = "PLANT_CODE")
  private String pickUpLoc;
}
