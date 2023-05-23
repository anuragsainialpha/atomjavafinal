package com.api.apollo.atom.entity.ops;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "LOADSLIP_INV_HEADER")
@Setter
@Getter
public class LoadslipInvoiceHeader {

  @EmbeddedId
  @AttributeOverrides({
      @AttributeOverride(name = "loadslipId", column = @Column(name = "LOADSLIP_ID", nullable = false)),
      @AttributeOverride(name = "invoiceNum", column = @Column(name = "INVOICE_NUMBER", nullable = false))})
  private InvoiceHederId invoiceHeader;

  @Column(name = "SHIPMENT_ID")
  private String shipmentId;

  @Column(name = "DELIVERY_NUMBER")
  private String deliveryNum;

  @Column(name = "SO_STO_NUM")
  private String sostoNum;

  @Column(name = "INVOICE_DATE")
  private Date invoiceDate;

  @Column(name = "INSERT_USER")
  private String insertUser;

  @Column(name = "INSERT_DATE")
  private Date insertDate;

  @Column(name = "UPDATE_DATE")
  private String updateDate;

  @Column(name = "UPDATE_USER")
  private String updateUser;

  @Column(name = "LR_NUMBER")
  private String lrNumber;

  @Column(name = "TRUCK_NUMBER")
  private String truckNumber;

  @Column(name = "SOURCE_LOC")
  private String sourceLoc;

  @Column(name = "DEST_LOC")
  private String destLoc;

  @Column(name = "LR_DATE")
  private Date lrDate;

  @Column(name = "SAP_INV_VALUE")
  private Integer sapInvoiceVolue;

  @Column(name = "E_WAY_BILL_NO")
  private String eWayBillNum;

  @Column(name = "E_WAY_BILL_DATE")
  private Date eWayBillDate;

  @Column(name = "TOTAL_WEIGHT")
  private Integer totalWeight;

  @Column(name = "TOTAL_WEIGHT_UOM")
  private String weightUOM;


}
