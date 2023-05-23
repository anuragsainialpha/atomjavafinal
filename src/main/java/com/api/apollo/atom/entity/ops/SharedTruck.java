package com.api.apollo.atom.entity.ops;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "SHARED_TRUCK")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SharedTruck {

  @EmbeddedId
  @AttributeOverrides({
      @AttributeOverride(name = "shipmentId", column = @Column(name = "SHIPMENT_ID", nullable = false)),
      @AttributeOverride(name = "pickUpLoc", column = @Column(name = "PLANT_CODE", nullable = false))})
  private SharedTruckId sharedTruckId;

  @Column(name = "STATUS")
  private String status;

}
