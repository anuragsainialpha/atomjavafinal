package com.api.apollo.atom.entity.ops;

import com.api.apollo.atom.constant.Constants.LoadslipStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "LOADSLIP")
@Getter
@Setter
@NoArgsConstructor
public class Loadslip {
  @Id
  @Column(name = "LOADSLIP_ID", unique = true, nullable = false)
  private String loadslipId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "SHIPMENT_ID")
  @JsonBackReference
  private Shipment shipment;

  @Column(name = "LOADSLIP_NAME")
  private String loadslipName;

  @Column(name = "LOADSLIP_TYPE")
  private String loadslipType;

  @Column(name = "SOURCE_LOC")
  private String sourceLoc;

  @Column(name = "DEST_LOC")
  private String destLoc;

  @Column(name = "SHIP_TO")
  private String shipTo;

  @Column(name = "LR_NUM")
  private String lrNum;

  @Column(name = "LR_DATE")
  private Date lrDate;

  @Column(name = "BAY")
  private String bay;

  @Column(name = "STO_SO_NUM")
  private String stoSoNum;

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

  @Column(name = "QTY")
  private Integer qty;

  @Column(name = "TTE_QTY")
  private Double tte;

  @Column(name = "WEIGHT")
  private Double weight;

  @Column(name = "WEIGHT_UOM")
  private String weightUom;

  @Column(name = "VOLUME")
  private Double volume;

  @Column(name = "VOLUME_UOM")
  private String volumeUom;

  @Column(name = "STATUS")
  @Enumerated(EnumType.STRING)
  private LoadslipStatus status;

  @Column(name = "LOAD_SEQ")
  private Integer loadSeq;

  @Column(name = "DROP_SEQ")
  private Integer dropSeq;

  @Column(name = "ARRIVED_DATE")
  private Date arrivedDate;

  @Column(name = "LSPRINT_DATE")
  private Date lsprintDate;

  @Column(name = "LS_DATE")
  private Date lsDate;

  @Column(name = "LE_DATE")
  private Date leDate;

  @Column(name = "CONFIRM_DATE")
  private Date confirmDate;

  @Column(name = "RELEASE_DATE")
  private Date releaseDate;

  @Column(name = "US_DATE")
  private Date usDate;

  @Column(name = "UE_DATE")
  private Date ueDate;

  @Column(name = "TOT_TYRES")
  private Integer totTyres;

  @Column(name = "TOT_TUBES")
  private Integer totTubes;

  @Column(name = "TOT_FLAPS")
  private Integer totFlaps;

  @Column(name = "TOT_VALVE")
  private Integer totValve;

  @Column(name = "TOT_PCTR")
  private Integer totPctr;

  @Column(name = "OTHER")
  private Integer otherQty;

  @Column(name = "TOT_QTY")
  private Integer totQty;

  @Column(name = "INSERT_USER", nullable = false)
  private String insertUser;

  @Column(name = "INSERT_DATE", nullable = false)
  private Date insertDate;

  @Column(name = "UPDATE_USER")
  private String updateUser;

  @Column(name = "UPDATE_DATE")
  private Date updateDate;

  @Column(name = "ITEM_CATEGORY")
  private String itemCategory;

  @Column(name = "TTE_UTIL")
  private Double tteUtil;

  @Column(name = "WEIGHT_UTIL")
  private Double weightUtil;

  @Column(name = "VOLUME_UTIL")
  private Double volumeUtil;

  @Column(name = "SEND_FOR_BARCODE_DATE")
  private Date sendForBarcodeDate;

  @Column(name = "INT_STATUS")
  private String integrationStatus;

  @Column(name = "INT_MESSAGE")
  private String integrationMsg;

  @Column(name = "GO_APPROVAL_REASON")
  private String goApprovalReason;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "loadslip")
  @JsonManagedReference
  @JsonIgnore
  private List<LoadslipDetailBom> loadslipDetailBoms = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "loadslip")
  @JsonManagedReference
  @JsonIgnore
  private List<LoadslipDetail> loadslipDetails = new ArrayList<>();

  public Loadslip(Shipment shipment) {
    this.shipment = shipment;
  }

  @Column(name = "COMMENTS")
  private String comments;

  @Column(name = "DIT_QTY")
  private Integer ditQty;

  @Column(name = "SHORT_QTY")
  private Integer shortQty;

  @Column(name = "E_WAY_BILL_NO")
  private String eWayBillNo;

  @Column(name = "SAP_INV_VALUE")
  private Integer sapInvValue;

  @Column(name = "SAP_INV_WEIGHT")
  private Integer sapInvWeight;

  @Column(name = "MKT_SEG")
  private String marketSegment;

  @Column(name = "CUSTOM_INV_NUMBER")
  private String customInvoiceNumber;

  @Transient
  private String shipmentId;

  @Column(name = "TRACKING_CONSENT_STATUS")
  private String trackingConsentStatus;

  @Column(name = "CONSENT_PHONE_TELECOM")
  private String consentPhoneTelecom;


  public Loadslip(String loadslipId, LoadslipStatus status) {
    this.loadslipId = loadslipId;
    this.status = status;
  }

  public Loadslip(Map<String, Object> objectMap){
    this.shipmentId = objectMap.get("shipmentId") != null ?objectMap.get("shipmentId").toString() : null;
    this.totFlaps = objectMap.get("totalFlap") != null ? Integer.parseInt(objectMap.get("totalFlap").toString()) : null;
    this.otherQty = objectMap.get("other") != null ? Integer.parseInt(objectMap.get("other").toString()) : null;
    this.totValve = objectMap.get("totalValve") != null ? Integer.parseInt(objectMap.get("totalValve").toString()) : null;
    this.totTyres = objectMap.get("totalTyres") != null ? Integer.parseInt(objectMap.get("totalTyres").toString()) : null;
    this.totTubes = objectMap.get("totalTubes") != null ? Integer.parseInt(objectMap.get("totalTubes").toString()) : null;
    this.totQty = objectMap.get("totalQty") != null ? Integer.parseInt(objectMap.get("totalQty").toString()) : null;
    this.totPctr = objectMap.get("totalPctr") != null ? Integer.parseInt(objectMap.get("totalPctr").toString()) : null;
    this.lrNum = objectMap.get("lrNum") != null ? objectMap.get("lrNum").toString() : null;
    this.lrDate = objectMap.get("lrDate") != null ? (Date) objectMap.get("lrDate") : null;
    this.sapInvoice = objectMap.get("sapInv") != null ? objectMap.get("sapInv").toString() : null;
    this.trackingConsentStatus = objectMap.get("trackingConsentStatus") != null ? objectMap.get("trackingConsentStatus").toString() : null;
    this.consentPhoneTelecom = objectMap.get("consentPhoneTelecom") != null ? objectMap.get("consentPhoneTelecom").toString() : null;
    this.goApprovalReason = objectMap.get("goApprovalReason") != null ?  objectMap.get("goApprovalReason").toString() : null;
  }
}
