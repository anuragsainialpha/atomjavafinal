package com.api.apollo.atom.repository.ops;

import com.api.apollo.atom.entity.ops.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, String> {

  //To get Tte capacity of trucktype with shipement details
  @Query(value = "select t.tte_capacity, s.total_qty,s.total_tte,s.total_volume,s.weight_util, s.volume_util,s.truck_type, s.truck_number,s.actual_truck_type, t.gross_vol,t.gross_wt ,s.tte_util," +
                 " s.variant from mt_truck_type t, (SELECT actual_truck_type,total_qty,total_tte, tte_util,total_volume,weight_util, volume_util,truck_type, truck_number, (case  when variant_1 is null then 'NO VARIANT' else  variant_1 end) variant  from shipment where shipment_id = ?1) s" +
                 " where t.truck_type = s.actual_truck_type and  t.variant1 = s.variant ",nativeQuery = true)
   Map<String,Object> findShipmentAndTruckCapacity(String shipmentId);

  Optional<Shipment> findByShipmentId(String shipmentId);


}
