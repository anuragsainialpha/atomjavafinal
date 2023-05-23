package com.api.apollo.atom.controller;

import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.ops.*;
import com.api.apollo.atom.service.ExportService;
import com.api.apollo.atom.util.Utility;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Api(value = "FGS & RDC - Operations Management")
@RestController
@RequestMapping("/api/v1/export/")
@PreAuthorize("hasAnyAuthority('PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT')")
public class ExportController {

  @Autowired
  ExportService exportService;

  /**
   * Action - Create Intends for export
   *
   * @param indentDto
   * @param authentication
   * @return
   */
  @PostMapping(value = "indent")
  public ResponseEntity<ApiResponse> createIndent(@RequestBody IndentDto indentDto, Authentication authentication) {
    return ResponseEntity.ok(exportService.createIndents(indentDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * Action - Get Indents for export
   *
   * @param indentFilterDto
   * @param authentication
   * @return
   * @throws Exception
   */
  @PostMapping(value = "indents")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT')")
  public ResponseEntity<ApiResponse> getIndents(@RequestBody IndentFilterDto indentFilterDto, Authentication authentication) throws Exception {
    return ResponseEntity.ok(exportService.getIndents(indentFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * Action - Modify Indents
   *
   * @param indentInfoDto
   * @return
   */
  @PostMapping(value = "modify-indent")
  public ResponseEntity<ApiResponse> modifyIndents(@RequestBody IndentInfoDto indentInfoDto, Authentication authentication) {
    return ResponseEntity.ok(exportService.modifyIndents(indentInfoDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @PostMapping(value = "indent-report")
  public ResponseEntity<ApiResponse> reportIndent(@RequestBody TruckReportDto reportDto, Authentication authentication) throws Exception {
    return ResponseEntity.ok(exportService.reportTruck(reportDto, Utility.getApplicationUserFromAuthentication(authentication)));
  }

  /**
   * OPS will display all the indent truck report info to gate security by indent
   * id belongs to loggedInUser plant code.
   */
  @PreAuthorize("hasAuthority('PLT_GAT')")
  @GetMapping(value = "indent-report")
  public ResponseEntity<ApiResponse> getIndentReportInfo(@RequestParam(name = "indentId", required = true) String indentId, Authentication authentication) {
    return ResponseEntity.ok(exportService.getIndentTruckReportInfo(indentId, Utility.getApplicationUserFromAuthentication(authentication)));
  }


  /**
   * @param authentication Authorization
   * @param exportTrackerFilter Filter for tracker
   * @return ApiResponse
   * @author Mitesh
   * @apiSpec EXPOERT OPS will get export shipment tracker data.
   */
  @PostMapping(value = "export-trackers")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT','CHA')")
  public ResponseEntity<ApiResponse> getExportTrackers(@RequestBody ExportTrackerFilter exportTrackerFilter, Authentication authentication) {
    return ResponseEntity.ok(exportService.getExportTrackerData(Utility.getApplicationUserFromAuthentication(authentication),exportTrackerFilter));
  }


  /**
   * @param authentication Authorization
   * @return ApiResponse
   * @author Mitesh
   * @apiSpec EXPOERT OPS will UPDATE export shipment tracker data.
   */
  @PostMapping(value = "update-trackers")
  @PreAuthorize("hasAnyAuthority('DP_EXP','DP_OEM','DP_REP', 'L1_MGR', 'L2_MGR','PLT_PLN','PLT_3PL','RDC_PLN','RDC_3PL','PLT_GAT','RDC_GAT','JIT_PLN', 'JIT_GAT','CHA')")
  public ResponseEntity<ApiResponse> updateExportTrackers(@RequestBody ExportTrackerDto exportTrackerDto, Authentication authentication) {
    return ResponseEntity.ok(exportService.updateExportTrackerData(Utility.getApplicationUserFromAuthentication(authentication), exportTrackerDto));
  }

}
