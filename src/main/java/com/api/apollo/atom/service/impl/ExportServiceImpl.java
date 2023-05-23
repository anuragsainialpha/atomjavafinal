package com.api.apollo.atom.service.impl;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.ops.*;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.master.*;
import com.api.apollo.atom.entity.ops.*;
import com.api.apollo.atom.repository.UserRepository;
import com.api.apollo.atom.repository.master.*;
import com.api.apollo.atom.repository.ops.*;
import com.api.apollo.atom.repository.planner.DelInvHeaderRepository;
import com.api.apollo.atom.service.*;
import com.api.apollo.atom.util.DateUtils;
import com.api.apollo.atom.util.func.TriFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.api.apollo.atom.util.Utility.deriveTubeAndFlapBatchCodes;

@Service
public class ExportServiceImpl implements ExportService {

  @Autowired
  private TruckTypeInfoRepository truckTypeRepo;

  @Autowired
  private ItemRepository itemRepository;
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TransporterRepository transporterRepo;

  @Autowired
  private LocationRepository locationRepository;

  @Autowired
  private IndentSummaryRepository indentSummaryRepository;

  @Autowired
  private IndentDetailsRepository indentDetailsRepo;

  @Autowired
  FilterService filterService;

  @Autowired
  private TruckRepository truckRepo;

  @Autowired
  private MasterDataService masterDataServce;

  @Autowired
  private GpsProviderInfoRepository gpsProviderInfoRepo;

  @Autowired
  private TruckReportRepository truckReportRepo;

  @Autowired
  private ShipmentRepository shipmentRepository;


  @Autowired
  private OpsService opsService;

  @Autowired
  private LoadslipRepository loadslipRepository;

  @Autowired
  private DelInvHeaderRepository delInvHeaderRepository;

  @Autowired
  private UtilityService utilityService;

  @Autowired
  private FreightRepository freightRepository;

  private TriFunction<List<MTTruckTypeInfo>, UploadIndentDto, ApplicationUser, IndentSummary> createIndent = (
      List<MTTruckTypeInfo> masterTruckTypes, UploadIndentDto indentInfoDto, ApplicationUser loggedInUser) -> {
    IndentSummary indentSummary = new IndentSummary();
    // Akshay is generating Indent Id when data pushed to Indent_Summary table in backend db
  /*  indentSummary.setIndentId(indentSummaryRepository.findIndentSequence(Constants.INDENT_SEQ_PREFIX,
        indentInfoDto.getSource(), indentInfoDto.getDestination()));*/
    indentSummary.setDispatchDate(
        DateUtils.formatDate(indentInfoDto.getDispatchDate(), Constants.PLAN_RECORD_DATE_FORMAT));
    indentSummary.setSourceLocation(indentInfoDto.getSource());
    indentSummary.setDestinationLocation(indentInfoDto.getDestination());
    MTTruckTypeInfo masterTruckType = masterTruckTypes.stream().parallel()
        .filter(mt -> mt.getType().equalsIgnoreCase(indentInfoDto.getTruckType())).findFirst().get();
    indentSummary.setTruckType(masterTruckType.getType());
    indentSummary.setIndented(indentInfoDto.getIndented());
    indentSummary.setTte(masterTruckType.getTteCapacity() * indentInfoDto.getIndented());
    indentSummary.setTransporter(indentInfoDto.getTransporter());
    indentSummary.setLoadFactor(masterTruckType.getLoadFactor());
    indentSummary.setCategory(indentInfoDto.getMaterailGrp());
    indentSummary.setNetRequested(indentInfoDto.getIndented());
    indentSummary.setStatus(Constants.Status.OPEN);
    indentSummary.setComments(indentInfoDto.getComments());
    indentSummary.setInsertUser(loggedInUser.getUserId());
    return indentSummary;
  };


  @Override
  public ApiResponse createIndents(IndentDto indentDto, ApplicationUser loggedInUser) {
    // validate indent data
    List<String> errors = indentDto.validateIndents();
    if (errors.size() > 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", errors);
    // location code check
    if (indentDto.getIndents().parallelStream().map(UploadIndentDto::getSource)
        .anyMatch(source -> !(source.equals(loggedInUser.getPlantCode())))) {
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("Souce Location should be %s", loggedInUser.getPlantCode()));
    }

    // validate truck types are exists in system
    List<String> uniqueTruckTypes = indentDto.getIndents().parallelStream().map(UploadIndentDto::getTruckType)
        .distinct().collect(Collectors.toList());
    List<MTTruckTypeInfo> masterTruckTypes = truckTypeRepo.findTruckTypeAndTteAndLoadByTypeIn(uniqueTruckTypes);
    String invalidTruckTypes = String.join(",", indentDto.validateTruckTypes(masterTruckTypes, uniqueTruckTypes));
    if (!StringUtils.isEmpty(invalidTruckTypes))
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("truck types %s not found in system", invalidTruckTypes));

    // validate material groups are exists in system
    List<String> uniqueMaterialGrps = indentDto.getIndents().parallelStream().map(UploadIndentDto::getMaterailGrp)
        .distinct().collect(Collectors.toList());
    List<MTItem> masterMaterialGrps = itemRepository.findByCategoryIn(uniqueMaterialGrps);
    String invalidMaterialGrps = String.join(",",
        indentDto.validateMaterialGrps(masterMaterialGrps, uniqueMaterialGrps));
    if (!StringUtils.isEmpty(invalidMaterialGrps))
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("material groups %s not found in system", invalidMaterialGrps));

    // validate transporters are exists in system
    List<String> uniqueTransporters = indentDto.getIndents().parallelStream().map(UploadIndentDto::getTransporter)
        .distinct().collect(Collectors.toList());
    List<MTTransporter> masterTransporters = transporterRepo.findByServprovIn(uniqueTransporters);
    String invalidTransporters = String.join(",",
        indentDto.validateTransporters(masterTransporters, uniqueTransporters));
    if (!StringUtils.isEmpty(invalidTransporters))
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("transporters %s not found in system", invalidTransporters));

    // validate destinations are exists in system
    List<String> uniqueDestinations = indentDto.getIndents().parallelStream().map(UploadIndentDto::getDestination)
        .distinct().collect(Collectors.toList());
    List<String> masterDestinations = locationRepository.findByIdIn(uniqueDestinations);
    String invalidDestinations = String.join(",", indentDto.validateDestination(masterDestinations, uniqueDestinations));
    if (!StringUtils.isEmpty(invalidDestinations))
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("destinations %s not found in system", invalidDestinations));

    // persist indents
    List<IndentSummary> indents = new ArrayList<IndentSummary>();
    indentDto.getIndents().parallelStream().forEach(indentInfoDto -> {
      indents.add(createIndent.apply(masterTruckTypes, indentInfoDto, loggedInUser));
    });
    indentSummaryRepository.saveAll(indents);
    return new ApiResponse(HttpStatus.OK, "Indents created successfully");
  }

  @Override
  public ApiResponse getIndents(IndentFilterDto indentFilterDto, ApplicationUser loggedInUser) {

    if (!indentFilterDto.isIndentFilterByFGS()) {
      Page<IndentSummary> indentSummaries = indentSummaryRepository
          .getIndentSummeryWithDesc(loggedInUser.getPlantCode(), PageRequest.of(indentFilterDto.getIndex(),
              indentFilterDto.getPageLength(), Sort.Direction.DESC, "dispatchDate"));
      List<IndentSummary> summaries = indentSummaries.getContent();
      List<IndentDetails> indentDetails = indentDetailsRepo.findByIndentSummaryIndentIdIn(
          summaries.parallelStream().map(indent -> indent.getIndentId()).collect(Collectors.toList()));
      for (IndentSummary indentSummary : summaries) {
        Set<IndentDetails> details = indentDetails.parallelStream().filter(
            indentDetail -> indentSummary.getIndentId().equals(indentDetail.getIndent().getIndentId()))
            .collect(Collectors.toSet());
        indentSummary.setIndentDetails(details);
      }
      Page<IndentSummary> pagableIndents = new PageImpl<>(summaries);
      return new ApiResponse(HttpStatus.OK, "", new IndentFilterDto(pagableIndents));

    } else {
      Page<IndentSummary> pagableIndents;
      indentFilterDto.setSource(loggedInUser.getPlantCode());
      pagableIndents = filterService.filterIndents(indentFilterDto, loggedInUser);
      return new ApiResponse(HttpStatus.OK, "", new IndentFilterDto(pagableIndents));
    }
  }

  @Override
  public ApiResponse reportTruck(TruckReportDto truckReportDto, ApplicationUser loggedInUser) {
    Constants.TruckReportStatus status = Constants.TruckReportStatus.valueOf(truckReportDto.getStatus());
    switch (status) {
      case REPORTED:
        return this.truckReport(truckReportDto, loggedInUser);
      case GATED_IN:
        return this.reportGateIn(truckReportDto, loggedInUser);
      case INTRANSIT:
        return this.reportGateOut(truckReportDto, loggedInUser);
      case CALL_TRUCK:
        // User will call truck for loading (Here Bay Assignment is optional )
        return this.callTruck(truckReportDto, loggedInUser);
      case BAY_ASSIGNED:
        // User will assign bay for truck
        return this.assignBay(truckReportDto, loggedInUser);
      default:
        break;
    }
    return new ApiResponse(HttpStatus.NOT_FOUND, "Invalid truck report data ! ");
  }

  private ApiResponse callTruck(TruckReportDto truckReportDto, ApplicationUser loggedInUser) {
    // validate truck bay assigning details
    String invalidInfo = truckReportDto.validateTruckBayAssign();
    if (!StringUtils.isEmpty(invalidInfo))
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("Please provide %s info", invalidInfo));
    Optional<TruckReport> optionalTruckReport = truckReportRepo
        .findOneByGateControlCodeAndIndentSummaryIndentIdAndStatus(truckReportDto.getGateControlCode(),
            truckReportDto.getIndentId(), Constants.TruckReportStatus.REPORTED);
    if (!optionalTruckReport.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("Reported truck not found with indent id %s and Gate control code %s",
              truckReportDto.getIndentId(), truckReportDto.getGateControlCode()));
    }
    TruckReport truckReport = optionalTruckReport.get();
    truckReport.setBayStatus(Constants.BayStatus.CALL);
    truckReport.setUpdateUser(loggedInUser.getUserId());
    truckReport.setUpdateDate(new Date());
    return new ApiResponse(HttpStatus.OK, "",
        new TruckReportDto(null, truckReportRepo.save(truckReport), loggedInUser));
  }

  private ApiResponse assignBay(TruckReportDto truckReportDto, ApplicationUser loggedInUser) {
    // validate truck bay assigning details
    String invalidInfo = truckReportDto.validateTruckBayAssign();
    if (!StringUtils.isEmpty(invalidInfo))
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("Please provide %s info", invalidInfo));
    // At any point(Status), Bay will be assigned to truck so dont consider status
    // in the querying the truck report
    Optional<TruckReport> optionalTruckReport = truckReportRepo.findOneByGateControlCodeAndIndentSummaryIndentId(
        truckReportDto.getGateControlCode(), truckReportDto.getIndentId());
    if (!optionalTruckReport.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("Reported truck not found with indent id %s and Gate control code %s",
              truckReportDto.getIndentId(), truckReportDto.getGateControlCode()));
    }
    TruckReport truckReport = optionalTruckReport.get();
    if (!StringUtils.isEmpty(truckReportDto.getBayAssigned())) {
      truckReport.setBay(truckReportDto.getBayAssigned());
    }
    truckReport.setUpdateUser(loggedInUser.getUserId());
    truckReport.setUpdateDate(new Date());
    return new ApiResponse(HttpStatus.OK, "",
        new TruckReportDto(null, truckReportRepo.save(truckReport), loggedInUser));
  }

  private ApiResponse reportGateOut(TruckReportDto truckReportDto, ApplicationUser loggedInUser) {
    // validate truck gate out report details
    String invalidInfo = truckReportDto.validateReportedTruck();
    if (!StringUtils.isEmpty(invalidInfo))
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("Please provide %s info", invalidInfo));
    // change the status in below query from GATE_IN to LOADED once service is ready
    // for load slip.
    Optional<TruckReport> optionalTruckReport = truckReportRepo.findOneByGateControlCodeAndIndentSummaryIndentId(
        truckReportDto.getGateControlCode(), truckReportDto.getIndentId());
    if (!optionalTruckReport.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("Reported truck not found with indent id %s and Gate control code %s",
              truckReportDto.getIndentId(), truckReportDto.getGateControlCode()));
    }
    TruckReport truckReport = optionalTruckReport.get();
    // Commented due to client feedabck
    /*
     * if (truckReportDto.getOutWeight() != null && truckReport.getInWeight() >
     * truckReportDto.getOutWeight()) { return new ApiResponse(HttpStatus.NOT_FOUND,
     * String.
     * format("out weight should be greater than in weight for indent id %s and Gate control code %s"
     * , truckReportDto.getIndentId(), truckReportDto.getGateControlCode())); }
     */
    //Update Loadslip and shipment status when truck is GATED_OUT
    utilityService.updateLoadslipAndShipmentStatus(truckReport, loggedInUser.getPlantCode());

    truckReport.setGateOutDate(new Date());
    if (truckReportDto.getOutWeight() != null) {
      truckReport.setOutWeight(truckReportDto.getOutWeight());
      if (truckReport.getOutWeight() != null && truckReport.getInWeight() != null)
        truckReport.setNetWeight(truckReport.getOutWeight() - truckReport.getInWeight());
    }
    truckReport.setUpdateUser(loggedInUser.getUserId());
    truckReport.setUpdateDate(new Date());
    ApiResponse response = this.updateIndentDetails(truckReportDto.getIndentId(),
        truckReportDto.getGateControlCode(), Constants.TruckReportStatus.INTRANSIT, loggedInUser);
    if (response.getStatusCode() == HttpStatus.NOT_FOUND.value())
      return response;
    return new ApiResponse(HttpStatus.OK, "", new TruckReportDto(truckReportRepo.save(truckReport)));
  }

  private void updateLoadslipStatus(TruckReport truckReport, String plantCode) {
    if (!StringUtils.isEmpty(truckReport.getShipmentID())) {
      Optional<Shipment> optionalShipment = shipmentRepository.findById(truckReport.getShipmentID());
      if (optionalShipment.isPresent()) {
        // Fetch max drop seq value (last drop) from shipment loadslips
        Optional<Loadslip> optionalMaxDropSeqLoadslip = optionalShipment.get().getLoadslips().parallelStream().filter(loadslip -> loadslip.getStatus() != Constants.LoadslipStatus.CANCELLED).max(Comparator.comparing(Loadslip::getDropSeq));

        optionalShipment.get().setStatus(Constants.TruckReportStatus.INTRANSIT.name());
        optionalShipment.get().getLoadslips().parallelStream().forEach(loadslip -> {
          if (loadslip.getSourceLoc().equals(plantCode)) {
            loadslip.setStatus(Constants.LoadslipStatus.INTRANSIT);
          } else if (loadslip.getDestLoc().equals(plantCode)) {
            loadslip.setStatus(Constants.LoadslipStatus.COMPLETED);
            if (optionalMaxDropSeqLoadslip.isPresent() && optionalMaxDropSeqLoadslip.get().getDropSeq() == loadslip.getDropSeq()) {
              optionalShipment.get().setStatus(Constants.TruckReportStatus.COMPLETED.name());
            }
          }
        });
        shipmentRepository.save(optionalShipment.get());
      }
    }
  }

  private ApiResponse reportGateIn(TruckReportDto truckReportDto, ApplicationUser loggedInUser) {
    // validate truck gate in report details
    String invalidInfo = truckReportDto.validateReportedTruck();
    if (!StringUtils.isEmpty(invalidInfo))
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("Please provide %s info", invalidInfo));
    // change the status in below query from REPORTED and check Bay assigned or not
    // once service is ready for Bay .
    Optional<TruckReport> optionalTruckReport = truckReportRepo.findOneByGateControlCodeAndIndentSummaryIndentIdAndStatus(truckReportDto.getGateControlCode(),
        truckReportDto.getIndentId(), Constants.TruckReportStatus.REPORTED);
    if (!optionalTruckReport.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("Bay assigned truck not found with indent id %s and Gate control code %s",
              truckReportDto.getIndentId(), truckReportDto.getGateControlCode()));
    }
    TruckReport truckReport = optionalTruckReport.get();
    truckReport.setStatus(Constants.TruckReportStatus.GATED_IN);
    truckReport.setGateInDate(new Date());
    if (truckReportDto.getInWeight() != null) {
      truckReport.setInWeight(truckReportDto.getInWeight());
    }
    truckReport.setUpdateUser(loggedInUser.getUserId());
    truckReport.setUpdateDate(new Date());
    ApiResponse response = this.updateIndentDetails(truckReportDto.getIndentId(),
        truckReportDto.getGateControlCode(), Constants.TruckReportStatus.GATED_IN, loggedInUser);
    if (response.getStatusCode() == HttpStatus.NOT_FOUND.value())
      return response;
    return new ApiResponse(HttpStatus.OK, "", new TruckReportDto(truckReportRepo.save(truckReport)));
  }

  private ApiResponse truckReport(TruckReportDto indentReportDto, ApplicationUser loggedInUser) {
    // validate truck report details
    String invalidInfo = indentReportDto.validateTruckReport();
    GpsProviderInfo gpsProvider = null;
    if (!StringUtils.isEmpty(invalidInfo))
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("Please provide %s info", invalidInfo));

    List<MTTruckTypeInfo> truckTypeInfoList = truckTypeRepo.findOneByType(indentReportDto.getTruckType());
    if (truckTypeInfoList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("Given truck type %s is not found in system", indentReportDto.getTruckType()));
    Optional<MTTruckTypeInfo> optionalTruckType = truckTypeInfoList.stream().findFirst();
    Optional<IndentSummary> optionalIndentSummary = indentSummaryRepository
        .findOneByIndentId(indentReportDto.getIndentId());
    if (!optionalIndentSummary.isPresent())
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("Given indent id %s not found in system", indentReportDto.getIndentId()));

    IndentSummary indentSummary = optionalIndentSummary.get();
    if (!loggedInUser.getPlantCode().equals(indentSummary.getSourceLocation()))
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("You are not authorized to report indent  %s", indentReportDto.getIndentId()));
    if (indentSummary.getIndented().intValue() == indentSummary.getReported()
        || indentSummary.getIndented() < indentSummary.getReported()) {
      return new ApiResponse(HttpStatus.NOT_FOUND, String
          .format("You are not report morethan requested qty for indent  %s", indentReportDto.getIndentId()));
    }
    if (indentReportDto.isGpsEnabled()) {
      if (StringUtils.isEmpty(indentReportDto.getGpsProvider()))
        return new ApiResponse(HttpStatus.NOT_FOUND, "please provide the Gps provider");
      gpsProvider = gpsProviderInfoRepo.findOneByGpsProvider(indentReportDto.getGpsProvider());
      if (gpsProvider == null)
        return new ApiResponse(HttpStatus.NOT_FOUND,
            String.format("Gps provider %s not found in system", indentReportDto.getGpsProvider()));
    }

    // uncomment the code once done with report truck.
    // validation - one truck can be reported at one plant at a time.

    /*
     * if (truckReportRepo.
     * existsByTruckTruckNumberAndReportLocationAndStatusAndRejectedStatus(
     * indentReportDto.getTruckNumber(), loggedInUser.getPlantCode(),
     * TruckReportStatus.REPORTED, TruckReportStatus.NORMAL)) { return new
     * ApiResponse(HttpStatus.NOT_FOUND,
     * String.format("Truck %s is already reported at this location ",
     * indentReportDto.getTruckNumber())); }
     */

    // update master truck details if exist else insert new one
    MTTruck mtTruck = this.updateTruckDetails(indentReportDto, indentSummary.getTransporter(), loggedInUser,
        optionalTruckType.get(), gpsProvider,
        truckRepo.findOneByTruckNumberIgnoreCase(indentReportDto.getTruckNumber()));

    if (mtTruck.getStatus() != null && mtTruck.getStatus().equals(Constants.TruckStatus.BLACKLISTED))
      return new ApiResponse(HttpStatus.METHOD_NOT_ALLOWED,
          String.format("Truck %s is blacklisted in system !", mtTruck.getTruckNumber()));

    IndentDetails indentDetails = this.createIndentDetails(indentSummary, indentReportDto, mtTruck, loggedInUser);

    TruckReport truckReport = this.createTruckReporting(indentSummary, indentDetails, loggedInUser, mtTruck);
    // truckReportRepo.save(truckReport);

    Integer reportedTrucksCount = indentSummary.getReported();
    indentSummary.setReported(reportedTrucksCount == null ? 1 : reportedTrucksCount + 1);
    indentDetails.setGateControlCode(truckReport.getGateControlCode());
    // indentDetailsRepo.save(indentDetails);

    indentSummary.indentDetails.add(indentDetails);
    indentSummary.truckReports.add(truckReport);

    indentSummaryRepository.save(indentSummary);

    return new ApiResponse(HttpStatus.OK,
        String.format("Truck reported successfully for indent %s", indentSummary.getIndentId()),
        new IndentInfoDto(indentSummary));
  }

  private MTTruck updateTruckDetails(TruckReportDto indentReportDto, String transporter, ApplicationUser loggedInUser,
                                     MTTruckTypeInfo truckType, GpsProviderInfo gpsProvider, Optional<MTTruck> masterTruckInfo) {
    return truckRepo.save(masterDataServce.createTruck(indentReportDto, transporter, loggedInUser, truckType, gpsProvider, masterTruckInfo));
  }

  private IndentDetails createIndentDetails(IndentSummary indentSummary, TruckReportDto indentReportDto,
                                            MTTruck mtTruck, ApplicationUser loggedInUser) {
    IndentDetails indentDetails = new IndentDetails();
    Integer lineNumber = indentDetailsRepo.findLineNumberByIndentId(indentSummary.getIndentId());
    MTTruckTypeInfo actualTruckTypeInfo = mtTruck.getTtId();
    indentDetails.setIndentSummary(indentSummary);
    indentDetails.setIndent(new IndentId(indentSummary.getIndentId(), lineNumber == null ? 1 : lineNumber + 1));
    indentDetails.setTruck(mtTruck);
    indentDetails.setTruckType(indentSummary.getTruckType());
    indentDetails.setActualTruckType(mtTruck.getTtId().getType());
    indentDetails.setVariant1(mtTruck.getVariant1());
    indentDetails.setTte(actualTruckTypeInfo.getTteCapacity());
    indentDetails.setDriverName(indentReportDto.getDriverName());
    indentDetails.setDriverMobile(indentReportDto.getDriverContact());
    indentDetails.setDriverLicense(indentReportDto.getDriverLicense());
    indentDetails.setGpsEnabled(mtTruck.getGpsEnabled());
    indentDetails.setGpsProvider(mtTruck.getGpsProvider() != null ? mtTruck.getGpsProvider().getGpsProvider() : null);
    indentDetails.setStatus(Constants.TruckReportStatus.REPORTED);
    indentDetails.setPassingWeight(mtTruck.getPassingWeight());
    indentDetails.setInsertUser(loggedInUser.getUserId());
    return indentDetails;
  }

  private TruckReport createTruckReporting(IndentSummary indentSummary, IndentDetails indentDetails, ApplicationUser loggedInUser, MTTruck mtTruck) {
    TruckReport truckReport = new TruckReport();
    truckReport.setGateControlCode(indentSummaryRepository.findIndentSequence(Constants.INDENT_GATE_CONTROL_CODE_PREFIX,
        loggedInUser.getPlantCode(), Constants.INDENT_GATE_CONTROL_CODE_SUFFIX));
    truckReport.setIndentSummary(indentSummary);
    // need to change the below system type to RMS for RMS services
    truckReport.setType(Constants.SystemType.FGS);
    Map<String, Object> freightMap = utilityService.calculateTrasporterSapCode(indentSummary, indentDetails);

    truckReport.setTransporterSapCode(!StringUtils.isEmpty(freightMap.get("transporterSapCode")) ? freightMap.get("transporterSapCode").toString() : null);
    // Doing previously, now this logic is moved to save shipment freight calculation place
//    truckReport.setTtDays(!StringUtils.isEmpty(freightMap.get("ttDays")) ? Double.parseDouble(freightMap.get("ttDays").toString()) : null);
    truckReport.setTruck(mtTruck);
    truckReport.setDriverName(indentDetails.getDriverName());
    truckReport.setDriverMobile(indentDetails.getDriverMobile());
    truckReport.setDriverLicense(indentDetails.getDriverLicense());
    truckReport.setServprov(indentSummary.getTransporter());
    truckReport.setTruckType(indentSummary.getTruckType());
    truckReport.setReportLocation(loggedInUser.getPlantCode());
    truckReport.setSourceLocation(indentSummary.getSourceLocation());
    truckReport.setDestinationLocation(indentSummary.getDestinationLocation());
    truckReport.setReportDate(new Date());
    truckReport.setStatus(Constants.TruckReportStatus.REPORTED);
    truckReport.setWaitTimeHrs((double) DateUtils.diffBetweenDates(new Date(), truckReport.getReportDate()));
    truckReport.setRejectedStatus(Constants.TruckReportStatus.NORMAL);
    truckReport.setActualTruckType(indentDetails.getActualTruckType());
    truckReport.setInsertUser(loggedInUser.getUserId());
    return truckReport;
  }

  private String getTransporterCodeByFreights(String transporterSapCode, List<Freight> freights) {
  /*  if (freights != null && !freights.isEmpty()) {
      for (Freight freight : freights) {
        if (DateUtils.isSameDay(freight.getEffectiveDate())) {
          transporterSapCode = freight.getTransporterSapCode();
        }
      }
      if (StringUtils.isEmpty(transporterSapCode)) {
        transporterSapCode = freights.get(0).getTransporterSapCode();
      }
    }*/
    if (freights != null && !freights.isEmpty()) {
      transporterSapCode = freights.get(0).getTransporterSapCode();
    }

    return transporterSapCode;
  }
  private ApiResponse updateIndentDetails(String indentId, String gateControlCode, Constants.TruckReportStatus status, ApplicationUser applicationUser) {
    Optional<IndentDetails> optionalIndentDetails = indentDetailsRepo
        .findOneByIndentSummaryIndentIdAndGateControlCode(indentId, gateControlCode);
    if (!optionalIndentDetails.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("Indent details are not found for indent id %s", indentId));
    }
    IndentDetails indentDetails = optionalIndentDetails.get();
    indentDetails.setStatus(status);
    indentDetails.setUpdateDate(new Date());
    indentDetails.setUpdateUser(applicationUser.getUserId());
    indentDetailsRepo.save(indentDetails);
    return new ApiResponse(HttpStatus.OK, "");
  }

  @Override
  public ApiResponse modifyIndents(IndentInfoDto indentInfoDto, ApplicationUser applicationUser) {
    if (StringUtils.isEmpty(indentInfoDto))
      return new ApiResponse(HttpStatus.NOT_FOUND, "IndentId is missing");
    Optional<IndentSummary> optionalIndentSummary = indentSummaryRepository.findOneByIndentId(indentInfoDto.getIndentId());
    if (!optionalIndentSummary.isPresent())
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("%s Indent Id not found in the System", indentInfoDto.getIndentId()));
    IndentSummary indentSummary = optionalIndentSummary.get();
    if (!(indentInfoDto.getIndented() > 0))
      return new ApiResponse(HttpStatus.BAD_REQUEST, "Please provide valid number of indented trucks");
    if (indentInfoDto.getIndented() < indentInfoDto.getCancelled())
      return new ApiResponse(HttpStatus.BAD_REQUEST, "Cancelled trucks should not be more then Indented trucks");
    indentSummary.setIndented(indentInfoDto.getIndented());
    indentSummary.setCancelled(indentInfoDto.getCancelled());
    if (indentInfoDto.getStatus().equals(Constants.Status.CANCELLED)) {
      if (indentSummary.getTransAssigned() > 0 || indentSummary.getTransConfirmed() > 0 || indentSummary.getReported() > 0)
        return new ApiResponse(HttpStatus.NOT_FOUND, String.format("operation not allowed after transporter responded to indent %s", indentInfoDto.getIndentId()));
      indentSummary.setStatus(Constants.Status.CANCELLED);
    }
    indentSummary.setNetRequested(indentInfoDto.getIndented() - indentSummary.getCancelled());
    indentSummary.setComments(indentInfoDto.getComments());
    indentSummary.setUpdateDate(new Date());
    indentSummary.setUpdateUser(applicationUser.getUserId());
    IndentSummary modifiedIndent = indentSummaryRepository.save(indentSummary);
    return new ApiResponse(HttpStatus.OK, "Indent Updated successfully", new IndentInfoDto(modifiedIndent));
  }

  @Override
  public ApiResponse getIndentTruckReportInfo(String indentId, ApplicationUser loggedInUser) {
    return null;
  }


  @Override
  public ApiResponse getExportContainer(Constants.DelInvType type, String loadslipId, ApplicationUser applicationUser) {
    List<Map<String, String>> gatedInTrucks = new ArrayList<>();
    // removed String formart
    if (!StringUtils.isEmpty(loadslipId)) {
      Optional<Loadslip> loadslipOpt = loadslipRepository.findByLoadslipIdAndStatusNot(loadslipId, Constants.LoadslipStatus.CANCELLED);
      loadslipOpt.ifPresent(loadslip -> {
            if (type != null && type.equals(Constants.DelInvType.JIT_OEM)) {

              gatedInTrucks.addAll(truckReportRepo.findTrucksByStatusAndStatusNotAndSourceAndDestLocAndReportingLocWithContainerNoAll(Constants.TruckReportStatus.GATED_IN.name(),
                  Constants.TruckReportStatus.INTRANSIT.name(), applicationUser.getPlantCode(), loadslip.getDestLoc(), applicationUser.getPlantCode()));
            } else {

              gatedInTrucks.addAll(truckReportRepo.findTrucksByStatusAndStatusNotAndSourceAndDestLocAndReportingLocWithContainerNo(Constants.TruckReportStatus.GATED_IN.name(),
                  Constants.TruckReportStatus.INTRANSIT.name(), applicationUser.getPlantCode(), loadslip.getDestLoc(), applicationUser.getPlantCode()));
            }
          }
      );
    } else {
      if (type != null && type.equals(Constants.DelInvType.JIT_OEM)) {
        gatedInTrucks.addAll(truckReportRepo.findTrucksByStatusAndStatusNotAndSourceAndDestLocAndReportingLocWithContainerNoNotDestLocAll(Constants.TruckReportStatus.GATED_IN.name(),
            Constants.TruckReportStatus.INTRANSIT.name(), applicationUser.getPlantCode(), applicationUser.getPlantCode()));
      } else {
        gatedInTrucks.addAll(truckReportRepo.findTrucksByStatusAndStatusNotAndSourceAndDestLocAndReportingLocWithContainerNoNotDestLoc(Constants.TruckReportStatus.GATED_IN.name(),
            Constants.TruckReportStatus.INTRANSIT.name(), applicationUser.getPlantCode(), applicationUser.getPlantCode()));
      }
    }

    ExportDto exportDto = new ExportDto(null, null, gatedInTrucks, null, null, deriveTubeAndFlapBatchCodes(), null);
    return new ApiResponse(HttpStatus.OK, "unique Container from Truck_reporting table for source .", exportDto);
  }

  @Override
  public ApiResponse getLoadSlipUtilizationsAndExportData(ApplicationUser loggedInUser, LsUtilizationDto lsUtilizationDto) {

    LoadSlipUtilizationDto lsUtilization = (LoadSlipUtilizationDto) opsService.getLoadSlipUtilizations(lsUtilizationDto, loggedInUser).getData();
    String destLoc = null;
    if (!StringUtils.isEmpty(lsUtilizationDto.getLsDropDestLoc())){
      destLoc = lsUtilizationDto.getLsDropDestLoc();
      lsUtilization.setDestLoc(destLoc);
    }else {
      destLoc = lsUtilizationDto.getDestLoc();
    }
//    lsDropLoc will be empty for Share truck
    if(!StringUtils.isEmpty(lsUtilizationDto.getShipmentId()) && StringUtils.isEmpty(lsUtilizationDto.getLsDropDestLoc())){
      Optional<Shipment> optionalShipment = shipmentRepository.findByShipmentId(lsUtilizationDto.getShipmentId());
      Shipment shipment = optionalShipment.get();
//      Optional<String> destLocOpt = loadslipRepository.findAllByShipmentShipmentId(shipment.getShipmentId()).stream().findFirst().map(t -> t.getDestLoc());
      /*Getting the destination based on the selected LS from shipment*/
      /*When drafted LS is opened, to fetch dest of perticular LS we use LoadslipId*/
      Optional<String> destLocOpt = null;
      if (!StringUtils.isEmpty(lsUtilizationDto.getLoadSlipId())) {
        destLocOpt = loadslipRepository.findAllByShipmentShipmentId(shipment.getShipmentId()).stream().
            filter(ls -> ls.getLoadslipId().equals(lsUtilizationDto.getLoadSlipId())).findFirst().map(t -> t.getDestLoc());
      }else {
        //When truck is shared loadslipId will be null
        destLocOpt = loadslipRepository.findAllByShipmentShipmentId(shipment.getShipmentId()).stream().findFirst().map(t -> t.getDestLoc());
      }
      destLoc = destLocOpt.isPresent()?destLocOpt.get():destLoc;
      lsUtilization.setDestLoc(destLoc);
    }
    lsUtilization.setCity(locationRepository.findCityByLocId(destLoc));
    lsUtilization.setDestDesc(locationRepository.findDescriptionByLocId(destLoc));

    ExportDto exportInvoiceData = (ExportDto) opsService.getExportInvoiceWithType(lsUtilizationDto.getType(), destLoc, lsUtilizationDto.getLoadSlipId(), loggedInUser).getData();
    exportInvoiceData.setGatedInTrucks(null);
    exportInvoiceData.setLoadSlipUtilizationDto(lsUtilization);
    return new ApiResponse(HttpStatus.OK, "unique Invoices from DEL_INV_HEADER table for source to destination.",
        exportInvoiceData);
  }

  @Override
  public ApiResponse getExportTrackerData(ApplicationUser loggedInUser, ExportTrackerFilter exportTrackerFilter) {
    // List<ExportTrackerDto> trackerList = shipmentRepository.getExportTrackerBySource(loggedInUser.getPlantCode());
    String transporter = (String) userRepository.findTransporterByUser(loggedInUser.getUserId());
    if (exportTrackerFilter.validateExportTracker(loggedInUser,transporter)) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "No Transporter is associated for this User");
    }

    List<ExportTrackerDto> trackerList = filterService.filterExportTracker(loggedInUser, exportTrackerFilter);
  List<String> uniqueDest = trackerList.parallelStream().map(ExportTrackerDto::getDestLoc).distinct().collect(Collectors.toList());
    if (uniqueDest.size() > 0) {
      List<Map<String, String>> destWithDesc = locationRepository.findDestDescWtihDestinations(uniqueDest);
      trackerList.forEach(truck ->{
        truck.setDestDis(destWithDesc.stream().filter(destDescMap -> destDescMap.get("destLoc").equals(truck.getDestLoc())).findAny().get().get("DESTDESC"));
      });
    }
    return new ApiResponse(HttpStatus.OK, "Tracker details for source.",
        trackerList);
  }

  @Override
  public ApiResponse updateExportTrackerData(ApplicationUser loggedInUser, ExportTrackerDto exportTrackerDto) {
    Optional<Shipment> shipmentOpt = shipmentRepository.findById(exportTrackerDto.getShipmentId());
    if (shipmentOpt.isPresent()) {
      Shipment shipment = shipmentOpt.get();
      exportTrackerDto.updateTruckTracker(shipment, loggedInUser);
      shipmentRepository.save(shipment);
      exportTrackerPostProcess(shipment, loggedInUser);
    }

    return new ApiResponse(HttpStatus.OK, "Tracker details for source.",
        exportTrackerDto);
  }

  private void exportTrackerPostProcess(Shipment shipment, ApplicationUser applicationUser) {
/*
    if(shipment.getVesselArrivePodDate() != null && (t.getIncoterm().equals("CIF") || t.getIncoterm().equals("CFR"))){
      completeLoadslipAndShipment(shipment);
    }else if(shipment.getGateInDatePort() != null && (t.getIncoterm().equals("FOB"))){
      completeLoadslipAndShipment(shipment);
    }else {
    }*/
    if ((shipment.getShippedOnboardDate() != null)) {
      completeLoadslipAndShipment(shipment, applicationUser);
    }
  }

  private void completeLoadslipAndShipment(Shipment shipment, ApplicationUser applicationUser) {
    shipment.setStatus(Constants.Status.COMPLETED.name());
    shipment.setUpdateUser(applicationUser.getUserId());
    shipment.setUpdateDate(new Date());
    shipmentRepository.save(shipment);
    List<Loadslip> loadslipList = shipment.getLoadslips();
    loadslipList.stream().parallel().forEach(loadslip -> {
      loadslip.setUpdateUser(applicationUser.getUserId());
      loadslip.setUpdateDate(new Date());
      loadslip.setStatus(Constants.LoadslipStatus.COMPLETED);
    });
    loadslipRepository.saveAll(loadslipList);
    List<TruckReport> reportedTruckList = truckReportRepo.findAllByShipmentID(shipment.getShipmentId());
    reportedTruckList.stream().parallel().forEach(truckReport -> {
        truckReport.setUpdateUser(applicationUser.getUserId());
        truckReport.setUpdateDate(new Date());
        truckReport.setStatus(Constants.TruckReportStatus.COMPLETED);
    });
    truckReportRepo.saveAll(reportedTruckList);
    opsService.sendActualShipment(shipment.getShipmentId());
  }

}
