package com.api.apollo.atom.dto.ops;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class DashboardFilterDto {

  private Boolean isOpenPlans = false;
  private Boolean isIndentStatus = false;
  private Boolean isTruckStatus = false;
  private Boolean isShipmentStatus = false;
  private Boolean isPlanAgeing = false;
  private Boolean isPlacementStatus = false;
  private Boolean isDelayedDays = false;
  private Boolean isPlanUpload = false;
  private Boolean isDispatchedPlans = false;

//  Plan Age Bucket values
  private int firstLineStart = 0;
  private int firstLineEnd = 2;
  private int secondLineStart = 3;
  private int secondLineEnd = 5;
  private int thirdLine = 6;

  private String sourceLoc;

  private List<String> sourceList = new ArrayList<>();

  private String destination;

  private String fromDispatchDate;

  private String toDispatchDate;

  private String fromDispatchDateAgeing;

  private String toDispatchDateAgeing;

  private String transporter;

  List<String> materialGroup = new ArrayList<>();

  List<String> marketSegments = new ArrayList<>();

  private boolean withReservedQty = false;

  private List<String> appStatusList = new ArrayList<>();

  private String planStatus;

  private String appStatus;

  List<String> truckTypes = new ArrayList<>();

  private List<String> shipmentStatusList = new ArrayList<>();

  private List<String> transporterList = new ArrayList<>();

}
