package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.master.Freight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FreightRepository extends JpaRepository<Freight, Double> {

  Optional<Freight> findBySourceLocAndDestLocAndServprovAndTruckType(String source, String destination ,String servprov ,String truckType);

  List<Freight> findAllBySourceLocAndDestLocAndServprovAndTruckTypeOrderByEffectiveDateDesc(String source, String destination ,String servprov ,String truckType);

  List<Freight> findAllBySourceLocAndDestLocAndServprovOrderByEffectiveDateDesc(String source, String destination ,String servprov);

  List<Freight> findAllBySourceLocAndServprovOrderByEffectiveDateDesc(String source, String servprov);

  boolean existsBySourceLocAndDestLocAndServprovAndTruckType(String source, String destination ,String servprov ,String truckType);

  List<Freight> findBySourceLocAndDestLocAndServprovAndTruckTypeAndCondition1(String source, String destination ,String servprov ,String truckType,String variant);

  List<Freight> findTop3BySourceLocAndDestLocAndTruckTypeOrderByBaseFreightAsc(String souceLoc,String destLoc,String truckType);

  List<Freight> findBySourceLocAndDestLocAndTruckTypeOrderByBaseFreightAsc(String souceLoc,String destLoc,String truckType);


  @Query(value = "select ft from Freight ft where ft.sourceLoc = ?1 and ft.destLoc = ?2 and ft.servprov = ?3 and ft.truckType = ?4 " +
      "and ((?5 >= ft.effectiveDate and ft.expiryDate is null) OR (?5 >= ft.effectiveDate and ?5 <= ft.expiryDate)) order by ft.effectiveDate desc")
  List<Freight> findAllBySourceAndDestAndServProvAndTruckTypeBetweenEffectiveAndExpiryDate(String source, String destination ,String servprov ,String truckType, Date currentDate);

  @Query(value = "select ft from Freight ft where ft.sourceLoc = ?1 and ft.destLoc = ?2 and ft.servprov = ?3 " +
      "and ((?4 >= ft.effectiveDate and ft.expiryDate is null) OR (?4 >= ft.effectiveDate and ?4 <= ft.expiryDate)) order by ft.effectiveDate desc")
  List<Freight> findAllBySourceAndDestAndServProvBetweenEffectiveAndExpiryDate(String source, String destination ,String servprov , Date currentDate);

  @Query(value = "select ft from Freight ft where ft.sourceLoc = ?1  and ft.servprov = ?2 " +
      "and ((?3 >= ft.effectiveDate and ft.expiryDate is null) OR (?3 >= ft.effectiveDate and ?3 <= ft.expiryDate)) order by ft.effectiveDate desc")
  List<Freight> findAllBySourceAndServProvBetweenEffectiveAndExpiryDate(String source, String servprov , Date currentDate);


  @Query(nativeQuery = true, value = "select ft.* from freight ft where servprov= ?3 and source_loc= ?1 and dest_loc= ?2 and truck_type= ?4 ")
  List<Freight> findTranspoterSAPCodeSourceAndDestAndServProvAndTruckType(String source, String destination, String servprov, String truckType);

//  List<Freight> findByIdIn(List<Double> freightIdList);

/*  @Query(nativeQuery = true, value = "select transporter_sap_code from freight where servprov= ?1 and source_loc= ?2 and dest_loc= ?3  and rownum=1")
  String findTrasnporterSapCodeWithSourceDestServepro(String servprov, String source, String destination);

  @Query(nativeQuery = true, value = "select transporter_sap_code from freight where servprov= ?1 and source_loc= ?2 and rownum=1")
  String findTrasnporterSapCodeWithSourceServpro(String servprov, String source);*/


}