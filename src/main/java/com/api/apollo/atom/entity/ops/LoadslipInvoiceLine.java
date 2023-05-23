package com.api.apollo.atom.entity.ops;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "LOADSLIP_INV_LINE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoadslipInvoiceLine {

  @EmbeddedId
  @AttributeOverrides({
      @AttributeOverride(name = "loadslipId", column = @Column(name = "LOADSLIP_ID", nullable = false)),
      @AttributeOverride(name = "invoiceNum", column = @Column(name = "INVOICE_NUMBER", nullable = false)),
      @AttributeOverride(name = "lineNum", column = @Column(name = "LINE_NO", nullable = false)),
      @AttributeOverride(name = "sapLineNum", column = @Column(name = "SAP_LINE_NO", nullable = false)),
      @AttributeOverride(name = "itemId", column = @Column(name = "ITEM_ID", nullable = false))})
  private LoadslipInvLineId loadslipInvLineId;

  @Column(name = "INSERT_DATE")
  private Date insertDate;

  @Column(name = "INSERT_USER")
  private String insertUser;

  @Column(name = "QTY")
  private Integer quantity;

  @Column(name = "UPDATE_DATE")
  private  Date updateDate;

  @Column(name = "UPDATE_USER")
  private String updateUser;

  @Column(name = "WEIGHT")
  private Double weight;

  @Column(name = "WEIGHT_UOM")
  private String weightUom;

  @Column(name = "BATCH_CODE")
  private String batchCode;


}
