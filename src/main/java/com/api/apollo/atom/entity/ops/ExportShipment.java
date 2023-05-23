package com.api.apollo.atom.entity.ops;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;


@Entity
@Table(name = "SHIPMENT_EXPORT")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExportShipment {

  @Id
  @Column(name = "SAP_INVOICE")
  private String sapInvoice;

  @Column(name = "SHIPMENT_ID")
  private String shipmentId;

  @Column(name = "ACTUAL_VESSEL")
  private String actualVessel;

  @Column(name = "BILLING_PARTY")
  private String billingParty;

  @Column(name = "BOOKING_NUM")
  private String bookingNum;

  @Column(name = "CHA")
  private String cha;

  @Column(name = "COFD")
  private String cofd;

  @Column(name = "CONTAINER_NUM")
  private String containerNum;

  @Column(name = "CONT_PICK_DATE")
  private Date countPicDate;

  @Column(name = "CUSTOMER_NAME")
  private String customerName;

  @Column(name = "CUSTOMS_EXAM_DATE")
  private Date customsExamDate;

  @Column(name = "ETA_POD")
  private Date etaPod;

  @Column(name = "EXPORT_REMARKS")
  private String exportRemarks;

  @Column(name = "FORWARDER")
  private String forwarder;

  @Column(name = "GATEIN_DATE_CFS")
  private Date gateInDateCfs;

  @Column(name = "GATEIN_DATE_PORT")
  private Date gateInDatePort;

  @Column(name = "GATEOUT_DATE_CFS")
  private Date gateOutDateCgs;

  @Column(name = "INCO_TERM")
  private String incoTerm;

  @Column(name = "INSERT_DATE")
  private Date insertDate;

  @Column(name = "INSERT_USER")
  private String insertUser;

  @Column(name = "INV_AMOUNT")
  private String invAmount;

  @Column(name = "IS_SYNC_OTM")
  private String isSyncOtm;

  @Column(name = "LEO_DATE")
  private Date leoDate;

  @Column(name = "PAYMENT_TERMS")
  private String paymetTerms;

  @Column(name = "PI_NO")
  private String piNum;

  @Column(name = "PLANNED_VESSEL")
  private String plannedVessel;

  @Column(name = "POD")
  private String pod;

  @Column(name = "POL")
  private String pol;

  @Column(name = "POST_INV_NO")
  private String postInvNum;

  @Column(name = "PRE_INV_NO")
  private String preInvNum;

  @Column(name = "SHIPPED_ONBOARD_DATE")
  private Date shipmentOnBordDate;

  @Column(name = "SHIPPING_BILL")
  private String shippingBill;

  @Column(name = "SHIPPING_BILL_DATE")
  private Date shippingBillDate;

  @Column(name = "SHIPPING_LINE")
  private String shippingLine;

  @Column(name = "SOURCE_LOC")
  private String sourceLoc;

  @Column(name = "STUFFING_DATE")
  private Date stuffingDate;

  @Column(name = "UPDATE_DATE")
  private Date updateDate;

  @Column(name = "UPDATE_USER")
  private String updateUser;

  @Column(name = "VESSEL_DEPART_POL_DATE")
  private Date vesslDepartPolDate;

}
