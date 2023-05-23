package com.api.apollo.atom.controller;

import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.master.*;
import com.api.apollo.atom.dto.ops.LsUtilizationDto;
import com.api.apollo.atom.service.MasterDataService;
import com.api.apollo.atom.util.Utility;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import oracle.jdbc.proxy.annotation.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "Master Data Management")
@RestController
@RequestMapping("/api/v1/master/")
@PreAuthorize("permitAll")
public class MasterController {

  @Autowired
  MasterDataService masterDateService;

  @GetMapping(value = "search")
  public ResponseEntity<ApiResponse> searchMasterData(
          @RequestParam(name = "destination", required = false) String destination,
          @RequestParam(name = "description", required = false) String description,
          @RequestParam(name = "materialCode", required = false) String materialCode,
          @RequestParam(name = "batchCode", required = false) String batchCode,
          @RequestParam(name = "materialGrp", required = false) String materialGrp,
          @RequestParam(name = "truckType", required = false) String truckType,
          @RequestParam(name = "truckDescription", required = false) String truckDescription,
          @RequestParam(name = "transporter", required = false) String transporter,
          @RequestParam(name = "materialDesc", required = false) String materialDesc,
          @RequestParam(name = "gpsProvider", required = false) String gpsProvider,
          @RequestParam(name = "index", required = true, defaultValue = "0") int index,
          @RequestParam(name = "destCountrySearch", required = false) String destCountrySearch,
          @RequestParam(name = "podSearch", required = false) String podSearch,
          @RequestParam(name = "isIntransitTruck", required = false) boolean isIntransitTruck,
          @RequestParam(name = "forIndent", required = false) boolean forIndent,
          Authentication authentication) {
    return ResponseEntity.ok(masterDateService.searchMasterDataInfo(destination, description, materialCode, batchCode,
            materialGrp, truckType, truckDescription, transporter, materialDesc, gpsProvider, Utility.getApplicationUserFromAuthentication(authentication), index, destCountrySearch, podSearch, isIntransitTruck, forIndent));
  }

//  @GetMapping(value = "search")
//  public ResponseEntity<ApiResponse> searchMasterData(
//          @RequestParam(name = "locationId", required = false) String locationId,
//          RequestParam(name="index", required =true, defaultValue ="0"),
//
////  Authentication authentication)
////          return ResponseEntity.ok(masterDateService.searchMasterDataInfo(destination, materialCode, batchCode,
////  materialGrp, truckType, truckDescription, transporter, materialDesc, gpsProvider, Utility.getApplicationUserFromAuthentication(authentication), index));
////}
//}




  @GetMapping(value = "bay-info")
  public ResponseEntity<ApiResponse> getPlantBayInfo(Authentication authentication) {
    return ResponseEntity
        .ok(masterDateService.getPlantBayInfo(Utility.getApplicationUserFromAuthentication(authentication)));
  }

  @GetMapping(value = "variants-data")
  public ResponseEntity<ApiResponse> getVariants() {
    return ResponseEntity.ok(masterDateService.getVariants());
  }

  /**
   * Master Truck Types data to show as List of values(Dropdown) in UI
   * @return
   */
  @GetMapping(value = "master-truckTypes")
  public ResponseEntity<ApiResponse> getAllTrucktypes() {
    return ResponseEntity.ok(masterDateService.getTruckTypes());
  }

  @GetMapping(value = "master-truckTypes-dest")
  public ResponseEntity<ApiResponse> getTruckTypesBySourceAndDest(@RequestParam(name = "sourceLoc") String sourceLoc, @RequestParam(name = "destLoc") String destLoc){
    return ResponseEntity.ok(masterDateService.getTruckTypesBySourceAndDest(sourceLoc, destLoc));
  }

  @PostMapping(value = "transporter-freight")
  public ResponseEntity<ApiResponse> trasportersToCreateIndet(@RequestBody LsUtilizationDto lsUtilizationDto) {
    return ResponseEntity.ok(masterDateService.transportersTocreateIndnet(lsUtilizationDto));
  }

  @GetMapping(value = "master-batchcodes")
  public ResponseEntity<ApiResponse> getBatchCodesmasterData() {
    return ResponseEntity.ok(masterDateService.getBatchCodesmasterData());
  }

  @GetMapping(value = "master-category")
  public ResponseEntity<ApiResponse> getMTMatrialGroupscmGroupMasterData() {
    return ResponseEntity.ok(masterDateService.getMTMatrialGroupscmGroupMasterData());
  }

  /*@GetMapping(value = "master-category")
  public ResponseEntity<ApiResponse> getItemCategoryMasterData() {
    return ResponseEntity.ok(masterDateService.getItemCategoryMasterData());
  }*/




    //Admin

    //Need to verify
 /* ApiResponse searchMasterDataInfo(String destination, String materialCode, String batchCode, String materialGrp,
                                   String truckType, String transporter, String materialDesc, String gpsProvider, String itemId
      , String itemClassification, String itemType, String itemGroup , String itemCategory
      , ApplicationUser loggedInUser, int index);*/






    @PostMapping(value= "getMTItem")
    public ResponseEntity<ApiResponse> getMTItem(@RequestBody MtItemFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getMTItem(itemFilterDto));
    }

    @PostMapping("/uploadmtitem")
    public ResponseEntity<ApiResponse> uploadmtitem(@RequestParam("file") MultipartFile multipartFile, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadmtitem(multipartFile, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping(value = "update-mtitem-formdata")
    public ResponseEntity<ApiResponse> updateMTITEMFormData(@RequestBody MtItemFilterDto mtItemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateMtitemData(mtItemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping(value= "freights")
    public ResponseEntity<ApiResponse> getFreightsInfo(@RequestBody FreightFilterDto freightFilterDto){
        return  ResponseEntity.ok(masterDateService.getFreightsInfo(freightFilterDto));
    }

    /*
    @PostMapping("/upload-freight-file")
    public ResponseEntity<ApiResponse> uploadFreightFile(@RequestParam("file") MultipartFile multipartFile, Authentication authentication) throws Exception {
      return ResponseEntity.ok(masterDateService.uploadFreightFile(multipartFile, Utility.getApplicationUserFromAuthentication(authentication)));
    }
    */
    @PostMapping("/upload-freight-json")
    public ResponseEntity<ApiResponse> uploadFreightAsJson(@RequestBody FreightFilterDto freightFilterDto, Authentication authentication) throws Exception {
       // System.out.println(new Gson().toJson(freightFilterDto));
        return ResponseEntity.ok(masterDateService.uploadFreightFileAsJson(freightFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping(value= "getLocationScan")
    public ResponseEntity<ApiResponse> getLocationScan(@RequestBody LocationScanFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getLocationScan(itemFilterDto));
    }

    @PostMapping("/addOrUploadLocationScan")
    public ResponseEntity<ApiResponse> addOrUploadLocationScan(@RequestBody LocationScanFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.addOrUploadLocationScan(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateLocationScan")
    public ResponseEntity<ApiResponse> updateLocationScan(@RequestBody LocationScanFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateLocationScan(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/approve-freight-json")
    public ResponseEntity<ApiResponse> approveFreightJson(@RequestBody FreightFilterDto freightFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.approveFreightJson(freightFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping(value= "getMTOeBomInfo")
    public ResponseEntity<ApiResponse> getMTOeBomInfo(@RequestBody MTOeBomFilterDto mtOeBomFilterDto){
        return  ResponseEntity.ok(masterDateService.getMTOeBomInfo(mtOeBomFilterDto));
    }

    @PostMapping(value= "getMTRepBomInfo")
    public ResponseEntity<ApiResponse> getMTRepBomInfo(@RequestBody MTRepBomFilterDto mtRepBomFilterDto){
        return  ResponseEntity.ok(masterDateService.getMTRepBomInfo(mtRepBomFilterDto));
    }

    @PostMapping(value= "getMTPlantItemInfo")
    public ResponseEntity<ApiResponse> getMTPlantItemInfo(@RequestBody MTPlantItemFilterDto mtPlantItemFilterDto){
        return  ResponseEntity.ok(masterDateService.getMTPlantItemInfo(mtPlantItemFilterDto));
    }

    @PostMapping(value= "getMtBatchCodes")
    public ResponseEntity<ApiResponse> getMtBatchCodes(@RequestBody MtBatchCodesFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getMtBatchCodes(itemFilterDto));
    }

    @PostMapping("/addOrUploadMtBatchCodes")
    public ResponseEntity<ApiResponse> addOrUploadMtBatchCodes(@RequestBody MtBatchCodesFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.addOrUploadMtBatchCodes(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateMtBatchCodes")
    public ResponseEntity<ApiResponse> updateMtBatchCodes(@RequestBody MtBatchCodesFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateMtBatchCodes(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping(value= "getMtSapTruckType")
    public ResponseEntity<ApiResponse> getMtSapTruckType(@RequestBody MtSapTruckTypeFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getMtSapTruckType(itemFilterDto));
    }

    @PostMapping("/addOrUploadMtSapTruckType")
    public ResponseEntity<ApiResponse> addOrUploadMtSapTruckType(@RequestBody MtSapTruckTypeFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.addOrUploadMtSapTruckType(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateMtSapTruckType")
    public ResponseEntity<ApiResponse> updateMtSapTruckType(@RequestBody MtSapTruckTypeFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateMtSapTruckType(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping(value= "getMtTruckType")
    public ResponseEntity<ApiResponse> getMtTruckType(@RequestBody MtTruckTypeFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getMtTruckType(itemFilterDto));
    }

    @PostMapping("/addOrUploadMtTruckType")
    public ResponseEntity<ApiResponse> addOrUploadMtTruckType(@RequestBody MtTruckTypeFilterDto itemFilterDto, Authentication authentication) throws Exception {
         System.out.println(new Gson().toJson(itemFilterDto));
        return ResponseEntity.ok(masterDateService.addOrUploadMtTruckType(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateMtTruckType")
    public ResponseEntity<ApiResponse> updateMtTruckType(@RequestBody MtTruckTypeFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateMtTruckType(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping(value= "getMtMaterialGroup")
    public ResponseEntity<ApiResponse> getMtMaterialGroup(@RequestBody MtMaterialGroupFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getMtMaterialGroup(itemFilterDto));
    }

    @PostMapping("/uploadMtMaterialGroup")
    public ResponseEntity<ApiResponse> uploadMtMaterialGroup(@RequestBody MtMaterialGroupFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadMtMaterialGroup(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateMtMaterialGroup")
    public ResponseEntity<ApiResponse> updateMtMaterialGroup(@RequestBody MtMaterialGroupFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateMtMaterialGroup(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }


    @PostMapping(value= "getOrderTypeLookup")
    public ResponseEntity<ApiResponse> getOrderTypeLookup(@RequestBody OrderTypeLookupFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getOrderTypeLookup(itemFilterDto));
    }

    @PostMapping("/uploadOrderTypeLookup")
    public ResponseEntity<ApiResponse> uploadOrderTypeLookup(@RequestBody OrderTypeLookupFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadOrderTypeLookup(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateOrderTypeLookup")
    public ResponseEntity<ApiResponse> updateOrderTypeLookup(@RequestBody OrderTypeLookupFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateOrderTypeLookup(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

@PostMapping(value= "getMtValve")
public ResponseEntity<ApiResponse> getMtValve(@RequestBody MTValveFilterDto itemFilterDto){
    return  ResponseEntity.ok(masterDateService.getMtValve(itemFilterDto));
}

    @PostMapping("/uploadMtValve")
    public ResponseEntity<ApiResponse> uploadMtValve(@RequestBody MTValveFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadMtValve(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateMtValve")
    public ResponseEntity<ApiResponse> updateMtValve(@RequestBody MTValveFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateMtValve(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }


    @PostMapping(value= "getTransporters")
    public ResponseEntity<ApiResponse> getTransportersInfo(@RequestBody TransporterFilterDto transporterFilterDto){
        return  ResponseEntity.ok(masterDateService.getTransportersInfo(transporterFilterDto));
    }

    @PostMapping("/updateTransporter")
    public ResponseEntity<ApiResponse> updateTransporter(@RequestBody TransporterFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateTransporter(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }






    @PostMapping(value= "getMtElr")
    public ResponseEntity<ApiResponse> getMtElr(@RequestBody MtElrFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getMtElr(itemFilterDto));
    }

    @PostMapping("/uploadMtElr")
    public ResponseEntity<ApiResponse> uploadMtElr(@RequestBody MtElrFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadMtElr(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateMtElr")
    public ResponseEntity<ApiResponse> updateMtElr(@RequestBody MtElrFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateMtElr(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping(value= "getMtExcessWaitingLocLimit")
    public ResponseEntity<ApiResponse> getMtExcessWaitingLocLimit(@RequestBody MtExcessWaitingLocLimitFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getMtExcessWaitingLocLimit(itemFilterDto));
    }

    @PostMapping("/uploadMtExcessWaitingLocLimit")
    public ResponseEntity<ApiResponse> uploadMtExcessWaitingLocLimit(@RequestBody MtExcessWaitingLocLimitFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadMtExcessWaitingLocLimit(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateMtExcessWaitingLocLimit")
    public ResponseEntity<ApiResponse> updateMtExcessWaitingLocLimit(@RequestBody MtExcessWaitingLocLimitFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateMtExcessWaitingLocLimit(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }


    @PostMapping(value= "getMtExcessWaitingRepLimit")
    public ResponseEntity<ApiResponse> getMtExcessWaitingRepLimit(@RequestBody MtExcessWaitingRepLimitFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getMtExcessWaitingRepLimit(itemFilterDto));
    }

    @PostMapping("/uploadMtExcessWaitingRepLimit")
    public ResponseEntity<ApiResponse> uploadMtExcessWaitingRepLimit(@RequestBody MtExcessWaitingRepLimitFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadMtExcessWaitingRepLimit(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateMtExcessWaitingRepLimit")
    public ResponseEntity<ApiResponse> updateMtExcessWaitingRepLimit(@RequestBody MtExcessWaitingRepLimitFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateMtExcessWaitingRepLimit(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }


    @PostMapping(value= "getMtLocationBay")
    public ResponseEntity<ApiResponse> getMtLocationBay(@RequestBody MtLocationBayFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getMtLocationBay(itemFilterDto));
    }

    @PostMapping("/uploadMtLocationBay")
    public ResponseEntity<ApiResponse> uploadMtLocationBay(@RequestBody MtLocationBayFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadMtLocationBay(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/deleteMtLocationBay")
    public ResponseEntity<ApiResponse> deleteMtLocationBay(@RequestBody MtLocationBayFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.deleteMtLocationBay(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }


    @PostMapping(value= "getMtLocation")
    public ResponseEntity<ApiResponse> getMtLocation(@RequestBody MtLocationFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getMtLocation(itemFilterDto));
    }


    @PostMapping("/uploadMtLocation")
    public ResponseEntity<ApiResponse> uploadMtLocation(@RequestBody MtLocationFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadMtLocation(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateMtLocation")
    public ResponseEntity<ApiResponse> updateMtLocation(@RequestBody MtLocationFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateMtLocation(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }


    @GetMapping(value = "/get-all-mktseg")
    public ResponseEntity<ApiResponse> getAllDistinctMKTSEG(Authentication authentication) throws Exception{
      return ResponseEntity.ok(masterDateService.getDistinctMarketSegments(Utility.getApplicationUserFromAuthentication(authentication)));
    }

  @GetMapping(value = "/get-truck-status-locs")
  public ResponseEntity<ApiResponse> getTruckStatusLocs(Authentication authentication) throws Exception{
    return ResponseEntity.ok(masterDateService.getTruckStatusLocs(Utility.getApplicationUserFromAuthentication(authentication)));
  }














    @PostMapping(value= "getCtUom")
    public ResponseEntity<ApiResponse> getCtUom(@RequestBody CtUomFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getCtUom(itemFilterDto));
    }

    @PostMapping("/uploadCtUom")
    public ResponseEntity<ApiResponse> uploadCtUom(@RequestBody CtUomFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadCtUom(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateCtUom")
    public ResponseEntity<ApiResponse> updateCtUom(@RequestBody CtUomFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateCtUom(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }



    @PostMapping(value= "getCtUomMap")
    public ResponseEntity<ApiResponse> getCtUomMap(@RequestBody CtUomMapFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getCtUomMap(itemFilterDto));
    }

    @PostMapping("/uploadCtUomMap")
    public ResponseEntity<ApiResponse> uploadCtUomMap(@RequestBody CtUomMapFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadCtUomMap(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateCtUomMap")
    public ResponseEntity<ApiResponse> updateCtUomMap(@RequestBody CtUomMapFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateCtUomMap(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }


    @PostMapping(value= "getMtIncoterms")
    public ResponseEntity<ApiResponse> getMtIncoterms(@RequestBody MtIncotermsFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getMtIncoterms(itemFilterDto));
    }

    @PostMapping("/uploadMtIncoterms")
    public ResponseEntity<ApiResponse> uploadMtIncoterms(@RequestBody MtIncotermsFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadMtIncoterms(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateMtIncoterms")
    public ResponseEntity<ApiResponse> updateMtIncoterms(@RequestBody MtIncotermsFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateMtIncoterms(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping(value= "getMtTruck")
    public ResponseEntity<ApiResponse> getMtTruck(@RequestBody MtTruckFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getMtTruck(itemFilterDto));
    }

    @PostMapping("/uploadMtTruck")
    public ResponseEntity<ApiResponse> uploadMtTruck(@RequestBody MtTruckFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadMtTruck(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateMtTruck")
    public ResponseEntity<ApiResponse> updateMtTruck(@RequestBody MtTruckFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateMtTruck(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }


    @PostMapping(value= "getMtContact")
    public ResponseEntity<ApiResponse> getMtContact(@RequestBody MtContactFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getMtContact(itemFilterDto));
    }

    @PostMapping("/uploadMtContact")
    public ResponseEntity<ApiResponse> uploadMtContact(@RequestBody MtContactFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadMtContact(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateMtContact")
    public ResponseEntity<ApiResponse> updateMtContact(@RequestBody MtContactFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateMtContact(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }


    @PostMapping(value= "getMtCustomer")
    public ResponseEntity<ApiResponse> getMtCustomer(@RequestBody MtCustomerFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getMtCustomer(itemFilterDto));
    }

    @PostMapping("/uploadMtCustomer")
    public ResponseEntity<ApiResponse> uploadMtCustomer(@RequestBody MtCustomerFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadMtCustomer(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateMtCustomer")
    public ResponseEntity<ApiResponse> updateMtCustomer(@RequestBody MtCustomerFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateMtCustomer(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping(value= "getMtCustomerShipTo")
    public ResponseEntity<ApiResponse> getMtCustomerShipTo(@RequestBody MtCustomerShipToFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getMtCustomerShipTo(itemFilterDto));
    }

    @PostMapping("/uploadMtCustomerShipTo")
    public ResponseEntity<ApiResponse> uploadMtCustomerShipTo(@RequestBody MtCustomerShipToFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadMtCustomerShipTo(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateMtCustomerShipTo")
    public ResponseEntity<ApiResponse> updateMtCustomerShipTo(@RequestBody MtCustomerShipToFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateMtCustomerShipTo(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }






    @PostMapping(value= "getUser")
    public ResponseEntity<ApiResponse> getUser(@RequestBody UserFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getUser(itemFilterDto));
    }

    @PostMapping("/uploadUser")
    public ResponseEntity<ApiResponse> uploadUser(@RequestBody UserFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadUser(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateUser")
    public ResponseEntity<ApiResponse> updateUser(@RequestBody UserFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateUser(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }


    @PostMapping("/updatePassword")
    public ResponseEntity<ApiResponse> updatePassword(@RequestBody UserFilterDto itemFilterDto, Authentication authentication) throws Exception {
        System.out.println(new Gson().toJson(itemFilterDto));
        return ResponseEntity.ok(masterDateService.updatePassword(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }


    @PostMapping(value= "getUserAssociation")
    public ResponseEntity<ApiResponse> getUserAssociation(@RequestBody UserAssociationFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getUserAssociation(itemFilterDto));
    }

    @PostMapping("/uploadUserAssociation")
    public ResponseEntity<ApiResponse> uploadUserAssociation(@RequestBody UserAssociationFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadUserAssociation(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateUserAssociation")
    public ResponseEntity<ApiResponse> updateUserAssociation(@RequestBody UserAssociationFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateUserAssociation(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }


    @PostMapping(value= "getUserRole")
    public ResponseEntity<ApiResponse> getUserRole(@RequestBody UserRoleFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getUserRole(itemFilterDto));
    }

    @PostMapping("/uploadUserRole")
    public ResponseEntity<ApiResponse> uploadUserRole(@RequestBody UserRoleFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadUserRole(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateUserRole")
    public ResponseEntity<ApiResponse> updateUserRole(@RequestBody UserRoleFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateUserRole(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }


    @PostMapping(value= "getCtOtmFreightBasis")
    public ResponseEntity<ApiResponse> getCtOtmFreightBasis(@RequestBody CtOtmFreightBasisFilterDto itemFilterDto){
        return  ResponseEntity.ok(masterDateService.getCtOtmFreightBasis(itemFilterDto));
    }

    @PostMapping("/uploadCtOtmFreightBasis")
    public ResponseEntity<ApiResponse> uploadCtOtmFreightBasis(@RequestBody CtOtmFreightBasisFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.uploadCtOtmFreightBasis(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping("/updateCtOtmFreightBasis")
    public ResponseEntity<ApiResponse> updateCtOtmFreightBasis(@RequestBody CtOtmFreightBasisFilterDto itemFilterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.updateCtOtmFreightBasis(itemFilterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }




    @PostMapping(value= "getServProList")
    public ResponseEntity<ApiResponse> getServProList(@RequestBody MtScacFilterDto filterDto){
        System.out.println(new Gson().toJson(filterDto));
        return  ResponseEntity.ok(masterDateService.getServProList(filterDto));
    }

    @PostMapping("/addServProv")
    public ResponseEntity<ApiResponse> addServProv(@RequestBody MtScacFilterDto filterDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(masterDateService.addServProv(filterDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }


    @GetMapping(value= "getServPros")
    public ResponseEntity<ApiResponse> getServPros(){
        return  ResponseEntity.ok(masterDateService.getServPros());
    }

    @GetMapping(value= "getLocationIdsList")
    public ResponseEntity<ApiResponse> getLocationIdsList(){
        return  ResponseEntity.ok(masterDateService.getLocationIdsList());
    }

    @GetMapping(value= "getLocationClasses")
    public ResponseEntity<ApiResponse> getLocationClasses(){
        return  ResponseEntity.ok(masterDateService.getLocationClasses());
    }



    @GetMapping(value= "getUserIdsList")
    public ResponseEntity<ApiResponse> getUserIdsList(){
        return  ResponseEntity.ok(masterDateService.getUserIdsList());
    }

    @GetMapping(value= "getUserRoleIdsList")
    public ResponseEntity<ApiResponse> getUserRoleIdsList(){
        return  ResponseEntity.ok(masterDateService.getUserRoleIdsList());
    }

    @GetMapping(value= "getDistinctScmGroupList")
    public ResponseEntity<ApiResponse> getDistinctScmGroupList(){
        return  ResponseEntity.ok(masterDateService.getDistinctScmGroupList());
    }

    @GetMapping(value= "getPaasTruckTypeList")
    public ResponseEntity<ApiResponse> getPaasTruckTypeList(){
        return  ResponseEntity.ok(masterDateService.getPaasTruckTypeList());
    }

    @GetMapping(value= "getTruckTypeList")
    public ResponseEntity<ApiResponse> getTruckTypeList(){
        return  ResponseEntity.ok(masterDateService.getTruckTypeList());
    }

    @GetMapping(value= "getVariantsList")
    public ResponseEntity<ApiResponse> getVariantsList(){
        return  ResponseEntity.ok(masterDateService.getVariantsList());
    }

    @GetMapping(value= "getItemGroupsList")
    public ResponseEntity<ApiResponse> getItemGroupsList(){
        return  ResponseEntity.ok(masterDateService.getItemGroupsList());
    }
    @GetMapping(value= "getbatchCategoryList")
    public ResponseEntity<ApiResponse> getbatchCategoryList(){
        return  ResponseEntity.ok(masterDateService.getbatchCategoryList());
    }

    @GetMapping(value= "getbatchCodesForValves")
    public ResponseEntity<ApiResponse> getbatchCodesForValves(){
        return  ResponseEntity.ok(masterDateService.getbatchCodesForValves());
    }

    @GetMapping(value= "getItemIdList")
    public ResponseEntity<ApiResponse> getItemIdList(){
        return  ResponseEntity.ok(masterDateService.getItemIdList());
    }

  @GetMapping(value = "/check-material-code-tyre")
  public ApiResponse checkMaterialCodeIsTyre(@RequestParam(name = "materialCode", required = true) String materialCode){
      return masterDateService.checkMaterialCodeIsTyre(materialCode);
  }

  @GetMapping("/get-batch-code-prefixes")
  public ApiResponse getBatchCodePrefixes(){
      return masterDateService.getBatchCodePrefixes();
  }


  @GetMapping("/delete-freight-record")
  public ApiResponse deleteFreightData(@RequestParam(name = "rowId") Double rowId, Authentication authentication){
      return masterDateService.deleteFreightData(rowId, Utility.getApplicationUserFromAuthentication(authentication));
  }

  @PostMapping("/truck-dedicated")
  public ApiResponse getMTTruckDedicatedData(@RequestBody TruckDedicatedFilterDto  truckDedicatedFilterDto, Authentication authentication){
      return masterDateService.getMTTruckDedicatedData(truckDedicatedFilterDto, Utility.getApplicationUserFromAuthentication(authentication));
  }

  @PostMapping("/save-truck-dedicated")
  public ApiResponse saveMTTruckDedicated(@RequestBody MTTruckDedicatedDto mtTruckDedicatedDto, Authentication authentication){
      return masterDateService.saveMTTruckDedicated(mtTruckDedicatedDto, Utility.getApplicationUserFromAuthentication(authentication));
  }

  @GetMapping("/delete-truck-dedicated")
  public ApiResponse deleteMTTruckDedicatedRecord(@RequestParam(name = "id") Long rowId, Authentication authentication){
      return masterDateService.deleteMTTruckDedicatedRecord(rowId, Utility.getApplicationUserFromAuthentication(authentication));
  }

  @PostMapping("/update-freight")
  ApiResponse updateFreightDetails(@RequestBody FreightInfoDto freightInfoDto, Authentication authentication){
      return masterDateService.updateFreightDetails(freightInfoDto, Utility.getApplicationUserFromAuthentication(authentication));
  }


  @PostMapping("/get-mt-customers")
  ApiResponse getMTCustomers(@RequestBody MtCustomerFilterDto mtCustomerFilterDto, Authentication authentication){
    return masterDateService.getMtCustomerDetails(mtCustomerFilterDto, Utility.getApplicationUserFromAuthentication(authentication));
  }

  @GetMapping("/search-cust-loc")
  public ApiResponse searchMTCustomerLoc(@RequestParam(name = "locationId", required = false) String locationId,
                                         @RequestParam(name = "custName", required = false) String custName,
                                         @RequestParam(name = "city", required = false) String city,
                                         @RequestParam(name = "state", required = false) String state,
                                         @RequestParam(name = "custAcctGrp", required = false) String custAcctGro, Authentication authentication){
      return masterDateService.searchMTCustomer(locationId, custName, city, state, custAcctGro, Utility.getApplicationUserFromAuthentication(authentication));
  }

  @GetMapping("/get-transporter-list")
  public ApiResponse getTransporterList(Authentication authentication){
      return masterDateService.getTransporterList(Utility.getApplicationUserFromAuthentication(authentication));
  }

  @GetMapping("/get-state-codes")
  public ApiResponse getStateCodeList(Authentication authentication){
      return masterDateService.getStateCodes(Utility.getApplicationUserFromAuthentication(authentication));
  }

  @GetMapping("/get-cust-types")
  public ApiResponse getCustTypeList(Authentication authentication){
    return masterDateService.getCustTypes(Utility.getApplicationUserFromAuthentication(authentication));
  }


}
