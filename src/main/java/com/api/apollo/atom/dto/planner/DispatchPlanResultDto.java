package com.api.apollo.atom.dto.planner;

import javax.persistence.StoredProcedureQuery;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.Constants.CodesInfo;
import com.api.apollo.atom.constant.Constants.UploadPlanStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DispatchPlanResultDto {

	private Integer totalRecords;
	private Integer totalErrorRecords;
	private Integer totalTyreCount;
	private Integer c1Count;
	private Integer c2Count;
	private Integer c3Count;
	private Integer c4Count;
	private Integer c5Count;
	private Integer c6Count;
	private Long planId;
	private String message;
	private UploadPlanStatus status;
	private CodesInfo codesInfo;

	public DispatchPlanResultDto() {
	}

	public DispatchPlanResultDto(StoredProcedureQuery storedProcedure) {
		this.totalRecords = storedProcedure.getOutputParameterValue("p_tot_records") != null
				? (Integer) storedProcedure.getOutputParameterValue("p_tot_records")
				: null;
		this.totalErrorRecords = storedProcedure.getOutputParameterValue("p_tot_error_records") != null
				? (Integer) storedProcedure.getOutputParameterValue("p_tot_error_records")
				: null;
		this.totalTyreCount = storedProcedure.getOutputParameterValue("p_total_tyre_count") != null
				? (Integer) storedProcedure.getOutputParameterValue("p_total_tyre_count")
				: null;
		this.c1Count = storedProcedure.getOutputParameterValue("p_c1_count") != null
				? (Integer) storedProcedure.getOutputParameterValue("p_c1_count")
				: null;
		this.c2Count = storedProcedure.getOutputParameterValue("p_c2_count") != null
				? (Integer) storedProcedure.getOutputParameterValue("p_c2_count")
				: null;
		this.c3Count = storedProcedure.getOutputParameterValue("p_c3_count") != null
				? (Integer) storedProcedure.getOutputParameterValue("p_c3_count")
				: null;
		this.c4Count = storedProcedure.getOutputParameterValue("p_c4_count") != null
				? (Integer) storedProcedure.getOutputParameterValue("p_c4_count")
				: null;
		this.c5Count = storedProcedure.getOutputParameterValue("p_c5_count") != null
				? (Integer) storedProcedure.getOutputParameterValue("p_c5_count")
				: null;
		this.c6Count = storedProcedure.getOutputParameterValue("p_c6_count") != null
				? (Integer) storedProcedure.getOutputParameterValue("p_c6_count")
				: null;

		this.planId = storedProcedure.getOutputParameterValue("p_plan_id") != null
				? (Long) storedProcedure.getOutputParameterValue("p_plan_id")
				: null;
		
		if (this.totalErrorRecords > 0) {
			this.status = Constants.UploadPlanStatus.PLAN_PENDING;
			this.message = "Plan ID : " + this.planId + " created with errors";
		} else {
			this.status = Constants.UploadPlanStatus.PLAN_SUCCESS;
			this.message = "Plan ID : " + this.planId + " created successfully";
		}
		this.codesInfo = new Constants().new CodesInfo();
	}

	public DispatchPlanResultDto(Integer totalRecords, Integer totalErrorRecords, Integer totalTyreCount,
			Integer c1Count, Integer c2Count, Integer c3Count, Integer c4Count, Integer c5Count, Integer c6Count,String status,
			Long planId) {
		super();
		this.totalRecords = totalRecords;
		this.totalErrorRecords = totalErrorRecords;
		this.totalTyreCount = totalTyreCount;
		this.c1Count = c1Count;
		this.c2Count = c2Count;
		this.c3Count = c3Count;
		this.c4Count = c4Count;
		this.c5Count = c5Count;
		this.c6Count = c6Count;
		this.planId = planId;
		this.status = Constants.UploadPlanStatus.valueOf(status);
		if (this.totalErrorRecords > 0) {
			this.message = "Plan ID : " + this.planId + " created with errors";
		} else {
			this.message = "Plan ID : " + this.planId + " created successfully";
		}
		this.codesInfo = new Constants().new CodesInfo();
	}

}
