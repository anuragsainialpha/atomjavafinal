package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.master.MtScac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MTScasRepository extends JpaRepository<MtScac, Double> {

  @Query(value = "select distinct scac from MtScac order by scac ")
  List<String> getTransporterList();
}
