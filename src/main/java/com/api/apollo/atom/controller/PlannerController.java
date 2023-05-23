package com.api.apollo.atom.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.planner.DispatchPlanDto;
import com.api.apollo.atom.dto.planner.DispatchPlanItemDto;
import com.api.apollo.atom.dto.planner.DispatchPlanFilterDto;
import com.api.apollo.atom.service.PlannerService;
import com.api.apollo.atom.util.Utility;

import io.swagger.annotations.Api;

@Api(value = "FGS - Planner Management")
@RestController
@RequestMapping("/api/v1/planner/")
@PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP','L1_MGR','L2_MGR','PLT_PLN','RDC_PLN')")
public class PlannerController {

	@Autowired
	PlannerService plannerService;

	/**
	 * <b>Uploading the plan using excel/Creating Manual Plan </b>
	 * @param planDto - Required Dispatch plan dto for upload or create the plan {@link DispatchPlanDto}
	 * @param authentication - Logged In User
	 * @return - Success/Error message along with the error lines and error code count
	 * @throws Exception
	 */
	@PostMapping(value = "upload-plan")
	public ResponseEntity<ApiResponse> uploadPlan(@RequestBody DispatchPlanDto planDto, Authentication authentication) throws Exception {
		return ResponseEntity.ok(plannerService.uploadDispatchPlan(planDto, Utility.getApplicationUserFromAuthentication(authentication)));
	}

	/**
	 * <b>Fetching the error info of the plan</b>
	 * @param planId - required dispatch plan id
	 * @param errorCode - required error code for that plan
	 * @param index - starting index of the page
	 * @param pageLength - no of rows in the page
	 * @return - list of plan item with the given error code
	 * @throws Exception
	 */
	@GetMapping(value = "error/info")
	public ResponseEntity<ApiResponse> getErrorPlanInfo(@RequestParam(name = "planId", required = true) Long planId,
			@RequestParam(name = "errorCode", required = true) String errorCode,
			@RequestParam(name = "index", required = false, defaultValue = "0") int index,
			@RequestParam(name = "pageLength", required = false, defaultValue = "10") int pageLength) throws Exception {
		return ResponseEntity.ok(plannerService.getErrorPlanInfo(planId, errorCode, index, pageLength));
	}

	/**
	 * <b>Getting the count of each error code for a plan</b>
	 * @param planId - required dispatch plan id
	 * @param authentication - Logged in user
	 * @return - count of each error code of the plan
	 * @throws AccessDeniedException
	 */
	@GetMapping(value = "error/count")
	public ResponseEntity<ApiResponse> getDispatchPlanInfoCount(
			@RequestParam(name = "planId", required = true) Long planId, Authentication authentication)
			throws AccessDeniedException {
		return ResponseEntity.ok(plannerService.getDispatchPlanErrorInfoCount(planId, Utility.getApplicationUserFromAuthentication(authentication)));
	}

	/**
	 * <b>Fetching the list of dispatch plan created with error</b>
	 * @param searchDto - required filter criteria {@link DispatchPlanFilterDto}
	 * @param authentication - Logged in user
	 * @return - List of Dispatch plans which are created with errors
	 */
	@PostMapping(value = "pending-plans")
	public ResponseEntity<ApiResponse> getPendingDispatchPlans(@RequestBody DispatchPlanFilterDto searchDto,
			Authentication authentication) {
		return ResponseEntity.ok(plannerService.getUserPendingDispatchPlans(Utility.getApplicationUserFromAuthentication(authentication), searchDto));
	}

	/**
	 * <b>Getting the dispatch plan details</b>
	 * @param searchDto - required filter criteria {@link DispatchPlanFilterDto}
	 * @param authentication - Logged in user
	 * @return - list of dispatch plans which are created with out any errors
	 */
	@PostMapping(value = "plan-info")
	public ResponseEntity<ApiResponse> getDispatchPlanInfo(@RequestBody DispatchPlanFilterDto searchDto, Authentication authentication) {
		return ResponseEntity.ok(plannerService.getDispatchPlanInfo(searchDto, Utility.getApplicationUserFromAuthentication(authentication)));
	}

	/**
	 * <b>Resolving the error</b>
	 * @param searchDto - required changed made in the dispatch plan {@link DispatchPlanFilterDto}
	 * @param authentication - logged in user
	 * @return - remaining error count after resolving a perticular error
	 */
	@PostMapping(value = "error/resolve")
	public ResponseEntity<ApiResponse> resolvePlanItems(@RequestBody DispatchPlanFilterDto searchDto, Authentication authentication) {
		return ResponseEntity.ok(plannerService.resolvePlanErrors(searchDto, Utility.getApplicationUserFromAuthentication(authentication)));
	}

	/**
	 * <b>Approve/Unapprove the dispatch plan</b>
	 * @param searchDto - required quantity to be approved or unapproved {@link DispatchPlanFilterDto}
	 * @param authentication - logged in user
	 * @return - updated dispatch plan details
	 */
	@PostMapping(value = "plan-info/approve")
	public ResponseEntity<ApiResponse> approvePlanInfo(@RequestBody DispatchPlanFilterDto searchDto, Authentication authentication) {
		return ResponseEntity.ok(plannerService.approveDispatchPlanInfo(searchDto,Utility.getApplicationUserFromAuthentication(authentication)));
	}

	/**
	 * <b>Deleting the dispatch plan details</b>
	 * @param ids - ids of the plans to be deleted
	 * @param authentication - logged in user
	 * @return - success/error message
	 */
	@DeleteMapping(value = "plan-info/remove")
	public ResponseEntity<ApiResponse> removePlanInfo(@RequestBody List<Long> ids, Authentication authentication) {
		return ResponseEntity.ok(plannerService.deleteDispatchPlanInfo(ids, Utility.getApplicationUserFromAuthentication(authentication)));
	}

	/**
	 * <b>Deleting the error plan record</b>
	 * @param id - id of the plan to be deleted
	 * @param code - error of the plan to be deleted
	 * @param status - required boolean value to check if ther are multiple error line with the error code for the plan
	 * @param authentication - logged in user
	 * @return - remaining error count after deleting a perticular error
	 */
	@DeleteMapping(value = "plan-info/temp/remove")
	public ResponseEntity<ApiResponse> removePlanTempInfo(@RequestParam("id") Long id,
			@RequestParam("code") String code, @RequestParam(name = "status", defaultValue = "false") boolean status, Authentication authentication) {
		return ResponseEntity.ok(plannerService.deleteDispatchPlanTempInfo(id, code, Utility.getApplicationUserFromAuthentication(authentication), status));
	}

	/**
	 * <b>Calling the create dispatch plan procedure after resolving all the errors</b>
	 * @param id - id of the plan to be created
	 * @param authentication - - logged in user
	 * @return - success message along with plan id/error message
	 */
	@GetMapping(value = "create-plan")
	public ResponseEntity<ApiResponse> createDispatchPlan(@RequestParam("id") Long id, Authentication authentication) {
		return ResponseEntity.ok(plannerService.createDispatchPlan(id, Utility.getApplicationUserFromAuthentication(authentication)));
	}

	/**
	 *<b>Modify diapatch plan</b>
	 * @param dispatchPlanItemDtoList - required changed to be made in the dispatch plan {@link DispatchPlanItemDto}
	 * @param authentication - logged in user
	 * @return - the updated dispatch plan details
	 */
	@PostMapping(value = "plan-info/modify")
	public ResponseEntity<ApiResponse> approvePlanInfo(@RequestBody List<DispatchPlanItemDto> dispatchPlanItemDtoList,Authentication authentication) {
		return ResponseEntity.ok(plannerService.modifyDispatchPlan(dispatchPlanItemDtoList,Utility.getApplicationUserFromAuthentication(authentication)));
	}


}
