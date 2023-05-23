package com.api.apollo.atom.controller;

import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.planner.DispatchPlanDto;
import com.api.apollo.atom.dto.planner.DispatchPlanFilterDto;
import com.api.apollo.atom.dto.planner.DispatchPlanItemDto;
import com.api.apollo.atom.service.PlannerService;
import com.api.apollo.atom.util.Utility;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Api(value = "RDC- Planner Management")
@RestController
@RequestMapping("/api/v1/rdc/planner")
@PreAuthorize("hasAnyAuthority('RDC_PLN')")
public class RDCController {

  @Autowired
  private  PlannerService plannerService;

  @PostMapping(value = "upload-plan")
  public ResponseEntity<ApiResponse> uploadPlan(@RequestBody DispatchPlanDto planDto, Authentication authentication) throws Exception {
    return ResponseEntity.ok(plannerService.uploadDispatchPlan(planDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @GetMapping(value = "error/info")
  public ResponseEntity<ApiResponse> getErrorPlanInfo(@RequestParam(name = "planId", required = true) Long planId,
                                                      @RequestParam(name = "errorCode", required = true) String errorCode,
                                                      @RequestParam(name = "index", required = false, defaultValue = "0") int index,
                                                      @RequestParam(name = "pageLength", required = false, defaultValue = "10") int pageLength) throws Exception {
    return ResponseEntity.ok(plannerService.getErrorPlanInfo(planId, errorCode, index, pageLength));
  }

  @GetMapping(value = "error/count")
  public ResponseEntity<ApiResponse> getDispatchPlanInfoCount(
      @RequestParam(name = "planId", required = true) Long planId, Authentication authentication)
      throws AccessDeniedException {
    return ResponseEntity.ok(plannerService.getDispatchPlanErrorInfoCount(planId, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @PostMapping(value = "pending-plans")
  public ResponseEntity<ApiResponse> getPendingDispatchPlans(@RequestBody DispatchPlanFilterDto searchDto,
                                                             Authentication authentication) {
    return ResponseEntity.ok(plannerService.getUserPendingDispatchPlans(Utility.getApplicationUserFromAuthentication(authentication), searchDto));
  }

  @PostMapping(value = "plan-info")
  public ResponseEntity<ApiResponse> getDispatchPlanInfo(@RequestBody DispatchPlanFilterDto searchDto, Authentication authentication) {
    return ResponseEntity.ok(plannerService.getDispatchPlanInfo(searchDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @PostMapping(value = "error/resolve")
  public ResponseEntity<ApiResponse> resolvePlanItems(@RequestBody DispatchPlanFilterDto searchDto, Authentication authentication) {
    return ResponseEntity.ok(plannerService.resolvePlanErrors(searchDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @PostMapping(value = "plan-info/approve")
  public ResponseEntity<ApiResponse> approvePlanInfo(@RequestBody DispatchPlanFilterDto searchDto, Authentication authentication) {
    return ResponseEntity.ok(plannerService.approveDispatchPlanInfo(searchDto,Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @DeleteMapping(value = "plan-info/remove")
  public ResponseEntity<ApiResponse> removePlanInfo(@RequestBody List<Long> ids, Authentication authentication) {
    return ResponseEntity.ok(plannerService.deleteDispatchPlanInfo(ids, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @DeleteMapping(value = "plan-info/temp/remove")
  public ResponseEntity<ApiResponse> removePlanTempInfo(@RequestParam("id") Long id,
                                                        @RequestParam("code") String code, @RequestParam(name = "status", defaultValue = "false") boolean status, Authentication authentication) {
    return ResponseEntity.ok(plannerService.deleteDispatchPlanTempInfo(id, code, Utility.getApplicationUserFromAuthentication(authentication), status));
  }

  @GetMapping(value = "create-plan")
  public ResponseEntity<ApiResponse> createDispatchPlan(@RequestParam("id") Long id, Authentication authentication) {
    return ResponseEntity.ok(plannerService.createDispatchPlan(id, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @PostMapping(value = "plan-info/modify")
  public ResponseEntity<ApiResponse> approvePlanInfo(@RequestBody List<DispatchPlanItemDto> dispatchPlanItemDtoList,Authentication authentication) {
    return ResponseEntity.ok(plannerService.modifyDispatchPlan(dispatchPlanItemDtoList,Utility.getApplicationUserFromAuthentication(authentication)));
  }

}
