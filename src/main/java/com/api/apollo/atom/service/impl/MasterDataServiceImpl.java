package com.api.apollo.atom.service.impl;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.Constants.BayStatus;
import com.api.apollo.atom.constant.ItemType;
import com.api.apollo.atom.constant.LocationType;
import com.api.apollo.atom.constant.UserRole;
import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.core.MasterDataDto;
import com.api.apollo.atom.dto.core.MasterTrucktypeDto;
import com.api.apollo.atom.dto.master.*;
import com.api.apollo.atom.dto.ops.LsUtilizationDto;
import com.api.apollo.atom.dto.ops.TruckReportDto;
import com.api.apollo.atom.dto.ops.TruckTypesWithFreightDto;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.master.*;
import com.api.apollo.atom.repository.master.*;
import com.api.apollo.atom.repository.ops.MTCustomerRepository;
import com.api.apollo.atom.repository.ops.TruckReportSummaryRepository;
import com.api.apollo.atom.service.FilterService;
import com.api.apollo.atom.service.MasterDataService;
import com.api.apollo.atom.util.DateUtils;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.sql.Clob;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MasterDataServiceImpl implements MasterDataService {

  @Autowired
  LocationRepository locationRepo;

  @Autowired
  ItemRepository itemRepo;

  @Autowired
  private BatchCodeRepository batchCodeRepo;

  @Autowired
  private TruckTypeInfoRepository truckTypeRepository;

  @Autowired
  private TransporterRepository transporterRepository;

  @Autowired
  private GpsProviderInfoRepository gpsProviderRepo;

  @Autowired
  private LocationBayRepository locationBayRepo;

  @Autowired
  private FreightRepository freightRepository;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private FilterService filterService;

  @Autowired
  private FreightTempRepository freightTempRepository;

  @Autowired
  private MTMatrialGroupRepository mtMatrialGroupRepository;

  @Autowired
  private CTCountryRepository ctCountryRepository;

  @Autowired
  private OrderTypeLookupRepository orderTypeLookupRepository;

  @Autowired
  private TruckReportSummaryRepository truckReportSummaryRepository;

  @Autowired
  private MTPlantBatchRepository mtPlantBatchRepository;

  @Autowired
  MTTruckDedidatedRepository mtTruckDedidatedRepository;

  @Autowired
  MTCustomerRepository mtCustomerRepository;

  @Autowired
  MTScasRepository mtScasRepository;
  @Override
  public ApiResponse searchMasterDataInfo(String destination, String description, String materialCode, String batchCode,
                                          String materialGrp, String truckType, String truckDescription, String transporter, String materialDesc, String gpsProvider,
                                          ApplicationUser loggedInUser, int index, String destCountry, String pod, boolean isIntransitTruck, boolean forIndent) {
    if (!StringUtils.isEmpty(destination)) {
      MasterDataDto masterDataDto;
      if (StringUtils.isEmpty(loggedInUser.getPlantCode())) {
        masterDataDto = new MasterDataDto(locationRepo.getDestinationForTrasnsporter(destination, PageRequest.of(index, Constants.MASTER_PAGE_LIMIT)));
      } else {
        Optional<MTLocation> optionalMTLocation = locationRepo.findByLocationId(loggedInUser.getPlantCode());
        if (forIndent) {
          masterDataDto = new MasterDataDto(locationRepo.findAllDestLocationsLikeForIndent(destination, loggedInUser.getPlantCode(), PageRequest.of(index, Constants.MASTER_PAGE_LIMIT)));
        } else {
          /*isIntransitTruck is used to fetch the loggedInLoc in the destination LOVs for all the users*/
          if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole()) || (optionalMTLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equals(optionalMTLocation.get().getLocationClass()))
              || (isIntransitTruck)) {
            masterDataDto = new MasterDataDto(locationRepo.findAllDestLocationsLikeDPREP(destination, PageRequest.of(index, Constants.MASTER_PAGE_LIMIT)));
          } else {
            masterDataDto = new MasterDataDto(locationRepo.findAllDestLocationsLike(destination, loggedInUser.getPlantCode(), PageRequest.of(index, Constants.MASTER_PAGE_LIMIT)));
          }
        }
      }
      return new ApiResponse(HttpStatus.OK, "", masterDataDto);
    }
    if (!StringUtils.isEmpty(materialCode)) {
      return new ApiResponse(HttpStatus.OK, "", new MasterDataDto(null, itemRepo.findAllItemInfoIdLike(materialCode, PageRequest.of(index, Constants.MASTER_PAGE_LIMIT)),
          null, null, null, null, null, null, null));
    }
    if (!StringUtils.isEmpty(batchCode)) {
      return new ApiResponse(HttpStatus.OK, "", new MasterDataDto(null, null, batchCodeRepo.findAllBatchCodesLike(batchCode, PageRequest.of(index, Constants.MASTER_PAGE_LIMIT)),
          null, null, null, null, null, null));
    }
    if (!StringUtils.isEmpty(materialGrp)) {
      return new ApiResponse(HttpStatus.OK, "", new MasterDataDto(null, null, null, itemRepo.findAllByMaterialGrp(materialGrp, PageRequest.of(index, Constants.MASTER_PAGE_LIMIT)),
          null, null, null, null, null));
    }
    if (!StringUtils.isEmpty(truckType)) {
      return new ApiResponse(HttpStatus.OK, "", new MasterDataDto(null, null, null, null, truckTypeRepository.findAllTruckTypesLike(truckType, PageRequest.of(index, Constants.MASTER_PAGE_LIMIT)), null, null, null, null));
    }

    if (!StringUtils.isEmpty(truckDescription)) {
      System.out.println("****");
      return new ApiResponse(HttpStatus.OK, "", new MasterDataDto(null, null, null, null, null, truckTypeRepository.findAllTruckTypAndDescLike(truckDescription, PageRequest.of(index, Constants.MASTER_PAGE_LIMIT)), null, null, null));
    }

    if (!StringUtils.isEmpty(transporter)) {
      return new ApiResponse(HttpStatus.OK, "", new MasterDataDto(null, null, null, null, null, null, transporterRepository.findAllTransportersLike(
          transporter, PageRequest.of(index, Constants.MASTER_PAGE_LIMIT)), null, null));
    }
    if (!StringUtils.isEmpty(materialDesc)) {
      return new ApiResponse(HttpStatus.OK, "", new MasterDataDto(null, itemRepo.findAllItemInfoDescriptionLike(materialDesc,
          PageRequest.of(index, Constants.MASTER_PAGE_LIMIT)), null, null, null, null, null, null, null));
    }
    if (!StringUtils.isEmpty(gpsProvider)) {
      return new ApiResponse(HttpStatus.OK, "", new MasterDataDto(null, null, null, null, null, null, null, gpsProviderRepo
          .findByGpsProviderLike(gpsProvider, PageRequest.of(index, Constants.MASTER_PAGE_LIMIT)), null));
    }
    if (!StringUtils.isEmpty(description)) {
      if (forIndent){
        return new ApiResponse(HttpStatus.OK, "",
            new MasterDataDto(null, null, null, null, null, null, null, null, locationRepo.findAllDestLocationsDescriptionLikeForIndent(description, loggedInUser.getPlantCode(), PageRequest.of(index, Constants.MASTER_PAGE_LIMIT))));
      }else {
        if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
          return new ApiResponse(HttpStatus.OK, "", new MasterDataDto(null, null, null, null, null, null, null, null, locationRepo.findAllDestLocationsDescriptionLike(description, PageRequest.of(index, Constants.MASTER_PAGE_LIMIT))));
        } else {
          return new ApiResponse(HttpStatus.OK, "", new MasterDataDto(null, null, null, null, null, null, null, null, locationRepo.findAllDestLocationsDescriptionLikeOtherThanLoggedInLoc(description, loggedInUser.getPlantCode(), PageRequest.of(index, Constants.MASTER_PAGE_LIMIT))));
        }
      }
    }

    if (!StringUtils.isEmpty(destCountry)) {
      MasterDataDto masterDataDto;
      masterDataDto = new MasterDataDto(ctCountryRepository.findAllDestLocationsWithTypePORT(destCountry, PageRequest.of(index, Constants.MASTER_PAGE_LIMIT)));
      return new ApiResponse(HttpStatus.OK, "", masterDataDto);
    }

    /*For POD destination with locationType = PORT*/
    if (!StringUtils.isEmpty(pod)) {
      MasterDataDto masterDataDto = null;
      masterDataDto = new MasterDataDto(locationRepo.findAllDestLocationsWithTypePORT(pod, PageRequest.of(index, Constants.MASTER_PAGE_LIMIT)));
      return new ApiResponse(HttpStatus.OK, "", masterDataDto);
    }
    return new ApiResponse(HttpStatus.NOT_FOUND, "Search not found", Collections.emptyList());
  }

  @Override
  public ApiResponse getPlantBayInfo(ApplicationUser loggedInUser) {
    return new ApiResponse(HttpStatus.OK, "", locationBayRepo.findByLocationIdAndBayStatus(loggedInUser.getPlantCode(), BayStatus.OPEN.name()));
  }

  @Override
  public MTTruck createTruck(TruckReportDto reportDto, String transporter, ApplicationUser loggedInUser, MTTruckTypeInfo truckType, GpsProviderInfo gpsInfo, Optional<MTTruck> optionalTruck) {
//    MTTruck truck = optionalTruck.isPresent() ? optionalTruck.get() : new MTTruck();
    MTTruck truck = optionalTruck.orElseGet(MTTruck::new);
    truck.setTruckNumber(reportDto.getTruckNumber());
    truck.setServprov(transporter);
    truck.setTtId(truckType);
    truck.setGpsEnabled(reportDto.isGpsEnabled() ? Constants.GPS_ENABLED : Constants.GPS_DISABLED);
    truck.setGpsProvider(gpsInfo);
    truck.setInsertUser(loggedInUser.getUserId());
    truck.setPassingWeight(reportDto.getTruckCapacity());
    truck.setPassingWeightUom("KG");

    return truck;
  }

  @Override
  public ApiResponse getVariants() {
    List<MTTruckTypeInfo> truckTypes = truckTypeRepository.getVariants();
    List<String> variant1 = truckTypes.stream().map(MTTruckTypeInfo::getVariant1).distinct().filter(Objects::nonNull).sorted()
        .collect(Collectors.toList());
    List<String> variant2 = truckTypes.stream().map(MTTruckTypeInfo::getVariant2).distinct().filter(Objects::nonNull).sorted()
        .collect(Collectors.toList());
    Map<String, List<String>> variantMap = new HashMap<>();
    variantMap.put("variant1", variant1);
    variantMap.put("variant2", variant2);
    return new ApiResponse(HttpStatus.OK, "", variantMap);
  }

  @Override
  public ApiResponse getTruckTypes() {
    // No variant is mentioned so get Unique Truck Types whose variant1 is NULL
    List<MTTruckTypeInfo> truckTypes = truckTypeRepository.findDistinctTruckTypesByOrderByTruckTypeAsc();
    List<MasterTrucktypeDto> masterTrucktypeDtos = truckTypes.stream().map(mtTruckTypeInfo -> new MasterTrucktypeDto(mtTruckTypeInfo.getType(), mtTruckTypeInfo.getTteCapacity() != null ? mtTruckTypeInfo.getTteCapacity() : 0)).collect(Collectors.toList());
    return new ApiResponse(HttpStatus.OK, "Master Truck Types Data", masterTrucktypeDtos);
  }

  @Override
  public ApiResponse getTruckTypesBySourceAndDest(String sourceLoc, String destLoc) {
//    All truck types
    List<MTTruckTypeInfo> truckTypes = truckTypeRepository.findDistinctTruckTypesByOrderByTruckTypeAsc();
    /*Getting the truck Types from FREIGHT with source AND dest AND EFFECTIVE and EXPIRY date*/
    List<Map<String, Object>> truckTypesMap = truckTypeRepository.findDistinctTruckTypesBySourceAndDestByOrderByTruckTypeAsc(sourceLoc, destLoc, DateUtils.setTimeToMidnight(new Date()));
//    When source is EXT_WAREHOUSE then checking the freight availability with likend plant loc
    if (truckTypesMap == null || truckTypesMap.isEmpty()) {
      Optional<MTLocation> optionalMTLocation = locationRepo.findByLocationId(sourceLoc);
      if (optionalMTLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equals(optionalMTLocation.get().getLocationClass())) {
        truckTypesMap = truckTypeRepository.findDistinctTruckTypesBySourceAndDestByOrderByTruckTypeAsc(optionalMTLocation.get().getLinkedPlant(), destLoc, DateUtils.setTimeToMidnight(new Date()));
      }
    }
    List<MTTruckTypeInfo> trucksWithFreight = new ArrayList<>();
    if (truckTypesMap != null && !truckTypesMap.isEmpty()) {
      for (Map<String, Object> map : truckTypesMap) {
        Optional<MTTruckTypeInfo> optionalMTTruckTypeInfo = truckTypes.stream().filter(masterType -> masterType.getType().equals(map.get("TRUCKTYPE").toString())).findFirst();
        if (optionalMTTruckTypeInfo.isPresent()) {
          trucksWithFreight.add(optionalMTTruckTypeInfo.get());
        }
      }
    }
    if (trucksWithFreight != null && !trucksWithFreight.isEmpty()) {
      /*Removing the truck types which are having freight from the master truck types list*/
      truckTypes.removeAll(trucksWithFreight);
    }

    List<TruckTypesWithFreightDto> truckTypesWithFreightDtos = new ArrayList<>();
    TruckTypesWithFreightDto truckTypesWithFreightDto = new TruckTypesWithFreightDto();
    /*With Freight Truck Types*/
    truckTypesWithFreightDto.setName("WITH APPROVED FREIGHT");
    truckTypesWithFreightDto.setMtTruckTypeInfos(trucksWithFreight.stream().map(mtTruckTypeInfo -> new MasterTrucktypeDto(mtTruckTypeInfo.getType(), mtTruckTypeInfo.getTteCapacity() != null ? mtTruckTypeInfo.getTteCapacity() : 0))
        .collect(Collectors.toList()));
    truckTypesWithFreightDtos.add(truckTypesWithFreightDto);

    truckTypesWithFreightDto = new TruckTypesWithFreightDto();
    /*All the Master truck types expext trucks with freight*/
    truckTypesWithFreightDto.setName("WITHOUT FREIGHT");
    truckTypesWithFreightDto.setMtTruckTypeInfos(truckTypes.stream().map(mtTruckTypeInfo -> new MasterTrucktypeDto(mtTruckTypeInfo.getType(), mtTruckTypeInfo.getTteCapacity() != null ? mtTruckTypeInfo.getTteCapacity() : 0))
        .collect(Collectors.toList()));
    truckTypesWithFreightDtos.add(truckTypesWithFreightDto);

    return new ApiResponse(HttpStatus.OK, "Master Truck Types Data", truckTypesWithFreightDtos);
  }

  @Override
  public ApiResponse transportersTocreateIndnet(LsUtilizationDto lsUtilizationDto) {
    List<Freight> freights = freightRepository.findBySourceLocAndDestLocAndTruckTypeOrderByBaseFreightAsc
        (lsUtilizationDto.getSouceLoc(), lsUtilizationDto.getDestLoc(), lsUtilizationDto.getTrucktype());
    /*if source is EXT_WAREHOUSE then search with linkedPlantLoc
     * For displaying L1,L2 and L3, when truck type is selected*/
    if (freights == null || freights.size() == 0) {
      Optional<MTLocation> optionalMTLocation = locationRepo.findByLocationId(lsUtilizationDto.getSouceLoc());
      if (optionalMTLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equals(optionalMTLocation.get().getLocationClass())) {
        freights = freightRepository.findBySourceLocAndDestLocAndTruckTypeOrderByBaseFreightAsc
            (optionalMTLocation.get().getLinkedPlant(), lsUtilizationDto.getDestLoc(), lsUtilizationDto.getTrucktype());
      }
    }
    if (StringUtils.isEmpty(lsUtilizationDto.getDate()))
      return new ApiResponse(HttpStatus.BAD_REQUEST, "Please provide indent date and re-select the truck type");
    if (!DateUtils.isDateValid(lsUtilizationDto.getDate(), Constants.PLAN_RECORD_DATE_FORMAT))
      return new ApiResponse(HttpStatus.BAD_REQUEST, "Please provide date in valid format");
    Date date = DateUtils.formatDate(lsUtilizationDto.getDate(), Constants.PLAN_RECORD_DATE_FORMAT);
    List<Freight> freightList = new ArrayList<>();
    freights.forEach(freight -> {
      if (freight.getEffectiveDate() != null && freight.getExpiryDate() != null) {
        if (date.after(freight.getEffectiveDate()) && date.before(freight.getExpiryDate())) {
          freightList.add(freight);
        }
      } else if (freight.getEffectiveDate() != null && freight.getExpiryDate() == null) {
        if (date.after(freight.getEffectiveDate())) {
          freightList.add(freight);
        }
      }
    });
    List<Double> distinctBaseFreights = freightList.parallelStream().map(Freight::getBaseFreight).distinct().collect(Collectors.toList());
    Map<Double, String> labelMap = new HashMap<>();
    for (Double baseFreight : distinctBaseFreights) {
      labelMap.put(baseFreight, ("L" + (distinctBaseFreights.indexOf(baseFreight) + 1)));
    }
    List<LsUtilizationDto> lsUtilizationDtos = freightList.stream().map(freight -> new LsUtilizationDto(freight.getServprov(), freight.getBaseFreight(), labelMap)).collect(Collectors.toList());
    return new ApiResponse(HttpStatus.OK, "", lsUtilizationDtos);
  }

  @Override
  public ApiResponse getBatchCodesmasterData() {
    List<String> batchCodesMasterData = batchCodeRepo.findDistinctBatchCodesByOrderByCodeAsc();
    return new ApiResponse(HttpStatus.OK, "Master Batch Codes Data", batchCodesMasterData);
  }

  @Override
  public ApiResponse getMTMatrialGroupscmGroupMasterData() {
    List<String> scmGroupsMasterData = mtMatrialGroupRepository.findDistinctMTMatrialGroupByOrderByIdAsc();
    return new ApiResponse(HttpStatus.OK, "Master MTMatrialGroup scmGroup Data", scmGroupsMasterData);
  }

  /*@Override
  public ApiResponse getItemCategoryMasterData() {
    List<String> itemCategoriesMasterData = itemRepo.findDistinctItemCategoryByOrderByIdAsc();
    return new ApiResponse(HttpStatus.OK, "Master Item categories Data", itemCategoriesMasterData);
  }*/

  /// ADMIN


  @Override
  public ApiResponse updateMtitemData(MtItemFilterDto mtItemFilterDto, ApplicationUser loggedInUser) throws Exception {
    if (mtItemFilterDto.items.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    return new ApiResponse(HttpStatus.OK, "MTItem Saved", this.callMtItemProcedure(mtItemFilterDto, loggedInUser));
    //	return new ApiResponse(HttpStatus.OK, "MTItem Saved", mtItemFilterDto);
  }


  @Override
  public ApiResponse uploadmtitem(MultipartFile multipartFile, ApplicationUser loggedInUser) throws Exception {
    List<MtItemFilterDto> mtItemsList = new ArrayList<MtItemFilterDto>();
    XSSFWorkbook workbook = new XSSFWorkbook(multipartFile.getInputStream());
    XSSFSheet worksheet = workbook.getSheetAt(0);
    MtItemFilterDto mtItemFilterDto = new MtItemFilterDto();
    MTItemInfoDto mTItemInfoDto = null;
    for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {
      mTItemInfoDto = new MTItemInfoDto();
      XSSFRow row = worksheet.getRow(i);

      if (row.getCell(0) != null)
        mTItemInfoDto.setId(row.getCell(0).getStringCellValue());
      if (row.getCell(1) != null) {
        mTItemInfoDto.setClassification(row.getCell(1).getStringCellValue());
      } else {
        mTItemInfoDto.setClassification("");
      }


      if (row.getCell(2) != null) {
        mTItemInfoDto.setDescription(row.getCell(2).getStringCellValue());
      } else {
        mTItemInfoDto.setDescription("");
      }
      if (row.getCell(21) != null) {
        mTItemInfoDto.setCategory(row.getCell(21).getStringCellValue());
      } else {
        mTItemInfoDto.setCategory("");
      }

      if (row.getCell(3) != null)
        mTItemInfoDto.setType(ItemType.valueOf(row.getCell(3).getStringCellValue()));
      if (row.getCell(5) != null)
        mTItemInfoDto.setTte(row.getCell(5).getNumericCellValue());
      if (row.getCell(6) != null)
        mTItemInfoDto.setLoadFactor(row.getCell(6).getNumericCellValue());

      mtItemFilterDto.items.add(mTItemInfoDto);
    }

    if (mtItemFilterDto.items.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please upload a valid document")));
    return new ApiResponse(HttpStatus.OK, "MTItem Saved", this.callMtItemProcedure(mtItemFilterDto, loggedInUser));

  }

  public boolean isEmpty(String field) {
    return field == null;
  }


  private MTItemResultDto callMtItemProcedure(MtItemFilterDto mtItemDto, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_item_line");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);

    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(mtItemDto)));

    storedProcedure.setParameter("p_root_element", "items");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();

    return new MTItemResultDto(storedProcedure);
  }


  @Override
  public ApiResponse getFreightsInfo(FreightFilterDto freightFilterDto) {
    Page<Freight> pageableFreights = filterService.filterFreights(freightFilterDto, freightFilterDto.getTransporterSapCode(), freightFilterDto.getServprov(), freightFilterDto.getSourceLoc(), freightFilterDto.getSourceDesc(), freightFilterDto.getDestLoc(), freightFilterDto.getDestDesc(), freightFilterDto.getTruckType(), freightFilterDto.getCondition1()
        , freightFilterDto.getEffectiveDate(), freightFilterDto.getExpiryDate(), freightFilterDto.getTtDays()
        , freightFilterDto.getBaseFreight(), freightFilterDto.getStatus(), freightFilterDto.getFromInsertDate(), freightFilterDto.getToInsertDate(),
        freightFilterDto.getExpiryDateFilterType(), freightFilterDto.getFromExpiryDate(), freightFilterDto.getToExpiryDate());
    return new ApiResponse(HttpStatus.OK, "Freights List", new FreightFilterDto(freightFilterDto, pageableFreights));
  }


  @Override
  public ApiResponse uploadFreightFile(MultipartFile multipartFile, ApplicationUser loggedInUser) throws Exception {
    List<FreightFilterDto> mtFreightsList = new ArrayList<FreightFilterDto>();
    XSSFWorkbook workbook = new XSSFWorkbook(multipartFile.getInputStream());
    XSSFSheet worksheet = workbook.getSheetAt(0);
    FreightFilterDto freightFilterDto = new FreightFilterDto();
    FreightInfoDto freightInfoDto = null;
    for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {
      freightInfoDto = new FreightInfoDto();
      XSSFRow row = worksheet.getRow(i);

      //transporterSapCode
      if (row.getCell(0) != null) {
        freightInfoDto.setTransporterSapCode(row.getCell(0).getStringCellValue());
      } else {
        freightInfoDto.setTransporterSapCode("");
      }

      //servprov
      if (row.getCell(1) != null) {
        freightInfoDto.setServprov(row.getCell(1).getStringCellValue());
      } else {
        freightInfoDto.setServprov("");
      }

      //sourceLoc
      if (row.getCell(2) != null) {
        freightInfoDto.setSourceLoc(row.getCell(2).getStringCellValue());
      } else {
        freightInfoDto.setSourceLoc("");
      }

      //destLoc
      if (row.getCell(3) != null) {
        freightInfoDto.setDestLoc(row.getCell(3).getStringCellValue());
      } else {
        freightInfoDto.setDestLoc("");
      }

      //truckType
      if (row.getCell(4) != null) {
        freightInfoDto.setTruckType(row.getCell(4).getStringCellValue());
      } else {
        freightInfoDto.setTruckType("");
      }

      //condition1
      if (row.getCell(5) != null) {
        freightInfoDto.setCondition1(row.getCell(5).getStringCellValue());
      } else {
        freightInfoDto.setCondition1("");
      }

      //effectiveDate
      if (row.getCell(6) != null) {
        freightInfoDto.setEffectiveDate(row.getCell(6).getDateCellValue());
      } else {
        freightInfoDto.setEffectiveDate(new Date());
      }

      //expiryDate
      if (row.getCell(7) != null) {
        freightInfoDto.setExpiryDate(row.getCell(7).getDateCellValue());
      } else {
        freightInfoDto.setExpiryDate(new Date());
      }

      //ttDays
      if (row.getCell(8) != null) {
        freightInfoDto.setTtDays(Double.valueOf(row.getCell(8).getStringCellValue()));
      } else {
        freightInfoDto.setTtDays(Double.valueOf("0"));
      }


      //baseFreight
      if (row.getCell(9) != null) {
        freightInfoDto.setBaseFreight(Double.valueOf(row.getCell(9).getStringCellValue()));
      } else {
        freightInfoDto.setBaseFreight(Double.valueOf("0"));
      }


      //baseFreightUom
      if (row.getCell(10) != null) {
        freightInfoDto.setBaseFreightUom(row.getCell(10).getStringCellValue());
      } else {
        freightInfoDto.setBaseFreightUom("");
      }


      //basis
      if (row.getCell(11) != null) {
        freightInfoDto.setBasis(row.getCell(11).getStringCellValue());
      } else {
        freightInfoDto.setBasis("");
      }

      //minValue
      if (row.getCell(12) != null) {
        freightInfoDto.setMinValue(Double.valueOf(row.getCell(12).getStringCellValue()));
      } else {
        freightInfoDto.setMinValue(Double.valueOf("0"));
      }

      //minValueUom
      if (row.getCell(13) != null) {
        freightInfoDto.setMinValueUom(row.getCell(13).getStringCellValue());
      } else {
        freightInfoDto.setMinValueUom("");
      }

      //transportMode
      if (row.getCell(14) != null) {
        freightInfoDto.setTransportMode(row.getCell(14).getStringCellValue());
      } else {
        freightInfoDto.setTransportMode("");
      }
		/*
		  System.out.println(row.getCell(0).getStringCellValue());
		  System.out.println(row.getCell(1).getStringCellValue());
		  System.out.println(row.getCell(2).getStringCellValue());
		  System.out.println(row.getCell(3).getStringCellValue());
		  System.out.println(row.getCell(4).getStringCellValue());
		  //System.out.println(row.getCell(5).getStringCellValue()); // condition1
		  System.out.println(row.getCell(6).getDateCellValue()); //effective date
		  System.out.println(row.getCell(7).getDateCellValue()); //expiry date
		  System.out.println(row.getCell(8).getStringCellValue());
		  System.out.println(row.getCell(9).getStringCellValue());
		  System.out.println(row.getCell(10).getStringCellValue());
		  System.out.println(row.getCell(11).getStringCellValue());
		  System.out.println(row.getCell(12).getStringCellValue());
		  System.out.println(row.getCell(13).getStringCellValue());
		  System.out.println(row.getCell(14).getStringCellValue());
		  //System.out.println(row.getCell(15).getStringCellValue());
		*/

      freightFilterDto.freights.add(freightInfoDto);

    }

    Integer totalRecords = freightFilterDto.freights.size();

    if (freightFilterDto.freights.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please upload a valid document")));
    return new ApiResponse(HttpStatus.OK, "Item Saved", this.callFreightUploadProcedure(freightFilterDto, totalRecords, loggedInUser));

  }

  private FreightResultDto callFreightUploadProcedure(FreightFilterDto freightFilterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.upload_freight_data");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    //storedProcedure.registerStoredProcedureParameter("p_tot_error_records", Integer.class, ParameterMode.OUT);
    //storedProcedure.registerStoredProcedureParameter("p_error_out", Clob.class, ParameterMode.OUT);

    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(freightFilterDto)));
    System.out.println(new Gson().toJson(freightFilterDto));
    storedProcedure.setParameter("p_root_element", "freights");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    //System.out.println(storedProcedure.getOutputParameterValue("p_error_out"));
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.FREIGHT_MASTER_PAGE_LIMIT));
    System.out.println("freightTempRepository.findByStatus executed successfully");
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }

  @Override
  public ApiResponse uploadFreightFileAsJson(FreightFilterDto freightFilterDto, ApplicationUser loggedInUser) {
    if (freightFilterDto.freights.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = freightFilterDto.freights.size();
//    for (FreightInfoDto ftdto : freightFilterDto.freights) {
//      System.out.println("ID: " + ftdto.getId() + "  " + ftdto.getBaseFreight());
//    }
    System.out.println("BEFORE CALLING CALLABLE STATEMENT:  " + new Gson().toJson(freightFilterDto));
    return new ApiResponse(HttpStatus.OK, "Freights Saved", this.callFreightUploadProcedure(freightFilterDto, totalRecords, loggedInUser));
  }

  @Override
  public ApiResponse approveFreightJson(FreightFilterDto freightFilterDto, ApplicationUser loggedInUser) {
    if (freightFilterDto.freights.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = freightFilterDto.freights.size();
    System.out.println("totalRecords: " + totalRecords);
    return new ApiResponse(HttpStatus.OK, "Freights Status Saved", this.approveFreightJson(freightFilterDto, totalRecords, loggedInUser));
  }

  private FreightResultDto approveFreightJson(FreightFilterDto freightFilterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_freight_status");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    //storedProcedure.registerStoredProcedureParameter("p_tot_error_records", Integer.class, ParameterMode.OUT);
    //storedProcedure.registerStoredProcedureParameter("p_error_out", Clob.class, ParameterMode.OUT);

    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(freightFilterDto)));


    System.out.println(new Gson().toJson(freightFilterDto));

    storedProcedure.setParameter("p_root_element", "freights");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();

    //System.out.println(storedProcedure.getOutputParameterValue("p_error_out"));

    //Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE",PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    //System.out.println("freightTempRepository.findByStatus executed successfully");
    return new FreightResultDto(storedProcedure, totalRecords);
  }

  @Override
  public ApiResponse getTransportersInfo(TransporterFilterDto transporterFilterDto) {
    Page<MTTransporter> pageableTransporters = filterService.filterTransporters(transporterFilterDto);
    return new ApiResponse(HttpStatus.OK, "Freights List", new TransporterFilterDto(transporterFilterDto, pageableTransporters));
  }


  @Override
  public ApiResponse getMTItem(MtItemFilterDto itemFilterDto) {
    Page<MTItem> pageableItems = filterService.getMTItem(itemFilterDto, itemFilterDto.getItemId(), itemFilterDto.getDescription(), itemFilterDto.getClassification(), itemFilterDto.getType(), itemFilterDto.getGroup(), itemFilterDto.getCategory(), itemFilterDto.getTte(), itemFilterDto.getLoadfactor()
        //,PageRequest.of(itemFilterDto.getIndex(),itemFilterDto.getPageLength(), Sort.Direction.DESC, "ITEM_ID")
    );
    return new ApiResponse(HttpStatus.OK, "Item List", new MtItemFilterDto(itemFilterDto, pageableItems));
  }


  @Override
  public ApiResponse getMTOeBomInfo(MTOeBomFilterDto mtOeBomFilterDto) {
    Page<MTOeBom> pageableFreights = filterService.getFilteredMTOeBom(mtOeBomFilterDto);
    return new ApiResponse(HttpStatus.OK, "MTOeBom List", new MTOeBomFilterDto(mtOeBomFilterDto, pageableFreights));
  }

  @Override
  public ApiResponse getMTRepBomInfo(MTRepBomFilterDto mtRepBomFilterDto) {
    Page<MTRepBom> pageableFreights = filterService.getFilteredMTRepBom(mtRepBomFilterDto);
    return new ApiResponse(HttpStatus.OK, "MTRepBom List", new MTRepBomFilterDto(mtRepBomFilterDto, pageableFreights));
  }

  @Override
  public ApiResponse getMTPlantItemInfo(MTPlantItemFilterDto mtPlantItemFilterDto) {
    Page<MTPlantItem> pageableFreights = filterService.getFilteredMTPlantItem(mtPlantItemFilterDto);
    return new ApiResponse(HttpStatus.OK, "MTPlantItem List", new MTPlantItemFilterDto(mtPlantItemFilterDto, pageableFreights));
  }


  @Override
  public ApiResponse getLocationScan(LocationScanFilterDto filterDto) {
    Page<LocationScan> pageableFreights = filterService.getLocationScan(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new LocationScanFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse addOrUploadLocationScan(LocationScanFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.locationScanList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.locationScanList.size();
    System.out.println("BEFORE CALLING CALLABLE STATEMENT:  " + new Gson().toJson(filterDto));
    return new ApiResponse(HttpStatus.OK, "Location Saved Or Updated", this.addOrUploadLocationScanProcedure(filterDto, totalRecords, loggedInUser));
  }

  @Override
  public ApiResponse updateLocationScan(LocationScanFilterDto filterDto, ApplicationUser loggedInUser) {
    System.out.println("BEFORE CALLING CALLABLE STATEMENT:  " + new Gson().toJson(filterDto));
    if (filterDto.locationScanList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.locationScanList.size();
    return new ApiResponse(HttpStatus.OK, "Location Scan Updated", this.updateLocationScanProcedure(filterDto, totalRecords, loggedInUser));
  }


  private ResultsDto addOrUploadLocationScanProcedure(LocationScanFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.upload_location_scan_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "locationScanList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());

      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    //System.out.println("*************MtExcessWaitingLocLimit responseMessage "+responseMessage);
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }
//    storedProcedure.execute();
//    //System.out.println(storedProcedure.getOutputParameterValue("p_error_out"));
//    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
//    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
//  }

  private ResultsDto updateLocationScanProcedure(LocationScanFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_location_scan"); // yet to get from mangaiah
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "locationScanList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  @Override
  public ApiResponse getMtMaterialGroup(MtMaterialGroupFilterDto filterDto) {
    Page<MtMaterialGroup> pageableFreights = filterService.getMtMaterialGroup(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new MtMaterialGroupFilterDto(filterDto, pageableFreights));
  }


  @Override
  public ApiResponse uploadMtMaterialGroup(MtMaterialGroupFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.mtMaterialGroupList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.mtMaterialGroupList.size();
    return new ApiResponse(HttpStatus.OK, "Material Group Saved", this.uploadMtMaterialGroupProcedure(filterDto, totalRecords, loggedInUser));
  }

  @Override
  public ApiResponse updateMtMaterialGroup(MtMaterialGroupFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.mtMaterialGroupList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.mtMaterialGroupList.size();
    //System.out.println("BEFORE CALLING CALLABLE STATEMENT:  " + new Gson().toJson(filterDto));
    return new ApiResponse(HttpStatus.OK, "Material Group Updated", this.updateMtMaterialGroupProcedure(filterDto, totalRecords, loggedInUser));
  }

  @Override
  public ApiResponse getMtBatchCodes(MtBatchCodesFilterDto filterDto) {
    Page<MtBatchCodes> pageableFreights = filterService.getMtBatchCodes(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new MtBatchCodesFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse addOrUploadMtBatchCodes(MtBatchCodesFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    for (MtBatchCodesInfoDto ftdto : filterDto.itemsList) {
      System.out.println("ID: " + ftdto.getBatchCode() + "  " + ftdto.getCategory());
    }
    System.out.println("BEFORE CALLING CALLABLE STATEMENT:  " + new Gson().toJson(filterDto));
    return new ApiResponse(HttpStatus.OK, "Batch Code Saved Or Updated", this.addOrUploadMtBatchCodesProcedure(filterDto, totalRecords, loggedInUser));
  }

  @Override
  public ApiResponse updateMtBatchCodes(MtBatchCodesFilterDto filterDto, ApplicationUser loggedInUser) {
    System.out.println("BEFORE CALLING CALLABLE STATEMENT MtBatchCodes:  " + new Gson().toJson(filterDto));
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    for (MtBatchCodesInfoDto ftdto : filterDto.itemsList) {
      System.out.println("ID: " + ftdto.getBatchCode() + "  " + ftdto.getCategory());
    }
    System.out.println("BEFORE CALLING CALLABLE STATEMENT MtBatchCodes:  " + new Gson().toJson(filterDto));
    return new ApiResponse(HttpStatus.OK, "Batch Code Updated", this.updateMtBatchCodesProcedure(filterDto, totalRecords, loggedInUser));
  }


  @Override
  public ApiResponse getMtSapTruckType(MtSapTruckTypeFilterDto filterDto) {
    Page<MtSapTruckType> pageableFreights = filterService.getMtSapTruckType(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new MtSapTruckTypeFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse addOrUploadMtSapTruckType(MtSapTruckTypeFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Sap Truck Type Saved Or Updated", this.addOrUploadMtSapTruckTypeProcedure(filterDto, totalRecords, loggedInUser));
  }

  @Override
  public ApiResponse updateMtSapTruckType(MtSapTruckTypeFilterDto filterDto, ApplicationUser loggedInUser) {
    System.out.println("BEFORE CALLING CALLABLE STATEMENT MtSapTruckType:  " + new Gson().toJson(filterDto));
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "SAP Truck Type Updated", this.updateMtSapTruckTypeProcedure(filterDto, totalRecords, loggedInUser));
  }

  @Override
  public ApiResponse getMtTruckType(MtTruckTypeFilterDto filterDto) {
    Page<MtTruckType> pageableFreights = filterService.getMtTruckType(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new MtTruckTypeFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse addOrUploadMtTruckType(MtTruckTypeFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Truck Type Saved", this.addOrUploadMtTruckTypeProcedure(filterDto, totalRecords, loggedInUser));
  }

  @Override
  public ApiResponse updateMtTruckType(MtTruckTypeFilterDto filterDto, ApplicationUser loggedInUser) {
    System.out.println("BEFORE CALLING CALLABLE STATEMENT MtTruckType:  " + new Gson().toJson(filterDto));
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    for (MtTruckTypeInfoDto ftdto : filterDto.itemsList) {
      System.out.println("ID: " + ftdto.getTruckType() + "  " + ftdto.getTruckDesc());
    }
    System.out.println("BEFORE CALLING CALLABLE STATEMENT:  " + new Gson().toJson(filterDto));
    return new ApiResponse(HttpStatus.OK, "Truck Type Updated", this.updateMtTruckTypeProcedure(filterDto, totalRecords, loggedInUser));
  }

  @Override
  public ApiResponse getOrderTypeLookup(OrderTypeLookupFilterDto filterDto) {
    Page<OrderTypeLookup> pageableFreights = filterService.getOrderTypeLookup(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new OrderTypeLookupFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse getMtValve(MTValveFilterDto filterDto) {
    Page<MTValve> pageableFreights = filterService.getMTValve(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new MTValveFilterDto(filterDto, pageableFreights));
  }


  @Override
  public ApiResponse uploadOrderTypeLookup(OrderTypeLookupFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Order types Saved", this.uploadOrderTypeLookupProcedure(filterDto, totalRecords, loggedInUser));
  }

  @Override
  public ApiResponse updateOrderTypeLookup(OrderTypeLookupFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Order type updated", this.updateOrderTypeLookupProcedure(filterDto, totalRecords, loggedInUser));
  }

  @Override
  public ApiResponse uploadMtValve(MTValveFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, " Valve Saved", this.uploadMTValveProcedure(filterDto, totalRecords, loggedInUser));
  }

  @Override
  public ApiResponse updateMtValve(MTValveFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Vale updated ", this.updateMTValveProcedure(filterDto, totalRecords, loggedInUser));
  }

  @Override
  public ApiResponse updateTransporter(TransporterFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    System.out.println("BEFORE CALLING CALLABLE STATEMENT:  " + new Gson().toJson(filterDto));
    return new ApiResponse(HttpStatus.OK, "Servprov updated ", this.updateTransporterProcedure(filterDto, totalRecords, loggedInUser));
  }

  private ResultsDto addOrUploadMtBatchCodesProcedure(MtBatchCodesFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.upload_batch_code_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  private ResultsDto updateMtBatchCodesProcedure(MtBatchCodesFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_batch_code_data"); // yet to get from mangaiah
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }


  private ResultsDto addOrUploadMtSapTruckTypeProcedure(MtSapTruckTypeFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.upload_sap_truck_type_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }


  private ResultsDto updateMtSapTruckTypeProcedure(MtSapTruckTypeFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_sap_truck_type_data"); // yet to get from mangaiah
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }


  private ResultsDto addOrUploadMtTruckTypeProcedure(MtTruckTypeFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_truck_type_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }


  private ResultsDto updateMtTruckTypeProcedure(MtTruckTypeFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_truck_type_data"); // yet to get from mangaiah
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  private ResultsDto uploadMtMaterialGroupProcedure(MtMaterialGroupFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    System.out.println(new Gson().toJson(filterDto));
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.upload_material_group_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      storedProcedure.setParameter("p_root_element", "mtMaterialGroupList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  private ResultsDto updateMtMaterialGroupProcedure(MtMaterialGroupFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    System.out.println(new Gson().toJson(filterDto));
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_material_group_data"); // yet to get from mangaiah
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "mtMaterialGroupList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  private ResultsDto uploadOrderTypeLookupProcedure(OrderTypeLookupFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_order_type_lookup_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }


  private ResultsDto updateOrderTypeLookupProcedure(OrderTypeLookupFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_order_type_lookup_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }


  private ResultsDto updateMTValveProcedure(MTValveFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_mt_valve_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }


  private ResultsDto uploadMTValveProcedure(MTValveFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_mt_valve_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  private ResultsDto updateTransporterProcedure(TransporterFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_transporter_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  @Override
  public ApiResponse getMtElr(MtElrFilterDto filterDto) {
    Page<MtElr> pageableFreights = filterService.getMtElr(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new MtElrFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse uploadMtElr(MtElrFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "ELR Saved", this.uploadMtElrProcedure(filterDto, totalRecords, loggedInUser));
  }

  private ResultsDto uploadMtElrProcedure(MtElrFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.upload_elr_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  @Override
  public ApiResponse updateMtElr(MtElrFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "ELR updated ", this.updateMtElrProcedure(filterDto, totalRecords, loggedInUser));
  }


  private ResultsDto updateMtElrProcedure(MtElrFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_elr_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  @Override
  public ApiResponse getMtExcessWaitingLocLimit(MtExcessWaitingLocLimitFilterDto filterDto) {
    Page<MtExcessWaitingLocLimit> pageableFreights = filterService.getMtExcessWaitingLocLimit(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new MtExcessWaitingLocLimitFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse uploadMtExcessWaitingLocLimit(MtExcessWaitingLocLimitFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Excess Waiting Loc Limit Saved", this.uploadMtExcessWaitingLocLimitProcedure(filterDto, totalRecords, loggedInUser));
  }

  private ResultsDto uploadMtExcessWaitingLocLimitProcedure(MtExcessWaitingLocLimitFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_excess_waiting_loc_limit_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    //System.out.println("*************MtExcessWaitingLocLimit responseMessage "+responseMessage);
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  @Override
  public ApiResponse updateMtExcessWaitingLocLimit(MtExcessWaitingLocLimitFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Excess Waiting Loc Limit updated ", this.updateMtExcessWaitingLocLimitProcedure(filterDto, totalRecords, loggedInUser));
  }

  private ResultsDto updateMtExcessWaitingLocLimitProcedure(MtExcessWaitingLocLimitFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_excess_waiting_loc_limit_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  @Override
  public ApiResponse getMtExcessWaitingRepLimit(MtExcessWaitingRepLimitFilterDto filterDto) {
    Page<MtExcessWaitingRepLimit> pageableFreights = filterService.getMtExcessWaitingRepLimit(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new MtExcessWaitingRepLimitFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse uploadMtExcessWaitingRepLimit(MtExcessWaitingRepLimitFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Excess Waiting Rep Limit Saved", this.uploadMtExcessWaitingRepLimitProcedure(filterDto, totalRecords, loggedInUser));
  }

  private ResultsDto uploadMtExcessWaitingRepLimitProcedure(MtExcessWaitingRepLimitFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_excess_waiting_rep_limit_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  @Override
  public ApiResponse updateMtExcessWaitingRepLimit(MtExcessWaitingRepLimitFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Excess Waiting Rep Limit updated ", this.updateMtExcessWaitingRepLimitProcedure(filterDto, totalRecords, loggedInUser));
  }


  private ResultsDto updateMtExcessWaitingRepLimitProcedure(MtExcessWaitingRepLimitFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_excess_waiting_rep_limit_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  @Override
  public ApiResponse getMtLocationBay(MtLocationBayFilterDto filterDto) {
    Page<MTLocationBayEntity> pageableFreights = filterService.getMtLocationBay(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new MtLocationBayFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse uploadMtLocationBay(MtLocationBayFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Location Bay Saved", this.uploadMtLocationBayProcedure(filterDto, totalRecords, loggedInUser));
  }

  private ResultsDto uploadMtLocationBayProcedure(MtLocationBayFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.upload_location_bay_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  @Override
  public ApiResponse deleteMtLocationBay(MtLocationBayFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Location Bay Deleted ", this.deleteMtLocationBayProcedure(filterDto, totalRecords, loggedInUser));
  }


  private ResultsDto deleteMtLocationBayProcedure(MtLocationBayFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.delete_location_bay_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  @Override
  public ApiResponse getMtLocation(MtLocationFilterDto filterDto) {
    Page<MTLocation> pageableFreights = filterService.getMtLocation(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new MtLocationFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse uploadMtLocation(MtLocationFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Location Saved", this.uploadMtLocationProcedure(filterDto, totalRecords, loggedInUser));
  }

  private ResultsDto uploadMtLocationProcedure(MtLocationFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_location_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  @Override
  public ApiResponse updateMtLocation(MtLocationFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Location updated ", this.updateMtLocationProcedure(filterDto, totalRecords, loggedInUser));
  }

  @Override
  public ApiResponse getDistinctMarketSegments(ApplicationUser loggedInUser) throws Exception {
    List<String> mktSegs = orderTypeLookupRepository.getAllDistinctMKTSEG();
    return new ApiResponse(HttpStatus.OK, "", mktSegs);
  }

  private ResultsDto updateMtLocationProcedure(MtLocationFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_location_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  @Override
  public ApiResponse getCtOtmFreightBasis(CtOtmFreightBasisFilterDto filterDto) {
    Page<CtOtmFreightBasis> pageableFreights = filterService.getCtOtmFreightBasis(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new CtOtmFreightBasisFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse uploadCtOtmFreightBasis(CtOtmFreightBasisFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Mt Valve Saved", this.uploadCtOtmFreightBasisProcedure(filterDto, totalRecords, loggedInUser));
  }

  private FreightResultDto uploadCtOtmFreightBasisProcedure(CtOtmFreightBasisFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_mt_valve_data");
    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(filterDto)));
    System.out.println(new Gson().toJson(filterDto));
    storedProcedure.setParameter("p_root_element", "itemsList");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }


  @Override
  public ApiResponse updateCtOtmFreightBasis(CtOtmFreightBasisFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Mt Vale updated ", this.updateCtOtmFreightBasisProcedure(filterDto, totalRecords, loggedInUser));
  }


  private FreightResultDto updateCtOtmFreightBasisProcedure(CtOtmFreightBasisFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_mt_valve_data");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(filterDto)));
    System.out.println(new Gson().toJson(filterDto));
    storedProcedure.setParameter("p_root_element", "itemsList");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }

  @Override
  public ApiResponse getCtUom(CtUomFilterDto filterDto) {
    Page<CtUom> pageableFreights = filterService.getCtUom(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new CtUomFilterDto(filterDto, pageableFreights));
  }


  @Override
  public ApiResponse uploadCtUom(CtUomFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Mt Valve Saved", this.uploadCtUomProcedure(filterDto, totalRecords, loggedInUser));
  }

  private FreightResultDto uploadCtUomProcedure(CtUomFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_mt_valve_data");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(filterDto)));
    System.out.println(new Gson().toJson(filterDto));
    storedProcedure.setParameter("p_root_element", "itemsList");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }


  @Override
  public ApiResponse updateCtUom(CtUomFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Mt Vale updated ", this.updateCtUomProcedure(filterDto, totalRecords, loggedInUser));
  }


  private FreightResultDto updateCtUomProcedure(CtUomFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_mt_valve_data");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(filterDto)));
    System.out.println(new Gson().toJson(filterDto));
    storedProcedure.setParameter("p_root_element", "itemsList");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }

  @Override
  public ApiResponse getCtUomMap(CtUomMapFilterDto filterDto) {
    Page<CtUomMap> pageableFreights = filterService.getCtUomMap(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new CtUomMapFilterDto(filterDto, pageableFreights));
  }


  @Override
  public ApiResponse uploadCtUomMap(CtUomMapFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Mt Valve Saved", this.uploadCtUomMapProcedure(filterDto, totalRecords, loggedInUser));
  }

  private FreightResultDto uploadCtUomMapProcedure(CtUomMapFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_mt_valve_data");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(filterDto)));
    System.out.println(new Gson().toJson(filterDto));
    storedProcedure.setParameter("p_root_element", "itemsList");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }


  @Override
  public ApiResponse updateCtUomMap(CtUomMapFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Mt Vale updated ", this.updateCtUomMapProcedure(filterDto, totalRecords, loggedInUser));
  }


  private FreightResultDto updateCtUomMapProcedure(CtUomMapFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_mt_valve_data");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(filterDto)));
    System.out.println(new Gson().toJson(filterDto));
    storedProcedure.setParameter("p_root_element", "itemsList");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }


  @Override
  public ApiResponse getMtIncoterms(MtIncotermsFilterDto filterDto) {
    Page<MtIncoterms> pageableFreights = filterService.getMtIncoterms(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new MtIncotermsFilterDto(filterDto, pageableFreights));
  }


  @Override
  public ApiResponse uploadMtIncoterms(MtIncotermsFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Mt Valve Saved", this.uploadMtIncotermsProcedure(filterDto, totalRecords, loggedInUser));
  }

  private FreightResultDto uploadMtIncotermsProcedure(MtIncotermsFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_mt_valve_data");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(filterDto)));
    System.out.println(new Gson().toJson(filterDto));
    storedProcedure.setParameter("p_root_element", "itemsList");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }

  @Override
  public ApiResponse updateMtIncoterms(MtIncotermsFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Mt Vale updated ", this.updateMtIncotermsProcedure(filterDto, totalRecords, loggedInUser));
  }


  private FreightResultDto updateMtIncotermsProcedure(MtIncotermsFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_mt_valve_data");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(filterDto)));
    System.out.println(new Gson().toJson(filterDto));
    storedProcedure.setParameter("p_root_element", "itemsList");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }

  @Override
  public ApiResponse getMtTruck(MtTruckFilterDto filterDto) {
    Page<MTTruck> pageableFreights = filterService.getMtTruck(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new MtTruckFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse uploadMtTruck(MtTruckFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Mt Valve Saved", this.uploadMtTruckProcedure(filterDto, totalRecords, loggedInUser));
  }

  private FreightResultDto uploadMtTruckProcedure(MtTruckFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_mt_valve_data");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(filterDto)));
    System.out.println(new Gson().toJson(filterDto));
    storedProcedure.setParameter("p_root_element", "itemsList");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }


  @Override
  public ApiResponse updateMtTruck(MtTruckFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Mt Vale updated ", this.updateMtTruckProcedure(filterDto, totalRecords, loggedInUser));
  }


  private FreightResultDto updateMtTruckProcedure(MtTruckFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_mt_valve_data");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(filterDto)));
    System.out.println(new Gson().toJson(filterDto));
    storedProcedure.setParameter("p_root_element", "itemsList");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }

  @Override
  public ApiResponse getMtContact(MtContactFilterDto filterDto) {
    Page<MtContact> pageableFreights = filterService.getMtContact(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new MtContactFilterDto(filterDto, pageableFreights));
  }


  @Override
  public ApiResponse uploadMtContact(MtContactFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Mt Valve Saved", this.uploadMtContactProcedure(filterDto, totalRecords, loggedInUser));
  }

  private FreightResultDto uploadMtContactProcedure(MtContactFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_mt_valve_data");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(filterDto)));
    System.out.println(new Gson().toJson(filterDto));
    storedProcedure.setParameter("p_root_element", "itemsList");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }


  @Override
  public ApiResponse updateMtContact(MtContactFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Mt Vale updated ", this.updateMtContactProcedure(filterDto, totalRecords, loggedInUser));
  }


  private FreightResultDto updateMtContactProcedure(MtContactFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_mt_valve_data");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(filterDto)));
    System.out.println(new Gson().toJson(filterDto));
    storedProcedure.setParameter("p_root_element", "itemsList");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }

  @Override
  public ApiResponse getMtCustomer(MtCustomerFilterDto filterDto) {
    Page<MTCustomer> pageableFreights = filterService.getMtCustomer(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new MtCustomerFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse uploadMtCustomer(MtCustomerFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Mt Valve Saved", this.uploadMtCustomerProcedure(filterDto, totalRecords, loggedInUser));
  }

  private FreightResultDto uploadMtCustomerProcedure(MtCustomerFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_mt_valve_data");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(filterDto)));
    System.out.println(new Gson().toJson(filterDto));
    storedProcedure.setParameter("p_root_element", "itemsList");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }

  @Override
  public ApiResponse updateMtCustomer(MtCustomerFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Mt Vale updated ", this.updateMtCustomerProcedure(filterDto, totalRecords, loggedInUser));
  }

  private FreightResultDto updateMtCustomerProcedure(MtCustomerFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_mt_valve_data");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(filterDto)));
    System.out.println(new Gson().toJson(filterDto));
    storedProcedure.setParameter("p_root_element", "itemsList");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }


  @Override
  public ApiResponse getMtCustomerShipTo(MtCustomerShipToFilterDto filterDto) {
    Page<MtCustomerShipTo> pageableFreights = filterService.getMtCustomerShipTo(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new MtCustomerShipToFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse getTruckStatusLocs(ApplicationUser applicationUser) {
    return new ApiResponse(HttpStatus.OK, "", truckReportSummaryRepository.findAllTruckStatusLocs());
  }


  @Override
  public ApiResponse uploadMtCustomerShipTo(MtCustomerShipToFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Mt Valve Saved", this.uploadMtCustomerShipToProcedure(filterDto, totalRecords, loggedInUser));
  }

  private FreightResultDto uploadMtCustomerShipToProcedure(MtCustomerShipToFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_mt_valve_data");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(filterDto)));
    System.out.println(new Gson().toJson(filterDto));
    storedProcedure.setParameter("p_root_element", "itemsList");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }


  @Override
  public ApiResponse updateMtCustomerShipTo(MtCustomerShipToFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "Mt Vale updated ", this.updateMtCustomerShipToProcedure(filterDto, totalRecords, loggedInUser));
  }

  private FreightResultDto updateMtCustomerShipToProcedure(MtCustomerShipToFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_mt_valve_data");

    storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
    storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
        .createClob(new Gson().toJson(filterDto)));
    System.out.println(new Gson().toJson(filterDto));
    storedProcedure.setParameter("p_root_element", "itemsList");
    storedProcedure.setParameter("p_user", loggedInUser.getUserId());
    storedProcedure.execute();
    Page<FreightTemp> pageableFreightTemps = freightTempRepository.findByStatus("VE", PageRequest.of(0, Constants.MASTER_PAGE_LIMIT));
    return new FreightResultDto(storedProcedure, pageableFreightTemps, totalRecords);
  }


  @Override
  public ApiResponse getUser(UserFilterDto filterDto) {
    Page<UserEntity> pageableFreights = filterService.getUser(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new UserFilterDto(filterDto, pageableFreights));
  }


  @Override
  public ApiResponse uploadUser(UserFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    UserInfoDto userInfoDto = filterDto.itemsList.get(0);
    String userid = userInfoDto.getUserId().trim();
    String password = userInfoDto.getPassword().trim();
    byte[] base64Password = Base64.getEncoder().encode((userid + ":" + password).getBytes());
    filterDto.itemsList.get(0).setPassword(new String(base64Password));
    return new ApiResponse(HttpStatus.OK, "User Saved", this.uploadUserProcedure(filterDto, totalRecords, loggedInUser));
  }

  private ResultsDto uploadUserProcedure(UserFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_user_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }


  @Override
  public ApiResponse updateUser(UserFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    UserInfoDto userInfoDto = filterDto.itemsList.get(0);
    if (userInfoDto.getPassword() != null) {
      String userid = userInfoDto.getUserId().trim();
      String password = userInfoDto.getPassword().trim();
      byte[] base64Password = Base64.getEncoder().encode((userid + ":" + password).getBytes());
      filterDto.itemsList.get(0).setPassword(new String(base64Password));
    }
    return new ApiResponse(HttpStatus.OK, "User updated ", this.updateUserProcedure(filterDto, totalRecords, loggedInUser));
  }

//  @Override
//  public ApiResponse updatePassword(UserFilterDto filterDto, ApplicationUser loggedInUser) {
//    String userid = filterDto.getUserId().trim();
//    String password = filterDto.getPassword().trim();
//    //System.out.println("*************userid  "+userid);
//    //System.out.println("*************password "+password);
//    byte[] base64Password = Base64.getEncoder().encode((userid + ":" + password).getBytes());
//    System.out.println("*************"+ new String(base64Password));
//    filterDto.setPassword(new String(base64Password));
//    int i = filterService.updatePassword(filterDto);
//    if(i ==1){
//      return new ApiResponse(HttpStatus.OK, "Password updated ");
//    }else{
//      return new ApiResponse(HttpStatus.OK, "Password updated failed");
//    }
//  }


  @Override
  public ApiResponse updatePassword(UserFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    UserInfoDto userInfoDto = filterDto.itemsList.get(0);
    if (userInfoDto.getPassword() != null) {
      String userid = userInfoDto.getUserId().trim();
      String password = userInfoDto.getPassword().trim();
      byte[] base64Password = Base64.getEncoder().encode((userid + ":" + password).getBytes());
      filterDto.itemsList.get(0).setPassword(new String(base64Password));
    }
    return new ApiResponse(HttpStatus.OK, "Password updated ", this.updatePasswordProcedure(filterDto, totalRecords, loggedInUser));
  }

  private ResultsDto updatePasswordProcedure(UserFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.change_password");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  private ResultsDto updateUserProcedure(UserFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_user_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  @Override
  public ApiResponse getUserAssociation(UserAssociationFilterDto filterDto) {
    Page<UmUserAssociationEntity> pageableFreights = filterService.getUserAssociation(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new UserAssociationFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse uploadUserAssociation(UserAssociationFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "User Association Saved", this.uploadUserAssociationProcedure(filterDto, totalRecords, loggedInUser));
  }

  private ResultsDto uploadUserAssociationProcedure(UserAssociationFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_user_association_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  @Override
  public ApiResponse updateUserAssociation(UserAssociationFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "User Association updated ", this.updateUserAssociationProcedure(filterDto, totalRecords, loggedInUser));
  }

  private ResultsDto updateUserAssociationProcedure(UserAssociationFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_user_association_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  @Override
  public ApiResponse getUserRole(UserRoleFilterDto filterDto) {
    Page<UmUserRole> pageableFreights = filterService.getUserRole(filterDto);
    return new ApiResponse(HttpStatus.OK, "Item List", new UserRoleFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse uploadUserRole(UserRoleFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "User Role Saved", this.uploadUserRoleProcedure(filterDto, totalRecords, loggedInUser));
  }

  private ResultsDto uploadUserRoleProcedure(UserRoleFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_user_role_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }

  @Override
  public ApiResponse updateUserRole(UserRoleFilterDto filterDto, ApplicationUser loggedInUser) {
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, "User Roleupdated ", this.updateUserRoleProcedure(filterDto, totalRecords, loggedInUser));
  }

  private ResultsDto updateUserRoleProcedure(UserRoleFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.update_user_role_data");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }


  @Override
  public ApiResponse addServProv(MtScacFilterDto filterDto, ApplicationUser loggedInUser) {
    System.out.println(" ***** addServProv  " + new Gson().toJson(filterDto));
    if (filterDto.itemsList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", Lists.newArrayList(("Please submit  valid form data")));
    Integer totalRecords = filterDto.itemsList.size();
    return new ApiResponse(HttpStatus.OK, " Serprov Saved", this.addServProvProcedure(filterDto, totalRecords, loggedInUser));
  }

  private ResultsDto addServProvProcedure(MtScacFilterDto filterDto, Integer totalRecords, ApplicationUser loggedInUser) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("ATL_MASTER_DATA_FLOW_PKG.insert_servprov");
    String responseMessage = "";
    try {
      storedProcedure.registerStoredProcedureParameter("p_json_data", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_root_element", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_tot_records", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_json_data", org.hibernate.engine.jdbc.NonContextualLobCreator.INSTANCE
          .createClob(new Gson().toJson(filterDto)));
      System.out.println(new Gson().toJson(filterDto));
      storedProcedure.setParameter("p_root_element", "itemsList");
      storedProcedure.setParameter("p_user", loggedInUser.getUserId());
      boolean status = storedProcedure.execute();
      responseMessage = "Sucess";
    } catch (Exception e) {
      responseMessage = "Failed";
    }
    return new ResultsDto(storedProcedure, responseMessage, totalRecords);
  }


  @Override
  public ApiResponse getServProList(MtScacFilterDto filterDto) {
    Page<MtScac> pageableFreights = filterService.getServProList(filterDto);
    return new ApiResponse(HttpStatus.OK, "Items List", new MtScacFilterDto(filterDto, pageableFreights));
  }

  @Override
  public ApiResponse getServPros() {
    List<String> list = filterService.getServPros();
    return new ApiResponse(HttpStatus.OK, "Servpros Ids List", list);
  }

  @Override
  public ApiResponse getLocationIdsList() {
    List<String> list = filterService.getLocationIdsList();
    return new ApiResponse(HttpStatus.OK, "Location Ids List", list);
  }

  @Override
  public ApiResponse getLocationClasses() {
    List<String> list = filterService.getLocationClasses();
    return new ApiResponse(HttpStatus.OK, "Location Classes List", list);
  }

  @Override
  public ApiResponse getUserIdsList() {
    List<String> list = filterService.getUserIdsList();
    return new ApiResponse(HttpStatus.OK, "User Ids List", list);
  }

  @Override
  public ApiResponse getUserRoleIdsList() {
    List<String> list = filterService.getUserRoleIdsList();
    return new ApiResponse(HttpStatus.OK, "User Roles List", list);
  }

  @Override
  public ApiResponse getDistinctScmGroupList() {
    List<String> list = filterService.getDistinctScmGroupList();
    return new ApiResponse(HttpStatus.OK, "Distinct SCM Group List", list);
  }

  @Override
  public ApiResponse getPaasTruckTypeList() {
    List<String> list = filterService.getPaasTruckTypeList();
    return new ApiResponse(HttpStatus.OK, "Distinct Paas Truck Type List", list);
  }

  @Override
  public ApiResponse getTruckTypeList() {
    List<String> list = filterService.getTruckTypeList();
    return new ApiResponse(HttpStatus.OK, "Distinct Truck Type List", list);
  }

  @Override
  public ApiResponse getVariantsList() {
    List<String> list = filterService.getVariantsList();
    return new ApiResponse(HttpStatus.OK, "Distinct Variant1 List", list);
  }

  @Override
  public ApiResponse getItemGroupsList() {
    List<String> list = filterService.getItemGroupsList();
    return new ApiResponse(HttpStatus.OK, "Distinct Item Group List", list);
  }

  @Override
  public ApiResponse getbatchCategoryList() {
    List<String> list = filterService.getbatchCategoryList();
    return new ApiResponse(HttpStatus.OK, "Distinct Batch Category List", list);
  }

  @Override
  public ApiResponse getbatchCodesForValves() {
    List<String> list = filterService.getbatchCodesForValves();
    return new ApiResponse(HttpStatus.OK, "Distinct Batch Codes for Category type Valve", list);
  }

  @Override
  public ApiResponse getItemIdList() {
    List<String> list = filterService.getItemIdList();
    return new ApiResponse(HttpStatus.OK, "Distinct Item Ids List", list);
  }

  @Override
  public ApiResponse checkMaterialCodeIsTyre(String materialCode) {
    if (StringUtils.isEmpty(materialCode)) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please provide the material code.");
    }
    if (itemRepo.existsByIdAndClassification(materialCode, "TYRE")) {
      return new ApiResponse(HttpStatus.OK, "Check the batch code", true);
    } else {
      return new ApiResponse(HttpStatus.OK, "Dont check the batch code.", false);
    }
  }

  @Override
  public ApiResponse getBatchCodePrefixes() {
    return new ApiResponse(HttpStatus.OK, "", mtPlantBatchRepository.findAllBatchCodePrefix());
  }

  @Override
  public ApiResponse deleteFreightData(Double rowId, ApplicationUser applicationUser) {
//    If logged in user is not ADMIN/L1MGR/L2MGR
    if (!UserRole.getADMINScreenRoles().contains(applicationUser.getRole())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "You are not allowed to delete the freight record");
    }
    Optional<Freight> optionalFreight = freightRepository.findById(rowId);
    if (!optionalFreight.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "No freight record found with the id: " + rowId);
    }
    if (("Level2 Approved").equals(optionalFreight.get().getStatus())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Cannot delete the record as freight is already approved by L2_MGR!");
    }

    freightRepository.deleteById(optionalFreight.get().getId());
    return new ApiResponse(HttpStatus.OK, "Freight record deleted Successfully!", optionalFreight.get().getId());
  }

  @Override
  public ApiResponse getMTTruckDedicatedData(TruckDedicatedFilterDto truckDedicatedFilterDto, ApplicationUser applicationUser) {

    Page<MTTruckDedicated> mtTruckDedicatedPage = null;
    if (truckDedicatedFilterDto.isFilterService()) {
      mtTruckDedicatedPage = filterService.getFilteredMTTruckDedicated(truckDedicatedFilterDto, applicationUser);
    } else {
      mtTruckDedicatedPage = mtTruckDedidatedRepository.findAll(PageRequest.of(truckDedicatedFilterDto.getIndex(), truckDedicatedFilterDto.getPageLength(), Sort.Direction.DESC, "insertDate"));
    }
    return new ApiResponse(HttpStatus.OK, "", new MTTruckDedicatedData(mtTruckDedicatedPage));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse saveMTTruckDedicated(MTTruckDedicatedDto mtTruckDedicatedDto, ApplicationUser applicationUser) {
    String errorMsg = validateTruckDedicatedData(mtTruckDedicatedDto);
    if (!StringUtils.isEmpty(errorMsg)) {
      return new ApiResponse(HttpStatus.NOT_FOUND, errorMsg);
    }
    MTTruckDedicated mtTruckDedicated = null;
    if (mtTruckDedicatedDto.getId() != null) {
      Optional<MTTruckDedicated> optionalMTTruckDedicated = mtTruckDedidatedRepository.findById(mtTruckDedicatedDto.getId());
      if (!optionalMTTruckDedicated.isPresent()) {
        return new ApiResponse(HttpStatus.NOT_FOUND, "No Record found with the gived id: " + mtTruckDedicatedDto.getId());
      }
      mtTruckDedicated = optionalMTTruckDedicated.get();
      mtTruckDedicated.setUpdateDate(new Date());
      mtTruckDedicated.setUpdateUser(applicationUser.getUserId());
    } else {
      mtTruckDedicated = new MTTruckDedicated();
      mtTruckDedicated.setInsertUser(applicationUser.getUserId());
//      Insert date is auto generated at db level
//      mtTruckDedicated.setInsertDate(new Date());
    }
    setMTTruckDedicatedDate(mtTruckDedicated, mtTruckDedicatedDto, applicationUser);
    if (mtTruckDedicatedDto.getId() == null) {
     /* mtTruckDedidatedRepository.saveMTTruckDedicated(mtTruckDedicated.getServProv(), mtTruckDedicated.getSourceLoc(), mtTruckDedicated.getDestLoc(),
          mtTruckDedicated.getSourceDesc(), mtTruckDedicated.getDestDesc(), mtTruckDedicated.getTruckType(), mtTruckDedicated.getTruckNumber(), DateUtils.formatDate(mtTruckDedicated.getExpiryDate(), Constants.DATE_FORMATE_WITH_HYPHEN), mtTruckDedicated.getInsertUser());*/

      /* Since ID field is of type IDENTITY at the DB we were unable to use the repo.save() function. As nextVal of ID is not know to JPA.
       * So have implemented it using entityManager.executeUpdate().
       * This will not give the the last INSERTED DATE so fetching all the records and sending it in response*/
      Page<MTTruckDedicated> mtTruckDedicatedPage = this.saveMTTruckDedicated(mtTruckDedicated);
      return new ApiResponse(HttpStatus.OK, "Truck Dedicated record saved successfullt", new MTTruckDedicatedData(mtTruckDedicatedPage));
    } else {
      mtTruckDedicated = mtTruckDedidatedRepository.save(mtTruckDedicated);
    }
    return new ApiResponse(HttpStatus.OK, "Truck Dedicated details updated successfully", new MTTruckDedicatedDto(mtTruckDedicated));
  }

  private String validateTruckDedicatedData(MTTruckDedicatedDto mtTruckDedicatedDto) {
    if (!this.isValidLoc(mtTruckDedicatedDto.getSourceLoc())) {
      return "Source " + mtTruckDedicatedDto.getSourceLoc() + " doesnot exists in the system.";
    }
    if (!this.isValidLoc(mtTruckDedicatedDto.getDestLoc())) {
      return "Destination " + mtTruckDedicatedDto.getDestLoc() + " doesnot exists in the system.";
    }
    if (!transporterRepository.existsByServprov(mtTruckDedicatedDto.getServprov())) {
      return "Transporter " + mtTruckDedicatedDto.getServprov() + " doesnot exists in the system";
    }
    if (!StringUtils.isEmpty(mtTruckDedicatedDto.getSourceLoc()) && !StringUtils.isEmpty(mtTruckDedicatedDto.getDestLoc())) {
      if (mtTruckDedicatedDto.getSourceLoc().equals(mtTruckDedicatedDto.getDestLoc())) {
        return "Destination Loc cannot be same as Source Loc";
      }
    }
    return null;
  }

  @Override
  public ApiResponse deleteMTTruckDedicatedRecord(Long rowId, ApplicationUser applicationUser) {
    if (rowId == null) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please provide the id");
    }
    Optional<MTTruckDedicated> optionalMTTruckDedicated = mtTruckDedidatedRepository.findById(rowId);
    if (!optionalMTTruckDedicated.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "No record found with the id: " + rowId);
    }
    mtTruckDedidatedRepository.deleteById(optionalMTTruckDedicated.get().getId());
    return new ApiResponse(HttpStatus.OK, "Truck Dedicated record deleted successfully!", optionalMTTruckDedicated.get().getId());
  }

  private void setMTTruckDedicatedDate(MTTruckDedicated mtTruckDedicated, MTTruckDedicatedDto mtTruckDedicatedDto, ApplicationUser applicationUser) {
    mtTruckDedicated.setServProv(mtTruckDedicatedDto.getServprov());
    mtTruckDedicated.setSourceLoc(mtTruckDedicatedDto.getSourceLoc());
    mtTruckDedicated.setSourceDesc(locationRepo.findDescriptionByLocId(mtTruckDedicatedDto.getSourceLoc()));
    mtTruckDedicated.setDestLoc(mtTruckDedicatedDto.getDestLoc());
    mtTruckDedicated.setDestDesc(locationRepo.findDescriptionByLocId(mtTruckDedicatedDto.getDestLoc()));
    mtTruckDedicated.setTruckType(mtTruckDedicatedDto.getTruckType());
    mtTruckDedicated.setTruckNumber(mtTruckDedicatedDto.getTruckNumber());
    mtTruckDedicated.setExpiryDate(DateUtils.formatDate(mtTruckDedicatedDto.getExpiryDate(), Constants.PLAN_RECORD_DATE_FORMAT));
  }

  private Page<MTTruckDedicated> saveMTTruckDedicated(MTTruckDedicated mtTruckDedicated) {
    int big = entityManager.createNativeQuery("insert into MT_Truck_Dedicated " +
        "(SERPVORV, source_Loc, dest_Loc, source_Desc, dest_Desc, truck_Type, truck_Number, expiry_Date, insert_User) values " +
        "(:servProv, :sourceLoc, :destLoc, :sourceDesc, :destDesc, :truckType, :truckNumber, :expiryDate, :insertUser) ")

        .setParameter("servProv", mtTruckDedicated.getServProv())
        .setParameter("sourceLoc", mtTruckDedicated.getSourceLoc())
        .setParameter("destLoc", mtTruckDedicated.getDestLoc())
        .setParameter("sourceDesc", mtTruckDedicated.getSourceDesc())
        .setParameter("destDesc", mtTruckDedicated.getDestDesc())
        .setParameter("truckType", mtTruckDedicated.getTruckType())
        .setParameter("truckNumber", mtTruckDedicated.getTruckNumber())
        .setParameter("expiryDate", mtTruckDedicated.getExpiryDate())
        .setParameter("insertUser", mtTruckDedicated.getInsertUser()).executeUpdate();
//    BigDecimal bigDecimal = big != null ? (BigDecimal) big : null;
//    Long id = bigDecimal != null ? bigDecimal.longValue() : 0;
//
//    Optional<MTTruckDedicated> dedicated = null;
//    if (id != 0){
//      dedicated = mtTruckDedidatedRepository.findById(id);
//      if (dedicated.isPresent()){
//        return dedicated.get();
//      }
    //}

    return mtTruckDedidatedRepository.findAll(PageRequest.of(Constants.PAGE_INDEX, Constants.PAGE_LIMIT, Sort.by(Sort.Direction.DESC, "insertDate")));

  }


  @Override
  public ApiResponse updateFreightDetails(FreightInfoDto freightInfoDto, ApplicationUser applicationUser) {
    //    If logged in user is not ADMIN/L1MGR/L2MGR
    if (!UserRole.getADMINScreenRoles().contains(applicationUser.getRole())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "You are not allowed to update the freight record");
    }
    if (freightInfoDto.getId() == null) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please provide the freight id");
    }
    Optional<Freight> optionalFreight = freightRepository.findById(freightInfoDto.getId());
    if (!optionalFreight.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "No record found with the id: " + freightInfoDto.getId());
    }
    optionalFreight.get().setTtDays(freightInfoDto.getTtDays());
    optionalFreight.get().setDistance(freightInfoDto.getDistance());
    if (freightInfoDto.getIsUpdateExpiryDate()) {
      optionalFreight.get().setExpiryDate(DateUtils.formatDateToUTC(freightInfoDto.getExpiryDateString(), Constants.PLAN_RECORD_DATE_FORMAT));
    }
//    after update it should  go to L1 and L2 approve
    optionalFreight.get().setStatus(null);
    optionalFreight.get().setUpdateDate(new Date());
    optionalFreight.get().setUpdateUser(applicationUser.getUserId());

    Freight updateFreight = freightRepository.save(optionalFreight.get());

    return new ApiResponse(HttpStatus.OK, "Freight details updated successfully!", new FreightInfoDto(updateFreight));
  }
  private boolean isValidLoc(String locationId) {
    if (!StringUtils.isEmpty(locationId)) {
      if (locationRepo.existsById(locationId) || mtCustomerRepository.existsById(locationId)) {
        return true;
      }
    }
    return false;
  }


  @Override
  public ApiResponse getMtCustomerDetails(MtCustomerFilterDto mtCustomerFilterDto, ApplicationUser applicationUser) {
    Page<MTCustomer> mtCustomerPage = filterService.filterMTCustomer(mtCustomerFilterDto, applicationUser);

    return new ApiResponse(HttpStatus.OK, "", new MTCustomerResponseDto(mtCustomerPage));
  }

  @Override
  public ApiResponse searchMTCustomer(String locationId, String custName, String city, String state, String custAcctGrp, ApplicationUser applicationUser) {
    if (!StringUtils.isEmpty(locationId)) {
      return new ApiResponse(HttpStatus.OK, "CUST_ID", new MasterDataDto(locationRepo.findAllCustLocationsLike(locationId, PageRequest.of(Constants.PAGE_INDEX, Constants.MASTER_PAGE_LIMIT))));
    }
    if (!StringUtils.isEmpty(custName)){
      return new ApiResponse(HttpStatus.OK, "CUST_NAME", locationRepo.findAllCustNameLike(custName, PageRequest.of(Constants.PAGE_INDEX, Constants.MASTER_PAGE_LIMIT)));
    }
    if (!StringUtils.isEmpty(city)){
      return new ApiResponse(HttpStatus.OK, "CITY", locationRepo.findAllCityLike(city, PageRequest.of(Constants.PAGE_INDEX, Constants.MASTER_PAGE_LIMIT)));
    }
    if (!StringUtils.isEmpty(state)){
      return new ApiResponse(HttpStatus.OK, "STATE", locationRepo.findAllStateLike(state, PageRequest.of(Constants.PAGE_INDEX, Constants.MASTER_PAGE_LIMIT)));
    }
    if (!StringUtils.isEmpty(custAcctGrp)){
      return new ApiResponse(HttpStatus.OK, "CUST_ACCT_GRP", locationRepo.findAllCustAcctGrpLike(custAcctGrp, PageRequest.of(Constants.PAGE_INDEX, Constants.MASTER_PAGE_LIMIT)));
    }
    return new ApiResponse(HttpStatus.NOT_FOUND, "Something went wrong...");
  }

  @Override
  public ApiResponse getTransporterList(ApplicationUser applicationUserFromAuthentication) {
    return new ApiResponse(HttpStatus.OK, "", mtScasRepository.getTransporterList());
  }

  @Override
  public ApiResponse getStateCodes(ApplicationUser applicationUser) {
    return new ApiResponse(HttpStatus.OK, "", mtCustomerRepository.findDistinctStateCode());
  }

  @Override
  public ApiResponse getCustTypes(ApplicationUser applicationUser) {
    return new ApiResponse(HttpStatus.OK, "", mtCustomerRepository.findDistinctCustType());
  }

}
