package com.api.apollo.atom.service;

import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.master.*;
import com.api.apollo.atom.dto.ops.LsUtilizationDto;
import com.api.apollo.atom.dto.ops.TruckReportDto;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.master.GpsProviderInfo;
import com.api.apollo.atom.entity.master.MTTruck;
import com.api.apollo.atom.entity.master.MTTruckTypeInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface MasterDataService {

  ApiResponse searchMasterDataInfo(String destination,String description, String materialCode, String batchCode, String materialGrp,
                                   String truckType, String truckDescription, String transporter, String materialDesc, String gpsProvider, ApplicationUser loggedInUser, int index,
                                   String destCountry, String pod, boolean isIntransitTruck, boolean forIndent);

  MTTruck createTruck(TruckReportDto reportDto, String transporter, ApplicationUser loggedInUser, MTTruckTypeInfo truckType, GpsProviderInfo gpsInfo,
                      Optional<MTTruck> optionalTruck);

  ApiResponse getPlantBayInfo(ApplicationUser loggedInUser);

  ApiResponse getVariants();

  ApiResponse getTruckTypes();

  ApiResponse getTruckTypesBySourceAndDest(String sourceLoc, String destLoc);

  ApiResponse transportersTocreateIndnet(LsUtilizationDto lsUtilizationDto);

  ApiResponse getBatchCodesmasterData();

  ApiResponse getMTMatrialGroupscmGroupMasterData();
  //ApiResponse getItemCategoryMasterData();


  //Admin

//  ApiResponse searchMasterDataInfo(String destination, String materialCode, String batchCode, String materialGrp,
//                                   String truckType, String transporter, String materialDesc, String gpsProvider, String itemId
//      ,String itemClassification,String itemType,String itemGroup ,String itemCategory
//      ,ApplicationUser loggedInUser, int index);



  ApiResponse getMTItem(MtItemFilterDto itemFilterDto);
  ApiResponse uploadmtitem(MultipartFile reapExcelDataFile, ApplicationUser loggedInUser)throws Exception;
  ApiResponse updateMtitemData(MtItemFilterDto mtItemDto, ApplicationUser loggedInUser)throws Exception;

  ApiResponse getFreightsInfo(FreightFilterDto freightFilterDto);
  ApiResponse uploadFreightFile(MultipartFile reapExcelDataFile, ApplicationUser loggedInUser) throws Exception;
  ApiResponse uploadFreightFileAsJson(FreightFilterDto freightFilterDto,ApplicationUser loggedInUser) throws Exception;
  ApiResponse approveFreightJson(FreightFilterDto freightFilterDto,ApplicationUser loggedInUser) throws Exception;

  ApiResponse getLocationScan(LocationScanFilterDto filterDto);
  ApiResponse addOrUploadLocationScan(LocationScanFilterDto itemFilterDto,ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateLocationScan(LocationScanFilterDto itemFilterDto,ApplicationUser loggedInUser) throws Exception;

  ApiResponse getMTOeBomInfo(MTOeBomFilterDto filterDto);
  ApiResponse getMTRepBomInfo(MTRepBomFilterDto filterDto);
  ApiResponse getMTPlantItemInfo(MTPlantItemFilterDto filterDto);

  ApiResponse getMtBatchCodes(MtBatchCodesFilterDto filterDto);
  ApiResponse addOrUploadMtBatchCodes(MtBatchCodesFilterDto itemFilterDto,ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateMtBatchCodes(MtBatchCodesFilterDto itemFilterDto,ApplicationUser loggedInUser) throws Exception;

  ApiResponse getMtSapTruckType(MtSapTruckTypeFilterDto filterDto);
  ApiResponse addOrUploadMtSapTruckType(MtSapTruckTypeFilterDto itemFilterDto,ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateMtSapTruckType(MtSapTruckTypeFilterDto itemFilterDto,ApplicationUser loggedInUser) throws Exception;

  ApiResponse getMtTruckType(MtTruckTypeFilterDto filterDto);
  ApiResponse addOrUploadMtTruckType(MtTruckTypeFilterDto itemFilterDto,ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateMtTruckType(MtTruckTypeFilterDto itemFilterDto,ApplicationUser loggedInUser) throws Exception;

  ApiResponse getMtMaterialGroup(MtMaterialGroupFilterDto filterDto);
  ApiResponse uploadMtMaterialGroup(MtMaterialGroupFilterDto itemFilterDto,ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateMtMaterialGroup(MtMaterialGroupFilterDto itemFilterDto,ApplicationUser loggedInUser) throws Exception;

  ApiResponse getOrderTypeLookup(OrderTypeLookupFilterDto filterDto);
  ApiResponse uploadOrderTypeLookup(OrderTypeLookupFilterDto itemFilterDto,ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateOrderTypeLookup(OrderTypeLookupFilterDto itemFilterDto,ApplicationUser loggedInUser) throws Exception;

  ApiResponse getMtValve(MTValveFilterDto filterDto);
  ApiResponse uploadMtValve(MTValveFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateMtValve(MTValveFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;



  ApiResponse getTransportersInfo(TransporterFilterDto transporterFilterDto);
  ApiResponse updateTransporter(TransporterFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;

  ApiResponse getMtElr(MtElrFilterDto filterDto);
  ApiResponse uploadMtElr(MtElrFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateMtElr(MtElrFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;

  ApiResponse getMtExcessWaitingLocLimit(MtExcessWaitingLocLimitFilterDto filterDto);
  ApiResponse uploadMtExcessWaitingLocLimit(MtExcessWaitingLocLimitFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateMtExcessWaitingLocLimit(MtExcessWaitingLocLimitFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;

  ApiResponse getMtExcessWaitingRepLimit(MtExcessWaitingRepLimitFilterDto filterDto);
  ApiResponse uploadMtExcessWaitingRepLimit(MtExcessWaitingRepLimitFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateMtExcessWaitingRepLimit(MtExcessWaitingRepLimitFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;

  ApiResponse getMtLocationBay(MtLocationBayFilterDto filterDto);
  ApiResponse uploadMtLocationBay(MtLocationBayFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse deleteMtLocationBay(MtLocationBayFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;

  ApiResponse getMtLocation(MtLocationFilterDto filterDto);
  ApiResponse uploadMtLocation(MtLocationFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateMtLocation(MtLocationFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;

  ApiResponse getDistinctMarketSegments(ApplicationUser loggedInUser)throws Exception;
  ApiResponse getCtOtmFreightBasis(CtOtmFreightBasisFilterDto filterDto);
  ApiResponse uploadCtOtmFreightBasis(CtOtmFreightBasisFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateCtOtmFreightBasis(CtOtmFreightBasisFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse getCtUom(CtUomFilterDto filterDto);
  ApiResponse uploadCtUom(CtUomFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateCtUom(CtUomFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse getCtUomMap(CtUomMapFilterDto filterDto);
  ApiResponse uploadCtUomMap(CtUomMapFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateCtUomMap(CtUomMapFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse getMtIncoterms(MtIncotermsFilterDto filterDto);
  ApiResponse uploadMtIncoterms(MtIncotermsFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateMtIncoterms(MtIncotermsFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse getMtTruck(MtTruckFilterDto filterDto);
  ApiResponse uploadMtTruck(MtTruckFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateMtTruck(MtTruckFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse getMtContact(MtContactFilterDto filterDto);
  ApiResponse uploadMtContact(MtContactFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateMtContact(MtContactFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse getMtCustomer(MtCustomerFilterDto filterDto);
  ApiResponse uploadMtCustomer(MtCustomerFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateMtCustomer(MtCustomerFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;

  ApiResponse getMtCustomerShipTo(MtCustomerShipToFilterDto filterDto);
  ApiResponse uploadMtCustomerShipTo(MtCustomerShipToFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateMtCustomerShipTo(MtCustomerShipToFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;

  ApiResponse getUser(UserFilterDto filterDto);
  ApiResponse uploadUser(UserFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateUser(UserFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updatePassword(UserFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;

  ApiResponse getUserAssociation(UserAssociationFilterDto filterDto);
  ApiResponse uploadUserAssociation(UserAssociationFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateUserAssociation(UserAssociationFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;

  ApiResponse getUserRole(UserRoleFilterDto filterDto);
  ApiResponse uploadUserRole(UserRoleFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse updateUserRole(UserRoleFilterDto itemFilterDto, ApplicationUser loggedInUser) throws Exception;
  ApiResponse getTruckStatusLocs(ApplicationUser applicationUserFromAuthentication);

  ApiResponse getServProList(MtScacFilterDto filterDto);
  ApiResponse addServProv(MtScacFilterDto filterDto, ApplicationUser loggedInUser) throws Exception;

  ApiResponse getServPros();
  ApiResponse getLocationIdsList();
  ApiResponse getLocationClasses();
  ApiResponse getUserIdsList();
  ApiResponse getUserRoleIdsList();
  ApiResponse getDistinctScmGroupList();
  ApiResponse getTruckTypeList();
  ApiResponse getPaasTruckTypeList();
  ApiResponse getVariantsList();
  ApiResponse getItemGroupsList();
  ApiResponse getbatchCategoryList();
  ApiResponse getbatchCodesForValves();
  ApiResponse getItemIdList();

  public ApiResponse checkMaterialCodeIsTyre(String materialCode);
  public ApiResponse getBatchCodePrefixes();

  ApiResponse deleteFreightData(Double rowId, ApplicationUser applicationUser);

  ApiResponse getMTTruckDedicatedData(TruckDedicatedFilterDto  truckDedicatedFilterDto, ApplicationUser applicationUser);

  ApiResponse saveMTTruckDedicated(MTTruckDedicatedDto mtTruckDedicatedDto, ApplicationUser applicationUser);

  ApiResponse deleteMTTruckDedicatedRecord(Long rowId, ApplicationUser applicationUser);

  ApiResponse updateFreightDetails(FreightInfoDto freightInfoDto, ApplicationUser applicationUser);

  ApiResponse getMtCustomerDetails(MtCustomerFilterDto mtCustomerFilterDto, ApplicationUser applicationUserFromAuthentication);

  ApiResponse searchMTCustomer(String locationId, String custName, String city, String state, String custAcctGro, ApplicationUser applicationUser);

  ApiResponse getTransporterList(ApplicationUser applicationUserFromAuthentication);

  ApiResponse getStateCodes(ApplicationUser applicationUser);

  ApiResponse getCustTypes(ApplicationUser applicationUser);
}
