package com.api.apollo.atom.service;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.master.*;
import com.api.apollo.atom.dto.ops.*;
import com.api.apollo.atom.dto.planner.DispatchPlanFilterDto;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.master.UmUserRole;
import com.api.apollo.atom.entity.master.*;
import com.api.apollo.atom.entity.ops.ExportShipment;
import com.api.apollo.atom.entity.ops.IndentSummary;
import com.api.apollo.atom.entity.ops.TruckReport;
import com.api.apollo.atom.entity.plan.DispatchPlanItemInfo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface FilterService {

  Page<DispatchPlanItemInfo> filterDispatchPlanItems(DispatchPlanFilterDto searchDto, ApplicationUser loggedInUser, Optional<MTLocation> mtLocation);

  Page<IndentSummary> filterIndents(IndentFilterDto indentFilterDto, ApplicationUser loggedInUser);

  Page<TruckReport> filterReportedTrucks(TruckReportFilterDto truckReportFilterDto, ApplicationUser loggedInUser, List<Constants.TruckReportStatus> truckReportStatuses, String trucksType, List<String> shipmentIds);

  List<LoadslipMetaData> filterLoadslips(ApplicationUser loggedInUser, List<String> statuses, LoadslipFilterDto loadslipFilterDto);

  List<LoadslipMetaData> noInvoiceLoadslipFilter(ApplicationUser applicationUser, List<String> statuses, LoadslipFilterDto loadslipDraftDto);

  Page<LoadslipMetaData> filterLoadslipsMovement(ApplicationUser loggedInUser, List<String> statuses, LoadslipFilterDto loadslipFilterDto);

  List<ExportTrackerDto> filterExportTracker(ApplicationUser loggedInUser, ExportTrackerFilter exportTrackerFilter);

  Page<TruckReport> filterTrucksInfo(TruckReportFilterDto truckReportFilterDto, List<Constants.TruckReportStatus> truckReportStatuses, List<String> incomingShipmentIds, ApplicationUser applicationUser, List<String> distinctShipmentIds);

  Page<TruckReport> filterIntransitTrucksInfo(TruckReportFilterDto truckReportFilterDto, List<Constants.TruckReportStatus> truckReportStatuses, ApplicationUser applicationUser);


  //Admin

  Page<MTItem> getMTItem(MtItemFilterDto itemFilterDto, String itemId, String description, String classification, String type, String group, String category, String tte, String loadfactor);

  Page<Freight> filterFreights(FreightFilterDto freightFilterDto, String transporterSapCode, String servprov, String sourceLoc, String sourceDesc, String destLoc, String destDesc, String truckType, String condition1,
                               String effectiveDate, String expiryDate, String ttDays, String baseFreight, String status, String fromInsertDate, String toInsertDate, String expiryDateFilterType, String fromExpiryDate, String toExpiryDate);
  Page<MTTransporter> filterTransporters(TransporterFilterDto transporterFilterDto);

  /*New Filter Service for DispatchPlans*/
  DispatchPlanFilterDto filterDisplanPlanInfo(DispatchPlanFilterDto filterDto, ApplicationUser applicationUser, Optional<MTLocation> mtLocation);


  Page<MTOeBom> getFilteredMTOeBom(MTOeBomFilterDto filterDto);
  Page<MTRepBom> getFilteredMTRepBom(MTRepBomFilterDto filterDto);
  Page<MTPlantItem> getFilteredMTPlantItem(MTPlantItemFilterDto filterDto);

  Page<LocationScan> getLocationScan(LocationScanFilterDto filterDto);
  Page<MtBatchCodes> getMtBatchCodes(MtBatchCodesFilterDto filterDto);
  Page<MtSapTruckType> getMtSapTruckType(MtSapTruckTypeFilterDto filterDto);
  Page<MtTruckType> getMtTruckType(MtTruckTypeFilterDto filterDto);
  Page<MtMaterialGroup> getMtMaterialGroup(MtMaterialGroupFilterDto filterDto);

  Page<OrderTypeLookup> getOrderTypeLookup(OrderTypeLookupFilterDto filterDto);
  Page<MTValve> getMTValve(MTValveFilterDto filterDto);
  Page<MtScac> getServProList(MtScacFilterDto filterDto);
  Page<MtElr> getMtElr(MtElrFilterDto filterDto);
  Page<MtExcessWaitingLocLimit> getMtExcessWaitingLocLimit(MtExcessWaitingLocLimitFilterDto filterDto);

  Page<MtExcessWaitingRepLimit> getMtExcessWaitingRepLimit(MtExcessWaitingRepLimitFilterDto filterDto);
  Page<MTLocationBayEntity> getMtLocationBay(MtLocationBayFilterDto filterDto);
  Page<MTLocation> getMtLocation(MtLocationFilterDto filterDto);
  //Page<MTLocation> getMtLocationExporter(MtLocationFilterDto filterDto);

  Page<TrucksMetaData> filterTrucksMovement(ApplicationUser loggedInUser,TruckReportFilterDto truckReportFilterDto);
  Page<CtOtmFreightBasis> getCtOtmFreightBasis(CtOtmFreightBasisFilterDto filterDto);
  Page<CtUom> getCtUom(CtUomFilterDto filterDto);
  Page<CtUomMap> getCtUomMap(CtUomMapFilterDto filterDto);
  Page<MtIncoterms> getMtIncoterms(MtIncotermsFilterDto filterDto);
  Page<MTTruck> getMtTruck(MtTruckFilterDto filterDto);
  Page<MtContact> getMtContact(MtContactFilterDto filterDto);
  Page<MTCustomer> getMtCustomer(MtCustomerFilterDto filterDto);
  Page<MtCustomerShipTo> getMtCustomerShipTo(MtCustomerShipToFilterDto filterDto);
  Page<UserEntity> getUser(UserFilterDto filterDto);

  int updatePassword(UserFilterDto itemFilterDto);

  Page<UmUserAssociationEntity> getUserAssociation(UserAssociationFilterDto filterDto);
  Page<UmUserRole> getUserRole(UserRoleFilterDto filterDto);

  Page<ExportShipment> filterExportShipmentData(ExportShipmentTractingDto exportShipmentBean);



  List<String> getServPros();
  List<String> getLocationIdsList();
  List<String> getLocationClasses();
  List<String> getUserIdsList();
  List<String> getUserRoleIdsList();
  List<String> getDistinctScmGroupList();
  List<String> getPaasTruckTypeList();
  List<String> getTruckTypeList();
  List<String> getVariantsList();
  List<String> getItemGroupsList();
  List<String> getbatchCategoryList();
  List<String> getbatchCodesForValves();
  List<String> getItemIdList();

  Page<MTTruckDedicated> getFilteredMTTruckDedicated(TruckDedicatedFilterDto truckDedicatedFilterDto, ApplicationUser applicationUser);

  Page<MTCustomer> filterMTCustomer(MtCustomerFilterDto mtCustomerFilterDto, ApplicationUser applicationUser);

  ApiResponse getClsData(ClsDetailsFilterDto filterDto, ApplicationUser loggedInUser);
}
