package com.api.apollo.atom.service;

import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.ops.IndentFilterDto;
import com.api.apollo.atom.dto.ops.IndentInfoDto;
import com.api.apollo.atom.dto.ops.TruckReportDto;
import com.api.apollo.atom.entity.ApplicationUser;
import org.springframework.security.core.Authentication;

public interface TransporterService {

  ApiResponse getIndents(IndentFilterDto indentSearchDto, ApplicationUser applicationUser);

  ApiResponse updateIndents(IndentInfoDto indentInfoDto, ApplicationUser loggedInUser) throws Exception;

  //ApiResponse assignTruck(TruckReportDto indentReportDto, ApplicationUser loggedInUser);
}
