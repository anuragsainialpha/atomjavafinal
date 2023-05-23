package com.api.apollo.atom.service.impl;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.ops.LoadslipTypeDto;
import com.api.apollo.atom.dto.ops.TruckReportDto;
import com.api.apollo.atom.dto.ops.TruckReportFilterDto;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.ops.Loadslip;
import com.api.apollo.atom.entity.ops.SharedTruck;
import com.api.apollo.atom.entity.ops.ShipmentStop;
import com.api.apollo.atom.entity.ops.TruckReport;
import com.api.apollo.atom.repository.ops.*;
import com.api.apollo.atom.service.FilterService;
import com.api.apollo.atom.service.OpsService;
import com.api.apollo.atom.service.RDCService;
import com.api.apollo.atom.service.UtilityService;
import com.api.apollo.atom.util.DateUtils;
import com.api.apollo.atom.util.Utility;
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

@Service
public class RDCServiceImpl implements RDCService {

  @Autowired
  TruckReportRepository truckReportRepo;

  @Autowired
  private IndentSummaryRepository indentSummaryRepository;

  @Autowired
  private FilterService filterService;

  @Autowired
  private LoadslipRepository loadslipRepository;

  @Autowired
  ShipmentRepository shipmentRepository;

  @Autowired
  ShipmentStopRepository shipmentStopRepository;

  @Autowired
  SharedTruckRepository sharedTruckRepository;

  @Autowired
  private OpsService opsService;

  @Autowired
  private UtilityService utilityService;


  @Override
  public ApiResponse getIntransitTruckInfo(TruckReportFilterDto truckReportFilterDto, ApplicationUser loggedInUser) {
    Page<TruckReport> truckReportsData = null;
    List<Constants.TruckReportStatus> truckStatuses = null;

    /*List<ShipmentStop> shipmentStops = shipmentStopRepository.findAllByLocationId(loggedInUser.getPlantCode());
    List<String> shipmentIds = new ArrayList<>();
    if (shipmentStops != null && !shipmentStops.isEmpty()) {
      shipmentIds = shipmentStops.parallelStream().map(shipmentStop -> shipmentStop.getShipmentStopId().getShipmentId()).collect(Collectors.toList());
    }*/

    // For RDC Inventory & History Page
    if (truckReportFilterDto.getType() != null) {
      truckReportsData = getInventoryTrucksData(truckReportFilterDto, loggedInUser, truckStatuses);
    }
    // For Receiving RDC Intransit Trucks and Report Incoming Intransit Trucks
    else {
      List<String> incomingShipmentIds = shipmentStopRepository.findShipmentIdsByPlantLocFromShipmentStopAndSharedTruck(loggedInUser.getPlantCode());
      //NOT INVENOTRY TRUCKS, Show Intransit Trucks means INCOMING TRUCKS FROM FGS (SOURCE)
      // For Incoming trucks , we will have shipment stop data ,query trucks using the shipmentIds present in Shipment_Stop table based on the user plant code
      truckStatuses = Constants.TruckReportStatus.getIntransitTruckStatus();
      if (!truckReportFilterDto.isReportedTrucksFilter()) {
        //Old logic
       /* truckReportsData = truckReportRepo.findAllByDestinationLocationAndStatusInAndRefCodeIsNull(loggedInUser.getPlantCode(),
            truckStatuses, PageRequest.of(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength()));*/
        // shipment stop logic
        truckReportsData = truckReportRepo.findAllByShipmentIDInAndStatusInOrderByReportDateAsc(incomingShipmentIds, truckStatuses,
            PageRequest.of(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength(), Sort.Direction.DESC, "reportDate"));
      } else {
        //for Incoming TRUCKS at Destination, destination loc is RDC User's Plant code and source is from User Input in Filters
        truckReportsData = filterService.filterReportedTrucks(truckReportFilterDto, loggedInUser, truckStatuses, "INCOMING_TRUCKS", incomingShipmentIds);
      }
      //TODO: Temparory check
      List<TruckReport> truckReports = truckReportsData.stream().peek(truckReport -> {
        List<String> truckReportLocations = truckReportRepo.findReportLocationByShipmentID(truckReport.getShipmentID());
        if (truckReportLocations != null && truckReportLocations.contains(loggedInUser.getPlantCode())) {
          truckReport.setTruckReported(true);
        }
      }).collect(Collectors.toList());
      truckReportsData = new PageImpl<>(truckReports, PageRequest.of(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength(), Sort.Direction.DESC, "reportDate"), truckReportsData.getTotalElements());
    }
    return new ApiResponse(HttpStatus.OK, "", new TruckReportFilterDto(loggedInUser, truckReportsData,truckReportFilterDto.getPageLength()));
  }

  private void setLoadslipTimestamps(Page<TruckReport> truckReportsData, ApplicationUser loggedInUser) {
    truckReportsData.stream().parallel().forEach(truckReport -> {
      List<Loadslip> loadslips = loadslipRepository.findAllByShipmentShipmentIdAndStatusNot(truckReport.getShipmentID(), Constants.LoadslipStatus.CANCELLED);
      if (loadslips != null && !loadslips.isEmpty()) {
        getTruckEventDatesFromLoadslip(loadslips, truckReport, loggedInUser);
      }
    });
  }

  private Page<TruckReport> getInventoryTrucksData(TruckReportFilterDto truckReportFilterDto, ApplicationUser loggedInUser, List<Constants.TruckReportStatus> truckStatuses) {
    Page<TruckReport> truckReportsData;// TYPE is INVENTORY, show all inventory trucks (INCOMING & SHIPMENT TRUCKS  in RDC)
    if (truckReportFilterDto.getType().equalsIgnoreCase("INVENTORY")) {
      if (!StringUtils.isEmpty(truckReportFilterDto.getStatus())) {
        List<String> statuses = new ArrayList<>(Arrays.asList(truckReportFilterDto.getStatus().split(",")));
        truckStatuses = statuses.parallelStream().map(Constants.TruckReportStatus::valueOf).collect(Collectors.toList());
      } else {
        truckStatuses = Constants.TruckReportStatus.getRDCSecurityReportedStatuses();
      }
    } else if (truckReportFilterDto.getType().equalsIgnoreCase("HISTORY")) {
      // TYPE is HISTORY, show all HISTORY trucks (INTRANSIT TRUCKS == GATED OUT trucks from RDC)
      if (!StringUtils.isEmpty(truckReportFilterDto.getStatus())) {
        List<String> statuses = new ArrayList<>(Arrays.asList(truckReportFilterDto.getStatus().split(",")));
        truckStatuses = statuses.parallelStream().map(Constants.TruckReportStatus::valueOf).collect(Collectors.toList());
      } else {
        truckStatuses = Constants.TruckReportStatus.getTruckHistoryStatus();
      }
    }
    if (!truckReportFilterDto.isReportedTrucksFilter()) {
      // Fetch all trucks  from TRUCK_REPORTING which are  reported at RDC (Destination)
      truckReportsData = truckReportRepo.findAllByReportLocationAndStatusInOrderByReportDateAsc(loggedInUser.getPlantCode(),
          truckStatuses, PageRequest.of(truckReportFilterDto.getIndex(), truckReportFilterDto.getPageLength(), Sort.Direction.DESC, "reportDate"));
    } else {
      // Here trucks are RDC Inventory Trucks (means trucks reported at RDC)
      truckReportsData = filterService.filterReportedTrucks(truckReportFilterDto, loggedInUser, truckStatuses, "INVENTORY_TRUCKS", null);
    }
    // set Unloading start & Unloading end dates from loadlsips of the shipment which is saved in trcuk report
    setLoadslipTimestamps(truckReportsData, loggedInUser);
    return truckReportsData;
  }

  @Override
  public ApiResponse reportIntransitTruck(TruckReportDto truckReportDto, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(truckReportDto.getTruckNumber())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please Provide Truck Number ");
    }
    if (StringUtils.isEmpty(truckReportDto.getStatus())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please Provide Truck status");
    }
    Constants.TruckReportStatus status = Constants.TruckReportStatus.valueOf(truckReportDto.getStatus());
    switch (status) {
      case GATED_IN:
        return reportGateIn(truckReportDto, loggedInUser);
      case REPORTED:
        return reportTruck(truckReportDto, loggedInUser);
      case UNLOADING:
        // UNLOADING_START
        return this.startUnloading(truckReportDto, loggedInUser);
      case DELIVERED:
        // UNLOADING_END
        return this.completeUnloading(truckReportDto, loggedInUser);
      case INTRANSIT:
        // GATED_OUT
        return this.reportGateOut(truckReportDto, loggedInUser);
      case RELEASED:
        Optional<TruckReport> optionalTruckReport = truckReportRepo.findOneByGateControlCodeAndTruckTruckNumber(truckReportDto.getGateControlCode(), truckReportDto.getTruckNumber());
        if (optionalTruckReport.isPresent()) {
          optionalTruckReport.get().setBayStatus(Constants.BayStatus.RELEASE);
          optionalTruckReport.get().setUpdateDate(new Date());
          optionalTruckReport.get().setUpdateUser(loggedInUser.getUserId());

          List<Loadslip> loadslips = loadslipRepository.findAllByShipmentShipmentIdAndDestLocAndStatusNot(optionalTruckReport.get().getShipmentID(), loggedInUser.getPlantCode(), Constants.LoadslipStatus.CANCELLED);
          TruckReport truckReport = truckReportRepo.save(optionalTruckReport.get());
          if (!loadslips.isEmpty()) {
            truckReport = getTruckEventDatesFromLoadslip(loadslips, truckReport, loggedInUser);
          }
          TruckReportDto reportDto = new TruckReportDto(truckReport, loggedInUser);
          reportDto.setDestDis(truckReportDto.getDestDis());
          return new ApiResponse(HttpStatus.OK, "Truck is released successfully", reportDto);
        } else {
          return new ApiResponse(HttpStatus.NOT_FOUND, String.format("truck not found with Gate control code %s", truckReportDto.getGateControlCode()));
        }
      default:
        break;
    }
    return new ApiResponse(HttpStatus.NOT_FOUND, "Invalid Truck Event ! ");
  }

  private ApiResponse reportGateOut(TruckReportDto truckReportDto, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(truckReportDto.getGateControlCode())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please Provide Gate Control code");
    }
    Optional<TruckReport> optionalTruckReport = truckReportRepo.findOneByGateControlCodeAndTruckTruckNumber(truckReportDto.getGateControlCode(), truckReportDto.getTruckNumber());
    if (optionalTruckReport.isPresent()) {
      List<Loadslip> loadslips = loadslipRepository.findAllByShipmentShipmentIdAndDestLocAndStatusNot(optionalTruckReport.get().getShipmentID(), loggedInUser.getPlantCode(), Constants.LoadslipStatus.CANCELLED);
      if ((loadslips != null && loadslips.size() > 0) && !(Constants.BayStatus.RELEASE.equals(optionalTruckReport.get().getBayStatus()))) {
        return new ApiResponse(HttpStatus.NOT_FOUND, "Truck can not be gated out as it is not released ");
      }
      optionalTruckReport.get().setGateOutDate(Utility.currentTimestamp());
      optionalTruckReport.get().setUpdateDate(Utility.currentTimestamp());
      optionalTruckReport.get().setUpdateUser(loggedInUser.getUserId());
      //Update Loadslip and shipment status when truck is GATED_OUT
      utilityService.updateLoadslipAndShipmentStatus(optionalTruckReport.get(), loggedInUser.getPlantCode());
      Optional<SharedTruck> optionalSharedTruck = sharedTruckRepository.findBySharedTruckIdShipmentIdAndSharedTruckIdPickUpLoc(optionalTruckReport.get().getShipmentID(), loggedInUser.getPlantCode());
      if (optionalSharedTruck.isPresent()) {
        SharedTruck sharedTruck = optionalSharedTruck.get();
        sharedTruck.setStatus("CLOSED");
        sharedTruckRepository.save(sharedTruck);
      }
      if (!StringUtils.isEmpty(optionalTruckReport.get().getActivity()) && optionalTruckReport.get().getActivity().equals("P")) {
        try {
          opsService.sendActualShipment(optionalTruckReport.get().getShipmentID());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      TruckReportDto truckReport = new TruckReportDto(truckReportRepo.save(optionalTruckReport.get()), loggedInUser);
      truckReport.setDestDis(truckReportDto.getDestDis());
      truckReport.setDestCountryName(truckReportDto.getDestCountryName());
      return new ApiResponse(HttpStatus.OK, "Truck Gated-Out Successfully", truckReport);
    }
    return new ApiResponse(HttpStatus.NOT_FOUND, String.format("No Truck found with the Truck Number %s and Gate Control Code %s ",
        truckReportDto.getTruckNumber(), truckReportDto.getGateControlCode()));
  }

  private ApiResponse completeUnloading(TruckReportDto truckReportDto, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(truckReportDto.getGateControlCode())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please Provide Gate Control code");
    }
    Optional<TruckReport> optionalTruckReport = truckReportRepo.findOneByGateControlCodeAndTruckTruckNumber(truckReportDto.getGateControlCode(), truckReportDto.getTruckNumber());
    if (optionalTruckReport.isPresent()) {
      optionalTruckReport.get().setStatus(Constants.TruckReportStatus.DELIVERED);
      optionalTruckReport.get().setUpdateDate(Utility.currentTimestamp());
      optionalTruckReport.get().setUpdateUser(loggedInUser.getUserId());

      List<Loadslip> loadslips = loadslipRepository.findAllByShipmentShipmentIdAndStatusNot(optionalTruckReport.get().getShipmentID(), Constants.LoadslipStatus.CANCELLED);
      List<Loadslip> updatedLoadslips = new ArrayList<>();
      loadslips.parallelStream().filter(loadslip -> loadslip.getDestLoc().equals(loggedInUser.getPlantCode())).forEach(loadslip -> {
        loadslip.setStatus(Constants.LoadslipStatus.DELIVERED);
        loadslip.setUeDate(Utility.currentTimestamp());
        loadslip.setUpdateUser(loggedInUser.getUserId());
        loadslip.setUpdateDate(new Date());
        updatedLoadslips.add(loadslip);
      });
      loadslipRepository.saveAll(updatedLoadslips);
      TruckReport truckReport = truckReportRepo.save(optionalTruckReport.get());
      if (!updatedLoadslips.isEmpty()) {
        truckReport = getTruckEventDatesFromLoadslip(updatedLoadslips, truckReport, loggedInUser);
      }
      TruckReportDto reportDto = new TruckReportDto(truckReport, loggedInUser);
      reportDto.setDestDis(truckReportDto.getDestDis());
      return new ApiResponse(HttpStatus.OK, "Truck Unloading Completed Successfully", reportDto);
    }
    return new ApiResponse(HttpStatus.NOT_FOUND, String.format("No Truck found with the Truck Number %s and Gate Control Code %s ",
        truckReportDto.getTruckNumber(), truckReportDto.getGateControlCode()));
  }

  private TruckReport getTruckEventDatesFromLoadslip(List<Loadslip> loadslips, TruckReport truckReport, ApplicationUser loggedInUser) {
    loadslips.parallelStream().forEach(loadslip -> {
      //provide unloading start and end dates when the loadslip belongs to current loggedIn user location
      if (loadslip.getDestLoc().equals(loggedInUser.getPlantCode())) {
        if (!StringUtils.isEmpty(loadslip.getUsDate()))
        truckReport.setLoadslipUSDate(DateUtils.formatDate(loadslip.getUsDate(), Constants.DATE_TIME_FORMAT));
        if (!StringUtils.isEmpty(loadslip.getUeDate()))
        truckReport.setLoadslipUEDate(DateUtils.formatDate(loadslip.getUeDate(), Constants.DATE_TIME_FORMAT));
//    truckReport.setReleaseDate(DateUtils.formatDate(loadslip.getReleaseDate(), Constants.DATE_TIME_FORMAT));
      }
    });
    return truckReport;
  }

  private ApiResponse startUnloading(TruckReportDto truckReportDto, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(truckReportDto.getGateControlCode())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please Provide Gate Control code");
    }
    Optional<TruckReport> optionalTruckReport = truckReportRepo.findOneByGateControlCodeAndTruckTruckNumber(truckReportDto.getGateControlCode(), truckReportDto.getTruckNumber());
    if (optionalTruckReport.isPresent()) {
      optionalTruckReport.get().setStatus(Constants.TruckReportStatus.UNLOADING);
      optionalTruckReport.get().setUpdateDate(Utility.currentTimestamp());
      optionalTruckReport.get().setUpdateUser(loggedInUser.getUserId());

      List<Loadslip> loadslips = loadslipRepository.findAllByShipmentShipmentIdAndStatusNot(optionalTruckReport.get().getShipmentID(), Constants.LoadslipStatus.CANCELLED);
      List<Loadslip> updatedLoadslips = new ArrayList<>();
      loadslips.parallelStream().filter(loadslip -> loadslip.getDestLoc().equals(loggedInUser.getPlantCode())).forEach(loadslip -> {
        loadslip.setStatus(Constants.LoadslipStatus.UNLOADING);
        loadslip.setUsDate(Utility.currentTimestamp());
        loadslip.setUpdateDate(new Date());
        loadslip.setUpdateUser(loggedInUser.getUserId());
        updatedLoadslips.add(loadslip);
      });
      loadslipRepository.saveAll(updatedLoadslips);
      TruckReport truckReport = truckReportRepo.save(optionalTruckReport.get());
      if (!updatedLoadslips.isEmpty()) {
        truckReport = getTruckEventDatesFromLoadslip(updatedLoadslips, truckReport, loggedInUser);
      }
      TruckReportDto reportDto = new TruckReportDto(truckReport, loggedInUser);
      reportDto.setDestDis(truckReportDto.getDestDis());
      return new ApiResponse(HttpStatus.OK, "Truck Unloading Started Successfully", reportDto);
    }
    return new ApiResponse(HttpStatus.NOT_FOUND, String.format("No Truck found with the Truck Number %s and Gate Control Code %s ",
        truckReportDto.getTruckNumber(), truckReportDto.getGateControlCode()));
  }

  private ApiResponse reportGateIn(TruckReportDto truckReportDto, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(truckReportDto.getGateControlCode())) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please Provide Gate Control code");
    }
    Optional<TruckReport> optionalTruckReport = truckReportRepo.findOneByGateControlCodeAndTruckTruckNumber(truckReportDto.getGateControlCode(), truckReportDto.getTruckNumber());
    if (optionalTruckReport.isPresent()) {
      optionalTruckReport.get().setStatus(Constants.TruckReportStatus.GATED_IN);
      optionalTruckReport.get().setGateInDate(Utility.currentTimestamp());
      optionalTruckReport.get().setUpdateDate(Utility.currentTimestamp());
      optionalTruckReport.get().setUpdateUser(loggedInUser.getUserId());
      TruckReportDto truckReport = new TruckReportDto(truckReportRepo.save(optionalTruckReport.get()), loggedInUser);
      truckReport.setDestDis(truckReportDto.getDestDis());
      truckReport.setDestCountryName(truckReportDto.getDestCountryName());
      return new ApiResponse(HttpStatus.OK, "Truck Gated-In Successfully", truckReport);
    }
    return new ApiResponse(HttpStatus.NOT_FOUND, String.format("No Truck found with the Truck Number %s and Gate Control Code %s ",
        truckReportDto.getTruckNumber(), truckReportDto.getGateControlCode()));
  }

  private ApiResponse reportTruck(TruckReportDto truckReportDto, ApplicationUser loggedInUser) {
    TruckReport intransitTruck = truckReportRepo.findByGateControlCode(truckReportDto.getGateControlCode());
    if (intransitTruck != null && intransitTruck.getStatus().equals(Constants.TruckReportStatus.INTRANSIT)) {
      TruckReport receivedTruck = createReceivedTruck(intransitTruck, loggedInUser);
      List<ShipmentStop> shipmentStopList = shipmentStopRepository.findByShipmentStopIdShipmentIdAndLocationId(intransitTruck.getShipmentID(), loggedInUser.getPlantCode());
      if (shipmentStopList.size() > 0) {
        ShipmentStop shipmentStop = shipmentStopList.get(0);
        receivedTruck.setActivity(shipmentStop.getActivity());
      }

      if (truckReportRepo.existsByTruckTruckNumberAndStatusInAndReportLocation(truckReportDto.getTruckNumber(),
          Constants.TruckReportStatus.getInventoryTrucks(),loggedInUser.getPlantCode())) {
        return new ApiResponse(HttpStatus.NOT_FOUND,
            String.format("Truck %s is already reported ", truckReportDto.getTruckNumber()));
      }
      List<TruckReport> trucks = Arrays.asList(intransitTruck,receivedTruck);
      truckReportRepo.saveAll(trucks);
      //Set isReported flag to true
      intransitTruck.setTruckReported(true);
      TruckReportDto reportDto = new TruckReportDto(intransitTruck, loggedInUser);
      reportDto.setDestDis(truckReportDto.getDestDis());
      return new ApiResponse(HttpStatus.OK, "Truck Reported Successfully", reportDto);
    }
    return new ApiResponse(HttpStatus.NOT_FOUND, "No Intransit Truck found with the Truck Number " + truckReportDto.getTruckNumber());
  }

  private TruckReport createReceivedTruck(TruckReport intransitTruck, ApplicationUser loggedInUser) {
    TruckReport receivedTruck = new TruckReport();
    receivedTruck.setGateControlCode(indentSummaryRepository.findIndentSequence(Constants.INDENT_GATE_CONTROL_CODE_PREFIX,
        loggedInUser.getPlantCode(), Constants.INDENT_GATE_CONTROL_CODE_SUFFIX));
    receivedTruck.setIndentSummary(intransitTruck.getIndentSummary());
    // need to change the below system type to RMS for RMS services
    receivedTruck.setType(Constants.SystemType.FGS);
    receivedTruck.setTransporterSapCode(intransitTruck.getTransporterSapCode());
    receivedTruck.setTruck(intransitTruck.getTruck());
    receivedTruck.setDriverName(intransitTruck.getDriverName());
    receivedTruck.setDriverMobile(intransitTruck.getDriverMobile());
    receivedTruck.setDriverLicense(intransitTruck.getDriverLicense());
    receivedTruck.setServprov(intransitTruck.getServprov());
    receivedTruck.setTruckType(intransitTruck.getTruckType());
    receivedTruck.setReportLocation(loggedInUser.getPlantCode());
    receivedTruck.setSourceLocation(intransitTruck.getSourceLocation());
    receivedTruck.setDestinationLocation(intransitTruck.getDestinationLocation());
    receivedTruck.setReportDate(new Date());
    receivedTruck.setStatus(Constants.TruckReportStatus.REPORTED);
    receivedTruck.setWaitTimeHrs((double) DateUtils.diffBetweenDates(new Date(), receivedTruck.getReportDate()));
    receivedTruck.setRejectedStatus(Constants.TruckReportStatus.NORMAL);
    receivedTruck.setActualTruckType(intransitTruck.getActualTruckType());
    receivedTruck.setReportedTruckType(intransitTruck.getReportedTruckType());
    receivedTruck.setInsertUser(loggedInUser.getUserId());
    receivedTruck.setRefCode(intransitTruck.getGateControlCode());
    receivedTruck.setShipmentID(intransitTruck.getShipmentID());
    receivedTruck.setContainerNum(intransitTruck.getContainerNum());
    receivedTruck.setComments(intransitTruck.getComments());
    receivedTruck.setDestCountry(intransitTruck.getDestCountry());
    return receivedTruck;
  }


  @Override
  public ApiResponse getLoadslipsWithShipment(String shipmentId, ApplicationUser loggedInUser) {
    if (StringUtils.isEmpty(shipmentId))
      return new ApiResponse(HttpStatus.NOT_FOUND, "Please give the shipmentId");
    //code for get loadslipIds list from shipment stop table
  /*  List<ShipmentStop> shipmentStops = shipmentStopRepository.findByShipmentStopIdShipmentIdAndLocationId(shipmentId,loggedInUser.getPlantCode());
    if(shipmentStops.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND,String.format("NO Loadslips found with the shipmentId %s",shipmentId));
    List<String> loadslipIds = shipmentStops.stream().map(shipmentStop -> shipmentStop.getShipmentStopId().getLoadslipId()).collect(Collectors.toList());
    return new ApiResponse(HttpStatus.OK,"",loadslipIds);*/

    List<Loadslip> loadslipList = loadslipRepository.findAllByShipmentShipmentIdAndStatusNot(shipmentId, Constants.LoadslipStatus.CANCELLED);
    if (loadslipList.size() == 0)
      return new ApiResponse(HttpStatus.NOT_FOUND, String.format("No Loadslips find with shipmentId %s", shipmentId));
    List<LoadslipTypeDto> loadslipTypeDtos = loadslipList.stream().map(loadslip -> new LoadslipTypeDto(loadslip.getLoadslipId(), loadslip.getLoadslipType())).collect(Collectors.toList());
    return new ApiResponse(HttpStatus.OK, "", loadslipTypeDtos);
  }


}
