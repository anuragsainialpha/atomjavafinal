package com.api.apollo.atom.repository.planner;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.api.apollo.atom.constant.Constants.ErrorCode;
import com.api.apollo.atom.entity.plan.DispatchPlanItemErrorInfo;

public interface DispatchPlanItemErrorInfoRepository extends CrudRepository<DispatchPlanItemErrorInfo, Long> {
	
	DispatchPlanItemErrorInfo findByPlanIdAndCodeAndLineNumber(Long planId,ErrorCode code,Integer lineNumber);
	
	List<DispatchPlanItemErrorInfo> findByPlanIdAndLineNumber(Long planId,Integer lineNumber);
	
	List<DispatchPlanItemErrorInfo> findByPlanIdAndLineNumberAndCodeNot(Long planId,Integer lineNumber,ErrorCode code);
	
	//@Transactional
	//Long deleteByPlanIdAndCodeAndLineNumber(Long planId,String code,Integer lineNumber);

	boolean existsByPlanIdAndLineNumber(Long planId,Integer lineNumber);

	boolean existsByPlanId(Long planId);

	boolean existsByPlanIdAndLineNumberAndCode(Long planId, Integer lineNumber , ErrorCode errorCode);
	
	@Modifying
	@Transactional
	@Query("delete from DispatchPlanItemErrorInfo e where  planId = ?1 and code in ?2 and lineNumber = ?3")
	void deleteByPlanIdAndCodesInAndLineNumber(Long planId, List<ErrorCode> codes, Integer lineNumber);
	
	@Modifying
	@Transactional
	@Query("delete from DispatchPlanItemErrorInfo e where  planId = ?1 and lineNumber = ?2")
	void deleteByPlanIdAndLineNumber(Long planId,Integer lineNumber);
	
	@Query("SELECT COUNT(e) FROM DispatchPlanItemErrorInfo e WHERE e.planId =?1")
	Long findCountByPlanId(Long id);
	
	
	@Query("SELECT COUNT(e) FROM DispatchPlanItemErrorInfo e where  e.planId = ?1 and e.code = ?2 and e.lineNumber = ?3")
    Long findCountByPlanIdAndCodeAndLineNumber(Long planId,ErrorCode code,Integer lineNumber);

}
