package com.api.apollo.atom.service;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.ops.*;
import com.api.apollo.atom.dto.planner.DispatchPlanFilterDto;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.ops.Loadslip;
import com.itextpdf.text.DocumentException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.List;

public interface OpsService {

  ApiResponse getDispatchPlanInfo(DispatchPlanFilterDto searchDto, ApplicationUser loggedInUser);

  ApiResponse createIndents(IndentDto indentDto, ApplicationUser loggedInUser);

  ApiResponse generateIndentPdf(IndentCommunicationDto indentCommDto, ApplicationUser loggedInUser) throws DocumentException;

  ApiResponse modifyIndents(IndentInfoDto indentInfoDto,ApplicationUser applicationUser);

  ApiResponse sendIndentMail(IndentCommunicationDto indentCommDto, ApplicationUser loggedInUser);

  ApiResponse getIndents(IndentFilterDto indentFilterDto, ApplicationUser loggedInUser);

  ApiResponse reportTruck(TruckReportDto indentReportDto, boolean isGateSecurityScreen, ApplicationUser loggedInUser);

  ApiResponse getIndentTruckReportInfo(String indentId, ApplicationUser loggedInUser);

  ApiResponse getOpenDestinations(ApplicationUser applicationUser);

  ApiResponse getPlantReportedTruckInfo(TruckReportFilterDto truckReportFilterDto, ApplicationUser loggedInUser);

  ApiResponse getLoadSlipPlans(String destLoc, String loadslipId, String itemCategory, ApplicationUser loggedInUser);

  ApiResponse getLoadSlipUtilizations(LsUtilizationDto lsUtilizationDto, ApplicationUser applicationUser);

    ApiResponse saveLoadSlip(LoadslipDraftDto loadslipDraftDto, ApplicationUser loggedInUser) throws SQLException;

    ApiResponse updateTruckWeight(TruckReportDto reportDto, ApplicationUser loggedInUser);

  ApiResponse getTruckStatus(String reportLoc, ApplicationUser loggedInUser);

  ApiResponse getTrucksData(TruckReportFilterDto truckReportFilterDto ,ApplicationUser loggedInUser);

  ApiResponse viewLoadslips(ApplicationUser loggedInUser, LoadslipFilterDto loadslipFilterDto);

  ApiResponse viewLoadslipsMovement(ApplicationUser loggedInUser, LoadslipFilterDto loadslipFilterDto);

  ApiResponse updateLoadslipEvent(LoadslpEventDto loadslpEventDto, ApplicationUser loggedInUser, HttpServletRequest request);

  ApiResponse getLoadslipDetails(String loadslipID, ApplicationUser loggedInUser);

  ByteArrayOutputStream getLoadslipPdf(Loadslip loadslip);

  ByteArrayOutputStream getLoadslipExcel(Loadslip loadslip);

  ByteArrayOutputStream getLoadReceiptPdf(Loadslip loadslip);

  ApiResponse getIntransitTrucks(ApplicationUser loggedInUser, String sourceLocation);

  ApiResponse sendToSAPForBarcodeScan(String loadslipID, ApplicationUser applicationUserFromAuthentication);

  ApiResponse rollBackLoadslip(String loadslipID, ApplicationUser applicationUserFromAuthentication);

    ApiResponse saveMultiLoadslip(LoadslipDraftDto loadslipDraftDto, ApplicationUser applicationUser) throws SQLException;

  ApiResponse getExportDestinations(ApplicationUser applicationUser);

  ApiResponse getExportInvoice(String destLocation, String loadslipId, ApplicationUser applicationUser);

  ApiResponse getExportInvoiceWithType(Constants.DelInvType type, String destLocation, String loadslipId, ApplicationUser applicationUser);

  ApiResponse getExportInvoiceItem(List<String> invoiceList, ApplicationUser loggedInUser);

  ApiResponse shareTruck(String shipmentId, String pickupLoc, ApplicationUser loggedInUser);

  ApiResponse getAllPlantCodes(ApplicationUser loggedInUser);

  ApiResponse getSharedTrucksInfo(ApplicationUser loggedInUser);

  ApiResponse deleteSOSTO(String loadslipId, ApplicationUser loggedInUser);

  void saveShipmentStopAndTruckDestination(String loadslipId,ApplicationUser loggedInUser);

  ApiResponse updatePlanDispatchQtyFromLoadedQty(StosoLoadslipBean loadslipBean) throws Exception;

  ApiResponse sendActualShipment(String shipmentId);

  ApiResponse addCommentsToLoadslip(LoadslipMetaData loadslipMetaData, ApplicationUser applicationUser);

  ApiResponse addCommentsToTruckReport(TruckReportDto truckReportDto,ApplicationUser applicationUser);

  ApiResponse getReportedDriverDetails(String truckNumber, String indentId);

  ApiResponse getLoadslipInvoiceData(String loadslipId, ApplicationUser loggedInUser);

  ApiResponse updateTruckInventoryDetails(TruckInventoryDto inventoryDto,ApplicationUser applicationUser);


  ApiResponse fetchTruckRejectionCodes(ApplicationUser loggedInUser);

  ApiResponse viewTrucksMovement(ApplicationUser loggedInUser,TruckReportFilterDto truckReportFilterDto);

  ApiResponse exportShipmentTractingFile(MultipartFile multipartFile, ApplicationUser loggedInUser) throws Exception;


  ApiResponse fetchExportShipmentData(ExportShipmentTractingDto exportShipmentBean,ApplicationUser loggedInUser);

  ByteArrayOutputStream generateDispatchPlanExcel(DispatchPlanFilterDto searchDto, ApplicationUser applicationUser) throws  Exception;

  public ByteArrayOutputStream generateIndentsExcel(IndentFilterDto indentFilterDto, ApplicationUser applicationUser) throws Exception;

  public ApiResponse checkMultiLoadslipAllowed(String shipment, ApplicationUser applicationUser);

  ApiResponse getDashboardData(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser);

  ApiResponse getPriorityList(ApplicationUser applicationUser);

  ApiResponse getOpenPlansSourceLocs(ApplicationUser applicationUser);

  ApiResponse getLSCategoryList(ApplicationUser applicationUser);

  ApiResponse getClsData(ClsDetailsFilterDto filterDto, ApplicationUser loggedInUser);
}


