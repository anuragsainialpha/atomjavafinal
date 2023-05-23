package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.constant.LocationType;
import com.api.apollo.atom.entity.master.MTLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LocationRepository extends PagingAndSortingRepository<MTLocation, String> {

  @Query("SELECT l.id from MTLocation l")
  List<String> findAllLocationIds();

  @Query("SELECT new com.api.apollo.atom.entity.master.MTLocation(l.id,l.description) " +
      "FROM   MTLocation l " +
      "WHERE  LOWER(l.id) LIKE LOWER(concat(:id,'%')) " +
      "AND    l.id != :sourceId")
  Page<MTLocation> findAllLocationsLike(@Param("id") String id, @Param("sourceId") String sourceId, Pageable pageable);

  /*@Query(nativeQuery = true, value = "(SELECT l.location_id as value, l.location_desc as description FROM mt_location l WHERE  LOWER(l.location_id) LIKE LOWER(concat('%',concat(?1,'%'))) AND l.location_id != ?2) " +
      " UNION " +
      "(SELECT c.cust_id, c.cust_name FROM mt_customer c WHERE  LOWER(c.cust_id) LIKE LOWER(concat('%',concat(?1,'%'))) AND c.cust_id != ?2 )")*/
  @Query(nativeQuery = true,value = "(select base.value, base.description from (select mt.location_id as value, mt.location_desc as description from mt_location mt " +
      "UNION ALL " +
      "select c.cust_id as value, c.cust_name as description from mt_customer c)  base where LOWER(base.value) LIKE LOWER(concat('%',concat(?1,'%'))) AND base.value != ?2)")
  List<Object[]> findAllDestLocationsLike(String id, String sourceId, Pageable pageable);

  //where (mt.location_type != 'PORT' OR (mt.location_type = 'PORT' AND mt.location_id LIKE 'IN%') )
//  For Indents when dest is searched showing only INDIAN PORTS
  @Query(nativeQuery = true,value = "(select base.value, base.description from (select mt.location_id as value, mt.location_desc as description from mt_location mt where (mt.location_type != 'PORT' OR (mt.location_type = 'PORT' AND mt.location_id LIKE 'IN%')) " +
      "UNION ALL " +
      "select c.cust_id as value, c.cust_name as description from mt_customer c)  base where LOWER(base.value) LIKE LOWER(concat('%',concat(?1,'%'))) AND base.value != ?2)")
  List<Object[]> findAllDestLocationsLikeForIndent(String id, String sourceId, Pageable pageable);
  @Query(nativeQuery = true,value = "select  base.value,base.description  from (select mt.location_id as value , mt.location_desc as description   from mt_location mt  " +
      "UNION ALL  " +
      "select  c.cust_id as value, c.cust_name as description  from mt_customer c)  base where LOWER(base.description) LIKE LOWER(concat('%',concat(?1,'%')))")
  List<Object[]> findAllDestLocationsDescriptionLike(String description, Pageable pageable);

  @Query(nativeQuery = true,value = "select  base.value,base.description  from (select mt.location_id as value , mt.location_desc as description   from mt_location mt  " +
      "UNION ALL  " +
      "select  c.cust_id as value, c.cust_name as description  from mt_customer c)  base where base.value != ?2 AND LOWER(base.description) LIKE LOWER(concat('%',concat(?1,'%')))")
  List<Object[]> findAllDestLocationsDescriptionLikeOtherThanLoggedInLoc(String description, String loggedInSource ,Pageable pageable);
  /*For DP_REP user we are displaying destination location including current plant_loc*/
  @Query(nativeQuery = true,value = "(select base.value, base.description from (select mt.location_id as value, mt.location_desc as description from mt_location mt " +
      "UNION ALL " +
      "select c.cust_id as value, c.cust_name as description from mt_customer c)  base where LOWER(base.value) LIKE LOWER(concat('%',concat(?1,'%'))))")
  List<Object[]> findAllDestLocationsLikeDPREP(String id,  Pageable pageable);

//  For Indent selecting the port location whose country is IN
  @Query(nativeQuery = true,value = "select  base.value,base.description  from (select mt.location_id as value , mt.location_desc as description   from mt_location mt where (mt.location_type != 'PORT' OR (mt.location_type = 'PORT' AND mt.location_id LIKE 'IN%')) " +
      "UNION ALL  " +
      "select  c.cust_id as value, c.cust_name as description  from mt_customer c)  base where base.value != ?2 AND LOWER(base.description) LIKE LOWER(concat('%',concat(?1,'%')))")
  List<Object[]> findAllDestLocationsDescriptionLikeForIndent(String description, String loggedInSource ,Pageable pageable);

  /*Searching the MT_CUSTOMER locations for new screen MT-CUSTOMER in ADMIN */
  @Query(nativeQuery = true,value = "(select base.value, base.description from (select c.cust_id as value, c.cust_name as description from mt_customer c)  " +
      " base where LOWER(base.value) LIKE LOWER(concat('%',concat(?1,'%'))))")
  List<Object[]> findAllCustLocationsLike(String id,  Pageable pageable);

/*  @Query(nativeQuery = true, value = "(SELECT l.location_id as value, l.location_desc as description FROM mt_location l WHERE  LOWER(l.location_id) LIKE LOWER(concat('%',concat(?1,'%')))) " +
      " UNION " +
      "(SELECT c.cust_id, c.cust_name FROM mt_customer c WHERE  LOWER(c.cust_id) LIKE LOWER(concat('%',concat(?1,'%'))))")*/
  @Query(nativeQuery = true , value = "(select base.value, base.description from (select mt.location_id as value, mt.location_desc as description from mt_location mt " +
      " UNION ALL " +
      " select c.cust_id as value, c.cust_name as description from mt_customer c)  base where LOWER(base.value) LIKE LOWER(concat('%',concat(?1,'%'))))")
  List<Object[]> getDestinationForTrasnsporter(String id, Pageable pageable);

  //boolean existsById(String locationId);

  @Query("SELECT new com.api.apollo.atom.entity.master.MTLocation(l.id,l.description) FROM   MTLocation l WHERE  LOWER(l.id) = LOWER(:id)")
  MTLocation findDescById(@Param("id") String id);

  @Query(nativeQuery = true, value = "(SELECT l.location_id as destLoc FROM mt_location l WHERE l.location_id in ?1) " +
      " UNION " +
      "(SELECT c.cust_id FROM mt_customer c WHERE c.cust_id in ?1)")
  List<String> findByIdIn(List<String> destinations);

  LocationType findTypeById(String source);

  @Query(nativeQuery = true, value = "SELECT l.LOCATION_ID FROM MT_LOCATION l WHERE  l.LOCATION_ID != ?1")
  List<String> findAllOtherPlantLocations(String plantCode);

  //using same query to get gst and pan number
  @Query(nativeQuery = true,value = " SELECT l.location_id as destLoc, l.LOCATION_DESC as destDesc, l.pan_no as panNum, l.gst_no as gstNum FROM mt_location l WHERE l.location_id in ?1" +
      "  UNION" +
      "  SELECT c.cust_id , c.CUST_NAME, c.pan_no , c.gst_no FROM mt_customer c WHERE c.cust_id in ?1")
 List<Map<String,String>> findDestDescWtihDestinations(List<String> destinations);

  @Query("SELECT new com.api.apollo.atom.entity.master.MTLocation(l.id,l.gstNum,l.panNum) FROM   MTLocation l WHERE l.id = ?1")
  Optional<MTLocation> findGstNumPanNumByLocation(@Param("id") String id);

  @Query(nativeQuery = true,value = "(select mt.location_id as value, mt.location_desc as description from mt_location mt " +
      "where LOWER(mt.location_id) LIKE LOWER(concat('%',concat(?1,'%'))) and mt.location_type = 'PORT')")
  List<Object[]> findAllDestLocationsWithTypePORT(String id, Pageable pageable);

//  boolean existsByIdAndType(String pod, LocationType port);

  /*This created beacause Akshay has changed the locationType from ENUM to String on 07/06/2019*/
  boolean existsByIdAndType(String pod, String port);

  @Query(nativeQuery = true,value = "select * from mt_location mt where mt.location_id = ?1")
  Optional<MTLocation> findByLocationId(String locationId);

  @Query(nativeQuery = true,value = "select * from mt_location mt where mt.location_type = ?1 ")
  List<MTLocation> findAllLocationWithType(String locationType);

  @Query(nativeQuery = true,value = "select mt.location_id from mt_location mt where mt.linked_plant = ?1 ")
  List<String> findIdByLinkedPlant(String plantCode);


//  List<MTLocation> findAllByIdIn(List<String> actualSourceList);
  @Query(value = "SELECT mt FROM MTLocation mt")
  List<MTLocation> findAll();

  List<MTLocation> findAllByIdIn(List<String> LocationIdList);

  @Query(nativeQuery = true, value = "SELECT " +
      " CASE WHEN (SELECT location_desc FROM   mt_location WHERE  location_id = ?1) IS NOT NULL " +
      "           THEN (SELECT location_desc FROM   mt_location WHERE location_id = ?1) " +
      " ELSE (SELECT cust_name FROM   mt_customer WHERE  cust_id = ?1) " +
      " END AS description " +
      "FROM   dual")
  String findDescriptionByLocId(String locationId);

  @Query(nativeQuery = true, value = "SELECT  CASE WHEN (SELECT CITY FROM   mt_location WHERE  location_id =?1 ) IS NOT NULL  THEN (SELECT city FROM   mt_location WHERE location_id = ?1) " +
      "ELSE (SELECT CITY FROM   mt_customer WHERE  cust_id = ?1)        END AS city   FROM   dual ")
  String findCityByLocId(String locationId);

  @Query(nativeQuery = true, value = "select DISTINCT cust_name from  mt_customer where cust_name is NOT null AND LOWER(cust_name) LIKE LOWER(concat(?1,'%')) ORDER BY cust_name ")
  List<String> findAllCustNameLike(String custName, Pageable pageable);

  @Query(nativeQuery = true, value = "select DISTINCT city from  mt_customer where city is not null AND LOWER(city) LIKE LOWER(concat(?1,'%')) ORDER BY city ")
  List<String> findAllCityLike(String city, Pageable pageable);

  @Query(nativeQuery = true, value = "select DISTINCT state from  mt_customer where (state is not null AND LOWER(state) LIKE LOWER(concat(?1,'%'))) ORDER BY state ")
  List<String> findAllStateLike(String state, Pageable pageable);

  @Query(nativeQuery = true, value = "select DISTINCT cust_acct_grp from  mt_customer where cust_acct_grp is not null AND LOWER(cust_acct_grp) LIKE LOWER(concat(?1,'%')) ORDER BY cust_acct_grp ")
  List<String> findAllCustAcctGrpLike(String state, Pageable pageable);

}
