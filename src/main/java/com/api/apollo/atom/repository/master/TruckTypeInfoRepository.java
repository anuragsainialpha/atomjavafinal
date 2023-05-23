package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.master.MTTruckTypeInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TruckTypeInfoRepository extends JpaRepository<MTTruckTypeInfo, String> {

  @Query("select new com.api.apollo.atom.entity.master.MTTruckTypeInfo(T.type,T.tteCapacity) from MTTruckTypeInfo T where LOWER(T.type) LIKE lower(concat(:type,'%'))")
  Page<MTTruckTypeInfo> findAllTruckTypesLike(@Param("type") String type, Pageable pageable);

  @Query("select new com.api.apollo.atom.entity.master.MTTruckTypeInfo(T.truckDescription,T.type) from MTTruckTypeInfo T where LOWER(T.truckDescription) LIKE lower(concat(:type,'%'))")
  Page<MTTruckTypeInfo> findAllTruckTypAndDescLike(@Param("type") String truckDescription, Pageable pageable);

  @Query("select new com.api.apollo.atom.entity.master.MTTruckTypeInfo(T.type,T.tteCapacity,T.loadFactor) from MTTruckTypeInfo T where T.type in :truckTypes")
  List<MTTruckTypeInfo> findTruckTypeAndTteAndLoadByTypeIn(@Param("truckTypes") List<String> truckTypes);

  List<MTTruckTypeInfo>  findOneByType(String truckType);

  Optional<MTTruckTypeInfo> findOneByTypeAndVariant1(String trucktype, String variant1);

  @Query(nativeQuery = true, value = "select * from table(atl_business_flow_pkg.get_truck_type_details(?1, ?2)) ")
  Map<String, Object> findMTTruckTypeInfoByTypeAndVariant(String truckType, String variant1);

  @Query("select distinct new com.api.apollo.atom.entity.master.MTTruckTypeInfo(T.type,T.tteCapacity)  from MTTruckTypeInfo T WHERE T.variant1 IS NULL ORDER BY T.type ASC")
  List<MTTruckTypeInfo> findDistinctTruckTypesByOrderByTruckTypeAsc();

  /*Truck Types specific to the source ANd destination*/
  @Query( value = "select distinct T.truck_type as TRUCKTYPE  from FREIGHT T  WHERE T.source_loc = ?1 AND T.dest_loc = ?2 " +
      " AND ((?3 >= T.effective_date and T.expiry_date is null) OR (?3 >= T.effective_date and ?3 <= T.expiry_date))" +
      " ORDER BY T.truck_type ASC", nativeQuery = true)
  List<Map<String, Object>> findDistinctTruckTypesBySourceAndDestByOrderByTruckTypeAsc(String sourceLoc, String destLoc, Date date);

  @Query("Select distinct new com.api.apollo.atom.entity.master.MTTruckTypeInfo(T.variant1,T.variant2) from MTTruckTypeInfo T")
  List<MTTruckTypeInfo> getVariants();

  Optional<MTTruckTypeInfo> findOneByTtId(Integer ttId);

  boolean existsByType(String truckType);

  MTTruckTypeInfo findByType(String truckType);

}
