package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.master.MTBatchCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BatchCodeRepository extends JpaRepository<MTBatchCode, Integer> {
  @Query("SELECT B.code from MTBatchCode B")
  List<String> findAllBatchCodes();

  @Query("SELECT B.code from MTBatchCode B where LOWER(B.code) LIKE lower(concat(:code,'%'))")
  Page<String> findAllBatchCodesLike(@Param("code") String code, Pageable pageable);

  boolean existsByCode(String batchCode);

  boolean existsByCodeAndPlantCode(String batchCode,String plantCode);

  Optional<MTBatchCode> findByCodeIgnoreCase(String batchCode);

  @Query("SELECT DISTINCT B.code from MTBatchCode B ORDER BY B.code ASC")
  List<String> findDistinctBatchCodesByOrderByCodeAsc();

  boolean existsByCodeAndCategory(String tubeBatch, String category);

  List<MTBatchCode> findAllByCodeIn(List<String> batchCodeList);

}
