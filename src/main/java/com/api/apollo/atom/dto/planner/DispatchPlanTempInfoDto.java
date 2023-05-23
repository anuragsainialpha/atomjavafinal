package com.api.apollo.atom.dto.planner;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.plan.DispatchPlanItemTempInfo;
import com.api.apollo.atom.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DispatchPlanTempInfoDto {

	private Long id;

	private Long planId;

	private String dispatchDate;

	private String sourceLocation;

	private String destinationLocation;

	private String materialCode;

	private String materialDescription;

	private String batchCode;

	private Integer quantity;

	private Integer priority;

	private String marketSegment;

	private Integer lineNumber;

	private String category;
	
	private Double tte;

	public DispatchPlanTempInfoDto() {
	}

	public DispatchPlanTempInfoDto(DispatchPlanItemTempInfo planItemInfo) {
		this.id = planItemInfo.getId();
		this.planId = planItemInfo.getPlanId();
		this.dispatchDate = DateUtils.formatDate(planItemInfo.getDispatchDate(), Constants.PLAN_RECORD_DATE_FORMAT);
		this.sourceLocation = planItemInfo.getSourceLocation();
		this.destinationLocation = planItemInfo.getDestinationLocation();
		this.materialCode = planItemInfo.getItemId();
		this.materialDescription = planItemInfo.getItemDescription();
		this.batchCode = planItemInfo.getBatchCode();
		this.quantity = planItemInfo.getQuantity();
		this.priority = planItemInfo.getPriority();
		this.marketSegment = planItemInfo.getMarketSegment();
		this.lineNumber = planItemInfo.getLineNumber();
		this.category = planItemInfo.getCategory();
		this.tte = planItemInfo.getTte();
		
	}

}
