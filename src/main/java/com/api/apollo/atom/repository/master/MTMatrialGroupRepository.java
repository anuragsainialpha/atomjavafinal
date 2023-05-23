package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.master.MTMatrialGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MTMatrialGroupRepository extends JpaRepository<MTMatrialGroup,String> {

  @Query("SELECT distinct M.scmGroup FROM MTMatrialGroup M where M.scmGroup is not null order by  M.scmGroup")
  List<String> findDistinctMTMatrialGroupByOrderByIdAsc();

  @Query("SELECT distinct M.scmGroup FROM MTMatrialGroup M where M.scmGroup in ?1")
  List<String> findDistinctMTMatrialGroupByOrderByIdAsc(List<String> scmGroup);
}
