package com.api.apollo.atom.repository;

import com.api.apollo.atom.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<ApplicationUser, String> {

  //@Query("select new com.api.apollo.atom.entity.ApplicationUser(a.userId,a.password,a.role,a.firstName,a.lastName,a.email) from ApplicationUser a where LOWER(a.userId) = LOWER(:userId)")
  Optional<ApplicationUser> findOneByUserIdIgnoreCase(String userId);

  @Query("select new com.api.apollo.atom.entity.ApplicationUser(a.userId) from ApplicationUser a where a.plantCode =:plantCode")
  List<ApplicationUser> findUserIdByPlantCode(@Param("plantCode") String plantCode);

  @Query(nativeQuery = true,value = "select association_value from um_user_association a, um_user b where a.user_id = b.user_id and a.association_identifier='SERVPROV' and b.user_id =:userID")
  Object findTransporterByUser(@Param("userID") String userID);

  @Query( "select user.email from ApplicationUser user where userId in :userIds" )
  List<String> findemilsByuserid(@Param("userIds") List<String> userIds);

  @Query("select user.email from ApplicationUser user where plantCode =:userIds and role ='PLT_PLN'")
  List<String> findEmailToCC(@Param("userIds") String userIds);

}
