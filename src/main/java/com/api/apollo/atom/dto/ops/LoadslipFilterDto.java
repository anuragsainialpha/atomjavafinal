package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.constant.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class LoadslipFilterDto {

	private String loadslipId;
	private String stopType;
	private String transhipment;
	private String destination;
	private List<String> truckType = new ArrayList<>();
	private String invoice;
	private String itemId;
	private String fromCreatedDate;
	private String toCreatedDate;
	private List<String> marketSegment = new ArrayList<>();
	private String truckNumber;
	private String containerNum;

	//Consent Status Added
	private String trackingConsentStatus;
	private String consentPhoneTelecom;

	private List<String> itemCategories = new ArrayList<>();

	/*
	 * to differentiate  while getting predicates for loadslip with invoice , loadslip with out invoice
	 * */
	private boolean loadslipsWithItems = true;
	// For Drafted or Printed Loadslips
	private String type;
	private String shipmentId;
	private List<String> status = new ArrayList<>();

	/*Used only for Filter when userRole id DP_REP*/
	private String source;

	private List<String> lsStatus = new ArrayList<>();

	private String destCountry;

	private String transporter;

	private String insertUser;

	private int index = Constants.PAGE_INDEX;
	private int pageLength = Constants.PAGE_LIMIT;
	private long total;
	private List<LoadslipMetaData> loadslips = new ArrayList<>();

	public LoadslipFilterDto(Page<LoadslipMetaData> loadslipMetaData) {
		this.loadslips = loadslipMetaData.getContent();
		this.total = loadslipMetaData.getTotalElements();
	}


	public boolean isLoadslipsFilter() {
		return !StringUtils.isEmpty(this.loadslipId) || !StringUtils.isEmpty(this.stopType) || !StringUtils.isEmpty(this.transhipment)
				|| !StringUtils.isEmpty(this.destination) || (this.truckType != null && this.truckType.size()>0) || !StringUtils.isEmpty(this.invoice)
				|| !StringUtils.isEmpty(this.itemId) || !StringUtils.isEmpty(this.fromCreatedDate) || !StringUtils.isEmpty(this.toCreatedDate) || (this.marketSegment != null && this.marketSegment.size()>0)
				|| !StringUtils.isEmpty(this.shipmentId)  || !StringUtils.isEmpty(this.source) || !StringUtils.isEmpty(this.truckNumber) || !StringUtils.isEmpty(this.containerNum)
				|| !StringUtils.isEmpty(this.destCountry) || !StringUtils.isEmpty(this.transporter) ||(this.lsStatus != null && this.lsStatus.size()>0) ||
				(this.itemCategories != null && !this.itemCategories.isEmpty()) || !StringUtils.isEmpty(this.insertUser);
	}
}
