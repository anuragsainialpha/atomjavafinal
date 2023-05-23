package com.api.apollo.atom.dto.planner;

import com.api.apollo.atom.constant.Case;
import com.api.apollo.atom.constant.CaseType;
import com.api.apollo.atom.util.Utility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;

@Getter
public class PlanUploadDto {

	@JsonProperty("Dispatch Date")
	@SerializedName(value = "DispatchDate")
	public String dispatchDate;

	@JsonProperty("Source Location")
	@Case(CaseType.TO_UPPER)
	@SerializedName(value = "SourceLocation")
	public String sourceLocation;

	@JsonProperty("Destination Location")
	@Case(CaseType.TO_UPPER)
	@SerializedName(value = "DestinationLocation")
	public String destinationLocation;

	@JsonProperty("Material Code")
	@Case(CaseType.TO_UPPER)
	@SerializedName(value = "MaterialCode")
	public String materialCode;

	@JsonProperty("Material Description")
	@Case(CaseType.TO_UPPER)
	@SerializedName(value = "MaterialDescription")
	public String materialDescription;

	@JsonProperty("Batch Code")
	@Case(CaseType.TO_UPPER)
	@SerializedName(value = "BatchCode")
	public String batchCode;

	@JsonProperty("Marketing Segment")
	@SerializedName(value = "MarketingSegment")
	public String marketingSegment;

	@JsonProperty("Quantity")
	@SerializedName(value = "Quantity")
	public String quantity;

	@JsonProperty("Priority")
	@SerializedName(value = "Priority")
	public String priority;

	@JsonProperty("Comments")
	@Case(CaseType.TO_UPPER)
	@SerializedName(value = "Comments")
	public String comments;

	@JsonCreator
	public PlanUploadDto() {

	}

	@JsonCreator
	public PlanUploadDto(@JsonProperty("Dispatch Date") String dispatchDate, @JsonProperty("Source Location") String sourceLocation, @JsonProperty("Destination Location") String destinationLocation, @JsonProperty("Material Code") String materialCode,
											 @JsonProperty("Material Description") String materialDescription, @JsonProperty("Batch Code") String batchCode, @JsonProperty("Marketing Segment") String marketingSegment, @JsonProperty("Quantity") String quantity,
											 @JsonProperty("Priority") String priority, @JsonProperty("Comments") String comments) {
		this.dispatchDate = dispatchDate;
		this.sourceLocation = sourceLocation;
		this.destinationLocation = destinationLocation;
		this.materialCode = materialCode;
		this.materialDescription = materialDescription;
		this.batchCode = batchCode;
		this.marketingSegment = marketingSegment;
		this.quantity = quantity;
		this.priority = priority;
		this.comments = comments;

		Utility.parse(this);
	}
}