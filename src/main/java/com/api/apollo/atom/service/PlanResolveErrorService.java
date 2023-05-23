package com.api.apollo.atom.service;

import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.planner.DispatchPlanFilterDto;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.plan.DispatchPlanItemTempInfo;

public interface PlanResolveErrorService {

	ApiResponse resolveC1(DispatchPlanFilterDto searchDto,ApplicationUser loggedInUser);

	ApiResponse resolveC2(DispatchPlanFilterDto searchDto,ApplicationUser loggedInUser);

	ApiResponse resolveC3(DispatchPlanFilterDto searchDto,ApplicationUser loggedInUser);

	ApiResponse resolveC4(DispatchPlanFilterDto searchDto,ApplicationUser loggedInUser);

	ApiResponse resolveC5(DispatchPlanFilterDto searchDto,ApplicationUser loggedInUser);

	ApiResponse resolveC6(DispatchPlanFilterDto searchDto,ApplicationUser loggedInUser);

	String storeDuplicateRecord(DispatchPlanItemTempInfo PlanItemTempInfo, ApplicationUser applicationUser);

}
