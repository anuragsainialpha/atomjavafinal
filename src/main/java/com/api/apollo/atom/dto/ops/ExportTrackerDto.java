package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.ops.Shipment;
import com.api.apollo.atom.entity.ops.TruckReport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExportTrackerDto {

  private String containerNum;

  private String shipmentId;

  private String truckNum;

  private String actualTtype;

  private String truckType;

  private String sourceLoc;

  private String reportLocation;

  private String destLoc;

  private String destDis;

  private Date reportingDate;

  private Date gateInDate;

  private Date gateOutDate;

  private Date gateInDateCfs;

  private Date gateOutDateCfs;

  private Date gateInDatePort;

  private Date shippedOnBoard;

  private Date vesselDepartPol;

  private Date vesselArrivePod;


  public ExportTrackerDto(String containerNum, String shipmentId, String truckNum, String actualTtype, String truckType, String sourceLoc, String reportLocation, String destLoc, Date reportingDate, Date gateInDate, Date gateOutDate, Date gateInDateCfs, Date gateOutDateCfs, Date gateInDatePort, Date shippedOnBoard, Date vesselDepartPol, Date vesselArrivePod) {
    this.containerNum = containerNum;
    this.shipmentId = shipmentId;
    this.truckNum = truckNum;
    this.actualTtype = actualTtype;
    this.truckType = truckType;
    this.sourceLoc = sourceLoc;
    this.reportLocation = reportLocation;
    this.destLoc = destLoc;
    this.reportingDate = reportingDate;
    this.gateInDate = gateInDate;
    this.gateOutDate = gateOutDate;
    this.gateInDateCfs = gateInDateCfs;
    this.gateOutDateCfs = gateOutDateCfs;
    this.gateInDatePort = gateInDatePort;
    this.shippedOnBoard = shippedOnBoard;
    this.vesselDepartPol = vesselDepartPol;
    this.vesselArrivePod = vesselArrivePod;
  }

  public ExportTrackerDto(String containerNum, String shipmentId, String truckNum, String actualTtype, String truckType, String sourceLoc, String destLoc, Date reportingDate, Date gateInDate, Date gateOutDate, Date gateInDateCfs, Date gateOutDateCfs, Date gateInDatePort, Date shippedOnBoard, Date vesselDepartPol, Date vesselArrivePod) {
    this.containerNum = containerNum;
    this.shipmentId = shipmentId;
    this.truckNum = truckNum;
    this.actualTtype = actualTtype;
    this.truckType = truckType;
    this.sourceLoc = sourceLoc;
    this.destLoc = destLoc;
    this.reportingDate = reportingDate;
    this.gateInDate = gateInDate;
    this.gateOutDate = gateOutDate;
    this.gateInDateCfs = gateInDateCfs;
    this.gateOutDateCfs = gateOutDateCfs;
    this.gateInDatePort = gateInDatePort;
    this.shippedOnBoard = shippedOnBoard;
    this.vesselDepartPol = vesselDepartPol;
    this.vesselArrivePod = vesselArrivePod;
  }

  public ExportTrackerDto(TruckReport truckReport, Shipment shipment) {
    destLoc = truckReport.getDestinationLocation();
    reportingDate = truckReport.getReportDate();
    gateInDate = truckReport.getGateInDate();
    gateOutDate = truckReport.getGateOutDate();
    sourceLoc = truckReport.getSourceLocation();

    if (shipment != null) {
      containerNum = shipment.getContainerNum();
      truckNum = shipment.getTruckNumber();
      actualTtype = shipment.getActualTruckType();
      truckType = shipment.getTruckType();
      gateInDateCfs = shipment.getGateInDateCfs();
      gateOutDateCfs = shipment.getGateOutDateCfs();
      gateInDatePort = shipment.getGateInDatePort();
      shippedOnBoard = shipment.getShippedOnboardDate();
      vesselDepartPol = shipment.getVesselDepartPolDate();
      vesselArrivePod = shipment.getVesselArrivePodDate();
    }
  }

  public Shipment updateTruckTracker(Shipment shipment,  ApplicationUser applicationUser) {
    shipment.setGateInDateCfs(this.gateInDateCfs);
    shipment.setGateOutDateCfs(this.gateOutDateCfs);
    shipment.setGateInDatePort(this.gateInDatePort);
    shipment.setShippedOnboardDate(this.shippedOnBoard);
    shipment.setVesselDepartPolDate(this.vesselDepartPol);
    shipment.setVesselArrivePodDate(this.vesselArrivePod);
    shipment.setUpdateDate(new Date());
    shipment.setUpdateUser(applicationUser.getUserId());
    return shipment;
  }

}
