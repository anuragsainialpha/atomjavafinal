package com.api.apollo.atom.dto.planner;

import java.util.ArrayList;
import java.util.List;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.Constants.Status;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.plan.DispatchPlan;
import com.api.apollo.atom.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class DispatchPlanDto {

	private Long planId;
	private Integer totalQuantity;
	private Status status;
	private String createdDate;

	public List<PlanUploadDto> planItems = new ArrayList<>();

	public DispatchPlanDto() {

	}

	public DispatchPlanDto(DispatchPlan dispatchPlan) {
		this.planId = dispatchPlan.getId();
		this.totalQuantity = dispatchPlan.getTotalQuantity();
		this.status = dispatchPlan.getStatus();
		this.createdDate = DateUtils.formatDate(dispatchPlan.getInsertDate(), Constants.PLAN_RECORD_DATE_FORMAT);
	}

	public DispatchPlan toDispatchPlan(ApplicationUser loggedInUser) {
		DispatchPlan dispatchPlan = new DispatchPlan();
		dispatchPlan.setTotalQuantity(
				this.planItems.parallelStream().mapToInt(item -> Integer.parseInt(item.quantity)).sum());
		// dispatchPlan.setStatus("PENDING_APPROVAL");
		// dispatchPlan.setInsertUser(loggedInUser);
		return dispatchPlan;
	}

}
