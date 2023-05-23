
package com.api.apollo.atom.controller;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.ops.*;
import com.api.apollo.atom.dto.planner.DispatchPlanFilterDto;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.ops.Loadslip;
import com.api.apollo.atom.repository.ops.LoadReceiptRepository;
import com.api.apollo.atom.repository.ops.LoadslipRepository;
import com.api.apollo.atom.repository.planner.DelInvHeaderRepository;
import com.api.apollo.atom.service.DashboardService;
import com.api.apollo.atom.service.ExportService;
import com.api.apollo.atom.service.OpsService;
import com.api.apollo.atom.service.RDCService;
import com.api.apollo.atom.util.Utility;
import com.itextpdf.text.DocumentException;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Api(value = "FGS & RDC - Operations Management")
@RestController
@RequestMapping("/api/v1/ops/")
@PreAuthorize("hasAnyAuthority('PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_GAT','JIT_PLN','DP_REP','L1_MGR','L2_MGR', 'FPL')")
public class OpsController {

  @Autowired
  LoadReceiptRepository loadReceiptRepository;

  @Autowired
  LoadslipRepository loadslipRepository;

  @Autowired
  OpsService opsService;

  @Autowired
  DelInvHeaderRepository delInvHeaderRepository;


  @Autowired
  private RDCService rdcService;

  @Autowired
  private ExportService exportService;

  @Autowired
  DashboardService dashboardService;


  @PostMapping(value = "plan-info")
  public ResponseEntity<ApiResponse> getDispatchPlanInfo(@RequestBody DispatchPlanFilterDto searchDto, Authentication authentication) {
    return ResponseEntity.ok(opsService.getDispatchPlanInfo(searchDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @PostMapping(value = "indent")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> createIndent(@RequestBody IndentDto indentDto, Authentication authentication) {
    return ResponseEntity.ok(opsService.createIndents(indentDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @PostMapping(value = "indents")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> getIndents(@RequestBody IndentFilterDto indentFilterDto, Authentication authentication) throws Exception {
    return ResponseEntity.ok(opsService.getIndents(indentFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @PostMapping(value = "indent-pdf")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> generateIndentPdf(@RequestBody IndentCommunicationDto indentCommDto, Authentication authentication) throws DocumentException {
    return ResponseEntity.ok(opsService.generateIndentPdf(indentCommDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @PostMapping(value = "mail-indentDetails")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> mailIndetDetails(@RequestBody IndentCommunicationDto indentCommDto, Authentication authentication) {
    return ResponseEntity.ok(opsService.sendIndentMail(indentCommDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @PostMapping(value = "modify-indent")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> modifyIndents(@RequestBody IndentInfoDto indentInfoDto, Authentication authentication) {
    return ResponseEntity.ok(opsService.modifyIndents(indentInfoDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @PostMapping(value = "indent-report")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> reportIndent(@RequestBody TruckReportDto reportDto, @RequestParam(name = "isGateSecurity", required = false) boolean isGateSecurityScreen, Authentication authentication) throws Exception {
    return ResponseEntity.ok(opsService.reportTruck(reportDto, isGateSecurityScreen, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * OPS will display all the indent truck report info to gate security by indent
   * id belongs to loggedInUser plant code.
   */
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  @GetMapping(value = "indent-report")
  public ResponseEntity<ApiResponse> getIndentReportInfo(@RequestParam(name = "indentId", required = true) String indentId, Authentication authentication) {
    return ResponseEntity.ok(opsService.getIndentTruckReportInfo(indentId, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * OPS will display screen to show a drop down listing all unique DEST_LOC from
   * DISPATCH_PLAN table where status = OPEN.
   */
  @GetMapping(value = "open-destinations")
  public ResponseEntity<ApiResponse> getOpenDestinations(Authentication authentication) {
    return ResponseEntity.ok(opsService.getOpenDestinations(Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * Fetching all trukcs info based on the LoggedIn User Role
   * This accessed by FGS OPERATIONS & FGS GATE SECURITY AND RDC OPERATIONS & RDC GATE roles
   */
  @PostMapping(value = "reported")
  @PreAuthorize("hasAnyAuthority('PLT_PLN','PLT_3PL','RDC_3PL','PLT_GAT','JIT_PLN', 'JIT_GAT','RDC_PLN','RDC_GAT')")
  public ResponseEntity<ApiResponse> getPlantTruckReportedInfo(@RequestBody TruckReportFilterDto truckReportFilterDto, Authentication authentication) {
    // For the User with Role of FGS Operations or FGS Gate Security
    return ResponseEntity.ok(opsService.getPlantReportedTruckInfo(truckReportFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * Fetching all Intransit trukcs info at RDC
   * This accessed by User with RDC_GATE role
   */
  @PostMapping(value = "intransit-trucks")
  @PreAuthorize("hasAnyAuthority('PLT_PLN','PLT_3PL','RDC_3PL','PLT_GAT','JIT_PLN', 'JIT_GAT','RDC_GAT','RDC_PLN','ABU_PLN')")
  public ResponseEntity<ApiResponse> getIntransitTrucks(@RequestBody TruckReportFilterDto truckReportFilterDto, Authentication authentication) {
    ApiResponse response = null;
    ApplicationUser loggedInUser = Utility.getApplicationUserFromAuthentication(authentication);
       /* if (loggedInUser.getRole().isRDCPlannerOrRDCGate()) {
            // For the User with Role of RDC Operations or RDC Gate Security
            response = rdcService.getIntransitTruckInfo(truckReportFilterDto, loggedInUser);
        }*/
    response = rdcService.getIntransitTruckInfo(truckReportFilterDto, loggedInUser);
    return ResponseEntity.ok(response);
  }

  @PostMapping(value = "trucks-info")
  @PreAuthorize("hasAnyAuthority('PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_GAT','JIT_PLN','ABU_PLN','DP_REP', 'L1_MGR', 'L2_MGR')")
  public ResponseEntity<ApiResponse> getTrucksData(@RequestBody TruckReportFilterDto truckReportFilterDto, Authentication authentication) {
    return ResponseEntity.ok(opsService.getTrucksData(truckReportFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * OPS will display list of all dispatch plans grouped up by material code
   * sorted in ascending order of PRIORITY. OPS will fetch the details from
   * DISPATCH_PLAN and DISPATCH_PLAN_BOM tables.
   * If loadslipId is available show all Plans for that destination excluding the plan items involved with given laodlsipId
   */
  @GetMapping(value = "load-slip-plans")
  public ResponseEntity<ApiResponse> getLoadSlipPlans(@RequestParam("destLoc") String destLoc, @RequestParam(value = "loadslipId", required = false) String loadslipId,@RequestParam(value = "itemCategory", required = false) String itemCategory, Authentication authentication) {
    return ResponseEntity.ok(opsService.getLoadSlipPlans(destLoc, loadslipId, itemCategory, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * OPS will display list of all dispatch plans grouped up by material code
   * sorted in ascending order of PRIORITY.
   */
  @PostMapping(value = "load-slip-utilization")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> getLoadSlipUtilizations(@RequestBody LsUtilizationDto lsUtilizationDto, Authentication authentication) {
    return ResponseEntity.ok(opsService.getLoadSlipUtilizations(lsUtilizationDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * Save Draft to save Load Slip Details
   */

  @PostMapping(value = "loadslip-draft")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> saveLoadSlip(@RequestBody LoadslipDraftDto loadslipDraftDto, Authentication authentication,
                                                  HttpServletResponse response) throws Exception {
    ApiResponse apiResponse = null;
    try {
      apiResponse = opsService.saveLoadSlip(loadslipDraftDto, Utility.getApplicationUserFromAuthentication(authentication));
    } catch (SQLException e) {
      e.printStackTrace();
      apiResponse = new ApiResponse(HttpStatus.EXPECTATION_FAILED, "Failed some business rules", new LoadSlipResponseDto(e.getMessage()));
      return ResponseEntity.ok(apiResponse);
    }

    if (apiResponse.getStatusCode() == 200) {
      LoadSlipResponseDto dto = (LoadSlipResponseDto) apiResponse.getData();
      if (dto.getErrorMsgs().isEmpty()) {
        opsService.saveShipmentStopAndTruckDestination(dto.getLoadSlipId(), Utility.getApplicationUserFromAuthentication(authentication));
      }
    }
    return ResponseEntity.ok(apiResponse);
  }

  /**
   * @return ApiResponse
   * @author Rajender Ratnam
   * @apiSpec loadslip pdf or excel
   */
  @GetMapping(value = "print-loadslip")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity getLoadslipPdf(@RequestParam(name = "loadslipId") String loadslipId, @RequestParam(name = "excel",required = false) boolean isExcel, HttpServletResponse response) {
    Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(loadslipId);
    String validationMsg = Utility.validateExistingLoadslip(optionalLoadslip);
    if (validationMsg != null) {
      return ResponseEntity.ok(new ApiResponse(HttpStatus.NOT_FOUND, String.format(validationMsg + " with the loadslip Id %s", loadslipId)));
    }
    if(isExcel){
      final byte[] array = opsService.getLoadslipExcel(optionalLoadslip.get()).toByteArray();
      final String mimeType = "application/vnd.ms-xlsx";
      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.valueOf(mimeType));
      response.setHeader("Content-Disposition", "download; filename=" +optionalLoadslip.get().getLoadslipId()+".xlsx");
      return new ResponseEntity<>(array,headers, HttpStatus.CREATED);
    }
    return ResponseEntity.ok(new ApiResponse(HttpStatus.OK, "Loadslip printed successfully", opsService.getLoadslipPdf(optionalLoadslip.get()).toByteArray()));
  }


  /**
   * View Loadslips which are saved as Draft
   */
  @PostMapping(value = "view-loadslips")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> getDraftedLoadslips(@RequestBody LoadslipFilterDto loadslipFilterDto,
                                                         Authentication authentication) throws Exception {
    return ResponseEntity.ok(opsService.viewLoadslips(Utility.getApplicationUserFromAuthentication(authentication),
        loadslipFilterDto));
  }

  @PostMapping(value = "view-loadslips-movement")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> getDraftedLoadslipsMovement(@RequestBody LoadslipFilterDto loadslipFilterDto,
                                                         Authentication authentication) throws Exception {
    return ResponseEntity.ok(opsService.viewLoadslipsMovement(Utility.getApplicationUserFromAuthentication(authentication),
        loadslipFilterDto));
  }

  /**
   * View Loadslips which are saved as Draft
   */
  @GetMapping(value = "view-loadslip-items/{loadslipID}")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','JIT_PLN', 'JIT_GAT','RDC_GAT','ABU_PLN')")
  public ResponseEntity<ApiResponse> getLoadslipDetails(@PathVariable(value = "loadslipID") String loadslipID, Authentication authentication) throws Exception {
    return ResponseEntity.ok(opsService.getLoadslipDetails(loadslipID, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * Roll back of loadslip is like a cancellation. This can happen pre-loading, post-loading, post sending to SAP.
   */
  @PostMapping(value = "cancel-loadlsip/{loadslipID}")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> rollBackLoadslip(@PathVariable(value = "loadslipID") String loadslipID, Authentication authentication) throws Exception {
    return ResponseEntity.ok(opsService.rollBackLoadslip(loadslipID, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * Update Truck GateIn Weight / GateOut Weight
   */
  @PostMapping(value = "report-truck-weight")
  @PreAuthorize("hasAnyAuthority('PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT','RDC_PLN')")
  public ResponseEntity<ApiResponse> updateTruckWeight(@RequestBody TruckReportDto reportDto,
                                                       Authentication authentication) throws Exception {
    return ResponseEntity.ok(
        opsService.updateTruckWeight(reportDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @PreAuthorize("hasAnyAuthority('PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','JIT_PLN', 'JIT_GAT')")
  @GetMapping(value = "trucks-status")
  public ResponseEntity<ApiResponse> getTrucksStatus(@RequestParam(value = "reportLoc", required = false) String reportLoc, Authentication authentication) {
    return ResponseEntity
        .ok(opsService.getTruckStatus(reportLoc, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @PostMapping(value = "loadslip-event")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> updateLoadslipEvent(@Valid @RequestBody LoadslpEventDto loadslpEventDto,
                                                         Authentication authentication, HttpServletRequest request) {
    return ResponseEntity.ok(opsService.updateLoadslipEvent(loadslpEventDto,
        Utility.getApplicationUserFromAuthentication(authentication), request));
  }


  @GetMapping(value = "intransit-trucks")
  public ResponseEntity<ApiResponse> getIntransitTrucks(Authentication authentication, String sourceLocation) {
    return ResponseEntity
        .ok(opsService.getTruckStatus(null, Utility.getApplicationUserFromAuthentication(authentication)));
  }

//  @GetMapping(value = "generate_indentPdf")
//  public HttpEntity generateIndentPdf(HttpServletResponse response) throws Exception {
//    ByteArrayOutputStream baos = opsService.getIndentPdf();
//    final HttpHeaders headers = new HttpHeaders();
//    headers.setContentType(MediaType.valueOf("application/pdf"));
//    response.setHeader("Content-Disposition", "attachment; filename=sample.pdf");
//    return new HttpEntity<>(baos.toByteArray(), headers);
//  }

  @PostMapping(value = "sap-barcode-scan/{loadslipID}")
  public ResponseEntity<ApiResponse> sendToSAPForBarcodeScan(@PathVariable(value = "loadslipID") String loadslipID,
                                                             Authentication authentication) {
    return ResponseEntity.ok(opsService.sendToSAPForBarcodeScan(loadslipID,
        Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @PostMapping(value = "intransit-truck-report")
  @PreAuthorize("hasAnyAuthority('PLT_PLN','PLT_3PL','RDC_3PL','PLT_GAT','JIT_PLN', 'JIT_GAT','RDC_GAT','RDC_PLN','ABU_PLN')")
  public ResponseEntity<ApiResponse> reportIntransitTruck(@RequestBody TruckReportDto reportDto, Authentication authentication) throws Exception {
    return ResponseEntity.ok(rdcService.reportIntransitTruck(reportDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * Save Draft to save Load Slip Details
   */
  @PostMapping(value = "multi-loadslip-draft")
  @PreAuthorize("hasAnyAuthority('PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> saveMultiLoadSlip(@RequestBody LoadslipDraftDto loadslipDraftDto, Authentication authentication,
                                                       HttpServletResponse response) {
    ApiResponse apiResponse = null;
    try {
      apiResponse = opsService.saveMultiLoadslip(loadslipDraftDto, Utility.getApplicationUserFromAuthentication(authentication));
    } catch (SQLException e) {
      e.printStackTrace();
      apiResponse = new ApiResponse(HttpStatus.EXPECTATION_FAILED, "Failed some business rules", new LoadSlipResponseDto(e.getMessage()));
      return ResponseEntity.ok(apiResponse);
    }

    LoadSlipResponseDto dto = (LoadSlipResponseDto) apiResponse.getData();
    if (dto.getErrorMsgs().isEmpty()) {
      opsService.saveShipmentStopAndTruckDestination(dto.getLoadSlipId(), Utility.getApplicationUserFromAuthentication(authentication));
    }
    return ResponseEntity.ok(apiResponse);
  }

  /**
   * @param shipmentId
   * @param pickupLoc
   * @param authentication
   * @return FGS will then click on “Share Truck” link. This link will display list of plant codes.
   * FGS will select the plant code with which the truck is to be shared. OPS will display the shipment and loading slip to the FGS of the shared plant.
   */
  @PostMapping(value = "share-truck")
  @PreAuthorize("hasAnyAuthority('PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> shareTruck(@RequestParam(value = "shipmentId") String shipmentId,
                                                @RequestParam(value = "pickupLoc") String pickupLoc, Authentication authentication) {
    return ResponseEntity.ok(opsService.shareTruck(shipmentId, pickupLoc, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * @param authentication
   * @return list of plant codes except current loggedIn User plant to select the plant code with which the truck is to be shared.
   */
  @GetMapping(value = "pickup-locations")
  @PreAuthorize("hasAnyAuthority('PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> getAllPlantsLocations(Authentication authentication) {
    return ResponseEntity.ok(opsService.getAllPlantCodes(Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * @return Fetch All shared trucks info for the current loggedIn user plant
   */
  @GetMapping(value = "shared-trucks-info")
  @PreAuthorize("hasAnyAuthority('PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> getSharedTrucksInfo(Authentication authentication) {
    return ResponseEntity.ok(opsService.getSharedTrucksInfo(Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * @param authentication Authorization
   * @return ApiResponse
   * @author Mitesh
   * @apiSpec EXPOERT OPS will display screen to show a drop down listing all unique Destination from loggedInSource
   */
  @GetMapping(value = "open-export-destinations")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> getExportDestination(Authentication authentication) {


    return ResponseEntity.ok(opsService.getExportDestinations(Utility.getApplicationUserFromAuthentication(authentication)));
  }


  /**
   * @param authentication Authorization
   * @param loadslipId     Loadslip Id
   * @return ApiResponse
   * @author Mitesh
   * @apiSpec EXPOERT OPS will display screen to show a drop down listing all unique Container from loggedInSource
   */
  @GetMapping(value = "open-export-containers")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> getExportContainer(@RequestParam(value = "type", required = false) Constants.DelInvType type, @RequestParam(value = "loadslipId", required = false) String loadslipId, Authentication authentication) {
    return ResponseEntity.ok(exportService.getExportContainer(type, loadslipId, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * OPS will display list of all dispatch plans grouped up by material code
   * sorted in ascending order of PRIORITY.
   */
  @PostMapping(value = "load-slip-utilization-export")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> getLoadSlipUtilizationsExport(@RequestBody LsUtilizationDto lsUtilizationDto, Authentication authentication) {
    return ResponseEntity.ok(exportService.getLoadSlipUtilizationsAndExportData(Utility.getApplicationUserFromAuthentication(authentication), lsUtilizationDto));
  }


  /**
   * @param destLoc        Destinations Location
   * @param authentication Authorization
   * @return ApiResponse
   * @author Mitesh
   * @apiSpec EXPOERT OPS will display screen to show a drop down listing all unique INVOICES for
   */
  @GetMapping(value = "open-invoices")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> getExportInvoices(@RequestParam(value = "destLoc") String destLoc, @RequestParam(value = "loadslipId", required = false) String loadslipId, Authentication authentication) {
    return ResponseEntity.ok(opsService.getExportInvoice(destLoc, loadslipId, Utility.getApplicationUserFromAuthentication(authentication)));
  }


  /**
   * @param invoiceListMap Destinations Location
   * @param authentication Authorization
   * @return ApiResponse
   * @author Mitesh
   * @apiSpec Display item list of items acoording to invoice number
   */
  @PostMapping(value = "export-load-slip-items")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> getExportsLoadSlipItems(@RequestBody Map<String, List<String>> invoiceListMap, Authentication authentication) {
    //System.out.println(invoiceList);
    List<String> invoiceList = invoiceListMap.getOrDefault("invoiceList", new ArrayList<String>());

    return ResponseEntity.ok(opsService.getExportInvoiceItem(invoiceList, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * @param
   * @return
   * @author Mitesh
   * @implNote loadSlip creation for fgs export
   */
  @PostMapping("loadslip-draft-export")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT')")
  public ResponseEntity<ApiResponse> saveExportLoadSlip(@RequestBody LoadslipDraftDto loadslipDraftDto, Authentication authentication,
                                                        HttpServletResponse response) throws Exception {
    return null;

  }

  @GetMapping(value = "open-loadslipIds")
  @PreAuthorize("hasAnyAuthority('PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT','ABU_PLN','DP_REP', 'L1_MGR', 'L2_MGR')")
  public ResponseEntity<ApiResponse> openLoadslipDetails(@RequestParam(value = "shipmentId", required = true) String shipmentId, Authentication authentication) {
    return ResponseEntity.ok(rdcService.getLoadslipsWithShipment(shipmentId, Utility.getApplicationUserFromAuthentication(authentication)));
  }


  @GetMapping(value = "delete-sosto")
  public ResponseEntity<ApiResponse> deleteSOSTO(@RequestParam(value = "loadslipId", required = true) String loadslipId, Authentication authentication) {
    return ResponseEntity.ok(opsService.deleteSOSTO(loadslipId, Utility.getApplicationUserFromAuthentication(authentication)));
  }


  /**
   * Inspirage Tech - View Load Receipts
   */
  @GetMapping(value = "generate-lrnum")
  public ResponseEntity generateLrNumber(@RequestParam(name = "loadslipId") String loadslipId) {
    String loadReceiptNo = loadReceiptRepository.getLRNumber(loadslipId);
    Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(loadslipId);
    String validationMsg = Utility.validateExistingLoadslip(optionalLoadslip);
    if (validationMsg != null) {
      return ResponseEntity.ok(new ApiResponse(HttpStatus.NOT_FOUND, String.format(validationMsg + " with the loadslip Id %s", loadslipId)));
    }
    return ResponseEntity.ok(new ApiResponse(HttpStatus.OK, "Load Receipt number generated successfully", null, loadReceiptNo));
  }

  /**
   * Inspirage Tech - View Load Receipts
   */
  @GetMapping(value = "print-loadreceipt")
  public ResponseEntity getLoadReceiptPdf(@RequestParam(name = "loadslipId") String loadslipId) {
    //String loadReceiptNo = loadReceiptRepository.getLRNumber(loadslipId);
    Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(loadslipId);
    String validationMsg = Utility.validateExistingLoadslip(optionalLoadslip);
    String loadReceiptNo = optionalLoadslip.get().getLrNum();
    if (loadReceiptNo == null || loadReceiptNo == "") {
      return ResponseEntity.ok(new ApiResponse(HttpStatus.NOT_FOUND, "Load Receipt Number is not generated for this load slip id.", loadslipId));
    }
    if (validationMsg != null) {
      return ResponseEntity.ok(new ApiResponse(HttpStatus.NOT_FOUND, String.format(validationMsg + " with the loadslip Id %s", loadslipId)));
    }
    LoadSlipResponseDto loadSlipResponseDto = new LoadSlipResponseDto(loadslipId,
        opsService.getLoadReceiptPdf(optionalLoadslip.get()).toByteArray());
    return ResponseEntity.ok(new ApiResponse(HttpStatus.OK, "Load Receipt printed successfully", loadSlipResponseDto.getLoadslip(), loadReceiptNo));
  }

  /**
   * Inspirage Tech - getElrFlag
   */
  //returning flag while getting loadslip details i.e., view-loadslip-items API
 /* @GetMapping(value = "get-elr-flag")
  public ResponseEntity getElrFlag(@RequestParam(name = "loadslipId") String loadslipId) {

    Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(loadslipId);
    String validationMsg = Utility.validateExistingLoadslip(optionalLoadslip);
    if (validationMsg != null) {
      return ResponseEntity.ok(new ApiResponse(HttpStatus.NOT_FOUND, String.format(validationMsg + " with the loadslip Id %s", loadslipId)));
     }
     String  vElrFlag = loadReceiptRepository.getElrFlag(loadslipId);
      return ResponseEntity.ok(new ApiResponse(HttpStatus.OK, "Elr Flag value", vElrFlag != null ? vElrFlag :"N",optionalLoadslip.get().getLrNum()));
    }*/

  @PostMapping("/add-comments-to-loadslip")
  ResponseEntity<ApiResponse> addCommentsToLoadslip(@RequestBody LoadslipMetaData loadslipMetaData, Authentication authentication) {
    return ResponseEntity.ok(opsService.addCommentsToLoadslip(loadslipMetaData, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @GetMapping("/reported-driver-details")
  ResponseEntity<ApiResponse> getReportedDriverDetails(@RequestParam(value = "truckNumber", required = true) String truckNumber, @RequestParam(value = "indentId", required = true) String indentId) {
    return ResponseEntity.ok(opsService.getReportedDriverDetails(truckNumber, indentId));
  }

  @GetMapping(value = "loadslip-invoice-data")
  ResponseEntity<ApiResponse> loadslipInvoiceData(@RequestParam(name = "loadslipId") String loadslipId, Authentication authentication) {
    return ResponseEntity.ok(opsService.getLoadslipInvoiceData(loadslipId, Utility.getApplicationUserFromAuthentication(authentication)));
  }
  @PostMapping("/add-comments-to-truck-report")
  public ResponseEntity<ApiResponse> addCommentsToTruckReport(@RequestBody TruckReportDto truckReportDto, Authentication authentication) {
    return ResponseEntity.ok(opsService.addCommentsToTruckReport(truckReportDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * @param inventoryDto TruckInventoryDto
   * @param authentication Authorization
   * @return ApiResponse
   * @author Rajender Ratnam
   * @apiSpec  Edit truck details(driver name, mobile number, truck number,destination,truck type,container number, driver Licence num)
   */
  @PostMapping("/update-truck-inventory")
  public ResponseEntity<ApiResponse> updateTruckInventoryDetails(@RequestBody TruckInventoryDto inventoryDto, Authentication authentication){
    return ResponseEntity.ok(opsService.updateTruckInventoryDetails(inventoryDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }


  @GetMapping("/truck-rejection-codes")
  public ResponseEntity<ApiResponse> fetchTruckRejectionCodes(Authentication authentication){
    return ResponseEntity.ok(opsService.fetchTruckRejectionCodes(Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @PostMapping(value = "view-trucks-movement")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','JIT_PLN', 'JIT_GAT', 'RDC_GAT')")
  public ResponseEntity<ApiResponse> getViewTrucksMovement(@RequestBody TruckReportFilterDto  truckReportFilterDto,
                                                                 Authentication authentication) throws Exception {
    return ResponseEntity.ok(opsService.viewTrucksMovement(Utility.getApplicationUserFromAuthentication(authentication),
        truckReportFilterDto));
  }


  @PostMapping(value = "uploadExportShipmentFile")
  @PreAuthorize("hasAnyAuthority('FPL')")
  public ResponseEntity<ApiResponse> exportShipmentTracting(@RequestParam MultipartFile multipartFile, Authentication authentication) throws Exception {
    return ResponseEntity.ok(opsService.exportShipmentTractingFile(multipartFile,Utility.getApplicationUserFromAuthentication(authentication)));
  }


  @PostMapping(value = "view-ExportShipment-data")
  @PreAuthorize("hasAnyAuthority('FPL')")
  public ResponseEntity<ApiResponse> viewExportShipmentData(@RequestBody ExportShipmentTractingDto exportShipmentBean, Authentication authentication) {
    return ResponseEntity.ok(opsService.fetchExportShipmentData(exportShipmentBean,Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @PostMapping("/dipatch-plan-excel")
  public HttpEntity<byte[]> getDispatchExcel(@RequestBody DispatchPlanFilterDto filterDto, Authentication authentication, final HttpServletResponse response) throws  Exception{

    ByteArrayOutputStream bos = opsService.generateDispatchPlanExcel(filterDto, Utility.getApplicationUserFromAuthentication(authentication));
    final byte[] array = bos.toByteArray();
    final String mimeType = "application/vnd.ms-xlsx";
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf(mimeType));
    response.setHeader("Content-Disposition", "download; filename=dispatch-plan-data.xlsx");
    bos.flush();
    bos.close();
    return new HttpEntity<byte[]>(array, headers);

  }

  @PostMapping("/indents-excel")
  public HttpEntity<byte[]> getIndentExcel(@RequestBody IndentFilterDto filterDto, Authentication authentication, final HttpServletResponse response) throws  Exception{

    ByteArrayOutputStream bos = opsService.generateIndentsExcel(filterDto, Utility.getApplicationUserFromAuthentication(authentication));
    final byte[] array = bos.toByteArray();
    final String mimeType = "application/vnd.ms-xlsx";
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf(mimeType));
    response.setHeader("Content-Disposition", "download; filename=Indents-data.xlsx");
    bos.flush();
    bos.close();
    return new HttpEntity<byte[]>(array, headers);

  }

  @GetMapping("/is-multi-loadslip-allowed")
  public ApiResponse checkMultiLoadslipAllowed(@RequestParam(name = "shipmentId",required = true) String shipment, ApplicationUser applicationUser, Authentication authentication){
    return opsService.checkMultiLoadslipAllowed(shipment, Utility.getApplicationUserFromAuthentication(authentication));
  }

  @PostMapping("/get-dashboard-data")
  public ApiResponse getDashboardData(@RequestBody DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser, Authentication authentication){
    return opsService.getDashboardData(dashboardFilterDto, Utility.getApplicationUserFromAuthentication(authentication));
  }

  @GetMapping("/priority-list")
  ApiResponse getPriorityList(Authentication authentication){
    return opsService.getPriorityList(Utility.getApplicationUserFromAuthentication(authentication));
  }

  @GetMapping("/shipment-source-list")
  ApiResponse getShipmentSourceList(){
    return new ApiResponse(HttpStatus.OK, "", dashboardService.getShipmentSourceList());
  }

  @GetMapping("/open-plans-sources")
  public ApiResponse getOpenPlansSource(Authentication authentication){
    return this.opsService.getOpenPlansSourceLocs(Utility.getApplicationUserFromAuthentication(authentication));
  }

  @GetMapping("/loadslip-categories")
  public ApiResponse getLSCategoryList(Authentication authentication){
    return this.opsService.getLSCategoryList(Utility.getApplicationUserFromAuthentication(authentication));
  }

  @PostMapping("/get-cls-data")
  public ApiResponse getClsData(@RequestBody ClsDetailsFilterDto filterDto, Authentication authentication){
    return opsService.getClsData(filterDto, Utility.getApplicationUserFromAuthentication(authentication));
  }

}
