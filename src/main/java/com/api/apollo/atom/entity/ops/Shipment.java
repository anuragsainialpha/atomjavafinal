package com.api.apollo.atom.entity.ops;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "SHIPMENT")
@Getter
@Setter
@NoArgsConstructor
@SequenceGenerator(name = "shipment_seq", sequenceName = "SHIPMENT_SEQ", allocationSize = 1)
public class Shipment {

  @Id
  @Column(name = "SHIPMENT_ID", unique = true, nullable = false)
  private String shipmentId;

  @Column(name = "SERVPROV")
  private String servprov;

  @Column(name = "SHIPMENT_NAME")
  private String shipmentName;

  @Column(name = "SHIPMENT_TYPE")
  private String shipmentType;

  @Column(name = "TRANSPORTER_SAP_CODE")
  private String transporterSapCode;

  @Column(name = "TRUCK_TYPE")
  private String truckType;

  @Column(name = "TRUCK_NUMBER")
  private String truckNumber;

  @Column(name = "ACTUAL_TRUCK_TYPE")
  private String actualTruckType;

  @Column(name = "VARIANT_1")
  private String variant1;

  @Column(name = "VARIANT_2")
  private String variant2;

  @Column(name = "INDENT_ID")
  private String indentId;

  @Column(name = "TRANSPORT_MODE")
  private String transportMode;

  @Column(name = "DRIVER_LICENSE")
  private String driverLicense;

  @Column(name = "DRIVER_MOBILE")
  private String driverMobile;

  @Column(name = "DRIVER_NAME")
  private String driverName;

  @Column(name = "TRANSHIPMENT")
  private String transhipment;

  @Column(name = "FREIGHT")
  private Double freight;

  @Column(name = "FREIGHT_UOM")
  private String freightUom;

  @Column(name = "STD_TT")
  private Double stdTt;

  @Column(name = "SO_STO_NUM")
  private String soStoNum;

  @Column(name = "PO_NUM")
  private String poNum;

  @Column(name = "DELIVERY")
  private String delivery;

  @Column(name = "SAP_INVOICE")
  private String sapInvoice;

  @Column(name = "SAP_INVOICE_DATE")
  private Date sapInvoiceDate;

  @Column(name = "GRN")
  private String grn;

  @Column(name = "GRN_DATE")
  private Date grnDate;

  @Column(name = "START_TIME")
  private Date startTime;

  @Column(name = "END_TIME")
  private Date endTime;

  @Column(name = "TOTAL_QTY")
  private Integer totalQty;

  @Column(name = "TOTAL_TTE")
  private Double totalTte;

  @Column(name = "TOTAL_WEIGHT")
  private Double totalWeight;

  @Column(name = "TOTAL_WEIGHT_UOM")
  private String totalWeightUom;

  @Column(name = "TOTAL_VOLUME")
  private Double totalVolume;

  @Column(name = "TOTAL_VOLUME_UOM")
  private String totalVolumeUom;

  @Column(name = "TTE_UTIL")
  private Double tteUtil;

  @Column(name = "WEIGHT_UTIL")
  private Double weightUtil;

  @Column(name = "VOLUME_UTIL")
  private Double volumeUtil;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "STOP_TYPE")
  private String stopType;

  @Column(name = "INSERT_USER", nullable = false)
  private String insertUser;

  @Column(name = "INSERT_DATE", nullable = false)
  private Date insertDate;

  @Column(name = "UPDATE_USER")
  private String updateUser;

  @Column(name = "UPDATE_DATE")
  private Date updateDate;

  @Column(name = "FRT_AVAIL_FLAG")
  private String freightAvailability;

  @Column(name = "CONTAINER_NUM")
  private String containerNum;


  @Column(name = "GATEIN_DATE_CFS")
  private Date gateInDateCfs;

  @Column(name = "GATEOUT_DATE_CFS")
  private Date gateOutDateCfs;

  @Column(name = "GATEIN_DATE_PORT")
  private Date gateInDatePort;

  @Column(name = "SHIPPED_ONBOARD_DATE")
  private Date shippedOnboardDate;

  @Column(name = "VESSEL_DEPART_POL_DATE")
  private Date vesselDepartPolDate;

  @Column(name = "VESSEL_ARRIVE_POD_DATE")
  private Date vesselArrivePodDate;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "shipment")
  @JsonManagedReference
  @JsonIgnore
  private List<Loadslip> loadslips = new ArrayList<>();

  @Column(name = "DEST_COUNTRY")
  private String destCountry;

  @Column(name = "FT_TRIP_ID")
  private String ftTripId;

  @Column(name = "TRACKING_CONSENT_STATUS")
  private String shtrackingConsentStatus;

  @Column(name = "CONSENT_PHONE_TELECOM")
  private String shconsentPhoneTelecom;

  public Shipment(String shipmentId, String ftTripId){
    this.shipmentId = shipmentId;
    this.ftTripId = ftTripId;
  }

}
