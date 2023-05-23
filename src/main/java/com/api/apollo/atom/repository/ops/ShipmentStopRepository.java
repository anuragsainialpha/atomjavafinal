package com.api.apollo.atom.repository.ops;

import com.api.apollo.atom.entity.ops.ShipmentStop;
import com.api.apollo.atom.entity.ops.ShipmentStopId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ShipmentStopRepository extends JpaRepository<ShipmentStop, ShipmentStopId> {


  Optional<ShipmentStop> findFirstByOrderByInsertDateDesc();

  List<ShipmentStop> findAllByLocationId(String location);

  @Query(nativeQuery = true, value = "SELECT sh.shipment_id  from ((SELECT DISTINCT st.shipment_id as shipmentId from shared_truck  st WHERE st.plant_code = ?1 AND st.status = 'OPEN') " +
      "      UNION " +
      "     (SELECT DISTINCT s.shipment_id from shipment_stop s  WHERE s.location_id = ?1 and s.activity = 'D')) a, shipment  sh where sh.shipment_id = a.shipmentId and sh.status = 'INTRANSIT' ")
  List<String> findShipmentIdsByPlantLocFromShipmentStopAndSharedTruck(String plantCode);

  /*Intransit Trucks For DP_REP ROLES*/
  @Query(nativeQuery = true, value = "SELECT sh.shipment_id  from ((SELECT DISTINCT st.shipment_id as shipmentId from shared_truck  st WHERE  st.status = 'OPEN') " +
      "      UNION " +
      "     (SELECT DISTINCT s.shipment_id from shipment_stop s  WHERE s.activity = 'D')) a, shipment  sh where sh.shipment_id = a.shipmentId and sh.status = 'INTRANSIT' ")
  List<String> findShipmentIdsFromShipmentStopAndSharedTruck();

  List<ShipmentStop> findByShipmentStopIdShipmentIdAndLocationId(String shipmentId, String location);

  Optional<ShipmentStop> findTop1ByShipmentStopIdShipmentIdOrderByShipmentStopIdStopNumDesc(String shipmentId);

  boolean existsByShipmentStopIdShipmentIdAndLocationIdAndShipmentStopIdStopNumAndActivity(String shipmentId, String stopLocation, int stopNumber, String activity);

  boolean existsByShipmentStopIdShipmentIdAndLocationIdAndAndActivity(String shipmentId, String stopLocation, String activity);

  @Query("select DISTINCT s.locationId from ShipmentStop s where s.activity = 'P' ORDER BY s.locationId ASC")
  List<String> findDistinctSourceLoc();
}
