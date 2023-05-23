package com.api.apollo.atom.repository.ops;

import com.api.apollo.atom.entity.master.MTCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MTCustomerRepository extends JpaRepository<MTCustomer, String> {

  @Query(nativeQuery = true, value = "SELECT Count(*) FROM "
      + "            (SELECT cust_id FROM mt_customer " +
      "              UNION " +
      "              SELECT cust_id FROM mt_customer_ship_to) WHERE  cust_id = ?1")
  int getLocationCount(String destLoc);

  @Query(nativeQuery = true, value = "SELECT ship_to_id FROM mt_customer_ship_to WHERE cust_id = ?1")
  List<String> findShipToLocationsByCustomerId(String destLoc);

  @Query(nativeQuery = true, value = "select SHIP_TO_ID from mt_customer_ship_to where SHIP_TO_ID = ?1")
  List<String> existsByShipToId(String shipToId);

  @Query("SELECT new com.api.apollo.atom.entity.master.MTCustomer(m.id,m.gstNum,m.panNum) FROM MTCustomer m where m.id = ?1")
  Optional<MTCustomer> getGstNumPanNumByCustId(String custId);

  boolean existsById(String custId);

  @Query(nativeQuery = true, value = "select mt.cust_id as custId, mt.country as country  from mt_customer mt where mt.cust_id = ?1")
  Map<String, Object> findByCustId(String custId);

  @Query("select distinct mt.stateCode from MTCustomer mt where mt.stateCode is NOT NULL ORDER BY mt.stateCode")
  List<String> findDistinctStateCode();

  @Query("select distinct mt.customerType from MTCustomer mt where mt.customerType is NOT NULL ORDER BY mt.customerType")
  List<String> findDistinctCustType();
}
