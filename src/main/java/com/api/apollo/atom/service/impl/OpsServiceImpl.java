

package com.api.apollo.atom.service.impl;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.Constants.*;
import com.api.apollo.atom.constant.LocationType;
import com.api.apollo.atom.constant.UserRole;
import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.master.CTRejectionDto;
import com.api.apollo.atom.dto.ops.*;
import com.api.apollo.atom.dto.planner.DispatchPlanFilterDto;
import com.api.apollo.atom.dto.planner.DispatchPlanItemDto;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.master.*;
import com.api.apollo.atom.entity.ops.*;
import com.api.apollo.atom.entity.plan.DispatchPlanItemInfo;
import com.api.apollo.atom.exception.ImproperDataException;
import com.api.apollo.atom.exception.InvalidException;
import com.api.apollo.atom.exception.UnAuthorisedException;
import com.api.apollo.atom.repository.UserRepository;
import com.api.apollo.atom.repository.master.*;
import com.api.apollo.atom.repository.ops.*;
import com.api.apollo.atom.repository.planner.DelInvHeaderRepository;
import com.api.apollo.atom.repository.planner.DelInvLineRepository;
import com.api.apollo.atom.repository.planner.DispatchPlanItemInfoRepository;
import com.api.apollo.atom.service.*;
import com.api.apollo.atom.util.*;
import com.api.apollo.atom.util.func.ExcelUtil;
import com.api.apollo.atom.util.func.TriFunction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import javax.servlet.http.HttpServletRequest;
import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayOutputStream;
import java.security.InvalidParameterException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.api.apollo.atom.constant.Constants.*;
import static com.api.apollo.atom.util.Utility.currentTimestamp;
import static com.api.apollo.atom.util.Utility.deriveTubeAndFlapBatchCodes;
import static java.lang.String.format;

//import static org.hamcrest.CoreMatchers.nullValue;

@Service
public class OpsServiceImpl implements OpsService {
  @Autowired
  LoadReceiptRepository loadReceiptRepository;

  @Autowired
  FilterService filterService;

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private TruckTypeInfoRepository truckTypeRepo;

  @Autowired
  private IndentSummaryRepository indentSummaryRepository;

  @Autowired
  private IndentDetailsRepository indentDetailsRepo;

  @Autowired
  private TruckRepository truckRepo;

  @Autowired
  private MasterDataService masterDataServce;

  @Autowired
  private TransporterRepository transporterRepo;

  @Autowired
  private TruckReportRepository truckReportRepo;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private LocationRepository locationRepository;

  @Autowired
  private DispatchPlanItemInfoRepository dispatchPlanItemInfoRepository;

  @Autowired
  private MTCustomerRepository mtCustomerRepositary;

  @Autowired
  private GpsProviderInfoRepository gpsProviderInfoRepo;

  @Autowired
  private TruckReportSummaryRepository truckReportSummaryRepository;

  @Autowired
  private ShipmentRepository shipmentRepository;

  @Autowired
  private LoadslipRepository loadslipRepository;

  @Autowired
  private LoadslipDetailRepository loadslipDetailRepository;

  @Autowired
  private LoadslipDetailBomRepository loadslipDetailBomRepository;

  @Autowired
  private OpsService opsService;

  @Autowired
  private FreightRepository freightRepository;

  @Autowired
  private MTLaneRepository laneRepository;

  @Autowired
  private DelInvHeaderRepository delInvHeaderRepository;

  @Autowired
  private DelInvLineRepository delInvLineRepository;

  @Autowired
  private ExportShipmentRepo exportShipmentRepo;

  @Autowired
  private UtilityService utilityService;

    /*  @Autowired
  private JavaMailSender mailSender;*/

  @Autowired
  private UmUserAssociationRepository userAssociationRepo;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BatchCodeRepository batchCodeRepository;

  @Autowired
  private LoadslipInvHeaderRepo invoiceHeaderRepo;

  @Autowired
  private LoadslipInvoiceLineRepo invoiceLineRepo;

  @Autowired
  private CTCountryRepository ctCountryRepository;

  @Autowired
  private CTRejectionRepository ctRejectionRepository;

  @Autowired
  private MTMatrialGroupRepository mtMatrialGroupRepository;

  @Autowired
  private ObjectMapper mapper;


  private RestTemplate restTemplate = new RestTemplate();

  @Value("${send.ls.sap.url}")
  private String sendLsToSAPURL;

  @Value("${send.ls.sap.barcode.url}")
  private String sapBarcodeURL;

  @Value("${actualShipment.url}")
  private String sendActualShipmentUrl;

  @Value("${sap.username}")
  private String sapUserName;

  @Value("${sap.password}")
  private String sapPassword;

  @Value("${smpt.userId}")
  private String smtpUserId;

  @Value("${smpt.password}")
  private String smtpPassword;

  @Value("${smtp.host}")
  private String host;

  @Value("${smtp.port}")
  private String port;

  @Value(("${delete.sosto.url}"))
  private String sostoURL;

  @Value(("${ewayBill.url}"))
  private String eWayBillURL;

  @Autowired
  private SharedTruckRepository sharedTruckRepository;

  @Autowired
  ShipmentStopRepository shipmentStopRepository;

  @Autowired
  MTValveRepository valveRepository;

  @Autowired
  private MTTruckDedidatedRepository mtTruckDedidatedRepository;

  @Autowired
  private CTScaleInvoiceWtDiffRepository ctScaleInvoiceWtDiffRepository;

  @Autowired
  private OrderTypeLookupRepository orderTypeLookupRepository;

  @Autowired
  private TruckTypeInfoRepository truckTypeInfoRepository;


  @Autowired
  private DashboardService dashboardService;

  private static Date apply(LoadslipMetaData d) {
    return ((Date) d.getCreatedDate());
  }

  @Override
  public ApiResponse getDispatchPlanInfo(DispatchPlanFilterDto searchDto, ApplicationUser loggedInUser) {
    Optional<MTLocation> mtLocation = locationRepository.findByLocationId(loggedInUser.getPlantCode());
//		List<String> extWarehouseLocs = utilityService.getLinkedExtWareHouse("1007");
//		Page<DispatchPlanItemInfo> dispatchPlanItemInfos = filterService.filterDispatchPlanItems(searchDto, loggedInUser, mtLocation);
//		if (mtLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equalsIgnoreCase(mtLocation.get().getLocationClass()) && searchDto.getIsViewPlans()) {
//			dispatchPlanItemInfos = new PageImpl<>(dispatchPlanItemInfos.stream().parallel().filter(dp -> !(dp.getSourceLocation().equals("1007") && extWarehouseLocs.contains(dp.getDestinationLocation())))
//					.collect(Collectors.toList()), PageRequest.of(searchDto.getIndex(), searchDto.getPageLength()), dispatchPlanItemInfos.getTotalElements());
//		}
    DispatchPlanFilterDto dispatchPlanFilterDto = filterService.filterDisplanPlanInfo(searchDto, loggedInUser, mtLocation);
    /*Getting STANDARD_FTL truck Details*/
    MTTruckTypeInfo mtTruckTypeInfo = truckTypeInfoRepository.findOneByType("STANDARD_FTL").parallelStream().filter(mt -> StringUtils.isEmpty(mt.getVariant1())).findFirst().get();
    dispatchPlanFilterDto.setStandardFtlWeight(mtTruckTypeInfo.getGrossWt());
    dispatchPlanFilterDto.setStandardFtlTteCapacity(mtTruckTypeInfo.getTteCapacity());
    dispatchPlanFilterDto.setStandardFTLVolumne(mtTruckTypeInfo.getGrossVol());

    return new ApiResponse(HttpStatus.OK, "", dispatchPlanFilterDto);
  }

  private TriFunction<List<MTTruckTypeInfo>, UploadIndentDto, ApplicationUser, IndentSummary> createIndent = (
      List<MTTruckTypeInfo> masterTruckTypes, UploadIndentDto indentInfoDto, ApplicationUser loggedInUser) -> {
    IndentSummary indentSummary = new IndentSummary();
    // Akshay is generating Indent Id when data pushed to Indent_Summary table in backend db
    /*indentSummary.setIndentId(indentSummaryRepository.findIndentSequence(Constants.INDENT_SEQ_PREFIX,
        indentInfoDto.getSource(), indentInfoDto.getDestination()));*/
    /*indentSummary.setDispatchDate(
        DateUtils.formatDate(indentInfoDto.getDispatchDate(), Constants.PLAN_RECORD_DATE_FORMAT));*/
    Date dispatchDate = DateUtils.constantDateFormat(indentInfoDto.getDispatchDate());
    if (dispatchDate != null) {
      indentSummary.setDispatchDate(dispatchDate);
    }
    indentSummary.setSourceLocation(indentInfoDto.getSource());
    indentSummary.setDestinationLocation(indentInfoDto.getDestination());
    MTTruckTypeInfo masterTruckType = masterTruckTypes.stream().parallel()
        .filter(mt -> mt.getType().equalsIgnoreCase(indentInfoDto.getTruckType())).findFirst().get();
    indentSummary.setTruckType(masterTruckType.getType());
    indentSummary.setIndented(indentInfoDto.getIndented());
    indentSummary.setNetBalance(indentInfoDto.getIndented());
    indentSummary.setTte((masterTruckType.getTteCapacity() != null ? masterTruckType.getTteCapacity() : 0) * indentInfoDto.getIndented());
    indentSummary.setTransporter(indentInfoDto.getTransporter());
    indentSummary.setLoadFactor(masterTruckType.getLoadFactor() != null ? masterTruckType.getLoadFactor() : 0);
    indentSummary.setCategory(indentInfoDto.getMaterailGrp());
    indentSummary.setNetRequested(indentInfoDto.getIndented());
    indentSummary.setStatus(Status.OPEN);
    indentSummary.setComments(indentInfoDto.getComments());
    indentSummary.setInsertUser(loggedInUser.getUserId());
    indentSummary.setInsertDate(new Date());
    indentSummary.setDestinationDescription(indentInfoDto.getDestinationDescription());
    CTCountry ctCountry = ctCountryRepository.findByCountryName(indentInfoDto.getDestCountryName());
    indentSummary.setDestCountry(ctCountry != null ? ctCountry.getCountryCode() : null);
    indentSummary.setPod(indentInfoDto.getPod());
    // Old Logic
		/*boolean isExist = freightRepository.existsBySourceLocAndDestLocAndServprovAndTruckType
				(indentInfoDto.getSource(), indentInfoDto.getDestination(), indentInfoDto.getTransporter(), indentInfoDto.getTruckType());
		if (isExist)
			indentSummary.setIsFreightAvailable("Y");
		else {
			//If source is EXT_WAREHOUSE then check if freight is available for linkedPlant,dest,transporter and truckType
			//if freight is available the set Y else N
			Optional<MTLocation> optionalMTLocation = locationRepository.findByLocationId(indentInfoDto.getSource());
			if (optionalMTLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equals(optionalMTLocation.get().getLocationClass()) &&
					freightRepository.existsBySourceLocAndDestLocAndServprovAndTruckType(optionalMTLocation.get().getLinkedPlant(), indentInfoDto.getDestination(),
							indentInfoDto.getTransporter(), indentInfoDto.getTruckType())) {
				indentSummary.setIsFreightAvailable("Y");
			} else {
				indentSummary.setIsFreightAvailable("N");
			}
		}*/
    // New Logic By Lalit
    boolean isFreightAvailable = getFreightAvailability(indentInfoDto.getSource(), indentInfoDto.getDestination(), indentInfoDto.getTransporter(), indentInfoDto.getTruckType());
    indentSummary.setIsFreightAvailable(isFreightAvailable ? "Y" : "N");
    return indentSummary;
  };

  private boolean getFreightAvailability(String source, String destination, String transporter, String truckType) {
    List<Freight> freights = freightRepository.findAllBySourceAndDestAndServProvAndTruckTypeBetweenEffectiveAndExpiryDate(source, destination, transporter, truckType, DateUtils.setTimeToMidnight(new Date()));
    if (freights != null && !freights.isEmpty()) {
      return true;
    } else {
      //If source is EXT_WAREHOUSE then check if freight is available for linkedPlant,dest,transporter and truckType
      //if freight is available the set Y else N
      Optional<MTLocation> optionalMTLocation = locationRepository.findByLocationId(source);
      if (optionalMTLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equals(optionalMTLocation.get().getLocationClass())) {
        List<Freight> extWareHouseFreights = freightRepository.findAllBySourceAndDestAndServProvAndTruckTypeBetweenEffectiveAndExpiryDate(optionalMTLocation.get().getLinkedPlant(), destination, transporter, truckType, DateUtils.setTimeToMidnight(new Date()));
        return extWareHouseFreights != null && extWareHouseFreights.size() > 0;
      }
    }
    return false;
  }

  @Override
  public ApiResponse createIndents(IndentDto indentDto, ApplicationUser loggedInUser) {
    // validate indent data
    List<String> errors = indentDto.validateIndents();
    if (errors.size() > 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "", errors);
    // location code check
    if (indentDto.getIndents().parallelStream().map(UploadIndentDto::getSource)
        .anyMatch(source -> !(source.equals(loggedInUser.getPlantCode())))) {
      errors.add(String.format("Source Location should be %s", loggedInUser.getPlantCode()));
      return new ApiResponse(HttpStatus.NOT_FOUND, "", errors);
    }

    // validate truck types are exists in system
    List<String> uniqueTruckTypes = indentDto.getIndents().parallelStream().map(UploadIndentDto::getTruckType)
        .distinct().collect(Collectors.toList());
    List<MTTruckTypeInfo> masterTruckTypes = truckTypeRepo.findTruckTypeAndTteAndLoadByTypeIn(uniqueTruckTypes);
    String invalidTruckTypes = String.join(",", indentDto.validateTruckTypes(masterTruckTypes, uniqueTruckTypes));
    if (!StringUtils.isEmpty(invalidTruckTypes)) {
      errors.add(String.format("truck types %s not found in system", invalidTruckTypes));
      return new ApiResponse(HttpStatus.NOT_FOUND, "", errors);
    }

    // validate material groups are exists in system
    List<String> uniqueMaterialGrps = indentDto.getIndents().parallelStream().filter(indent -> indent.getMaterailGrp() != null).map(UploadIndentDto::getMaterailGrp)
        .distinct().collect(Collectors.toList());
    if (uniqueMaterialGrps != null && uniqueMaterialGrps.size() > 0) {
      List<String> masterMtMaterialGrpScmGroups = mtMatrialGroupRepository.findDistinctMTMatrialGroupByOrderByIdAsc(uniqueMaterialGrps);
      String invalidMaterialGrps = String.join(",",
          indentDto.validateMaterialscmGrps(masterMtMaterialGrpScmGroups, uniqueMaterialGrps));
      if (!StringUtils.isEmpty(invalidMaterialGrps)) {
        errors.add(String.format("material groups %s not found in system", invalidMaterialGrps));
        return new ApiResponse(HttpStatus.NOT_FOUND, "", errors);
      }
    }

    // validate transporters are exists in system
    List<String> uniqueTransporters = indentDto.getIndents().parallelStream().map(UploadIndentDto::getTransporter)
        .distinct().collect(Collectors.toList());
    List<MTTransporter> masterTransporters = transporterRepo.findByServprovIn(uniqueTransporters);
    String invalidTransporters = String.join(",",
        indentDto.validateTransporters(masterTransporters, uniqueTransporters));
    if (!StringUtils.isEmpty(invalidTransporters)) {
      errors.add(String.format("transporter %s not found in system", invalidTransporters));
      return new ApiResponse(HttpStatus.NOT_FOUND, "", errors);
    }

    // validate destinations are exists in system
    List<String> uniqueDestinations = indentDto.getIndents().parallelStream().map(UploadIndentDto::getDestination)
        .distinct().collect(Collectors.toList());
    List<String> masterDestinations = locationRepository.findByIdIn(uniqueDestinations);
    String invalidDestinations = String.join(",", indentDto.validateDestination(masterDestinations, uniqueDestinations));
    if (!StringUtils.isEmpty(invalidDestinations)) {
      errors.add(String.format("destination %s not found in system", invalidDestinations));
      return new ApiResponse(HttpStatus.NOT_FOUND, "", errors);
    }

    for (UploadIndentDto uploadIndentDto : indentDto.getIndents()) {
      if (!StringUtils.isEmpty(uploadIndentDto.getPod())) {
        if (!(locationRepository.existsByIdAndType(uploadIndentDto.getPod(), "PORT"))) {
          errors.add(String.format("POD %s not found in the system", uploadIndentDto.getPod()));
        }
      }
    }
    if (errors != null && !errors.isEmpty()) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "", errors);
    }

    /*Checking if given destCountry is valid or not*/
    for (UploadIndentDto uploadIndentDto : indentDto.getIndents()) {
      if (!StringUtils.isEmpty(uploadIndentDto.getDestCountryName())) {
        if (!(ctCountryRepository.existsByCountryName(uploadIndentDto.getDestCountryName()))) {
          errors.add(String.format("Destination country %s not found in the system", uploadIndentDto.getDestCountry()));
        }
      }
    }
    if (errors != null && !errors.isEmpty()) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "", errors);
    }

    // persist indents
    List<IndentSummary> indents = new ArrayList<IndentSummary>();
    indentDto.getIndents().parallelStream().forEach(indentInfoDto -> {
      indents.add(createIndent.apply(masterTruckTypes, indentInfoDto, loggedInUser));
    });
    indentSummaryRepository.saveAll(indents);
    // Commented on Akshay's Request Email- 14th May
  /*  String nofreightIndents = indents.stream().filter(indent -> indent.getIsFreightAvailable().equals("N")).map(IndentSummary::getIndentId).collect(Collectors.joining(","));
    if (!StringUtils.isEmpty(nofreightIndents))
      return new ApiResponse(HttpStatus.OK, "Indents created successfully", String.format("Freight is not available For IndentId's " + nofreightIndents));
    else*/
    return new ApiResponse(HttpStatus.OK, "Indents created successfully");
  }

  @Override
  public ApiResponse modifyIndents(IndentInfoDto indentInfoDto, ApplicationUser applicationUser) {
    if (StringUtils.isEmpty(indentInfoDto))
      return new ApiResponse(HttpStatus.NOT_FOUND, "IndentId is missing");
    Optional<IndentSummary> optionalIndentSummary = indentSummaryRepository.findOneByIndentId(indentInfoDto.getIndentId());
    if (!optionalIndentSummary.isPresent())
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("%s Indent Id not found in the System", indentInfoDto.getIndentId()));
    IndentSummary indentSummary = optionalIndentSummary.get();
    if (indentSummary.getStatus().equals(Status.CANCELLED))
      return new ApiResponse(HttpStatus.BAD_REQUEST, "Not allowed to modify cancelled indent");
    if (indentSummary.getNetPlaced() > (indentInfoDto.getIndented() - indentInfoDto.getCancelled()))
      return new ApiResponse(HttpStatus.BAD_REQUEST, "You are not allowed to reduce the net request trucks count less than net placed trucks count");
    if (!(indentInfoDto.getIndented() > 0))
      return new ApiResponse(HttpStatus.BAD_REQUEST, "Please provide valid number of indented trucks");
    if (indentInfoDto.getIndented() < indentInfoDto.getCancelled())
      return new ApiResponse(HttpStatus.BAD_REQUEST, "Cancelled trucks should not be more then Indented trucks");
    indentSummary.setIndented(indentInfoDto.getIndented());

    if (indentInfoDto.getStatus().equals(Status.CANCELLED)) {
      if (indentSummary.getNetPlaced() > 0)
        return new ApiResponse(HttpStatus.NOT_FOUND, String.format("operation not allowed after transporter responded or trucks reported to indent %s", indentInfoDto.getIndentId()));
      indentSummary.setCancelled(indentSummary.getIndented());
    } else {
      if ((indentInfoDto.getIndented() - indentInfoDto.getCancelled()) < indentSummary.getNetPlaced()) {
        throw new InvalidException("Invalid cancel quantity");
      } else {
        indentSummary.setCancelled(indentInfoDto.getCancelled());
      }
    }
    indentSummary.setNetRequested(indentSummary.getIndented() - indentSummary.getCancelled());
    indentSummary.setNetBalance(indentSummary.getNetRequested() - indentSummary.getNetPlaced());
    indentSummary.setComments(indentInfoDto.getComments());

    if (indentSummary.getNetBalance() > 0) {
      indentSummary.setStatus(Status.OPEN);
    } else {
      indentSummary.setStatus(Status.CLOSED);
    }
    if (indentSummary.getIndented().equals(indentSummary.getCancelled())) {
      indentSummary.setStatus(Status.CANCELLED);
    }
    /*Setting last modified user details*/
    indentSummary.setUpdateDate(new Date());
    indentSummary.setUpdateUser(applicationUser.getUserId());

    IndentSummary modifiedIndent = indentSummaryRepository.save(indentSummary);
    IndentInfoDto infoDto = new IndentInfoDto(modifiedIndent);

    if (!StringUtils.isEmpty(indentSummary.getDestCountry())) {
      CTCountry ctCountry = ctCountryRepository.findByCountryCode(indentSummary.getDestCountry());
      infoDto.setDestCountryName(ctCountry.getCountryName());
    }

    infoDto.setDestDis(indentInfoDto.getDestDis());
    infoDto.setDestCountryName(indentInfoDto.getDestCountryName());
    return new ApiResponse(HttpStatus.OK, "Indent Updated successfully", infoDto);
  }

  @Override
  public ApiResponse generateIndentPdf(IndentCommunicationDto indentCommDto, ApplicationUser loggedInUser) throws DocumentException {
    if (indentCommDto.getIndentIds().size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please provide at least one indent Details");
    List<IndentSummary> indentSummaries = indentSummaryRepository.findByIndentIdIn(indentCommDto.getIndentIds());
    boolean exist = indentSummaries.stream().anyMatch(indentSummary -> indentSummary.getSourceLocation().equals(loggedInUser.getPlantCode()));
    if (!exist)
      return new ApiResponse(HttpStatus.METHOD_NOT_ALLOWED, "You are not allowed to print this indent");
    boolean transporterCheck = indentSummaries.stream().map(IndentSummary::getTransporter).distinct().limit(2).count() <= 1;
    if (!transporterCheck)
      return new ApiResponse(HttpStatus.BAD_REQUEST, "All indents transporter should be same");
    String noTrucksIndents = indentSummaries.stream().filter(indentSummary -> indentSummary.getNetRequested() <= 0).map(IndentSummary::getIndentId).collect(Collectors.joining(","));
    if (!StringUtils.isEmpty(noTrucksIndents))
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("No trucks found with the indentid(s) %s", noTrucksIndents));
    PDFTableUtil pdfTableUtil = new PDFTableUtil();
    if (indentSummaries.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "No Indent Details found with given IndentIds");
    ByteArrayOutputStream baos = pdfTableUtil.generatePdfWithIndentData(indentSummaries);
    if (baos != null) {
      return new ApiResponse(HttpStatus.OK, "PDF generated Successfully", baos.toByteArray());
    }
    return new ApiResponse(HttpStatus.OK, "PDF generation failed");
  }

  @Override
  public ApiResponse sendIndentMail(IndentCommunicationDto indentCommDto, ApplicationUser loggedInUser) {
    if (indentCommDto.getIndentIds().size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please provide at least one indent Details");
    List<IndentSummary> indentSummaries = indentSummaryRepository.findByIndentIdIn(indentCommDto.getIndentIds());
    boolean souceLocationCheck = indentSummaries.stream().allMatch(indentSummary -> indentSummary.getSourceLocation().equals(loggedInUser.getPlantCode()));
    if (!souceLocationCheck)
      return new ApiResponse(HttpStatus.BAD_REQUEST, "You are not permitted to send mail");
    boolean transporterCheck = indentSummaries.stream().map(IndentSummary::getTransporter).distinct().limit(2).count() <= 1;
    if (!transporterCheck)
      return new ApiResponse(HttpStatus.BAD_REQUEST, "All indents transporter should be same");
    PDFTableUtil pdfTableUtil = new PDFTableUtil();
    if (indentSummaries.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, "No Indent Details found with given IndentIds");
    try {
      String htmlContent = "<table width='100%' border=1px solid black align='center' style='border-collapse: collapse' >"
          + "<tr  style='background-color: #5a3094 ;color:white;' align='center'; >"
          + "<td width='10%'><b>IndentID</b></td>"
          + "<td width='5%' ><b>Dispatch Date</b></td>"
          + "<td width='5%'><b>Source</b></td>"
          + "<td width='10%'><b>Source Desc</b></td>"
          + "<td width='5%'><b>Destination</b></td>"
          + "<td width='10%'><b>Dest Desc</b></td>"
          + "<td width='10%'><b>Truck Type</b></td>"
          + "<td width='10%'><b>Transporter</b></td>"
          + "<td width='5%'><b>Freight Available</b></td>"
          + "<td width='5%'><b>Category</b></td>"
          + "<td width='5%'><b>Indented</b></td>"
          + "<td width='5%'><b>Cancelled</b></td>"
          + "<td width='5%'><b>Net Indented</b></td>"
          + "<td width='5%'><b>Confirmed</b></td>"
          + "<td width='5%'><b>Declined</b></td>"
          + "<td width='5%'><b>To Be Confirmed</b></td>"
          + "<td width= '5%'><b>Reported</b></td>"
          + "<td width= '5%'><b>Rejected</b></td>"
          + "<td width= '5%'><b>Net Balance</b></td>"
          + "<td width= '5%'><b>Status</b></td>"
          + "<td width='20%'><b>Comments</b></td>"
          + "</tr>";

      for (IndentSummary indentSummary : indentSummaries) {
        String sourceLocDesc = locationRepository.findDescriptionByLocId(indentSummary.getSourceLocation());
        String destDesc = locationRepository.findDescriptionByLocId(indentSummary.getDestinationLocation());
        htmlContent = htmlContent + "<tr align='center'>"
            + "<td width='10%'> " + indentSummary.getIndentId() + "</td>"
            + "<td width='5%'>" + DateUtils.formatDate(indentSummary.getDispatchDate(), Constants.PLAN_RECORD_DATE_FORMAT) + "</td>"
            + "<td width='5%'>" + indentSummary.getSourceLocation() + "</td>"
            + "<td width='10%'>" +( sourceLocDesc == null ? "-" : sourceLocDesc) + "</td>"
            + "<td width='5%'>" + indentSummary.getDestinationLocation() + "</td>"
            + "<td width='10%'>" + (destDesc == null ? "-" : destDesc) + "</td>"
            + "<td width='10%'>" + indentSummary.getTruckType() + "</td>"
            + "<td width='10%'>" + indentSummary.getTransporter() + "</td>"
            + "<td width='5%'>" + (indentSummary.getIsFreightAvailable() != null ? indentSummary.getIsFreightAvailable() : "-") + "</td>"
            + "<td width='5%'>" + (indentSummary.getCategory() != null ? indentSummary.getCategory() : " ") + "</td>"
            + "<td width='5%'>" + indentSummary.getIndented() + "</td>"
            + "<td width='5%'>" + indentSummary.getCancelled() + "</td>"
            + "<td width='5%'>" + indentSummary.getNetRequested() + "</td>"
            + "<td width='5%'>" + indentSummary.getTransConfirmed() + "</td>"
            + "<td width='5%'>" + indentSummary.getTransDeclined() + "</td>"
            + "<td width='5%'>" + indentSummary.getNetBalance() + "</td>"
            + "<td width='5%'>" + indentSummary.getReported() + "</td>"
            + "<td width='5%'>" + indentSummary.getRejected() + "</td>"
            + "<td width='5%'>" + indentSummary.getNetBalance() + "</td>"
            + "<td width='5%'>" + indentSummary.getStatus() + "</td>"
            + "<td width='20%'>" + (indentSummary.getComments() != null ? indentSummary.getComments() : " ") + "</td>"
            + "</tr>";
      }
      htmlContent = htmlContent + "</table>";
      Clob htmlContentClob = new javax.sql.rowset.serial.SerialClob(htmlContent.toCharArray());
      Blob pdfBlob = new javax.sql.rowset.serial.SerialBlob(pdfTableUtil.generatePdfWithIndentData(indentSummaries).toByteArray());
      StoredProcedureQuery storedProcedure = entityManager
          .createStoredProcedureQuery("atl_business_flow_pkg.indent_notify");

      storedProcedure.registerStoredProcedureParameter("p_login_user", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_indent_id", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_eb", Clob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_att", Blob.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_fn", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_mimt", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("p_status", String.class, ParameterMode.OUT);
      storedProcedure.setParameter("p_login_user", loggedInUser.getUserId());
      storedProcedure.setParameter("p_indent_id", indentSummaries.get(0).getIndentId());
      storedProcedure.setParameter("p_eb", htmlContentClob);
      storedProcedure.setParameter("p_att", pdfBlob);
      storedProcedure.setParameter("p_fn", indentSummaries.get(0).getIndentId() + ".pdf");
      storedProcedure.setParameter("p_mimt", " text/csv");
      storedProcedure.execute();
      String result = storedProcedure.getOutputParameterValue("p_status") != null ? (String) storedProcedure.getOutputParameterValue("p_status") : null;
      if (result != null && result.equalsIgnoreCase("SUCCESS"))
        return new ApiResponse(HttpStatus.OK, "Mail send successfully", result);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return new ApiResponse(HttpStatus.NOT_FOUND, "Mail sending failed");
  }

  @Override
  public ApiResponse getIndents(IndentFilterDto indentFilterDto, ApplicationUser loggedInUser) {
    Page<IndentSummary> pagableIndents;
    if (!indentFilterDto.isIndentFilterByFGS()) {
      Page<IndentSummary> indentSummaries = null;
      /*DP_REP, L1_MGR, L2_MGR*/
      if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
        indentSummaries = indentSummaryRepository.getAllIndentSummeryWithDescDPREP(PageRequest.of(indentFilterDto.getIndex(),
            indentFilterDto.getPageLength(), Sort.Direction.DESC, "dispatchDate"));
      } else {
        Sort sort = Sort.by(Sort.Order.desc("dispatchDate"), Sort.Order.desc("id"));
        indentSummaries = indentSummaryRepository
            .getIndentSummeryWithDesc(loggedInUser.getPlantCode(), PageRequest.of(indentFilterDto.getIndex(),
                indentFilterDto.getPageLength(), sort));
      }
      List<IndentSummary> summaries = indentSummaries.getContent();

      List<String> indentIds = summaries.parallelStream().map(indent -> indent.getIndentId()).collect(Collectors.toList());
      while (!indentIds.isEmpty()) {
        int maxSize = indentIds.size() < 1000 ? indentIds.size() : 1000;
        List<String> subindentIds = indentIds.subList(0, maxSize);
//				List<IndentDetails> indentDetails = indentDetailsRepo.findByIndentSummaryIndentIdIn(subindentIds);

        List<Map<String, Object>> indentDetailsMap = indentDetailsRepo.findAllIndentDetailsByIndentIdIn(subindentIds);

        List<IndentDetails> indentDetails = indentDetailsMap.stream().parallel().map(IndentDetails::new).collect(Collectors.toList());

        for (IndentSummary indentSummary : summaries) {
          Set<IndentDetails> details = indentDetails.parallelStream().filter(
              indentDetail -> indentSummary.getIndentId().equals(indentDetail.getIndentId()))
              .collect(Collectors.toSet());
          indentSummary.setIndentDetails(details);
        }
        indentIds.removeAll(subindentIds);
      }

      pagableIndents = new PageImpl<>(summaries, PageRequest.of(indentFilterDto.getIndex(), indentFilterDto.getPageLength(), Sort.Direction.DESC, "dispatchDate"), indentSummaries.getTotalElements());
    } else {
      /*DP_REP, L1_MGR, L2_MGR*/
      if (!UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole()))
        indentFilterDto.setSource(loggedInUser.getPlantCode());
      pagableIndents = filterService.filterIndents(indentFilterDto, loggedInUser);
    }
    /*Setting the reported trucks list
    * To display in report-indent page-> bottom grid*/
    List<String> indentIds = pagableIndents.stream().parallel().map(IndentSummary::getIndentId).distinct().collect(Collectors.toList());
    List<TruckReport> truckReportList = new ArrayList<>();
    ListUtils.partition(indentIds, 999).stream().forEach(subList -> {
      truckReportList.addAll(truckReportRepo.findByIndentSummaryIdIn(subList));
    });

    pagableIndents.stream().forEach(indent -> {
      indent.setTruckReports(truckReportList.parallelStream().filter(truck -> truck.getIndentSummary().getIndentId().equals(indent.getIndentId())).collect(Collectors.toSet()));
    });

    List<String> countryCodeList = pagableIndents.stream().parallel().map(page -> page.getDestCountry()).distinct().collect(Collectors.toList());

    List<CTCountry> ctCountryList = ctCountryRepository.findAllByCountryCodeIn(countryCodeList);

//		while (!countryCodeList.isEmpty()) {
//			int maxSize = countryCodeList.size() < 1000 ? countryCodeList.size() : 1000;
//			List<String> subcountryCodes = countryCodeList.subList(0, maxSize);
//			List<CTCountry> ctCountryList = ctCountryRepository.findAllByCountryCodeIn(subcountryCodes);

    if (ctCountryList.size() > 0) {
      pagableIndents.stream().parallel().forEach(indentSummary -> {
        ctCountryList.parallelStream().forEach(ctCountry -> {
          if (!StringUtils.isEmpty(ctCountry.getCountryCode()) && !StringUtils.isEmpty(indentSummary.getDestCountry())) {
            if (ctCountry.getCountryCode().equalsIgnoreCase(indentSummary.getDestCountry())) {
              indentSummary.setDestCountryName(ctCountry.getCountryName());
            }
          }
        });
      });
    }
//			countryCodeList.removeAll(subcountryCodes);
//		}


//    IndentFilterDto indentsList = null;
//    if (!indentFilterDto.isIndentFilterByFGS()) {
//      indentsList = new IndentFilterDto(pagableIndents, "");
//    } else {
//      indentsList = new IndentFilterDto(pagableIndents);
//    }

    IndentFilterDto indentsList = new IndentFilterDto(pagableIndents, "");

    List<String> uniqueDest = pagableIndents.stream().parallel().map(IndentSummary::getDestinationLocation).distinct().collect(Collectors.toList());
    if (uniqueDest.size() > 0) {
      List<Map<String, String>> destWithDesc = locationRepository.findDestDescWtihDestinations(uniqueDest);
      indentsList.getIndents().parallelStream().forEach(indent -> {
        indent.setDestDis(destWithDesc.stream().filter(destDescMap -> destDescMap.get("destLoc").equals(indent.getDestination())).findAny().get().get("DESTDESC"));
      });
    }
    return new ApiResponse(HttpStatus.OK, "", indentsList);
  }

  @Override
  public ApiResponse reportTruck(TruckReportDto truckReportDto, boolean isGateSecurityScreen, ApplicationUser loggedInUser) {
    TruckReportStatus status = TruckReportStatus.valueOf(truckReportDto.getStatus());
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
      case REJECT:
        //Ofter inspection, ops can reject the truck
        return this.rejectTruck(truckReportDto, loggedInUser, isGateSecurityScreen);
      case BAY_ASSIGNED:
        // User will assign bay for truck
        return this.assignBay(truckReportDto, loggedInUser);
      default:
        break;
    }
    return new ApiResponse(HttpStatus.NOT_FOUND, "Invalid truck report data ! ");
  }

  private ApiResponse rejectTruck(TruckReportDto truckReportDto, ApplicationUser loggedInUser, boolean isGateSecurityScreen) {
    String invalidInfo = truckReportDto.validateToRejectTruck();
    if (!StringUtils.isEmpty(invalidInfo))
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("Please provide %s info", invalidInfo));

    Optional<IndentSummary> optionalIndentSummary = indentSummaryRepository.findOneByIndentId(truckReportDto.getIndentId());
    if (!optionalIndentSummary.isPresent())
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("IndentId %s is not present In system", truckReportDto.getIndentId()));
    Optional<IndentDetails> optionalIndentDetails = indentDetailsRepo.findByIndentSummaryIndentIdAndTruckTruckNumberAndGateControlCodeAndStatusNot(truckReportDto.getIndentId(), truckReportDto.getTruckNumber(), truckReportDto.getGateControlCode(), TruckReportStatus.INTRANSIT);
    if (!optionalIndentDetails.isPresent())
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format(" Indent Details are not found with IndentId %s ", truckReportDto.getIndentId()));
    Optional<TruckReport> optionalTruckReport = truckReportRepo
        .findOneByGateControlCodeAndIndentSummaryIndentIdAndTruckTruckNumber(truckReportDto.getGateControlCode(), truckReportDto.getIndentId(), truckReportDto.getTruckNumber());
    if (!optionalTruckReport.isPresent())
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("Truck details not found with truck number %s", truckReportDto.getTruckNumber()));
    TruckReport truckReport = optionalTruckReport.get();
    if ((truckReport.getStatus().equals(TruckReportStatus.GATED_IN)) || (truckReport.getStatus().equals(TruckReportStatus.REPORTED))) {
      // For GS role, truck status is reported then ask for rejection code and save rejection code in table

      if (!StringUtils.isEmpty(truckReportDto.getRejectionCode())) {
        //save rejection code
        truckReport.setRejectionCode(truckReportDto.getRejectionCode());
      } else {
        return new ApiResponse(HttpStatus.BAD_REQUEST, String.format("Please provide rejection code for truck number %s", truckReportDto.getTruckNumber()));
      }

      truckReport.setRejectedStatus(TruckReportStatus.REJECTED);
      /*isGateSecurityScreen only used when operator from RDC loc rejects the truck from GATE-SECURITY -> INVENTORY-SHIPPIN/RECEIVING
      * Earlier when operator used to reject the truck from gate_security screen the truck status was not updated to COMPLETED*/
      if (loggedInUser.getRole().isRoleGateSecurity() || isGateSecurityScreen)
        truckReport.setStatus(TruckReportStatus.COMPLETED);
      truckReport.setUpdateUser(loggedInUser.getUserId());
      truckReport.setUpdateDate(new Date());
      truckReportRepo.save(truckReport);

      IndentSummary indentSummary = optionalIndentSummary.get();
      indentSummary.setRejected(indentSummary.getRejected() + 1);
      indentSummary.setNetPlaced(indentSummary.getReported() - indentSummary.getRejected());
      indentSummary.setNetBalance(indentSummary.getNetRequested() - indentSummary.getNetPlaced());
      indentSummary.setUpdateDate(new Date());
      indentSummary.setStatus(Status.OPEN);
      indentSummary.setUpdateUser(loggedInUser.getUserId());
      indentSummaryRepository.save(indentSummary);

      IndentDetails indentDetails = optionalIndentDetails.get();
      indentDetails.setStatus(TruckReportStatus.REJECTED);
      indentDetails.setUpdateUser(loggedInUser.getUserId());
      indentDetails.setUpdateDate(new Date());
      indentDetailsRepo.save(indentDetails);
      TruckReportDto reportDto = new TruckReportDto(truckReport);
      reportDto.setDestDis(truckReportDto.getDestDis());

      Optional<CTRejection> ctRejection = ctRejectionRepository.findById(truckReportDto.getRejectionCode());
      if (ctRejection.isPresent())
        reportDto.setRejectionDesc(ctRejection.get().getRejectionDesc());
      reportDto.setDestCountryName(truckReportDto.getDestCountryName());

      return new ApiResponse(HttpStatus.OK, String.format("Truck %s rejected successfully", truckReportDto.getTruckNumber()), reportDto);
    } else {
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("Truck can not be rejected ", truckReportDto.getTruckNumber()));
    }
  }

  private ApiResponse callTruck(TruckReportDto truckReportDto, ApplicationUser loggedInUser) {
    // validate truck bay assigning details
    String invalidInfo = truckReportDto.validateTruckBayAssign();
    if (!StringUtils.isEmpty(invalidInfo))
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("Please provide %s info", invalidInfo));
    Optional<TruckReport> optionalTruckReport = truckReportRepo
        .findOneByGateControlCodeAndIndentSummaryIndentIdAndStatus(truckReportDto.getGateControlCode(),
            truckReportDto.getIndentId(), TruckReportStatus.REPORTED);
    if (!optionalTruckReport.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("Reported truck not found with indent id %s and Gate control code %s",
              truckReportDto.getIndentId(), truckReportDto.getGateControlCode()));
    }
    TruckReport truckReport = optionalTruckReport.get();
    truckReport.setBayStatus(BayStatus.CALL);
    truckReport.setUpdateUser(loggedInUser.getUserId());
    truckReport.setUpdateDate(new Date());
    TruckReportDto reportDto = new TruckReportDto(null, truckReportRepo.save(truckReport), loggedInUser);
    reportDto.setDestDis(truckReportDto.getDestDis());
    reportDto.setDestCountryName(truckReportDto.getDestCountryName());
    return new ApiResponse(HttpStatus.OK, "", reportDto);
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
    //TO REMOVE duplicates of reported trucks and indent reports
    Set<TruckReport> truckReports = indentSummary.getTruckReports().stream().distinct().collect(Collectors.toSet());
    Set<IndentDetails> indentDetailsList = indentSummary.getIndentDetails().stream().distinct().collect(Collectors.toSet());
    indentSummary.setIndentDetails(indentDetailsList);
    indentSummary.setTruckReports(truckReports);
    /*Making container number mandetory when destination is PORT and country is Nepal*/
    String countryCode = null;
    Optional<MTLocation> mtLocationOpttional = locationRepository.findByLocationId(indentSummary.getDestinationLocation());
    if (mtLocationOpttional.isPresent()) {
      countryCode = mtLocationOpttional.get().getCountry();
    } else {
      Map<String, Object> mTCustomerMap = mtCustomerRepositary.findByCustId(indentSummary.getDestinationLocation());
      if (mTCustomerMap != null) {
        countryCode = mTCustomerMap.get("country").toString();
      }
    }
    if (StringUtils.isEmpty(indentReportDto.getContainerNum())) {
      if ((mtLocationOpttional.isPresent() && LocationType.PORT.name().equals(mtLocationOpttional.get().getType()))
          || (ctCountryRepository.existsByCountryCodeAndIsExport(countryCode, "Y"))) {
        return new ApiResponse(HttpStatus.NOT_FOUND, "Container number is mandatory for the truck indented to Export location!");
      }
    }
    if (indentSummary.getStatus().equals(Status.CANCELLED)) {
      return new ApiResponse(HttpStatus.BAD_REQUEST, String.format("Indent with id %s is cancelled,Not allowed to report truck", indentReportDto.getIndentId()));
    }
    if (!loggedInUser.getPlantCode().equals(indentSummary.getSourceLocation()))
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("You are not authorized to report indent  %s", indentReportDto.getIndentId()));
    if (indentSummary.getStatus().equals(Status.CLOSED) || indentSummary.getNetBalance() == 0) {
      return new ApiResponse(HttpStatus.NOT_FOUND, String
          .format("You are not report more than requested qty for indent  %s", indentReportDto.getIndentId()));
    }
    // Un commment the code if GPS functionality require
/*
    if (indentReportDto.isGpsEnabled()) {
      if (StringUtils.isEmpty(indentReportDto.getGpsProvider()))
        return new ApiResponse(HttpStatus.NOT_FOUND, "please provide the Gps provider");
      gpsProvider = gpsProviderInfoRepo.findOneByGpsProvider(indentReportDto.getGpsProvider());
      if (gpsProvider == null)
        return new ApiResponse(HttpStatus.NOT_FOUND,
            String.format("Gps provider %s not found in system", indentReportDto.getGpsProvider()));
    }
*/

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
    if (truckReportRepo.existsByTruckTruckNumberAndStatusInAndReportLocation(indentReportDto.getTruckNumber(),
        TruckReportStatus.getInventoryTrucks(), loggedInUser.getPlantCode())) {
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("Truck %s is already reported ", indentReportDto.getTruckNumber()));
    }

    // update master truck details if exist else insert new one
    MTTruck mtTruck = this.updateTruckDetails(indentReportDto, indentSummary.getTransporter(), loggedInUser,
        optionalTruckType.get(), gpsProvider,
        truckRepo.findOneByTruckNumberIgnoreCase(indentReportDto.getTruckNumber()));

    if (mtTruck.getStatus() != null && mtTruck.getStatus().equals(TruckStatus.BLACKLISTED))
      return new ApiResponse(HttpStatus.METHOD_NOT_ALLOWED,
          String.format("Truck %s is blacklisted in system !", mtTruck.getTruckNumber()));

    IndentDetails indentDetails = this.createIndentDetails(indentSummary, indentReportDto, mtTruck, loggedInUser);
    TruckReport truckReport = this.createTruckReporting(indentSummary, indentDetails, loggedInUser, mtTruck);


//		indentDetails.setContainerNum(indentReportDto.getContainerNum());
//		truckReport.setContainerNum(indentReportDto.getContainerNum());
    indentDetailsRepo.save(indentDetails);


    Integer reportedTrucksCount = indentSummary.getReported();
    indentSummary.setReported(reportedTrucksCount == null ? 1 : reportedTrucksCount + 1);
    indentSummary.setNetPlaced(indentSummary.getReported() - (indentSummary.getRejected() == null ? 0 : indentSummary.getRejected()));
    indentSummary.setNetBalance(indentSummary.getNetRequested() - indentSummary.getNetPlaced());
    if (indentSummary.getNetBalance() == 0)
      indentSummary.setStatus(Status.CLOSED);
    indentSummary.setUpdateUser(loggedInUser.getUserId());
    indentSummary.setUpdateDate(new Date());
    indentDetails.setGateControlCode(truckReport.getGateControlCode());
    indentDetails.setUpdateDate(new Date());
//		indentDetails.setUpdateUser(loggedInUser.getUserId());
    // indentDetailsRepo.save(indentDetails);

    /*
     *While editing truck these fields are required
     * */
    truckReport.setInWeight(indentReportDto.getInWeight());
    truckReport.setOutWeight(indentReportDto.getOutWeight());
    truckReport.setNetWeight(indentReportDto.getNetWeight());
    if (!StringUtils.isEmpty(indentReportDto.getEditTruckStatus()))
      truckReport.setStatus(TruckReportStatus.valueOf(indentReportDto.getEditTruckStatus()));
    if (!StringUtils.isEmpty(indentReportDto.getBayStatus()))
      truckReport.setBayStatus(BayStatus.valueOf(indentReportDto.getBayStatus()));
    truckReport.setBay(indentReportDto.getBayAssigned());
    truckReport.setComments(indentReportDto.getComments());
    truckReport.setGateInDate(indentReportDto.getEditGateInDate());
    if (indentReportDto.getEditReportedDate() != null) {
      truckReport.setReportDate(indentReportDto.getEditReportedDate());
    }
    if (!StringUtils.isEmpty(indentReportDto.getUpdateUser())) {
      truckReport.setUpdateUser(loggedInUser.getUserId());
      truckReport.setUpdateDate(new Date());
    }

    /*If the Destination country is Nepal i.e., IS_EXPORT = 'Y' then set containerNum = truckNumber*/
    /*used when updating truck number*/
    if (!StringUtils.isEmpty(countryCode) && ctCountryRepository.existsByCountryCodeAndIsExport(countryCode, "Y")) {
      indentDetails.setContainerNum(indentReportDto.getTruckNumber());
      truckReport.setContainerNum(indentReportDto.getTruckNumber());
    } else {
      indentDetails.setContainerNum(indentReportDto.getContainerNum());
      truckReport.setContainerNum(indentReportDto.getContainerNum());
    }

    //setting truck report flags
    truckReport.setIsPuc(indentReportDto.isPuc() ? "Y" : "N");
    truckReport.setIsInsurance(indentReportDto.isInsurance() ? "Y" : "N");
    truckReport.setIsSeatBelt(indentReportDto.isSeatBelt() ? "Y" : "N");
    truckReport.setIsFirstAid(indentReportDto.isFirstAid() ? "Y" : "N");
    truckReport.setIsFireExtenguisher(indentReportDto.isFireExtenguisher() ? "Y" : "N");
    truckReport.setIsEmergencyCard(indentReportDto.isEmergencyCard() ? "Y" : "N");
    truckReport.setIsSparKArrestor(indentReportDto.isSparKArrestor() ? "Y" : "N");
    truckReport.setIsFitnessCert(indentReportDto.isFitnessCert() ? "Y" : "N");
    truckReport.setTruckCapacity(indentReportDto.getTruckCapacity());
    truckReport.setTruckGrossVehicleWt(indentReportDto.getTruckGrossVehicleWt());
    truckReport.setTruckUnladenWt(indentReportDto.getTruckUnladenWt());
    truckReport.setBsNorms(indentReportDto.getBsNorms());
    truckReport.setFuelType(indentReportDto.getFuelType());


    truckReportRepo.save(truckReport);

    indentSummary.indentDetails.add(indentDetails);
    indentSummary.truckReports.add(truckReport);

    indentSummaryRepository.save(indentSummary);
    IndentInfoDto indentInfoDto = new IndentInfoDto(indentSummary);
    indentInfoDto.setEditTruckReport(truckReport);
    indentInfoDto.setDestDis(indentReportDto.getDestDis());
    if (!StringUtils.isEmpty(indentInfoDto.getDestCountry())) {
      CTCountry ctCountry = ctCountryRepository.findByCountryCode(indentInfoDto.getDestCountry());
      indentInfoDto.setDestCountryName(ctCountry.getCountryName());
    }
    return new ApiResponse(HttpStatus.OK,
        String.format("Truck reported successfully for indent %s", indentSummary.getIndentId()), indentInfoDto);
  }

  @Override
  public ApiResponse getReportedDriverDetails(String truckNumber, String indentId) {
    if (StringUtils.isEmpty(truckNumber))
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please give the truckNumber");

    TruckReportDto truckReportDto = new TruckReportDto();
    Optional<IndentSummary> indentSummary = indentSummaryRepository.findOneByIndentId(indentId);
    if (!indentSummary.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "No indent found with the indent id : " + indentId);
    }
    Optional<MTLocation> optionalMTLocation = locationRepository.findByLocationId(indentSummary.get().getDestinationLocation());
    String countryCode = null;
    if (optionalMTLocation.isPresent()) {
      countryCode = optionalMTLocation.get().getCountry();
    } else {
      Map<String, Object> mtCustomerMap = mtCustomerRepositary.findByCustId(indentSummary.get().getDestinationLocation());
      if (mtCustomerMap != null) {
        countryCode = mtCustomerMap.get("country").toString();
      }
    }
    if (!StringUtils.isEmpty(countryCode) && ctCountryRepository.existsByCountryCodeAndIsExport(countryCode, "Y")) {
      truckReportDto.setContainerNum(truckNumber);
    }

    Optional<TruckReport> truckReport = truckReportRepo.findFirst1ByTruckTruckNumberOrderByReportDateDesc(truckNumber);
    if (truckReport.isPresent()) {
      truckReportDto.setDriverName(truckReport.get().getDriverName());
      truckReportDto.setDriverContact(truckReport.get().getDriverMobile());
      truckReportDto.setDriverLicense(truckReport.get().getDriverLicense());
      //truckReportDto.setContainerNum(truckReport.get().getContainerNum());
      truckReportDto.setTruckType(truckReport.get().getTruckType());
      //Get from Master Truck
      truckReportDto.setTruckGrossVehicleWt(truckReport.get().getTruckGrossVehicleWt());
      truckReportDto.setTruckUnladenWt(truckReport.get().getTruckUnladenWt());
      truckReportDto.setFuelType(truckReport.get().getFuelType());
      truckReportDto.setBsNorms(truckReport.get().getBsNorms());
    }
    return new ApiResponse(HttpStatus.OK, "", truckReportDto);
  }

  @Override
  public ApiResponse getOpenDestinations(ApplicationUser applicationUser) {
    // removed String formart
    // Fetch All Open Destinations from Dispatch Plan With Open status but exclude the locations whose Location Type is PORT
    List<String> sourceList = new ArrayList<>();
    sourceList.add(applicationUser.getPlantCode());
    Optional<MTLocation> optionalMTLocation = locationRepository.findByLocationId(applicationUser.getPlantCode());
    if (optionalMTLocation.isPresent()) {
      if (!StringUtils.isEmpty(optionalMTLocation.get().getLinkedPlant())) {
        sourceList.add(optionalMTLocation.get().getLinkedPlant());
      }
    }
		/*return new ApiResponse(HttpStatus.OK, "unique DEST_LOC from DISPATCH_PLAN table where status = OPEN.",
				dispatchPlanItemInfoRepository.findOpenDestinationsInOrderByDest(Status.OPEN, sourceList));*/
    List<Map<String, Object>> destinationList = dispatchPlanItemInfoRepository.findOpenDestinationsAndDescriptionInOrderByDest(Status.OPEN, sourceList);
    List<Map<String, Object>> descriptionList = new ArrayList<>();
    descriptionList.addAll(destinationList);
    /*Sorting the destination list with Description in ASC order*/
//		Comparator<Map<String, Object>> nameSorter = (a, b) -> a.get("dest_desc") != null && b.get("dest_desc") != null ? a.get("dest_desc").toString().compareToIgnoreCase(b.get("dest_desc").toString()) : 0 ;
//		Collections.sort(descriptionList, nameSorter);
    descriptionList = descriptionList.parallelStream().filter(m -> !StringUtils.isEmpty(m.get("dest_desc"))).
        sorted(Comparator.comparing(m -> m.get("dest_desc").toString().toLowerCase())).collect(Collectors.toList());
    OpenDestinationDto openDestinationDto = new OpenDestinationDto(destinationList, descriptionList);

    return new ApiResponse(HttpStatus.OK, "unique DEST_LOC from DISPATCH_PLAN table where status = OPEN.", openDestinationDto);
  }

  @Override
  public ApiResponse getLoadSlipPlans(String destLoc, String loadslipId, String itemCategory, ApplicationUser loggedInUser) {
    // Fetch all shipTo Ids for given customerID, Here destLocation is CustomerID
    // for MT_CUSTOMER & MT_CUSTOMER_SHIP_TO tables;
    List<String> shipToLocations = mtCustomerRepositary.findShipToLocationsByCustomerId(destLoc);
    List<String> destinationList = new ArrayList<>();
    if (shipToLocations != null && !shipToLocations.isEmpty()) {
      destinationList = shipToLocations;
    } else {
      destinationList.add(destLoc);
    }

    // FGS will display all the trucks from TRUCK_REPORTING table which are in
    // status GATED_IN.
/*
    List<Map<String, String>> gatedInTrucks = truckReportRepo.findTrucksByStatusAndStatusNotAndSourceAndDestLocAndReportingLoc(TruckReportStatus.GATED_IN.name(),
        TruckReportStatus.INTRANSIT.name(), loggedInUser.getPlantCode(), destLoc, loggedInUser.getPlantCode());
*/

    /*To get reported trucks with shipTo locations and itemCategory*/
    List<Map<String, String>> gatedInTrucks = truckReportRepo.findTrucksByStatusAndStatusNotAndSourceAndDestLocInAndReportingLoc(TruckReportStatus.GATED_IN.name(),
        TruckReportStatus.INTRANSIT.name(), loggedInUser.getPlantCode(), destinationList, loggedInUser.getPlantCode());
		/*if (!StringUtils.isEmpty(itemCategory)){
	    gatedInTrucks = truckReportRepo.findTrucksByStatusAndStatusNotAndSourceAndDestLocInAndReportingLocAndItemCategory(TruckReportStatus.GATED_IN.name(),
					TruckReportStatus.INTRANSIT.name(), loggedInUser.getPlantCode(), destinationList, loggedInUser.getPlantCode(), itemCategory);
		} else {
      gatedInTrucks = truckReportRepo.findTrucksByStatusAndStatusNotAndSourceAndDestLocInAndReportingLoc(TruckReportStatus.GATED_IN.name(),
					TruckReportStatus.INTRANSIT.name(), loggedInUser.getPlantCode(), destinationList, loggedInUser.getPlantCode());
		}
*/
    List<Map<String, Object>> map2 = new ArrayList<>();
    List<Map<String, Object>> loadSlipPlans = null;

    /*If loggedInplantcode = EXT_WAREHOUSE then actual source will be its Linked plant (Only 1007) else actualSource = loggedinPlantCode*/
    String actualSourceLoc = utilityService.getLinkedPlntLoc(loggedInUser.getPlantCode(), destLoc);
    Set<String> itemCategories = new HashSet<>();
    if (!StringUtils.isEmpty(loadslipId)) {
      if (!StringUtils.isEmpty(itemCategory)) {
        // this will display list of all dispatch plans grouped up by material code with a particular item category
        List<String> categories = new ArrayList<>(Arrays.asList(itemCategory.split(",")));

        loadSlipPlans = dispatchPlanItemInfoRepository.findApprovedPlansByDestinationExcludingCurrentLoadslipItemsAndItemCategory(actualSourceLoc, destLoc, categories, loadslipId);
      } else {
        // Displays all the dispatch plans from Dispatch plan and Dispatch plan bom table group by item id and batch code, excluding the items involved in current loadslip
        /*      loadSlipPlans = dispatchPlanItemInfoRepository.findApprovedPlansByDestinationExcludingCurrentLoadslipItems(loggedInUser.getPlantCode(), destLoc, loadslipId);*/
        //List<String> categories = new ArrayList<>(Arrays.asList(itemCategory.split(",")));
        loadSlipPlans = dispatchPlanItemInfoRepository.findApprovedPlansByDestinationExcludingCurrentLoadslipItems(actualSourceLoc, destLoc, loadslipId);
      }
      List<Map<String, Object>> openDestPlans = dispatchPlanItemInfoRepository.findApprovedPlansByDestinationExcludingCurrentLoadslipItems(actualSourceLoc, destLoc, loadslipId);
      for (Map<String, Object> map : openDestPlans) {
        itemCategories.add(map.get("item_category").toString());
      }
    } else {
      if (!StringUtils.isEmpty(itemCategory)) {
        // this will display list of all dispatch plans grouped up by material code with a particular item category
        List<String> categories = new ArrayList<>(Arrays.asList(itemCategory.split(",")));

        loadSlipPlans = dispatchPlanItemInfoRepository.findApprovedPlansByDestinationAndItemCategory(actualSourceLoc, destLoc, categories);
      } else {
        // OPS will display list of all dispatch plans grouped up by material code
        // sorted in ascending order of PRIORITY and BOM details related to that
        // material code in DISPATCH_PLAN_BOM .
        // OPS will fetch the details from DISPATCH_PLAN and DISPATCH_PLAN_BOM tables.
        /*loadSlipPlans = dispatchPlanItemInfoRepository.findApprovedPlansByDestination(loggedInUser.getPlantCode(), destLoc);*/
        loadSlipPlans = dispatchPlanItemInfoRepository.findApprovedPlansByDestination(actualSourceLoc, destLoc);
      }
      List<Map<String, Object>> openDestPlans = dispatchPlanItemInfoRepository.findApprovedPlansByDestination(actualSourceLoc, destLoc);
      for (Map<String, Object> map : openDestPlans) {
        itemCategories.add(map.get("item_category").toString());
      }
    }
    for (Map<String, Object> map : loadSlipPlans) {
      Map<String, Object> tempMap = new HashMap<>();
      tempMap.putAll(map);
      tempMap.put("ITEM_CLASSIFICATION", itemRepository.findById(map.get("ITEM_ID").toString()).get().getClassification());
      map2.add(tempMap);
//			itemCategories.add(map.get("item_category").toString());
    }
    String city = locationRepository.findCityByLocId(destLoc);
    LoadSlipPlansDto loadSlipPlansDto = new LoadSlipPlansDto(shipToLocations, gatedInTrucks, map2, deriveTubeAndFlapBatchCodes(), itemCategories, city);
    return new ApiResponse(HttpStatus.OK, "", loadSlipPlansDto);
  }

  @Override
  public ApiResponse getLoadSlipUtilizations(LsUtilizationDto lsUtilizationDto, ApplicationUser applicationUser) {
    LoadSlipUtilizationDto loadSlipUtilizationDto = new LoadSlipUtilizationDto();
    String variant1 = getVariant1BySelectedTruck(lsUtilizationDto, loadSlipUtilizationDto, applicationUser);
    lsUtilizationDto.setVariant1(variant1);
    loadSlipUtilizationDto.setVariant1(variant1);

    // Get Ship_To_Loc from truck if truck is indented for ship_to_loc
    loadSlipUtilizationDto.setShipToDestination(findShipToLocFromTruck(lsUtilizationDto, applicationUser));

    // Loadslip is not yet created
    if (StringUtils.isEmpty(lsUtilizationDto.getShipmentId())) {
      Optional<MTTruck> optionalMtTruck = truckRepo.findOneByTruckNumberIgnoreCase(lsUtilizationDto.getTruckNumber());
      if (!StringUtils.isEmpty(lsUtilizationDto.getActucaltrucktype()))
        getMtTrucktypeDetails(lsUtilizationDto.getActucaltrucktype(), lsUtilizationDto.getVariant1(), loadSlipUtilizationDto);
      else
        getMtTrucktypeDetails(lsUtilizationDto.getTrucktype(), lsUtilizationDto.getVariant1(), loadSlipUtilizationDto);
      // If truck is available in MT_TRUCK master table and has value of
      // PASSING_WEIGHT, select the PASSING_WEIGHT as the Truck Weight Capacity
      // If truck is not available in the MT_TRUCK master table or if the value of
      // PASSING_WEIGHT is not available, take the GROSS_WT from the MT_TRUCK_TYPE as
      // Truck Weight capacity
      if (optionalMtTruck.isPresent() && optionalMtTruck.get().getPassingWeight() != null
          && optionalMtTruck.get().getPassingWeight() > 0) {
        loadSlipUtilizationDto.setTruckWeightCapacity(optionalMtTruck.get().getPassingWeight());
      }

      loadSlipUtilizationDto.setTruckType(lsUtilizationDto.getTrucktype());
      loadSlipUtilizationDto.setActualTruckType(lsUtilizationDto.getActucaltrucktype());
      loadSlipUtilizationDto.setTruckNumber(lsUtilizationDto.getTruckNumber());
      loadSlipUtilizationDto.setTruckIndentCategory(getIndentCategoryByTruckNumber(lsUtilizationDto.getTruckNumber(), applicationUser));

    } else if (!StringUtils.isEmpty(lsUtilizationDto.getShipmentId()) && (!StringUtils.isEmpty(lsUtilizationDto.getTrucktype()) || !StringUtils.isEmpty(lsUtilizationDto.getActucaltrucktype()))) {
      Optional<Shipment> optionalShipment = shipmentRepository.findByShipmentId(lsUtilizationDto.getShipmentId());
      if (!optionalShipment.isPresent())
        return new ApiResponse(HttpStatus.NOT_FOUND, String.format("No shipment find with Id %s", lsUtilizationDto.getShipmentId()));
      Shipment shipment = optionalShipment.get();
      Optional<String> destLocOpt = loadslipRepository.findAllByShipmentShipmentId(shipment.getShipmentId()).stream().findFirst().map(t -> t.getDestLoc());
      String destLoc = destLocOpt.isPresent() ? destLocOpt.get() : null;
      Optional<MTTruck> optionalMtTruck = truckRepo.findOneByTruckNumberIgnoreCase(lsUtilizationDto.getTruckNumber());
      if (!StringUtils.isEmpty(lsUtilizationDto.getActucaltrucktype())) {
        getMtTrucktypeDetails(lsUtilizationDto.getActucaltrucktype(), lsUtilizationDto.getVariant1(), loadSlipUtilizationDto);
      } else {
        getMtTrucktypeDetails(lsUtilizationDto.getTrucktype(), lsUtilizationDto.getVariant1(), loadSlipUtilizationDto);
      }
      if (optionalMtTruck.isPresent() && optionalMtTruck.get().getPassingWeight() != null
          && optionalMtTruck.get().getPassingWeight() > 0) {
        loadSlipUtilizationDto.setTruckWeightCapacity(optionalMtTruck.get().getPassingWeight());
      }

      loadSlipUtilizationDto.setTruckIndentCategory(getIndentCategoryByTruckNumber(shipment.getTruckNumber(), applicationUser));

      loadSlipUtilizationDto.setVariant1(lsUtilizationDto.getVariant1());
      loadSlipUtilizationDto.setShipmentTotalTTEUtil(shipment.getTteUtil() != null ? shipment.getTteUtil() : 0);
      loadSlipUtilizationDto.setShipmentTotalWtUtil(shipment.getWeightUtil());
      loadSlipUtilizationDto.setShipmentTotalVolUtil(shipment.getVolumeUtil());
      loadSlipUtilizationDto.setTruckNumber(shipment.getTruckNumber());
      loadSlipUtilizationDto.setContainerNum(shipment.getContainerNum());
      loadSlipUtilizationDto.setDestLoc(destLoc);
      loadSlipUtilizationDto.setTruckType(lsUtilizationDto.getTrucktype());
      loadSlipUtilizationDto.setActualTruckType(lsUtilizationDto.getActucaltrucktype());
      loadSlipUtilizationDto.setServprov(shipment.getServprov());
      loadSlipUtilizationDto.setFreightAvailable(shipment.getFreightAvailability());
      return new ApiResponse(HttpStatus.OK, "", loadSlipUtilizationDto);
    } else if (!StringUtils.isEmpty(lsUtilizationDto.getShipmentId())) {
      Optional<Shipment> optionalShipment = shipmentRepository.findByShipmentId(lsUtilizationDto.getShipmentId());
      if (!optionalShipment.isPresent())
        return new ApiResponse(HttpStatus.NOT_FOUND, String.format("No shipment find with Id %s", lsUtilizationDto.getShipmentId()));
      Shipment shipment = optionalShipment.get();
      Optional<String> destLocOpt = loadslipRepository.findAllByShipmentShipmentId(shipment.getShipmentId()).stream().findFirst().map(t -> t.getDestLoc());
      String destLoc = destLocOpt.isPresent() ? destLocOpt.get() : null;
      Optional<MTTruck> optionalMtTruck = truckRepo.findOneByTruckNumberIgnoreCase(lsUtilizationDto.getTruckNumber());

      lsUtilizationDto.setTrucktype(shipment.getTruckType());
      lsUtilizationDto.setActucaltrucktype(shipment.getActualTruckType());
      lsUtilizationDto.setVariant1(shipment.getVariant1());

      if (!StringUtils.isEmpty(lsUtilizationDto.getActucaltrucktype()))
        getMtTrucktypeDetails(lsUtilizationDto.getActucaltrucktype(), lsUtilizationDto.getVariant1(), loadSlipUtilizationDto);
      else
        getMtTrucktypeDetails(lsUtilizationDto.getTrucktype(), lsUtilizationDto.getVariant1(), loadSlipUtilizationDto);
      if (optionalMtTruck.isPresent() && optionalMtTruck.get().getPassingWeight() != null
          && optionalMtTruck.get().getPassingWeight() > 0) {
        loadSlipUtilizationDto.setTruckWeightCapacity(optionalMtTruck.get().getPassingWeight());
      }

      loadSlipUtilizationDto.setTruckIndentCategory(getIndentCategoryByTruckNumber(shipment.getTruckNumber(), applicationUser));

      loadSlipUtilizationDto.setVariant1(shipment.getVariant1());
      loadSlipUtilizationDto.setShipmentTotalTTEUtil(shipment.getTteUtil() != null ? shipment.getTteUtil() : 0);
      loadSlipUtilizationDto.setShipmentTotalWtUtil(shipment.getWeightUtil());
      loadSlipUtilizationDto.setShipmentTotalVolUtil(shipment.getVolumeUtil());
      loadSlipUtilizationDto.setTruckNumber(shipment.getTruckNumber());
      loadSlipUtilizationDto.setContainerNum(shipment.getContainerNum());
      loadSlipUtilizationDto.setDestLoc(destLoc);
      loadSlipUtilizationDto.setTruckType(shipment.getTruckType());
      loadSlipUtilizationDto.setActualTruckType(shipment.getActualTruckType());
      loadSlipUtilizationDto.setServprov(shipment.getServprov());
      loadSlipUtilizationDto.setFreightAvailable(shipment.getFreightAvailability());

    }
    return new ApiResponse(HttpStatus.OK, "", loadSlipUtilizationDto);
  }

  private String getIndentCategoryByTruckNumber(String truckNumber, ApplicationUser applicationUser) {
    if (!StringUtils.isEmpty(truckNumber)) {
      List<String> statuses = TruckReportStatus.getInventoryTrucks().parallelStream().map(status -> status.name()).collect(Collectors.toList());
      String truckIndentCategory = truckReportRepo.findTruckIndentCatgory(truckNumber, statuses, applicationUser.getPlantCode());
      return truckIndentCategory;
    }
    return null;
  }

  private String findShipToLocFromTruck(LsUtilizationDto lsUtilizationDto, ApplicationUser applicationUser) {
    Optional<TruckReport> optionalTruckReport = truckReportRepo.findByTruckTruckNumberAndReportLocationAndStatus(lsUtilizationDto.getTruckNumber(), applicationUser.getPlantCode(), TruckReportStatus.GATED_IN);
    if (optionalTruckReport.isPresent()) {
      List<String> isAvailableInMTCustShiTo = mtCustomerRepositary.existsByShipToId(optionalTruckReport.get().getDestinationLocation());
      if (isAvailableInMTCustShiTo.size() > 0) {
        return optionalTruckReport.get().getDestinationLocation();
      }
    }
    return null;
  }

  private String getVariant1BySelectedTruck(LsUtilizationDto lsUtilizationDto, LoadSlipUtilizationDto loadSlipUtilizationDto, ApplicationUser applicationUser) {
    /*Getting variant1 value if there is only one record in freight table with source,destination,truck_type,servpro and current date is between effective and expiry date*/
      /*List<Freight> freightList = freightRepository.findAllBySourceLocAndDestLocAndServprovAndTruckTypeOrderByEffectiveDateDesc(lsUtilizationDto.getSouceLoc(),
          lsUtilizationDto.getDestLoc(), lsUtilizationDto.getTrasporter(), lsUtilizationDto.getTrucktype());*/
    MTTruckDedicated mtTruckDedicated = null;
    Optional<MTLocation> optionalMTLocation = locationRepository.findByLocationId(lsUtilizationDto.getSouceLoc());
    if (StringUtils.isEmpty(lsUtilizationDto.getVariant1())) {
      /*if varient1c is still empty then checking if truckNumber, source, destination, servPro and truckType is available in MT_TRUCK_DEDICATED
       * if available then set variant1 as DED*/
      mtTruckDedicated = mtTruckDedidatedRepository.findByEffectiveDateAndExpiryDate(lsUtilizationDto.getSouceLoc(),
          lsUtilizationDto.getDestLoc(), lsUtilizationDto.getTrasporter(), lsUtilizationDto.getTrucktype(), lsUtilizationDto.getTruckNumber(), DateUtils.setTimeToMidnight(new Date()));
      /*if (!StringUtils.isEmpty(mtTruckDedicated)) {
        return "DED"; // Default
      }*/
      if (mtTruckDedicated == null && optionalMTLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equals(optionalMTLocation.get().getLocationClass())) {
        /*Checking if source isEXT_WAREHOUSE if yes then serach MT_DEDICATED table with linkedPlantLoc,dest,truckYtpu,servpro,truckNumber*/
        mtTruckDedicated = mtTruckDedidatedRepository.findByEffectiveDateAndExpiryDate(optionalMTLocation.get().getLinkedPlant(),
            lsUtilizationDto.getDestLoc(), lsUtilizationDto.getTrasporter(), lsUtilizationDto.getTrucktype(), lsUtilizationDto.getTruckNumber(), DateUtils.setTimeToMidnight(new Date()));

      }

      if (mtTruckDedicated != null) {
        return "DED";
      } else {
        List<Freight> freightList = null;
        freightList = freightRepository.findAllBySourceAndDestAndServProvAndTruckTypeBetweenEffectiveAndExpiryDate(lsUtilizationDto.getSouceLoc(),
            lsUtilizationDto.getDestLoc(), lsUtilizationDto.getTrasporter(), lsUtilizationDto.getTrucktype(), DateUtils.setTimeToMidnight(new Date()));
        if (!freightList.isEmpty() && freightList.size() == 1) {
          /*If size of freight list is 1 then directly set veriant1 = condition 1*/
          return freightList.get(0).getCondition1();
        } else {
          /*if we dint get single freight value and source is EXT_WAREHOUSE then search for freight with linkedPlantLoc(1007),dest,servepro,truckType*/
          if (optionalMTLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equals(optionalMTLocation.get().getLocationClass())) {
            freightList = freightRepository.findAllBySourceAndDestAndServProvAndTruckTypeBetweenEffectiveAndExpiryDate(optionalMTLocation.get().getLinkedPlant(),
                lsUtilizationDto.getDestLoc(), lsUtilizationDto.getTrasporter(), lsUtilizationDto.getTrucktype(), DateUtils.setTimeToMidnight(new Date()));
            if (!freightList.isEmpty() && freightList.size() == 1) {
              return freightList.get(0).getCondition1();
            }
          }
        }
      }
    }
    return lsUtilizationDto.getVariant1();
  }

  private LoadSlipUtilizationDto getMtTrucktypeDetails(String truckType, String variant1, LoadSlipUtilizationDto loadSlipUtilizationDto) {
    Map<String, Object> resultMap = truckTypeRepo.findMTTruckTypeInfoByTypeAndVariant(truckType, variant1);
    if (resultMap.size() == 0)
      return loadSlipUtilizationDto;
    loadSlipUtilizationDto.setVariant1(resultMap.get("VARIANT1"));
    loadSlipUtilizationDto.setTruckWeightCapacity(resultMap.get("GROSS_WT"));
    loadSlipUtilizationDto.setTruckVolumeCapacity(resultMap.get("GROSS_VOL"));
    loadSlipUtilizationDto.setTteCapacity(resultMap.get("TTE_CAPACITY"));
    return loadSlipUtilizationDto;
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
    indentDetails.setTruckType(mtTruck.getTtId().getType());
    indentDetails.setActualTruckType(mtTruck.getTtId().getType());
    indentDetails.setVariant1(mtTruck.getVariant1());
    indentDetails.setTte(actualTruckTypeInfo.getTteCapacity());
    indentDetails.setDriverName(indentReportDto.getDriverName());
    indentDetails.setDriverMobile(indentReportDto.getDriverContact());
    indentDetails.setDriverLicense(indentReportDto.getDriverLicense());
    indentDetails.setGpsEnabled(mtTruck.getGpsEnabled());
    indentDetails.setGpsProvider(mtTruck.getGpsProvider() != null ? mtTruck.getGpsProvider().getGpsProvider() : null);
    indentDetails.setStatus(TruckReportStatus.REPORTED);
    indentDetails.setPassingWeight(mtTruck.getPassingWeight());
    indentDetails.setInsertUser(loggedInUser.getUserId());
    indentDetails.setInsertDate(new Date());
    return indentDetails;
  }

  @Override
  public ApiResponse getIndentTruckReportInfo(String indentId, ApplicationUser loggedInUser) {
    Optional<IndentSummary> optionalIndentSummary = indentSummaryRepository
        .findOneByIndentIdAndSourceLocation(indentId, loggedInUser.getPlantCode());
    if (!optionalIndentSummary.isPresent())
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("Indent not found with given id %s", indentId));
    IndentSummary indentSummary = optionalIndentSummary.get();
    return new ApiResponse(HttpStatus.OK, "", indentSummary.getIndentDetails().parallelStream()
        .map(TruckReportDto::new).collect(Collectors.toSet()));
  }

  private TruckReport createTruckReporting(IndentSummary indentSummary, IndentDetails indentDetails, ApplicationUser loggedInUser, MTTruck mtTruck) {
    TruckReport truckReport = new TruckReport();
    truckReport.setGateControlCode(indentSummaryRepository.findIndentSequence(Constants.INDENT_GATE_CONTROL_CODE_PREFIX,
        loggedInUser.getPlantCode(), Constants.INDENT_GATE_CONTROL_CODE_SUFFIX));
    truckReport.setIndentSummary(indentSummary);
    // need to change the below system type to RMS for RMS services
    truckReport.setType(SystemType.FGS);
    Map<String, Object> freightMap = utilityService.calculateTrasporterSapCode(indentSummary, indentDetails);

    truckReport.setTransporterSapCode(!StringUtils.isEmpty(freightMap.get("transporterSapCode")) ? freightMap.get("transporterSapCode").toString() : null);
    // Doing previously, now this logic is moved to save shipment freight calculation place
//    truckReport.setTtDays(!StringUtils.isEmpty(freightMap.get("ttDays")) ? Double.parseDouble(freightMap.get("ttDays").toString()) : null);
    truckReport.setTruck(mtTruck);
    truckReport.setDriverName(indentDetails.getDriverName());
    truckReport.setDriverMobile(indentDetails.getDriverMobile());
    truckReport.setDriverLicense(indentDetails.getDriverLicense());
    truckReport.setServprov(indentSummary.getTransporter());
    truckReport.setTruckType(indentDetails.getTruckType());
    truckReport.setReportLocation(loggedInUser.getPlantCode());
    truckReport.setSourceLocation(indentSummary.getSourceLocation());
    truckReport.setDestinationLocation(indentSummary.getDestinationLocation());
    truckReport.setReportDate(new Date());
    truckReport.setStatus(TruckReportStatus.REPORTED);
    truckReport.setWaitTimeHrs((Double) (DateUtils.diffBetweenDates(new Date(), truckReport.getReportDate()).doubleValue()));
    truckReport.setRejectedStatus(TruckReportStatus.NORMAL);
    truckReport.setReportedTruckType(indentDetails.getActualTruckType());
    truckReport.setInsertUser(loggedInUser.getUserId());
    truckReport.setInsertDate(new Date());
    truckReport.setDestCountry(indentSummary.getDestCountry());
    return truckReport;
  }


  @Override
  public ApiResponse getPlantReportedTruckInfo(TruckReportFilterDto truckReportFilterDto, ApplicationUser loggedInUser) {
    // removed null from trckReportsData initialization
    Page<TruckReport> truckReportsData = null;
    List<TruckReportStatus> truckReportStatuses = null;
    if (truckReportFilterDto.getType() != null) {
      if (truckReportFilterDto.getType().equalsIgnoreCase("INVENTORY")) {
        if (!StringUtils.isEmpty(truckReportFilterDto.getStatus())) {
          List<String> statuses = new ArrayList<>(Arrays.asList(truckReportFilterDto.getStatus().split(",")));
          truckReportStatuses = statuses.parallelStream().map(Constants.TruckReportStatus::valueOf).collect(Collectors.toList());
        } else {
          truckReportStatuses = Constants.TruckReportStatus.getFGSSecurityReportedStatuses();
        }
        if (!truckReportFilterDto.isReportedTrucksFilter()) {
          // fetch reported trucks for for gate security.
          truckReportsData = truckReportRepo.findAllByReportLocationAndStatusInOrderByReportDateAsc(
              loggedInUser.getPlantCode(), truckReportStatuses, PageRequest.of(truckReportFilterDto.getIndex(),
                  truckReportFilterDto.getPageLength(), Sort.Direction.DESC, "reportDate"));
        } else {
          truckReportsData = filterService.filterReportedTrucks(truckReportFilterDto, loggedInUser, truckReportStatuses, "INVENTORY_TRUCKS", null);
        }
      } else if (truckReportFilterDto.getType().equalsIgnoreCase("HISTORY")) {
        if (!StringUtils.isEmpty(truckReportFilterDto.getStatus())) {
          List<String> statuses = new ArrayList<>(Arrays.asList(truckReportFilterDto.getStatus().split(",")));
          truckReportStatuses = statuses.parallelStream().map(Constants.TruckReportStatus::valueOf).collect(Collectors.toList());
        } else {
          truckReportStatuses = Constants.TruckReportStatus.getTruckHistoryStatus();
        }
        if (!truckReportFilterDto.isReportedTrucksFilter()) {
          // fetch reported trucks for for gate security.
          truckReportsData = truckReportRepo.findAllByReportLocationAndStatusInOrderByReportDateAsc(
              loggedInUser.getPlantCode(), truckReportStatuses, PageRequest.of(truckReportFilterDto.getIndex(),
                  truckReportFilterDto.getPageLength(), Sort.Direction.DESC, "reportDate"));
        } else {
          truckReportsData = filterService.filterReportedTrucks(truckReportFilterDto, loggedInUser, truckReportStatuses, "INVENTORY_TRUCKS", null);
        }
      }
    }
    return new ApiResponse(HttpStatus.OK, "", new TruckReportFilterDto(truckReportsData, loggedInUser));
  }

  private ApiResponse reportGateIn(TruckReportDto truckReportDto, ApplicationUser loggedInUser) {
    // validate truck gate in report details
    String invalidInfo = truckReportDto.validateReportedTruck();
    if (!StringUtils.isEmpty(invalidInfo))
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("Please provide %s info", invalidInfo));
    // change the status in below query from REPORTED and check Bay assigned or not
    // once service is ready for Bay .
    Optional<TruckReport> optionalTruckReport = truckReportRepo.findOneByGateControlCodeAndIndentSummaryIndentIdAndStatus(truckReportDto.getGateControlCode(),
        truckReportDto.getIndentId(), TruckReportStatus.REPORTED);
    if (!optionalTruckReport.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("Bay assigned truck not found with indent id %s and Gate control code %s",
              truckReportDto.getIndentId(), truckReportDto.getGateControlCode()));
    }
    TruckReport truckReport = optionalTruckReport.get();
    truckReport.setStatus(TruckReportStatus.GATED_IN);
    truckReport.setGateInDate(new Date());
//    truckReport.setGateInDate(Utility.currentTimestamp());
    if (truckReportDto.getInWeight() != null) {
      truckReport.setInWeight(truckReportDto.getInWeight());
    }
    truckReport.setUpdateUser(loggedInUser.getUserId());
    truckReport.setUpdateDate(new Date());
    ApiResponse response = this.updateIndentDetails(truckReportDto.getIndentId(),
        truckReportDto.getGateControlCode(), TruckReportStatus.GATED_IN, loggedInUser);
    if (response.getStatusCode() == HttpStatus.NOT_FOUND.value())
      return response;
    TruckReportDto reportDto = new TruckReportDto(truckReportRepo.save(truckReport));
    reportDto.setDestDis(truckReportDto.getDestDis());
    reportDto.setDestCountryName(truckReportDto.getDestCountryName());
    return new ApiResponse(HttpStatus.OK, "", reportDto);
  }

  private ApiResponse updateIndentDetails(String indentId, String gateControlCode, TruckReportStatus status, ApplicationUser loggedInUser) {
    Optional<IndentDetails> optionalIndentDetails = indentDetailsRepo
        .findOneByIndentSummaryIndentIdAndGateControlCode(indentId, gateControlCode);
    if (!optionalIndentDetails.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("Indent details are not found for indent id %s", indentId));
    }
    IndentDetails indentDetails = optionalIndentDetails.get();
    indentDetails.setStatus(status);
    indentDetails.setUpdateUser(loggedInUser.getUserId());
    indentDetails.setUpdateDate(new Date());
    indentDetailsRepo.save(indentDetails);
    return new ApiResponse(HttpStatus.OK, "");
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
    List<Loadslip> loadslips = loadslipRepository.findAllByShipmentShipmentIdAndSourceLocAndStatusNot(truckReport.getShipmentID(), loggedInUser.getPlantCode(), LoadslipStatus.CANCELLED);
    if ((loadslips != null && loadslips.size() > 0) && !(BayStatus.RELEASE.equals(truckReport.getBayStatus()))) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Truck can not be gated out as it is not released at Loadslip ");
    }
    /*If truck is gated out with out assign-ls then he has to select a rejection code
     * and if truck is rejected by OPERATOR then no need to consider rejection code*/
    if (TruckReportStatus.GATED_IN.equals(truckReport.getStatus()) && !TruckReportStatus.REJECTED.equals(truckReport.getRejectedStatus())) {
      if (!StringUtils.isEmpty(truckReportDto.getRejectionCode())) {
        truckReport.setRejectionCode(truckReportDto.getRejectionCode());
      } else {
        return new ApiResponse(HttpStatus.BAD_REQUEST, String.format("Please provide rejection code for truck number %s", truckReport.getTruckNumber()));
      }
    }
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
    if (truckReport.getTTHrs() != null) {
      LocalDate localDate = LocalDate.now().plusDays((truckReport.getTTHrs().longValue()));
      java.util.Date date = java.sql.Date.valueOf(localDate);
      truckReport.setEtaDest(date);
    }
    truckReport = truckReportRepo.save(truckReport);
    /*As per Client requirment we sending data for every pickup*/
    /*boolean isFirstPickUP = shipmentStopRepository.existsByShipmentStopIdShipmentIdAndLocationIdAndAndActivity(truckReport.getShipmentID(), truckReport.getSourceLocation(), "P");
    if (isFirstPickUP) {
      try {
        opsService.sendActualShipment(truckReport.getShipmentID());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }*/
    if (!StringUtils.isEmpty(truckReport.getActivity()) && truckReport.getActivity().equals("P")) {
      try {
        opsService.sendActualShipment(truckReport.getShipmentID());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    /*Set Status as completed when Total available quantity = 0 & loaded_qty = 0 & reserved_qty = 0(When plan item used in other loadslips,
      the qty will be in loaded/reserved qty till the second LS is Confirmed)  & Status is OPEN & AppStatus is APPROVED or APPROVED-PART*/
    List<DispatchPlanItemInfo> planItemInfoList = new ArrayList<>();
    if (loadslips != null && loadslips.size() > 0) {
      for (Loadslip loadslip : loadslips) {
        if (loadslip.getLoadslipDetails() != null && loadslip.getLoadslipDetails().size() > 0) {
          for (LoadslipDetail loadslipDetail : loadslip.getLoadslipDetails()) {
            List<DispatchPlanItemInfo> dispatchPlanItemInfoList = fetchDispatchPlansByLoadslipItem(loadslipDetail, loggedInUser, loadslip.getDestLoc());
            if (dispatchPlanItemInfoList != null && dispatchPlanItemInfoList.size() > 0) {
              for (DispatchPlanItemInfo dispatchPlanItemInfo : dispatchPlanItemInfoList) {
                if (Status.OPEN.equals(dispatchPlanItemInfo.getStatus()) && dispatchPlanItemInfo.getTotalAvailableQuantity() == 0
                    && dispatchPlanItemInfo.getReservedQuantity() == 0 && dispatchPlanItemInfo.getLoadedQty() == 0
                    && (DispatchPlanItemAppStatus.APPROVED.equals(dispatchPlanItemInfo.getAppStatus()) || DispatchPlanItemAppStatus.APPROVED_PART.equals(dispatchPlanItemInfo.getAppStatus()))) {
                  dispatchPlanItemInfo.setStatus(Status.COMPLETED);
                  dispatchPlanItemInfo.setUpdateDate(new Date());
                  dispatchPlanItemInfo.setUpdateUser(loggedInUser.getUserId());
                  planItemInfoList.add(dispatchPlanItemInfo);
                }
              }
            }
          }
        }
      }
    }
    if (planItemInfoList != null && planItemInfoList.size() > 0) {
      dispatchPlanItemInfoRepository.saveAll(planItemInfoList);
      dispatchPlanItemInfoRepository.flush();
    }

    ApiResponse response = this.updateIndentDetails(truckReportDto.getIndentId(),
        truckReportDto.getGateControlCode(), TruckReportStatus.INTRANSIT, loggedInUser);
    if (response.getStatusCode() == HttpStatus.NOT_FOUND.value())
      return response;
    TruckReportDto reportDto = new TruckReportDto(truckReport);
    reportDto.setDestDis(truckReportDto.getDestDis());
    reportDto.setDestCountryName(truckReportDto.getDestCountryName());
    if (!StringUtils.isEmpty(reportDto.getRejectionCode())) {
      Optional<CTRejection> ctRejection = ctRejectionRepository.findById(reportDto.getRejectionCode());
      if (ctRejection.isPresent())
        reportDto.setRejectionDesc(ctRejection.get().getRejectionDesc());
    }
    return new ApiResponse(HttpStatus.OK, "", reportDto);
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
    truckReport.setBay(truckReportDto.getBayAssigned());
    truckReport.setUpdateUser(loggedInUser.getUserId());
    truckReport.setUpdateDate(new Date());
    TruckReportDto reportDto = new TruckReportDto(null, truckReportRepo.save(truckReport), loggedInUser);
    reportDto.setDestDis(truckReportDto.getDestDis());
    reportDto.setDestCountryName(truckReportDto.getDestCountryName());
    return new ApiResponse(HttpStatus.OK, "", reportDto);
  }

  @Override
  @Transactional(rollbackFor = SQLException.class)
  public ApiResponse saveLoadSlip(LoadslipDraftDto loadslipDraftDto, ApplicationUser loggedInUser) throws SQLException {
    if (loadslipDraftDto.getLoadslipDraftDataDtos().isEmpty() && !isExistingLoadslip(loadslipDraftDto)) {
      return createLoadslipJitExports(loadslipDraftDto, loggedInUser);
    }
    Loadslip loadslip = null;
    List<String> validationMsgs = validateLoadslipItems(loadslipDraftDto);
    if (!validationMsgs.isEmpty()) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "", new LoadSlipResponseDto(validationMsgs));
    }
    /*----------------------------------------------------------------------------------------------------------------------------------------------------*/
//    List<LoadslipDetail> loadslipDetailList = new ArrayList<>();
//    List<String> loadslipWithSource = new ArrayList<>();
//    List<String> actualSourceList = new ArrayList<>();
//    String actualSource = utilityService.getLinkedPlntLoc(loadslipDraftDto.getSource(), loadslipDraftDto.getDestination());
////      loadslipWithSource.add(actualSource);
//    if (!loadslipDraftDto.getSource().contentEquals(actualSource) || loadslipDraftDto.getSource().contentEquals("1007")) {
//      /*actualSourceList.add(actualSource);*/
//      /*when loadslipDraftDto.getSource() = TNR2/TNR4/TNR5/TNR6/TNR7 then actualSource will be 1007
//       * then all the linked ext_warehouse of 1007 and add to actual_source_list*/
//      List<String> sourceList = utilityService.getLinkedExtWareHouse(actualSource);
//      if (sourceList != null && !sourceList.isEmpty()) {
//        loadslipWithSource.addAll(sourceList);
//      }
//      actualSourceList = getLoadslipWithActualSourceList(loadslipWithSource);
//    }
//    actualSourceList.add(actualSource);
//    final List<String> LSDetailsSourceList =actualSourceList;
//    List<String> lsDetailsItemIdList = loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().map(LoadslipDraftDataDto::getItemId).collect(Collectors.toList());
//    List<String> lsDetailsItemDescList = loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().map(LoadslipDraftDataDto::getItemDesc).collect(Collectors.toList());
//    List<String> lsDetailsItemBatchCodeList = loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().map(LoadslipDraftDataDto::getBatchCode).collect(Collectors.toList());
//    if (loggedInUser.getRole().isRDCPlannerOrRDCGate()){
//      /*for (LoadslipDraftDataDto loadslipDraftDataDto : loadslipDraftDto.getLoadslipDraftDataDtos()) {
//        loadslipDetailList.addAll(loadslipDetailRepository.findAllByLoadslipDetailIdItemIdAndItemDescriptionAndLoadslipDestLocAndLoadslipSourceLocIn(
//            loadslipDraftDataDto.getItemId(), loadslipDraftDataDto.getItemDesc(), loadslipDraftDto.getDestination(), LSDetailsSourceList));
//      }*/
//      loadslipDetailList.addAll(loadslipDetailRepository.findAllByLoadslipDetailIdItemIdInAndItemDescriptionInAndLoadslipDestLocAndLoadslipSourceLocIn(
//          lsDetailsItemIdList, lsDetailsItemDescList, loadslipDraftDto.getDestination(), actualSourceList));
//    }else {
///*      for (LoadslipDraftDataDto loadslipDraftDataDto : loadslipDraftDto.getLoadslipDraftDataDtos()) {
//        loadslipDetailList.addAll(loadslipDetailRepository
//            .findAllByLoadslipDetailIdItemIdAndItemDescriptionAndBatchCodeAndLoadslipDestLocAndLoadslipSourceLocIn(
//                loadslipDraftDataDto.getItemId(), loadslipDraftDataDto.getItemDesc(),
//                loadslipDraftDataDto.getBatchCode(), loadslipDraftDto.getDestination(), LSDetailsSourceList));
//      }*/
//      loadslipDetailList.addAll(loadslipDetailRepository.findAllByLoadslipDetailIdItemIdInAndItemDescriptionInAndBatchCodeInAndLoadslipDestLocAndLoadslipSourceLocIn
//          (lsDetailsItemIdList, lsDetailsItemDescList, lsDetailsItemBatchCodeList, loadslipDraftDto.getDestination(), actualSourceList));
    //  }
    /*---------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

    List<String> errorMessages = new ArrayList<String>();
    if (isExistingLoadslip(loadslipDraftDto)) {
      Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(loadslipDraftDto.getLoadSlipId());
      String validationMsg = Utility.validateExistingLoadslip(optionalLoadslip);
      if (validationMsg != null) {
        return new ApiResponse(HttpStatus.NOT_FOUND, String.format(validationMsg + " with the loadslip Id %s", loadslipDraftDto.getLoadSlipId()));
      }
      if (!Constants.DelInvType.isExportOrJIT(optionalLoadslip.get().getLoadslipType())) {
        validateScannedQty(loadslipDraftDto, optionalLoadslip.get(), loggedInUser, errorMessages);
      }
      if (errorMessages.size() > 0) {
        return new ApiResponse(HttpStatus.METHOD_NOT_ALLOWED, "", new LoadSlipResponseDto(errorMessages));
      }
      loadslip = updateLoadslipData(optionalLoadslip.get(), errorMessages, loadslipDraftDto, loggedInUser);
    } else {
      loadslip = saveShipmentAndLoadslipData(errorMessages, loadslipDraftDto, loggedInUser);
    }
    LoadSlipResponseDto loadSlipResponseDto = null;
    if (loadslip == null) {
      // If any validation error occured, loadslip will become null so here we raise exception to rollback of dispatch_plan
      throw new SQLException(errorMessages.get(0));
     /* try {
        throw new SQLException();
      } catch (Exception e) {
        return new ApiResponse(HttpStatus.EXPECTATION_FAILED, "Failed some business rules", new LoadSlipResponseDto(errorMessages));
      }*/
    }

    /*adding details to del_inv_header
    * Earlier used to do after getting the response from getLoadslipDetails() service
    * It was not displaying the item category for newly added invoice in save-draft*/
    if (!loadslipDraftDto.getInvoiceList().isEmpty() || Constants.DelInvType.isExportOrJIT(loadslip.getLoadslipType())) {
      updateDelInvHeaderAndLine(loadslipDraftDto.getInvoiceList(), loadslip, loadslipDraftDto.getContainerNum(), loadslip.getShipment().getShipmentId());
    }
    /*Recalculating LS weight if LS is not of Export or JIT*/
    if (!Constants.DelInvType.isExportOrJIT(loadslip.getLoadslipType())){
      this.callLoadslipWeightRecalculatioinProcedure(loadslip.getLoadslipId());
      loadslip = loadslipRepository.findByLoadslipId(loadslip.getLoadslipId()).get();
    }
    //Fetch loadslip details for this loadslip and send back to UI in the response
    LoadslipDraftDto loadslipDetails = null;
    ApiResponse apiResponse = getLoadslipDetails(loadslip.getLoadslipId(), loggedInUser);
    if (apiResponse.getStatusCode() == 200) {
      loadslipDetails = (LoadslipDraftDto) apiResponse.getData();
    }
    if (loadslipDraftDto.getAction().equals("PRINT")) {
      loadSlipResponseDto = new LoadSlipResponseDto(loadslip.getLoadslipId(),
          loadslip.getShipment().getShipmentId(), opsService.getLoadslipPdf(loadslip).toByteArray(), loadslip.getLsprintDate() != null ?
          DateUtils.formatDate(new Date(loadslip.getLsprintDate().getTime()), Constants.DATE_TIME_FORMAT) : null, loadslip.getShipment().getVariant1(), loadslip.getShipment().getVariant2(),
          loadslip.getShipment().getFreightAvailability(),
          loadslip.getShipment().getActualTruckType()
          , new ArrayList(), loadslipDetails, loadslip.getLoadslipType());
     /* if (!loadslipDraftDto.getInvoiceList().isEmpty() || Constants.DelInvType.isExportOrJIT(loadslip.getLoadslipType())) {
        updateDelInvHeaderAndLine(loadslipDraftDto.getInvoiceList(), loadslip, loadslipDraftDto.getContainerNum(), loadslip.getShipment().getShipmentId());
      }*/
      return new ApiResponse(HttpStatus.OK, "Loadslip printed successfully", loadSlipResponseDto);
    }
    loadSlipResponseDto = new LoadSlipResponseDto(loadslip.getLoadslipId(), loadslip.getShipment().getShipmentId(),
        null, loadslip.getLsprintDate() != null ? DateUtils.formatDate(new Date(loadslip.getLsprintDate().getTime()), Constants.DATE_TIME_FORMAT) : null, loadslip.getShipment().getVariant1(), loadslip.getShipment().getVariant2(),
        loadslip.getShipment().getFreightAvailability(),
        loadslip.getShipment().getActualTruckType(),
        new ArrayList(), loadslipDetails, loadslip.getLoadslipType());
    //adding details to del_inv_header
   /* if (!loadslipDraftDto.getInvoiceList().isEmpty() || Constants.DelInvType.isExportOrJIT(loadslip.getLoadslipType())) {
      updateDelInvHeaderAndLine(loadslipDraftDto.getInvoiceList(), loadslip, loadslipDraftDto.getContainerNum(), loadslip.getShipment().getShipmentId());
    }*/
    return new ApiResponse(HttpStatus.OK, "Loadslip saved as Draft successfully", loadSlipResponseDto);
  }

  public void callLoadslipWeightRecalculatioinProcedure(String loadslipId){
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery("atl_business_flow_pkg.loadslip_wt_vol_cal");
    storedProcedure.registerStoredProcedureParameter("p_loadslip_id", String.class, ParameterMode.IN);
    storedProcedure.setParameter("p_loadslip_id", loadslipId);
    storedProcedure.execute();
  }

  private ApiResponse createLoadslipJitExports(LoadslipDraftDto loadslipDraftDto, ApplicationUser loggedInUser) {
    Loadslip loadslip = new Loadslip();
    loadslip.setLoadslipId(indentSummaryRepository.findIndentSequence(Constants.LOADSLIP_SEQ_PREFIX,
        loadslipDraftDto.getSource(), loadslipDraftDto.getDestination()));
    loadslip.setSourceLoc(loadslipDraftDto.getSource());
    loadslip.setDestLoc(loadslipDraftDto.getDestination());
    loadslip.setInsertUser(loggedInUser.getUserId());
    loadslip.setInsertDate(new Date());
    loadslip.setStatus(LoadslipStatus.CREATED);
    loadslip.setLoadslipType(loadslipDraftDto.getType().name());
    loadslip.setDropSeq(loadslipDraftDto.getDropSeq());

    /*Setting loadslip MKT_SEG with OrderTypeLookUp.Market_segment when OrderTypeLookup.orderType = loadslip.loadslipType*/
    String mktSeg = getLoadslipMktSegFromLSType(loadslip.getLoadslipType());
    if (!StringUtils.isEmpty(mktSeg))
      loadslip.setMarketSegment(mktSeg);

    Shipment shipment = null;
    Optional<TruckReport> optionalTruckReport = truckReportRepo.findByTruckTruckNumberAndReportLocationAndStatus(loadslipDraftDto.getTruckNumber(), loggedInUser.getPlantCode(), Constants.TruckReportStatus.GATED_IN);
    TruckReport truckReport = null;
    if (optionalTruckReport.isPresent()) {
      truckReport = optionalTruckReport.get();
    }
    shipment = saveShipment(truckReport, loadslipDraftDto, loggedInUser, shipment, loadslip);
    loadslip.setShipment(shipment);
    loadslip = loadslipRepository.save(loadslip);
    LoadSlipResponseDto loadSlipResponseDto = null;
    loadSlipResponseDto = new LoadSlipResponseDto(loadslip.getLoadslipId(), loadslip.getShipment().getShipmentId(),
        null, loadslip.getLsprintDate() != null ? DateUtils.formatDate(new Date(loadslip.getLsprintDate().getTime()), Constants.DATE_TIME_FORMAT) : null, loadslip.getShipment().getVariant1(), loadslip.getShipment().getVariant2(),
        loadslip.getShipment().getFreightAvailability(),
        loadslip.getShipment().getActualTruckType(),
        new ArrayList(), null, loadslip.getLoadslipType());
    return new ApiResponse(HttpStatus.OK, "Loadslip saved as Draft successfully", loadSlipResponseDto);
  }


  private List<String> validateScannedQty(LoadslipDraftDto loadslipDraftDto, Loadslip loadslip, ApplicationUser loggedInUser, List<String> errorMessages) {
    List<String> invoicesList = loadslipDraftDto.getInvoiceList();
    //For JIT & EXPORTS Loadslips, invoices will be there; if invoicesList is empty it is neither JIT nor EXPORT so dont consider validationos
    if (invoicesList.isEmpty()) {
      /*validation :- loadslip Item loaded Quantity should not be less then the scan Quantity of the Item*/
      if (loggedInUser.getRole().isRDCPlannerOrRDCGate()) {
        /*In RDC :- Not considering the batch code i.e., if the item is same then sum the loaded quantity of the item and compare with loaded Quantity*/
        Map<String, Integer> existingItemScanQtyMap = loadslip.getLoadslipDetails().stream().collect(Collectors.groupingBy(loadslipDetail -> loadslipDetail.getLoadslipDetailId().getItemId(), Collectors.summingInt(loadslipDetail -> loadslipDetail.getScannedQty() != null ? loadslipDetail.getScannedQty() : 0)));
        Map<String, Integer> draftItemScanQtyMap = loadslipDraftDto.getLoadslipDraftDataDtos().stream().collect(Collectors.groupingBy(LoadslipDraftDataDto::getItemId, Collectors.summingInt(LoadslipDraftDataDto::getLoadedQty)));
        draftItemScanQtyMap.entrySet().forEach(draftItem -> {
          existingItemScanQtyMap.entrySet().forEach(existingItem -> {
            if (draftItem.getKey().equals(existingItem.getKey())) {
              if (existingItem.getValue() > draftItem.getValue()) {
                errorMessages.add(String.format("Loaded Quantity of item %s  should not be less than scan quantity", draftItem.getKey()));
              }
            }
          });
        });

      } else {
        /*In FGS :- Considering itemId and batch code combination to check scanned Quantity is less the the loadded quantity*/
        Map<String, Map<String, Integer>> existingItemScanQtyMap = loadslip.getLoadslipDetails().stream().collect(Collectors.groupingBy(loadslipDetail -> loadslipDetail.getLoadslipDetailId().getItemId(), Collectors.groupingBy(LoadslipDetail::getBatchCode, Collectors.summingInt(loadslipDetail -> loadslipDetail.getScannedQty() != null ? loadslipDetail.getScannedQty() : 0))));
        Map<String, Map<String, Integer>> draftItemScanQtyMap = loadslipDraftDto.getLoadslipDraftDataDtos().stream().collect(Collectors.groupingBy(LoadslipDraftDataDto::getItemId, Collectors.groupingBy(LoadslipDraftDataDto::getBatchCode, Collectors.summingInt(LoadslipDraftDataDto::getLoadedQty))));
        draftItemScanQtyMap.entrySet().forEach(draftItem -> {
          existingItemScanQtyMap.entrySet().forEach(existingItem -> {
            if (draftItem.getKey().equals(existingItem.getKey())) {
              draftItem.getValue().entrySet().forEach(draftItemBatch -> {
                existingItem.getValue().entrySet().forEach(existingItemBatch -> {
                  if (draftItemBatch.getKey().equals(existingItemBatch.getKey())) {
                    if (existingItemBatch.getValue() > draftItemBatch.getValue()) {
                      errorMessages.add(String.format("Loaded Quantity of item %s and Batch Code %s  should not be less than scan quantity", draftItem.getKey(), draftItemBatch.getKey()));
                    }
                  }
                });
              });
            }
          });
        });
      }
    }
    return errorMessages;
  }

  private List<String> validateLoadslipItems(LoadslipDraftDto loadslipDraftDto) {
    List<String> errorMessages = new ArrayList<>();


    for (LoadslipDraftDataDto loadslipDraftDataDto : loadslipDraftDto.getLoadslipDraftDataDtos()) {
      String errorMessage = "";
      String prefixLabel = "For the item " + loadslipDraftDataDto.getItemId() + " having batch code of " + loadslipDraftDataDto.getBatchCode();
      String validationMsg = "";


      if (!StringUtils.isEmpty(loadslipDraftDataDto.getTubeSKU()) && !itemRepository.existsByIdAndClassification(loadslipDraftDataDto.getTubeSKU(), ItemClassification.TUBE.name())) {
        validationMsg = validationMsg.concat("TUBE SKU code ") + loadslipDraftDataDto.getTubeSKU();
      }
      if (!StringUtils.isEmpty(loadslipDraftDataDto.getFlapSKU()) && !itemRepository.existsByIdAndClassification(loadslipDraftDataDto.getFlapSKU(), ItemClassification.FLAP.name())) {
        validationMsg = (!validationMsg.isEmpty() ? validationMsg.concat(", ") : validationMsg).concat("FLAP SKU code ") + loadslipDraftDataDto.getFlapSKU();
      }
      if (!StringUtils.isEmpty(loadslipDraftDataDto.getValveSKU()) && !itemRepository.existsByIdAndClassification(loadslipDraftDataDto.getValveSKU(), ItemClassification.VALVE.name())) {
        validationMsg = (!validationMsg.isEmpty() ? validationMsg.concat(", ") : validationMsg).concat("VALVE SKU code ") + loadslipDraftDataDto.getValveSKU();
      }

      if (!StringUtils.isEmpty(loadslipDraftDataDto.getTubeBatch()) && !batchCodeRepository.existsByCodeAndCategory(loadslipDraftDataDto.getTubeBatch(), ItemCategory.Tube.name())) {
        validationMsg = (!validationMsg.isEmpty() ? validationMsg.concat(", ") : validationMsg).concat("TUBE Batch code ") + loadslipDraftDataDto.getTubeBatch();
      }
      if (!StringUtils.isEmpty(loadslipDraftDataDto.getFlapBatch()) && !batchCodeRepository.existsByCodeAndCategory(loadslipDraftDataDto.getFlapBatch(), ItemCategory.Flap.name())) {
        validationMsg = (!validationMsg.isEmpty() ? validationMsg.concat(", ") : validationMsg).concat("FLAP Batch code ") + loadslipDraftDataDto.getFlapBatch();
      }
      if (!StringUtils.isEmpty(loadslipDraftDataDto.getValveBatch()) && !batchCodeRepository.existsByCodeAndCategory(loadslipDraftDataDto.getValveBatch(), ItemCategory.Valve.name())) {
        validationMsg = (!validationMsg.isEmpty() ? validationMsg.concat(", ") : validationMsg).concat("VALVE Batch code ") + loadslipDraftDataDto.getValveBatch();
      }
      if (!StringUtils.isEmpty(loadslipDraftDataDto.getBatchCode()) && !batchCodeRepository.existsByCode(loadslipDraftDataDto.getBatchCode())) {
        validationMsg = (!validationMsg.isEmpty() ? validationMsg.concat(", ") : validationMsg).concat("Item Batch code ") + loadslipDraftDataDto.getBatchCode();
      }


      if (!StringUtils.isEmpty(validationMsg)) {
        String suffixLabel = " not found in master data";
        errorMessage += prefixLabel + "\n" + validationMsg + suffixLabel;
//        errorMessage.concat(prefixLabel).concat(validationMsg).concat(" not found in master data");
        errorMessages.add(errorMessage);
      }
    }
    return errorMessages;
  }

  private boolean existsByBatchCodeAndCategory(String batchCode, String name, List<MTBatchCode> mtBatchCodes) {
    return mtBatchCodes.parallelStream().anyMatch(b -> b.getCode().contentEquals(batchCode) && b.getCategory().equals(name));
  }

  private boolean existsItemCompByClassification(String itemSku, String name, List<MTItem> mtItems) {
    return mtItems.parallelStream().anyMatch(i -> i.getId().contentEquals(itemSku) && i.getClassification().equals(name));
  }

  private Loadslip updateLoadslipData(Loadslip existingLoadslip, List<String> errorMessages, LoadslipDraftDto loadslipDraftDto, ApplicationUser loggedInUser) {
    // All source locations from External warehouses or location = 1007
    List<String> actualSourceList = getSourceLocationsForExternalWarehouse(loadslipDraftDto);
    validatePlanQtyWithLSQty(loadslipDraftDto, loggedInUser, errorMessages, actualSourceList);
    if (!errorMessages.isEmpty()) {
      return null;
    }
    // Step1: Delete all Loadslip details and bom details from Loadslip_detail and loadslip_detail_bom and roll back dipatch plan qunatities
    //Step2: set required data to existing loadlsip
    //step3: create loadslip_detail and loadslip_detail_bom
    // step4 : update dispatch plan qunatities
    // step5: continue rest of the process

    //Delete loadslip detail and bom data & Reset or rollback dispatch plan quantities every time we update the loadslip
    List<DispatchPlanItemInfo> rolledBackPlans = new ArrayList<>();
    List<String> lsDetailsItemIdList = loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().map(LoadslipDraftDataDto::getItemId).collect(Collectors.toList());
    List<String> lsDetailsItemDescList = loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().map(LoadslipDraftDataDto::getItemDesc).collect(Collectors.toList());
    List<LoadslipDetail> details = new ArrayList<>();
    if (loggedInUser.getRole().isRDCPlannerOrRDCGate()) {
      details = loadslipDetailRepository
          .findAllByLoadslipDetailIdItemIdInAndItemDescriptionInAndLoadslipDestLocAndLoadslipSourceLocInAndLoadslipStatusNotInAndLoadslipLoadslipIdNot(
              lsDetailsItemIdList, lsDetailsItemDescList, loadslipDraftDto.getDestination(), actualSourceList, Constants.LoadslipStatus.getCompletedLoadslipStatuses(), existingLoadslip.getLoadslipId());

    } else {
      List<String> lsDetailsItemBatchCodeList = loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().map(LoadslipDraftDataDto::getBatchCode).collect(Collectors.toList());
      details = loadslipDetailRepository
          .findAllByLoadslipDetailIdItemIdInAndItemDescriptionInAndBatchCodeInAndLoadslipDestLocAndLoadslipSourceLocInAndLoadslipStatusNotInAndLoadslipLoadslipIdNot(
              lsDetailsItemIdList, lsDetailsItemDescList,
              lsDetailsItemBatchCodeList, loadslipDraftDto.getDestination(), actualSourceList, Constants.LoadslipStatus.getCompletedLoadslipStatuses(), existingLoadslip.getLoadslipId());
    }
    details.addAll(existingLoadslip.getLoadslipDetails());

//		for (LoadslipDetail loadslipDetail : existingLoadslip.getLoadslipDetails()) {
    for (LoadslipDetail loadslipDetail : details) {
      List<DispatchPlanItemInfo> dispatchPlanItemInfos = fetchDispatchPlansByLoadslipItem(loadslipDetail, loggedInUser, existingLoadslip.getDestLoc());
      rollBackDispatchPlan(existingLoadslip, dispatchPlanItemInfos, loadslipDetail, rolledBackPlans, existingLoadslip.getStatus().equals(LoadslipStatus.SENT_SAP));
    }
    if (!rolledBackPlans.isEmpty()) {
      dispatchPlanItemInfoRepository.saveAll(rolledBackPlans);
    }


    // Delete all Loadslip details and bom details from Loadslip_detail and loadslip_detail_bom for this loadslip
    List<LoadslipDetail> existingDetailList = loadslipDetailRepository.findAllByLoadslipDetailIdLoadslipId(existingLoadslip.getLoadslipId());
    loadslipDetailRepository.deleteAll(existingDetailList);
    existingLoadslip.getLoadslipDetails().clear();
    List<LoadslipDetailBom> existingBomList = loadslipDetailBomRepository.findAllByLoadslipDetailBomIdLoadslipId(existingLoadslip.getLoadslipId());
    loadslipDetailBomRepository.deleteAll(existingBomList);
    existingLoadslip.getLoadslipDetailBoms().clear();
//    existingLoadslip.setLoadslipDetails(new ArrayList<>());
//    existingLoadslip.setLoadslipDetailBoms(new ArrayList<>());

    // Re-create Loadslip details and Bom data
    // For Exports and JIT Loadslips
    List<String> invoiceList = loadslipDraftDto.getInvoiceList();
    if (invoiceList != null && !invoiceList.isEmpty()) {
      saveLoadslipDeatilAndBomExport(errorMessages, loadslipDraftDto, existingLoadslip, loggedInUser);
      //Update invoice date and invoice number null in case of update
     /* existingLoadslip.setSapInvoiceDate(null);
      existingLoadslip.setSapInvoice(null);*/
    }
    // For FGS Loadslips
    else {
      saveLoadslipDeatilAndBom(errorMessages, loadslipDraftDto, existingLoadslip, loggedInUser, "UPDATE", actualSourceList);
    }
    // If any validation error for loaded qunatity, return immediately with null
    if (!errorMessages.isEmpty()) {
      return null;
    }
//    loadslipRepository.saveAndFlush(existingLoadslip);

    //set loadslip data
    setLoadslipData(loadslipDraftDto, existingLoadslip, loggedInUser, "UPDATE");

    Shipment existingShipment = existingLoadslip.getShipment();

    // Now Fetching truck report using truck number and Shipment id which saved in truck_reporting when create loadslip
    Optional<TruckReport> optionalTruckReport = truckReportRepo.findByTruckTruckNumberAndShipmentIDAndReportLocation(existingShipment.getTruckNumber(), existingShipment.getShipmentId(), loggedInUser.getPlantCode());

    //IF USER SELECT NON TRUCK
    if (StringUtils.isEmpty(loadslipDraftDto.getTruckNumber())) {
      if (!StringUtils.isEmpty(existingShipment.getTruckNumber())) {
        resetShipmentData(existingShipment);
        resetTruckDetails(optionalTruckReport.get(), loggedInUser);
        existingLoadslip.setShipment(existingShipment);
        //elr details reverting
        existingLoadslip.setLrDate(null);
        existingLoadslip.setLrNum(null);
      }
      optionalTruckReport = Optional.empty();

      //IF USER SWITCHED FROM ONE TRUCK TO OTHER
    } else if (optionalTruckReport.isPresent() && !existingShipment.getTruckNumber().equals(loadslipDraftDto.getTruckNumber())) {
      Optional<TruckReport> newTruck = truckReportRepo.findByTruckTruckNumberAndReportLocationAndStatus(loadslipDraftDto.getTruckNumber(), loggedInUser.getPlantCode(), TruckReportStatus.GATED_IN);
      if (newTruck.isPresent()) {
        newTruck.get().setBayStatus(optionalTruckReport.get().getBayStatus());
        newTruck.get().setStatus(optionalTruckReport.get().getStatus());
        // Elr details set to null when servprov is changed.
        if (!newTruck.get().getServprov().equalsIgnoreCase(optionalTruckReport.get().getServprov())) {
          existingLoadslip.setLrDate(null);
          existingLoadslip.setLrNum(null);
        }
        resetTruckDetails(optionalTruckReport.get(), loggedInUser);
        optionalTruckReport = newTruck;
      } else {
        errorMessages.add(String.format("No truck found with truck number %s", loadslipDraftDto.getTruckNumber()));
      }
      //LOADSLIP CREATED WITH NON TRUCK AND SELECT NEW TRUCK
    } else if (!optionalTruckReport.isPresent() && !StringUtils.isEmpty(loadslipDraftDto.getTruckNumber())) {
      optionalTruckReport = truckReportRepo.findByTruckTruckNumberAndReportLocationAndStatus(loadslipDraftDto.getTruckNumber(), loggedInUser.getPlantCode(), Constants.TruckReportStatus.GATED_IN);
      if (!optionalTruckReport.isPresent()) {
        errorMessages.add(String.format("No truck found with truck number %s", loadslipDraftDto.getTruckNumber()));
      }
      if (existingLoadslip.getStatus().equals(LoadslipStatus.LOADING)) {
        optionalTruckReport.get().setBayStatus(BayStatus.LSPB);
        optionalTruckReport.get().setStatus(TruckReportStatus.LOADING);
      }
      if (existingLoadslip.getStatus().equals(LoadslipStatus.LOADED)) {
        optionalTruckReport.get().setBayStatus(BayStatus.LEPB);
        optionalTruckReport.get().setStatus(TruckReportStatus.LOADED);
      }
    }
    if (errorMessages.size() > 0) {
      return null;
    }
    TruckReport truckReport = null;
    if (optionalTruckReport.isPresent()) {
      truckReport = optionalTruckReport.get();
      if (!StringUtils.isEmpty(existingLoadslip.getArrivedDate()) && existingLoadslip.getStatus().equals(LoadslipStatus.CREATED)) {
        truckReport.setBayStatus(BayStatus.ARVD);
      }
      existingLoadslip.setShipment(saveShipment(truckReport, loadslipDraftDto, loggedInUser, existingShipment, existingLoadslip));
      existingLoadslip.setBay(optionalTruckReport.get().getBay());
    }

    existingLoadslip = loadslipRepository.save(existingLoadslip);
    //Save or Update shipment stop table
    /*executeShipmentStopProcedure(existingShipment.getShipmentId(), loggedInUser.getUserId());
    if (truckReport != null) {
      setTruckReportActivity(truckReport, existingLoadslip, loggedInUser);
    }*/
    return existingLoadslip;
  }

  private void resetTruckDetails(TruckReport truckReport, ApplicationUser loginUser) {
    truckReport.setUpdateUser(loginUser.getUserId());
    truckReport.setActivity(null);
    truckReport.setActualTruckType(null);
    truckReport.setUpdateDate(new Date());
    truckReport.setStatus(TruckReportStatus.GATED_IN);
    truckReport.setShipmentID(null);
    truckReport.setBayStatus(null);
    truckReportRepo.save(truckReport);
  }

  private void resetShipmentData(Shipment existingShipment) {
    existingShipment.setServprov(null);
    existingShipment.setTruckType(null);
    existingShipment.setTruckNumber(null);
    existingShipment.setActualTruckType(null);
    existingShipment.setVariant1(null);
    existingShipment.setVariant2(null);
    existingShipment.setIndentId(null);
    existingShipment.setDriverLicense(null);
    existingShipment.setDriverMobile(null);
    existingShipment.setDriverName(null);
    existingShipment.setFreightAvailability(null);
    existingShipment.setFreight(null);
    existingShipment.setFreightUom(null);
    existingShipment.setTteUtil(null);
    existingShipment.setWeightUtil(null);
    existingShipment.setVolumeUtil(null);
  }

  private void validatePlanQtyWithLSQty(LoadslipDraftDto loadslipDraftDto, ApplicationUser loggedInUser, List<String> errorMessages, List<String> actualSourceList) {
    List<String> invoiceList = loadslipDraftDto.getInvoiceList();
    // For Exports and JIT Loadslips, invoice list will be there and no plans here
    if (invoiceList.isEmpty()) {

      // External Warehouse case Logic
      String actualSource = utilityService.getLinkedPlntLoc(loadslipDraftDto.getSource(), loadslipDraftDto.getDestination());
      List<String> itemIds = loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream()
          .map(LoadslipDraftDataDto::getItemId).collect(Collectors.toList());
/*      List<DispatchPlanItemInfo> dispatchPlanItemInfos = dispatchPlanItemInfoRepository
          .findAllBySourceLocationAndDestinationLocationAndItemIdInAndStatusOrderByDispatchDateAsc(loadslipDraftDto.getSource(), loadslipDraftDto.getDestination(), itemIds);*/
      List<DispatchPlanItemInfo> dispatchPlanItemInfos = dispatchPlanItemInfoRepository
          .findAllBySourceLocationAndDestinationLocationAndItemIdInAndStatusOrderByDispatchDateAsc(actualSource, loadslipDraftDto.getDestination(), itemIds, Status.OPEN);
      //Iterate Loadslip Items
      List<LoadslipDraftDataDto> uniqueLSDraftDataList;
      if (loggedInUser.getRole().isRDCPlannerOrRDCGate()) {
        uniqueLSDraftDataList = loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().filter(Utility.distinctByKey(LoadslipDraftDataDto::getItemId)).collect(Collectors.toList());
      } else {
        uniqueLSDraftDataList = loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().filter(Utility.distinctByKey(lsd -> Arrays.asList(lsd.getItemId(), lsd.getBatchCode()))).collect(Collectors.toList());
      }

      for (LoadslipDraftDataDto loadslipDraftDataDto : uniqueLSDraftDataList) {
        int totalLoaddedQty = 0;
        List<LoadslipDetail> details;
        //Fetch all Loadslip Item Details which are having same item_id & source_loc & dest_loc & batch_code (in case of FGS)
        // Take sum of Loaded Qty from all Loadslip Items
        if (loggedInUser.getRole().isRDCPlannerOrRDCGate()) {
          /*details = loadslipDetailRepository
              .findAllByLoadslipDetailIdItemIdAndItemDescriptionAndLoadslipDestLocAndLoadslipSourceLoc(
                  loadslipDraftDataDto.getItemId(), loadslipDraftDataDto.getItemDesc(), loadslipDraftDto.getDestination(), loadslipDraftDto.getSource());*/
          details = loadslipDetailRepository
              .findAllByLoadslipDetailIdItemIdAndItemDescriptionAndLoadslipDestLocAndLoadslipSourceLocInAndLoadslipStatusNotIn(
                  loadslipDraftDataDto.getItemId(), loadslipDraftDataDto.getItemDesc(), loadslipDraftDto.getDestination(), actualSourceList, Constants.LoadslipStatus.getCompletedLoadslipStatuses());

          totalLoaddedQty += loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().filter(lsd -> lsd.getItemId().equals(loadslipDraftDataDto.getItemId())).mapToInt(LoadslipDraftDataDto::getLoadedQty).sum();
        } else {
          /*details = loadslipDetailRepository
              .findAllByLoadslipDetailIdItemIdAndItemDescriptionAndBatchCodeAndLoadslipDestLocAndLoadslipSourceLoc(
                  loadslipDraftDataDto.getItemId(), loadslipDraftDataDto.getItemDesc(),
                  loadslipDraftDataDto.getBatchCode(), loadslipDraftDto.getDestination(), loadslipDraftDto.getSource());*/
          details = loadslipDetailRepository
              .findAllByLoadslipDetailIdItemIdAndItemDescriptionAndBatchCodeAndLoadslipDestLocAndLoadslipSourceLocInAndLoadslipStatusNotIn(
                  loadslipDraftDataDto.getItemId(), loadslipDraftDataDto.getItemDesc(),
                  loadslipDraftDataDto.getBatchCode(), loadslipDraftDto.getDestination(), actualSourceList, Constants.LoadslipStatus.getCompletedLoadslipStatuses());

          totalLoaddedQty += loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().filter(lsd -> (lsd.getItemId().equals(loadslipDraftDataDto.getItemId()) && lsd.getBatchCode().equals(loadslipDraftDataDto.getBatchCode())))
              .mapToInt(LoadslipDraftDataDto::getLoadedQty).sum();
        }
        if (details != null && details.size() > 0) {
          List<String> loadslipIdList = details.parallelStream().map(loadslipDetail -> loadslipDetail.getLoadslipDetailId().getLoadslipId()).collect(Collectors.toList());
          List<Loadslip> loadslipList = loadslipRepository.findAllByLoadslipIdIn(loadslipIdList);
          for (LoadslipDetail dtl : details) {
            // If loadslip is not freezed i.e loadslip is not confirmed and released then only consider loadslip loadedQuantity into total Loaded Qty
            if (!isLoadslipFreezed(dtl, loadslipList)) {
              if (dtl.getLoadslipDetailId().getLoadslipId().equals(loadslipDraftDto.getLoadSlipId())) {
                /*if (loadslipDraftDataDto.getLineNumber() != dtl.getLoadslipDetailId().getLineNo()) {
                  totalLoaddedQty += dtl.getLoadQty();
                }*/
              } else {
                totalLoaddedQty += dtl.getLoadQty();
              }
            }
          }
        }
        int totalAvailableQty = 0, totalReservedQty = 0;
        for (DispatchPlanItemInfo dispatchPlanItemInfo : getDispatchPlanItemsByItem(dispatchPlanItemInfos, loadslipDraftDataDto, loggedInUser)) {
          totalAvailableQty += dispatchPlanItemInfo.getAvailableQuantity();
          totalReservedQty += dispatchPlanItemInfo.getReservedQuantity();
        }
        totalAvailableQty += totalReservedQty;
        // total available qty of all plans regarding an item is should be more than total loaded qty of this item
        if (loadslipDraftDto.getInvoiceList().isEmpty() && totalAvailableQty < totalLoaddedQty) {
          // Add validation here
          errorMessages.add("Loaded Qty should not be more than Available Quantity for the item \n " + loadslipDraftDataDto.getItemId() + " having Available Qty as "
              + (totalAvailableQty - totalReservedQty) + " & Reserved Qty as " + totalReservedQty);
        }
      }
    }
  }

  private Loadslip setLoadslipData(LoadslipDraftDto loadslipDraftDto, Loadslip loadslip, ApplicationUser loggedInUser, String action) {
    int totalLoadedQty = 0;
    for (LoadslipDraftDataDto loadslipDraftDataDto : loadslipDraftDto.getLoadslipDraftDataDtos()) {
      totalLoadedQty += loadslipDraftDataDto.getLoadedQty();
    }
    loadslip.setQty(totalLoadedQty);
    loadslip.setTte(loadslipDraftDto.getTotalTTE());
    loadslip.setWeight(loadslipDraftDto.getTotalWeight());
    loadslip.setVolume(loadslipDraftDto.getTotalVolume());
    loadslip.setGoApprovalReason(loadslipDraftDto.getGoApprovalReason());
    loadslip.setTotTubes(loadslipDraftDto.getTotalTubes());
    loadslip.setTotTyres(loadslipDraftDto.getTotalTyres());
    loadslip.setTotFlaps(loadslipDraftDto.getTotalFlaps());
    loadslip.setTotValve(loadslipDraftDto.getTotalValves());
    loadslip.setOtherQty(loadslipDraftDto.getOtherQty());
    // Truck TTE Utilization for this current truck
    loadslip.setTteUtil(loadslipDraftDto.getTteUtil());
    loadslip.setWeightUtil(loadslipDraftDto.getWeightUtil());
    loadslip.setVolumeUtil(loadslipDraftDto.getVolumeUtil());
    loadslip.setTotPctr(loadslipDraftDto.getTotPctr());
    //total quantity is sum of total tyres,tubes,flaps,valves,pctr
    loadslip.setTotQty(loadslipDraftDto.getTotalTubes() + loadslipDraftDto.getTotalTyres() + loadslipDraftDto.getTotalFlaps() + loadslipDraftDto.getTotalValves() + loadslipDraftDto.getTotPctr() + loadslipDraftDto.getOtherQty());
    loadslip.setWeightUom("KG");
    loadslip.setVolumeUom("CUMTR");

    loadslip.setShipTo(loadslipDraftDto.getShipTo());
    //TODO : Drop sequence to zero... Review by ramesh
    loadslip.setDropSeq(loadslipDraftDto.getDropSeq());
    /*if (loadslipDraftDto.getDropSeq() != 0) {
      loadslip.setDropSeq(loadslipDraftDto.getDropSeq());
    }*/
    if (loadslipDraftDto.getLoadSeq() != 0) {
      loadslip.setLoadSeq(loadslipDraftDto.getLoadSeq());
    }

    if (action.equals("UPDATE")) {
      loadslip.setUpdateDate(new Date());
      loadslip.setUpdateUser(loggedInUser.getUserId());
    } else {
      loadslip.setInsertUser(loggedInUser.getUserId());
      loadslip.setInsertDate(new Date());
    }

    // Existing Loadslip status is before Loadslip PRINTED status i.e (CREATED, PRINTED) else for statuses like LOADING, LOADED, SENT_SAP etc,,statuses will remain the same.
    if (loadslip.getStatus() == null || getLoadslipEventsIndex(loadslip.getStatus()) < 2) {
      if (loadslipDraftDto.getAction().equals("PRINT")) {
        loadslip.setStatus(LoadslipStatus.PRINTED);
      } else {
        loadslip.setStatus(Constants.LoadslipStatus.CREATED);
      }
    }
    if (loadslipDraftDto.getAction().equals("PRINT")) {
      loadslip.setLsprintDate(Utility.currentTimestamp());
    }

    return loadslip;
  }

  private String updateLoadSlipCategory(List<String> itemIds) {
    List<String> itemCategories = itemRepository.findDistinctItemCategoryByItemIdIn(itemIds);
    if (itemCategories != null && !itemCategories.isEmpty()) {
      return (itemCategories.size() == 1) ? itemCategories.get(0) : "MIX";
    }
    return null;
  }


  private boolean isExistingLoadslip(LoadslipDraftDto loadslipDraftDto) {
    return !StringUtils.isEmpty(loadslipDraftDto.getLoadSlipId());
  }

  private Loadslip saveShipmentAndLoadslipData(List<String> errorMessages, LoadslipDraftDto loadslipDraftDto, ApplicationUser loggedInUser) {
    // All source locations from External warehouses or location = 1007
    List<String> actualSourceList = getSourceLocationsForExternalWarehouse(loadslipDraftDto);
    validatePlanQtyWithLSQty(loadslipDraftDto, loggedInUser, errorMessages, actualSourceList);
    if (!errorMessages.isEmpty()) {
      return null;
    }
    return createLoadSlip(errorMessages, loadslipDraftDto, loggedInUser, actualSourceList);
  }

  private Loadslip createLoadSlip(List<String> errorMessages, LoadslipDraftDto loadslipDraftDto,
                                  ApplicationUser loggedInUser, List<String> actualSourceList) {
    // Save LOADSLIP data
    Loadslip loadslip = new Loadslip();
    loadslip.setLoadslipId(indentSummaryRepository.findIndentSequence(Constants.LOADSLIP_SEQ_PREFIX,
        loadslipDraftDto.getSource(), loadslipDraftDto.getDestination()));
    loadslip.setSourceLoc(loadslipDraftDto.getSource());
    loadslip.setDestLoc(loadslipDraftDto.getDestination());
    loadslip.setGoApprovalReason(loadslipDraftDto.getGoApprovalReason());
    // To generate LoadslipType,param3 is item_id (t will be any random Item ID from
    // respective Loadslip as per requirements)
    loadslip.setLoadslipType(loadslipRepository.findOrderType(loadslipDraftDto.getSource(),
        loadslipDraftDto.getDestination(), loadslipDraftDto.getLoadslipDraftDataDtos().get(0).getItemId()));
    if (!loadslipDraftDto.getInvoiceList().isEmpty()) {
      loadslip.setLoadslipType(loadslipDraftDto.getType().name());
    }

    /*Setting loadslip MKT_SEG with OrderTypeLookUp.Market_segment when OrderTypeLookup.orderType = loadslip.loadslipType*/
    String mktSeg = getLoadslipMktSegFromLSType(loadslip.getLoadslipType());
    if (!StringUtils.isEmpty(mktSeg))
      loadslip.setMarketSegment(mktSeg);

    //set loadslip data
    setLoadslipData(loadslipDraftDto, loadslip, loggedInUser, "SAVE");

    // For Exports and JIT Loadslips
    List<String> invoiceList = loadslipDraftDto.getInvoiceList();
    if (invoiceList != null && !invoiceList.isEmpty()) {
      saveLoadslipDeatilAndBomExport(errorMessages, loadslipDraftDto, loadslip, loggedInUser);
    }
    // For FGS Loadslips
    else {
      // Save LOADSLIP_DETAIL and LOADSLIP_DETAIL_BOM
      saveLoadslipDeatilAndBom(errorMessages, loadslipDraftDto, loadslip, loggedInUser, "SAVE", actualSourceList);
    }
    if (!errorMessages.isEmpty()) {
      return null;
    }
    //Shipment And Shipment_Stop
    return createShipmentAndShipmentSTop(loadslipDraftDto, loggedInUser, loadslip, errorMessages);
  }

  private String getLoadslipMktSegFromLSType(String loadslipType) {
    if (!StringUtils.isEmpty(loadslipType)) {
      Map<String, Object> orderTypeLookupMap = orderTypeLookupRepository.findMktSegByLSType(loadslipType);
      if (orderTypeLookupMap != null && !orderTypeLookupMap.isEmpty()) {
        return orderTypeLookupMap.get("mktSeg").toString();
      }
    }
    return null;
  }

  private Loadslip saveLoadslipDeatilAndBom(List<String> errorMessages, LoadslipDraftDto loadslipDraftDto,
                                            Loadslip loadslip, ApplicationUser loggedInUser, String action, List<String> actualSourceList) {
    if (loadslipDraftDto.getLoadslipDraftDataDtos() != null
        && !loadslipDraftDto.getLoadslipDraftDataDtos().isEmpty()) {
      List<LoadslipDetail> loadslipDetails = new ArrayList<LoadslipDetail>();
      List<LoadslipDetailBom> loadslipDetailBomData = new ArrayList<LoadslipDetailBom>();

      /*Creating a LS from EXT_WAREHOUSE to NOT EXT_WAREHOUSE
       * getting the LINKED_PLANT code from EXT_WAREHOUSE SOURCE*/
      String actualSource = utilityService.getLinkedPlntLoc(loggedInUser.getPlantCode(), loadslipDraftDto.getDestination());

      List<String> itemIds = loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream()
          .map(LoadslipDraftDataDto::getItemId).collect(Collectors.toList());
/*      List<DispatchPlanItemInfo> dispatchPlanItemInfos = dispatchPlanItemInfoRepository
          .findAllBySourceLocationAndDestinationLocationAndItemIdInAndStatusOrderByDispatchDateAsc(loadslipDraftDto.getSource(), loadslipDraftDto.getDestination(), itemIds);*/
      List<DispatchPlanItemInfo> dispatchPlanItemInfos = dispatchPlanItemInfoRepository
          .findAllBySourceLocationAndDestinationLocationAndItemIdInAndStatusOrderByDispatchDateAsc(actualSource, loadslipDraftDto.getDestination(), itemIds, Status.OPEN);

      // Laodslip item_category based on Items
      loadslip.setItemCategory(updateLoadSlipCategory(itemIds));

      List<DispatchPlanItemInfo> updatedPlanItemInfos = new ArrayList<DispatchPlanItemInfo>();
//      List<String> itemIdList = loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().map(LoadslipDraftDataDto::getItemId).distinct().collect(Collectors.toList());

      List<String> itemIdList = new ArrayList<>();
      for (LoadslipDraftDataDto draftDataDto : loadslipDraftDto.getLoadslipDraftDataDtos()) {
        if (!StringUtils.isEmpty(draftDataDto.getTubeSKU())) {
          itemIdList.add(draftDataDto.getTubeSKU());
        }
        if (!StringUtils.isEmpty(draftDataDto.getFlapSKU())) {
          itemIdList.add(draftDataDto.getFlapSKU());
        }
        if (!StringUtils.isEmpty(draftDataDto.getValveSKU())) {
          itemIdList.add(draftDataDto.getValveSKU());
        }
        if (!StringUtils.isEmpty(draftDataDto.getItemId())) {
          itemIdList.add(draftDataDto.getItemId());
        }
      }
      List<MTItem> mtItemList = itemRepository.findIdTteCategoryAndDescriptionByIdIn(itemIdList);
      List<String> itemCategoryList = new ArrayList<>();
      itemCategoryList.add("PCR");
      itemCategoryList.add("TBR");
      List<MTValve> mtValveList = valveRepository.findAllByItemCategoryIn(itemCategoryList);
      for (LoadslipDraftDataDto loadslipDraftDataDto : loadslipDraftDto.getLoadslipDraftDataDtos()) {
        //Update valve SKU code and Description for each item in loadslip if item_category is TBR or PCR , when valve sku and batch codes are empty from UI
//        if (StringUtils.isEmpty(loadslipDraftDataDto.getValveSKU()) || StringUtils.isEmpty(loadslipDraftDataDto.getValveBatch())) {
        updateValveSkuCodeAndDesc(loadslip, loadslipDraftDataDto, mtItemList, mtValveList);
        updateTubeDescAndFlapDesc(loadslip, loadslipDraftDataDto, mtItemList);
//        }
        addLoadSlipDetails(loadslip, loadslipDetails, loadslipDraftDataDto, loggedInUser, loadslipDraftDto, mtItemList);
        addLoadslipDetailBom(loadslip, loadslipDetailBomData, loadslipDraftDataDto, loggedInUser);
      }
      // RDC case
      if (loggedInUser.getRole().isRDCPlannerOrRDCGate()) {
        loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().filter(Utility.distinctByKey(LoadslipDraftDataDto::getItemId)).collect(Collectors.toList()).forEach(loadslipDraftDataDto -> {
          updateDispatchPlanQtsForLoadslip(errorMessages, dispatchPlanItemInfos, updatedPlanItemInfos,
              loadslipDraftDataDto, loadslipDraftDto, action, loggedInUser, actualSourceList);
        });
      } else {
        //FGS
        loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().filter(Utility.distinctByKey(lsd -> Arrays.asList(lsd.getItemId(), lsd.getBatchCode()))).collect(Collectors.toList()).forEach(loadslipDraftDataDto -> {
          updateDispatchPlanQtsForLoadslip(errorMessages, dispatchPlanItemInfos, updatedPlanItemInfos,
              loadslipDraftDataDto, loadslipDraftDto, action, loggedInUser, actualSourceList);
        });
      }
      if (!errorMessages.isEmpty()) {
        return null;
      }
      dispatchPlanItemInfoRepository.saveAll(updatedPlanItemInfos);
      loadslip.setLoadslipDetails(loadslipDetails);
      loadslip.setLoadslipDetailBoms(loadslipDetailBomData);
    }
    return loadslip;
  }

  private void updateValveSkuCodeAndDesc(Loadslip loadslip, LoadslipDraftDataDto loadslipDraftDataDto, List<MTItem> mtItemList, List<MTValve> mtValveList) {
    //Update valve SKU code and Description for each item in loadslip if item_category is TBR or PCR
    if (!StringUtils.isEmpty(loadslipDraftDataDto.getItemId())) {
      /*String itemCategory = itemRepository.findCategoryByItemID(loadslipDraftDataDto.getItemId());*/
      Optional<MTItem> optionalMTItem = mtItemList.parallelStream().filter(item -> item.getId().contentEquals(loadslipDraftDataDto.getItemId())).findFirst();
      if (optionalMTItem.isPresent() && (optionalMTItem.get().getCategory().equals("TBR") || optionalMTItem.get().getCategory().equals("PCR"))) {
//        Optional<MTValve> optionalMtValve = valveRepository.findByItemCategory(optionalMTItem.get().getCategory());
        Optional<MTValve> optionalMTValve = mtValveList.parallelStream().filter(mtValve -> mtValve.getItemCategory().contentEquals(optionalMTItem.get().getCategory())).findFirst();
        if (optionalMTValve.isPresent()) {
          if (!StringUtils.isEmpty(optionalMTValve.get().getItemId())) {
            loadslipDraftDataDto.setValveSKU(optionalMTValve.get().getItemId());
          }
          if (!StringUtils.isEmpty(optionalMTValve.get().getBatchCode())) {
            loadslipDraftDataDto.setValveBatch(optionalMTValve.get().getBatchCode());
          }
          if (!StringUtils.isEmpty(optionalMTValve.get().getItemDescription())) {
            loadslipDraftDataDto.setValveDesc(optionalMTValve.get().getItemDescription());
          }
        }
      }
    }
  }

  private void updateTubeDescAndFlapDesc(Loadslip loadslip, LoadslipDraftDataDto loadslipDraftDataDto, List<MTItem> mtItemList) {
    //update tube desc
    if (!StringUtils.isEmpty(loadslipDraftDataDto.getTubeSKU())) {
//      String tubeDesc = itemRepository.getItemDescriptionByItemId(loadslipDraftDataDto.getTubeSKU());
      Optional<MTItem> optionalMTItem = mtItemList.parallelStream().filter(mtItem -> mtItem.getId().contentEquals(loadslipDraftDataDto.getTubeSKU())).findFirst();
      loadslipDraftDataDto.setTubeDesc(optionalMTItem.get().getDescription());
      //update Flap desc
      if (!StringUtils.isEmpty(loadslipDraftDataDto.getFlapSKU())) {
//        String flapDesc = itemRepository.getItemDescriptionByItemId(loadslipDraftDataDto.getFlapSKU());
        Optional<MTItem> optionalMTItemFlap = mtItemList.parallelStream().filter(mtItem -> mtItem.getId().contentEquals(loadslipDraftDataDto.getFlapSKU())).findFirst();
        loadslipDraftDataDto.setFlapDesc(optionalMTItemFlap.get().getDescription());
      }
    }
  }

  private Loadslip saveLoadslipDeatilAndBomExport(List<String> errorMessages, LoadslipDraftDto loadslipDraftDto,
                                                  Loadslip loadslip, ApplicationUser loggedInUser) {
    if (loadslipDraftDto.getLoadslipDraftDataDtos() != null
        && !loadslipDraftDto.getLoadslipDraftDataDtos().isEmpty()) {
      List<LoadslipDetail> loadslipDetails = new ArrayList<LoadslipDetail>();
      List<LoadslipDetailBom> loadslipDetailBomData = new ArrayList<LoadslipDetailBom>();
      List<String> itemIds = loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream()
          .map(LoadslipDraftDataDto::getItemId).collect(Collectors.toList());
      for (LoadslipDraftDataDto loadslipDraftDataDto : loadslipDraftDto.getLoadslipDraftDataDtos()) {
        addLoadSlipDetailsExport(loadslip, loadslipDetails, loadslipDraftDataDto, loggedInUser);
        addLoadslipDetailBomExport(loadslip, loadslipDetailBomData, loadslipDraftDataDto, loggedInUser);
      }
      if (!errorMessages.isEmpty()) {
        return null;
      }
      loadslip.setItemCategory(updateLoadSlipCategory(itemIds));
      loadslip.setTteUtil(loadslipDraftDto.getTteUtil());
      loadslip.setLoadslipDetails(loadslipDetails);
      loadslip.setLoadslipDetailBoms(loadslipDetailBomData);
    }
    return loadslip;
  }


  private void updateDispatchPlanQtsForLoadslip(List<String> errorMessages,
                                                List<DispatchPlanItemInfo> dispatchPlanItemInfos, List<DispatchPlanItemInfo> updatedPlanItemInfos,
                                                LoadslipDraftDataDto loadslipDraftDataDto, LoadslipDraftDto loadslipDraftDto, String action, ApplicationUser loggedInUser, List<String> actualSourceList) {
    int qtyToBeLoaded = 0;
    int approvedQty = 0;
    int availableQty = 0;
    int reservedQty = 0;
    int totalLoaddedQty = 0;
    boolean isRemaingQty = true;

    List<LoadslipDetail> details;
   /* List<String> loadslipWithSource = new ArrayList<>();
    List<String> actualSourceList = new ArrayList<>();
    actualSourceList.add(loadslipDraftDto.getSource());
    String actualSource = getLinkedPlntLoc(loadslipDraftDto.getSource(), loadslipDraftDto.getDestination());
    if (!loadslipDraftDto.getSource().contentEquals(actualSource)) {
      *//*actualSourceList.add(actualSource);*//*
     *//*when loadslipDraftDto.getSource() = TNR2/TNR4/TNR5/TNR6/TNR7 then actualSource will be 1007
     * then all the linked ext_warehouse of 1007 and add to actual_source_list*//*
      List<String> sourceList = utilityService.getLinkedExtWareHouse(actualSource);
      if (sourceList != null && !sourceList.isEmpty()) {
        loadslipWithSource.addAll(sourceList);
      }
      actualSourceList = getLoadslipWithActualSourceList(loadslipWithSource);
      actualSourceList.add(actualSource);
    }
    if (loadslipDraftDto.getSource().contentEquals("1007")) {
      List<String> sourceList = utilityService.getLinkedExtWareHouse(actualSource);
      if (sourceList != null && !sourceList.isEmpty()) {
        loadslipWithSource.addAll(sourceList);
      }
      actualSourceList = getLoadslipWithActualSourceList(loadslipWithSource);
      actualSourceList.add(actualSource);
    }*/
    if (loggedInUser.getRole().isRDCPlannerOrRDCGate()) {
      /*details = loadslipDetailRepository
          .findAllByLoadslipDetailIdItemIdAndItemDescriptionAndLoadslipDestLocAndLoadslipSourceLoc(
              loadslipDraftDataDto.getItemId(), loadslipDraftDataDto.getItemDesc(), loadslipDraftDto.getDestination(), loadslipDraftDto.getSource());*/
      details = loadslipDetailRepository
          .findAllByLoadslipDetailIdItemIdAndItemDescriptionAndLoadslipDestLocAndLoadslipSourceLocInAndLoadslipStatusNotIn(
              loadslipDraftDataDto.getItemId(), loadslipDraftDataDto.getItemDesc(), loadslipDraftDto.getDestination(), actualSourceList, Constants.LoadslipStatus.getCompletedLoadslipStatuses());

      totalLoaddedQty += loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().filter(lsd -> lsd.getItemId().equals(loadslipDraftDataDto.getItemId())).mapToInt(LoadslipDraftDataDto::getLoadedQty).sum();
    } else {
      /*details = loadslipDetailRepository
          .findAllByLoadslipDetailIdItemIdAndItemDescriptionAndBatchCodeAndLoadslipDestLocAndLoadslipSourceLoc(
              loadslipDraftDataDto.getItemId(), loadslipDraftDataDto.getItemDesc(),
              loadslipDraftDataDto.getBatchCode(), loadslipDraftDto.getDestination(), loadslipDraftDto.getSource());*/
      details = loadslipDetailRepository
          .findAllByLoadslipDetailIdItemIdAndItemDescriptionAndBatchCodeAndLoadslipDestLocAndLoadslipSourceLocInAndLoadslipStatusNotIn(
              loadslipDraftDataDto.getItemId(), loadslipDraftDataDto.getItemDesc(),
              loadslipDraftDataDto.getBatchCode(), loadslipDraftDto.getDestination(), actualSourceList, Constants.LoadslipStatus.getCompletedLoadslipStatuses());

      totalLoaddedQty += loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().filter(lsd -> (lsd.getItemId().equals(loadslipDraftDataDto.getItemId()) && lsd.getBatchCode().equals(loadslipDraftDataDto.getBatchCode())))
          .mapToInt(LoadslipDraftDataDto::getLoadedQty).sum();
    }

    /*
     * if (!details.isEmpty()) { List<String> slipDetailIds =
     * details.parallelStream().filter(slipDetail ->
     * !slipDetail.getLoadslipDetailId().getLoadslipId().equals(loadslipDraftDto.
     * getLoadSlipId()) && !isLoadslipFreezed(slipDetail)) .map(slipDetail ->
     * slipDetail.getLoadslipDetailId().getLoadslipId()).collect(Collectors.toList()
     * ); if (slipDetailIds != null && slipDetailIds.size() > 0) {
     */
    // List<Object[]> loadedQuantities =
    // loadslipDetailRepository.getItemQty(loadslipDraftDataDto.getItemId(),
    // loadslipDraftDataDto.getItemDesc(), loadslipDraftDataDto.getBatchCode());
    if (details != null && details.size() > 0) {
      List<String> loadslipIdList = details.parallelStream().map(loadslipDetail -> loadslipDetail.getLoadslipDetailId().getLoadslipId()).collect(Collectors.toList());
      List<Loadslip> loadslipList = loadslipRepository.findAllByLoadslipIdIn(loadslipIdList);
      for (LoadslipDetail dtl : details) {
        // If loadslip is not freezed i.e loadslip is not confirmed and released then only consider loadslip loadedQuantity into total Loaded Qty
        if (!isLoadslipFreezed(dtl, loadslipList)) {
          if (dtl.getLoadslipDetailId().getLoadslipId().equals(loadslipDraftDto.getLoadSlipId())) {
            if (loadslipDraftDataDto.getLineNumber() != dtl.getLoadslipDetailId().getLineNo()) {
              totalLoaddedQty += dtl.getLoadQty();
            }
          } else {
            totalLoaddedQty += dtl.getLoadQty();
          }
        }
      }
    }
    // }
    // }


    int totalApprovedQty = 0, totalAvailableQty = 0, totalReservedQty = 0;
    for (DispatchPlanItemInfo dispatchPlanItemInfo : getDispatchPlanItemsByItem(dispatchPlanItemInfos, loadslipDraftDataDto, loggedInUser)) {
      totalAvailableQty += dispatchPlanItemInfo.getAvailableQuantity();
      totalReservedQty += dispatchPlanItemInfo.getReservedQuantity();

      // approvedQty after loadedQty column added
      int totalUtilizedQty = (dispatchPlanItemInfo.getLoadedQty() != null ? dispatchPlanItemInfo.getLoadedQty() : 0) + dispatchPlanItemInfo.getDispatchedQuantity();
      if (isRemaingQty) {
        approvedQty = dispatchPlanItemInfo.getApprovedQuantity();
        if (qtyToBeLoaded == 0) {
          // Total loaded qty is nothing but sum of loaded quantities from all Loadslips
          // in which source , dest, item_id and batch_Code are same as input source,
          // destination, item_id and batch_code
          // here this sum is equal to either sum of reserved quantities or sum of
          // dispatched quantities
          qtyToBeLoaded = (/*loadslipDraftDataDto.getLoadedQty() +*/ totalLoaddedQty);
        }
        approvedQty = approvedQty - dispatchPlanItemInfo.getDeletedQuantity() - totalUtilizedQty;
        totalApprovedQty += approvedQty;

        if (approvedQty >= qtyToBeLoaded) {
          availableQty = approvedQty - qtyToBeLoaded;
          reservedQty = qtyToBeLoaded;
          isRemaingQty = false;
        } else {
          qtyToBeLoaded = qtyToBeLoaded - approvedQty;
          availableQty = 0;
          reservedQty = approvedQty;
        }

        dispatchPlanItemInfo.setAvailableQuantity(availableQty);
        dispatchPlanItemInfo.setReservedQuantity(reservedQty);
        dispatchPlanItemInfo.setTotalAvailableQuantity(dispatchPlanItemInfo.getAvailableQuantity() + dispatchPlanItemInfo.getUnapprovedQuantity());
        updatedPlanItemInfos.add(dispatchPlanItemInfo);
        if (!isRemaingQty) {
          break;
        }
      }
      // Currently There is No direct update , delete all loadslips and roll back plan qty then again save loadslips and update plan qty
      else if (action.equalsIgnoreCase("UPDATE")) {
        // In update scenario, if total loadedQty(total loadedQty = sum of all loadslips
        // loadQty) is completely updated in one of the plans of an item then we are
        // resetting remaining plans of same item to 0 (loadedQty and reservedQty as 0)
        qtyToBeLoaded = 0;
        reservedQty = 0;
        availableQty = dispatchPlanItemInfo.getApprovedQuantity();

        dispatchPlanItemInfo.setAvailableQuantity(availableQty);
        dispatchPlanItemInfo.setReservedQuantity(reservedQty);
        dispatchPlanItemInfo.setTotalAvailableQuantity(dispatchPlanItemInfo.getAvailableQuantity() + dispatchPlanItemInfo.getUnapprovedQuantity());
        updatedPlanItemInfos.add(dispatchPlanItemInfo);
      }
    }
 /*   if (loadslipDraftDto.getLoadSlipId() != null) {
      // Create Laodslip case
      totalAvailableQty += totalReservedQty;
    }
    // total available qty of all plans regarding an item is should be more than total loaded qty of this item
    if (loadslipDraftDto.getInvoiceList().isEmpty() && totalAvailableQty < loadslipDraftDataDto.getLoadedQty()) {
      // Add validation here
      errorMessages.add("Loaded Qty should not be more than Available Quantity for the item \n " + loadslipDraftDataDto.getItemId() + " having Available Qty as "
          + totalAvailableQty + " & Reserved Qty as " + totalReservedQty);
    }*/
  }

  private List<String> getLoadslipWithActualSourceList(List<String> actualSourceList) {

    List<String> sourceToSearchList = new ArrayList<>();
    List<Map<String, Object>> loadslipList = loadslipRepository.findAllBySourceLocIn(actualSourceList,
        Constants.LoadslipStatus.getCompletedLoadslipStatuses().parallelStream().map(item -> item.name()).collect(Collectors.toList()));
    List<String> loadslipDestinations = loadslipList.parallelStream().map(loadslip -> loadslip.get("destLoc").toString()).distinct().collect(Collectors.toList());
    if (loadslipDestinations != null && !loadslipDestinations.isEmpty()) {
      actualSourceList.addAll(loadslipDestinations);
    }
    if (loadslipList.size() > 0) {
//      actualSourceList.add("1007"); // Default plant Loc for warehouses
      //List<MTLocation> mtLocationList = locationRepository.findAll();
      List<MTLocation> mtLocationList = locationRepository.findAllByIdIn(actualSourceList);

      for (Map<String, Object> loadslip : loadslipList) {
//        String linkedPlant = getLinkedPlntLoc(loadslip.get("sourceLoc").toString(), loadslip.get("destLoc").toString());
        String linkedPlant = getLinkedPlntLocFromMasterLoc(loadslip.get("sourceLoc").toString(), loadslip.get("destLoc").toString(), mtLocationList);
        if (linkedPlant.contentEquals("1007") && !loadslip.get("sourceLoc").toString().contentEquals("1007")) {
          sourceToSearchList.add(loadslip.get("sourceLoc").toString());
        }
      }
    }
    return sourceToSearchList.parallelStream().distinct().collect(Collectors.toList());

  }

  private String getLinkedPlntLocFromMasterLoc(String source, String destination, List<MTLocation> mtLocationList) {
    /*Considering Only plant 1007*/
    if (mtLocationList != null && !mtLocationList.isEmpty()) {
      if (!StringUtils.isEmpty(source)) {
        Optional<MTLocation> optionalMTLocation = mtLocationList.parallelStream().filter(mtLocation -> mtLocation.getId().contentEquals(source)).findFirst();
        if (optionalMTLocation.isPresent()) {
          if (!StringUtils.isEmpty(optionalMTLocation.get().getLocationClass()) && !StringUtils.isEmpty(optionalMTLocation.get().getLinkedPlant())
              && optionalMTLocation.get().getLocationClass().equalsIgnoreCase("EXT_WAREHOUSE") && optionalMTLocation.get().getLinkedPlant().equalsIgnoreCase("1007")) {
            /*Destinaton should be other 1007 && Location_Class = null OR NOT EXT_WAREHOUSE*/
            if (!StringUtils.isEmpty(destination)) {
              Optional<MTLocation> optionalMTLocationDest = mtLocationList.parallelStream().filter(mtLocation -> mtLocation.getId().contentEquals(destination)).findFirst();
              if (optionalMTLocationDest.isPresent() && !optionalMTLocationDest.get().getId().contentEquals("1007") &&
                  (!LocationType.EXT_WAREHOUSE.name().equalsIgnoreCase(optionalMTLocationDest.get().getLocationClass()))) {
                return optionalMTLocation.get().getLinkedPlant();
              }
            }
          }
        }
      }
    }
    return source;

  }

  private List<DispatchPlanItemInfo> getDispatchPlanItemsByItem(List<DispatchPlanItemInfo> dispatchPlanItemInfos, LoadslipDraftDataDto loadslipDraftDataDto, ApplicationUser loggedInUser) {
    List<DispatchPlanItemInfo> planItems;
    // In case of RDC Loadslips, don't consider the item bach code check,coz of split functionality in loadslip
    if (loggedInUser.getRole().isRDCPlannerOrRDCGate()) {
      planItems = dispatchPlanItemInfos.parallelStream().filter(obj -> obj.getItemId().equals(loadslipDraftDataDto.getItemId())).collect(Collectors.toList());
    } else {
      // Other than RDC Loadslips
      planItems = dispatchPlanItemInfos.parallelStream().filter(obj -> obj.getItemId().equals(loadslipDraftDataDto.getItemId())
          && obj.getBatchCode().equals(loadslipDraftDataDto.getBatchCode())).collect(Collectors.toList());
    }
    return planItems;
  }

  private boolean isLoadslipFreezed(LoadslipDetail slipDetail, List<Loadslip> loadslipList) {
//    Optional<Loadslip> loadslip = loadslipRepository.findById(slipDetail.getLoadslipDetailId().getLoadslipId());
    Optional<Loadslip> loadslip = loadslipList.parallelStream().filter(ls -> ls.getLoadslipId().contentEquals(slipDetail.getLoadslipDetailId().getLoadslipId())).findFirst();
    String validationError = Utility.validateExistingLoadslip(loadslip);
    if (validationError == null) {
      // Loadslip FREEZ POINT is CONFIRM EVENT its INDEX is 4 (LOADSLIP_FREEZ_INDEX), so once loadslip is reached its freezing point we cant consider that loadslip to update quantities
      return getLoadslipEventsIndex(loadslip.get().getStatus()) >= LOADSLIP_FREEZ_INDEX;
    }
    return true;
  }

  private void addLoadslipDetailBom(Loadslip loadslip, List<LoadslipDetailBom> loadslipDetailBomData,
                                    LoadslipDraftDataDto loadslipDraftDataDto, ApplicationUser loggedInUser) {
    LoadslipDetailBom loadslipDetailBom = new LoadslipDetailBom();
    loadslipDetailBom.setLoadslip(loadslip);
    LoadslipDetailBomId loadslipDetailBomId = new LoadslipDetailBomId(loadslip.getLoadslipId(),
        loadslipDraftDataDto.getLineNumber(), loadslipDraftDataDto.getItemId());
    loadslipDetailBom.setLoadslipDetailBomId(loadslipDetailBomId);
    loadslipDetailBom
        .setTubeSku(loadslipDraftDataDto.getTubeSKU() != null ? loadslipDraftDataDto.getTubeSKU() : "");
    loadslipDetailBom
        .setTubeBatch(loadslipDraftDataDto.getTubeBatch() != null ? loadslipDraftDataDto.getTubeBatch() : "");
    loadslipDetailBom.setTubeQty(loadslipDraftDataDto.getTubeQty());
    loadslipDetailBom
        .setFlapSku(loadslipDraftDataDto.getFlapSKU() != null ? loadslipDraftDataDto.getFlapSKU() : "");
    loadslipDetailBom
        .setFlapBatch(loadslipDraftDataDto.getFlapBatch() != null ? loadslipDraftDataDto.getFlapBatch() : "");
    loadslipDetailBom.setFlapQty(loadslipDraftDataDto.getFalpQty());
    loadslipDetailBom
        .setValveSku(loadslipDraftDataDto.getValveSKU() != null ? loadslipDraftDataDto.getValveSKU() : "");
    loadslipDetailBom.setValveBatch(
        loadslipDraftDataDto.getValveBatch() != null ? loadslipDraftDataDto.getValveBatch() : "");
    loadslipDetailBom.setValveQty(loadslipDraftDataDto.getValveQty());

    loadslipDetailBom.setInsertUser(loggedInUser.getUserId());
    loadslipDetailBom.setInsertDate(new Date());
    loadslipDetailBom.setUpdateDate(new Date());
    loadslipDetailBom.setUpdateUser(loggedInUser.getUserId());
    loadslipDetailBom.setTubeDesc(loadslipDraftDataDto.getTubeDesc());
    loadslipDetailBom.setFlapDesc(loadslipDraftDataDto.getFlapDesc());
    loadslipDetailBom.setValveDesc(loadslipDraftDataDto.getValveDesc());
    loadslipDetailBom.setPctr(loadslipDraftDataDto.getPctr());

    loadslipDetailBomData.add(loadslipDetailBom);
  }

  private void addLoadSlipDetails(Loadslip loadslip, List<LoadslipDetail> loadslipDetails,
                                  LoadslipDraftDataDto loadslipDraftDataDto, ApplicationUser loggedInUser, LoadslipDraftDto loadslipDraftDto, List<MTItem> mtItemList) {
    LoadslipDetail loadslipDetail = new LoadslipDetail();
    loadslipDetail.setLoadslip(loadslip);
    LoadslipDetailId loadslipDetailsId = new LoadslipDetailId(loadslip.getLoadslipId(),
        loadslipDraftDataDto.getLineNumber(), loadslipDraftDataDto.getItemId());
    loadslipDetail.setLoadslipDetailId(loadslipDetailsId);
    loadslipDetail.setItemDescription(loadslipDraftDataDto.getItemDesc());
    loadslipDetail.setBatchCode(loadslipDraftDataDto.getBatchCode());
    loadslipDetail.setLoadQty(loadslipDraftDataDto.getLoadedQty());
    loadslipDetail.setGrossWt(loadslipDraftDataDto.getGrossWt());
    loadslipDetail.setGrossVol(loadslipDraftDataDto.getGrossVol());
    loadslipDetail.setTte(loadslipDraftDataDto.getTte());
    loadslipDetail.setInvoiceNumber(loadslipDraftDataDto.getInvoiceNumber());
    loadslipDetail.setScannedQty(loadslipDraftDataDto.getScannedQty());
    loadslipDetail.setItemCategory(loadslipDraftDataDto.getItemCategory() != null ? loadslipDraftDataDto.getItemCategory().toString() : "");

//    Optional<MTItem> optionalMTItem = itemRepository.findById(loadslipDraftDataDto.getItemId());
    Optional<MTItem> optionalMTItem = mtItemList.parallelStream().filter(mtItem -> mtItem.getId().contentEquals(loadslipDraftDataDto.getItemId())).findFirst();
    if (optionalMTItem.isPresent()) {
      String scannable = loadslipDetailRepository.isLoadslipItemScannable(optionalMTItem.get().getCategory(),
          loadslipDraftDto.getSource());
      loadslipDetail.setScannable(!StringUtils.isEmpty(scannable) ? scannable : "N");
    }
    loadslipDetail.setIsSplit(!StringUtils.isEmpty(loadslipDraftDataDto.getIsSplit()) ? loadslipDraftDataDto.getIsSplit() : "N");
    loadslipDetail.setInsertUser(loggedInUser.getUserId());
    loadslipDetail.setInsertDate(new Date());
    loadslipDetail.setUpdateUser(loggedInUser.getUserId());
    loadslipDetail.setUpdateDate(new Date());

    loadslipDetails.add(loadslipDetail);
  }

  private void addLoadSlipDetailsExport(Loadslip loadslip, List<LoadslipDetail> loadslipDetails,
                                        LoadslipDraftDataDto loadslipDraftDataDto, ApplicationUser loggedInUser) {
    LoadslipDetail loadslipDetail = new LoadslipDetail();
    loadslipDetail.setLoadslip(loadslip);
    LoadslipDetailId loadslipDetailsId = new LoadslipDetailId(loadslip.getLoadslipId(),
        loadslipDraftDataDto.getLineNumber(), loadslipDraftDataDto.getItemId());
    loadslipDetail.setLoadslipDetailId(loadslipDetailsId);
    loadslipDetail.setItemDescription(loadslipDraftDataDto.getItemDesc());
    loadslipDetail.setBatchCode(loadslipDraftDataDto.getBatchCode());
    loadslipDetail.setLoadQty(loadslipDraftDataDto.getLoadedQty());
    loadslipDetail.setGrossWt(loadslipDraftDataDto.getGrossWt());
    loadslipDetail.setGrossVol(loadslipDraftDataDto.getGrossVol());
    loadslipDetail.setTte(loadslipDraftDataDto.getTte());
    loadslipDetail.setInvoiceNumber(loadslipDraftDataDto.getInvoiceNumber());
    loadslipDetail.setScannedQty(loadslipDraftDataDto.getScannedQty());

    Optional<MTItem> optionalMTItem = itemRepository.findById(loadslipDraftDataDto.getItemId());
    if (optionalMTItem.isPresent()) {
      String scannable = loadslipDetailRepository.isLoadslipItemScannable(optionalMTItem.get().getCategory(),
          loggedInUser.getPlantCode());
      loadslipDetail.setScannable(!StringUtils.isEmpty(scannable) ? scannable : "N");
    }
    loadslipDetail.setIsSplit(!StringUtils.isEmpty(loadslipDraftDataDto.getIsSplit()) ? loadslipDraftDataDto.getIsSplit() : "N");
    loadslipDetail.setInsertUser(loggedInUser.getUserId());
    loadslipDetail.setInsertDate(new Date());
   /* loadslipDetail.setUpdateUser(loggedInUser.getUserId());
    loadslipDetail.setUpdateDate(new Date());*/

    loadslipDetails.add(loadslipDetail);

  }

  private void addLoadslipDetailBomExport(Loadslip loadslip, List<LoadslipDetailBom> loadslipDetailBomData,
                                          LoadslipDraftDataDto loadslipDraftDataDto, ApplicationUser loggedInUser) {
    LoadslipDetailBom loadslipDetailBom = new LoadslipDetailBom();
    loadslipDetailBom.setLoadslip(loadslip);
    LoadslipDetailBomId loadslipDetailBomId = new LoadslipDetailBomId(loadslip.getLoadslipId(),
        loadslipDraftDataDto.getLineNumber(), loadslipDraftDataDto.getItemId());
    loadslipDetailBom.setLoadslipDetailBomId(loadslipDetailBomId);
    loadslipDetailBom
        .setTubeSku(loadslipDraftDataDto.getTubeSKU() != null ? loadslipDraftDataDto.getTubeSKU() : "");
    loadslipDetailBom
        .setTubeBatch(loadslipDraftDataDto.getTubeBatch() != null ? loadslipDraftDataDto.getTubeBatch() : "");
    loadslipDetailBom.setTubeQty(loadslipDraftDataDto.getTubeQty());
    loadslipDetailBom
        .setFlapSku(loadslipDraftDataDto.getFlapSKU() != null ? loadslipDraftDataDto.getFlapSKU() : "");
    loadslipDetailBom
        .setFlapBatch(loadslipDraftDataDto.getFlapBatch() != null ? loadslipDraftDataDto.getFlapBatch() : "");
    loadslipDetailBom.setFlapQty(loadslipDraftDataDto.getFalpQty());
    loadslipDetailBom
        .setValveSku(loadslipDraftDataDto.getValveSKU() != null ? loadslipDraftDataDto.getValveSKU() : "");
    loadslipDetailBom.setValveBatch(
        loadslipDraftDataDto.getValveBatch() != null ? loadslipDraftDataDto.getValveBatch() : "");
    loadslipDetailBom.setValveQty(loadslipDraftDataDto.getValveQty());

    loadslipDetailBom.setInsertUser(loggedInUser.getUserId());
//    loadslipDetailBom.setUpdateUser(loggedInUser.getUserId());
    loadslipDetailBom.setInsertDate(new Date());
//    loadslipDetailBom.setUpdateDate(new Date());
    loadslipDetailBom.setTubeDesc(loadslipDraftDataDto.getTubeDesc());
    loadslipDetailBom.setFlapDesc(loadslipDraftDataDto.getFlapDesc());
    loadslipDetailBom.setValveDesc(loadslipDraftDataDto.getValveDesc());

    //TODO chekc if works for all over
//    loadslipDetailBomRepository.save(loadslipDetailBom);
    //TODO Complete

    loadslipDetailBomData.add(loadslipDetailBom);
  }


  private Loadslip createShipmentAndShipmentSTop(LoadslipDraftDto loadslipDraftDto, ApplicationUser loggedInUser, Loadslip loadslip, List<String> errorMessages) {
    //Save Shipment
    Shipment shipment = new Shipment();
    TruckReport truckReport = null;
    if (StringUtils.isEmpty(loadslipDraftDto.getTruckNumber())) {
      // if truck is not selected, Save SHIPMENT with SHIPMENT_ID
      shipment = saveShipment(null, loadslipDraftDto, loggedInUser, null, loadslip);
    } else {
      // if truck is selected, Save SHIPMENT with SHIPMENT_ID along other data from
      // TRUCK_REPORTING using truckNumber & status (GATED_IN)
      List<TruckReport> turks = truckReportRepo.findByTruckTruckNumberAndStatusAndReportLocation(loadslipDraftDto.getTruckNumber(),
          TruckReportStatus.GATED_IN, loggedInUser.getPlantCode());

      if ((turks != null && !turks.isEmpty())) {
        truckReport = turks.get(0);
        shipment = saveShipment(truckReport, loadslipDraftDto, loggedInUser, null, loadslip);
        if (truckReport != null) {
          // If truck is selected then only get Bay from TRUCK_REPORTING table and set to
          // LOADSLIP.BAY
          loadslip.setBay(truckReport.getBay());
        }
      } else {
        errorMessages.add(String.format("No truck found with truck number %s", loadslipDraftDto.getTruckNumber()));
      }
    }
    if (!errorMessages.isEmpty()) {
      return null;
    }
    loadslip.setShipment(shipment);
    loadslip = loadslipRepository.save(loadslip);
    // For Saving Shipment_Stop Table, currently not using, instead use Akshay provided procedure
    //    saveShipmentStopDetails(shipment, loadslip, loggedInUser);
    //Save or Update shipment stop table
    /*executeShipmentStopProcedure(shipment.getShipmentId(), loggedInUser.getUserId());
    if (truckReport != null) {
      setTruckReportActivity(truckReport, loadslip, loggedInUser);
    }*/
    return loadslip;
  }

  private TruckReport setTruckReportActivity(TruckReport truckReport, Loadslip loadslip, ApplicationUser loggedInUser) {
    List<ShipmentStop> shipmentStopList = shipmentStopRepository.findByShipmentStopIdShipmentIdAndLocationId(loadslip.getShipment().getShipmentId(), loggedInUser.getPlantCode());
    if (shipmentStopList.size() > 0) {
      ShipmentStop shipmentStop = shipmentStopList.get(0);
      truckReport.setActivity(shipmentStop.getActivity());
    }
    return truckReport;
  }

  private void executeShipmentStopProcedure(String shipmentId, String userId) {
    StoredProcedureQuery storedProcedure = entityManager
        .createStoredProcedureQuery(SHIPMENT_STOP_PROCEDURE_NAME);

    storedProcedure.registerStoredProcedureParameter("p_shipment_id", String.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("p_user_id", String.class, ParameterMode.IN);

    storedProcedure.setParameter("p_shipment_id", shipmentId);
    storedProcedure.setParameter("p_user_id", userId);
    storedProcedure.execute();
  }

  private void saveShipmentStopDetails(Shipment shipment, Loadslip loadslip, ApplicationUser loggedInUser) {
    int lastShipmentStopNumber = 0;
    Optional<ShipmentStop> optionalShipmentStop = shipmentStopRepository.findFirstByOrderByInsertDateDesc();
    // TODO: need to get  the stop number sequence logic form client
    if (optionalShipmentStop.isPresent()) {
      lastShipmentStopNumber = optionalShipmentStop.get().getShipmentStopId().getStopNum();
    }
    ShipmentStopId sourceshipmentStopId = new ShipmentStopId(shipment.getShipmentId(), lastShipmentStopNumber, loadslip.getLoadslipId());
    ShipmentStop sourceStopShipment = new ShipmentStop(sourceshipmentStopId, loadslip.getSourceLoc(), "P", loggedInUser, Utility.currentTimestamp());

    ShipmentStopId dropShipmentStopId = new ShipmentStopId(shipment.getShipmentId(), lastShipmentStopNumber, loadslip.getLoadslipId());
    ShipmentStop dropStopShipment = new ShipmentStop(dropShipmentStopId, loadslip.getDestLoc(), "D", loggedInUser, Utility.currentTimestamp());

    List<ShipmentStop> shipmentStops = new ArrayList<>();
    shipmentStops.add(sourceStopShipment);
    shipmentStops.add(dropStopShipment);
    shipmentStopRepository.saveAll(shipmentStops);
  }

  private Shipment caluculateShipmentUtilizations(Loadslip currentLoadslip, Shipment shipment, LoadslipDraftDto loadslipDraftDto) {
    double shipmentTotalTTEUtil = 0, shipmentTotalWtUtil = 0, shipmentTotalVolUtil = 0, shipmentTotalTTE = 0, shipmentTotalWt = 0, shipmentTotalVol = 0;
    int shipmentTotalLoadQty = 0;
    //Update Loadslip
    if (isExistingLoadslip(loadslipDraftDto)) {
      List<Loadslip> loadslips = loadslipRepository.findAllByShipmentShipmentIdAndStatusNot(shipment.getShipmentId(), LoadslipStatus.CANCELLED);
      if (loadslips != null && !loadslips.isEmpty()) {
        for (Loadslip loadslip : loadslips) {
          if (!loadslip.getLoadslipId().equals(currentLoadslip.getLoadslipId())) {
            shipmentTotalTTEUtil += (loadslip.getTteUtil() != null ? loadslip.getTteUtil() : 0);
            shipmentTotalWtUtil += (loadslip.getWeightUtil() != null ? loadslip.getWeightUtil() : 0);
            shipmentTotalVolUtil += (loadslip.getVolumeUtil() != null ? loadslip.getVolumeUtil() : 0);

            // In case of multi stop, these TOTAL_QTY,TOTAL_TTE,TOTAL_WEIGHT,TOTAL_VOLUME for Shipment are sum of all Loadslips Qty,TTE,Weight,volume
            shipmentTotalLoadQty += loadslip.getQty() != null ? loadslip.getQty() : 0;
            shipmentTotalTTE += loadslip.getTte() != null ? loadslip.getTte() : 0;
            shipmentTotalWt += loadslip.getWeight() != null ? loadslip.getWeight() : 0;
            shipmentTotalVol += loadslip.getVolume() != null ? loadslip.getVolume() : 0;
          }
        }
      }
    }
    // Save Loadslip
    else {
      shipmentTotalTTEUtil = shipment.getTteUtil() != null ? shipment.getTteUtil() : 0;
      shipmentTotalWtUtil = shipment.getWeightUtil() != null ? shipment.getWeightUtil() : 0;
      shipmentTotalVolUtil = shipment.getVolumeUtil() != null ? shipment.getVolumeUtil() : 0;

      // In case of multi stop, these TOTAL_QTY,TOTAL_TTE,TOTAL_WEIGHT,TOTAL_VOLUME for Shipment are sum of all Loadslips Qty,TTE,Weight,volume
      shipmentTotalLoadQty = shipment.getTotalQty() != null ? shipment.getTotalQty() : 0;
      shipmentTotalTTE = shipment.getTotalTte() != null ? shipment.getTotalTte() : 0;
      shipmentTotalWt = shipment.getTotalWeight() != null ? shipment.getTotalWeight() : 0;
      shipmentTotalVol = shipment.getTotalVolume() != null ? shipment.getTotalVolume() : 0;
    }
    shipment.setTteUtil(shipmentTotalTTEUtil + (currentLoadslip.getTteUtil() != null ? currentLoadslip.getTteUtil() : 0));
    shipment.setWeightUtil(shipmentTotalWtUtil + (currentLoadslip.getWeightUtil() != null ? currentLoadslip.getWeightUtil() : 0));
    shipment.setVolumeUtil(shipmentTotalVolUtil + (currentLoadslip.getVolumeUtil() != null ? currentLoadslip.getVolumeUtil() : 0));
    shipment.setTotalQty(shipmentTotalLoadQty + (currentLoadslip.getQty() != null ? currentLoadslip.getQty() : 0));
    shipment.setTotalTte(shipmentTotalTTE + (currentLoadslip.getTte() != null ? currentLoadslip.getTte() : 0));
    shipment.setTotalWeight(shipmentTotalWt + (currentLoadslip.getWeight() != null ? currentLoadslip.getWeight() : 0));
    shipment.setTotalVolume(shipmentTotalVol + (currentLoadslip.getVolume() != null ? currentLoadslip.getVolume() : 0));
    return shipmentRepository.save(shipment);
  }

  public void updateDelInvHeaderAndLine(List<String> invoiceList, Loadslip loadslip, String containerNum, String shipmentNum) {
    List<DelInvHeader> delheadersbyloadSlip = delInvHeaderRepository.findByLoadslipId(loadslip.getLoadslipId());
    updateDelInvHeaderLoadslipIdAndContainer(delheadersbyloadSlip, null, null, null);
    List<DelInvHeader> delheaders = delInvHeaderRepository.findByInvoiceNumberInOrderByInsertDateDesc(invoiceList);
    updateDelInvHeaderLoadslipIdAndContainer(delheaders, loadslip.getLoadslipId(), containerNum, shipmentNum);
    //update sap invoice date and number
    StringBuilder invoiceBuilder = new StringBuilder();
    StringBuilder soStoBuilder = new StringBuilder();
    StringBuilder deliveryBuilder = new StringBuilder();
    StringBuilder lrBuilder = new StringBuilder();
    loadslip.setSapInvoice(null);
    loadslip.setStoSoNum(null);
    loadslip.setDelivery(null);
    loadslip.setSapInvoiceDate(null);
    loadslip.setLrDate(null);
    delheaders.stream().parallel().forEach(delInvHeader -> {
      if (loadslip.getSapInvoiceDate() == null || delInvHeader.getInvoiceDate() != null && delInvHeader.getInvoiceDate().compareTo(loadslip.getSapInvoiceDate()) > 0) {
        loadslip.setSapInvoiceDate(delInvHeader.getInvoiceDate());
      }

      if (loadslip.getLrDate() == null || delInvHeader.getLrDate() != null && delInvHeader.getLrDate().compareTo(loadslip.getLrDate()) > 0) {
        loadslip.setLrDate(delInvHeader.getLrDate());
      }

      if (loadslip.getSapInvoice() == null) {
        invoiceBuilder.append(delInvHeader.getInvoiceNumber());
      } else {
//        invoiceBuilder.append(", ").append(delInvHeader.getInvoiceNumber());
        invoiceBuilder.append("| ").append(delInvHeader.getInvoiceNumber());
      }

      if (loadslip.getStoSoNum() == null) {
        soStoBuilder.append(delInvHeader.getSoStoNum());
      } else {
        soStoBuilder.append("| ").append(delInvHeader.getSoStoNum());
//        soStoBuilder.append(", ").append(delInvHeader.getSoStoNum());
      }

      if (loadslip.getDelivery() == null) {
        deliveryBuilder.append(delInvHeader.getDeliveryNumber());
      } else {
//        deliveryBuilder.append(", ").append(delInvHeader.getDeliveryNumber());
        deliveryBuilder.append("| ").append(delInvHeader.getDeliveryNumber());
      }

      if (loadslip.getLrNum() == null) {
        lrBuilder.append(delInvHeader.getLrNumber());
      } else {
//        lrBuilder.append(", ").append(delInvHeader.getLrNumber());
        lrBuilder.append("| ").append(delInvHeader.getLrNumber());
      }

      loadslip.setSapInvoice(invoiceBuilder.toString());
      loadslip.setStoSoNum(soStoBuilder.toString());
      loadslip.setDelivery(deliveryBuilder.toString());
      loadslip.setLrNum(lrBuilder.toString());
    });
    loadslipRepository.save(loadslip);
  }

  private void updateDelInvHeaderLoadslipIdAndContainer(List<DelInvHeader> delInvHeaders, String loadslipId, String containerNum, String shipmentNum) {
    delInvHeaders.stream().forEach((t) -> {
      //t.setContainerNum(containerNum);
      t.setLoadslipId(loadslipId);
      t.setShipmentId(shipmentNum);
    });
    delInvHeaderRepository.saveAll(delInvHeaders);
  }

  private Shipment saveShipment(TruckReport report, LoadslipDraftDto loadslipDraftDto, ApplicationUser loggedInUser,
                                Shipment existingShipment, Loadslip currentLoadslip) {
    Shipment shipment = null;
    if (existingShipment == null) {
      shipment = new Shipment();
      shipment.setShipmentId(indentSummaryRepository.findIndentSequence(Constants.SHIPMENT_SEQ_PREFIX,
          loadslipDraftDto.getSource(), loadslipDraftDto.getDestination()));
      shipment.setInsertUser(loggedInUser.getUserId());
      shipment.setInsertDate(new Date());
    } else {
      shipment = existingShipment;
    }
    shipment.setUpdateUser(loggedInUser.getUserId());
    shipment.setUpdateDate(new Date());
    if (report != null) {
      /**
       * Maintain the status of Truck as "Assign_LS" in case of the Truck is assigned
       * in load slip & do not show in another LS and don't consider this status in
       * the multistop scenario.
       */
      if (report.getStatus().equals(TruckReportStatus.GATED_IN)) {
        report.setStatus(TruckReportStatus.ASSIGN_LS);
      }
      report.setShipmentID(shipment.getShipmentId());
      if (loadslipDraftDto.getActualTruckType() != null)
        report.setActualTruckType(loadslipDraftDto.getActualTruckType());
      Optional<Freight> optionalFreight = calculateFreight(loadslipDraftDto, report, null);
      /*if freight is not available with source and  source is EXT_WAREHOUSE then calculate freight with linkedPlantLoc */
      if (!optionalFreight.isPresent()) {
        Optional<MTLocation> optionalMTLocation = locationRepository.findByLocationId(loadslipDraftDto.getSource());
        if (optionalMTLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equals(optionalMTLocation.get().getLocationClass()))
          optionalFreight = calculateFreight(loadslipDraftDto, report, optionalMTLocation.get().getLinkedPlant());
      }
      if (optionalFreight.isPresent()) {
        Freight freight = optionalFreight.get();
        report.setTTHrs(freight.getTtDays());
        report.setTtDays(freight.getTtDays());
        shipment.setFreightAvailability("Y");
        shipment.setFreight(freight.getBaseFreight());
        shipment.setFreightUom(freight.getBaseFreightUom());
      } else {
        shipment.setFreightAvailability("N");
      }
      truckReportRepo.save(report);

      if (!StringUtils.isEmpty(report.getServprov())) {
        shipment.setServprov(report.getServprov());
        shipment.setTransporterSapCode(report.getTransporterSapCode());
      }
      shipment.setTruckNumber(report.getTruck().getTruckNumber());
      shipment.setTruckType(report.getTruckType());
      shipment.setActualTruckType(StringUtils.isEmpty(loadslipDraftDto.getActualTruckType()) ? null : loadslipDraftDto.getActualTruckType());
      shipment.setDriverLicense(report.getDriverLicense());
      shipment.setDriverMobile(report.getDriverMobile());
      shipment.setDriverName(report.getDriverName());
      shipment.setIndentId(report.getIndentSummary().getIndentId());
      shipment.setDestCountry(report.getDestCountry());
      shipment.setContainerNum(loadslipDraftDto.getContainerNum());
      // TODO: Change in case of other than export
      shipment.setShipmentType(loadslipDraftDto.getType() != null ? loadslipDraftDto.getType().name() : null);
      // truck type,transporterId,TRANSHIPMENT but now we considering source and
      // destination,transporter,trucktype
      // TODO: Need to save distance in shipment table now we don't have field in
      //  Double distance =   laneRepository.findBySourceLocationIdAndDestLocation(loggedInUser.getPlantCode(), loadslipDraftDto.getDestination());
    }
    shipment.setVariant1(StringUtils.isEmpty(loadslipDraftDto.getVariant1()) ? null : loadslipDraftDto.getVariant1());
    shipment.setVariant2(StringUtils.isEmpty(loadslipDraftDto.getVariant2()) ? null : loadslipDraftDto.getVariant2());
    // TODO: For now, change it later once got update from client`
    shipment.setTransportMode(TransportersMode.TL.name());
    // TODO: Default N, FGS will check “Transhipment” field to mark that the
    // Shipment is a transshipment then make "Y".
    shipment.setTranshipment("N");
    Optional<SharedTruck> optionalSharedTruck = sharedTruckRepository.findBySharedTruckIdShipmentIdAndSharedTruckIdPickUpLocNotAndStatus(shipment.getShipmentId(), loggedInUser.getPlantCode(), Status.OPEN.name());
    if (optionalSharedTruck.isPresent()){
      shipment.setStopType(StopType.SHR.name());
    }else{
      shipment.setStopType(StopType.S.name());
    }
    // When Save or  update Loadslip,calculate Shipment TTE, Weight and Volume Utilizations
    return caluculateShipmentUtilizations(currentLoadslip, shipment, loadslipDraftDto);
  }

  @Override
  public ApiResponse updateTruckWeight(TruckReportDto truckReportDto, ApplicationUser loggedInUser) {
    String invalidInfo = truckReportDto.validateReportedTruck();
    Optional<CTScaleInvoiceWtDiff> ctScaleInvoiceWtDiff = ctScaleInvoiceWtDiffRepository.findById(loggedInUser.getPlantCode());
    if (!StringUtils.isEmpty(invalidInfo))
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("Please provide %s info", invalidInfo));
    if (truckReportDto.getInWeight() == null && truckReportDto.getOutWeight() == null) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please provide Truck Weight");
    }
    Optional<TruckReport> optionalTruckReport = truckReportRepo
        .findOneByGateControlCodeAndIndentSummaryIndentIdAndStatus(truckReportDto.getGateControlCode(),
            truckReportDto.getIndentId(), TruckReportStatus.valueOf(truckReportDto.getStatus()));
    if (!optionalTruckReport.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND,
          String.format("Reported truck not found with indent id %s and Gate control code %s",
              truckReportDto.getIndentId(), truckReportDto.getGateControlCode()));
    }
    TruckReport truckReport = optionalTruckReport.get();
    if (truckReportDto.getInWeight() != null) {
      truckReport.setInWeight(truckReportDto.getInWeight());
    } else if (truckReportDto.getOutWeight() != null) {
      truckReport.setOutWeight(truckReportDto.getOutWeight());
    }
    if (ctScaleInvoiceWtDiff.isPresent())
      truckReport.setCtDiffWt(ctScaleInvoiceWtDiff.get().getWeightDiff());

    List<Loadslip> loadslips = loadslipRepository.findAllByShipmentShipmentId(truckReport.getShipmentID());
    String loadslipComments = null;
    if (loadslips != null && !loadslips.isEmpty()) {
      loadslipComments = loadslips.parallelStream().filter(ls -> !StringUtils.isEmpty(ls.getComments())).map(Loadslip::getComments).collect(Collectors.joining(", "));
    }
    if (!StringUtils.isEmpty(truckReport.getRejectionCode())) {
      Optional<CTRejection> ctRejection = ctRejectionRepository.findById(truckReport.getRejectionCode());
      if (ctRejection.isPresent())
        truckReport.setRejectionDesc(ctRejection.get().getRejectionDesc());
    }
    truckReport.setLoadslipComments(loadslipComments);
    truckReport.setUpdateUser(loggedInUser.getUserId());
    truckReport.setUpdateDate(new Date());
    TruckReportDto reportDto = new TruckReportDto(null, truckReportRepo.save(truckReport), loggedInUser);
    reportDto.setDestDis(truckReportDto.getDestDis());
    reportDto.setDestCountryName(truckReportDto.getDestCountryName());
    return new ApiResponse(HttpStatus.OK, "", reportDto);
  }

  @Override
  public ApiResponse getTruckStatus(String reportLoc, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(reportLoc)) {
      if (!UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
        Optional<TruckReportSummary> optinalTruckReportSummary = truckReportSummaryRepository
            .findById(loggedInUser.getPlantCode());
        TruckReportDto truckReportDto = null;
        if (optinalTruckReportSummary.isPresent()) {
          TruckReportSummary truckReportSummary = optinalTruckReportSummary.get();
          truckReportDto = new TruckReportDto(truckReportSummary);
          Long excessWaitingTime = truckReportSummaryRepository.findExcessWaitingTimeForLoc(loggedInUser.getPlantCode());
				/*if (excessWaitingTime == null) {
					excessWaitingTime = truckReportSummaryRepository.findExcessWaitingTimeForLoc("ALL_LOCATIONS");
				}*/
          truckReportDto.setExcessWaitingTime(excessWaitingTime);
          Long excessReportAndWaitTime = truckReportSummaryRepository.findExcessReportAndWaitTime(loggedInUser.getPlantCode());
				/*if (excessReportAndWaitTime == null) {
          excessReportAndWaitTime = truckReportSummaryRepository.findExcessReportAndWaitTime("ALL_LOCATIONS");
				}*/
          truckReportDto.setExcessReportingWaitingTime(excessReportAndWaitTime);

        }
        return new ApiResponse(HttpStatus.OK, "", truckReportDto);
      }
    } else {
      Optional<TruckReportSummary> optinalTruckReportSummary = truckReportSummaryRepository
          .findById(reportLoc);
      TruckReportDto truckReportDto = null;
      if (optinalTruckReportSummary.isPresent()) {
        TruckReportSummary truckReportSummary = optinalTruckReportSummary.get();
        truckReportDto = new TruckReportDto(truckReportSummary);
        Long excessWaitingTime = truckReportSummaryRepository.findExcessWaitingTimeForLoc(reportLoc);
        if (excessWaitingTime != null) {
          truckReportDto.setExcessWaitingTime(excessWaitingTime);
        }
        Long excessReportAndWaitTime = truckReportSummaryRepository.findExcessReportAndWaitTime(reportLoc);
        if (excessReportAndWaitTime != null) {
          truckReportDto.setExcessReportingWaitingTime(excessReportAndWaitTime);
        }
      }
      return new ApiResponse(HttpStatus.OK, "", truckReportDto);
    }
//    For L1_MGR, L2_MGR there will be no source loc so  sending empty response
    return new ApiResponse(HttpStatus.OK, "");
  }

  @Override
  public ApiResponse viewLoadslips(ApplicationUser loggedInUser, LoadslipFilterDto loadslipFilterDto) {
    String errorMessage = Arrays.asList(loadslipFilterDto.getType().split(",")).parallelStream()
        .filter(status -> !(LoadslipStatus.isExist(status))).collect(Collectors.joining(","));
    if (!StringUtils.isEmpty(errorMessage)) {
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("Given type %s not found in system", errorMessage));
    }
    Page<LoadslipMetaData> loadslipMetaData = null;
    List<String> statuses = new ArrayList<>(Arrays.asList(loadslipFilterDto.getType().split(",")));
    if (loadslipFilterDto.isLoadslipsFilter()) {
      int totalPageLenght = loadslipFilterDto.getPageLength();
      List<LoadslipMetaData> loadslips = new ArrayList<>();
      if (StringUtils.isEmpty(loadslipFilterDto.getItemId()) && StringUtils.isEmpty(loadslipFilterDto.getInvoice())) {
        loadslips.addAll(filterService.noInvoiceLoadslipFilter(loggedInUser, statuses, loadslipFilterDto));
      }
// loadslipFilterDto.setPageLength(totalPageLenght - loadslips.size());
      loadslips.addAll(filterService.filterLoadslips(loggedInUser, statuses, loadslipFilterDto));
//TODO: change below logic, its a temp work around
      int totalElements = loadslips.size();
      int startIndex = loadslipFilterDto.getIndex() * loadslipFilterDto.getPageLength();
      int endIndex = (loadslipFilterDto.getIndex() + 1) * loadslipFilterDto.getPageLength();
      if (endIndex > loadslips.size()) {
        endIndex = loadslips.size();
      }
      loadslips = loadslips.subList(startIndex, endIndex);
      loadslips.sort(Comparator.comparing(OpsServiceImpl::apply).reversed());
      loadslipMetaData = new PageImpl<>(loadslips, PageRequest.of(loadslipFilterDto.getIndex(), loadslipFilterDto.getPageLength()), totalElements);

      loadslipMetaData.forEach((lsmd) -> {
        if (!StringUtils.isEmpty(lsmd.getLoadslipId())) {
          int size = delInvHeaderRepository.findByLoadslipId((String) lsmd.getLoadslipId()).size();
          lsmd.setInvoiceCnt(size > 0 ? size : null);
        }
        lsmd.setCreatedDate(lsmd.getCreatedDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getCreatedDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setGatedOutDate(lsmd.getGatedOutDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getGatedOutDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setLrDate(lsmd.getLrDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getLrDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setSapInvoiceDate(lsmd.getSapInvoiceDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getSapInvoiceDate(), Constants.DATE_TIME_FORMAT) : null);
      });
    } else {
      Page<Map<String, Object>> loadslipMap = null;
      /*DP_REP, L1_MGR, L2_MGR*/
      if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
        loadslipMap = loadslipRepository.getLoadSlipsByStatusForDPREP(statuses, loadslipFilterDto.getStatus(), PageRequest.of(loadslipFilterDto.getIndex(), loadslipFilterDto.getPageLength()));
      } else {
        loadslipMap = loadslipRepository.getLoadSlipsBySourceLocAndStatus(
            loggedInUser.getPlantCode(), statuses, loadslipFilterDto.getStatus(), PageRequest.of(loadslipFilterDto.getIndex(), loadslipFilterDto.getPageLength()));
      }
      loadslipMetaData = new PageImpl<>(loadslipMap.stream().parallel().map(LoadslipMetaData::new).collect(Collectors.toList()),
          PageRequest.of(loadslipFilterDto.getIndex(), loadslipFilterDto.getPageLength()), loadslipMap.getTotalElements());
    }
    LoadslipFilterDto loadslipDto = new LoadslipFilterDto(loadslipMetaData);

    List<String> countryCodeList = loadslipMetaData.stream().parallel().map(loadslipMeta -> !StringUtils.isEmpty(loadslipMeta.getDestCountry()) ? loadslipMeta.getDestCountry().toString() : null).distinct().collect(Collectors.toList());
    List<CTCountry> ctCountryList = ctCountryRepository.findAllByCountryCodeIn(countryCodeList);

    List<String> uniqueDest = loadslipDto.getLoadslips().stream().parallel().map(loadslip -> loadslip.getDestLoc().toString()).distinct().collect(Collectors.toList());
    if (uniqueDest.size() > 0) {
      List<Map<String, String>> destWithDesc = locationRepository.findDestDescWtihDestinations(uniqueDest);
      List<String> loadslipIds = loadslipDto.getLoadslips().parallelStream().map(loadslipMetaData1 -> loadslipMetaData1.getLoadslipId().toString()).collect(Collectors.toList());
      List<LoadslipMetaData> loadslipMapList = null;
      if (loadslipIds.size() > 0) {
        loadslipMapList = findAllLoadslipDetails(loadslipIds);
      }
      for (LoadslipMetaData lsmd : loadslipDto.getLoadslips()) {
        Optional<Map<String, String>> destLoc = destWithDesc.stream().filter(destDescMap -> destDescMap.get("destLoc").equals(lsmd.getDestLoc())).findAny();
        if (destLoc.isPresent() && destLoc.get().containsKey("destLoc")){
          lsmd.setDestDis(destLoc.get().get("DESTDESC"));
        }
        //For FGS & RDC Operations role, Loadslip cant be cancelled if there is STO/SO or scan qty in atleast one of Loadslip Items
        if (loggedInUser.getRole().isFGSOrRDCOperationsRole()) {
          //Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(lsmd.getLoadslipId().toString());
          if (loadslipMapList != null && loadslipMapList.size() > 0) {
            for (LoadslipMetaData lsm : loadslipMapList) {
              if (lsm.getLoadslipId().toString().equals(lsmd.getLoadslipId()) && !DelInvType.isExportOrJIT(lsm.getLoadslipType().toString())) {
                // LS can not be cancelled once sto/so or scan qty is present
                if (!StringUtils.isEmpty(lsm.getStoSoNum()) || lsm.getScannedQty() != null && (Integer) lsm.getScannedQty() > 0) {
                  lsmd.setCanLSCancelled(false);
                  break;
                }
              }
            }
          }
        }
        if (!StringUtils.isEmpty(lsmd.getDestCountry())) {
          Optional<CTCountry> optionalCTCountry = ctCountryList.parallelStream().filter(ctCountry -> ctCountry.getCountryCode().equals(lsmd.getDestCountry())).findFirst();
          if (optionalCTCountry.isPresent()) {
            lsmd.setDestCountryName(optionalCTCountry.get().getCountryName());
          }
        }
      }
    }
    return new ApiResponse(HttpStatus.OK, "", loadslipDto);
  }

  @Override
  public ApiResponse viewLoadslipsMovement(ApplicationUser loggedInUser, LoadslipFilterDto loadslipFilterDto) {
    String errorMessage = Arrays.asList(loadslipFilterDto.getType().split(",")).parallelStream()
        .filter(status -> !(LoadslipStatus.isExist(status))).collect(Collectors.joining(","));
    if (!StringUtils.isEmpty(errorMessage)) {
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("Given type %s not found in system", errorMessage));
    }
    Page<LoadslipMetaData> loadslipMetaData = null;
    List<String> statuses = new ArrayList<>(Arrays.asList(loadslipFilterDto.getType().split(",")));
    if (loadslipFilterDto.isLoadslipsFilter()) {
      loadslipMetaData = filterService.filterLoadslipsMovement(loggedInUser, statuses, loadslipFilterDto);
      loadslipMetaData.forEach((lsmd) -> {
        /*if (!StringUtils.isEmpty(lsmd.getLoadslipId())) {
          int size = delInvHeaderRepository.findByLoadslipId((String) lsmd.getLoadslipId()).size();
          lsmd.setInvoiceCnt(size > 0 ? size : null);
        }*/
        lsmd.setCreatedDate(lsmd.getCreatedDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getCreatedDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setGatedOutDate(lsmd.getGatedOutDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getGatedOutDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setLrDate(lsmd.getLrDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getLrDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setSapInvoiceDate(lsmd.getSapInvoiceDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getSapInvoiceDate(), Constants.DATE_TIME_FORMAT) : null);

        lsmd.setGateInDate(lsmd.getGateInDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getGateInDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setGateOutDate(lsmd.getGateOutDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getGateOutDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setReportDate(lsmd.getReportDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getReportDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setReleaseDate(lsmd.getReleaseDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getReleaseDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setReleasedDate(lsmd.getReleasedDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getReleasedDate(), Constants.DATE_TIME_FORMAT) : null);

        lsmd.setBayArrivedDate(lsmd.getBayArrivedDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getBayArrivedDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setConfirmedDate(lsmd.getConfirmedDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getConfirmedDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setLoadingStartDate(lsmd.getLoadingStartDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getLoadingStartDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setLoadingEndDate(lsmd.getLoadingEndDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getLoadingEndDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setLsPrintDate(lsmd.getLsPrintDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getLsPrintDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setSendForBarcodeDate(lsmd.getSendForBarcodeDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getSendForBarcodeDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setGrnDate(lsmd.getGrnDate() != null ? DateUtils.formatDate((java.util.Date) lsmd.getGrnDate(), Constants.DATE_TIME_FORMAT) : null);
        lsmd.setOtherQty((lsmd.getOtherQty() != null && Integer.parseInt(lsmd.getOtherQty().toString()) == 0) ? null : lsmd.getOtherQty());
        if (!StringUtils.isEmpty(lsmd.getInWeight()) && !StringUtils.isEmpty(lsmd.getOutWeight()))
          lsmd.setNetWeight(Math.abs(Double.valueOf(lsmd.getOutWeight().toString()) - Double.valueOf(lsmd.getInWeight().toString())));
        if (!StringUtils.isEmpty(lsmd.getNetWeight()) && !StringUtils.isEmpty(lsmd.getSapInvWeight()))
          lsmd.setDiffInvWeight(Math.abs((Double.valueOf(lsmd.getNetWeight().toString())) - (Double.valueOf(lsmd.getSapInvWeight().toString()))));
      });
    } else {
      Page<Map<String, Object>> loadslipMap = null;
      /*DP_REP, L1_MGR, L2_MGR*/
      if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
        loadslipMap = loadslipRepository.getLoadSlipsMovementBySourceLocAndStatusDprep(statuses, loadslipFilterDto.getStatus(), PageRequest.of(loadslipFilterDto.getIndex(), loadslipFilterDto.getPageLength()));
      } else {
        loadslipMap = loadslipRepository.getLoadSlipsMovementBySourceLocAndStatus(
            loggedInUser.getPlantCode(), statuses, loadslipFilterDto.getStatus(), PageRequest.of(loadslipFilterDto.getIndex(), loadslipFilterDto.getPageLength()));
      }
      loadslipMetaData = new PageImpl<>(loadslipMap.stream().parallel().map(LoadslipMetaData::new).collect(Collectors.toList()),
          PageRequest.of(loadslipFilterDto.getIndex(), loadslipFilterDto.getPageLength()), loadslipMap.getTotalElements());
    }
    LoadslipFilterDto loadslipDto = new LoadslipFilterDto(loadslipMetaData);

//    Optional<CTScaleInvoiceWtDiff> ctScaleInvoiceWtDiff = ctScaleInvoiceWtDiffRepository.findById(loggedInUser.getPlantCode());
    /*Getting the ctScaleWeightDiff
     * If Role = DP_REP,L1_MGR, L2_MGR then get CTScaleInvoiceWtDiff with list of truck-reportLoc
     * For normal user get CTScaleInvoiceWtDiff with loggedIn plantCode*/
    Optional<CTScaleInvoiceWtDiff> optionalCTScaleInvoiceWtDiff = null;
    List<CTScaleInvoiceWtDiff> ctScaleInvoiceWtDiffs = null;
    if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
      List<String> loadslipDestLocs = loadslipFilterDto.getLoadslips().parallelStream().map(lsDto -> lsDto.getDestLoc().toString()).distinct().collect(Collectors.toList());
      ctScaleInvoiceWtDiffs = ctScaleInvoiceWtDiffRepository.findAllByLocationIdIn(loadslipDestLocs);
    } else {
      optionalCTScaleInvoiceWtDiff = ctScaleInvoiceWtDiffRepository.findById(loggedInUser.getPlantCode());
    }

    List<String> countryCodeList = loadslipMetaData.stream().parallel().map(loadslipMeta -> !StringUtils.isEmpty(loadslipMeta.getDestCountry()) ? loadslipMeta.getDestCountry().toString() : null).distinct().collect(Collectors.toList());
    List<CTCountry> ctCountryList = ctCountryRepository.findAllByCountryCodeIn(countryCodeList);

    List<String> uniqueDest = loadslipDto.getLoadslips().stream().parallel().map(loadslip -> loadslip.getDestLoc().toString()).distinct().collect(Collectors.toList());
    if (uniqueDest.size() > 0) {
      List<Map<String, String>> destWithDesc = locationRepository.findDestDescWtihDestinations(uniqueDest);
      List<String> loadslipIds = loadslipDto.getLoadslips().parallelStream().map(loadslipMetaData1 -> loadslipMetaData1.getLoadslipId().toString()).collect(Collectors.toList());
      List<LoadslipMetaData> loadslipMetaDatas = null;
      if (loadslipIds.size() > 0) {
        loadslipMetaDatas = findAllLoadslipDetails(loadslipIds);
      }
      for (LoadslipMetaData lsmd : loadslipDto.getLoadslips()) {
//        if (ctScaleInvoiceWtDiff.isPresent() && !StringUtils.isEmpty(ctScaleInvoiceWtDiff.get().getWeightDiff()))
//          lsmd.setWeightDiffStatus((lsmd.getDiffInvWeight() != null ? Double.valueOf(lsmd.getDiffInvWeight().toString()) : 0.0) > (Double.valueOf(ctScaleInvoiceWtDiff.get().getWeightDiff())));

        /*if role = DP_REP,L1_MGR, L2_MGR then finding the ctScaleWeightDiff for that reportLoc
         * for normal user no need to execute this IF - block as we will have only one CTScaleInvoiceWtDiff record*/
        if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
          if (ctScaleInvoiceWtDiffs != null && ctScaleInvoiceWtDiffs.size() > 0) {
            optionalCTScaleInvoiceWtDiff = ctScaleInvoiceWtDiffs.parallelStream().filter(ct -> lsmd.getSourceLoc().toString().equals(ct.getLocationId())).findFirst();
          }
        }
        if (optionalCTScaleInvoiceWtDiff != null && optionalCTScaleInvoiceWtDiff.isPresent() && !StringUtils.isEmpty(optionalCTScaleInvoiceWtDiff.get().getWeightDiff())) {
          lsmd.setWeightDiffStatus((lsmd.getDiffInvWeight() != null ? Double.valueOf(lsmd.getDiffInvWeight().toString()) : 0.0) > (Double.valueOf(optionalCTScaleInvoiceWtDiff.get().getWeightDiff())));
        }
        lsmd.setDestDis(destWithDesc.stream().filter(destDescMap -> destDescMap.get("destLoc").equals(lsmd.getDestLoc())).findAny().get().get("DESTDESC"));
        //For FGS & RDC Operations role, Loadslip cant be cancelled if there is STO/SO or scan qty in atleast one of Loadslip Items
        if (loggedInUser.getRole().isFGSOrRDCOperationsRole()) {
          //Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(lsmd.getLoadslipId().toString());
          if (loadslipMetaDatas != null && loadslipMetaDatas.size() > 0) {
            for (LoadslipMetaData metaData : loadslipMetaDatas) {
              if (metaData.getLoadslipId().toString().equals(lsmd.getLoadslipId()) && !DelInvType.isExportOrJIT(metaData.getLoadslipType().toString())) {
                // LS can not be cancelled once sto/so or scan qty is present
                if (!StringUtils.isEmpty(metaData.getStoSoNum()) || metaData.getScannedQty() != null && (Integer) metaData.getScannedQty() > 0) {
                  lsmd.setCanLSCancelled(false);
                  break;
                }
              }
            }
           /* for (Map<String, Object> objectMap : loadslipMapList) {
              if (objectMap.get("loadslipId").toString().equals(lsmd.getLoadslipId()) && !DelInvType.isExportOrJIT(objectMap.get("loadslipType").toString())) {
                // LS can not be cancelled once sto/so or scan qty is present
                if (!StringUtils.isEmpty(objectMap.get("stoSoNum")) || objectMap.get("scannedQty") != null && (Integer) objectMap.get("scannedQty") > 0) {
                  lsmd.setCanLSCancelled(false);
                  break;
                }
              }
            }*/
          }
        }
        if (!StringUtils.isEmpty(lsmd.getDestCountry())) {
          Optional<CTCountry> optionalCTCountry = ctCountryList.parallelStream().filter(ctCountry -> ctCountry.getCountryCode().equals(lsmd.getDestCountry())).findFirst();
          if (optionalCTCountry.isPresent()) {
            lsmd.setDestCountryName(optionalCTCountry.get().getCountryName());
          }
        }
      }
    }
    return new ApiResponse(HttpStatus.OK, "", loadslipDto);
  }

  public List<LoadslipMetaData> findAllLoadslipDetails(List<String> inComingloadslipIds) {

    StringBuilder stringBuilder = new StringBuilder("SELECT l.loadslip_id   AS loadslipId, \n" +
        "       l.loadslip_type AS loadslipType, \n" +
        "       l.sto_so_num    AS stoSoNum, \n" +
        "       ld.scanned_qty  AS scannedQty \n" +
        " FROM   loadslip l \n" +
        "       JOIN loadslip_detail ld \n" +
        "         ON ld.loadslip_id = l.loadslip_id \n");
    List<List<String>> partitionIncomingLoadslipIds = null;

    if (null != inComingloadslipIds) {
      partitionIncomingLoadslipIds = Lists.partition(inComingloadslipIds, 1000);
      stringBuilder.append(" where (");
      for (int i = 0; i < partitionIncomingLoadslipIds.size(); i++) {
        stringBuilder.append(" l.LOADSLIP_ID  in (" + Utility.join(partitionIncomingLoadslipIds.get(i)) + ") ");
        if (i == partitionIncomingLoadslipIds.size() - 1) {
          stringBuilder.append(" ) ");
        } else {
          stringBuilder.append(" OR ");
        }
      }
    }
    Query q = entityManager.unwrap(org.hibernate.Session.class).createNativeQuery(stringBuilder.toString())
        .addScalar("loadslipId", StandardBasicTypes.STRING).addScalar("loadslipType", StandardBasicTypes.STRING)
        .addScalar("stoSoNum", StandardBasicTypes.STRING).addScalar("scannedQty", StandardBasicTypes.INTEGER);

    List<LoadslipMetaData> loadslipDatas = ((NativeQuery) q)
        .setResultTransformer(new AliasToBeanResultTransformer(LoadslipMetaData.class)).getResultList();


    return loadslipDatas;
  }


  public Page<TruckReport> getIntransitTrucksForMaxShipments(TruckReportFilterDto truckReportFilterDto, List<String> incomingShipmentIds, List<TruckReportStatus> truckStatuses, String reportLocation, ApplicationUser loggedInUser) {

    StringBuilder status = new StringBuilder();
    int count = 1;
    for (TruckReportStatus truckStatus : truckStatuses) {
      status.append("'");
      status.append(truckStatus);
      status.append("'");
      if (count != truckStatuses.size()) {
        status.append(",");
      }
      count++;
    }
    //updated for consent api
    StringBuilder stringBuilder = new StringBuilder("select tr.GATE_CONTROL_CODE as gateControlCode ,tr.TYPE as type ,tr.TRANSPORTER_SAP_CODE as transporterSapCode ,tr.CONTAINER_NUM as containerNum ," +
        "      tr.CONTAINER_CODE  as containerCode ,tr.DRIVER_NAME as driverName,tr.DRIVER_MOBILE as driverMobile ,tr.DRIVER_LICENSE as driverLicense ,tr.SERVPROV   as servprov ," +
        "      tr.TRUCK_TYPE  as truckType ,tr.REPORTING_LOCATION  as reportLocation ,tr.SOURCE_LOC  as sourceLocation,tr.DEST_LOC  as destinationLocation ,tr.REPORTING_DATE  as reportDate ," +
        "      tr.GATEIN_DATE  as gateInDate ,tr.GATEOUT_DATE  as gateOutDate ,tr.DEST_GEOFENCEIN_DATE  as destGeofenceDate,tr.DEREPORTING_DATE  as dereportDate,tr.REP_GI_HRS  as waitTimeHrs," +
        "      tr.GI_GO_HRS  as tTHrs,tr.STATUS  as status,tr.REJ_STATUS  as rejectedStatus,tr.IN_WEIGHT  as inWeight,tr.OUT_WEIGHT  as outWeight,tr.NET_WEIGHT  as  netWeight,tr.BAY  as bay, " +
        "      tr.BAY_STATUS  as bayStatus,tr.REJECTION_CODE  as rejectionCode,tr.ACTUAL_TRUCK_TYPE  as actualTruckType,tr.REF_CODE  as refCode ,tr.INSERT_USER  as insertUser,tr.UPDATE_USER  as updateUser," +
        "      tr.INSERT_DATE  as insertDate,tr.UPDATE_DATE  as updateDate,tr.Shipment_ID  as shipmentID,tr.ETA_DESTINATION  as EtaDest,tr.REPORTED_TRUCK_TYPE  as reportedTruckType,tr.ACTIVITY as  activity," +
        "      tr.COMMENTS  as comments,mt.TRUCK_NUMBER  as truckNumber,mt.GPS_ENABLED  as gpsEnabled,ct.GPS_PROVIDER  as gpsProvider,i.INDENT_ID  as indentId, tr.TRACKING_CONSENT_STATUS as trackingConsentStatus,"+
            "  tr.CONSENT_PHONE_TELECOM as consentPhoneTelecom from truck_reporting tr " +
        "      left join mt_truck mt on mt.TRUCK_NUMBER=tr.TRUCK_NUMBER " +
        "      left join CT_GPS ct on ct.GPS_PROVIDER=mt.GPS_PROVIDER " +
        "      left join indent_summary i on i.indent_Id = tr.indent_Id");

    List<List<String>> partitionIncomingShipmentIds = null;

    if (null != incomingShipmentIds) {
      partitionIncomingShipmentIds = Lists.partition(incomingShipmentIds, 1000);
      stringBuilder.append(" where (");
      for (int i = 0; i < partitionIncomingShipmentIds.size(); i++) {
        stringBuilder.append(" tr.Shipment_ID in (" + Utility.join(partitionIncomingShipmentIds.get(i)) + ") ");
        if (i == partitionIncomingShipmentIds.size() - 1) {
          stringBuilder.append(" ) ");
        } else {
          stringBuilder.append(" OR ");
        }
      }
      stringBuilder.append(" and tr.status IN (" + status + ") ");
//      stringBuilder.append("and (tr.REPORTING_LOCATION !='" + reportLocation + "' )");
      if (!UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
        stringBuilder.append("and (tr.REPORTING_LOCATION !='" + reportLocation + "' )");
      }
      stringBuilder.append(" order by tr.reporting_date desc");
    }


    Query q = entityManager.unwrap(org.hibernate.Session.class).createNativeQuery(stringBuilder.toString())
        .addScalar("gateControlCode", StandardBasicTypes.STRING).addScalar("type", Utility.getCustomEnumType(Constants.SystemType.class.getName())).addScalar("transporterSapCode", StandardBasicTypes.STRING)
        .addScalar("containerNum", StandardBasicTypes.STRING).addScalar("containerCode", StandardBasicTypes.STRING).addScalar("driverName", StandardBasicTypes.STRING)
        .addScalar("driverMobile", StandardBasicTypes.STRING).addScalar("driverLicense", StandardBasicTypes.STRING).addScalar("servprov", StandardBasicTypes.STRING)
        .addScalar("truckType", StandardBasicTypes.STRING).addScalar("reportLocation", StandardBasicTypes.STRING).addScalar("sourceLocation", StandardBasicTypes.STRING)
        .addScalar("destinationLocation", StandardBasicTypes.STRING).addScalar("reportDate", StandardBasicTypes.DATE).addScalar("gateInDate", StandardBasicTypes.DATE)
        .addScalar("gateOutDate", StandardBasicTypes.DATE).addScalar("destGeofenceDate", StandardBasicTypes.DATE).addScalar("dereportDate", StandardBasicTypes.DATE)
        .addScalar("waitTimeHrs", StandardBasicTypes.INTEGER).addScalar("tTHrs", StandardBasicTypes.INTEGER).addScalar("status", Utility.getCustomEnumType(Constants.TruckReportStatus.class.getName()))
        .addScalar("rejectedStatus", Utility.getCustomEnumType(Constants.TruckReportStatus.class.getName())).addScalar("inWeight", StandardBasicTypes.DOUBLE).addScalar("outWeight", StandardBasicTypes.DOUBLE)
        .addScalar("netWeight", StandardBasicTypes.DOUBLE).addScalar("bay", StandardBasicTypes.STRING).addScalar("bayStatus", Utility.getCustomEnumType(Constants.BayStatus.class.getName()))
        .addScalar("rejectionCode", StandardBasicTypes.STRING).addScalar("actualTruckType", StandardBasicTypes.STRING).addScalar("refCode", StandardBasicTypes.STRING)
        .addScalar("insertUser", StandardBasicTypes.STRING).addScalar("updateUser", StandardBasicTypes.STRING).addScalar("insertDate", StandardBasicTypes.DATE)
        .addScalar("updateDate", StandardBasicTypes.DATE).addScalar("shipmentID", StandardBasicTypes.STRING).addScalar("EtaDest", StandardBasicTypes.DATE)
        .addScalar("reportedTruckType", StandardBasicTypes.STRING).addScalar("activity", StandardBasicTypes.STRING).addScalar("comments", StandardBasicTypes.STRING)
        .addScalar("truckNumber", StandardBasicTypes.STRING).addScalar("gpsEnabled", StandardBasicTypes.STRING).addScalar("gpsProvider", StandardBasicTypes.STRING)
        .addScalar("indentId", StandardBasicTypes.STRING);

//    PaginationViewBean pageViewBean = new PaginationViewBean();
    int totalElement = q.getResultList().size();
    q.setFirstResult((truckReportFilterDto.getIndex()) * truckReportFilterDto.getPageLength());
    q.setMaxResults(truckReportFilterDto.getPageLength());
    List<TruckReport> truckReports = ((NativeQuery) q)
        .setResultTransformer(new AliasToBeanResultTransformer(TruckReport.class)).getResultList();


   /* TypedQuery<LoadslipMetaData> typedQuery = entityManager.createQuery(criteriaQuery);
    int count = typedQuery.getResultList().size();
    typedQuery.setFirstResult(loadslipFilterDto.getIndex() * loadslipFilterDto.getPageLength());
    typedQuery.setMaxResults(loadslipFilterDto.getPageLength());*/
    return new PageImpl<>(truckReports, PageRequest.of(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength()), totalElement);
  }

  @Override
  public ApiResponse getTrucksData(TruckReportFilterDto truckReportFilterDto, ApplicationUser loggedInUser) {
    if (!UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole()) && (StringUtils.isEmpty(truckReportFilterDto.getType()) || StringUtils.isEmpty(truckReportFilterDto.getReportLocation())))
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please provide  type of trucks and report Location");
    List<TruckReportStatus> truckReportStatuses = new ArrayList<>();
    Page<TruckReport> truckReports = null;

    if (truckReportFilterDto.getType().equalsIgnoreCase("INVENTORY")) {
      if (StringUtils.isEmpty(truckReportFilterDto.getStatus())) {
        truckReportStatuses = Constants.TruckReportStatus.getInventoryTrucks();
      } else {
        //truckReportStatuses.add(Constants.TruckReportStatus.valueOf(truckReportFilterDto.getStatus()));
        List<String> statuses = new ArrayList<>(Arrays.asList(truckReportFilterDto.getStatus().split(",")));
        truckReportStatuses = statuses.parallelStream().map(Constants.TruckReportStatus::valueOf).collect(Collectors.toList());
      }

      truckReports = getShipmentAndReceivingTrucksByActivity(truckReportFilterDto, truckReportStatuses, loggedInUser);
      if (truckReports != null) {
        // set Unloading start & Unloading end dates from loadlsips of the shipment which is saved in trcuk report
        setLoadslipTimestamps(truckReports, loggedInUser);
      }

    } else if (truckReportFilterDto.getType().equalsIgnoreCase("HISTORY")) {
      if (StringUtils.isEmpty(truckReportFilterDto.getStatus())) {
        truckReportStatuses = Constants.TruckReportStatus.getTruckHistoryStatus();
      } else {
        //truckReportStatuses.add(Constants.TruckReportStatus.valueOf(truckReportFilterDto.getStatus()));
        List<String> statuses = new ArrayList<>(Arrays.asList(truckReportFilterDto.getStatus().split(",")));
        truckReportStatuses = statuses.parallelStream().map(Constants.TruckReportStatus::valueOf).collect(Collectors.toList());
      }
      truckReports = getShipmentAndReceivingTrucksByActivity(truckReportFilterDto, truckReportStatuses, loggedInUser);

    } else if (truckReportFilterDto.getType().equalsIgnoreCase("INTRANSIT")) {
      truckReportStatuses = Constants.TruckReportStatus.getIntransitTruckStatus();
      List<String> truckStatusesList = truckReportStatuses.parallelStream().map(TruckReportStatus::name).collect(Collectors.toList());

      if (!truckReportFilterDto.isReportedTrucksFilter()) {
        Page<Map<String, Object>> truckReportMap = null;
        List<TruckReport> mappedTruckReport = null;
        Long total = 0L;
        if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
          truckReportMap = truckReportRepo.findByShipmentIDInAndStatusInOrderByReportDateAsc(truckStatusesList,
              PageRequest.of(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength()));
        } else {
          truckReportMap = truckReportRepo.findByShipmentIDInAndStatusInAndReportLocationNotOrderByReportDateAsc(loggedInUser.getPlantCode(),truckStatusesList,
              PageRequest.of(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength()));
        }
        mappedTruckReport = truckReportMap.stream().parallel().map(TruckReport::new).collect(Collectors.toList());

        /*Setting the truck Report boolean value
         * When any of the reporting location is equal to logged In loc then set to TRUE*/
        List<String> uniqueShipmentIds = mappedTruckReport.parallelStream().map(TruckReport::getShipmentID).distinct().collect(Collectors.toList());
        if (uniqueShipmentIds != null && !uniqueShipmentIds.isEmpty()) {
          List<Map<String, String>> truckReportLocations = new ArrayList<>();
          ListUtils.partition(uniqueShipmentIds, 999).parallelStream().forEach(subList -> {
            truckReportLocations.addAll(truckReportRepo.findReportLocationByShipmentIDIn(subList));
          });
          mappedTruckReport.parallelStream().forEach(truck -> {
            List<String> shipmentReportLocs = truckReportLocations.parallelStream().filter(map -> map.get("shipmentId").equals(truck.getShipmentID()))
                .map(m -> m.get("reportLoc")).collect(Collectors.toList());
            if (shipmentReportLocs != null && shipmentReportLocs.contains(loggedInUser.getPlantCode())) {
              truck.setTruckReported(true);
            }
          });
        }
        total = truckReportMap.getTotalElements();
        truckReports = new PageImpl<>(mappedTruckReport, PageRequest.of(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength()), total);
      } /*else {
        //Fetch All shipments which are Intransit to this Location from both SHIPMENT_STOP and SHARED_TRUCK
        List<String> incomingShipmentIds = null;
        if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
          *//*getting incoming shipment ids from all the locations*//*
          incomingShipmentIds = shipmentStopRepository.findShipmentIdsFromShipmentStopAndSharedTruck();
        } else {
          *//*Getting incoming shipmentIds for logged In location/reported loc *//*
          incomingShipmentIds = shipmentStopRepository.findShipmentIdsByPlantLocFromShipmentStopAndSharedTruck(truckReportFilterDto.getReportLocation());
        }

        if (incomingShipmentIds == null || incomingShipmentIds.isEmpty()) {
          return new ApiResponse(HttpStatus.NOT_FOUND, "No Data Found!");
        }
        List<String> distinctShipmentIds = null;
        if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())){
          distinctShipmentIds = truckReportRepo.findDistinctIntransitShipmentsIdForDprep();
        }else {
          distinctShipmentIds = truckReportRepo.findDistinctIntransitShipmentsId(loggedInUser.getPlantCode());
        }
        // Filter Trucks
        truckReports = filterService.filterTrucksInfo(truckReportFilterDto, truckReportStatuses, incomingShipmentIds, loggedInUser, distinctShipmentIds);
      *//* Filtering the trucks by Unique key combination i.e SHIPMENT_ID & REPORTING_LOCATION (In Multi pick up and single Drop scenario,two intransit trucks are shown at Drop Location.
       To overcome this, we are filtering based on shipment_id and reporting_location  ) *//*
        List<TruckReport> truckReportList = truckReports.stream().parallel().filter(Utility.distinctByKey(TruckReport::getShipmentID)).collect(Collectors.toList());
        truckReportList = truckReportList.parallelStream().peek(truckReport -> {
          List<String> truckReportLocations = truckReportRepo.findReportLocationByShipmentID(truckReport.getShipmentID());
          if (truckReportLocations != null && truckReportLocations.contains(loggedInUser.getPlantCode())) {
            truckReport.setTruckReported(true);
          }
        }).collect(Collectors.toList());
        truckReports = new PageImpl<>(truckReportList, PageRequest.of(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength()), truckReports.getTotalElements());
      }*/
      else{
        truckReports = filterService.filterIntransitTrucksInfo(truckReportFilterDto, truckReportStatuses, loggedInUser);
      }

    }
    setAdditionalInfoForTruckReport(loggedInUser, truckReports,truckReportFilterDto);

    TruckReportFilterDto reportFilterDto = null;
    if (truckReports != null) {
      /*for type HISTORY/INVENTORY/INTRANSIT we will use seperate Constructor with parameter didNotUse(we are passing empty string) */
      if ((truckReportFilterDto.getType().contentEquals("HISTORY") || truckReportFilterDto.getType().contentEquals("INVENTORY")
          || truckReportFilterDto.getType().contentEquals("INTRANSIT")) && !truckReportFilterDto.isReportedTrucksFilter()) {

        reportFilterDto = new TruckReportFilterDto(loggedInUser, truckReports, "", truckReportFilterDto.getPageLength());
      } else {
        if (truckReportFilterDto.getType().equals("INTRANSIT")){
          reportFilterDto = new TruckReportFilterDto(loggedInUser, truckReports, "", truckReportFilterDto.getPageLength());
        }else {
          reportFilterDto = new TruckReportFilterDto(loggedInUser, truckReports, truckReportFilterDto.getPageLength());
        }
      }
      List<String> uniqueDestinations = truckReports.stream().map(TruckReport::getDestinationLocation).distinct().collect(Collectors.toList());
      if (uniqueDestinations.size() > 0) {
        List<Map<String, String>> destWithDesc = locationRepository.findDestDescWtihDestinations(uniqueDestinations);
        reportFilterDto.getTruckReportsData().forEach(truckReportDto -> {
          truckReportDto.setDestDis(destWithDesc.stream().filter(destDescMap -> destDescMap.get("destLoc").equals(truckReportDto.getDestination())).findAny().get().get("DESTDESC"));
        });
      }
      return new ApiResponse(HttpStatus.OK, "", reportFilterDto);

    } else {
      return new ApiResponse(HttpStatus.OK, "No Trucks Found", null);
    }
  }

  private void setAdditionalInfoForTruckReport(ApplicationUser loggedInUser, Page<TruckReport> truckReports, TruckReportFilterDto truckReportFilterDto) {
    /*Getting the ctScaleWeightDiff
     * If Role = DP_REP,L1_MGR, L2_MGR then get CTScaleInvoiceWtDiff with list of truck-reportLoc
     * For normal user get CTScaleInvoiceWtDiff with loggedIn plantCode*/
    Optional<CTScaleInvoiceWtDiff> optionalCTScaleInvoiceWtDiff = null;
    List<CTScaleInvoiceWtDiff> ctScaleInvoiceWtDiffs = null;
    if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
      List<String> truckReportingLocs = truckReports.stream().parallel().map(truck -> truck.getReportLocation()).distinct().collect(Collectors.toList());
      ctScaleInvoiceWtDiffs = ctScaleInvoiceWtDiffRepository.findAllByLocationIdIn(truckReportingLocs);
    } else {
      optionalCTScaleInvoiceWtDiff = ctScaleInvoiceWtDiffRepository.findById(loggedInUser.getPlantCode());
    }
    /*to get the ctRejectionDesc*/
    List<CTRejection> ctRejections = ctRejectionRepository.findAll();
    List<Shipment> shipmentMapList = null;
    if (truckReportFilterDto.isReportedTrucksFilter()) {
      shipmentMapList = getShipmentMapFromIds(truckReports.getContent());
    }


    /*To display the comments entered by operator in drafted loadslips to gateSecurity*/
    List<String> uniqueShipments = truckReports.stream().parallel().map(TruckReport::getShipmentID).distinct().collect(Collectors.toList());
    List<String> countryCodeList = truckReports.stream().parallel().map(TruckReport::getDestCountry).distinct().collect(Collectors.toList());
    if (uniqueShipments.size() > 0) {
      List<Map<String, Object>> loadslipList = new ArrayList<>();
      getLoadslipListFromUniqueShipments(uniqueShipments, loadslipList);
      List<CTCountry> ctCountryList = ctCountryRepository.findAllByCountryCodeIn(countryCodeList);

      for (TruckReport truckReport : truckReports) {

        truckReport.setLoadslipComments(getCommentsFromLS(loadslipList, truckReport));
        /*Setting the Additional data in truck From LS*/
        setAdditionalInfoForTruckReportFromLoadslip(loadslipList, truckReport);


        if (!StringUtils.isEmpty(truckReport.getDestCountry())) {
          Optional<CTCountry> optionalCTCountry = ctCountryList.parallelStream().filter(ctCountry -> ctCountry.getCountryCode().equals(truckReport.getDestCountry())).findFirst();
          truckReport.setDestCountryName(optionalCTCountry.get().getCountryName());
        }
        /*if role = DP_REP,L1_MGR, L2_MGR then finding the ctScaleWeightDiff for that reportLoc
         * for normal user no need to execute this IF - block as we will have only one CTScaleInvoiceWtDiff record*/
        if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
          if (ctScaleInvoiceWtDiffs != null && ctScaleInvoiceWtDiffs.size() > 0) {
            optionalCTScaleInvoiceWtDiff = ctScaleInvoiceWtDiffs.parallelStream().filter(ct -> truckReport.getReportLocation().equals(ct.getLocationId())).findFirst();
          }
        }
        if (optionalCTScaleInvoiceWtDiff != null && optionalCTScaleInvoiceWtDiff.isPresent()) {
          truckReport.setCtDiffWt(optionalCTScaleInvoiceWtDiff.get().getWeightDiff());
        }
        if (!StringUtils.isEmpty(truckReport.getRejectionCode()) && ctRejections != null && !ctRejections.isEmpty())
          ctRejections.forEach(c -> {
            if (truckReport.getRejectionCode().equals(c.getRejectionCode()))
              truckReport.setRejectionDesc(c.getRejectionDesc());
          });
        /*Setting the FT_TRIP_ID from shipment table*/
        /*Used only when filter is applied*/
        if (truckReportFilterDto.isReportedTrucksFilter() &&shipmentMapList != null && !shipmentMapList.isEmpty()){
          if (!StringUtils.isEmpty(truckReport.getShipmentID())) {
            Optional<Shipment> shipmentMap = shipmentMapList.parallelStream().filter(s -> truckReport.getShipmentID().equals(s.getShipmentId())).findFirst();
            if (shipmentMap.isPresent()){
              truckReport.setFtTripId(shipmentMap.get().getFtTripId() != null?shipmentMap.get().getFtTripId() : null);
            }
          }
        }

      }
    }
  }

  private void setAdditionalInfoForTruckReportFromLoadslip(List<Map<String, Object>> loadslipList, TruckReport truckReport) {

   List<Loadslip> loadslipsInTruck = loadslipList.stream().filter(ls -> truckReport.getShipmentID() != null && ls.get("shipmentId") != null
    && truckReport.getShipmentID().equals(ls.get("shipmentId"))).map(Loadslip::new).collect(Collectors.toList());

   int totFlap = 0;
   int totTube = 0;
   int totValve = 0;
   int totTyre = 0;
   int totPctr = 0;
   int otherQty = 0;
   int totQty = 0;

   for (Loadslip loadslip : loadslipsInTruck){
    if (loadslip != null){
      totFlap = totFlap + (loadslip.getTotFlaps() != null ? loadslip.getTotFlaps() : 0);
      totPctr = totPctr + (loadslip.getTotPctr() != null ? loadslip.getTotPctr() : 0);
      totQty = totQty + (loadslip.getTotQty() != null ? loadslip.getTotQty() : 0);
      totTube = totTube + (loadslip.getTotTubes() != null ? loadslip.getTotTubes() : 0);
      totValve = totValve + (loadslip.getTotValve() != null ? loadslip.getTotValve() : 0);
      totTyre = totTyre + (loadslip.getTotTyres() != null ? loadslip.getTotTyres() : 0);
      otherQty = otherQty + (loadslip.getOtherQty() != null ? loadslip.getOtherQty() : 0);
    }
   }

   truckReport.setTotFlaps(totFlap);
   truckReport.setTotPctr(totPctr);
   truckReport.setTotValve(totValve);
   truckReport.setTotTyres(totTyre);
   truckReport.setTotTubes(totTube);
   truckReport.setTotQty(totQty);
   truckReport.setOtherQty(otherQty);
   truckReport.setLrNum(loadslipsInTruck.stream().filter(lt -> !StringUtils.isEmpty(lt.getLrNum())  && lt.getLrNum() != null && !lt.getLrNum().equalsIgnoreCase("null")).map(Loadslip::getLrNum).collect(Collectors.joining(",")));
   truckReport.setSapInvoice(loadslipsInTruck.stream().filter(ls -> !StringUtils.isEmpty(ls.getSapInvoice()) && ls.getSapInvoice() != null).map(Loadslip::getSapInvoice).collect(Collectors.joining(",")));
   loadslipsInTruck = loadslipsInTruck.stream().filter(ls -> ls.getLrDate() != null).sorted(Comparator.comparing(Loadslip::getLrDate).reversed()).collect(Collectors.toList());
   truckReport.setLrDate(loadslipsInTruck != null && !loadslipsInTruck.isEmpty() ? loadslipsInTruck.get(0).getLrDate() : null);
  }

  private List<Shipment> getShipmentMapFromIds(List<TruckReport> truckReports) {
    List<String> uniqueShipments = truckReports.parallelStream().map(TruckReport::getShipmentID).distinct().collect(Collectors.toList());
    if (uniqueShipments != null && !uniqueShipments.isEmpty()) {
      StringBuilder sqlString = new StringBuilder("select sh.shipment_id as shipmentId, sh.FT_TRIP_ID as ftTripId from shipment sh where sh.shipment_id IS NOT NULL AND (");
      List<List<String>> partitions = ListUtils.partition(uniqueShipments, 999);
      int i = 0;
      while (i < partitions.size()) {
        sqlString.append(" sh.shipment_id IN (" + Utility.join(partitions.get(i)) + ") ");
        i = i + 1;
        if (i < partitions.size()-1) {
          sqlString.append(" OR ");
        }
      }
      for (List<String> subUniqueShipments : partitions) {
      }
      sqlString.append(" )");
      Query query = entityManager.createNativeQuery(sqlString.toString());
      List<Object[]> shipmentObjectList = query.getResultList();
      List<Shipment> shipmentList = new ArrayList<>();
      if (shipmentObjectList != null && !shipmentObjectList.isEmpty()){
        shipmentList.addAll(shipmentObjectList.parallelStream().filter(s -> s[0] != null)
            .map(s -> new Shipment(s[0].toString(), s[1] != null ? s[1].toString() : null))
            .collect(Collectors.toList()));
      }
      return shipmentList;
    }
    return null;
  }

  private String getCommentsFromLS(List<Map<String, Object>> loadslipList, TruckReport truckReport) {
    String loadslipComments = null;
    for (Map<String, Object> objectMap : loadslipList) {
      if (!StringUtils.isEmpty(truckReport) && !StringUtils.isEmpty(objectMap) && !StringUtils.isEmpty(truckReport.getShipmentID())) {
        if (truckReport.getShipmentID().contentEquals(objectMap.get("shipmentId").toString()) && !StringUtils.isEmpty(objectMap.get("comments"))) {
          loadslipComments = loadslipComments + objectMap.get("comments") + " \n";
        }
      }
    }
    return loadslipComments;
  }

  private void getLoadslipListFromUniqueShipments(List<String> uniqueShipments, List<Map<String, Object>> loadslipList) {
    List<List<String>> partitions = ListUtils.partition(uniqueShipments, 999);
    for (List<String> subUniqueShipments : partitions ){
      loadslipList.addAll(loadslipRepository.findAllShipmentIdIn(subUniqueShipments));
    }
  }

  private Page<TruckReport> setLoadslipTimestamps(Page<TruckReport> truckReportsData, ApplicationUser loggedInUser) {
    /*truckReportsData.stream().parallel().forEach(truckReport -> {
      List<Loadslip> loadslips = loadslipRepository.findAllByShipmentShipmentIdAndStatusNot(truckReport.getShipmentID(), Constants.LoadslipStatus.CANCELLED);
      if (loadslips != null && !loadslips.isEmpty()) {
        getTruckEventDatesFromLoadslip(loadslips, truckReport, loggedInUser);
      }
    });*/
    /*Optimized truck->Inventory->Shipping/Receiving*/
    /*getTruckEventDatesFromLoadslip code is also included in this method */
    List<String> shipmentIdList = truckReportsData.stream().parallel().map(truckReport -> truckReport.getShipmentID()).collect(Collectors.toList());
    List<List<String>> partitions = Lists.partition(shipmentIdList, 999);
    List<Loadslip> loadslips = new ArrayList<>();
    for (List<String> subList : partitions) {
      if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
        loadslips.addAll(loadslipRepository.findAllByShipmentShipmentIdInAndStatusNot(subList, LoadslipStatus.CANCELLED));
      } else {
        loadslips.addAll(loadslipRepository.findAllByShipmentShipmentIdInAndDestLocAndStatusNot(subList, loggedInUser.getPlantCode(), Constants.LoadslipStatus.CANCELLED));
      }
    }

    if (loadslips != null && !loadslips.isEmpty()) {
      truckReportsData.stream().forEach(truckReport -> {
        if ((truckReport.getShipmentID() != null)) {
          loadslips.parallelStream().forEach(loadslip -> {
            //provide unloading start and end dates when the loadslip belongs to current loggedIn user location
            if ((truckReport.getShipmentID().equalsIgnoreCase(loadslip.getShipment().getShipmentId())) && (truckReport.getReportLocation().equals(loadslip.getDestLoc()))) {
              if (!StringUtils.isEmpty(loadslip.getUsDate()))
                truckReport.setLoadslipUSDate(DateUtils.formatDate(loadslip.getUsDate(), Constants.DATE_TIME_FORMAT));
              if (!StringUtils.isEmpty(loadslip.getUeDate()))
                truckReport.setLoadslipUEDate(DateUtils.formatDate(loadslip.getUeDate(), Constants.DATE_TIME_FORMAT));
//    truckReport.setReleaseDate(DateUtils.formatDate(loadslip.getReleaseDate(), Constants.DATE_TIME_FORMAT));
            }
          });
        }
      });
    }
    return truckReportsData;
  }

  private TruckReport getTruckEventDatesFromLoadslip(List<Loadslip> loadslips, TruckReport truckReport, ApplicationUser loggedInUser) {
    loadslips.parallelStream().forEach(loadslip -> {
      //provide unloading start and end dates when the loadslip belongs to current loggedIn user location
      if (loadslip.getDestLoc().equals(loggedInUser.getPlantCode())) {
        truckReport.setLoadslipUSDate(DateUtils.formatDate(loadslip.getUsDate(), Constants.DATE_TIME_FORMAT));
        truckReport.setLoadslipUEDate(DateUtils.formatDate(loadslip.getUeDate(), Constants.DATE_TIME_FORMAT));
//    truckReport.setReleaseDate(DateUtils.formatDate(loadslip.getReleaseDate(), Constants.DATE_TIME_FORMAT));
      }
    });
    return truckReport;
  }

  private Page<TruckReport> getShipmentAndReceivingTrucksByActivity(TruckReportFilterDto truckReportFilterDto, List<TruckReportStatus> truckReportStatuses, ApplicationUser applicationUser) {
    Page<TruckReport> truckReports = null;
    List<String> truckStatusList = truckReportStatuses.parallelStream().map(TruckReportStatus::name).collect(Collectors.toList());
    Page<Map<String, Object>> truckReportMap = null;
    if (!truckReportFilterDto.isReportedTrucksFilter()) {
      // If activity is P means SHIPMENT Trucks
      if (truckReportFilterDto.getActivity().equalsIgnoreCase("P")) {
        // fetch trucks having activity as P or NULL  as SHIPMENT Trucks
        /*Commented because it is takin lot of time to fetch records*/
        /*truckReports = truckReportRepo.findTruckReportsWithActivityP(
            truckReportFilterDto.getReportLocation(), truckReportStatuses, truckReportFilterDto.getActivity(), PageRequest.of(truckReportFilterDto.getIndex(),
                truckReportFilterDto.getPageLength(), Sort.Direction.DESC, "reportDate"));*/

        /*Optimized Code for trucks-info Shipping*/
        /*DP_REP, L1_MGR, L2_MGR*/
        if (UserRole.getDPREPAccessRoles().contains(applicationUser.getRole())) {
          truckReportMap = truckReportRepo.findTruckReportsWithActivityPNativeDPREP(truckStatusList, truckReportFilterDto.getActivity(), PageRequest.of(truckReportFilterDto.getIndex(),
              truckReportFilterDto.getPageLength(), Sort.Direction.DESC, "REPORTING_DATE"));
        } else {
          truckReportMap = truckReportRepo.findTruckReportsWithActivityPNative(truckReportFilterDto.getReportLocation(), truckStatusList, truckReportFilterDto.getActivity(), PageRequest.of(truckReportFilterDto.getIndex(),
              truckReportFilterDto.getPageLength(), Sort.Direction.DESC, "REPORTING_DATE"));
        }
      }
      // If activity is D means RECEIVING Trucks
      else if (truckReportFilterDto.getActivity().equalsIgnoreCase("D")) {
        // fetch trucks having activity as D  as RECEIVING Trucks
        /*Commented because it is takin lot of time to fetch records*/
        /*findAllByReportLocationAndStatusInAndActivityOrderByReportDateAsc*/

        //Optimized code for trucks-info Receiving
        /*DP_REP, L1_MGR, L2_MGR*/
        if (UserRole.getDPREPAccessRoles().contains(applicationUser.getRole())) {
          truckReportMap = truckReportRepo.findAByReportLocationAndStatusInAndActivityOrderByReportDateAscDPREP(truckStatusList, truckReportFilterDto.getActivity(), PageRequest.of(truckReportFilterDto.getIndex(),
              truckReportFilterDto.getPageLength(), Sort.Direction.DESC, "REPORTING_DATE"));
        } else {
          truckReportMap = truckReportRepo.findAByReportLocationAndStatusInAndActivityOrderByReportDateAsc(
              truckReportFilterDto.getReportLocation(), truckStatusList, truckReportFilterDto.getActivity(), PageRequest.of(truckReportFilterDto.getIndex(),
                  truckReportFilterDto.getPageLength(), Sort.Direction.DESC, "REPORTING_DATE"));
        }
      }
    } else {
      // Filter Trucks
      truckReports = filterService.filterTrucksInfo(truckReportFilterDto, truckReportStatuses, null, applicationUser, null);
    }
    if (!StringUtils.isEmpty(truckReportMap)) {
      List<TruckReport> mappedTruckReport = truckReportMap.stream().parallel().map(TruckReport::new).collect(Collectors.toList());
      truckReports = new PageImpl<>(mappedTruckReport, PageRequest.of(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength()), truckReportMap.getTotalElements());
    }
    return truckReports;

  }

  @Override
  public ApiResponse updateLoadslipEvent(LoadslpEventDto loadslpEventDto, ApplicationUser loggedInUser,
                                         HttpServletRequest request) {
    Optional<Loadslip> loadslip = loadslipRepository.findById(loadslpEventDto.getLoadslipID());
    String validationMsg = Utility.validateExistingLoadslip(loadslip);
    String viewMessage = "Loadslip is updated ";
    if (validationMsg != null) {
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format(validationMsg + " with the loadslip Id %s", loadslpEventDto.getLoadslipID()));
    }
    Loadslip lSlip = loadslip.get();

    LoadslipEvent loadslipEvent = LoadslipEvent.valueOf(loadslpEventDto.getEventType());
    switch (loadslipEvent) {
      case ARRIVED_BAY:
        lSlip.setArrivedDate(Utility.currentTimestamp());
        if (isUpdateStatusAllowed(lSlip)) {
          if (lSlip.getShipment() != null && lSlip.getShipment().getTruckNumber() != null) {
            updateTruckStatus(lSlip, null, BayStatus.ARVD, loggedInUser);
          }
        }
        lSlip.setUpdateUser(loggedInUser.getUserId());
        lSlip.setUpdateDate(new Date());
        loadslipRepository.save(lSlip);
        break;
      case LOADING_START:
        lSlip.setLsDate(Utility.currentTimestamp());
       if (isUpdateStatusAllowed(lSlip)){
         lSlip.setStatus(LoadslipStatus.LOADING);
         if (lSlip.getShipment() != null && lSlip.getShipment().getTruckNumber() != null) {
           updateTruckStatus(lSlip, TruckReportStatus.LOADING, BayStatus.LSPB, loggedInUser);
         }
       }
        lSlip.setUpdateUser(loggedInUser.getUserId());
        lSlip.setUpdateDate(new Date());
        loadslipRepository.save(lSlip);
        break;
      case LOADING_END:
        lSlip.setLeDate(Utility.currentTimestamp());
        if (isUpdateStatusAllowed(lSlip)){
          lSlip.setStatus(LoadslipStatus.LOADED);
          if (lSlip.getShipment() != null && lSlip.getShipment().getTruckNumber() != null) {
            updateTruckStatus(lSlip, TruckReportStatus.LOADED, BayStatus.LEPB, loggedInUser);
          }
        }
        lSlip.setUpdateUser(loggedInUser.getUserId());
        lSlip.setUpdateDate(new Date());
        loadslipRepository.save(lSlip);
        break;
      case CONFIRM:
        return sendLoadSlipToSAP(lSlip, loggedInUser);
      case RELEASED:
        if (!Constants.DelInvType.isExportOrJIT(lSlip.getLoadslipType())) {
          Map<String, Object> result = loadslipRepository.checkEqualityInvoiceQtyAndLoadedQty(loadslpEventDto.getLoadslipID());
          int totInvQty = Integer.parseInt(result.get("TOT_INV_QTY").toString());
          int totLsQty = Integer.parseInt(result.get("TOT_LS_QTY").toString());
          if (totInvQty == 0 && totLsQty == 0) {
            return new ApiResponse(HttpStatus.BAD_REQUEST, String.format("Total loaded quantity and total invoice quantity of the loadslip %s should not be zero", lSlip.getLoadslipId()));
          } else if (totInvQty != totLsQty) {
            return new ApiResponse(HttpStatus.BAD_REQUEST, "Not allowed to release the truck because total loaded quantity and total invoice quantity are not equal");
          }
        }
        /*Check if a trcuk can be released or not.
         * In case of single LS then truck canbe RELEASED directly
         * For MULTI-LS senarion all Other LS on truck should to RELEASED(i.e., ls.release_date should not be null)*/
        if (lSlip.getShipment() != null && lSlip.getShipment().getTruckNumber() != null) {
          lSlip.setReleaseDate(Utility.currentTimestamp());
          lSlip.setUpdateUser(loggedInUser.getUserId());
          lSlip.setUpdateDate(new Date());
          if (isLoadslipRelease(lSlip.getShipment().getShipmentId(), lSlip.getSourceLoc(), lSlip.getStatus().name(), lSlip.getLoadslipId())) {
            TruckReport truckReport = updateTruckStatus(lSlip, null, BayStatus.RELEASE, loggedInUser);
//            if (truckReport == null || TruckReportStatus.REPORTED.equals(truckReport.getStatus())){
//              return new ApiResponse(HttpStatus.NOT_FOUND, String.format("You cannot Release the truck before GateIn"));
//            }
          }
          loadslipRepository.save(lSlip);

          //eWay bill url integration
          viewMessage = eWayBillIntegration(lSlip.getLoadslipId(), viewMessage);

        } else
          return new ApiResponse(HttpStatus.NOT_FOUND, String.format("No trucks found with loadslip %s", lSlip.getLoadslipId()));
        break;
      case RETRY:

          if (lSlip.getShipment() != null && lSlip.getShipment().getTruckNumber() != null) {
            updateTruckStatus(lSlip, null, null, loggedInUser);
            return new ApiResponse(HttpStatus.OK, String.format("Driver Consent Retriggered!"));
          } else {
            return new ApiResponse(HttpStatus.NOT_FOUND, String.format("No Truck Assigned! Cannot Initiate Consent"));
          }

    }
    return new ApiResponse(HttpStatus.OK, viewMessage, getLoadslipDetails(lSlip.getLoadslipId(), loggedInUser));
  }

  private boolean isUpdateStatusAllowed(Loadslip lSlip) {
    if (!StringUtils.isEmpty(lSlip.getLoadslipType()) && (lSlip.getLoadslipType().equals("FGS_EXP") || lSlip.getLoadslipType().startsWith("JIT"))){
      if (lSlip.getReleaseDate() != null){
        return false;
      }else {
        return  true;
      }
    }else {
      if (lSlip.getConfirmDate() != null){
        return false;
      }else {
        return true;
      }
    }
  }

  private String eWayBillIntegration(String loadslipId, String viewMessage) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(Constants.API_AUTH, Utility.convertStringBase64Format(sapUserName + ":" + sapPassword));
    headers.set(Constants.LOAD_SLIP_ID, loadslipId);
    HttpEntity httpEntity = new HttpEntity<>(headers);
    ResponseEntity responseEntity = restTemplate.postForEntity(eWayBillURL, httpEntity, ResponseEntity.class);
    if (responseEntity.getStatusCodeValue() != 200) {
      viewMessage = "Loadslip is updated but e-way bill integration failed";
    }
    return viewMessage;
  }

  private boolean isLoadslipRelease(String shipmentId, String source, String Status, String currentLoadslipId) {
//    Fetching all the LS on shipment.
//    If there is only one LS in shipment then it can be released.
//    If there are multiple LS on truck, then All the other LS (Except the currentLs) should have RELEASE_DATE and its STATUS SHOULD be SENT_SAP
    List<Loadslip> loadslips = loadslipRepository.findAllByShipmentShipmentIdAndSourceLocAndStatusNot(shipmentId, source, LoadslipStatus.CANCELLED);
    if (loadslips != null && !loadslips.isEmpty()) {
      if (loadslips.size() == 1) {
        return true;
      } else {
        for (Loadslip loadslip : loadslips) {
//          ls.id != currentLs AND ls.release_date is null then truck is not allowed to release
          if (!loadslip.getLoadslipId().equals(currentLoadslipId) && StringUtils.isEmpty(loadslip.getReleaseDate())) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private TruckReport updateTruckStatus(Loadslip lSlip, TruckReportStatus truckStatus, BayStatus bayStatus, ApplicationUser loggedInUser) {
    // Now Fetching truck report using truck number and Shipment id which saved in truck_reporting when create loadslip
    Optional<TruckReport> optionalTruckReport = truckReportRepo.findByTruckTruckNumberAndShipmentIDAndReportLocation(lSlip.getShipment().getTruckNumber(), lSlip.getShipment().getShipmentId(), loggedInUser.getPlantCode());
    if (optionalTruckReport.isPresent()) {
      if (!StringUtils.isEmpty(truckStatus)) {
        optionalTruckReport.get().setStatus(truckStatus);
      }
      if (!StringUtils.isEmpty(bayStatus)) {
        optionalTruckReport.get().setBayStatus(bayStatus);
      }
      //setting truck tracking Consent Status to RETRY
      if (StringUtils.isEmpty(truckStatus) && StringUtils.isEmpty(bayStatus)){
        optionalTruckReport.get().setTrackingConsentStatus("RETRY");
      }
      optionalTruckReport.get().setUpdateUser(loggedInUser.getUserId());
      optionalTruckReport.get().setUpdateDate(new Date());
      return truckReportRepo.save(optionalTruckReport.get());
    }
    return null;
  }

  private ApiResponse sendLoadSlipToSAP(Loadslip loadslip, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(loadslip.getShipment().getTruckNumber())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please assign a truck to loadslip before confirm");
    }
    HttpHeaders headers = new HttpHeaders();
    headers.set(Constants.API_AUTH, Utility.convertStringBase64Format(sapUserName + ":" + sapPassword));
    headers.set(Constants.LOAD_SLIP_ID, loadslip.getLoadslipId());
    HttpEntity httpEntity = new HttpEntity<>(headers);
    ResponseEntity responseEntity = restTemplate.postForEntity(sendLsToSAPURL, httpEntity, ResponseEntity.class);
    if (responseEntity.getStatusCodeValue() == 200) {
      Optional<Loadslip> optional = loadslipRepository.findByLoadslipId(loadslip.getLoadslipId());
      if (!StringUtils.isEmpty(optional.get().getStoSoNum())) {
        return new ApiResponse(HttpStatus.OK, "Already SO/STO is created  with this loadslip_id =" + loadslip.getLoadslipId());
      }
      // Error response incase of SO/STO creation failed
      if (!responseEntity.getHeaders().containsKey(Constants.SAP_RESPONSE) || responseEntity.getHeaders().get(Constants.SAP_RESPONSE).get(0).equalsIgnoreCase("ERROR")) {
        return new ApiResponse(HttpStatus.OK, "SO/STO creation failed ");
      }
      for (LoadslipDetail loadslipDetail : loadslip.getLoadslipDetails()) {
//      loadslip.getLoadslipDetails().parallelStream().forEach(loadslipDetail -> {
        int loadslipLoadedQty = loadslipDetail.getLoadQty();
        boolean isLoadedQtyAvailable = true; // loadslipLoadedQty is having some qunatity > 0
        List<DispatchPlanItemInfo> dispatchPlanItemInfos = fetchDispatchPlansByLoadslipItem(loadslipDetail, loggedInUser, loadslip.getDestLoc());
        for (DispatchPlanItemInfo dispatchPlan : dispatchPlanItemInfos) {
          if (isLoadedQtyAvailable) {
            int planReservedQty = dispatchPlan.getReservedQuantity();
            int planLoadedQty = dispatchPlan.getLoadedQty() != null ? dispatchPlan.getLoadedQty() : 0;
            // Dispatch plan reserved qty is more than or equal to loadslip quantity
            if (planReservedQty >= loadslipLoadedQty) {
              planReservedQty = planReservedQty - loadslipLoadedQty;
              planLoadedQty += loadslipLoadedQty;
              loadslipLoadedQty = 0;
              isLoadedQtyAvailable = false;
            } else {
              planLoadedQty += planReservedQty;
              loadslipLoadedQty = loadslipLoadedQty - planReservedQty;
              planReservedQty = 0;
            }
            dispatchPlan.setReservedQuantity(planReservedQty);
            dispatchPlan.setLoadedQty(planLoadedQty);
          }
        }
        dispatchPlanItemInfoRepository.saveAll(dispatchPlanItemInfos);
//      });
      }
      // From SAP, they are updating LS status as SENT_SAP and Confrim date in DB
      Optional<Loadslip> optionalupdatedtLS = loadslipRepository.findById(loadslip.getLoadslipId());
      if (optionalupdatedtLS.isPresent()) {
        loadslip = optionalupdatedtLS.get();
        loadslip.setConfirmDate(Utility.currentTimestamp());
        loadslip.setStatus(LoadslipStatus.SENT_SAP);
        loadslip.setStoSoNum(responseEntity.getHeaders().get(Constants.SAP_RESPONSE).get(0));
        loadslip.setIntegrationStatus("SUCCESS");
        loadslip.setUpdateUser(loggedInUser.getUserId());
        loadslip.setUpdateDate(new Date());
        loadslip = loadslipRepository.save(loadslip);
      }
      Optional<Shipment> optionalShipment = shipmentRepository.findById(loadslip.getShipment().getShipmentId());
      if (optionalShipment.isPresent()) {
        optionalShipment.get().setStartTime(loadslip.getConfirmDate());
        shipmentRepository.save(optionalShipment.get());
      }
      // Success response incase of SO/STO created
      return new ApiResponse(HttpStatus.OK, String.format("SO/STO with %s generated successfully", responseEntity.getHeaders().get(Constants.SAP_RESPONSE).get(0)),
          getLoadslipDetails(loadslip.getLoadslipId(), loggedInUser));
    }
    //API failure case
    return new ApiResponse(responseEntity.getStatusCode(), responseEntity.getHeaders().get(Constants.SAP_API_RESPONSE).get(0));
  }

  @Override
  public ApiResponse updatePlanDispatchQtyFromLoadedQty(StosoLoadslipBean loadslipBean) {

    String userId = loadslipBean.getUsername();
    String password = (loadslipBean.getPassword());
    String loadslipId = loadslipBean.getLoadslipId();
    if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(password) || StringUtils.isEmpty(loadslipId))
      return new ApiResponse(HttpStatus.NOT_FOUND, "loadslip Id,userId and password can't be empty");
    Optional<ApplicationUser> user = userRepository.findOneByUserIdIgnoreCase(userId);
    if (!user.isPresent())
      return new ApiResponse(HttpStatus.FORBIDDEN, String.format("No user found with Id %s", userId));
    byte[] base64Password = Base64.getEncoder().encode((userId + ":" + password).getBytes());
    if (!(new String(base64Password).equals(user.get().getPassword())))
      return new ApiResponse(HttpStatus.FORBIDDEN, "Invalid credentials !");
    Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(loadslipId);
    if (!optionalLoadslip.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("No loadslip Id found with id %s", loadslipId));
    }
    Loadslip loadslip = optionalLoadslip.get();
    if (!loadslip.getInsertUser().equals(user.get().getUserId()))
      throw new UnAuthorisedException("You are not authorized");
    if (StringUtils.isEmpty(loadslip.getStoSoNum()))
      throw new InvalidException(format("No STO/SO available for the loadslip %s", loadslip.getLoadslipId()));

    List<StosoItemBean> requestItemList = new ArrayList<>();

    //grouping items comming from request
    Map<String, Map<String, Integer>> itemQtymap = loadslipBean.getItemsData().stream().collect(Collectors.groupingBy(StosoItemBean::getItemId, Collectors.groupingBy(StosoItemBean::getBatchCode, Collectors.summingInt(StosoItemBean::getQuantity))));

    itemQtymap.entrySet().forEach(items -> {
      items.getValue().entrySet().forEach(item -> {
        StosoItemBean bean = new StosoItemBean();
        bean.setItemId(items.getKey());
        bean.setBatchCode(item.getKey());
        bean.setQuantity(item.getValue());
        bean.setDetailQty(loadslip.getLoadslipDetails().stream().filter(detail -> (detail.getLoadslipDetailId().getItemId().equals(items.getKey())) && (detail.getBatchCode().equals(item.getKey()))).mapToInt(LoadslipDetail::getLoadQty).sum());
        bean.setLoadslipDetails(loadslip.getLoadslipDetails().stream().filter(detail -> (detail.getLoadslipDetailId().getItemId().equals(items.getKey())) && (detail.getBatchCode().equals(item.getKey()))).collect(Collectors.toList()));
        bean.setLoadslipId(loadslipBean.getLoadslipId());
        bean.setInvoiceNum(loadslipBean.getInvoiceNum());
        if (bean.getLoadslipDetails().size() > 0) {
          requestItemList.add(bean);
        }
      });
    });

    if (loadslipBean.isToDispatch()) {
      if (loadslipBean.getItemsData().size() > 0) {
        return toDispatchFromLoadedQty(requestItemList, loadslip, user.get(), loadslipBean.getInvoiceNum());
      } else {
        return updateDispachQty(loadslip.getLoadslipDetails(), user.get(), loadslip);
      }
    } else {
      if (loadslipBean.getItemsData().size() > 0) {
        return toLoadFromDispatchQty(requestItemList, loadslip, user.get(), loadslipBean.getInvoiceNum());
      } else {
        return updateLoadedQty(loadslip.getLoadslipDetails(), user.get(), loadslip);
      }
    }
  }


  private ApiResponse updateLoadedQty(List<LoadslipDetail> loadslipDetails, ApplicationUser user, Loadslip loadslip) {
    Map<String, String> errorItemMap = null;
    List<String> errors = new ArrayList<>();
    //filtring the item ids and batch codes which are already dispatched / not dispatched
    errorItemMap = loadslipDetails.stream().filter(loadslipDetail -> loadslipDetail.getIsLoaded() != null).filter(ld -> ld.getIsLoaded().equalsIgnoreCase("N")).collect(Collectors.toMap(ld -> ld.getLoadslipDetailId().getItemId(), LoadslipDetail::getBatchCode));

    if (errorItemMap.size() > 0) {
      errorItemMap.entrySet().forEach(error -> {
        errors.add(format("Load quantities's of item %s, batch code %s not yet dispatched", error.getKey(), error.getValue()));
      });
      return new ApiResponse(HttpStatus.BAD_REQUEST, "", errors);
    }

    for (LoadslipDetail loadslipDetail : loadslipDetails) {
      int loadslipDispatchedQty = loadslipDetail.getLoadQty();
      boolean isDispatchedQtyAvailable = true;
      List<DispatchPlanItemInfo> dispatchPlanItemInfos = fetchDispatchPlansByLoadslipItem(loadslipDetail, user, loadslip.getDestLoc());
      for (DispatchPlanItemInfo dispatchPlan : dispatchPlanItemInfos) {
        if (isDispatchedQtyAvailable) {
          int planLoadedQty = dispatchPlan.getLoadedQty() != null ? dispatchPlan.getLoadedQty() : 0;
          int planDispatchedQty = dispatchPlan.getDispatchedQuantity() != null ? dispatchPlan.getDispatchedQuantity() : 0;
          if (planDispatchedQty >= loadslipDispatchedQty) {
            planDispatchedQty = planDispatchedQty - loadslipDispatchedQty;
            planLoadedQty += loadslipDispatchedQty;
            loadslipDispatchedQty = 0;
            isDispatchedQtyAvailable = false;
          } else {
            planLoadedQty += planDispatchedQty;
            loadslipDispatchedQty = loadslipDispatchedQty - planDispatchedQty;
            planDispatchedQty = 0;
          }
          dispatchPlan.setLoadedQty(planLoadedQty);
          dispatchPlan.setDispatchedQuantity(planDispatchedQty);
        }
        loadslipDetail.setIsLoaded("N");
      }
      dispatchPlanItemInfoRepository.saveAll(dispatchPlanItemInfos);
    }
    loadslipDetailRepository.saveAll(loadslipDetails);
    return new ApiResponse(HttpStatus.OK, String.format("Invoices of the loadslip %s cancelled successfully", loadslip.getLoadslipId()));
  }

  //updating dispatch quantities
  private ApiResponse updateDispachQty(List<LoadslipDetail> loadslipDetails, ApplicationUser user, Loadslip loadslip) {

    Map<String, String> errorItemMap = null;
    List<String> errors = new ArrayList<>();
    //filtring the item ids and batch codes which are already dispatched / not dispatched
    errorItemMap = loadslipDetails.stream().filter(loadslipDetail -> loadslipDetail.getIsLoaded() != null).filter(ld -> ld.getIsLoaded().equalsIgnoreCase("Y")).collect(Collectors.toMap(ld -> ld.getLoadslipDetailId().getItemId(), LoadslipDetail::getBatchCode));

    if (errorItemMap.size() > 0) {
      errorItemMap.entrySet().forEach(error -> {
        errors.add(String.format("Load quantities's of item %s, batch code %s already dispatched", error.getKey(), error.getValue()));
      });
      return new ApiResponse(HttpStatus.BAD_REQUEST, "", errors);
    }

    for (LoadslipDetail loadslipDetail : loadslipDetails) {
      int loadslipDispatchQty = loadslipDetail.getLoadQty();
      boolean isLoadedQtyAvailable = true; // loadslipLoadedQty is having some qunatity > 0
      List<DispatchPlanItemInfo> dispatchPlanItemInfos = fetchDispatchPlansByLoadslipItem(loadslipDetail, user, loadslip.getDestLoc());
      for (DispatchPlanItemInfo dispatchPlan : dispatchPlanItemInfos) {
        if (isLoadedQtyAvailable) {
          int planLoadedQty = dispatchPlan.getLoadedQty() != null ? dispatchPlan.getLoadedQty() : 0;
          int planDispatchedQty = dispatchPlan.getDispatchedQuantity() != null ? dispatchPlan.getDispatchedQuantity() : 0;
          if (planLoadedQty >= loadslipDispatchQty) {
            planLoadedQty = planLoadedQty - loadslipDispatchQty;
            planDispatchedQty += loadslipDispatchQty;
            loadslipDispatchQty = 0;
            isLoadedQtyAvailable = false;
          } else {
            planDispatchedQty += planLoadedQty;
            loadslipDispatchQty = loadslipDispatchQty - planLoadedQty;
            planLoadedQty = 0;
          }
          dispatchPlan.setDispatchedQuantity(planDispatchedQty);
          dispatchPlan.setLoadedQty(planLoadedQty);
        }
      }
      dispatchPlanItemInfoRepository.saveAll(dispatchPlanItemInfos);
      loadslipDetail.setIsLoaded("Y");
    }
    loadslipDetailRepository.saveAll(loadslipDetails);
    return new ApiResponse(HttpStatus.OK, format("Dispatched  quantities of  loadslip %s are updated ", loadslip.getLoadslipId()));
  }


  private ApiResponse toDispatchFromLoadedQty(List<StosoItemBean> itemBeans, Loadslip loadslip, ApplicationUser user, String invNum) {

    for (StosoItemBean stosoItemBean : itemBeans) {
      List<LoadslipInvoiceLine> invoiceLines = invoiceLineRepo.findByLoadslipInvLineIdLoadslipIdAndLoadslipInvLineIdInvoiceNumNotAndLoadslipInvLineIdItemIdAndBatchCode
          (stosoItemBean.getLoadslipId(), stosoItemBean.getInvoiceNum(), stosoItemBean.getItemId(), stosoItemBean.getBatchCode());
      int previousInvQty = invoiceLines.stream().mapToInt(LoadslipInvoiceLine::getQuantity).sum();
      int newInvQty = stosoItemBean.getQuantity();
      int loadslipDispatchQty = 0;
      if (stosoItemBean.getLoadslipDetails().stream().anyMatch(ld -> ld.getIsLoaded().equals("N"))) {
        if ((previousInvQty + newInvQty) > stosoItemBean.getDetailQty()) {
          loadslipDispatchQty = stosoItemBean.getDetailQty() - previousInvQty;
        } else {
          loadslipDispatchQty = stosoItemBean.getQuantity();
        }
      }
      boolean isLoadedQtyAvailable = true;
      LoadslipDetail loadslipDetail = stosoItemBean.getLoadslipDetails().stream().findFirst().get();
      List<DispatchPlanItemInfo> dispatchPlanItemInfos = fetchDispatchPlansByLoadslipItem(loadslipDetail, user, loadslip.getDestLoc());
      for (DispatchPlanItemInfo dispatchPlan : dispatchPlanItemInfos) {
        if (isLoadedQtyAvailable) {
          int planLoadedQty = dispatchPlan.getLoadedQty() != null ? dispatchPlan.getLoadedQty() : 0;
          int planDispatchedQty = dispatchPlan.getDispatchedQuantity() != null ? dispatchPlan.getDispatchedQuantity() : 0;
          if (planLoadedQty >= loadslipDispatchQty) {
            planLoadedQty = planLoadedQty - loadslipDispatchQty;
            planDispatchedQty += loadslipDispatchQty;
            loadslipDispatchQty = 0;
            isLoadedQtyAvailable = false;
          } else {
            planDispatchedQty += planLoadedQty;
            loadslipDispatchQty = loadslipDispatchQty - planLoadedQty;
            planLoadedQty = 0;
          }
          dispatchPlan.setDispatchedQuantity(planDispatchedQty);
          dispatchPlan.setLoadedQty(planLoadedQty);
        }
      }
      dispatchPlanItemInfoRepository.saveAll(dispatchPlanItemInfos);
      stosoItemBean.getLoadslipDetails().forEach(detail -> {
        detail.setIsLoaded("N");
      });
      //updating is loaded flag
      if ((previousInvQty + newInvQty) >= stosoItemBean.getDetailQty()) {
        for (LoadslipDetail detail : stosoItemBean.getLoadslipDetails()) {
          detail.setIsLoaded("Y");
        }
      } else {
        int remainingInvQty = stosoItemBean.getQuantity() + previousInvQty;
        for (LoadslipDetail detail : stosoItemBean.getLoadslipDetails()) {
          if (detail.getLoadQty() <= remainingInvQty) {
            detail.setIsLoaded("Y");
            remainingInvQty = remainingInvQty - detail.getLoadQty();
          }
        }
      }
      loadslipDetailRepository.saveAll(stosoItemBean.getLoadslipDetails());
    }
    return new ApiResponse(HttpStatus.OK, String.format("quantities of invoice %s of laodslip %s are updated successfully ", invNum, loadslip.getLoadslipId()));
  }

  private ApiResponse toLoadFromDispatchQty(List<StosoItemBean> itemBeans, Loadslip loadslip, ApplicationUser user, String invNum) {

    for (StosoItemBean stosoItemBean : itemBeans) {
      List<LoadslipInvoiceLine> invoiceLines = invoiceLineRepo.findByLoadslipInvLineIdLoadslipIdAndLoadslipInvLineIdInvoiceNumNotAndLoadslipInvLineIdItemIdAndBatchCode
          (stosoItemBean.getLoadslipId(), stosoItemBean.getInvoiceNum(), stosoItemBean.getItemId(), stosoItemBean.getBatchCode());
      int remainInvQty = invoiceLines.stream().mapToInt(LoadslipInvoiceLine::getQuantity).sum();

      int loadslipDispatchedQty = 0;
      if ((remainInvQty + stosoItemBean.getQuantity()) < stosoItemBean.getDetailQty()) {
        loadslipDispatchedQty = stosoItemBean.getQuantity();
      } else {
        loadslipDispatchedQty = stosoItemBean.getDetailQty() - remainInvQty;
      }
      if (loadslipDispatchedQty > 0) {
        boolean isDispatchedQtyAvailable = true;
        LoadslipDetail loadslipDetail = stosoItemBean.getLoadslipDetails().get(0);
        List<DispatchPlanItemInfo> dispatchPlanItemInfos = fetchDispatchPlansByLoadslipItem(loadslipDetail, user, loadslip.getDestLoc());
        for (DispatchPlanItemInfo dispatchPlan : dispatchPlanItemInfos) {
          if (isDispatchedQtyAvailable) {
            int planLoadedQty = dispatchPlan.getLoadedQty() != null ? dispatchPlan.getLoadedQty() : 0;
            int planDispatchedQty = dispatchPlan.getDispatchedQuantity() != null ? dispatchPlan.getDispatchedQuantity() : 0;
            if (planDispatchedQty >= loadslipDispatchedQty) {
              planDispatchedQty = planDispatchedQty - loadslipDispatchedQty;
              planLoadedQty += loadslipDispatchedQty;
              loadslipDispatchedQty = 0;
              isDispatchedQtyAvailable = false;
            } else {
              planLoadedQty += planDispatchedQty;
              loadslipDispatchedQty = loadslipDispatchedQty - planDispatchedQty;
              planDispatchedQty = 0;
            }
            dispatchPlan.setLoadedQty(planLoadedQty);
            dispatchPlan.setDispatchedQuantity(planDispatchedQty);
          }

        }
        dispatchPlanItemInfoRepository.saveAll(dispatchPlanItemInfos);
        stosoItemBean.getLoadslipDetails().forEach(detail -> {
          detail.setIsLoaded("N");
        });
        if (remainInvQty > 0) {
          if (remainInvQty > stosoItemBean.getDetailQty()) {
            for (LoadslipDetail detail : stosoItemBean.getLoadslipDetails()) {
              detail.setIsLoaded("Y");
            }
          } else {
            for (LoadslipDetail detail : stosoItemBean.getLoadslipDetails()) {
              if (detail.getLoadQty() <= remainInvQty) {
                detail.setIsLoaded("Y");
                remainInvQty = remainInvQty - detail.getLoadQty();
              }
            }
          }
        }
        loadslipDetailRepository.save(loadslipDetail);
      }
    }
    return new ApiResponse(HttpStatus.OK, String.format("Invoice %s of the loadslip %s is cancelled successfully", invNum, loadslip.getLoadslipId()));
  }

  @Override
  public ApiResponse getLoadslipDetails(String loadslipID, ApplicationUser loggedInUser) {
    Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(loadslipID);
    if (!optionalLoadslip.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("NO loadslip found with loadslip id %s", loadslipID));
    }

    List<LoadslipDraftDataDto> loadslipDataList = new ArrayList<>();
    LoadslipDraftDto draftDto = new LoadslipDraftDto();
    Object avlQty;
    Object valveCompQty;
    Object tubeCompQty;
    Object flapCompQty;
    Object itemCategory;
    Object priority;
    String itemClassification;
    Loadslip loadslip = optionalLoadslip.get();

    if (loadslip.getLoadslipDetails().isEmpty()) {
      setLoadslipData(loadslip, draftDto, loggedInUser);
      return new ApiResponse(HttpStatus.OK, "", draftDto);
    }
    List<String> itemIds = loadslip.getLoadslipDetails().parallelStream()
        .map(LoadslipDetail::getLoadslipDetailId).map(LoadslipDetailId::getItemId)
        .collect(Collectors.toList());

    /*When source is ext_warehouse and destination is not 1007 then get the LinkedPlant code from source*/
//    String actualSource = utilityService.getLinkedPlntLoc(loggedInUser.getPlantCode(), loadslip.getDestLoc());
    String actualSource = utilityService.getLinkedPlntLoc(loadslip.getSourceLoc(), loadslip.getDestLoc());

/*    List<Map<String, Object>> dispatchPlans = dispatchPlanItemInfoRepository
        .findApprovedPlansByDestination(loggedInUser.getPlantCode(), loadslip.getDestLoc(), itemIds);*/
    List<Map<String, Object>> dispatchPlans = dispatchPlanItemInfoRepository
        .findApprovedPlansByDestination(actualSource, loadslip.getDestLoc(), itemIds);
    /*to show the selected item categories during after loadslip creation */
		/*Set<String> selectedItemCategories = new HashSet<>();
		for (Map<String,Object> map : dispatchPlans){
			selectedItemCategories.add(map.get("item_category").toString());
		}
		draftDto.setSelectedItemCategories(selectedItemCategories);*/
    DelInvType type = null;
    if (loadslip.getLoadslipType().equals(DelInvType.FGS_EXP.name())) {
      type = DelInvType.FGS_EXP;
    } else if (loadslip.getLoadslipType().equals(DelInvType.JIT_OEM.name())) {
      type = DelInvType.JIT_OEM;
    }
    /*
    * Elr flag is not required for export and jit
    * */
    if(StringUtils.isEmpty(type)){
      //setting elr flag
      String elrFlag = loadslipRepository.getElrFlag(loadslip.getLoadslipId());
      draftDto.setElrFlag(elrFlag != null && (elrFlag.equalsIgnoreCase("Y")));
      draftDto.setElrNumber(loadslip.getLrNum());
      draftDto.setElrDate(loadslip.getLrDate() != null ? DateUtils.formatDate(loadslip.getLrDate(), Constants.DATE_TIME_FORMAT) : null);
    }
    //Check if it is in transist
    draftDto.setIsInDraft(!loadslip.getStatus().equals(LoadslipStatus.INTRANSIT) && !loadslip.getStatus().equals(LoadslipStatus.COMPLETED) && !loadslip.getStatus().equals(LoadslipStatus.CANCELLED));

    // Commented this becoz we are fetching the DELINVHEADER details using LSUtilizationExport API.
    // This is data is not used anywhere in UI
  
    /*    if (loadslip.getLoadslipId() != null) {
      List<String> exportInvoiceList = findExportInvoices(type, loggedInUser.getPlantCode(), loadslip.getDestLoc(), loadslip.getLoadslipId(), loggedInUser);
      if (!exportInvoiceList.isEmpty()) {
        draftDto.setDelInvHeaderList(delInvHeaderRepository.findByInvoiceNumberInOrderByInsertDateDesc(exportInvoiceList));
        List<String> containerList = delInvHeaderRepository.getContainerNum(loadslip.getLoadslipId());
        draftDto.setDelInvLineList(delInvLineRepository.getAllExportInfoForInvoices(exportInvoiceList));
        // draftDto.setContainerNum(containerList.isEmpty() ? null : containerList.get(0));
      }
    }
*/

    if (loadslip.getLoadslipId() != null) {
//      Fetching the invoice number for that loadslipId
      List<String> exportInvoiceList = delInvHeaderRepository.findAllInvoiceNumberByLoadslipId(loadslip.getLoadslipId());
      if (!exportInvoiceList.isEmpty()) {
//        getting all the DelInvHeader
        draftDto.setDelInvHeaderList(delInvHeaderRepository.findAllHearderBySourceLoadslipTypeAndDest(loadslip.getSourceLoc(), loadslip.getLoadslipId(), type, loadslip.getDestLoc()));
        List<String> containerList = delInvHeaderRepository.getContainerNum(loadslip.getLoadslipId());
//        Dividing the invoiceList into partitions of 1000
        ListUtils.partition(exportInvoiceList, 999).parallelStream().forEach(subList -> {
//          draftDto.setDelInvHeaderList(delInvHeaderRepository.findByInvoiceNumberInOrderByInsertDateDesc(subList));
          draftDto.setDelInvLineList(delInvLineRepository.getAllExportInfoForInvoices(subList));
        });
        // draftDto.setContainerNum(containerList.isEmpty() ? null : containerList.get(0));
      }
    }

    List<MTItem> mtItemList = itemRepository.findByIdIn(itemIds);
    Map<String, Double> lines = new HashMap<>();

    for (LoadslipDetail loadslipDetail : loadslip.getLoadslipDetails()) {

      lines = generateSplitLineNumbersLogic(lines, loadslipDetail);

      //Get exportInfo here
/*
      Optional<ExportInfoDto> exportInfo = draftDto.getDelInvLineList().parallelStream().filter(
          t -> t.getItemId().
              equals(loadslipDetail.getLoadslipDetailId().getItemId()) && t.getInvoiceNumber().equals(loadslipDetail.getInvoiceNumber())
      ).findFirst();
*/
      Optional<ExportInfoDto> exportInfo = draftDto.getDelInvLineList().parallelStream().filter(
          t -> {
            if (t.getInvoiceNumber() != null && loadslipDetail.getInvoiceNumber() != null) {
              return t.getItemId().equals(loadslipDetail.getLoadslipDetailId().getItemId());
            } else {
              return t.getItemId().
                  equals(loadslipDetail.getLoadslipDetailId().getItemId());
            }
          }).findFirst();

     /* Optional<LoadslipDetail> loadslipDetailOptional = loadslip.getLoadslipDetails().parallelStream()
              .filter(t -> {
                if(t.getInvoiceNumber()!=null){
                  return t.getLoadslipDetailId().getItemId().equals(exportInfoDto.getItemId())
                          && t.getInvoiceNumber().equals(exportInfoDto.getInvoiceNumber())
                          && t.getLoadslipDetailId().getLineNo().equals(exportInfoDto.getSapLineNo());
                }else{
                  return t.getLoadslipDetailId().getItemId().equals(exportInfoDto.getItemId());
                }
              }).findFirst();
*/

      Optional<Map<String, Object>> data = dispatchPlans.parallelStream()
          .filter(map -> map.get("item_id").equals(loadslipDetail.getLoadslipDetailId().getItemId())
              && map.get("batch_code").equals(loadslipDetail.getBatchCode()))
          .findFirst();
      avlQty = 0;
      valveCompQty = 0;
      tubeCompQty = 0;
      flapCompQty = 0;
      itemCategory = "";
      priority = "";
      itemClassification = "";


      if (data.isPresent()) {
        String itemId = data.get().get("item_id").toString();
        avlQty = (data.get().get("sum_qty") != null ? data.get().get("sum_qty") : 0);
        tubeCompQty = (data.get().get("tube_comp_qty") != null ? data.get().get("tube_comp_qty") : 0);
        valveCompQty = (data.get().get("valve_comp_qty") != null ? data.get().get("valve_comp_qty") : 0);
        flapCompQty = (data.get().get("flap_comp_qty") != null ? data.get().get("flap_comp_qty") : 0);
        itemCategory = (data.get().get("item_category") != null ? data.get().get("item_category") : "");
        priority = (data.get().get("min_priority") != null ? data.get().get("min_priority") : "");
        //itemClassification = itemRepository.findById(itemId).get().getClassification();
        itemClassification = mtItemList.parallelStream().filter(mtItem -> mtItem.getId().equals(itemId)).findFirst().get().getClassification();

      } else {
        Optional<Map<String, Object>> splitItemData = dispatchPlans.parallelStream()
            .filter(map -> map.get("item_id").equals(loadslipDetail.getLoadslipDetailId().getItemId())).findFirst();
        if (splitItemData.isPresent()) {
          String itemId = splitItemData.get().get("item_id").toString();
          tubeCompQty = (splitItemData.get().get("tube_comp_qty") != null ? splitItemData.get().get("tube_comp_qty") : 0);
          valveCompQty = (splitItemData.get().get("valve_comp_qty") != null ? splitItemData.get().get("valve_comp_qty") : 0);
          flapCompQty = (splitItemData.get().get("flap_comp_qty") != null ? splitItemData.get().get("flap_comp_qty") : 0);
          itemCategory = (splitItemData.get().get("item_category") != null ? splitItemData.get().get("item_category") : "");
          priority = (splitItemData.get().get("min_priority") != null ? splitItemData.get().get("min_priority") : "");
          itemClassification = mtItemList.parallelStream().filter(mtItem -> mtItem.getId().equals(itemId)).findFirst().get().getClassification();
        }
      }

      LoadslipDraftDataDto loadslipData = new LoadslipDraftDataDto(loadslipDetail,
          loadslip.getLoadslipDetailBoms().parallelStream()
              .filter(obj -> obj.getLoadslipDetailBomId().getLoadslipId()
                  .equals(loadslipDetail.getLoadslipDetailId().getLoadslipId())
                  && obj.getLoadslipDetailBomId().getItemId()
                  .equals(loadslipDetail.getLoadslipDetailId().getItemId())
                  && obj.getLoadslipDetailBomId().getLineNo()
                  .equals(loadslipDetail.getLoadslipDetailId().getLineNo()))
              .findFirst(),
          avlQty, tubeCompQty, flapCompQty, valveCompQty, itemCategory, priority, exportInfo, itemClassification);

      //Loadslip Line items can not be deleted if there is STO/SO in loadlsip or scan qty is available in Loadslip detail (Loadslip item)
      if (!StringUtils.isEmpty(loadslip.getStoSoNum()) || (loadslipDetail.getScannedQty() != null && loadslipDetail.getScannedQty() > 0)) {
        loadslipData.setCanLSItemDeleted(false);
      }
      loadslipDataList.add(loadslipData);
    }
    draftDto.setSplitNumbersMap(lines);
    //sorting loadslip items
    draftDto.setLoadslipDraftDataDtos(loadslipDataList.stream().sorted(Comparator.comparing(LoadslipDraftDataDto::getIsScannable).reversed().thenComparing(loadslipDraftDataDto -> loadslipDraftDataDto.getItemCategory() != null ? loadslipDraftDataDto.getItemCategory().toString() : loadslipDraftDataDto.getItemId()).thenComparing(LoadslipDraftDataDto::getItemId)).collect(Collectors.toList()));
    setLoadslipData(loadslip, draftDto, loggedInUser);
//    Setting the item categories for cancelled LS in JIT/EXP
//    Since we remove the reference to DEL_INV_HEADER and DEL_INV_LINE when LS is cancelled
    if (LoadslipStatus.CANCELLED.equals(loadslip.getStatus()) &&
        (loadslip.getLoadslipType() != null && (loadslip.getLoadslipType().endsWith("EXP") || loadslip.getLoadslipType().startsWith("JIT")))){
//      Fetching the unique item_ids from loadslip_details
      List<String> cancelledLSItemIdList = draftDto.getLoadslipDraftDataDtos().parallelStream().map(loadslipDraftDataDto -> loadslipDraftDataDto.getItemId()).distinct().collect(Collectors.toList());
      List<Map<String, String>> mtItemMap = new ArrayList<>();
//      Dividing the itemId list into subLists of 999 ids
      ListUtils.partition(cancelledLSItemIdList, 999).parallelStream().forEach(subList -> {
        mtItemMap.addAll(itemRepository.findCategoryByIdIn(subList));
      });
      draftDto.getLoadslipDraftDataDtos().parallelStream().forEach(loadslipDraftDataDto -> {
        Optional<Map<String, String>> optionalMap = mtItemMap.parallelStream().filter(map -> map.get("itemId").equals(loadslipDraftDataDto.getItemId())).findFirst();
        if (optionalMap != null && optionalMap.isPresent()){
          loadslipDraftDataDto.setItemCategory(!StringUtils.isEmpty(loadslipDraftDataDto.getItemCategory()) ? loadslipDraftDataDto.getItemCategory() : optionalMap.get().get("category"));
        }
      });
    }
    return new ApiResponse(HttpStatus.OK, "", draftDto);
  }

  private void setLoadslipData(Loadslip loadslip, LoadslipDraftDto draftDto, ApplicationUser loggedInUser) {
    draftDto.setTotalFlaps(loadslip.getTotFlaps() != null ? loadslip.getTotFlaps() : 0);
    draftDto.setTotalTubes(loadslip.getTotTubes() != null ? loadslip.getTotTubes() : 0);
    draftDto.setTotalTyres(loadslip.getTotTyres() != null ? loadslip.getTotTyres() : 0);
    draftDto.setTotalValves(loadslip.getTotValve() != null ? loadslip.getTotValve() : 0);
    draftDto.setTotalLoadedQty(loadslip.getQty() != null ? loadslip.getQty() : 0);
    draftDto.setTotalTTE(loadslip.getTte() != null ? loadslip.getTte() : 0);
    draftDto.setTotalWeight(loadslip.getWeight() != null ? loadslip.getWeight() : 0);
    draftDto.setTotalVolume(loadslip.getVolume() != null ? loadslip.getVolume() : 0);
    draftDto.setWeightUom(loadslip.getWeightUom());
    draftDto.setLsMarketSegment(loadslip.getMarketSegment());

    draftDto.setTteUtil(loadslip.getTteUtil() != null ? loadslip.getTteUtil() : 0);
    draftDto.setVolumeUtil(loadslip.getVolumeUtil() != null ? loadslip.getVolumeUtil() : 0);
    draftDto.setWeightUtil(loadslip.getWeightUtil() != null ? loadslip.getWeightUtil() : 0);
    draftDto.setGoApprovalReason(loadslip.getGoApprovalReason());
    Shipment shipment = loadslip.getShipment();
    if (shipment != null) {
      draftDto.setShipmentTTEUtil(shipment.getTteUtil() != null ? shipment.getTteUtil() : 0);
      draftDto.setShipmentWeightUtil(shipment.getWeightUtil() != null ? shipment.getWeightUtil() : 0);
      draftDto.setShipmentVolumeUtil(shipment.getVolumeUtil() != null ? shipment.getVolumeUtil() : 0);
      draftDto.setContainerNum(shipment.getContainerNum());
      draftDto.setShipmentID(shipment.getShipmentId());
      draftDto.setTruckNumber(shipment.getTruckNumber());
      draftDto.setTruckType(shipment.getTruckType());
      draftDto.setServprov(shipment.getServprov());
      draftDto.setVariant1(shipment.getVariant1());
      draftDto.setFreightAvailableflag(shipment.getFreightAvailability());
      draftDto.setActualTruckType(shipment.getActualTruckType());

      //Added for Consent Status
      draftDto.setTrackingConsentStatus(shipment.getShtrackingConsentStatus());
      draftDto.setConsentPhoneTelecom(shipment.getShconsentPhoneTelecom());

      CTCountry ctCountry = ctCountryRepository.findByCountryCode(shipment.getDestCountry());
      if (ctCountry != null) {
        draftDto.setCountryName(ctCountry.getCountryName());
      }
      //view loadslip indentCategry
      String truckIndentCategory = truckReportRepo.findTruckIndentCatgoryByShipmentId(shipment.getTruckNumber(), shipment.getIndentId());
      draftDto.setTruckIndentCategory(truckIndentCategory);
      //Setting current loadslip belongs to Multi Stop Shipment or not
      List<Loadslip> loadslips = loadslipRepository.findAllByShipmentShipmentIdAndStatusNot(shipment.getShipmentId(), LoadslipStatus.CANCELLED);
      if (loadslips != null && loadslips.size() > 1) {
        // If more than one loadslips exist with that shipment then it is multiStop shipment having this current loadslip
        draftDto.setMultiStop(true);
      }
    }
    draftDto.setLoadSlipId(loadslip.getLoadslipId());
    draftDto.setDestination(loadslip.getDestLoc());
    draftDto.setDescription(locationRepository.findDescriptionByLocId(loadslip.getDestLoc()));
    draftDto.setCity(locationRepository.findCityByLocId(loadslip.getDestLoc()));
    draftDto.setSource(loadslip.getSourceLoc());
    draftDto.setBay(loadslip.getBay());
    draftDto.setShipTo(loadslip.getShipTo());
    draftDto.setDropSeq(loadslip.getDropSeq() != null ? loadslip.getDropSeq() : 0);
    draftDto.setLoadslipCategory(loadslip.getItemCategory());
    if (loadslip.getLsprintDate() != null) {
      draftDto.setLsPrintDate(DateUtils.formatDate(new Date(loadslip.getLsprintDate().getTime()),
          Constants.DATE_TIME_FORMAT));
    }
    if (loadslip.getArrivedDate() != null) {
      draftDto.setBayArrivedDate(DateUtils.formatDate(new Date(loadslip.getArrivedDate().getTime()),
          Constants.DATE_TIME_FORMAT));
    }
    if (loadslip.getLsDate() != null) {
      draftDto.setLoadingStartDate(
          DateUtils.formatDate(new Date(loadslip.getLsDate().getTime()), Constants.DATE_TIME_FORMAT));
    }
    if (loadslip.getLeDate() != null) {
      draftDto.setLoadingEndDate(
          DateUtils.formatDate(new Date(loadslip.getLeDate().getTime()), Constants.DATE_TIME_FORMAT));
    }
    if (loadslip.getConfirmDate() != null) {
      draftDto.setConfirmedDate(DateUtils.formatDate(new Date(loadslip.getConfirmDate().getTime()),
          Constants.DATE_TIME_FORMAT));
    }
    if (loadslip.getReleaseDate() != null) {
      draftDto.setReleasedDate(DateUtils.formatDate(new Date(loadslip.getReleaseDate().getTime()),
          Constants.DATE_TIME_FORMAT));
    }
    if (loadslip.getSendForBarcodeDate() != null) {
      draftDto.setSendForBarcodeDate(DateUtils.formatDate(new Date(loadslip.getSendForBarcodeDate().getTime()),
          Constants.DATE_TIME_FORMAT));
    }

    draftDto.setDropSeq(loadslip.getDropSeq() != null ? loadslip.getDropSeq() : 0);
    draftDto.setLoadSeq(loadslip.getLoadSeq() != null ? loadslip.getLoadSeq() : 0);
    draftDto.setSostoNumberVal(loadslip.getStoSoNum());
    draftDto.setSostoNumber(loadslip.getStoSoNum() != null);
    draftDto.setSapInvoice(loadslip.getSapInvoice() != null);
    draftDto.setLoadslipType(loadslip.getLoadslipType());
    draftDto.setTotQty(loadslip.getTotQty() != null ? loadslip.getTotQty() : 0);
    draftDto.setTotPctr(loadslip.getTotPctr() != null ? loadslip.getTotPctr() : 0);
    draftDto.setOtherQty(loadslip.getOtherQty() != null ? loadslip.getOtherQty() : 0);
    draftDto.setIsInDraft(!loadslip.getStatus().equals(LoadslipStatus.INTRANSIT) && !loadslip.getStatus().equals(LoadslipStatus.COMPLETED) && !loadslip.getStatus().equals(LoadslipStatus.CANCELLED));
  }

  private Map<String, Double> generateSplitLineNumbersLogic(Map<String, Double> lines, LoadslipDetail loadslipDetail) {
    String key = loadslipDetail.getLoadslipDetailId().getLineNo().intValue() + "line"; // suffix as line
    if (lines.containsKey(key)) {
      if (lines.get(key) < loadslipDetail.getLoadslipDetailId().getLineNo()) {
        lines.put(key, loadslipDetail.getLoadslipDetailId().getLineNo());
      }
    } else {
      lines.put(key, loadslipDetail.getLoadslipDetailId().getLineNo());
    }
    return lines;
  }

  @Override
  public ByteArrayOutputStream getLoadslipPdf(Loadslip loadslip) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      Document document = new Document(PageSize.A4, 10, 0, 5, 80);
      Font regular = FontFactory.getFont(FontFactory.TIMES, 12);
//      Font fontInGray = FontFactory.getFont(FontFactory.TIMES, 9);
      Font fontInGray = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.BOLDITALIC);
      Font valveDescFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLDITALIC);

      Font bold = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
      PdfWriter pdfWriter = PdfWriter.getInstance(document, baos);
//      PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream("src/main/resources/pdfTable.pdf"));
      PDFHeaderAndFooter pdfHeaderAndFooter = new PDFHeaderAndFooter();
      pdfWriter.setPageEvent(pdfHeaderAndFooter);
      document.open();
      PdfPTable table = new PdfPTable(1);
      table.setWidthPercentage(98);
      table.setSplitLate(false);
      Paragraph paragraph = new Paragraph();
      List<String> destinationlist = new ArrayList<>();
      destinationlist.add(loadslip.getDestLoc());
      List<Map<String, String>> destdis = locationRepository.findDestDescWtihDestinations(destinationlist);
      PdfPCell cell = new PdfPCell(new Phrase(paragraph));
      cell.disableBorderSide(Rectangle.BOTTOM);
      cell.setColspan(100);
      cell.setMinimumHeight(100);
      cell.setPadding(3.0f);
      //header table for seconde page ++
      PdfPTable headerTable2 = new PdfPTable(3);
      PDFTableUtil pdfTableUtil = new PDFTableUtil();
      PdfPCell secondPageHeaderCell = new PdfPCell(pdfTableUtil.headerTableForSecondPage(loadslip, headerTable2, pdfWriter, destdis.get(0).get("DESTDESC") != null ? destdis.get(0).get("DESTDESC") : " "));
//      secondPageHeaderCell.disableBorderSide(Rectangle.BOTTOM);
      table.addCell(secondPageHeaderCell);

      //header table for first page
      PdfPTable headerTable = new PdfPTable(3);
      PdfPCell firstHeaderCell = new PdfPCell(pdfTableUtil.setHeaderCellFirstTable(loadslip, headerTable, pdfWriter));
      firstHeaderCell.disableBorderSide(Rectangle.BOTTOM);
      table.addCell(firstHeaderCell);
      // first cell loadslip row
      PdfPTable loadslipIdTable = new PdfPTable(3);
      PdfPCell idCell = new PdfPCell(pdfTableUtil.barcodeLoadslipId(loadslip, loadslipIdTable));
      idCell.disableBorderSide(Rectangle.TOP);
      idCell.disableBorderSide(Rectangle.BOTTOM);
      table.addCell(idCell);
      // first cell second row
      PdfPTable secondrowTable = new PdfPTable(3);
      PdfPCell secondheaderCell = new PdfPCell(pdfTableUtil.setHeaderCellSecondTable(loadslip, secondrowTable));
      secondheaderCell.disableBorderSide(Rectangle.TOP);
      secondheaderCell.disableBorderSide(Rectangle.BOTTOM);
      table.addCell(secondheaderCell);
      //first cell third row
      PdfPTable locationTable = new PdfPTable(3);
      PdfPCell locationcell = new PdfPCell(pdfTableUtil.getLocationTable(loadslip, locationTable, destdis.get(0).get("DESTDESC") != null ? destdis.get(0).get("DESTDESC") : " "));
      locationcell.disableBorderSide(Rectangle.TOP);
      table.addCell(locationcell);
      // second row
      cell = new PdfPCell();
      cell.setColspan(100);
      cell.setMinimumHeight(100);
      cell.setPadding(3.0f);
      PdfPTable transportTable = new PdfPTable(3);
      table.addCell(pdfTableUtil.setTransportTable(loadslip, transportTable));
      // third row
      cell = new PdfPCell();
      cell.setColspan(100);
      cell.setMinimumHeight(50);
      PdfPTable materialDetailsTable = new PdfPTable(7);
      table.addCell(pdfTableUtil.setMaterialDetailsTable(loadslip, materialDetailsTable, loadslipRepository));
      // fourth row
      cell = new PdfPCell();
      cell.setColspan(100);
      cell.setMinimumHeight(50);
      PdfPTable tteDetailsTable = new PdfPTable(3);
      table.addCell(pdfTableUtil.setTteDetailsTable(loadslip, tteDetailsTable));
      // fifth row
      cell = new PdfPCell();
      cell.setColspan(100);
      cell.setMinimumHeight(700);
      cell.setPadding(3.0f);
      PdfPTable innerTable = new PdfPTable(6);
//            innerTable.setTotalWidth(770F);
      innerTable.setWidthPercentage(100);
      innerTable.setSpacingBefore(0f);
      innerTable.setSpacingAfter(0f);
      innerTable.setWidths(new float[]{11, 23, 3, 4.5F, 3, 3.5f});
      innerTable.getDefaultCell().setMinimumHeight(16);
      innerTable.setTotalWidth(500f);
      innerTable.setLockedWidth(false);
      innerTable.setHorizontalAlignment(Element.ALIGN_CENTER);
      table.setHeaderRows(1);
      table.setSkipFirstHeader(true);

      addTableHeader(innerTable);
      List<LoadslipDetail> loadslipDetails = loadslip.getLoadslipDetails().stream().sorted(Comparator.comparing(LoadslipDetail::getScannable).reversed().thenComparing(ld -> ld.getItemCategory() != null ? ld.getItemCategory() : ld.getLoadslipDetailId().getItemId()).thenComparing(ld -> ld.getLoadslipDetailId().getItemId())).collect(Collectors.toList());
      loadslipDetails.forEach(loadslipDetail -> {
            PdfPCell pdfPCell;
            Paragraph p = new Paragraph();
            p.add(new Chunk(loadslipDetail.getLoadslipDetailId() != null && loadslipDetail.getLoadslipDetailId().getItemId() != null ? loadslipDetail.getLoadslipDetailId().getItemId()
                : "-", regular));
            pdfPCell = new PdfPCell(p);
            innerTable.addCell(pdfPCell);
            //Description
            p = new Paragraph();
            p.add(new Chunk(loadslipDetail.getItemDescription() != null ? loadslipDetail.getItemDescription() : "-", bold));
            pdfPCell = new PdfPCell(p);
            innerTable.addCell(pdfPCell);
            //scannable
            p = new Paragraph();
            p.add(new Chunk(loadslipDetail.getScannable() != null ? loadslipDetail.getScannable() : "-", bold));
            pdfPCell = new PdfPCell(p);
            pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            innerTable.addCell(pdfPCell);
            //Batchcode
            p = new Paragraph();
            p.add(new Chunk(loadslipDetail.getBatchCode() != null ? loadslipDetail.getBatchCode() : "-", regular));
            pdfPCell = new PdfPCell(p);
            pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            innerTable.addCell(pdfPCell);
            //Quantity
            p = new Paragraph();
            p.add(new Chunk(loadslipDetail.getLoadQty() + "", bold));
            pdfPCell = new PdfPCell(p);
            pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            innerTable.addCell(pdfPCell);
            innerTable.addCell("");
            Optional<LoadslipDetailBom> detailBom = loadslip.getLoadslipDetailBoms().stream().filter(bom -> bom.getLoadslipDetailBomId().getLineNo().equals(loadslipDetail.getLoadslipDetailId().getLineNo())).findAny();
            if (detailBom.isPresent()) {
              LoadslipDetailBom loadslipDetailBom = detailBom.get();
              if (!StringUtils.isEmpty(loadslipDetailBom.getTubeSku())) {
                p = new Paragraph();
                p.add(new Chunk(loadslipDetailBom.getTubeSku(), fontInGray));
                pdfPCell = new PdfPCell(p);
                pdfPCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                innerTable.addCell(pdfPCell);
                //Description
                p = new Paragraph();
                p.add(new Chunk(loadslipDetailBom.getTubeDesc() != null ? loadslipDetailBom.getTubeDesc() : " ", fontInGray));
                pdfPCell = new PdfPCell(p);
                pdfPCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                innerTable.addCell(pdfPCell);
                //scannable
                p = new Paragraph();
                p.add(new Chunk(" - ", fontInGray));
                pdfPCell = new PdfPCell(p);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                innerTable.addCell(pdfPCell);
                //Batchcode
                p = new Paragraph();
                p.add(new Chunk(loadslipDetailBom.getTubeBatch() != null ? loadslipDetailBom.getTubeBatch() : " ", fontInGray));
                pdfPCell = new PdfPCell(p);
                pdfPCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                innerTable.addCell(pdfPCell);
                //Quantity
                p = new Paragraph();
                p.add(new Chunk(loadslipDetailBom.getTubeQty() != null ? loadslipDetailBom.getTubeQty() + "" : " ", fontInGray));
                pdfPCell = new PdfPCell(p);
                pdfPCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                innerTable.addCell(pdfPCell);
                pdfPCell = new PdfPCell();
                pdfPCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                innerTable.addCell(pdfPCell);
              }
              if (!StringUtils.isEmpty(loadslipDetailBom.getFlapSku())) {
                p = new Paragraph();
                p.add(new Chunk(loadslipDetailBom.getFlapSku(), fontInGray));
                pdfPCell = new PdfPCell(p);
                pdfPCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                innerTable.addCell(pdfPCell);
                //Description
                p = new Paragraph();
                p.add(new Chunk(loadslipDetailBom.getFlapDesc() != null ? loadslipDetailBom.getFlapDesc() : " ", fontInGray));
                pdfPCell = new PdfPCell(p);
                pdfPCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                innerTable.addCell(pdfPCell);
                //scannable
                p = new Paragraph();
                p.add(new Chunk(" - ", fontInGray));
                pdfPCell = new PdfPCell(p);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                innerTable.addCell(pdfPCell);
                //Batchcode
                p = new Paragraph();
                p.add(new Chunk(loadslipDetailBom.getFlapBatch() != null ? loadslipDetailBom.getFlapBatch() : " ", fontInGray));
                pdfPCell = new PdfPCell(p);
                pdfPCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                innerTable.addCell(pdfPCell);
                //Quantity
                p = new Paragraph();
                p.add(new Chunk(loadslipDetailBom.getFlapQty() != null ? loadslipDetailBom.getFlapQty() + "" : " ", fontInGray));
                pdfPCell = new PdfPCell(p);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                innerTable.addCell(pdfPCell);
                pdfPCell = new PdfPCell();
                pdfPCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                innerTable.addCell(pdfPCell);
              }
              if (loadslipDetailBom.getValveQty() > 0) {
                p = new Paragraph();
                p.add(new Chunk(loadslipDetailBom.getValveSku() != null ? loadslipDetailBom.getValveSku() : " ", regular));
                pdfPCell = new PdfPCell(p);
                innerTable.addCell(pdfPCell);
                //Description
                p = new Paragraph();
                p.add(new Chunk(loadslipDetailBom.getValveDesc() != null ? loadslipDetailBom.getValveDesc() : " ", valveDescFont));
                pdfPCell = new PdfPCell(p);
                innerTable.addCell(pdfPCell);
                //scannable
                p = new Paragraph();
                p.add(new Chunk(" - ", valveDescFont));
                pdfPCell = new PdfPCell(p);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                innerTable.addCell(pdfPCell);
                //Batchcode
                p = new Paragraph();
                p.add(new Chunk(loadslipDetailBom.getValveBatch() != null ? loadslipDetailBom.getValveBatch() : " ", regular));
                pdfPCell = new PdfPCell(p);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                innerTable.addCell(pdfPCell);
                //Quantity
                p = new Paragraph();
                p.add(new Chunk(loadslipDetailBom.getValveQty() != null ? loadslipDetailBom.getValveQty() + "" : " ", valveDescFont));
                pdfPCell = new PdfPCell(p);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                innerTable.addCell(pdfPCell);
                pdfPCell = new PdfPCell();
                innerTable.addCell(pdfPCell);
              }
            }

          }
      );
      table.addCell(innerTable);
      cell = new PdfPCell();
      cell.setColspan(100);
      cell.setMinimumHeight(100);
      document.add(table);
      if (!StringUtils.isEmpty(loadslip.getComments())) {
        PdfPTable commentsTable = new PdfPTable(1);
        commentsTable.setWidthPercentage(98);
        commentsTable.setSpacingBefore(15f);
        paragraph = new Paragraph("Comments : ", bold);
        paragraph.add(new Chunk(loadslip.getComments(), regular));
        cell = new PdfPCell(paragraph);
        commentsTable.addCell(cell);
        document.add(commentsTable);

      }
      pdfHeaderAndFooter.setIslastPage(true);
      pdfWriter.setPageEvent(pdfHeaderAndFooter);
      document.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return baos;
  }

  public ByteArrayOutputStream getLoadslipExcel(Loadslip loadslip) {
    ExcelUtil excelUtil = new ExcelUtil();
    return excelUtil.createLoadslipExcelSheet(loadslip, locationRepository);
  }


  @Override
  public ApiResponse getIntransitTrucks(ApplicationUser loggedInUser, String sourceLocation) {
    return new ApiResponse(HttpStatus.OK, "Intransit Trucks",
        truckReportRepo.findAllByStatusAndDestinationLocation(TruckReportStatus.INTRANSIT, sourceLocation));
  }

  @Override
  public ApiResponse sendToSAPForBarcodeScan(String loadslipID, ApplicationUser loggedInUser) {
    Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(loadslipID);
    String validationMsg = Utility.validateExistingLoadslip(optionalLoadslip);
    if (validationMsg != null) {
      return new ApiResponse(HttpStatus.NOT_FOUND, format(validationMsg + " with the loadslip Id %s", loadslipID));
    }
    HttpHeaders headers = new HttpHeaders();
    headers.set(Constants.API_AUTH, Utility.convertStringBase64Format(sapUserName + ":" + sapPassword));
    headers.set(Constants.LOAD_SLIP_ID, optionalLoadslip.get().getLoadslipId());
    HttpEntity httpEntity = new HttpEntity<>(headers);
    ResponseEntity responseEntity = restTemplate.postForEntity(sapBarcodeURL, httpEntity, ResponseEntity.class);
    if (responseEntity.getStatusCodeValue() == 200) {
      optionalLoadslip.get().setSendForBarcodeDate(currentTimestamp());
      optionalLoadslip.get().setUpdateDate(new Date());
      optionalLoadslip.get().setUpdateUser(loggedInUser.getUserId());
      loadslipRepository.save(optionalLoadslip.get());
      return new ApiResponse(HttpStatus.OK, "Sent to SAP For Barcode scanning",
          getLoadslipDetails(optionalLoadslip.get().getLoadslipId(), loggedInUser));
    }
    return new ApiResponse(responseEntity.getStatusCode(),
        responseEntity.getHeaders().get(Constants.SAP_API_RESPONSE).get(0));
  }

  @Override
  @Transactional
  public ApiResponse rollBackLoadslip(String loadslipID, ApplicationUser loggedInUser) {
    Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(loadslipID);
    String validationMsg = Utility.validateExistingLoadslip(optionalLoadslip);
    if (validationMsg != null) {
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format(validationMsg + " with the loadslip Id %s", loadslipID));
    }
    // Loadslip Truck is already released so can not cancel the loadslip
    boolean isUpdateableForExp = optionalLoadslip.get().getStatus().equals(LoadslipStatus.INTRANSIT) || optionalLoadslip.get().getStatus().equals(LoadslipStatus.COMPLETED);
    boolean isExportStatus = (optionalLoadslip.get().getLoadslipType().equals(DelInvType.FGS_EXP.name()) || optionalLoadslip.get().getLoadslipType().equals(DelInvType.JIT_OEM.name()));
    boolean exportCheck = isUpdateableForExp && isExportStatus;
    if (exportCheck) {
      return new ApiResponse(HttpStatus.BAD_REQUEST, String.format("Can not perform cancel loadslip with the loadslip Id %s", loadslipID));
    } else {
      if (!isExportStatus && optionalLoadslip.get().getReleaseDate() != null) {
        return new ApiResponse(HttpStatus.BAD_REQUEST, String.format("Can not perform cancel loadslip with the loadslip Id %s", loadslipID));
      }
    }
    List<DispatchPlanItemInfo> rollBackedPlans = new ArrayList<>();
    for (LoadslipDetail loadslipDetail : optionalLoadslip.get().getLoadslipDetails()) {
      List<DispatchPlanItemInfo> dispatchPlanItemInfos = fetchDispatchPlansByLoadslipItem(loadslipDetail, loggedInUser, optionalLoadslip.get().getDestLoc());
      if (optionalLoadslip.get().getStatus() == LoadslipStatus.SENT_SAP) {
        // Roll back post sending to SAP
      /*
        FGS user will click on “Cancel Loadslip”.  Cancel loadslip will trigger the below process in OPS
       -> Delete the dispatch qty from the dispatch plan table. The cancellation should be done from the same dispatch lines from where the initial quantity was consumed
       -> Recalculation of all the dispatch plan values and status
       -> Deletion of SAP invoice / delivery details received from SAP
       -> Deletion of loadslip, shipment tables
       */
        rollBackDispatchPlan(optionalLoadslip.get(), dispatchPlanItemInfos, loadslipDetail, rollBackedPlans, true);
      } else {
        // Roll back before sending to SAP
        /*
        FGS user will click on “Cancel Loadslip”.  Cancel loadslip will trigger the below process in OPS
        -> Delete the reserved qty from the dispatch plan table. The cancellation should be done from the same dispatch lines from where the initial quantity was consumed
        -> Recalculation of all the dispatch plan values and status
        -> Deletion of loadslip, shipment tables
         */
        rollBackDispatchPlan(optionalLoadslip.get(), dispatchPlanItemInfos, loadslipDetail, rollBackedPlans, false);
      }
    }
    if (!rollBackedPlans.isEmpty()) {
      dispatchPlanItemInfoRepository.saveAll(rollBackedPlans);
    }
    deleteLoadslipAndShipment(optionalLoadslip.get(), optionalLoadslip.get().getShipment().getShipmentId(), loggedInUser);

    //remove it from del-inv-header if available
    List<DelInvHeader> delheadersbyloadSlip = delInvHeaderRepository.findByLoadslipId(loadslipID);
    updateDelInvHeaderLoadslipIdAndContainer(delheadersbyloadSlip, null, null, null);

    return new ApiResponse(HttpStatus.OK, "Loadslip Cancelled ", loadslipID);
  }

  private List<DispatchPlanItemInfo> fetchDispatchPlansByLoadslipItem(LoadslipDetail loadslipDetail, ApplicationUser loggedInUser, String destLoc) {
    List<DispatchPlanItemInfo> dispatchPlanItemInfos;
    /*When source is EXT_WAREHOUSE and destination is not 1007*/
    String actualSource = utilityService.getLinkedPlntLoc(loggedInUser.getPlantCode(), destLoc);
    if (loggedInUser.getRole().isRDCPlannerOrRDCGate()) {
      /*dispatchPlanItemInfos = dispatchPlanItemInfoRepository.findAllBySourceLocationAndDestinationLocationAndItemIdAndStatusOrderByDispatchDateAsc(loggedInUser.getPlantCode(),
          destLoc, loadslipDetail.getLoadslipDetailId().getItemId());*/
      dispatchPlanItemInfos = dispatchPlanItemInfoRepository.findAllBySourceLocationAndDestinationLocationAndItemIdAndStatusOrderByDispatchDateAsc(actualSource,
          destLoc, loadslipDetail.getLoadslipDetailId().getItemId(), Status.OPEN);
    } else {
      /*dispatchPlanItemInfos = dispatchPlanItemInfoRepository.findAllBySourceLocationAndDestinationLocationAndBatchCodeAndItemIdAndStatusOrderByDispatchDateAsc(
          loggedInUser.getPlantCode(), destLoc, loadslipDetail.getBatchCode(), loadslipDetail.getLoadslipDetailId().getItemId());*/
      dispatchPlanItemInfos = dispatchPlanItemInfoRepository.findAllBySourceLocationAndDestinationLocationAndBatchCodeAndItemIdAndStatusOrderByDispatchDateAsc(
          actualSource, destLoc, loadslipDetail.getBatchCode(), loadslipDetail.getLoadslipDetailId().getItemId(), Status.OPEN);
    }
    return dispatchPlanItemInfos;
  }

  private void deleteLoadslipAndShipment(Loadslip loadslip, String shipmentId, ApplicationUser loggedInUser) {
//    loadslipRepository.deleteLoadslipLineDetail(loadslip.getLoadslipId());
//    loadslipRepository.deleteLoadslipByLoadslipId(loadslip.getLoadslipId());
//    loadslipDetailRepository.deleteLoadslipDetailByLoadslipId(loadslip.getLoadslipId());
//    loadslipDetailBomRepository.deleteLoadslipDetailBomByLoadslipId(loadslip.getLoadslipId());
    loadslip.setStatus(LoadslipStatus.CANCELLED);
    loadslip.setUpdateDate(new Date());
    loadslip.setUpdateUser(loggedInUser.getUserId());
    loadslipRepository.save(loadslip);

    Optional<Shipment> optionalShipment = shipmentRepository.findById(shipmentId);
    if (optionalShipment.isPresent()) {
      Shipment shipment = optionalShipment.get();
      // All Loadslips in Shipment are cancelled, make shipment status as CANCELLED
      if (loadslipRepository.findAllByShipmentShipmentIdAndStatusNot(shipmentId, LoadslipStatus.CANCELLED).isEmpty()) {
        shipment.setStatus(Status.CANCELLED.name());
        //Cancel sharedTruck
        List<SharedTruck> sharedTruckList = sharedTruckRepository.findAllBySharedTruckIdShipmentIdAndStatus(shipmentId, Status.OPEN.name());
        for (SharedTruck shardTruck : sharedTruckList) {
          shardTruck.setStatus(Status.CLOSED.name());
        }
        sharedTruckRepository.saveAll(sharedTruckList);
      }
      shipment = updateShipmentTruckUtils(shipment, loadslip);
      shipment.setUpdateUser(loggedInUser.getUserId());
      shipment.setUpdateDate(new Date());
//      getting all the loadslips in the shipments irrespective of source
      List<Loadslip> remainingLoadslipsInShipment = loadslipRepository.findAllByShipmentShipmentIdAndStatusNot(shipment.getShipmentId(),  LoadslipStatus.CANCELLED);
      /*Calculating the shipment stop type (i.e., S/MP/MD/MPMD)
      * sending the remaining LS's in the shipment, source of anyOne LS and dest of that LS
      * Since we can derive the stop type with any source and dest of the SHIPMENT*/
      if (remainingLoadslipsInShipment != null && !remainingLoadslipsInShipment.isEmpty()) {
        shipment.setStopType(deriveShipmentStopType(remainingLoadslipsInShipment, remainingLoadslipsInShipment.get(0).getSourceLoc(), remainingLoadslipsInShipment.get(0).getDestLoc()));
      }
      shipmentRepository.save(shipment);
      /*When LS is cancelled then make truck status as GATED_IN and bay = null*/
//      List<Loadslip> loadslipsInShipment = loadslipRepository.findAllByShipmentShipmentIdAndSourceLocAndStatusNot(shipment.getShipmentId(), loadslip.getSourceLoc(), LoadslipStatus.CANCELLED);
//      Optional<TruckReport> optionalTruckReport = truckReportRepo.findByShipmentIDAndReportLocation(shipmentId, loggedInUser.getPlantCode());
//      if (optionalTruckReport.isPresent()) {
//        optionalTruckReport.get().setStatus(TruckReportStatus.GATED_IN);
//        optionalTruckReport.get().setBayStatus(null);
//        optionalTruckReport.get().setUpdateUser(loggedInUser.getUserId());
//        optionalTruckReport.get().setUpdateDate(new Date());
//        truckReportRepo.save(optionalTruckReport.get());
//
//      }
//    }
//      Getting the remaining LS from shipment from a perticular source
      List<Loadslip> loadslipsInShipment = loadslipRepository.findAllByShipmentShipmentIdAndSourceLocAndStatusNot(shipment.getShipmentId(), loadslip.getSourceLoc(), LoadslipStatus.CANCELLED);

      /*When there are multiple LS on truck and one LS is cancelled then truck status not changed*/
      Optional<TruckReport> optionalTruckReport = truckReportRepo.findByShipmentIDAndReportLocation(shipmentId, loggedInUser.getPlantCode());
      if (optionalTruckReport.isPresent()) {
        if (loadslipsInShipment.isEmpty()) {
          /*No LS left on the truck*/
          optionalTruckReport.get().setStatus(TruckReportStatus.GATED_IN);
          optionalTruckReport.get().setBayStatus(null);
          /*Updating the truck destination with indent dest
           * When All the LS on the Shipment cancelled, IF the truck Dest was updated according to DROP_SEQ
           * then we are resetting the truck_dest to the initial dest*/
          String indentDestLoc = indentSummaryRepository.findIndentDestByIndentId(optionalTruckReport.get().getIndentSummary().getIndentId());
          if (!StringUtils.isEmpty(indentDestLoc)) {
            optionalTruckReport.get().setDestinationLocation(indentDestLoc);
          }
        } else {
          /*If There are still lodslips available in shipment
           * then check if all the other loadslips in the truck/shipment are RELEASE or not
           * if RELEASED then make the truck bay_status as RELEASED
           * If anyone of the LS is not released yet then do nothing*/
          boolean isTruckRelease = true;
          for (Loadslip lsOnShip : loadslipsInShipment) {
            if (StringUtils.isEmpty(lsOnShip.getReleaseDate())) {
              isTruckRelease = false;
              break;
            }
          }
          if (isTruckRelease) {
            /*Last Status of the truck is LOADED before making it INTRANSIT*/
            optionalTruckReport.get().setStatus(TruckReportStatus.LOADED);
            optionalTruckReport.get().setBayStatus(BayStatus.RELEASE);
          } else {
            /*When there are still other LS Which are not yet released then we are considering the LEAST STATUS
             * (i.e., ASSIGN_LS -> 1
             *        LOADING -> 2
             *        LOADED -> 3
             * Since only these 3 status are  considered when performing any operation on LS */
            TruckReportStatus truckReportStatus = getTheLeastStatusOfLSFromLoadslips(loadslipsInShipment);
            BayStatus bayStatus = getBayStatusFromTruckStatus(truckReportStatus);
            optionalTruckReport.get().setStatus(truckReportStatus);
            optionalTruckReport.get().setBayStatus(bayStatus);
          }

        }
        optionalTruckReport.get().setUpdateUser(loggedInUser.getUserId());
        optionalTruckReport.get().setUpdateDate(new Date());
        truckReportRepo.save(optionalTruckReport.get());
      }
    }
    /*//Save or Update shipment stop table
    executeShipmentStopProcedure(shipmentId, loggedInUser.getUserId());*/
    saveShipmentStopAndTruckDestination(loadslip.getLoadslipId(), loggedInUser);
  }

  private BayStatus getBayStatusFromTruckStatus(TruckReportStatus truckReportStatus) {
    BayStatus bayStatus = null;
    switch (truckReportStatus) {
      case LOADING:
        return BayStatus.LSPB;
      case LOADED:
        return BayStatus.LEPB;
      default:
        return BayStatus.ARVD;
    }
  }

  private TruckReportStatus getTheLeastStatusOfLSFromLoadslips(List<Loadslip> loadslipsInShipment) {
//    Setting the values for each STATUS
    Map<String, Integer> statusMap = new HashMap<>();
    statusMap.put(TruckReportStatus.ASSIGN_LS.name(), 1);
    statusMap.put(TruckReportStatus.LOADING.name(), 2);
    statusMap.put(TruckReportStatus.LOADED.name(), 3);

//    Gettingthe Truck Status based on the first LS-status in loop
    String truckStatus = getTruckStatusAccoringToLSStatus(loadslipsInShipment.get(0).getStatus().name());
    for (Loadslip loadslip : loadslipsInShipment) {
//      If ls is sent to sap then do nothing. Since it the we will not consider it while performing LS events
      if (StringUtils.isEmpty(loadslip.getReleaseDate()) && !LoadslipStatus.SENT_SAP.equals(loadslip.getStatus())) {
        String currentLoadslipStatus = loadslip.getStatus().name();
        String expectedTruckStatus = getTruckStatusAccoringToLSStatus(currentLoadslipStatus);
        if (!StringUtils.isEmpty(expectedTruckStatus)) {
          if (statusMap.get(expectedTruckStatus) < statusMap.get(truckStatus)) {
            truckStatus = expectedTruckStatus;
          }
        }
      }
    }
    return TruckReportStatus.valueOf(truckStatus);
  }

  private String getTruckStatusAccoringToLSStatus(String currentLoadslipStatus) {
    switch (currentLoadslipStatus) {
      case "CREATED":
        return TruckReportStatus.ASSIGN_LS.name();
      case "PRINTED":
        return TruckReportStatus.ASSIGN_LS.name();
      case "LOADING":
        return TruckReportStatus.LOADING.name();
      default:
        return LoadslipStatus.LOADED.name();
    }
  }

  private Shipment updateShipmentTruckUtils(Shipment shipment, Loadslip loadslip) {
    // When loadslip gets cancelled , needs to update SHipment Utilization values
    if (shipment.getTteUtil() != null && loadslip.getTteUtil() != null && shipment.getTteUtil() > 0 && shipment.getTteUtil() >= loadslip.getTteUtil()) {
      shipment.setTteUtil(shipment.getTteUtil() - loadslip.getTteUtil());
    }
    if (shipment.getWeightUtil() != null && loadslip.getWeightUtil() != null && shipment.getWeightUtil() > 0 && shipment.getWeightUtil() >= loadslip.getWeightUtil()) {
      shipment.setWeightUtil(shipment.getWeightUtil() - loadslip.getWeightUtil());
    }
    if (shipment.getVolumeUtil() != null && loadslip.getVolumeUtil() != null && shipment.getVolumeUtil() > 0 && shipment.getVolumeUtil() >= loadslip.getVolumeUtil()) {
      shipment.setVolumeUtil(shipment.getVolumeUtil() - loadslip.getVolumeUtil());
    }
    return shipment;
  }

  private void rollBackDispatchPlan(Loadslip loadslip, List<DispatchPlanItemInfo> dispatchPlanItemInfos, LoadslipDetail loadslipDetail, List<DispatchPlanItemInfo> rolledBackPlans, boolean isSentToSAP) {
    int rollBackQty = 0;
    int loadslipItemQty = loadslipDetail.getLoadQty();
    for (DispatchPlanItemInfo dispatchPlanItemInfo : dispatchPlanItemInfos) {
      if (loadslipItemQty > 0) {
        //If loadslip has invoices, then roll back qty from Dispatched Qty
        if (!StringUtils.isEmpty(loadslip.getSapInvoice())) {
          if (loadslipItemQty > dispatchPlanItemInfo.getDispatchedQuantity()) {
            rollBackQty = dispatchPlanItemInfo.getDispatchedQuantity();
          } else {
            rollBackQty = loadslipItemQty;
          }
          loadslipItemQty = loadslipItemQty - rollBackQty;
          dispatchPlanItemInfo.setDispatchedQuantity(dispatchPlanItemInfo.getDispatchedQuantity() - rollBackQty);
          dispatchPlanItemInfo.setLoadedQty(dispatchPlanItemInfo.getLoadedQty() + rollBackQty);
        } else if (isSentToSAP) {
          //  After sending to SAP
          if (loadslipItemQty > dispatchPlanItemInfo.getLoadedQty()) {
            rollBackQty = dispatchPlanItemInfo.getLoadedQty();
          } else {
            rollBackQty = loadslipItemQty;
          }
          loadslipItemQty = loadslipItemQty - rollBackQty;
          dispatchPlanItemInfo.setLoadedQty(dispatchPlanItemInfo.getLoadedQty() - rollBackQty);
          dispatchPlanItemInfo.setReservedQuantity(dispatchPlanItemInfo.getReservedQuantity() + rollBackQty);
        } else {
          //  Before sending to SAP
          if (loadslipItemQty > dispatchPlanItemInfo.getReservedQuantity()) {
            rollBackQty = dispatchPlanItemInfo.getReservedQuantity();
          } else {
            rollBackQty = loadslipItemQty;
          }
          loadslipItemQty = loadslipItemQty - rollBackQty;
          dispatchPlanItemInfo.setReservedQuantity(dispatchPlanItemInfo.getReservedQuantity() - rollBackQty);
          dispatchPlanItemInfo.setAvailableQuantity(dispatchPlanItemInfo.getAvailableQuantity() + rollBackQty);
          dispatchPlanItemInfo.setTotalAvailableQuantity(dispatchPlanItemInfo.getAvailableQuantity() + dispatchPlanItemInfo.getUnapprovedQuantity());
        }
        rolledBackPlans.add(dispatchPlanItemInfo);
      }
    }
  }

  private void addTableHeader(PdfPTable table) {
    Font headerFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
    Stream.of("Material", "Desc", "Scan", "Batch", "Qty", "Picked").forEach(columnTitle -> {
      PdfPCell header = new PdfPCell();
      header.setHorizontalAlignment(Element.ALIGN_CENTER);
      header.setPhrase(new Phrase(columnTitle, headerFont));
      table.addCell(header);
    });
  }

  private static int getLoadslipEventsIndex(LoadslipStatus loadslipStatus) {
    return loadslipEventsIndexMap.get(loadslipStatus);
  }

  @Override
  @Transactional(rollbackFor = SQLException.class)
  public ApiResponse saveMultiLoadslip(LoadslipDraftDto loadslipDraftDto, ApplicationUser loggedInUser) throws SQLException {
    Loadslip loadslip;
    if (loadslipDraftDto.getLoadslipDraftDataDtos().isEmpty() && !isExistingLoadslip(loadslipDraftDto)) {
      return createLSJitAndExportMultiStop(loadslipDraftDto, loggedInUser);
    }

//    /*---------------------------------------------------------------------------------------------------*/
//
//
//    List<LoadslipDetail> loadslipDetailList = new ArrayList<>();
//    List<String> loadslipWithSource = new ArrayList<>();
//    List<String> actualSourceList = new ArrayList<>();
//    String actualSource = utilityService.getLinkedPlntLoc(loadslipDraftDto.getSource(), loadslipDraftDto.getDestination());
////      loadslipWithSource.add(actualSource);
//    if (!loadslipDraftDto.getSource().contentEquals(actualSource) || loadslipDraftDto.getSource().contentEquals("1007")) {
//      /*actualSourceList.add(actualSource);*/
//      /*when loadslipDraftDto.getSource() = TNR2/TNR4/TNR5/TNR6/TNR7 then actualSource will be 1007
//       * then all the linked ext_warehouse of 1007 and add to actual_source_list*/
//      List<String> sourceList = utilityService.getLinkedExtWareHouse(actualSource);
//      if (sourceList != null && !sourceList.isEmpty()) {
//        loadslipWithSource.addAll(sourceList);
//      }
//      actualSourceList = getLoadslipWithActualSourceList(loadslipWithSource);
//    }
//    actualSourceList.add(actualSource);
//    final List<String> LSDetailsSourceList =actualSourceList;
//    if (loggedInUser.getRole().isRDCPlannerOrRDCGate()){
//      loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().forEach(loadslipDraftDataDto -> {
//        loadslipDetailList.addAll(loadslipDetailRepository.findAllByLoadslipDetailIdItemIdAndItemDescriptionAndLoadslipDestLocAndLoadslipSourceLocIn(
//            loadslipDraftDataDto.getItemId(), loadslipDraftDataDto.getItemDesc(), loadslipDraftDto.getDestination(), LSDetailsSourceList));
//      });
//    }else {
//      loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().forEach(loadslipDraftDataDto -> {
//        loadslipDetailList.addAll(loadslipDetailRepository
//            .findAllByLoadslipDetailIdItemIdAndItemDescriptionAndBatchCodeAndLoadslipDestLocAndLoadslipSourceLocIn(
//                loadslipDraftDataDto.getItemId(), loadslipDraftDataDto.getItemDesc(),
//                loadslipDraftDataDto.getBatchCode(), loadslipDraftDto.getDestination(), LSDetailsSourceList));
//      });
//    }
//
//
//    /*---------------------------------------------------------------------------------------------------*/

    List<String> errorMessages = new ArrayList<String>();
    if (isExistingLoadslip(loadslipDraftDto)) {
      Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(loadslipDraftDto.getLoadSlipId());
      String validationMsg = Utility.validateExistingLoadslip(optionalLoadslip);
      if (validationMsg != null) {
        return new ApiResponse(HttpStatus.NOT_FOUND, String.format(validationMsg + " with the loadslip Id %s", loadslipDraftDto.getLoadSlipId()));
      }
      loadslip = updateMultiLoadslip(optionalLoadslip.get(), errorMessages, loadslipDraftDto, loggedInUser);
    } else {
      loadslip = saveShipmentMultiDropLoadslip(errorMessages, loadslipDraftDto, loggedInUser);
    }
    LoadSlipResponseDto loadSlipResponseDto = null;
    if (loadslip == null) {
      throw new SQLException(errorMessages.get(0));
     /* try {
        throw new Exception();
      } catch (Exception e) {
        return new ApiResponse(HttpStatus.EXPECTATION_FAILED, "Failed some business rules", new LoadSlipResponseDto(errorMessages));
      }*/
    }
    /*Recalculating LS weight if LS is not of Export or JIT*/
    if (!Constants.DelInvType.isExportOrJIT(loadslip.getLoadslipType())){
      this.callLoadslipWeightRecalculatioinProcedure(loadslip.getLoadslipId());
      loadslip = loadslipRepository.findByLoadslipId(loadslip.getLoadslipId()).get();
    }
    //Fetch loadslip details for this loadslip and send back to UI in the response
    LoadslipDraftDto loadslipDetails = null;
    ApiResponse apiResponse = getLoadslipDetails(loadslip.getLoadslipId(), loggedInUser);
    if (apiResponse.getStatusCode() == 200) {
      loadslipDetails = (LoadslipDraftDto) apiResponse.getData();
    }
    //adding details to del_inv_header
    if (!loadslipDraftDto.getInvoiceList().isEmpty()) {
      updateDelInvHeaderAndLine(loadslipDraftDto.getInvoiceList(), loadslip, loadslipDraftDto.getContainerNum(), loadslip.getShipment().getShipmentId());
    }
    if (loadslipDraftDto.getAction().equals("PRINT")) {
      loadSlipResponseDto = new LoadSlipResponseDto(loadslip.getLoadslipId(),
          loadslip.getShipment().getShipmentId(), opsService.getLoadslipPdf(loadslip).toByteArray(),
          DateUtils.formatDate(new Date(loadslip.getLsprintDate().getTime()), Constants.DATE_TIME_FORMAT), new ArrayList(), loadslipDetails, loadslip.getLoadslipType());
      return new ApiResponse(HttpStatus.OK, "Loadslip printed successfully", loadSlipResponseDto);
    }
    loadSlipResponseDto = new LoadSlipResponseDto(loadslip.getLoadslipId(), loadslip.getShipment().getShipmentId(),
        null, loadslip.getLsprintDate() != null ? DateUtils.formatDate(new Date(loadslip.getLsprintDate().getTime()), Constants.DATE_TIME_FORMAT) : null, new ArrayList(), loadslipDetails, loadslip.getLoadslipType());

    /*//adding details to del_inv_header
    if (!loadslipDraftDto.getInvoiceList().isEmpty()) {
      updateDelInvHeaderAndLine(loadslipDraftDto.getInvoiceList(), loadslip, loadslipDraftDto.getContainerNum(), loadslip.getShipment().getShipmentId());
    }*/
    return new ApiResponse(HttpStatus.OK, "Loadslip saved as Draft successfully", loadSlipResponseDto);
  }

  private ApiResponse createLSJitAndExportMultiStop(LoadslipDraftDto loadslipDraftDto, ApplicationUser loggedInUser) {
    Loadslip loadslip = new Loadslip();
    loadslip.setLoadslipId(indentSummaryRepository.findIndentSequence(Constants.LOADSLIP_SEQ_PREFIX,
        loadslipDraftDto.getSource(), loadslipDraftDto.getDestination()));
    loadslip.setSourceLoc(loadslipDraftDto.getSource());
    loadslip.setDestLoc(loadslipDraftDto.getDestination());
    loadslip.setInsertUser(loggedInUser.getUserId());
    loadslip.setInsertDate(new Date());
    loadslip.setStatus(LoadslipStatus.CREATED);
    loadslip.setLoadslipType(loadslipDraftDto.getType().name());
    loadslip.setDropSeq(loadslipDraftDto.getDropSeq());

    /*Setting loadslip MKT_SEG with OrderTypeLookUp.Market_segment when OrderTypeLookup.orderType = loadslip.loadslipType*/
    String mktSeg = getLoadslipMktSegFromLSType(loadslip.getLoadslipType());
    if (!StringUtils.isEmpty(mktSeg))
      loadslip.setMarketSegment(mktSeg);

    // Shipment
    TruckReport truckReport = null;
    Optional<Shipment> optionalShipment = shipmentRepository.findById(loadslipDraftDto.getShipmentID());
    if (optionalShipment.isPresent()) {
      List<Loadslip> shipmentloadslips = loadslipRepository.findAllByShipmentShipmentIdAndStatusNot(optionalShipment.get().getShipmentId(), LoadslipStatus.CANCELLED);
      // Fetch Truck from truck reporting by assigned truck for the shipment
      Optional<TruckReport> optionalTruckReport = truckReportRepo.findByTruckTruckNumberAndShipmentIDAndReportLocation(optionalShipment.get().getTruckNumber(),
          optionalShipment.get().getShipmentId(), loggedInUser.getPlantCode());
      if (optionalTruckReport.isPresent()) {
        truckReport = optionalTruckReport.get();
        loadslip.setBay(truckReport.getBay());
        Optional<Freight> optionalFreight = calculateFreight(loadslipDraftDto, truckReport, null);
        if (optionalFreight.isPresent()) {
          Freight freight = optionalFreight.get();
          if (truckReport.getTTHrs() == null || freight.getTtDays() > truckReport.getTTHrs())
            truckReport.setTTHrs(freight.getTtDays());
        }
        if (truckReport.getStatus().equals(TruckReportStatus.GATED_IN)) {
          truckReport.setStatus(TruckReportStatus.ASSIGN_LS);
        }
      }
      optionalShipment.get().setStopType(deriveShipmentStopType(shipmentloadslips, loadslipDraftDto.getSource(), loadslipDraftDto.getDestination()));
      // When Save or  update Loadslip,calculate Shipment TTE, Weight and Volume Utilizations
      Shipment shipment = caluculateShipmentUtilizations(loadslip, optionalShipment.get(), loadslipDraftDto);
      loadslip.setShipment(shipment);
      // Save Shipment_Stop Table,use Akshay's db procedure for inserting data in shipment_stop table
//      saveShipmentStopDetails(shipment, loadslip, loggedInUser);
    }

    loadslip = loadslipRepository.save(loadslip);

    LoadSlipResponseDto loadSlipResponseDto = null;
    loadSlipResponseDto = new LoadSlipResponseDto(loadslip.getLoadslipId(), loadslip.getShipment().getShipmentId(),
        null, loadslip.getLsprintDate() != null ? DateUtils.formatDate(new Date(loadslip.getLsprintDate().getTime()), Constants.DATE_TIME_FORMAT) : null, loadslip.getShipment().getVariant1(), loadslip.getShipment().getVariant2(),
        loadslip.getShipment().getFreightAvailability(),
        loadslip.getShipment().getActualTruckType(),
        new ArrayList(), null, loadslip.getLoadslipType());
    return new ApiResponse(HttpStatus.OK, "Loadslip saved as Draft successfully", loadSlipResponseDto);
  }

  @Override
  public ApiResponse shareTruck(String shipmentId, String pickupLoc, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(shipmentId)) {
      return new ApiResponse(HttpStatus.BAD_REQUEST, "Shipment Id is Required");
    }
    if (StringUtils.isEmpty(pickupLoc)) {
      return new ApiResponse(HttpStatus.BAD_REQUEST, "Pickup Location is Required");
    }
    Optional<Shipment> optionalShipment = shipmentRepository.findById(shipmentId);
    if (optionalShipment.isPresent()) {
      SharedTruckId sharedTruckId = new SharedTruckId(optionalShipment.get().getShipmentId(), pickupLoc);
      SharedTruck sharedTruck = new SharedTruck(sharedTruckId, Status.OPEN.name());
      sharedTruckRepository.save(sharedTruck);
      optionalShipment.get().setStopType(StopType.SHR.name());
      shipmentRepository.save(optionalShipment.get());
      return new ApiResponse(HttpStatus.OK, "Truck is shared with the plant " + pickupLoc);
    } else {
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("Shipment not found with this shipment Id %s ", shipmentId));
    }
  }

  @Override
  public ApiResponse getAllPlantCodes(ApplicationUser loggedInUser) {
    // list of plant codes except current loggedIn User plant
    return new ApiResponse(HttpStatus.OK, "", locationRepository.findAllOtherPlantLocations(loggedInUser.getPlantCode()));
  }

  @Override
  public ApiResponse getSharedTrucksInfo(ApplicationUser loggedInUser) {
    // Fetch All shared trucks info for the current loggedIn user plant
    List<SharedTruck> sharedTrucks = null;
    if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
      sharedTrucks = sharedTruckRepository.findAllByStatus(Status.OPEN.name());
    } else {
      sharedTrucks = sharedTruckRepository.findAllBySharedTruckIdPickUpLocAndStatus(loggedInUser.getPlantCode(), Status.OPEN.name());
    }
    List<SharedTruckInfo> sharedTruckInfos = new ArrayList<>();
    if (sharedTrucks != null && !sharedTrucks.isEmpty()) {
      sharedTrucks.parallelStream().forEach(sharedTruck -> {
        Optional<Shipment> optionalShipment = shipmentRepository.findById(sharedTruck.getSharedTruckId().getShipmentId());
        if (optionalShipment.isPresent()) {
          Shipment shipment = optionalShipment.get();
          Optional<String> loadslipType = loadslipRepository.findAllByShipmentShipmentId(shipment.getShipmentId()).stream().findFirst().map(t -> t.getLoadslipType());
          String type = loadslipType.isPresent() ? loadslipType.get() : null;
          SharedTruckInfo sharedTruckInfo = new SharedTruckInfo(optionalShipment.get().getShipmentId(), shipment.getTruckNumber(), shipment.getTruckType(),
              shipment.getActualTruckType(), shipment.getVariant1(), shipment.getVariant2(), shipment.getContainerNum(), type);
          if (!UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())){
            Optional<TruckReport> optionalTruckReport = truckReportRepo.findByShipmentIDAndReportLocation(optionalShipment.get().getShipmentId(), loggedInUser.getPlantCode());
            if (!optionalTruckReport.isPresent()){
              sharedTruckInfo.setAllowCreateLS(false);
            }else{
              if (TruckReportStatus.REPORTED.equals(optionalTruckReport.get().getStatus())){
                sharedTruckInfo.setAllowCreateLS(false);
              }else{
                sharedTruckInfo.setAllowCreateLS(true);
              }
            }

          }
          sharedTruckInfos.add(sharedTruckInfo);
        }



      });
    }
    return new ApiResponse(HttpStatus.OK, "", sharedTruckInfos);
  }

  private Loadslip updateMultiLoadslip(Loadslip existingLoadslip, List<String> errorMessages, LoadslipDraftDto loadslipDraftDto, ApplicationUser loggedInUser) {
    // All source locations from External warehouses or location = 1007
    List<String> actualSourceList = getSourceLocationsForExternalWarehouse(loadslipDraftDto);
    validatePlanQtyWithLSQty(loadslipDraftDto, loggedInUser, errorMessages, actualSourceList);
    if (!errorMessages.isEmpty()) {
      return null;
    }
    // Step1: Delete all Loadslip details and bom details from Loadslip_detail and loadslip_detail_bom and roll back dipatch plan qunatities
    //Step2: set required data to existing loadlsip
    //step3: create loadslip_detail and loadslip_detail_bom
    // step4 : update dispatch plan qunatities
    // step5: continue rest of the process

    //Delete loadslip detail and bom data & Reset or rollback dispatch plan quantities every time we update the loadslip
    List<DispatchPlanItemInfo> rolledBackPlans = new ArrayList<>();


    List<String> lsDetailsItemIdList = loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().map(LoadslipDraftDataDto::getItemId).collect(Collectors.toList());
    List<String> lsDetailsItemDescList = loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().map(LoadslipDraftDataDto::getItemDesc).collect(Collectors.toList());
    List<LoadslipDetail> details = new ArrayList<>();
    if (loggedInUser.getRole().isRDCPlannerOrRDCGate()) {
      details = loadslipDetailRepository
          .findAllByLoadslipDetailIdItemIdInAndItemDescriptionInAndLoadslipDestLocAndLoadslipSourceLocInAndLoadslipStatusNotInAndLoadslipLoadslipIdNot(
              lsDetailsItemIdList, lsDetailsItemDescList, loadslipDraftDto.getDestination(), actualSourceList, Constants.LoadslipStatus.getCompletedLoadslipStatuses(), existingLoadslip.getLoadslipId());

    } else {
      List<String> lsDetailsItemBatchCodeList = loadslipDraftDto.getLoadslipDraftDataDtos().parallelStream().map(LoadslipDraftDataDto::getBatchCode).collect(Collectors.toList());
      details = loadslipDetailRepository
          .findAllByLoadslipDetailIdItemIdInAndItemDescriptionInAndBatchCodeInAndLoadslipDestLocAndLoadslipSourceLocInAndLoadslipStatusNotInAndLoadslipLoadslipIdNot(
              lsDetailsItemIdList, lsDetailsItemDescList,
              lsDetailsItemBatchCodeList, loadslipDraftDto.getDestination(), actualSourceList, Constants.LoadslipStatus.getCompletedLoadslipStatuses(), existingLoadslip.getLoadslipId());
    }
    details.addAll(existingLoadslip.getLoadslipDetails());

//		for (LoadslipDetail loadslipDetail : existingLoadslip.getLoadslipDetails()) {
    for (LoadslipDetail loadslipDetail : details) {
      List<DispatchPlanItemInfo> dispatchPlanItemInfos = fetchDispatchPlansByLoadslipItem(loadslipDetail, loggedInUser, existingLoadslip.getDestLoc());
      rollBackDispatchPlan(existingLoadslip, dispatchPlanItemInfos, loadslipDetail, rolledBackPlans, existingLoadslip.getStatus().equals(LoadslipStatus.SENT_SAP));
    }
    if (!rolledBackPlans.isEmpty()) {
      dispatchPlanItemInfoRepository.saveAll(rolledBackPlans);
    }


    // Delete all Loadslip details and bom details from Loadslip_detail and loadslip_detail_bom for this loadslip
    List<LoadslipDetail> existingDetailList = loadslipDetailRepository.findAllByLoadslipDetailIdLoadslipId(existingLoadslip.getLoadslipId());
    loadslipDetailRepository.deleteAll(existingDetailList);
    existingLoadslip.getLoadslipDetails().clear();
    List<LoadslipDetailBom> existingBomList = loadslipDetailBomRepository.findAllByLoadslipDetailBomIdLoadslipId(existingLoadslip.getLoadslipId());
    loadslipDetailBomRepository.deleteAll(existingBomList);
    existingLoadslip.getLoadslipDetailBoms().clear();
//    existingLoadslip.setLoadslipDetails(new ArrayList<>());
//    existingLoadslip.setLoadslipDetailBoms(new ArrayList<>());

    // Re-create Loadslip details and Bom data
    // For Exports and JIT Loadslips
    List<String> invoiceList = loadslipDraftDto.getInvoiceList();
    if (invoiceList != null && !invoiceList.isEmpty()) {
      saveLoadslipDeatilAndBomExport(errorMessages, loadslipDraftDto, existingLoadslip, loggedInUser);
      //Update invoice date and invoice number null in case of update
      existingLoadslip.setSapInvoiceDate(null);
      existingLoadslip.setSapInvoice(null);
    }
    // For FGS Loadslips
    else {
      saveLoadslipDeatilAndBom(errorMessages, loadslipDraftDto, existingLoadslip, loggedInUser, "UPDATE", actualSourceList);
    }
    // If any validation error for loaded qunatity, return immediately with null
    if (!errorMessages.isEmpty()) {
      return null;
    }
//    loadslipRepository.saveAndFlush(existingLoadslip);

    //set loadslip data
    setLoadslipData(loadslipDraftDto, existingLoadslip, loggedInUser, "UPDATE");

    Shipment existingShipment = existingLoadslip.getShipment();

    // When Save or  update Loadslip,calculate Shipment TTE, Weight and Volume Utilizations
    Shipment shipment = caluculateShipmentUtilizations(existingLoadslip, existingShipment, loadslipDraftDto);
    existingLoadslip.setShipment(shipment);

    existingLoadslip = loadslipRepository.save(existingLoadslip);
    //Save or Update shipment stop table
//    executeShipmentStopProcedure(shipment.getShipmentId(), loggedInUser.getUserId());

    // Fetch Truck from truck reporting by assigned truck for the shipment
    Optional<TruckReport> optionalTruckReport = truckReportRepo.findByTruckTruckNumberAndShipmentIDAndReportLocation(existingShipment.getTruckNumber(), existingShipment.getShipmentId(), loggedInUser.getPlantCode());
    if (optionalTruckReport.isPresent()) {
      //Update Truck dest loc based on the last stop loadslip destination location
      TruckReport truckReport = optionalTruckReport.get();
//      truckReport = updateTruckDestinationByDropSeq(truckReport);
      if (truckReport.getStatus().equals(TruckReportStatus.GATED_IN)) {
        truckReport.setStatus(TruckReportStatus.ASSIGN_LS);
        truckReport.setUpdateDate(new Date());
        truckReport.setUpdateUser(loggedInUser.getUserId());
      }
      truckReportRepo.save(truckReport);
    }

    return existingLoadslip;
  }

  private List<String> getSourceLocationsForExternalWarehouse(LoadslipDraftDto loadslipDraftDto) {
    List<String> loadslipWithSource = new ArrayList<>();
    List<String> actualSourceList = new ArrayList<>();
    String actualSource = utilityService.getLinkedPlntLoc(loadslipDraftDto.getSource(), loadslipDraftDto.getDestination());
//      loadslipWithSource.add(actualSource);
    if (!loadslipDraftDto.getSource().contentEquals(actualSource) || loadslipDraftDto.getSource().contentEquals("1007")) {
      //actualSourceList.add(actualSource);
      //when loadslipDraftDto.getSource() = TNR2/TNR4/TNR5/TNR6/TNR7 then actualSource will be 1007
      //* then all the linked ext_warehouse of 1007 and add to actual_source_list
      List<String> sourceList = utilityService.getLinkedExtWareHouse(actualSource);
      if (sourceList != null && !sourceList.isEmpty()) {
        loadslipWithSource.addAll(sourceList);
      }
      actualSourceList = getLoadslipWithActualSourceList(loadslipWithSource);
    }
    actualSourceList.add(actualSource);
    return actualSourceList;
  }

  private Loadslip saveShipmentMultiDropLoadslip(List<String> errorMessages, LoadslipDraftDto loadslipDraftDto, ApplicationUser loggedInUser) {
    List<String> actualSourceList = getSourceLocationsForExternalWarehouse(loadslipDraftDto);
    validatePlanQtyWithLSQty(loadslipDraftDto, loggedInUser, errorMessages, actualSourceList);
    if (!errorMessages.isEmpty()) {
      return null;
    }
    Loadslip loadslip = new Loadslip();
    loadslip.setLoadslipId(indentSummaryRepository.findIndentSequence(Constants.LOADSLIP_SEQ_PREFIX,
        loadslipDraftDto.getSource(), loadslipDraftDto.getDestination()));
    loadslip.setSourceLoc(loadslipDraftDto.getSource());
    loadslip.setDestLoc(loadslipDraftDto.getDestination());
    // To generate LoadslipType,param3 is item_id (t will be any random Item ID from
    // respective Loadslip as per requirements)
    loadslip.setLoadslipType(loadslipRepository.findOrderType(loadslipDraftDto.getSource(),
        loadslipDraftDto.getDestination(), loadslipDraftDto.getLoadslipDraftDataDtos().get(0).getItemId()));
    if (!loadslipDraftDto.getInvoiceList().isEmpty()) {
      loadslip.setLoadslipType(loadslipDraftDto.getType().name());
    }

    /*Setting loadslip MKT_SEG with OrderTypeLookUp.Market_segment when OrderTypeLookup.orderType = loadslip.loadslipType*/
    String mktSeg = getLoadslipMktSegFromLSType(loadslip.getLoadslipType());
    if (!StringUtils.isEmpty(mktSeg))
      loadslip.setMarketSegment(mktSeg);

    //set loadslip data
    setLoadslipData(loadslipDraftDto, loadslip, loggedInUser, "SAVE");

    // For Exports and JIT Loadslips
    List<String> invoiceList = loadslipDraftDto.getInvoiceList();
    if (invoiceList != null && !invoiceList.isEmpty()) {
      saveLoadslipDeatilAndBomExport(errorMessages, loadslipDraftDto, loadslip, loggedInUser);
    }
    // For FGS Loadslips
    else {
      // Save LOADSLIP_DETAIL and LOADSLIP_DETAIL_BOM
      saveLoadslipDeatilAndBom(errorMessages, loadslipDraftDto, loadslip, loggedInUser, "SAVE", actualSourceList);
    }
    if (!errorMessages.isEmpty()) {
      return null;
    }
    // Shipment
    TruckReport truckReport = null;
    Optional<Shipment> optionalShipment = shipmentRepository.findById(loadslipDraftDto.getShipmentID());
    if (optionalShipment.isPresent()) {
      List<Loadslip> shipmentloadslips = loadslipRepository.findAllByShipmentShipmentIdAndStatusNot(optionalShipment.get().getShipmentId(), LoadslipStatus.CANCELLED);
      // Fetch Truck from truck reporting by assigned truck for the shipment
      Optional<TruckReport> optionalTruckReport = truckReportRepo.findByTruckTruckNumberAndShipmentIDAndReportLocation(optionalShipment.get().getTruckNumber(),
          optionalShipment.get().getShipmentId(), loggedInUser.getPlantCode());
      if (optionalTruckReport.isPresent()) {
        truckReport = optionalTruckReport.get();
        loadslip.setBay(truckReport.getBay());
        Optional<Freight> optionalFreight = calculateFreight(loadslipDraftDto, truckReport, null);
        if (optionalFreight.isPresent()) {
          Freight freight = optionalFreight.get();
          if (truckReport.getTTHrs() == null || freight.getTtDays() > truckReport.getTTHrs())
            truckReport.setTTHrs(freight.getTtDays());
        }
        if (truckReport.getStatus().equals(TruckReportStatus.GATED_IN)) {
          truckReport.setStatus(TruckReportStatus.ASSIGN_LS);
        }
//        truckReport.setStatus(TruckReportStatus.ASSIGN_LS);
//        truckReport.setBayStatus(null);
      }
      optionalShipment.get().setStopType(deriveShipmentStopType(shipmentloadslips, loadslipDraftDto.getSource(), loadslipDraftDto.getDestination()));
      // When Save or  update Loadslip,calculate Shipment TTE, Weight and Volume Utilizations
      Shipment shipment = caluculateShipmentUtilizations(loadslip, optionalShipment.get(), loadslipDraftDto);
      loadslip.setShipment(shipment);
      // Save Shipment_Stop Table,use Akshay's db procedure for inserting data in shipment_stop table
//      saveShipmentStopDetails(shipment, loadslip, loggedInUser);
    }

    loadslip = loadslipRepository.save(loadslip);
    //Save or Update shipment stop table
  /*  executeShipmentStopProcedure(loadslipDraftDto.getShipmentID(), loggedInUser.getUserId());
    //Update Truck dest loc based on the last stop loadslip destination location
    updateTruckDestinationByDropSeq(truckReport);
    if (truckReport != null) {
      truckReportRepo.save(truckReport);
    }
    */
    return loadslip;
  }

  private Optional<Freight> calculateFreight(LoadslipDraftDto loadslipDraftDto, TruckReport truckReport, String linkedPlant) {
    List<Freight> freights = freightRepository.findBySourceLocAndDestLocAndServprovAndTruckTypeAndCondition1
        (!StringUtils.isEmpty(linkedPlant) ? linkedPlant : loadslipDraftDto.getSource(),
            loadslipDraftDto.getDestination(), truckReport.getServprov(), !StringUtils.isEmpty(loadslipDraftDto.getActualTruckType()) ? loadslipDraftDto.getActualTruckType() : truckReport.getReportedTruckType(), loadslipDraftDto.getVariant1());
    Date date = new Date();
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
    return freightList.stream().findFirst();
  }

  private TruckReport updateTruckDestinationByDropSeq(TruckReport truckReport) {
    if (!StringUtils.isEmpty(truckReport) && !StringUtils.isEmpty(truckReport.getShipmentID())) {
      // Fetch max Stop seq value (last drop or stop) from shipment_stop
      Optional<ShipmentStop> lastStopShipmentStop = shipmentStopRepository.findTop1ByShipmentStopIdShipmentIdOrderByShipmentStopIdStopNumDesc(truckReport.getShipmentID());
      if (lastStopShipmentStop.isPresent()) {
        truckReport.setDestinationLocation(lastStopShipmentStop.get().getLocationId());
      }
    }
    return truckReport;
  }

  private String deriveShipmentStopType(List<Loadslip> loadslips, String currentSource, String currentDest) {
//    List<Loadslip> loadslips = loadslipRepository.findAllByShipmentShipmentIdAndStatusNot(shipmentId, LoadslipStatus.CANCELLED);
    int totalLoadSlips = loadslips.size();
    int sameSourceLoadslips = loadslips.parallelStream().filter(loadslip -> loadslip.getSourceLoc().equals(currentSource)).collect(Collectors.toList()).size();
    int sameDestLoadslips = loadslips.parallelStream().filter(loadslip -> loadslip.getDestLoc().equals(currentDest)).collect(Collectors.toList()).size();
    if (sameSourceLoadslips == totalLoadSlips && sameDestLoadslips == totalLoadSlips) {
      return StopType.S.name();
    } else if (sameSourceLoadslips != totalLoadSlips && sameDestLoadslips == totalLoadSlips) {
      return StopType.MP.name();
    } else if (sameSourceLoadslips == totalLoadSlips && sameDestLoadslips != totalLoadSlips) {
      return StopType.MD.name();
    } else {
      return StopType.MPMD.name();
    }
  }

  @Override
  public ApiResponse getExportDestinations(ApplicationUser applicationUser) {
    // removed String formart

    return new ApiResponse(HttpStatus.OK, "unique DEST_LOC from DEL_INV_HEADER table for source .",
        delInvHeaderRepository.findOpenDestinations(applicationUser.getPlantCode()));
  }


  @Override
  public ApiResponse getExportInvoice(String destLocation, String loadslipId, ApplicationUser applicationUser) {
    // removed String formart

    List<String> exportInvoiceList = findExportInvoices(DelInvType.FGS_EXP, applicationUser.getPlantCode(), destLocation, loadslipId, applicationUser);
    List<DelInvHeader> delHeaderList = delInvHeaderRepository.findByInvoiceNumberInOrderByInsertDateDesc(exportInvoiceList);
    List<Map<String, String>> gatedInTrucks = truckReportRepo.findTrucksByStatusAndStatusNotAndSourceAndDestLocAndReportingLocWithContainerNo(TruckReportStatus.GATED_IN.name(),
        TruckReportStatus.INTRANSIT.name(), applicationUser.getPlantCode(), destLocation, applicationUser.getPlantCode());
    List<ExportInfoDto> exportInfoList = null;//delInvLineRepository.getAllExportInfoForInvoices(exportInvoiceList);
    ExportDto exportDto = new ExportDto(delHeaderList, null, gatedInTrucks, null, exportInfoList, deriveTubeAndFlapBatchCodes(), null);
    return new ApiResponse(HttpStatus.OK, "unique Invoices from DEL_INV_HEADER table for source to destination.",
        exportDto);
  }

  @Override
  public ApiResponse getExportInvoiceWithType(DelInvType type, String destLocation, String loadslipId, ApplicationUser applicationUser) {
    // removed String formart
//Here we were fetching the invoice numbers from DelInvHeader and Quering on the same table with IN clause.
//    Getting error when there are more than 1000 records.

//    List<String> exportInvoiceList = findExportInvoices(type, applicationUser.getPlantCode(), destLocation, loadslipId, applicationUser);
//    List<DelInvHeader> delHeaderList = delInvHeaderRepository.findByInvoiceNumberInOrderByInsertDateDesc(exportInvoiceList);

// Now combined the above two commented lines in single query
    List<DelInvHeader> delHeaderList = null;
    if (!StringUtils.isEmpty(loadslipId)){
      delHeaderList = delInvHeaderRepository.findAllHearderBySourceLoadslipTypeAndDest(applicationUser.getPlantCode(), loadslipId, type, destLocation);
    }else{
      delHeaderList = delInvHeaderRepository.findAllHearderBySourceTypeAndDest(applicationUser.getPlantCode(), type, destLocation);
    }
    List<Map<String, String>> gatedInTrucks = truckReportRepo.findTrucksByStatusAndStatusNotAndSourceAndDestLocAndReportingLocWithContainerNo(TruckReportStatus.GATED_IN.name(),
        TruckReportStatus.INTRANSIT.name(), applicationUser.getPlantCode(), destLocation, applicationUser.getPlantCode());
    List<ExportInfoDto> exportInfoList = null;//delInvLineRepository.getAllExportInfoForInvoices(exportInvoiceList);
    ExportDto exportDto = new ExportDto(delHeaderList, null, gatedInTrucks, null, exportInfoList, deriveTubeAndFlapBatchCodes(), null);
    return new ApiResponse(HttpStatus.OK, "unique Invoices from DEL_INV_HEADER table for source to destination.",
        exportDto);
  }


  public List<String> findExportInvoices(DelInvType delInvType, String sourceLoc, String destLocation, String loadslipId, ApplicationUser applicationUser) {
    List<String> exportInvoiceList = new ArrayList<>();
    //TODO chagne setting default value
    if (!StringUtils.isEmpty(loadslipId)) {
      exportInvoiceList.addAll(delInvHeaderRepository.findExportInvoices(sourceLoc, loadslipId, delInvType, destLocation));
    } else {
      exportInvoiceList.addAll(delInvHeaderRepository.findInCompleteExportInvoices(sourceLoc, delInvType, destLocation));
    }
    return exportInvoiceList;
  }


  @Override
  public ApiResponse getExportInvoiceItem(List<String> invoiceList, ApplicationUser loggedInUser) {
    if (invoiceList != null && !invoiceList.isEmpty()) {

//      Getting error when user selects more than 1000 invoices
      /*List<DelInvLine> itemList = delInvLineRepository.findByDelInvLineIdInvoiceNumberIn(invoiceList);
      List<ExportInfoDto> exportInfoList = delInvLineRepository.getAllExportInfoForInvoices(invoiceList);
      exportInfoList.forEach((t) -> {
        updateExportDetailsNew(delInvHeaderRepository.getLoadSlipIdForInvoice(t.getInvoiceNumber()), t);
      });*/
//      divided the invoice number list into subList of 999 items, since IN clause allows only 1000 values
      List<ExportInfoDto> exportInfoList = new ArrayList<>();
      List<DelInvHeader> delInvHeaderList = new ArrayList<>();
      List<List<String>> partitions = ListUtils.partition(invoiceList, 999);
      partitions.forEach(sunInvList -> {
        exportInfoList.addAll(delInvLineRepository.getAllExportInfoForInvoices(sunInvList));
        delInvHeaderList.addAll(delInvHeaderRepository.findInvNumLoadslipId(sunInvList));
      });
      exportInfoList.forEach((t) -> {
        Optional<DelInvHeader> optionalDelInvHeader = delInvHeaderList.parallelStream().filter(del -> del.getInvoiceNumber().equals(t.getInvoiceNumber())).findFirst();
        updateExportDetailsNew(optionalDelInvHeader.isPresent() ? optionalDelInvHeader.get().getLoadslipId() : null, t);
      });

      ExportDto exportDto = new ExportDto(null, null, null,
          null, exportInfoList, deriveTubeAndFlapBatchCodes(), null);
      return new ApiResponse(HttpStatus.OK, "Export Invoice Data", exportDto);
    } else {
      throw new InvalidParameterException("No InvoiceList available");
    }
  }

  @Deprecated
  public ExportInfoDto updateExportDetails(String loadslipID, ExportInfoDto exportInfoDto) {

    if (loadslipID != null) {

      Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(loadslipID);

      List<LoadslipDraftDataDto> loadslipDataList = new ArrayList<>();
      LoadslipDraftDto draftDto = new LoadslipDraftDto();
      Object avlQty;
      Object valveCompQty;
      Object tubeCompQty;
      Object flapCompQty;
      Object itemCategory;
      Object priority;
      if (optionalLoadslip.isPresent()) {

        Loadslip loadslip = optionalLoadslip.get();
        List<String> itemIds = loadslip.getLoadslipDetails().parallelStream()
            .map(LoadslipDetail::getLoadslipDetailId).map(LoadslipDetailId::getItemId)
            .collect(Collectors.toList());

        for (LoadslipDetail loadslipDetail : loadslip.getLoadslipDetails()) {

          avlQty = 0;
          valveCompQty = 0;
          tubeCompQty = 0;
          flapCompQty = 0;
          itemCategory = "";
          priority = "";
          Optional<LoadslipDetailBom> filterdBom = loadslip.getLoadslipDetailBoms().parallelStream()
              .filter(obj -> obj.getLoadslipDetailBomId().getLoadslipId()
                  .equals(loadslipDetail.getLoadslipDetailId().getLoadslipId())
                  && obj.getLoadslipDetailBomId().getItemId()
                  .equals(loadslipDetail.getLoadslipDetailId().getItemId())
                  && obj.getLoadslipDetailBomId().getLineNo()
                  .equals(loadslipDetail.getLoadslipDetailId().getLineNo()))
              .findFirst();
          return exportInfoDto.updateLoadSlipdetails(loadslipDetail, filterdBom,
              avlQty, tubeCompQty, flapCompQty, valveCompQty);

        }
      }
    }
    return exportInfoDto;
  }


  public ExportInfoDto updateExportDetailsNew(String loadslipID, ExportInfoDto exportInfoDto) {

    if (loadslipID != null) {

      Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(loadslipID);

      List<LoadslipDraftDataDto> loadslipDataList = new ArrayList<>();
      LoadslipDraftDto draftDto = new LoadslipDraftDto();
      Object avlQty;
      Object valveCompQty;
      Object tubeCompQty;
      Object flapCompQty;
      Object itemCategory;
      Object priority;
      if (optionalLoadslip.isPresent()) {

        Loadslip loadslip = optionalLoadslip.get();
        List<String> itemIds = loadslip.getLoadslipDetails().parallelStream()
            .map(LoadslipDetail::getLoadslipDetailId).map(LoadslipDetailId::getItemId)
            .collect(Collectors.toList());

        //for (LoadslipDetail loadslipDetail : loadslip.getLoadslipDetails())

        Optional<LoadslipDetail> loadslipDetailOptional = loadslip.getLoadslipDetails().parallelStream()
            .filter(t -> {
              if (t.getInvoiceNumber() != null && exportInfoDto.getInvoiceNumber() != null) {
                return t.getLoadslipDetailId().getItemId().equals(exportInfoDto.getItemId())
                    && t.getInvoiceNumber().equals(exportInfoDto.getInvoiceNumber());
              } else {
                return t.getLoadslipDetailId().getItemId().equals(exportInfoDto.getItemId());
              }
            }).findFirst();
        if (loadslipDetailOptional.isPresent()) {
          LoadslipDetail loadslipDetail = loadslipDetailOptional.get();
          avlQty = 0;
          valveCompQty = 0;
          tubeCompQty = 0;
          flapCompQty = 0;
          Optional<LoadslipDetailBom> filterdBom = Optional.empty();
          /* loadslip.getLoadslipDetailBoms().parallelStream()
                    .filter(obj -> obj.getLoadslipDetailBomId().getLoadslipId()
                            .equals(loadslipDetail.getLoadslipDetailId().getLoadslipId())
                            && obj.getLoadslipDetailBomId().getItemId()
                            .equals(loadslipDetail.getLoadslipDetailId().getItemId())
                            && obj.getLoadslipDetailBomId().getLineNo()
                            .equals(loadslipDetail.getLoadslipDetailId().getLineNo()))
                    .findFirst();*/
          return exportInfoDto.updateLoadSlipdetails(loadslipDetail, filterdBom,
              avlQty, tubeCompQty, flapCompQty, valveCompQty);
        }
      }
    }
    return exportInfoDto;
  }

  @Override
  public ApiResponse deleteSOSTO(String loadslipId, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(loadslipId))
      return new ApiResponse(HttpStatus.NOT_FOUND, "loadslip Id can't be empty");
    Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(loadslipId);
    if (!optionalLoadslip.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("No loadslip Id found with id %s", loadslipId));
    }
    Loadslip loadslip = optionalLoadslip.get();
    if (StringUtils.isEmpty(loadslip.getStoSoNum())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("STO/SO not found with loadslip %s", loadslip));
    }
    if (!StringUtils.isEmpty(loadslip.getSapInvoice())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "You are not allowed to delete STO/SO for loadslip with invoice");
    }
    HttpHeaders headers = new HttpHeaders();
    headers.set(Constants.API_AUTH, Utility.convertStringBase64Format(sapUserName + ":" + sapPassword));
    headers.set(Constants.LOAD_SLIP_ID, loadslipId);
    HttpEntity httpEntity = new HttpEntity<>(headers);
    ResponseEntity responseEntity = restTemplate.postForEntity(sostoURL, httpEntity, ResponseEntity.class);
    if (responseEntity.getStatusCodeValue() == 200) {
      if (responseEntity.getHeaders().get(Constants.SAP_RESPONSE).get(0).equalsIgnoreCase("ERROR")) {
        return new ApiResponse(HttpStatus.OK, "STO/SO number is not deleted ", null);
      }
      List<DispatchPlanItemInfo> rollBackedPlans = new ArrayList<>();
      for (LoadslipDetail loadslipDetail : loadslip.getLoadslipDetails()) {
        List<DispatchPlanItemInfo> dispatchPlanItemInfos = fetchDispatchPlansByLoadslipItem(loadslipDetail, loggedInUser, optionalLoadslip.get().getDestLoc());
        // Roll back plan qty from loaded to reserved or dispatched to reserved (if qty are dispatched)
        //isSentToSAP is true , coz status is SENT_SAP(LS confirmed),Delete STO/SO is enabled once LS is SENT TO SAP
        rollBackDispatchPlan(optionalLoadslip.get(), dispatchPlanItemInfos, loadslipDetail, rollBackedPlans, true);
      }
      if (!rollBackedPlans.isEmpty()) {
        dispatchPlanItemInfoRepository.saveAll(rollBackedPlans);
      }

      ApiResponse lsResponse = getLoadslipDetails(loadslip.getLoadslipId(), loggedInUser);
      LoadslipDraftDto lsData = (LoadslipDraftDto) lsResponse.getData();
      // TODO: Need to change later, for now handling as following way
      lsData.setSostoNumber(false);
      lsData.setSostoNumberVal(null);
      lsData.setConfirmedDate(null);

      //Loadslip Line items can not be deleted if there is a STO/SO number in loadlsip or scan qty is available in Loadslip detail (Loadslip item)
      lsData.setLoadslipDraftDataDtos(lsData.getLoadslipDraftDataDtos().parallelStream().peek(lsd -> {
        if (lsData.isSostoNumber() || lsd.getScannedQty() > 0) {
          lsd.setCanLSItemDeleted(false);
        } else {
          lsd.setCanLSItemDeleted(true);
        }
      }).collect(Collectors.toList()));


      return new ApiResponse(HttpStatus.OK, "STO/SO number is deleted successfully ", lsData);
    }
    return new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "failed some business logic");

  }

  @Override
  public void saveShipmentStopAndTruckDestination(String loadslipId, ApplicationUser loggedInUser) {
    Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(loadslipId);
    if (optionalLoadslip.isPresent()) {
      //Save or Update shipment stop table
      Shipment shipment = optionalLoadslip.get().getShipment();
      executeShipmentStopProcedure(shipment.getShipmentId(), loggedInUser.getUserId());
      Optional<TruckReport> optionalTruckReport = truckReportRepo.findByTruckTruckNumberAndShipmentIDAndReportLocation(shipment.getTruckNumber(), shipment.getShipmentId(), loggedInUser.getPlantCode());
      if (optionalTruckReport.isPresent()) {
        setTruckReportActivity(optionalTruckReport.get(), optionalLoadslip.get(), loggedInUser);
        //Update Truck dest loc based on the last stop loadslip destination location
        updateTruckDestinationByDropSeq(optionalTruckReport.get());
        truckReportRepo.save(optionalTruckReport.get());
      }
    }

  }

  @Override
  public ApiResponse sendActualShipment(String shipmentId) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(Constants.API_AUTH, Utility.convertStringBase64Format(sapUserName + ":" + sapPassword));
    headers.set(Constants.Shipment_id, shipmentId);
    HttpEntity httpEntity = new HttpEntity<>(headers);
    ResponseEntity responseEntity = restTemplate.postForEntity(sendActualShipmentUrl, httpEntity, ResponseEntity.class);
    if (responseEntity.getStatusCodeValue() == 200) {
      return new ApiResponse(HttpStatus.OK, "Request submitted successfully");
    } else if (responseEntity.getStatusCodeValue() == 500) {
      return new ApiResponse(HttpStatus.OK, "500 Internal server error");
    } else {
      return new ApiResponse(HttpStatus.NOT_FOUND, "No response from SAP");
    }
  }

  @Override
  public ByteArrayOutputStream getLoadReceiptPdf(Loadslip loadslip) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    String ConsignorAddress = null;
    try {

      List<Map<String, Object>> lrStampDetails = loadReceiptRepository.getLrStampDetails(loadslip.getLoadslipId());
      List<Map<String, Object>> lrFromAddress = loadReceiptRepository.getLrFromAdress(loadslip.getLoadslipId());
      Map<String, Object> destAddres = loadReceiptRepository.getToAddressToELr(loadslip.getDestLoc());
      String vLrTransporterDesc = loadReceiptRepository.getLrTransporterDesc(loadslip.getLoadslipId());
      Document document = new Document(PageSize.A4, 0, 0, 5, 50);
      PdfWriter pdfWriter = PdfWriter.getInstance(document, baos);
      PDFHeaderAndFooter pdfHeaderAndFooter = new PDFHeaderAndFooter();
      pdfWriter.setPageEvent(pdfHeaderAndFooter);
      document.open();

      List<String> labelList = new ArrayList<>();
      labelList.add("CONSIGNOR COPY");
      labelList.add("CONSIGNEE COPY");
      labelList.add("TRANSPORTER COPY");
      labelList.add("OFFICE COPY");
      List<LoadslipInvoiceHeader> invoiceHeaders = invoiceHeaderRepo.findAllByInvoiceHeaderLoadslipId(loadslip.getLoadslipId());
      String transGSTNum = null;
      String transPanNum = null;
      if (!StringUtils.isEmpty(loadslip.getShipment().getTransporterSapCode())) {
        Optional<MTTransporter> optionalTransporter = transporterRepo.findById(loadslip.getShipment().getTransporterSapCode());
        if (optionalTransporter.isPresent()) {
          transGSTNum = optionalTransporter.get().getGstNo();
          transPanNum = optionalTransporter.get().getPanNo();
        }
      }
      PDFLRTableUtil pdfLrUtil = new PDFLRTableUtil();
      for (int i = 0; i < labelList.size(); i++) {
        PdfPTable outerTable = new PdfPTable(1);
        outerTable.setSplitLate(false);
        outerTable.setWidthPercentage(99);
        outerTable.setKeepTogether(true);
        outerTable.setSpacingBefore(20);

        PdfPCell cell = new PdfPCell();
        PdfPTable labelTable = pdfLrUtil.addLabelTable(document, labelList.get(i));
        labelTable.setSpacingBefore(0f);
        labelTable.setSpacingAfter(0f);
        cell = new PdfPCell(labelTable);
        cell.disableBorderSide(Rectangle.BOX);
        outerTable.addCell(cell);

        PdfPTable stampTable = pdfLrUtil.addStampTable(document, loadslip, lrStampDetails, transGSTNum, transPanNum);
        stampTable.setSpacingBefore(0f);
        stampTable.setSpacingAfter(0f);
        cell = new PdfPCell(stampTable);
        cell.disableBorderSide(Rectangle.BOX);
        outerTable.addCell(cell);

        PdfPTable fromToTable = pdfLrUtil.addFromToTable(document, loadslip, lrFromAddress, destAddres.get("DESTDESC") != null ? destAddres.get("DESTDESC").toString() : null);
        fromToTable.setSpacingBefore(0f);
        fromToTable.setSpacingAfter(0f);
        cell = new PdfPCell(fromToTable);
        cell.disableBorderSide(Rectangle.BOX);
        outerTable.addCell(cell);

        PdfPTable gapTable = pdfLrUtil.addGapTable(document, lrFromAddress, destAddres);
        gapTable.setSpacingBefore(0f);
        gapTable.setSpacingAfter(0f);
        cell = new PdfPCell(gapTable);
        cell.disableBorderSide(Rectangle.BOX);
        outerTable.addCell(cell);

        PdfPTable receivingTable = pdfLrUtil.addReceivingTable(document, loadslip);
        receivingTable.setSpacingBefore(0f);
        receivingTable.setSpacingAfter(0f);
        cell = new PdfPCell(receivingTable);
        cell.disableBorderSide(Rectangle.BOX);
        outerTable.addCell(cell);

        PdfPTable invSignTable = pdfLrUtil.addInvSignTable(document, loadslip, invoiceHeaders);
        invSignTable.setSpacingBefore(0f);
        invSignTable.setSpacingAfter(0f);
        cell = new PdfPCell(invSignTable);
        cell.disableBorderSide(Rectangle.BOX);
        outerTable.addCell(cell);

        PdfPTable declTable = pdfLrUtil.addDeclTable(document, vLrTransporterDesc);
        invSignTable.setSpacingBefore(0f);
        invSignTable.setSpacingAfter(0f);
        cell = new PdfPCell(declTable);
        cell.disableBorderSide(Rectangle.BOX);
        outerTable.addCell(cell);
        outerTable.setSpacingAfter(20);

        document.add(outerTable);

        if (i % 2 == 0) {
          DottedLineSeparator line = new DottedLineSeparator();
          line.setLineWidth(2);
          document.add(line);
        }
      }
      document.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
    return baos;
  }

  @Override
  public ApiResponse addCommentsToLoadslip(LoadslipMetaData loadslipMetaData, ApplicationUser applicationUser) {
    if (StringUtils.isEmpty(loadslipMetaData.getLoadslipId())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please Provide LoadslipId");
    }
    /*if (StringUtils.isEmpty(loadslipDraftDto.getComments())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Comments section cannot be empty");
    }*/
    Loadslip loadslip = loadslipRepository.getOne(loadslipMetaData.getLoadslipId().toString());
    if (StringUtils.isEmpty(loadslip)) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "No loadslip found with the given ID");
    }
    loadslip.setUpdateDate(new Date());
    loadslip.setUpdateUser(applicationUser.getUserId());
    loadslip.setComments(!StringUtils.isEmpty(loadslipMetaData.getComments()) ? loadslipMetaData.getComments().toString() : null);
    loadslip = loadslipRepository.save(loadslip);
    loadslipMetaData.setUpdateUser(loadslip.getUpdateUser());
    loadslipMetaData.setComments(loadslip.getComments());

    return new ApiResponse(HttpStatus.OK, "Comments successfully added to the loadslip: " + loadslip.getLoadslipId(), loadslipMetaData);
  }

  @Override
  public ApiResponse getLoadslipInvoiceData(String loadslipId, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(loadslipId)) {
      return new ApiResponse(HttpStatus.BAD_REQUEST, "Please provide loadslip Id");
    }
    Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(loadslipId);
    if (!optionalLoadslip.isPresent()) {
      return new ApiResponse(HttpStatus.BAD_REQUEST, String.format("No loadslip found with %s id", loadslipId));
    }
    Loadslip loadslip = optionalLoadslip.get();
    List<Map<String, Object>> invoiceData = loadslipRepository.getLoadslipInvoiceData(loadslipId);
    LoadslipInvoiceData loadslipInvoiceData = new LoadslipInvoiceData();
    List<InvoiceDataDto> invoiceDataDtos = invoiceData.stream().map(InvoiceDataDto::new).collect(Collectors.toList());
    LoadslipDraftDto draftDto = new LoadslipDraftDto();
    setLoadslipData(loadslip, draftDto, loggedInUser);
    loadslipInvoiceData.setLoadslipDraftDto(draftDto);
    loadslipInvoiceData.setInvoiceDataDtos(invoiceDataDtos);
    return new ApiResponse(HttpStatus.OK, "", loadslipInvoiceData);

  }

  @Override
  public ApiResponse addCommentsToTruckReport(TruckReportDto truckReportDto, ApplicationUser applicationUser) {
    if (StringUtils.isEmpty(truckReportDto.getGateControlCode())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please Provide TruckReportId");
    }
    /*if (StringUtils.isEmpty(truckReportDto.getComments())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Comments section cannot be empty");
    }*/

    TruckReport truckReport = truckReportRepo.findByGateControlCode(truckReportDto.getGateControlCode());
    if (StringUtils.isEmpty(truckReport)) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "No Data found");
    }

    truckReport.setUpdateUser(applicationUser.getUserId());
    truckReport.setUpdateDate(new Date());
    truckReport.setComments(truckReportDto.getComments());
    truckReport = truckReportRepo.save(truckReport);
    truckReportDto.setUpdateUser(truckReport.getUpdateUser());
    truckReportDto.setComments(truckReport.getComments());

    return new ApiResponse(HttpStatus.OK, "Comments added successfully to the indent: " + truckReport.getIndentSummary().getIndentId(), truckReportDto);
  }

  @Override
  public ApiResponse updateTruckInventoryDetails(TruckInventoryDto inventoryDto, ApplicationUser applicationUser) {
    if (StringUtils.isEmpty(inventoryDto.getIndentId()) || StringUtils.isEmpty(inventoryDto.getGateControlCode())) {
      throw new ImproperDataException("Please enter indent id and gate control code");
    }
    //Validation for truck type (not allow edit if truck type is invalid)
    if (!StringUtils.isEmpty(inventoryDto.getTruckType())) {
      if (!truckTypeRepo.existsByType(inventoryDto.getTruckType()))
        throw new InvalidException(String.format("%s truck type is not found in system", inventoryDto.getTruckType()));
    }

    String destCountryName = null;
    String trackingConsentStatusInvntoryDto = "NOT AVAILABLE";
    Optional<TruckReport> optionalTruckReport = truckReportRepo.findById(inventoryDto.getGateControlCode());
    if (optionalTruckReport.isPresent()) {
      TruckReport truckReport = optionalTruckReport.get();
      destCountryName = inventoryDto.getDestCountryName();
      if (inventoryDto.getTrackingConsentStatus() == null) {
        trackingConsentStatusInvntoryDto = "NOT AVAILABLE";
      } else {
        trackingConsentStatusInvntoryDto = inventoryDto.getTrackingConsentStatus();
      }

      List<TruckReportStatus> checkStatus = Arrays.asList(TruckReportStatus.COMPLETED, TruckReportStatus.INTRANSIT);
      // user not allowed to edit once truck is assigned to loadslip
      if ((truckReport.getStatus().equals(TruckReportStatus.GATED_IN) || truckReport.getStatus().equals(TruckReportStatus.REPORTED))
              && (!trackingConsentStatusInvntoryDto.equals("RETRYING"))) {
        IndentSummary indentSummary = truckReport.getIndentSummary();
        indentSummary.getTruckReports().remove(truckReport);
        Optional<MTTruck> truck = truckRepo.findOneByTruckNumberIgnoreCase(truckReport.getTruck().getTruckNumber());
        Optional<IndentDetails> optionalIndentDetails = indentDetailsRepo.findOneByIndentSummaryIndentIdAndGateControlCode(indentSummary.getIndentId(), inventoryDto.getGateControlCode());
        IndentDetails indentDetails = new IndentDetails();
        if (optionalIndentDetails.isPresent()) {
          indentDetails = optionalIndentDetails.get();
          indentSummary.getIndentDetails().remove(indentDetails);
          indentDetailsRepo.delete(indentDetails);
        }
        truckReportRepo.delete(truckReport);

        if (!indentSummary.getIndentId().equals(inventoryDto.getIndentId())) {
          Optional<IndentSummary> optionalReportIndent = indentSummaryRepository.findOneByIndentId(inventoryDto.getIndentId().trim());
          if (optionalReportIndent.isPresent()) {
            IndentSummary newIndentSummary = optionalReportIndent.get();
            if (!indentSummary.getSourceLocation().equals(newIndentSummary.getSourceLocation())) {
              errorMessageForTruckEdit(String.format("New indent source location should be %s", indentSummary.getSourceLocation()), truckReport, indentDetails);
            }
            if (!newIndentSummary.getTransporter().equals(indentSummary.getTransporter())) {
              errorMessageForTruckEdit("New indent transporter should be same as previous indent", truckReport, indentDetails);
            }

            boolean isTruckExist = truckReportRepo.existsByTruckTruckNumberIgnoreCaseAndReportLocationAndStatusNotIn(inventoryDto.getTruckNumber(), applicationUser.getPlantCode(), checkStatus);
            if (isTruckExist) {
              errorMessageForTruckEdit(String.format("Truck with %s number already reported in plant %s", inventoryDto.getTruckNumber(), optionalReportIndent.get().getDestinationLocation()), truckReport, indentDetails);
            }
            TruckReportDto reportDto = prepareBeanToTruckReport(truckReport, inventoryDto, applicationUser);
            ApiResponse apiResponse = opsService.reportTruck(reportDto, false, applicationUser);
            if (apiResponse.getStatusCode() == 200) {
              //updating old indent summary details
              indentSummary.setNetBalance(indentSummary.getNetBalance() + 1);
              indentSummary.setNetPlaced(indentSummary.getNetPlaced() - 1);
              indentSummary.setReported(indentSummary.getReported() - 1);
              indentSummary.setStatus(Status.OPEN);
              indentSummary.setUpdateUser(applicationUser.getUserId());
              indentSummary.setUpdateDate(new Date());
              indentSummaryRepository.save(indentSummary);

              IndentInfoDto indentInfoDto = (IndentInfoDto) apiResponse.getData();
              TruckReport newIndentTruck = indentInfoDto.getEditTruckReport();
              String countryDest = newIndentTruck.getDestCountry();
              if (!StringUtils.isEmpty(countryDest)) {
                Optional<CTCountry> optionalCTCountry = ctCountryRepository.findAllByCountryCodeIn(Arrays.asList(countryDest)).stream().findFirst();
                if (optionalCTCountry.isPresent()) {
                  destCountryName = optionalCTCountry.get().getCountryName();
                }
              }
              TruckReportDto truckReportDto = new TruckReportDto(newIndentTruck);
              truckReportDto.setDestCountryName(destCountryName);
              truckReportDto.setDestDis(newIndentSummary.getDestinationDescription());
              return new ApiResponse(HttpStatus.OK, "Truck details updated successfully", truckReportDto);
            } else {
              errorMessageForTruckEdit(apiResponse.getMessage(), truckReport, indentDetails);
            }
          } else {
            errorMessageForTruckEdit(String.format("No indent found with id %s ", inventoryDto.getIndentId()), truckReport, indentDetails);
          }
        }
        boolean isTruckExist = truckReportRepo.existsByTruckTruckNumberIgnoreCaseAndReportLocationAndStatusNotIn(inventoryDto.getTruckNumber(), applicationUser.getPlantCode(), checkStatus);
        if (isTruckExist) {
          errorMessageForTruckEdit(String.format("Truck with %s number already reported in plant", inventoryDto.getTruckNumber()), truckReport, indentDetails);
        }
        String countryCode = null;
        Optional<MTLocation> optionalMTLocation = locationRepository.findByLocationId(indentSummary.getDestinationLocation());
        if (optionalMTLocation.isPresent()) {
          countryCode = optionalMTLocation.get().getCountry();
        } else {
          Map<String, Object> mtCustomerMap = mtCustomerRepositary.findByCustId(indentSummary.getDestinationLocation());
          if (mtCustomerMap != null) {
            countryCode = mtCustomerMap.get("country").toString();
          }
        }
        if (StringUtils.isEmpty(inventoryDto.getContainerNum())) {
          if ((optionalMTLocation.isPresent() && LocationType.PORT.equals(optionalMTLocation.get().getType())) ||
              (ctCountryRepository.existsByCountryCodeAndIsExport(countryCode, "Y"))) {
            errorMessageForTruckEdit(String.format("Container number is mandatory for the truck indented to Export location! "), truckReport, indentDetails);
          }
        }

        MTTruck mtTruck = new MTTruck();
        // to create truck in mt_truck if no truck is prasent with  truck number and truck type
        if (truck.isPresent()) {
          if (truck.get().getTruckNumber().equals(inventoryDto.getTruckNumber()) && truck.get().getTtId().getType().equalsIgnoreCase(inventoryDto.getTruckType())) {
            mtTruck = truck.get();
          } else {
            //creating new truck if user want to edit truck number or truck type
            List<MTTruckTypeInfo> truckTypeInfoList = truckTypeRepo.findOneByType(inventoryDto.getTruckType());
            if (truckTypeInfoList.size() == 0)
              return new ApiResponse(HttpStatus.NOT_FOUND,
                  String.format("Given truck type %s is not found in system", inventoryDto.getTruckType()));
            MTTruckTypeInfo ttid = truckTypeInfoList.stream().findFirst().get();


            //  Any records present in indent details with previous truck number do not delete in mt_truck
            boolean checkIndentDetailsExistence = indentDetailsRepo.existsByTruck(truck.get());
            if (!checkIndentDetailsExistence) {
              truckRepo.deleteById(truck.get().getTruckNumber());
            }
            TruckReportDto reportDto = new TruckReportDto();
            if (!StringUtils.isEmpty(inventoryDto.getTruckNumber()))
              reportDto.setTruckNumber(inventoryDto.getTruckNumber());
            else
              reportDto.setTruckNumber(indentDetails.getTruck().getTruckNumber());
            mtTruck = truckRepo.save(masterDataServce.createTruck(reportDto, truckReport.getServprov(), applicationUser, ttid, null, Optional.empty()));
          }
        }
        indentDetails.setTruck(mtTruck);
        /*Defaulting Container number with truck number when COUNTRY.IS_EXPORT = "Y"*/
        if (!StringUtils.isEmpty(countryCode) && ctCountryRepository.existsByCountryCodeAndIsExport(countryCode, "Y")) {
          //truckReport.setContainerNum(inventoryDto.getTruckNumber());
          indentDetails.setContainerNum(inventoryDto.getTruckNumber());
        } else {
          indentDetails.setContainerNum(inventoryDto.getContainerNum());
        }
        if (!StringUtils.isEmpty(inventoryDto.getTruckNumber())) {
          indentDetails.getTruck().setTruckNumber(inventoryDto.getTruckNumber());
        }

        if (!StringUtils.isEmpty(inventoryDto.getDriverLicense())) {
          indentDetails.setDriverLicense(inventoryDto.getDriverLicense());
        }
        if (!StringUtils.isEmpty(inventoryDto.getDriverContact())) {
          indentDetails.setDriverMobile(inventoryDto.getDriverContact());
        }
        if (!StringUtils.isEmpty(inventoryDto.getDriverName())) {
          indentDetails.setDriverName(inventoryDto.getDriverName());
        }
        if (!StringUtils.isEmpty(inventoryDto.getTruckType())) {
          indentDetails.setTruckType(inventoryDto.getTruckType());
        }
        indentDetailsRepo.save(indentDetails);
        updateTruckReport(indentSummary, indentDetails, applicationUser, mtTruck, truckReport, inventoryDto);
        truckReportRepo.save(truckReport);
        indentSummary.indentDetails.add(indentDetails);
        indentSummary.truckReports.add(truckReport);
        indentSummaryRepository.save(indentSummary);
        TruckReportDto truckReportDto = new TruckReportDto(truckReport);
        truckReportDto.setDestCountryName(destCountryName);
        truckReportDto.setDestDis(inventoryDto.getDestDesc());
        return new ApiResponse(HttpStatus.OK, "Truck details updated successfully", truckReportDto);
      } else if (inventoryDto.getTrackingConsentStatus().equals("RETRYING")){
        //when the Retry is tried on check consent status - it will only update the consent status
          if (truckReport.getShipmentID() == null){
            truckReportRepo.updateTruckReportConsentByGateControlCodeRetry(truckReport.getGateControlCode());
          } else {
            truckReportRepo.updateTruckReportConsentByShipmentIdRetry(truckReport.getShipmentID());
          }
          TruckReportDto truckReportDto = new TruckReportDto(truckReport);
          truckReportDto.setDestCountryName(destCountryName);
          truckReportDto.setDestDis(inventoryDto.getDestDesc());
          return new ApiResponse(HttpStatus.OK, "Check consent initiated", truckReportDto);
      } else {

        return new ApiResponse(HttpStatus.METHOD_NOT_ALLOWED, String.format("Not allowed to modify truck details after create a loadslip"));
      }
    }
    return new ApiResponse(HttpStatus.NOT_FOUND, String.format("No truck found with %s gate control code and %s indent Id", inventoryDto.getGateControlCode(), inventoryDto.getIndentId()));
  }

  private TruckReportDto prepareBeanToTruckReport(TruckReport truckReport, TruckInventoryDto inventoryDto, ApplicationUser applicationUser) {
    TruckReportDto reportDto = new TruckReportDto();
    reportDto.setDriverContact(inventoryDto.getDriverContact());
    reportDto.setDriverLicense(inventoryDto.getDriverLicense());
    reportDto.setDriverName(inventoryDto.getDriverName());
    reportDto.setIndentId(inventoryDto.getIndentId());
    reportDto.setTruckNumber(inventoryDto.getTruckNumber());
    reportDto.setTruckType(inventoryDto.getTruckType());
    reportDto.setStatus(TruckReportStatus.REPORTED.name());
    reportDto.setBayAssigned(truckReport.getBay());
    reportDto.setInWeight(truckReport.getInWeight());
    reportDto.setOutWeight(truckReport.getOutWeight());
    reportDto.setNetWeight(truckReport.getNetWeight());
    reportDto.setEditTruckStatus(truckReport.getStatus().name());
    if (!StringUtils.isEmpty(truckReport.getBayStatus()))
      reportDto.setBayStatus(truckReport.getBayStatus().name());
    reportDto.setComments(truckReport.getComments());
    reportDto.setEditGateInDate(truckReport.getGateInDate());
    reportDto.setEditReportedDate(truckReport.getReportDate());
    reportDto.setContainerNum(inventoryDto.getContainerNum());
    reportDto.setUpdateUser(applicationUser.getUserId());
    reportDto.setPuc(inventoryDto.isPuc());
    reportDto.setInsurance(inventoryDto.isInsurance());
    reportDto.setSeatBelt(inventoryDto.isSeatBelt());
    reportDto.setFirstAid(inventoryDto.isFirstAid());
    reportDto.setFireExtenguisher(inventoryDto.isFireExtenguisher());
    reportDto.setEmergencyCard(inventoryDto.isEmergencyCard());
    reportDto.setSparKArrestor(inventoryDto.isSparKArrestor());
    reportDto.setFitnessCert(inventoryDto.isFitnessCert());
    //May 2023
    reportDto.setTruckCapacity(inventoryDto.getTruckCapacity());
    reportDto.setTruckGrossVehicleWt(inventoryDto.getTruckGrossVehicleWt());
    reportDto.setTruckUnladenWt(inventoryDto.getTruckUnladenWt());
    reportDto.setBsNorms(inventoryDto.getBsNorms());
    reportDto.setFuelType(inventoryDto.getFuelType());
    return reportDto;
  }

  /*
   * we are deleting truck details in truck report and indent details
   * if any error occur again saving the trcuk and indent details and throw error message
   * */
  private void errorMessageForTruckEdit(String errorMessage, TruckReport truckReport, IndentDetails indentDetails) {
    truckReportRepo.save(truckReport);
    indentDetailsRepo.save(indentDetails);
    throw new InvalidException(StringUtils.isEmpty(errorMessage) ? "Error while truck details editing" : errorMessage);
  }


  private TruckReport updateTruckReport(IndentSummary indentSummary, IndentDetails indentDetails, ApplicationUser loggedInUser, MTTruck mtTruck, TruckReport truckReport, TruckInventoryDto inventoryDto) {
    truckReport.setIndentSummary(indentSummary);
    truckReport.setTruck(mtTruck);
    truckReport.setDriverName(indentDetails.getDriverName());
    truckReport.setDriverMobile(indentDetails.getDriverMobile());
    truckReport.setDriverLicense(indentDetails.getDriverLicense());
    truckReport.setServprov(indentSummary.getTransporter());
//		truckReport.setReportedTruckType(indentDetails.getTruckType());
    truckReport.setTruckType(indentDetails.getTruckType());
    truckReport.setUpdateDate(new Date());
    truckReport.setContainerNum(indentDetails.getContainerNum());
    truckReport.setUpdateUser(loggedInUser.getUserId());
    Map<String, Object> freightMap = utilityService.calculateTrasporterSapCode(indentSummary, indentDetails);
    truckReport.setTransporterSapCode(!StringUtils.isEmpty(freightMap.get("transporterSapCode")) ? freightMap.get("transporterSapCode").toString() : null);
    // Doing previously, now this logic is moved to save shipment freight calculation place
//    truckReport.setTtDays(!StringUtils.isEmpty(freightMap.get("ttDays")) ? Double.parseDouble(freightMap.get("ttDays").toString()) : null);
    truckReport.setIsPuc(inventoryDto.isPuc() ? "Y" : "N");
    truckReport.setIsInsurance(inventoryDto.isInsurance() ? "Y" : "N");
    truckReport.setIsSeatBelt(inventoryDto.isSeatBelt() ? "Y" : "N");
    truckReport.setIsFirstAid(inventoryDto.isFirstAid() ? "Y" : "N");
    truckReport.setIsFireExtenguisher(inventoryDto.isFireExtenguisher() ? "Y" : "N");
    truckReport.setIsEmergencyCard(inventoryDto.isEmergencyCard() ? "Y" : "N");
    truckReport.setIsSparKArrestor(inventoryDto.isSparKArrestor() ? "Y" : "N");
    truckReport.setIsFitnessCert(inventoryDto.isFitnessCert() ? "Y" : "N");

    return truckReport;
  }

  @Override
  public ApiResponse fetchTruckRejectionCodes(ApplicationUser loggedInUser) {
    List<CTRejection> ctRejections = ctRejectionRepository.findAll();
    List<CTRejectionDto> ctRejectionDtos = new ArrayList<>();
    if (!StringUtils.isEmpty(ctRejections))
      ctRejections.forEach(c -> {
        CTRejectionDto ctRejectionDto = new CTRejectionDto();
        ctRejectionDto.setRejectionCode(c.getRejectionCode());
        ctRejectionDto.setRejectionDesc(c.getRejectionDesc());
        ctRejectionDtos.add(ctRejectionDto);
      });
    return new ApiResponse(HttpStatus.OK, "", ctRejectionDtos);
  }

  @Override
  public ApiResponse viewTrucksMovement(ApplicationUser loggedInUser, TruckReportFilterDto truckReportFilterDto) {

    Page<TrucksMetaData> truckMetaData = null;

   /* if (truckReportFilterDto.isReportedTrucksFilter()) {*/
      truckMetaData = filterService.filterTrucksMovement(loggedInUser, truckReportFilterDto);
      truckMetaData.forEach((tmd) -> {

        tmd.setCreatedDate(tmd.getCreatedDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getCreatedDate(), Constants.DATE_TIME_FORMAT) : null);
        tmd.setGatedOutDate(tmd.getGatedOutDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getGatedOutDate(), Constants.DATE_TIME_FORMAT) : null);
        tmd.setLrDate(tmd.getLrDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getLrDate(), Constants.DATE_TIME_FORMAT) : null);
        tmd.setSapInvoiceDate(tmd.getSapInvoiceDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getSapInvoiceDate(), Constants.DATE_TIME_FORMAT) : null);

        tmd.setGateInDate(tmd.getGateInDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getGateInDate(), Constants.DATE_TIME_FORMAT) : null);
        tmd.setGateOutDate(tmd.getGateOutDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getGateOutDate(), Constants.DATE_TIME_FORMAT) : null);
        tmd.setReportDate(tmd.getReportDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getReportDate(), Constants.DATE_TIME_FORMAT) : null);
        tmd.setReleaseDate(tmd.getReleaseDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getReleaseDate(), Constants.DATE_TIME_FORMAT) : null);
        //tmd.setReleasedDate(tmd.getReleasedDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getReleasedDate(), Constants.DATE_TIME_FORMAT) : null);

        //set radio buttons flags
        tmd.setIsPuc(!StringUtils.isEmpty(tmd.getIsPuc()) ? tmd.getIsPuc().equals("N") ? "false" : "true" : "true");
        tmd.setIsInsurance(!StringUtils.isEmpty(tmd.getIsInsurance()) ? tmd.getIsInsurance().equals("N") ? "false" : "true" : "true");
        tmd.setIsSeatBelt(!StringUtils.isEmpty(tmd.getIsSeatBelt()) ? tmd.getIsSeatBelt().equals("N") ? "false" : "true" : "true");
        tmd.setIsFirstAid(!StringUtils.isEmpty(tmd.getIsFirstAid()) ? tmd.getIsFirstAid().equals("N") ? "false" : "true" : "true");
        tmd.setIsFireExtenguisher(!StringUtils.isEmpty(tmd.getIsFireExtenguisher()) ? tmd.getIsFireExtenguisher().equals("N") ? "false" : "true" : "true");
        tmd.setIsEmergencyCard(!StringUtils.isEmpty(tmd.getIsEmergencyCard()) ? tmd.getIsEmergencyCard().equals("N") ? "false" : "true" : "true");
        tmd.setIsSparKArrestor(!StringUtils.isEmpty(tmd.getIsSparKArrestor()) ? tmd.getIsSparKArrestor().equals("N") ? "false" : "true" : "true");
        tmd.setIsFitnessCert(!StringUtils.isEmpty(tmd.getIsFitnessCert()) ? tmd.getIsFitnessCert().equals("N") ? "false" : "true" : "true");

        tmd.setBayArrivedDate(tmd.getBayArrivedDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getBayArrivedDate(), Constants.DATE_TIME_FORMAT) : null);
        tmd.setConfirmedDate(tmd.getConfirmedDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getConfirmedDate(), Constants.DATE_TIME_FORMAT) : null);
        tmd.setLoadingStartDate(tmd.getLoadingStartDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getLoadingStartDate(), Constants.DATE_TIME_FORMAT) : null);
        tmd.setLoadingEndDate(tmd.getLoadingEndDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getLoadingEndDate(), Constants.DATE_TIME_FORMAT) : null);
        tmd.setLsPrintDate(tmd.getLsPrintDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getLsPrintDate(), Constants.DATE_TIME_FORMAT) : null);
        tmd.setSendForBarcodeDate(tmd.getSendForBarcodeDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getSendForBarcodeDate(), Constants.DATE_TIME_FORMAT) : null);
        tmd.setGrnDate(tmd.getGrnDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getGrnDate(), Constants.DATE_TIME_FORMAT) : null);
        tmd.setOtherQty((tmd.getOtherQty() != null && Integer.parseInt(tmd.getOtherQty().toString()) == 0) ? null : tmd.getOtherQty());
        if (!StringUtils.isEmpty(tmd.getInWeight()) && !StringUtils.isEmpty(tmd.getOutWeight()))
          tmd.setNetWeight(Math.abs(Double.valueOf(tmd.getOutWeight().toString()) - Double.valueOf(tmd.getInWeight().toString())));
        if (!StringUtils.isEmpty(tmd.getNetWeight()) && !StringUtils.isEmpty(tmd.getSapInvWeight()))
          tmd.setDiffInvWeight(Math.abs((Double.valueOf(tmd.getNetWeight().toString())) - (Double.valueOf(tmd.getSapInvWeight().toString()))));
        tmd.setIndentCreationDate(tmd.getIndentCreationDate() != null ? DateUtils.formatDate((java.util.Date) tmd.getIndentCreationDate(), Constants.DATE_TIME_FORMAT) : null);
        tmd.setReportingDateAtDest(tmd.getReportingDateAtDest() != null ? DateUtils.formatDate((java.util.Date) tmd.getReportingDateAtDest(), Constants.DATE_TIME_FORMAT) : null);
        tmd.setUnloadingDateAtDest(tmd.getUnloadingDateAtDest() != null ? DateUtils.formatDate((java.util.Date) tmd.getUnloadingDateAtDest(), Constants.DATE_TIME_FORMAT) : null);
      });
      /*Calling the same filter service when searched with nno filter
      * Uncomment 6394-6405 & if  case 6351*/
   /* } else {
      Page<Map<String, Object>> trucksMap = null;
      *//*DP_REP, L1_MGR, L2_MGR*//*
      if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
        trucksMap = truckReportRepo.getTrucksMovementBySourceLocDPREP(truckReportFilterDto.getStatusList(), PageRequest.of(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength()));
      } else {
        trucksMap = truckReportRepo.getTrucksMovementBySourceLoc(
            loggedInUser.getPlantCode(), truckReportFilterDto.getStatusList(), PageRequest.of(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength()));
      }
      truckMetaData = new PageImpl<>(trucksMap.stream().parallel().map(TrucksMetaData::new).collect(Collectors.toList()),
          PageRequest.of(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength()), trucksMap.getTotalElements());
    }*/
    String didnotuse = null;
    TruckReportFilterDto truckReportDto = new TruckReportFilterDto(truckMetaData, didnotuse);

//    Optional<CTScaleInvoiceWtDiff> ctScaleInvoiceWtDiff = ctScaleInvoiceWtDiffRepository.findById(loggedInUser.getPlantCode());
    List<String> truckDestLocs = truckReportDto.getTrucksMetaDatas().parallelStream().map(truck -> truck.getSourceLoc().toString()).distinct().collect(Collectors.toList());

    /*Getting the ctScaleWeightDiff
     * If Role = DP_REP,L1_MGR, L2_MGR then get CTScaleInvoiceWtDiff with list of truck-reportLoc
     * For normal user get CTScaleInvoiceWtDiff with loggedIn plantCode*/
    Optional<CTScaleInvoiceWtDiff> optionalCTScaleInvoiceWtDiff = null;
    List<CTScaleInvoiceWtDiff> ctScaleInvoiceWtDiffs = null;
    if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
      List<String> truckReportingLocs = truckReportDto.getTrucksMetaDatas().parallelStream().map(truck -> truck.getReportingLoc().toString()).distinct().collect(Collectors.toList());
      ctScaleInvoiceWtDiffs = ctScaleInvoiceWtDiffRepository.findAllByLocationIdIn(truckReportingLocs);
    } else {
      optionalCTScaleInvoiceWtDiff = ctScaleInvoiceWtDiffRepository.findById(loggedInUser.getPlantCode());
    }
    List<String> countryCodeList = truckMetaData.stream().parallel().map(truckMeta -> !StringUtils.isEmpty(truckMeta.getDestCountry()) ? truckMeta.getDestCountry().toString() : null).distinct().collect(Collectors.toList());
    List<CTCountry> ctCountryList = ctCountryRepository.findAllByCountryCodeIn(countryCodeList);

    /*to get the ctRejectionDesc*/
    List<CTRejection> ctRejections = ctRejectionRepository.findAll();

    List<String> uniqueDest = truckReportDto.getTrucksMetaDatas().stream().parallel().map(truck -> !StringUtils.isEmpty(truck.getDestLoc()) ? truck.getDestLoc().toString() : null).distinct().collect(Collectors.toList());
    if (uniqueDest.size() > 0) {
      List<Map<String, String>> destWithDesc = locationRepository.findDestDescWtihDestinations(uniqueDest);
      List<String> loadslipIds = truckReportDto.getTrucksMetaDatas().parallelStream().map(trucksMetaData -> !StringUtils.isEmpty(trucksMetaData.getLoadslipId()) ? trucksMetaData.getLoadslipId().toString() : null).collect(Collectors.toList());
      List<LoadslipMetaData> loadslipMapList = null;
      if (loadslipIds.size() > 0) {
        loadslipMapList = findAllLoadslipDetails(loadslipIds);
      }
      for (TrucksMetaData tmd : truckReportDto.getTrucksMetaDatas()) {
        /*if (ctScaleInvoiceWtDiff.isPresent() && !StringUtils.isEmpty(ctScaleInvoiceWtDiff.get().getWeightDiff()))
          tmd.setWeightDiffStatus((tmd.getDiffInvWeight() != null ? Double.valueOf(tmd.getDiffInvWeight().toString()) : 0.0) > (Double.valueOf(ctScaleInvoiceWtDiff.get().getWeightDiff())));*/
        /*if role = DP_REP,L1_MGR, L2_MGR then finding the ctScaleWeightDiff for that reportLoc
         * for normal user no need to execute this IF - block as we will have only one CTScaleInvoiceWtDiff record*/
        if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())) {
          if (ctScaleInvoiceWtDiffs != null && ctScaleInvoiceWtDiffs.size() > 0) {
            optionalCTScaleInvoiceWtDiff = ctScaleInvoiceWtDiffs.parallelStream().filter(ct -> tmd.getReportingLoc().toString().equals(ct.getLocationId())).findFirst();
          }
        }
        if (optionalCTScaleInvoiceWtDiff != null && optionalCTScaleInvoiceWtDiff.isPresent()) {
          tmd.setWeightDiffStatus((tmd.getDiffInvWeight() != null ? Double.valueOf(tmd.getDiffInvWeight().toString()) : 0.0) > (Double.valueOf(optionalCTScaleInvoiceWtDiff.get().getWeightDiff())));
        }
        if (!StringUtils.isEmpty(tmd.getDestLoc()))
          tmd.setDestDis(destWithDesc.stream().parallel().filter(destDescMap -> destDescMap.get("destLoc").equals(tmd.getDestLoc().toString())).findAny().get().get("DESTDESC"));
        //For FGS & RDC Operations role, Loadslip cant be cancelled if there is STO/SO or scan qty in atleast one of Loadslip Items
        if (!StringUtils.isEmpty(tmd.getRejectionCode()) && ctRejections != null && !ctRejections.isEmpty())
          ctRejections.forEach(c -> {
            if (tmd.getRejectionCode().equals(c.getRejectionCode()))
              tmd.setRejectionDesc(c.getRejectionDesc());
          });
        if (loggedInUser.getRole().isFGSOrRDCOperationsRole()) {
          //Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(lsmd.getLoadslipId().toString());
          if (loadslipMapList != null && loadslipMapList.size() > 0) {
            for (LoadslipMetaData metaData : loadslipMapList) {
              if (metaData.getLoadslipId().toString().equals(tmd.getLoadslipId()) && !DelInvType.isExportOrJIT(metaData.getLoadslipType().toString())) {
                // LS can not be cancelled once sto/so or scan qty is present
                if (!StringUtils.isEmpty(metaData.getStoSoNum()) || metaData.getScannedQty() != null && (Integer) metaData.getScannedQty() > 0) {
                  tmd.setCanLSCancelled(false);
                  break;
                }
              }
            }
          }
        }
        if (!StringUtils.isEmpty(tmd.getDestCountry())) {
          Optional<CTCountry> optionalCTCountry = ctCountryList.parallelStream().filter(ctCountry -> ctCountry.getCountryCode().equals(tmd.getDestCountry())).findFirst();
          if (optionalCTCountry.isPresent()) {
            tmd.setDestCountryName(optionalCTCountry.get().getCountryName());
          }
        }
      }
    }
    return new ApiResponse(HttpStatus.OK, "", truckReportDto);
  }


  public ApiResponse exportShipmentTractingFile(MultipartFile multipartFile, ApplicationUser loggedInUser) throws Exception {
    String fileName = multipartFile.getOriginalFilename();
    String extenstion = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
    if (extenstion != null && !extenstion.equalsIgnoreCase("csv")) {
      return new ApiResponse(HttpStatus.BAD_REQUEST, "Please upload csv file");
    }
    StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("ATL_BUSINESS_FLOW_PKG.UPLOAD_EXP_SHIPMENTS_DATA");
    storedProcedureQuery.registerStoredProcedureParameter("p_file_data", Blob.class, ParameterMode.IN);
    storedProcedureQuery.registerStoredProcedureParameter("p_user", String.class, ParameterMode.IN);
    storedProcedureQuery.registerStoredProcedureParameter("p_result", String.class, ParameterMode.OUT);
    SerialBlob blob = new SerialBlob(multipartFile.getBytes());

    storedProcedureQuery.setParameter("p_file_data", blob);
    storedProcedureQuery.setParameter("p_user", loggedInUser.getUserId());
    storedProcedureQuery.execute();
    String result = storedProcedureQuery.getOutputParameterValue("p_result") != null ? (String) storedProcedureQuery.getOutputParameterValue("p_result") : null;
    if (result != null && result.equalsIgnoreCase("SUCCESS"))
      return new ApiResponse(HttpStatus.OK, "file uploaded successfully", result);
    else
      return new ApiResponse(HttpStatus.OK, "file uploaded failed", result);
  }


  public ApiResponse fetchExportShipmentData(ExportShipmentTractingDto exportShipmentBean, ApplicationUser loggedInUser) {
    Page<ExportShipment> exportShipmentPage;
    if (exportShipmentBean.isFilterOfExportShipments()) {
      exportShipmentPage = filterService.filterExportShipmentData(exportShipmentBean);
    } else {
      exportShipmentPage = exportShipmentRepo.findAll(PageRequest.of(exportShipmentBean.getIndex(), exportShipmentBean.getPageLength()));
    }
    Map<Object, Object> resultMap = new HashMap<>();
    resultMap.put("pageLength", exportShipmentBean.getPageLength());
    resultMap.put("total", exportShipmentPage.getTotalElements());
    resultMap.put("ExportShipmentDto", exportShipmentPage.stream().parallel().map(ExportShipmentTractingDto::new).collect(Collectors.toList()));
    return new ApiResponse(HttpStatus.OK, "Export shipment upload data", resultMap);
  }


  @Override
  public ByteArrayOutputStream generateDispatchPlanExcel(DispatchPlanFilterDto searchDto, ApplicationUser applicationUser) throws Exception {


    DispatchPlanFilterDto dispatchPlanFilterDto = (DispatchPlanFilterDto) getDispatchPlanInfo(searchDto, applicationUser).getData();

    List<DispatchPlanItemDto> dispatchPlanItemDtos = dispatchPlanFilterDto.getPlanItems();

    String[] columnNames = {"Plan Id", "Disp Date", "Source", "Dest", "Material Code", "Material Desc", "Dest Desc", "Cat", "TTE", "Batch", "Pr", "Mkt Seg", "Plan Qty", "App Qty", "Un Appr Qty",
        "Appr Del", "Un Appr Del", "Reserved", "Loaded", "Disp", "Available", "Total Availablr Qty", "Avail TTE", "Truck Count", "Weight (Kg)", "Available Weight (Kg)", "Weight Util (%)", "Volume (CUMTR)",
        "Available Volume (CUMTR)", "Volume Util (%)", "Plan Status", "Insert User", "Update User", "Insert Date", "Update Date", "Plan Age (Days)", "Comments"};

    String[] fieldNames = {"planId", "dispatchDate", "sourceLocation", "destinationLocation", "itemId", "itemDescription", "destinationDescription", "category", "tte", "batchCode", "priority", "marketSegment", "quantity", "approvedQuantity", "unapprovedQuantity",
        "deletedApprQuantity", "deletedUnApprQuantity", "reservedQuantity", "loaded", "dispatchedQuantity", "availableQuantity", "totalAvailableQuantity", "availbleTTE", "truckCount", "weight", "totalWeight", "weightUtil", "volume",
        "totalVolume", "volumeUtil", "status", "insertUser", "updateUser", "tempInsertDate", "tempUpdateDate", "planAge", "comments"};

    Map<String, Object[]> reportData = new LinkedHashMap<String, Object[]>();
    reportData.put("1", columnNames);

    int rowNum = 2;
    for (DispatchPlanItemDto dispatchPlanItemDto : dispatchPlanItemDtos) {
      reportData.put(Integer.toString(rowNum), ExcelUtil.setDataToMap(fieldNames, dispatchPlanItemDto));
      rowNum++;
    }

    ByteArrayOutputStream bos = getExcelByteArrayOutputStream(columnNames, reportData, "DispatchPlans");

    return bos;
  }

  @Override
  public ByteArrayOutputStream generateIndentsExcel(IndentFilterDto indentFilterDto, ApplicationUser applicationUser) throws Exception {


    IndentFilterDto filterDto = (IndentFilterDto) getIndents(indentFilterDto, applicationUser).getData();

    List<IndentInfoDto> indentInfoDtos = filterDto.getIndents();

    /*Column names of the excel sheet.
     * It should be in the same order as the templet.*/
    String[] columnNames = {"Source", "Disp Date", "Dest", "Dest Description", "Truck/Cont Type", "Transporter", "Created Date", "Port Of Disc", "Country Of Dest", "  IndentId  ", "Frt", "Cat", "Indented", "Cancelled", "Net Indented",
        "Trans Confirmed", "Trans Declined", "To Be Confirmed", "Reported", "Rejected", "Net Balance", "Indent Age (Days)", "Status", "Insert User", "Update User", "Comments"};

    /*variables of the DTO/Class which corresponds to the column.
     * It should be in the same order as the COLUMNS in the above line*/
    String[] fieldNames = {"source", "dispatchDate", "destination", "destDis", "truckType", "transporter", "insertDate", "pod", "destCountryName", "indentId", "isFreightAvailable", "materailGrp", "indented", "cancelled", "netRequested",
        "confirmed", "declined", "toBeConfirmed", "reported", "rejected", "netBalance", "indentAge", "status", "insertUser", "updateUser", "comments"};

    Map<String, Object[]> reportData = new LinkedHashMap<String, Object[]>();
    reportData.put("1", columnNames);

    int rowNum = 2;
    for (IndentInfoDto indentInfoDto : indentInfoDtos) {
      reportData.put(Integer.toString(rowNum), ExcelUtil.setDataToMap(fieldNames, indentInfoDto));
      rowNum++;
    }

    ByteArrayOutputStream bos = getExcelByteArrayOutputStream(columnNames, reportData, "Indents");

    return bos;
  }

  private ByteArrayOutputStream getExcelByteArrayOutputStream(String[] columnNames, Map<String, Object[]> reportData, String sheetName) throws Exception {

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    SXSSFWorkbook wb = new SXSSFWorkbook();
    Sheet newSheet = wb.createSheet(sheetName);

    newSheet.createFreezePane(0, 1);
    ExcelUtil.generateExcel(wb, newSheet, reportData);

    SXSSFSheet sheet = wb.getSheetAt(0);
    for (int j = 0; j < columnNames.length; j++) {
      sheet.trackColumnForAutoSizing(j);
      sheet.setColumnWidth(j, ((columnNames[j].length() + 7) * 256));
    }
    wb.write(bos);

    bos.close();
    wb.dispose();
    return bos;
  }

  @Override
  public ApiResponse checkMultiLoadslipAllowed(String shipment, ApplicationUser applicationUser) {
    if (StringUtils.isEmpty(shipment)) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please Provide ShipmentId");
    }
    Optional<TruckReport> optionalTruckReport = truckReportRepo.findByShipmentIDAndReportLocation(shipment, applicationUser.getPlantCode());
    if (!optionalTruckReport.isPresent()) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "No Truck Found With ShipmentId");
    }
    if (optionalTruckReport.isPresent() && TruckReportStatus.INTRANSIT.equals(optionalTruckReport.get().getStatus())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Truck: " + optionalTruckReport.get().getTruck().getTruckNumber() + " is already Gated Out from the location: " + applicationUser.getPlantCode());
    }
    return new ApiResponse(HttpStatus.OK, "Multi-loadslip is allowed on the truck", true);
  }

  @Override
  public ApiResponse getDashboardData(DashboardFilterDto dashboardFilterDto, ApplicationUser applicationUser) {
    DashboardDataDto dashboardDataDto = new DashboardDataDto();

    Optional<MTLocation> optionalMTLocation = null;
    if (!UserRole.getDPREPAccessRoles().contains(applicationUser.getRole())){
      optionalMTLocation = locationRepository.findByLocationId(applicationUser.getPlantCode());
    }/*else {
      if (!StringUtils.isEmpty(dashboardFilterDto.getSourceLoc()))
      {
        optionalMTLocation = locationRepository.findByLocationId(dashboardFilterDto.getSourceLoc());
      }
    }*/
    if (dashboardFilterDto.getIsOpenPlans()) {
      dashboardDataDto.setPlansDataDtos(dashboardService.getOpenPlansDetails(dashboardFilterDto, applicationUser, optionalMTLocation));
    }

    if (dashboardFilterDto.getIsIndentStatus()){
      dashboardDataDto.setIndentStatusDtos(dashboardService.getIndentStatusWithCumm(dashboardFilterDto, applicationUser, false, optionalMTLocation));
    }
    if (dashboardFilterDto.getIsPlacementStatus()){
      dashboardDataDto.setPlacementStatusDtos(dashboardService.getIndentStatusWithCumm(dashboardFilterDto, applicationUser, true, optionalMTLocation));
    }
    if (dashboardFilterDto.getIsTruckStatus()){
      ApiResponse apiResponse = this.getTruckStatus(applicationUser.getPlantCode(),applicationUser);
      if (apiResponse.getStatusCode() == 200){
        dashboardDataDto.setTruckReportDto((TruckReportDto)apiResponse.getData());
      }
    }
    if (dashboardFilterDto.getIsShipmentStatus()){
      dashboardDataDto.setShipmentStatusList(dashboardService.getShipmentStatusData(dashboardFilterDto, applicationUser, optionalMTLocation));
    }
    if (dashboardFilterDto.getIsPlanAgeing()){
      dashboardDataDto.setPlanAgeing(dashboardService.getPlanAgeing(dashboardFilterDto, applicationUser, optionalMTLocation));
    }
    if (dashboardFilterDto.getIsDelayedDays()){
      dashboardDataDto.setDelayedDays(dashboardService.getDelayedDaysData(dashboardFilterDto, applicationUser, optionalMTLocation));
    }
    if (dashboardFilterDto.getIsPlanUpload()){
      dashboardDataDto.setPlanUploadDtos(dashboardService.getPlanUploadData(dashboardFilterDto, applicationUser, optionalMTLocation));
    }
    if (dashboardFilterDto.getIsDispatchedPlans()){
      dashboardDataDto.setDispatchedPlansDataList(dashboardService.getDispatchedPlanDetails(dashboardFilterDto, applicationUser, optionalMTLocation));
    }
    return new ApiResponse(HttpStatus.OK, "", dashboardDataDto);
  }

  @Override
  public ApiResponse getPriorityList(ApplicationUser loggedInUser) {
    List<Integer> priorityList = new ArrayList<>();
    if (UserRole.getDPREPAccessRoles().contains(loggedInUser.getRole())){
      priorityList = dispatchPlanItemInfoRepository.getPriorityList();
    }else{
      priorityList = dispatchPlanItemInfoRepository.getPriorityListForSource(loggedInUser.getPlantCode());
    }
    return new ApiResponse(HttpStatus.OK, "", priorityList.stream().map(Object::toString).collect(Collectors.toList()));
  }

  @Override
  public ApiResponse getOpenPlansSourceLocs(ApplicationUser applicationUser) {
    if (!StringUtils.isEmpty(applicationUser.getPlantCode())){
      Optional<MTLocation> optionalMTLocation = locationRepository.findById(applicationUser.getPlantCode());
      if (optionalMTLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equals(optionalMTLocation.get().getLocationClass())){
        List<String> sourceLocList = new ArrayList<>();
        sourceLocList.add(optionalMTLocation.get().getId());
        sourceLocList.add(optionalMTLocation.get().getLinkedPlant());
        return new ApiResponse(HttpStatus.OK, "", sourceLocList);
      }
    }
    return new ApiResponse(HttpStatus.OK, "", dispatchPlanItemInfoRepository.findDistinctSourceLoc());
  }

  @Override
  public ApiResponse getLSCategoryList(ApplicationUser applicationUser) {
    return new ApiResponse(HttpStatus.OK, "", loadslipRepository.getDistinctCategory());
  }

  @Override
  public ApiResponse getClsData(ClsDetailsFilterDto filterDto, ApplicationUser loggedInUser){
    return filterService.getClsData(filterDto, loggedInUser);
  }
}


