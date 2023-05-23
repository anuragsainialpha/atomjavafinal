package com.api.apollo.atom.service.impl;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.LocationType;
import com.api.apollo.atom.entity.master.Freight;
import com.api.apollo.atom.entity.master.MTLocation;
import com.api.apollo.atom.entity.ops.*;
import com.api.apollo.atom.repository.master.FreightRepository;
import com.api.apollo.atom.repository.master.LocationRepository;
import com.api.apollo.atom.repository.ops.*;
import com.api.apollo.atom.service.UtilityService;
import com.api.apollo.atom.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class UtilityServiceImpl implements UtilityService {

  @Autowired
  ShipmentRepository shipmentRepository;

  @Autowired
  ShipmentStopRepository shipmentStopRepository;

  @Autowired
  LoadslipRepository loadslipRepository;

  @Autowired
  TruckReportRepository truckReportRepository;

  @Autowired
  LocationRepository locationRepository;

  @Autowired
  private MTCustomerRepository mtCustomerRepository;

  @Autowired
  private FreightRepository freightRepository;

  @Override
  public void updateLoadslipAndShipmentStatus(TruckReport truckReport, String plantCode) {
    if (!StringUtils.isEmpty(truckReport.getShipmentID())) {
      Optional<Shipment> optionalShipment = shipmentRepository.findById(truckReport.getShipmentID());
      if (optionalShipment.isPresent()) {
        Shipment shipment = optionalShipment.get();
        if (optionalShipment.get().getStatus() != null && optionalShipment.get().getStatus().equals(Constants.LoadslipStatus.CANCELLED.name())) {
          //Truck Gated was assigned LS, the LS Cancelled and also shipment Cancelled (If only one LS on shipment),did not pick any Load slip and Gated out: TRUCK_REPORTING.STATUS = COMPLETED
          truckReport.setStatus(Constants.TruckReportStatus.COMPLETED);
        } else {
          // Fetch max Stop seq value (last drop or stop) from shipment_stop
          Optional<ShipmentStop> lastStopShipmentStop = shipmentStopRepository.findTop1ByShipmentStopIdShipmentIdOrderByShipmentStopIdStopNumDesc(shipment.getShipmentId());
          List<ShipmentStop> shipmentStops = shipmentStopRepository.findByShipmentStopIdShipmentIdAndLocationId(shipment.getShipmentId(), plantCode);

          // Loadslips are avilable in Shipment Stop table with shipment id for this truck
          if (shipmentStops != null && !shipmentStops.isEmpty()) {
            // Update all Loadslip statuses which are present in Shipment Stop table
            List<Loadslip> updatedLoadslips = new ArrayList<>();
            for (ShipmentStop currentShipmentStop : shipmentStops) {
              Optional<Loadslip> optionalLoadslip = loadslipRepository.findById(currentShipmentStop.getShipmentStopId().getLoadslipId());
              if (optionalLoadslip.isPresent()) {
                // case : loadslip is not cancelled (Actually Cancelled Loadslip should be removed from SHIPMENT_STOP when Loadslip is get cancelled)
                if (optionalLoadslip.get().getStatus() != Constants.LoadslipStatus.CANCELLED) {
                  if (currentShipmentStop.getActivity().equals("P")) {
                    optionalLoadslip.get().setStatus(Constants.LoadslipStatus.INTRANSIT);
                  } else if (currentShipmentStop.getActivity().equals("D")) {
                    optionalLoadslip.get().setStatus(Constants.LoadslipStatus.COMPLETED);
                  }
                  updatedLoadslips.add(optionalLoadslip.get());
                }
              }
            }
            if (updatedLoadslips.size() > 0) {
              loadslipRepository.saveAll(updatedLoadslips);
            }

            // here fetched shipmentStops are differ only in loadslip_id & stop_seq
            // but shipment_id,location & activity are same (this scenario will come if Multi Pick-up or Multi Drop is happening in same location)
            // So consider first  shipment stop,If  shipment stop location is the last shipment stop location
            ShipmentStop currentLocShipmentStop = shipmentStops.get(0);
            if (currentLocShipmentStop.getLocationId().equals(lastStopShipmentStop.get().getLocationId())) {
              shipment.setStatus(Constants.LoadslipStatus.COMPLETED.name());
              // Once shipment is completed, make all trucks as Completed which are  associated with this shipment
              List<TruckReport> truckReports = truckReportRepository.findAllByShipmentID(shipment.getShipmentId());
              if (truckReports != null && !truckReports.isEmpty()) {
                truckReports.parallelStream().forEach(tr -> {
                  tr.setStatus(Constants.TruckReportStatus.COMPLETED);
                });
              }
            } else {
              shipment.setStatus(Constants.LoadslipStatus.INTRANSIT.name());
              if (currentLocShipmentStop.getActivity().equals("P")) {
                truckReport.setStatus(Constants.TruckReportStatus.INTRANSIT);
              } else if (currentLocShipmentStop.getActivity().equals("D")) {
                truckReport.setStatus(Constants.TruckReportStatus.COMPLETED);
              }
            }
            shipmentRepository.save(shipment);
          } else {
            truckReport.setStatus(Constants.TruckReportStatus.INTRANSIT);
          }
        }
      }
      // No Loadslip or shipment is created for this truck
      else {
        //Case1: Truck Gated in but did not pick any Load slip and Gated out: TRUCK_REPORTING.STATUS = COMPLETED
        //Case2: Truck Gated in but rejected during inspection and Gated out, will not pick any Load slip: TRUCK_REPORTING.STATUS = COMPLETED, TRUCK_REPORTING.REJ_STATUS = REJECTED.
        truckReport.setStatus(Constants.TruckReportStatus.COMPLETED);
      }
    } // No Loadslip or shipment is created for this truck
    else {
      //Case1: Truck Gated in but did not pick any Load slip and Gated out: TRUCK_REPORTING.STATUS = COMPLETED
      //Case2: Truck Gated in but rejected during inspection and Gated out, will not pick any Load slip: TRUCK_REPORTING.STATUS = COMPLETED, TRUCK_REPORTING.REJ_STATUS = REJECTED.
      truckReport.setStatus(Constants.TruckReportStatus.COMPLETED);
    }
    truckReportRepository.save(truckReport);
  }

  @Override
  public String getLinkedPlntLoc(String source, String destination) {
    /*Considering Only plant 1007*/
    if (!StringUtils.isEmpty(source)) {
      Optional<MTLocation> optionalMTLocation = locationRepository.findByLocationId(source);
      if (optionalMTLocation.isPresent()) {
        if (!StringUtils.isEmpty(optionalMTLocation.get().getLocationClass()) && !StringUtils.isEmpty(optionalMTLocation.get().getLinkedPlant())
            && optionalMTLocation.get().getLocationClass().equalsIgnoreCase("EXT_WAREHOUSE") && optionalMTLocation.get().getLinkedPlant().equalsIgnoreCase("1007")) {
          /*Destinaton should be other 1007 && Location_Class = null OR NOT EXT_WAREHOUSE*/
          if (!StringUtils.isEmpty(destination)) {
            Optional<MTLocation> optionalMTLocationDest = locationRepository.findByLocationId(destination);
            if ((optionalMTLocationDest.isPresent() && !optionalMTLocationDest.get().getId().contentEquals("1007") &&
                (!LocationType.EXT_WAREHOUSE.name().equalsIgnoreCase(optionalMTLocationDest.get().getLocationClass()))) || (mtCustomerRepository.existsById(destination))) {
              return optionalMTLocation.get().getLinkedPlant();
            }
          }
        }
      }
    }
    return source;
  }

  @Override
  public List<String> getLinkedExtWareHouse(String plantSource) {
    List<String> mtLocationList = null;
    if (plantSource.contentEquals("1007")) {
      mtLocationList = locationRepository.findIdByLinkedPlant(plantSource);
    }
    return mtLocationList;
  }

  /*Generating TransporterSAPCode*/
  @Override
  public Map<String, Object> calculateTrasporterSapCode(IndentSummary indentSummary, IndentDetails indentDetails) {
    /*Transporter SAP code with source, destination, servpro, truckType*/
    String transporterSapCode = null;
    Double ttDays = null;
    Optional<MTLocation> optionalMTLocation = locationRepository.findByLocationId(indentSummary.getSourceLocation());
    List<Freight> freights = freightRepository.findAllBySourceAndDestAndServProvAndTruckTypeBetweenEffectiveAndExpiryDate(indentSummary.getSourceLocation(), indentSummary.getDestinationLocation(), indentSummary.getTransporter(), indentDetails.getActualTruckType(), DateUtils.setTimeToMidnight(new Date()));
    transporterSapCode = getTransporterCodeByFreights(transporterSapCode, freights);
    ttDays = getTTDaysByFreights(ttDays, freights);
    if (StringUtils.isEmpty(transporterSapCode) && optionalMTLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equals(optionalMTLocation.get().getLocationClass())) {
      /*if source is EXT_WAREHOUSE then search for freight with linkedPlantLoc(1007)*/
      freights = freightRepository.findAllBySourceAndDestAndServProvAndTruckTypeBetweenEffectiveAndExpiryDate(optionalMTLocation.get().getLinkedPlant(), indentSummary.getDestinationLocation(), indentSummary.getTransporter(), indentDetails.getActualTruckType(), DateUtils.setTimeToMidnight(new Date()));
      transporterSapCode = getTransporterCodeByFreights(transporterSapCode, freights);
      ttDays = getTTDaysByFreights(ttDays, freights);
    }
    if (StringUtils.isEmpty(transporterSapCode)) {
      /*Transporter SAP code with source, destination, servpro*/
      freights = freightRepository.findAllBySourceAndDestAndServProvBetweenEffectiveAndExpiryDate(indentSummary.getSourceLocation(), indentSummary.getDestinationLocation(), indentSummary.getTransporter(), DateUtils.setTimeToMidnight(new Date()));
      transporterSapCode = getTransporterCodeByFreights(transporterSapCode, freights);
      ttDays = getTTDaysByFreights(ttDays, freights);
      if (StringUtils.isEmpty(transporterSapCode) && optionalMTLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equals(optionalMTLocation.get().getLocationClass())) {
        /*if source is EXT_WAREHOUSE then search for freight with linkedPlantLoc(1007)*/
        freights = freightRepository.findAllBySourceAndDestAndServProvBetweenEffectiveAndExpiryDate(optionalMTLocation.get().getLinkedPlant(), indentSummary.getDestinationLocation(), indentSummary.getTransporter(), DateUtils.setTimeToMidnight(new Date()));
        transporterSapCode = getTransporterCodeByFreights(transporterSapCode, freights);
        ttDays = getTTDaysByFreights(ttDays, freights);
      }

      if (StringUtils.isEmpty(transporterSapCode)) {
        /*Transporter SAP code with source, servpro*/
        freights = freightRepository.findAllBySourceAndServProvBetweenEffectiveAndExpiryDate(indentSummary.getSourceLocation(), indentSummary.getTransporter(), DateUtils.setTimeToMidnight(new Date()));
        transporterSapCode = getTransporterCodeByFreights(transporterSapCode, freights);
        ttDays = getTTDaysByFreights(ttDays, freights);
        if (StringUtils.isEmpty(transporterSapCode) && optionalMTLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equals(optionalMTLocation.get().getLocationClass())) {
          /*if source is EXT_WAREHOUSE then search for freight with linkedPlantLoc(1007)*/
          freights = freightRepository.findAllBySourceAndServProvBetweenEffectiveAndExpiryDate(optionalMTLocation.get().getLinkedPlant(), indentSummary.getTransporter(), DateUtils.setTimeToMidnight(new Date()));
          transporterSapCode = getTransporterCodeByFreights(transporterSapCode, freights);
          ttDays = getTTDaysByFreights(ttDays, freights);
        }
        if (StringUtils.isEmpty(transporterSapCode)) {
          freights = freightRepository.findTranspoterSAPCodeSourceAndDestAndServProvAndTruckType(indentSummary.getSourceLocation(), indentSummary.getDestinationLocation(), indentSummary.getTransporter(), indentDetails.getActualTruckType());
          transporterSapCode = getTransporterCodeByFreights(transporterSapCode, freights);
          ttDays = getTTDaysByFreights(ttDays, freights);
          if (StringUtils.isEmpty(transporterSapCode) && optionalMTLocation.isPresent() && LocationType.EXT_WAREHOUSE.name().equals(optionalMTLocation.get().getLocationClass())) {
            /*if source is EXT_WAREHOUSE then search for freight with linkedPlantLoc(1007)*/
            freights = freightRepository.findTranspoterSAPCodeSourceAndDestAndServProvAndTruckType(optionalMTLocation.get().getLinkedPlant(), indentSummary.getDestinationLocation(), indentSummary.getTransporter(), indentDetails.getActualTruckType());
            transporterSapCode = getTransporterCodeByFreights(transporterSapCode, freights);
            ttDays = getTTDaysByFreights(ttDays, freights);
          }
        }
      }
    }
    Map<String, Object> freightMap = new HashMap<>();
    freightMap.put("transporterSapCode", transporterSapCode);
    freightMap.put("ttDays", ttDays);
    return freightMap;
  }

  private String getTransporterCodeByFreights(String transporterSapCode, List<Freight> freights) {

    if (freights != null && !freights.isEmpty()) {
      transporterSapCode = freights.get(0).getTransporterSapCode();
    }

    return transporterSapCode;
  }
  private Double getTTDaysByFreights(Double ttDays, List<Freight> freights) {

      if (freights != null && !freights.isEmpty()) {
      ttDays = freights.get(0).getTtDays();
    }

    return ttDays;
  }

}
