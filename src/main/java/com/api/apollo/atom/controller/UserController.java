package com.api.apollo.atom.controller;

import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.core.LoginBean;
import com.api.apollo.atom.dto.ops.StosoLoadslipBean;
import com.api.apollo.atom.repository.ops.LoadslipRepository;
import com.api.apollo.atom.service.OpsService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api("User Management")
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

	@Autowired
	private OpsService opsService;

	@Autowired
	private LoadslipRepository loadslipRepository;

	/**
	 * <b>To Login Into Application</b>
	 * @param login - User enters the email and password {@link LoginBean}
	 * @return - Logged in user details along with auth token
	 */

	@PostMapping(value = "/login")
	public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginBean login) {
		return new ResponseEntity<>(new ApiResponse(), HttpStatus.OK);
	}

	@PostMapping(value = "/update-dispatchQty")
	public ResponseEntity<ApiResponse> updatePlanDispatchQty(@RequestBody StosoLoadslipBean loadslipBean, HttpServletRequest httpServletRequest) throws Exception {
		return ResponseEntity.ok(opsService.updatePlanDispatchQtyFromLoadedQty(loadslipBean));
	}

}
