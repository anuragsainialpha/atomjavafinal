package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.Constants.Status;
import com.api.apollo.atom.entity.ops.IndentSummary;
import com.api.apollo.atom.entity.ops.TruckReport;
import com.api.apollo.atom.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class IndentInfoDto {

    private Long id;

    @NotNull(message = "Indent value should not be Empty.")
    private String indentId;

	@JsonProperty("dispatchDate")
	@SerializedName(value = "DispatchDate")
	private String dispatchDate;

	@JsonProperty("SouceLocation")
	@SerializedName(value = "SouceLocation")
	private String source;

	@JsonProperty("destination")
	@SerializedName(value = "destination")
	private String destination;

	@JsonProperty("truckType")
	@SerializedName(value = "truckType")
	private String truckType;

	@JsonProperty("transporter")
	@SerializedName(value = "transporter")
	private String transporter;

	@JsonProperty("materailGrp")
	@SerializedName(value = "materailGrp")
	private String materailGrp;

	@JsonProperty("indented")
	@SerializedName(value = "indented")
	private Integer indented;

	@JsonProperty("comments")
	@SerializedName(value = "comments")
	private String comments;

	@JsonProperty("TTE")
	@SerializedName(value = "TTE")
	private Double tte;

	private Integer netRequested;

	private Integer cancelled;

    @NotNull(message = "Confirmed Value Can Not Be Empty.")
    @PositiveOrZero(message = "Confirmed Value Must Be Greater Or Equals To Zero.")
    private Integer confirmed;

    @NotNull(message = "Declined Value Can Not Be Empty.")
    @PositiveOrZero(message = "Declined Value Must Be Greater Or Equals To Zero.")
    private Integer declined;

	private Integer assigned;

	private Integer reported;

	private Integer rejected;

	private Integer netPlaced;

	private Integer netBalance;

	private Status status;

	private String insertUser;

	private Double loadFactor;

	private String destDis;

	private String updateComment;

	private String containerNum;

	private String isFreightAvailable;

	private String insertDate;

	private String indentAge;

	private String updateUser;

	private String destCountry;

	private String pod;

	private String destCountryName;

	private Set<TruckReportDto> reportedTrucks = new HashSet<>();

	private TruckReport editTruckReport;

	private Integer toBeConfirmed;

	public IndentInfoDto(IndentSummary indentSummary) {
		setIndentSummaryData(indentSummary);
//		this.reportedTrucks = indentSummary.getIndentDetails().parallelStream().map(indentDetails -> new TruckReportDto(indentDetails)).collect(Collectors.toSet());
		this.reportedTrucks = indentSummary.getTruckReports().parallelStream().filter(truckReport -> truckReport.getReportLocation().equalsIgnoreCase(indentSummary.getSourceLocation())).map(TruckReportDto::new).collect(Collectors.toSet());
	}

	/*Used in optimed view-indents API only*/
	public IndentInfoDto(IndentSummary indentSummary, String dummyVariable) {
		setIndentSummaryData(indentSummary);
//		this.reportedTrucks = indentSummary.getIndentDetails().parallelStream().map(indentDetails -> new TruckReportDto(indentDetails, "")).collect(Collectors.toSet());
		this.reportedTrucks = indentSummary.getTruckReports().parallelStream().filter(truckReport -> truckReport.getReportLocation().equalsIgnoreCase(indentSummary.getSourceLocation())).map(TruckReportDto::new).collect(Collectors.toSet());
	}

	private void setIndentSummaryData(IndentSummary indentSummary) {
		this.id = indentSummary.getId();
		this.indentId = indentSummary.getIndentId();
		this.dispatchDate = DateUtils.formatDate(indentSummary.getDispatchDate(), Constants.PLAN_RECORD_DATE_FORMAT);
		this.source = indentSummary.getSourceLocation();
		this.destination = indentSummary.getDestinationLocation();
		this.truckType = indentSummary.getTruckType();
		this.transporter = indentSummary.getTransporter();
		this.materailGrp = indentSummary.getCategory();
		this.indented = indentSummary.getIndented();
		this.comments = indentSummary.getComments();
		this.tte = indentSummary.getTte();
		this.loadFactor = indentSummary.getLoadFactor();
		this.netRequested = indentSummary.getNetRequested();
		this.cancelled = indentSummary.getCancelled();
		this.confirmed = indentSummary.getTransConfirmed();
		this.declined = indentSummary.getTransDeclined();
		this.assigned = indentSummary.getTransAssigned();
		this.reported = indentSummary.getReported();
		this.rejected = indentSummary.getRejected();
		this.netPlaced = indentSummary.getNetPlaced();
		this.netBalance = indentSummary.getNetBalance();
		this.status = indentSummary.getStatus();
		this.insertUser = indentSummary.getInsertUser();
		this.destDis = indentSummary.getDestDis();
		this.materailGrp = indentSummary.getCategory();
		this.netRequested = indentSummary.getNetRequested();
		this.isFreightAvailable = indentSummary.getIsFreightAvailable();
		/*Calculatyed using Insert date*/
		/*if (indentSummary.getStatus().equals(Status.OPEN) || indentSummary.getStatus().equals(Status.PARTIALLY_CONFIRMED)) {
			this.indentAge = ChronoUnit.DAYS.between(Instant.ofEpochMilli(DateUtils.setTimeToMidnight(indentSummary.getInsertDate()).getTime()),
					Instant.ofEpochMilli(DateUtils.setTimeToMidnight(new Date()).getTime())) + " ";
		} else {
			this.indentAge = ChronoUnit.DAYS.between(Instant.ofEpochMilli(DateUtils.setTimeToMidnight(indentSummary.getInsertDate()).getTime()),
					Instant.ofEpochMilli(indentSummary.getUpdateDate() != null ? DateUtils.setTimeToMidnight(indentSummary.getUpdateDate()).getTime() : DateUtils.setTimeToMidnight(new Date()).getTime())) + " ";
		}*/

		/*Calculating using Dispatch date*/
		Long age = 0l;
		if (indentSummary.getStatus().equals(Status.OPEN) || indentSummary.getStatus().equals(Status.PARTIALLY_CONFIRMED)) {
			age = ChronoUnit.DAYS.between(Instant.ofEpochMilli(DateUtils.setTimeToMidnight(indentSummary.getDispatchDate()).getTime()),
					Instant.ofEpochMilli(DateUtils.setTimeToMidnight(new Date()).getTime()));
		} else {
			age = ChronoUnit.DAYS.between(Instant.ofEpochMilli(DateUtils.setTimeToMidnight(indentSummary.getDispatchDate()).getTime()),
					Instant.ofEpochMilli(indentSummary.getUpdateDate() != null ? DateUtils.setTimeToMidnight(indentSummary.getUpdateDate()).getTime() : DateUtils.setTimeToMidnight(new Date()).getTime()));
		}

		this.indentAge = age >= 0 ? age + "" : 0 + "";
		this.insertDate = !StringUtils.isEmpty(indentSummary.getInsertDate()) ? DateUtils.formatDate(indentSummary.getInsertDate(), Constants.DATE_TIME_FORMAT) : null;
		this.updateUser = indentSummary.getUpdateUser();
		this.insertUser = indentSummary.getInsertUser();
		this.destCountry = indentSummary.getDestCountry();
		this.destCountryName = indentSummary.getDestCountryName();
		this.pod = indentSummary.getPod();
		/*If operator cancels/decreases trucks indented after Transporter confirming then the to be confirmed should not be -ve values */
		if ((indentSummary.getNetRequested()) < (indentSummary.getTransConfirmed() + indentSummary.getTransDeclined())) {
			this.toBeConfirmed = 0;
		} else {
			this.toBeConfirmed = (indentSummary.getNetRequested()) - (indentSummary.getTransConfirmed() + indentSummary.getTransDeclined());
		}
	}
}
