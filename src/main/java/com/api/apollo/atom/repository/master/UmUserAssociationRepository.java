package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.UmUserAssociation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UmUserAssociationRepository extends JpaRepository<UmUserAssociation, String> {

  @Query(value = "SELECT USER_ID FROM UM_USER_ASSOCIATION WHERE ASSOCIATION_IDENTIFIER = 'SERVPROV' AND ASSOCIATION_VALUE = ?1", nativeQuery = true)
  List<String> findUserByTrasporter(String transporter);

}
