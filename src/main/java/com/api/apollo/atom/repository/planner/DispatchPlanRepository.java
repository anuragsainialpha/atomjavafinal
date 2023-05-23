package com.api.apollo.atom.repository.planner;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.api.apollo.atom.constant.Constants.Status;
import com.api.apollo.atom.entity.plan.DispatchPlan;

public interface DispatchPlanRepository extends CrudRepository<DispatchPlan, String> {

	boolean existsByIdAndInsertUser(Long id, String userId);

	boolean existsById(Long id);

	Page<DispatchPlan> findByInsertUserAndStatus(String userId, Status status,Pageable pageable);

	/*For DP_REP displaying error record from all the locations*/
	Page<DispatchPlan> findByStatus(Status status,Pageable pageable);
	
	Optional<DispatchPlan> findOneById(Long id);
	
	@Modifying
	@Transactional
	@Query("delete from DispatchPlan d where id = ?1")
	void deleteById(Long id);

/*	@Query(value = "select * from table(atl_business_flow_pkg.get_market_segment(?1,?2))",nativeQuery = true)*/
	@Query(value = "select atl_business_flow_pkg.get_market_segment (?1,?2) from dual",nativeQuery = true)
	String getMarketSegmentProcedure(String sourceLoc, String destLoc);

}
