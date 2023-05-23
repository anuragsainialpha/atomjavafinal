package com.api.apollo.atom.repository.ops;

import com.api.apollo.atom.entity.ops.LoadslipDetailBom;
import com.api.apollo.atom.entity.ops.LoadslipDetailBomId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface LoadslipDetailBomRepository extends JpaRepository<LoadslipDetailBom, LoadslipDetailBomId> {

  @Transactional
  @Modifying
  @Query(nativeQuery = true, value = " delete from loadslip_detail_bom where loadslip_id = ?1")
  void deleteLoadslipDetailBomByLoadslipId(String loadslipId);

  List<LoadslipDetailBom> findAllByLoadslipDetailBomIdLoadslipId(String loadslipId);
}
