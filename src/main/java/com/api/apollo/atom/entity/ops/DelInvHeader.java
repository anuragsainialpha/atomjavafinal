package com.api.apollo.atom.entity.ops;


import com.api.apollo.atom.constant.Constants;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "del_inv_header")
@Getter
@Setter
@NoArgsConstructor
public class DelInvHeader {

  @Id
  @Column(name = "INVOICE_NUMBER", nullable = false)
  private String invoiceNumber;

  @GeneratedValue
  @Column(name = "LOADSLIP_ID")
  private String loadslipId;

  @Column(name = "SHIPMENT_ID")
  private String shipmentId;


  @Column(name = "DELIVERY_NUMBER")
  private String deliveryNumber;

  @Column(name = "SO_STO_NUM")
  private String soStoNum;

  @Column(name = "INVOICE_DATE")
  @JsonFormat(pattern="dd-MMM-yyyy",timezone = "IST")
  private Date invoiceDate;

  @Column(name = "INSERT_USER", nullable = false)
  private String insertUser;

  @Column(name = "INSERT_DATE", nullable = false)
  @JsonFormat(pattern="dd-MMM-yyyy",timezone = "IST")
  private Date insertDate;

  @Column(name = "UPDATE_USER")
  private String updateUser;

  @Column(name = "UPDATE_DATE")
  private String updateDate;

  @Column(name = "LR_NUMBER")
  private String lrNumber;

  @Column(name = "TRUCK_NUMBER")
  private String truckNumber;

  @Column(name = "SOURCE_LOC")
  private String sourceLoc;

  @Column(name = "DEST_LOC")
  private String destLoc;

  @Column(name = "LR_DATE")
  @JsonFormat(pattern="dd-MMM-yyyy")
  private Date lrDate;

  @Column(name = "CONTAINER_NUM")
  private String containerNum;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "POL")
  private String pol;

  @Column(name = "POD")
  private String pod;

  @Column(name = "INCOTERM")
  private String incoterm;

  @Column(name = "INCOTERM_LOC")
  private String incotermLoc;

  @Column(name = "BILL_TO")
  private String billTo;

  @Column(name = "BILL_TO_NAME")
  private String billToName;

  @Column(name = "SHIP_TO")
  private String shipTo;

  @Column(name = "SHIP_TO_NAME")
  private String ShipToName;

  @Column(name = "DEST_LOC_COUNTRY")
  private String destLocCountry;

  @Column(name = "TYPE")
  @Enumerated(EnumType.STRING)
  private Constants.DelInvType type;


  public DelInvHeader(String invoiceNumber, String loadslipId) {
    this.invoiceNumber = invoiceNumber;
    this.loadslipId = loadslipId;
  }
}
