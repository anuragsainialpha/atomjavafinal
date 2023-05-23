package com.api.apollo.atom.repository.planner;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.api.apollo.atom.constant.Constants.ErrorCode;
import com.api.apollo.atom.entity.plan.DispatchPlanItemTempInfo;

public interface DispatchPlanItemTempInfoRepository extends JpaRepository<DispatchPlanItemTempInfo, Long> {

	// Removed pagination and showing all records
/*	@Query("select new com.api.apollo.atom.entity.plan.DispatchPlanItemTempInfo(a.id,a.planId,a.dispatchDate,a.sourceLocation,"
			+ "a.destinationLocation,a.itemId,a.itemDescription,a.batchCode,a.quantity,a.priority,a.marketSegment,a.insertUser,a.insertDate,a.lineNumber,a.category,a.tte) "
			+ "from DispatchPlanItemTempInfo a,DispatchPlanItemErrorInfo b where a.planId = b.planId and a.lineNumber = b.lineNumber and a.planId =:planId and b.code=:errorCode")
	List<DispatchPlanItemTempInfo> findByPlanIdAndErrorCode(@Param("planId") Long planId,
			@Param("errorCode") ErrorCode errorCode, Pageable pageable);*/

	@Query("select new com.api.apollo.atom.entity.plan.DispatchPlanItemTempInfo(a.id,a.planId,a.dispatchDate,a.sourceLocation,"
			+ "a.destinationLocation,a.itemId,a.itemDescription,a.batchCode,a.quantity,a.priority,a.marketSegment,a.insertUser,a.insertDate,a.lineNumber,a.category,a.tte) "
			+ "from DispatchPlanItemTempInfo a,DispatchPlanItemErrorInfo b where a.planId = b.planId and a.lineNumber = b.lineNumber and a.planId =:planId and b.code=:errorCode")
	List<DispatchPlanItemTempInfo> findByPlanIdAndErrorCode(@Param("planId") Long planId,
																													@Param("errorCode") ErrorCode errorCode);

	@Query(value = "select * from (atl_business_flow_pkg.get_disp_plan_summary(?1))", nativeQuery = true)
	Object findDispatchPlanResultCount(Long planId);

	@Query("select new com.api.apollo.atom.entity.plan.DispatchPlanItemTempInfo(a.id,a.planId,a.lineNumber,a.quantity) from DispatchPlanItemTempInfo a where a.id =:id")
	DispatchPlanItemTempInfo findPlanIdAndLineNumberByPlanItemId(@Param("id") Long id);

	@Modifying
	@Transactional
	@Query("delete from DispatchPlanItemTempInfo t where id = ?1")
	void deleteById(Long id);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update DispatchPlanItemTempInfo planInfo set planInfo.sourceLocation =:sourceLocation,planInfo.destinationLocation =:destinationLocation,planInfo.destinationDescription =:destinationDescription where planInfo.id =:id")
	void updateBySourceAndDestinationCode(@Param("sourceLocation") String sourceLocation,
			@Param("destinationLocation") String destinationLocation,@Param("destinationDescription") String destinationDescription, @Param("id") Long id);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update DispatchPlanItemTempInfo planInfo set planInfo.itemId =:itemId,planInfo.tte=:tte,planInfo.category=:category,planInfo.itemDescription=:itemDescription where planInfo.id =:id")
	void updateByMaterialCodeAndTteAndCategory(@Param("itemId") String itemId,@Param("tte") Double tte,@Param("category") String category, @Param("itemDescription")
			String itemDescription, @Param("id") Long id);
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update DispatchPlanItemTempInfo planInfo set planInfo.batchCode =:batchCode where planInfo.id =:id")
	void updateByBatchCode(@Param("batchCode") String batchCode, @Param("id") Long id);
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update DispatchPlanItemTempInfo planInfo set planInfo.tte =:tte where planInfo.id =:id")
	void updateByTte(@Param("tte") Double tte, @Param("id") Long id);
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update DispatchPlanItemTempInfo planInfo set planInfo.category =:category where planInfo.id =:id")
	void updateByCategory(@Param("category") String category, @Param("id") Long id);
	
	Long countByPlanId(Long planId);

	List<DispatchPlanItemTempInfo> findAllByPlanId(Long planId);

}