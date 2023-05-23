package com.api.apollo.atom.dto.planner;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.UserRole;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.master.MTTruckTypeInfo;
import com.api.apollo.atom.entity.plan.DispatchPlanItemInfo;
import com.api.apollo.atom.util.DateUtils;
import com.api.apollo.atom.util.Utility;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Setter
@Getter
public class DispatchPlanItemDto {

	private Long id;

	private Long planId;

	private Integer lineNumber;

	private String dispatchDate;

	private String sourceLocation;

	private String destinationLocation;

	private String itemId;

	private String itemDescription;

	private String category;

	private Double tte;

	private String batchCode;

	private Integer quantity;

	private Integer priority;

	private String status;
	
	private String appStatusValue;

	private String appStatus;

	private String marketSegment;

	private Integer approvedQuantity;

	private Integer approvalQuantity;

	private Integer unapprovalQuantity;
	
	private Integer unapprovedQuantity;

	private Integer deletedApprQuantity;
	
	private Integer deletedUnApprQuantity;
	
	private Integer deleteApprQuantity;
	
	private Integer deleteUnApprQuantity;

	private Integer quantityToBeDelete;

	private Integer availableQuantity;
	
	private Integer totalAvailableQuantity;

	private Integer reservedQuantity;

	private Integer dispatchedQuantity;

	private String insertUser;

	private String updateUser;

	private Integer loaded = 0;

	private String destinationDescription;

	private String comments;

	private String insertDate;

	private String updateDate;

	private String planAge;

	private Double weight;

	private String weightUom;

	private Double volume;

	private String volumeUom;

	private Double weightUtil;

	private Date tempDispatchDate;

	private Date tempInsertDate;

	private Date tempUpdateDate;

	private Constants.DispatchPlanItemAppStatus appStatusEnum;

	private Double volumeUtil;

	private Double availbleTTE;

	private Double truckCount;

	private Double totalWeight;

	private Double totalVolume;

	public DispatchPlanItemDto() {
	}

	public DispatchPlanItemDto(DispatchPlanItemInfo planItemInfo) {
		this.id = planItemInfo.getId();
		this.planId = planItemInfo.getDispatchPlan().getId();
		this.lineNumber = planItemInfo.getLineNumber();
		this.dispatchDate = DateUtils.formatDate(planItemInfo.getDispatchDate(), Constants.PLAN_RECORD_DATE_FORMAT);
		this.sourceLocation = planItemInfo.getSourceLocation();
		this.destinationLocation = planItemInfo.getDestinationLocation();
		this.itemId = planItemInfo.getItemId();
		this.itemDescription = planItemInfo.getItemDescription();
		this.destinationDescription = planItemInfo.getDestinationDescription();
		this.category = planItemInfo.getCategory();
		this.tte = planItemInfo.getTte();
		this.batchCode = planItemInfo.getBatchCode();
		this.quantity = planItemInfo.getQuantity();
		this.priority = planItemInfo.getPriority();
		this.status = planItemInfo.getStatus().name();
		this.appStatus = Utility.camelCase(planItemInfo.getAppStatus().getStatus());
		this.marketSegment = planItemInfo.getMarketSegment();
		this.approvedQuantity = planItemInfo.getApprovedQuantity();
		this.deletedApprQuantity = planItemInfo.getDeletedQuantity();
		this.deletedUnApprQuantity = planItemInfo.getUnapprovedDeletedQuantity();
		this.availableQuantity = planItemInfo.getAvailableQuantity();
		this.reservedQuantity = planItemInfo.getReservedQuantity();
		this.dispatchedQuantity = planItemInfo.getDispatchedQuantity();
		this.unapprovedQuantity = planItemInfo.getUnapprovedQuantity() == null ? 0 : planItemInfo.getUnapprovedQuantity();
		this.totalAvailableQuantity = planItemInfo.getTotalAvailableQuantity();
		this.loaded = planItemInfo.getLoadedQty() != null ? planItemInfo.getLoadedQty() : 0;
		this.approvalQuantity = 0;
		this.unapprovalQuantity = 0;
		this.deleteApprQuantity = 0;
		this.deleteUnApprQuantity = 0;
		this.insertUser=planItemInfo.getInsertUser();
		this.updateUser = planItemInfo.getUpdateUser();
		this.comments = planItemInfo.getComments();
		this.insertDate = DateUtils.formatDate(planItemInfo.getInsertDate(), Constants.DATE_TIME_FORMAT);
		this.updateDate = !StringUtils.isEmpty(planItemInfo.getUpdateDate()) ? DateUtils.formatDate(planItemInfo.getUpdateDate(), Constants.DATE_TIME_FORMAT) : null;
		/*Calculated using Insert Date*/
		/*if(planItemInfo.getStatus().equals(Constants.Status.OPEN)) {
      this.planAge = ChronoUnit.DAYS.between(Instant.ofEpochMilli(DateUtils.setTimeToMidnight(planItemInfo.getInsertDate()).getTime()),
          Instant.ofEpochMilli(DateUtils.setTimeToMidnight(new Date()).getTime())) + "";
    }else {
      this.planAge = !StringUtils.isEmpty(planItemInfo.getUpdateDate()) ? ChronoUnit.DAYS.between(Instant.ofEpochMilli(DateUtils.setTimeToMidnight(planItemInfo.getInsertDate()).getTime()),
          Instant.ofEpochMilli(DateUtils.setTimeToMidnight(planItemInfo.getUpdateDate()).getTime())) + "" : null;
    }*/

		/*Calculating using Dipstch Date*/
		Long age = 0l;
		if(planItemInfo.getStatus().equals(Constants.Status.OPEN)) {
			age = ChronoUnit.DAYS.between(Instant.ofEpochMilli(DateUtils.setTimeToMidnight(planItemInfo.getDispatchDate()).getTime()),
					Instant.ofEpochMilli(DateUtils.setTimeToMidnight(new Date()).getTime())) ;
		}else {
			age = ChronoUnit.DAYS.between(Instant.ofEpochMilli(DateUtils.setTimeToMidnight(planItemInfo.getDispatchDate()).getTime()),
					Instant.ofEpochMilli( !StringUtils.isEmpty(planItemInfo.getUpdateDate()) ?  DateUtils.setTimeToMidnight(planItemInfo.getUpdateDate()).getTime() : DateUtils.setTimeToMidnight(new Date()).getTime()));
		}
		this.planAge = age >= 0 ? age +"" : 0 +"";


		this.weight = planItemInfo.getWeight();
		this.weightUom = planItemInfo.getWeightUom();
		this.volume = planItemInfo.getVolume();
		this.volumeUom = planItemInfo.getVolumeUom();

//		This will be used in export excel
		this.tempInsertDate = planItemInfo.getInsertDate();
		this.tempUpdateDate = planItemInfo.getUpdateDate();
		this.tempDispatchDate = planItemInfo.getDispatchDate();
	}

	public String validateApprovalQuantity() {
		if (this.quantity == null || this.quantity == 0)
			return String.format("Invalid quantity for plan id %s, line number : %s", this.planId, this.lineNumber);
		if (this.approvalQuantity == null || this.approvalQuantity > this.quantity)
			return String.format("Quantity to be approved is invalid for plan id %s, line number : %s", this.planId,
					this.lineNumber);
		if (this.approvedQuantity == null)
			return String.format("Invalid approved quantity for plan id %s, line number : %s", this.planId,
					this.lineNumber);
		if (this.unapprovedQuantity == null || (this.quantity - this.approvedQuantity) < this.unapprovedQuantity)
			return String.format("Invalid un approval quantity for plan id %s, line number : %s", this.planId,
					this.lineNumber);
		if(this.approvalQuantity == 0 && this.unapprovalQuantity == 0){
			return String.format("Please enter input value for plan id %s, line number : %s", this.planId,this.lineNumber);
		}
		if(this.approvalQuantity > (this.quantity -(this.approvedQuantity + this.deletedUnApprQuantity))){
			return String.format("Invalid approved quantity for plan id %s, line number : %s", this.planId,this.lineNumber);
		}
		 if(((this.approvedQuantity + this.approvalQuantity)- this.deletedApprQuantity) < (this.unapprovalQuantity)){
		 	return String.format("Invalid unapproved quantity for plan id %s , line number : %s ",this.planId,this.lineNumber);
		 }

	/*	if(this.totalAvailableQuantity < this.approvalQuantity){
			return  String.format("Approval quantity should not be greater then the Total avilable quantity for plan id %s , line number : %s", this.planId, this.lineNumber);
		}*/
		return null;
	}
	
	public String validateModifyQuantity() {
		if (this.quantity == null || this.quantity == 0)
			return String.format("Invalid quantity for plan id %s, line number : %s", this.planId, this.lineNumber);
		if (this.deletedApprQuantity == null)
			return String.format("Invalid deleted approved quantity for plan id %s, line number : %s", this.planId,
					this.lineNumber);
		if (this.deletedUnApprQuantity == null)
			return String.format("Invalid deleted unapproved quantity for plan id %s, line number : %s", this.planId,
					this.lineNumber);
		if (this.approvedQuantity == null)
			return String.format("Invalid approved quantity for plan id %s, line number : %s", this.planId,
					this.lineNumber);
		if (this.unapprovedQuantity == null)
			return String.format("Invalid un approved quantity for plan id %s, line number : %s", this.planId,
					this.lineNumber);
		if (this.deleteApprQuantity == null || this.deleteApprQuantity > this.approvedQuantity)
			return String.format("Invalid approved deleted qty for plan id %s, line number : %s", this.planId,
					this.lineNumber);
		if (this.deleteUnApprQuantity == null || this.deleteUnApprQuantity > this.unapprovedQuantity)
			return String.format("Invalid un approved deleted qty for plan id %s, line number : %s", this.planId,
					this.lineNumber);
		return null;
	}

	public void calculateDispatchPlanBOMValues(DispatchPlanItemDto dispatchPlanItemDto, ApplicationUser loggedInUser, MTTruckTypeInfo mtTruckTypeInfo) {
		if (UserRole.RDC_PLN.equals(loggedInUser.getRole())){
			if (dispatchPlanItemDto.getQuantity() != null) {
				Double totalVolume = dispatchPlanItemDto.getVolume() != null ? dispatchPlanItemDto.getVolume() * dispatchPlanItemDto.getQuantity() : null;
				Double totalWeight = dispatchPlanItemDto.getWeight() != null ? dispatchPlanItemDto.getWeight() * dispatchPlanItemDto.getQuantity() : null;
				if (totalVolume != null){
					dispatchPlanItemDto.setTotalVolume(totalVolume);
				}
				if (totalWeight != null){
					dispatchPlanItemDto.setTotalWeight(totalWeight);
				}
				if (mtTruckTypeInfo.getGrossVol() != null){
					if (totalVolume != null){
						dispatchPlanItemDto.setVolumeUtil((totalVolume / mtTruckTypeInfo.getGrossVol()) * 100);
					}
				}
				if (mtTruckTypeInfo.getGrossWt() != null){
					if (totalWeight != null){
						dispatchPlanItemDto.setWeightUtil((totalWeight / mtTruckTypeInfo.getGrossWt()) * 100);
					}
				}
				if (mtTruckTypeInfo.getTteCapacity() != null){
					if (dispatchPlanItemDto.getTte() != null) {
						dispatchPlanItemDto.setTruckCount(((dispatchPlanItemDto.getTte()) * (dispatchPlanItemDto.getQuantity())) / (mtTruckTypeInfo.getTteCapacity()));
						dispatchPlanItemDto.setAvailbleTTE(((dispatchPlanItemDto.getTte()) * (dispatchPlanItemDto.getQuantity())));
					}
				}
			}

		}else {
			if (dispatchPlanItemDto.getAvailableQuantity() != null) {
				Double totalVolume = dispatchPlanItemDto.getVolume() != null ? dispatchPlanItemDto.getVolume() * dispatchPlanItemDto.getAvailableQuantity() : null;
				Double totalWeight = dispatchPlanItemDto.getWeight() != null ? dispatchPlanItemDto.getWeight() * dispatchPlanItemDto.getAvailableQuantity() : null;
				if (totalVolume != null){
					dispatchPlanItemDto.setTotalVolume(totalVolume);
				}
				if (totalWeight != null){
					dispatchPlanItemDto.setTotalWeight(totalWeight);
				}
				if (mtTruckTypeInfo.getGrossVol() != null){
					if (totalVolume != null){
						dispatchPlanItemDto.setVolumeUtil((totalVolume / mtTruckTypeInfo.getGrossVol()) * 100);
					}
				}
				if (mtTruckTypeInfo.getGrossWt() != null){
					if (totalWeight != null){
						dispatchPlanItemDto.setWeightUtil((totalWeight / mtTruckTypeInfo.getGrossWt()) * 100);
					}
				}
				if (mtTruckTypeInfo.getTteCapacity() != null){
					if (dispatchPlanItemDto.getTte() != null) {
						dispatchPlanItemDto.setTruckCount(((dispatchPlanItemDto.getTte()) * (dispatchPlanItemDto.getAvailableQuantity())) / (mtTruckTypeInfo.getTteCapacity()));
						dispatchPlanItemDto.setAvailbleTTE(((dispatchPlanItemDto.getTte()) * (dispatchPlanItemDto.getAvailableQuantity())));
					}
				}
			}
		}
	}

}
