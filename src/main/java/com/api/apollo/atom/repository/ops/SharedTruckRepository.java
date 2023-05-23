package com.api.apollo.atom.repository.ops;

import com.api.apollo.atom.entity.ops.SharedTruck;
import com.api.apollo.atom.entity.ops.SharedTruckId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SharedTruckRepository extends JpaRepository<SharedTruck, SharedTruckId> {

  List<SharedTruck> findAllBySharedTruckIdPickUpLocAndStatus(String plantCode, String status);

  /*For DP_REP roles*/
  List<SharedTruck> findAllByStatus(String status);

  List<SharedTruck> findAllBySharedTruckIdShipmentIdAndStatus(String shipmentId, String status);

  Optional<SharedTruck> findBySharedTruckIdShipmentIdAndSharedTruckIdPickUpLoc(String shipmentId,String pickUpLoc);

  Optional<SharedTruck> findBySharedTruckIdShipmentIdAndSharedTruckIdPickUpLocNotAndStatus(String shipmentId, String plantCode, String name);
}
