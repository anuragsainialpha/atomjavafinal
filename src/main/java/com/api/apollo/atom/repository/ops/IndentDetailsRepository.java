package com.api.apollo.atom.repository.ops;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MTTruck;
import com.api.apollo.atom.entity.ops.IndentDetails;
import com.api.apollo.atom.entity.ops.IndentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IndentDetailsRepository extends JpaRepository<IndentDetails, IndentId> {

	@Query(nativeQuery = true, value = "select max(line_num) from indent_detail where indent_id = ?1")
	Integer findLineNumberByIndentId(String indentId);

	Optional<IndentDetails> findOneByIndentSummaryIndentIdAndGateControlCode(String indentId,String gateControlCode);

	List<IndentDetails> findByIndentSummaryIndentIdIn(List<String> indentIds);

	List<IndentDetails> findByIndentIndentIdIn(List<String> indentId);

	Optional<IndentDetails> findByIndentSummaryIndentIdAndTruckTruckNumber(String indentId,String truckNumber);

	Optional<IndentDetails> findByIndentSummaryIndentIdAndTruckTruckNumberAndGateControlCodeAndStatusNot(String indentId, String truckNumber,String gateControlCode, Constants.TruckReportStatus status);

	boolean existsByTruck(MTTruck truck);

	/*Optimizing getIndents API*/
	@Query(nativeQuery = true, value = "SELECT ins.indent_id   AS indentId, " +
			"       ind.line_num          AS lineNum, " +
			"       mt.truck_number       AS truckNumber, " +
			"       ind.truck_type        AS truckType, " +
			"       ind.actual_truck_type AS actualTruckType, " +
			"       ind.variant1          AS variant1, " +
			"       ind.tte_capacity      AS tteCapacity, " +
			"       ind.driver_name       AS driverName, " +
			"       ind.driver_mobile     AS driverMobile, " +
			"       ind.driver_license    AS driverLicense, " +
			"       ind.passing_weight    AS passingWeight, " +
			"       ind.gps_enabled       AS gpsEnabled, " +
			"       ind.gps_provider      AS gpsProvider, " +
			"       ind.gate_control_code AS gateControlCode, " +
			"       ind.status            AS status, " +
			"       ind.insert_date       AS insertDate, " +
			"       ind.insert_user       AS insertUser, " +
			"       ind.update_date       AS updateDate, " +
			"       ind.update_user       AS updateUser, " +
			"       ind.container_num     AS containerNumber " +
			" FROM   indent_detail ind " +
			"       LEFT JOIN indent_summary ins " +
			"              ON ins.indent_id = ind.indent_id " +
			"       LEFT JOIN mt_truck mt " +
			"              ON mt.truck_number = ind.truck_number " +
			" where ins.indent_id IN (?1) ")
	List<Map<String, Object>> findAllIndentDetailsByIndentIdIn(List<String> indentIdList);

}
