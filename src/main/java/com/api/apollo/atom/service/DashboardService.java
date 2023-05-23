package com.api.apollo.atom.service;

import com.api.apollo.atom.dto.ops.*;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.master.MTLocation;

import java.util.List;
import java.util.Optional;

public interface DashboardService {

  List<String> getShipmentSourceList();

  OpenPlanDataDto getOpenPlansData(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation);

  List<IndentStatusDto> getIndentStatusWithCumm(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, boolean isPlacementStatus, Optional<MTLocation> optionalMTLocation);

  List<ShipmentStatusDto> getShipmentStatusData(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation);

  PlanAgeing getPlanAgeing(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation);

  PlanAgeing getDelayedDaysData(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation);

  List<PlanUploadDto> getPlanUploadData(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation);

  List<PlansDataDto> getOpenPlansDetails(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation);

  List<PlansDataDto> getDispatchedPlanDetails(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Optional<MTLocation> optionalMTLocation);

}
