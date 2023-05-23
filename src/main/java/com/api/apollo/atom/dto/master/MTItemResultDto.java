package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.Constants.CodesInfo;
import com.api.apollo.atom.constant.Constants.MTItemStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.StoredProcedureQuery;

@Getter
@Setter
public class MTItemResultDto {

	private Integer totalRecords;
	private Integer totalErrorRecords;

	private Long id;
	private String message;
	private MTItemStatus status;
	private CodesInfo codesInfo;

	public MTItemResultDto() {
	}

	public MTItemResultDto(StoredProcedureQuery storedProcedure) {
		this.totalRecords = storedProcedure.getOutputParameterValue("p_tot_records") != null
				? (Integer) storedProcedure.getOutputParameterValue("p_tot_records")
				: null;
//		this.totalErrorRecords = storedProcedure.getOutputParameterValue("mt_tot_error_records") != null
//				? (Integer) storedProcedure.getOutputParameterValue("mt_tot_error_records")
//				: null;

//		this.id = storedProcedure.getOutputParameterValue("mt_item_id") != null
//				? (Long) storedProcedure.getOutputParameterValue("mt_item_id")
//				: null;

//		if (this.totalErrorRecords > 0) {
//			this.status = MTItemStatus.MTITEM_PENDING;
//			this.message = "Item ID : " + this.id + " created with errors";
//		} else {
//			this.status = MTItemStatus.MTITEM_SUCCESS;
//			this.message = "Item ID : " + this.id + " created successfully";
//		}
		this.codesInfo = new Constants().new CodesInfo();
	}


//	public MTItemResultDto(Integer totalRecords, Integer totalErrorRecords, Integer totalTyreCount,
//                           Integer c1Count, Integer c2Count, Integer c3Count, Integer c4Count, Integer c5Count, Integer c6Count, String status,
//                           Long planId) {
//		super();
//		this.totalRecords = totalRecords;
//		this.totalErrorRecords = totalErrorRecords;
//		this.totalTyreCount = totalTyreCount;
//		this.c1Count = c1Count;
//		this.c2Count = c2Count;
//		this.c3Count = c3Count;
//		this.c4Count = c4Count;
//		this.c5Count = c5Count;
//		this.c6Count = c6Count;
//		this.id = planId;
//		this.status = MTItemStatus.valueOf(status);
//		if (this.totalErrorRecords > 0) {
//			this.message = "MTItem ID : " + this.id + " created with errors";
//		} else {
//			this.message = "MTItem ID : " + this.id + " created successfully";
//		}
//		this.codesInfo = new Constants().new CodesInfo();
//	}





}
