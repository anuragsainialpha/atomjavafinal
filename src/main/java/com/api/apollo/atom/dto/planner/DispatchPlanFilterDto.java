package com.api.apollo.atom.dto.planner;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.Constants.DispatchPlanItemAppStatus;
import com.api.apollo.atom.constant.UserRole;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.master.MTTruckTypeInfo;
import com.api.apollo.atom.entity.plan.DispatchPlanItemInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class DispatchPlanFilterDto {

  // properties for master data search
  private long total;

  // properties for plan detail search
  private String sourceCode;

//  Property for source multiselect
  private List<String> sourceCodeList = new ArrayList<>();

  private String destinationCode;
  private String materialCode;
  private String materialDescription;
  private List<String> materialGroup  = new ArrayList<>();;
  private String fromDispatchDate;
  private String toDispatchDate;
  private List<String> marketSegment = new ArrayList<>();
  private Long planId;
  private List<String> status = new ArrayList<>();
  private List<DispatchPlanItemAppStatus> statuses = new ArrayList<>();

  private int index = Constants.PAGE_INDEX;
  private int pageLength = Constants.PAGE_LIMIT;
  private List<DispatchPlanItemDto> planItems = new ArrayList<>();
  private List<DispatchPlanDto> plans = new ArrayList<>();

  private Long planItemId;
  private String errorCode;
  private String batchCode;
  /*Used to filter plan status(OPEN/COMPLETED*/
  private List<String> planStatus = new ArrayList<>();

  private String insertUser;

  private String updateUser;

  /*Used when we want to remove C6 error and allow duplicate record in Dispatch_Plan*/
  private boolean duplicateAllowed = false;

  private List<Long> planInfoIds = new ArrayList<>();

  private Boolean isViewPlans = false;

  private Integer standardFtlWeight;

  private Double standardFtlTteCapacity;

  private Integer standardFTLVolumne;

  private String itemClassification;

  private List<String> priorityList = new ArrayList<>();



  /*Optimized plans*/
  public DispatchPlanFilterDto(Page<DispatchPlanItemInfo> dispatchPlanItems, MTTruckTypeInfo mtTruckTypeInfo, ApplicationUser applicationUser) {
    this.planItems = dispatchPlanItems.stream().parallel().map(itemInfo -> {
      DispatchPlanItemDto planItemDto = new DispatchPlanItemDto(itemInfo);
      planItemDto.calculateDispatchPlanBOMValues(planItemDto, applicationUser,mtTruckTypeInfo);
      return planItemDto;
    }).collect(Collectors.toList());
    this.total = dispatchPlanItems.getTotalElements();
  }

  public DispatchPlanFilterDto(List<DispatchPlanItemInfo> dispatchPlanItems, MTTruckTypeInfo mtTruckTypeInfo, ApplicationUser applicationUser) {
    this.planItems = dispatchPlanItems.stream().parallel().map(itemInfo -> {
      DispatchPlanItemDto planItemDto = new DispatchPlanItemDto(itemInfo);
      planItemDto.calculateDispatchPlanBOMValues(planItemDto, applicationUser, mtTruckTypeInfo);
      return planItemDto;
    }).collect(Collectors.toList());
    this.total = dispatchPlanItems.size();
  }
  public DispatchPlanFilterDto(List<DispatchPlanItemDto> dispatchPlanItems, int total) {
    this.planItems = dispatchPlanItems;
    this.total = total;
  }

  public boolean isSearchFilter() {
    return !StringUtils.isEmpty(this.destinationCode) || !StringUtils.isEmpty(this.materialCode)
        || !StringUtils.isEmpty(this.materialDescription) || !(this.materialGroup != null && this.materialGroup.isEmpty())
        || !StringUtils.isEmpty(this.fromDispatchDate) && !StringUtils.isEmpty(this.toDispatchDate) || (this.marketSegment != null && this.marketSegment.size()>0)
        || !StringUtils.isEmpty(this.planId) || (this.status != null && this.status.size()>0) || !StringUtils.isEmpty(this.insertUser) || !StringUtils.isEmpty(this.sourceCode)
        || (this.planStatus != null && this.planStatus.size()>0)|| !StringUtils.isEmpty(this.updateUser) || (this.priorityList != null && !this.priorityList.isEmpty()) ||
        (this.sourceCodeList != null && !this.sourceCodeList.isEmpty());
  }

}
