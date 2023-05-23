package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.master.MTTruckDedicated;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface MTTruckDedidatedRepository extends JpaRepository<MTTruckDedicated, Long> {

  boolean existsByTruckNumberAndSourceLocAndDestLocAndServProvAndTruckType(String truckNumber, String sourceLoc, String destLoc, String servPro, String truckType);

  @Query(value = "select mt from MTTruckDedicated mt where mt.sourceLoc = ?1 and mt.destLoc = ?2 and mt.servProv = ?3 and mt.truckType = ?4 and  mt.truckNumber = ?5  and" +
      " ?6 <= mt.expiryDate order by mt.expiryDate desc ")
  MTTruckDedicated findByEffectiveDateAndExpiryDate(String sourceLoc, String destLoc, String servPro, String truckType, String truckNumber, Date currentDate);


  @Query(nativeQuery = true, value = "insert into MT_Truck_Dedicated (SERPVORV, source_Loc, dest_Loc, source_Desc, dest_Desc, truck_Type, truck_Number, expiry_Date, insert_User) values " +
      "(:servProv, :sourceLoc, :destLoc, :sourceDesc, :destDesc, " +
      ":truckType, :truckNumber, TO_DATE(:expiryDate, 'dd-MM-yyyy'), :insertUser) ")
  void saveMTTruckDedicated(@Param("servProv") String servProv, @Param("sourceLoc") String sourceLoc, @Param("destLoc") String destLoc,
                                        @Param("sourceDesc") String sourceDesc, @Param("destDesc") String destDesc,
                                        @Param("truckType") String truckType, @Param("truckNumber") String truckNumber,
                                        @Param("expiryDate") String expiryDate, @Param("insertUser") String insertUser);


}
