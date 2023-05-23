package com.api.apollo.atom.service;

import org.springframework.security.access.AccessDeniedException;

import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.planner.DispatchPlanDto;
import com.api.apollo.atom.dto.planner.DispatchPlanItemDto;
import com.api.apollo.atom.dto.planner.DispatchPlanFilterDto;
import com.api.apollo.atom.entity.ApplicationUser;

import java.util.List;

public interface PlannerService {

	/**
	 * <b>Uploading the plan using excel/Creating Manual Plan </b>
	 * @param plan - Required Dispatch plan dto for upload or create the plan {@link DispatchPlanDto}
	 * @param loggedInUser - Logged In User
	 * @return - Success/Error message along with the error lines and error code count
	 * @throws Exception
	 */
	ApiResponse uploadDispatchPlan(DispatchPlanDto plan, ApplicationUser loggedInUser) throws Exception;

	/**
	 * <b>Fetching the error info of the plan</b>
	 * @param planId - required dispatch plan id
	 * @param errorCode - required error code for that plan
	 * @param index - starting index of the page
	 * @param pageLength - no of rows in the page
	 * @return - list of plan item with the given error code
	 * @throws Exception
	 */
	ApiResponse getErrorPlanInfo(Long planId, String errorCode, int index, int pageLength);

	/**
	 * <b>Getting the count of each error code for a plan</b>
	 * @param planId - required dispatch plan id
	 * @param loggedInUser - Logged in user
	 * @return - count of each error code of the plan
	 * @throws java.nio.file.AccessDeniedException
	 */
	ApiResponse getDispatchPlanErrorInfoCount(Long planId, ApplicationUser loggedInUser) throws AccessDeniedException;

	/**
	 * <b>Fetching the list of dispatch plan created with error</b>
	 * @param searchDto - required filter criteria {@link DispatchPlanFilterDto}
	 * @param loggedInUser - Logged in user
	 * @return - List of Dispatch plans which are created with errors
	 */
	ApiResponse getUserPendingDispatchPlans(ApplicationUser loggedInUser, DispatchPlanFilterDto searchDto);

	/**
	 * <b>Getting the dispatch plan details</b>
	 * @param searchDto - required filter criteria {@link DispatchPlanFilterDto}
	 * @param loggedInUser - Logged in user
	 * @return - list of dispatch plans which are created with out any errors
	 */
	ApiResponse getDispatchPlanInfo(DispatchPlanFilterDto searchDto, ApplicationUser loggedInUser);

	/**
	 * <b>Resolving the error</b>
	 * @param searchDto - required changed made in the dispatch plan
	 * @param loggedInUser - logged in user
	 * @return - remaining error count after resolving a perticular error
	 */
	ApiResponse resolvePlanErrors(DispatchPlanFilterDto searchDto, ApplicationUser loggedInUser);

	/**
	 * <b>Approve/Unapprove the dispatch plan</b>
	 * @param searchDto - required quantity to be approved or unapproved
	 * @param loggedInUser - logged in user
	 * @return - updated dispatch plan details
	 */
	ApiResponse approveDispatchPlanInfo(DispatchPlanFilterDto searchDto, ApplicationUser loggedInUser);

	/**
	 * <b>Deleting the dispatch plan details</b>
	 * @param ids - ids of the plans to be deleted
	 * @param applicationUser - logged in user
	 * @return - success/error message
	 */
	ApiResponse deleteDispatchPlanInfo(List<Long> ids, ApplicationUser applicationUser);

	/**
	 * <b>Calling the create dispatch plan procedure after resolving all the errors</b>
	 * @param planId - id of the plan to be created
	 * @param loggedInUser - - logged in user
	 * @return - success message along with plan id/error message
	 */
	ApiResponse createDispatchPlan(Long planId, ApplicationUser loggedInUser);

	/**
	 * <b>Deleting the error plan record</b>
	 * @param planItemId - id of the plan to be deleted
	 * @param errorCode - error of the plan to be deleted
	 * @param status - required boolean value to check if ther are multiple error line with the error code for the plan
	 * @param loggedInUser - logged in user
	 * @return - remaining error count after deleting a perticular error
	 */
	ApiResponse deleteDispatchPlanTempInfo(Long planItemId, String errorCode, ApplicationUser loggedInUser, boolean status);

	/**
	 *<b>Modify diapatch plan</b>
	 * @param dispatchPlanItemDto - required changed to be made in the dispatch plan {@link DispatchPlanItemDto}
	 * @param applicationUser - logged in user
	 * @return - the updated dispatch plan details
	 */
	ApiResponse modifyDispatchPlan(List<DispatchPlanItemDto> dispatchPlanItemDto,ApplicationUser applicationUser);

}
