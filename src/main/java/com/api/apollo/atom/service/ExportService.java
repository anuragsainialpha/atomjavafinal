package com.api.apollo.atom.service;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.ops.*;
import com.api.apollo.atom.entity.ApplicationUser;

public interface ExportService {

  ApiResponse createIndents(IndentDto indentDto, ApplicationUser applicationUserFromAuthentication);

  ApiResponse getIndents(IndentFilterDto indentFilterDto, ApplicationUser loggedInUser);

  ApiResponse reportTruck(TruckReportDto indentReportDto, ApplicationUser loggedInUser);

  ApiResponse getIndentTruckReportInfo(String indentId, ApplicationUser loggedInUser);

  ApiResponse modifyIndents(IndentInfoDto indentInfoDto,  ApplicationUser applicationUser);


  ApiResponse getExportContainer(Constants.DelInvType type, String loadslipId, ApplicationUser applicationUser);

  ApiResponse getLoadSlipUtilizationsAndExportData(ApplicationUser loggedInUser, LsUtilizationDto lsUtilizationDto);

  ApiResponse getExportTrackerData(ApplicationUser loggedInUser, ExportTrackerFilter exportTrackerFilter);

  ApiResponse updateExportTrackerData(ApplicationUser loggedInUser, ExportTrackerDto exportTrackerDto);
}
