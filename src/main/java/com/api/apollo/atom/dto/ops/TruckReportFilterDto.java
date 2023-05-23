package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.ops.TruckReport;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class TruckReportFilterDto {

  private String destination;
  private String source;
  private List<String> truckType = new ArrayList<>();
  private String truckNumber;
  private String indentID;
  private String transporter;
  private String status;
  private List<String> statuses = new ArrayList<>();
  private String type;
  private int index = Constants.PAGE_INDEX;
  private int pageLength = Constants.PAGE_LIMIT;
  private long total;
  private String activity;
  //For list of the status
  private List<String> statusList = new ArrayList<>();

  private String reportLocation;

  private String fromReportDate;

  private String toReportDate;

  private String fromGateOutDate;

  private String toGateOutDate;

  private String fromGateInDate;

  private String toGateInDate;

  private String shipmentID;

  private String destCountry;

  private List<String> indentCategoryList = new ArrayList<>();

  private List<TruckReportDto> truckReportsData = new ArrayList<>();

  private List<TrucksMetaData> trucksMetaDatas = new ArrayList<>();

	private String loadslipId;
  private String containerNum;
  private String fromCreatedDate;
	private String toCreatedDate;
	private List<String> bayStatus = new ArrayList<>();
	private String invoice;
	private List<String> marketSegment = new ArrayList<>();
	private String itemId;
	private String stopType;
	private List<String> rejection = new ArrayList<>();

	private String insertUser;

  public TruckReportFilterDto(Page<TrucksMetaData> trucksMetaData ,String didnotuse) {
    this.trucksMetaDatas = trucksMetaData.getContent();
    this.total = trucksMetaData.getTotalElements();
  }

  // FROM FGS OPS & GATE
  public TruckReportFilterDto(Page<TruckReport> reportedTrcuks, ApplicationUser loggedInUser) {
    this.truckReportsData = reportedTrcuks.stream().parallel().map(reportInfo -> new TruckReportDto(null, reportInfo, loggedInUser)).collect(Collectors.toList());
    this.total = reportedTrcuks.getTotalElements();
  }

  // FROM RDC OPS & GATE
  public TruckReportFilterDto(ApplicationUser loggedInUser, Page<TruckReport> reportedTrcuks,int pageLength) {
    this.truckReportsData = reportedTrcuks.stream().parallel().map(reportInfo -> new TruckReportDto(reportInfo, loggedInUser)).collect(Collectors.toList());
    this.total = reportedTrcuks.getTotalElements();
    this.pageLength = pageLength;
  }

  /*Used for optimizing trucks-info api
  * didNotUse variable is used only to differentiate the constructors*/
  public TruckReportFilterDto(ApplicationUser loggedInUser, Page<TruckReport> reportedTrcuks,String didNotUse,int pageLength) {
    this.truckReportsData = reportedTrcuks.stream().parallel().map(reportInfo -> new TruckReportDto(reportInfo, loggedInUser, didNotUse)).collect(Collectors.toList());
    this.total = reportedTrcuks.getTotalElements();
    this.pageLength = pageLength;
  }

  public boolean isReportedTrucksFilter() {
    return !StringUtils.isEmpty(this.indentID) || !StringUtils.isEmpty(this.destination) || !StringUtils.isEmpty(this.truckNumber)
        || !StringUtils.isEmpty(this.transporter) || !StringUtils.isEmpty(this.status) || !StringUtils.isEmpty(this.source) || !StringUtils.isEmpty(this.fromReportDate) || !StringUtils.isEmpty(this.toReportDate)
        || !StringUtils.isEmpty(this.fromGateOutDate) || !StringUtils.isEmpty(this.toGateOutDate) || !StringUtils.isEmpty(this.fromGateInDate) || !StringUtils.isEmpty(this.toGateInDate )|| !StringUtils.isEmpty(this.shipmentID)
				|| !StringUtils.isEmpty(this.fromCreatedDate) || !StringUtils.isEmpty(this.toCreatedDate) || !StringUtils.isEmpty(this.loadslipId) || !StringUtils.isEmpty(this.containerNum) || (this.bayStatus != null && this.bayStatus.size()>0)
				|| !StringUtils.isEmpty(this.invoice)  || (this.marketSegment != null && this.marketSegment.size()>0) || !StringUtils.isEmpty(this.itemId) || !StringUtils.isEmpty(this.stopType) || !StringUtils.isEmpty(this.destCountry)
        || (this.rejection != null && this.rejection.size()>0) || (this.truckType != null && this.truckType.size()>0) || (this.statuses != null && this.statuses.size()>0) || (indentCategoryList != null && !indentCategoryList.isEmpty())
        || !StringUtils.isEmpty(this.insertUser);
  }

  public TruckReportFilterDto(Page<TruckReport> truckReports){
   this.truckReportsData = truckReports.stream().parallel().map(TruckReportDto::new).collect(Collectors.toList());
  }

}
