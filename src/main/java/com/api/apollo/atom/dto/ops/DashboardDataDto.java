package com.api.apollo.atom.dto.ops;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class DashboardDataDto {

  private OpenPlanDataDto openPlanDataDto = new OpenPlanDataDto();

  private TruckReportDto truckReportDto = new TruckReportDto();

  private List<IndentStatusDto> indentStatusDtos = new ArrayList<>();

  private List<IndentStatusDto> placementStatusDtos = new ArrayList<>();

  private  List<ShipmentStatusDto> shipmentStatusList = new ArrayList<>();

  private PlanAgeing planAgeing = new PlanAgeing();

  private PlanAgeing delayedDays = new PlanAgeing();

  private List<PlanUploadDto> planUploadDtos = new ArrayList<>();

  private List<PlansDataDto> plansDataDtos = new ArrayList<>();

  private List<PlansDataDto> dispatchedPlansDataList = new ArrayList<>();
}
