package com.api.apollo.atom.entity.ops;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "LOADSLIP_DETAIL")
@Getter
@Setter
public class LoadslipDetail {
  @EmbeddedId
  @AttributeOverrides({
      @AttributeOverride(name = "loadslipId", column = @Column(name = "LOADSLIP_ID", nullable = false)),
      @AttributeOverride(name = "lineNo", column = @Column(name = "LINE_NO", nullable = false)),
      @AttributeOverride(name = "itemId", column = @Column(name = "ITEM_ID", nullable = false))})
  private LoadslipDetailId loadslipDetailId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "LOADSLIP_ID", nullable = false, insertable = false, updatable = false)
  @JsonBackReference
  private Loadslip loadslip;

  @Column(name = "ITEM_DESCRIPTION")
  private String itemDescription;

  @Column(name = "BATCH_CODE")
  private String batchCode;

  @Column(name = "LOAD_QTY")
  private Integer loadQty;

  @Column(name = "GROSS_WT")
  private Double grossWt;

  @Column(name = "GROSS_WT_UOM")
  private String grossWtUom;

  @Column(name = "GROSS_VOL")
  private Double grossVol;

  @Column(name = "GROSS_VOL_UOM")
  private String grossVolUom;

  @Column(name = "TTE")
  private Double tte;

  @Column(name = "SCANNABLE")
  private String scannable;

  @Column(name = "SCANNED_QTY")
  private Integer scannedQty;

  @Column(name = "INSERT_USER", nullable = false)
  private String insertUser;

  @Column(name = "INSERT_DATE", nullable = false)
  private Date insertDate;

  @Column(name = "UPDATE_USER")
  private String updateUser;

  @Column(name = "UPDATE_DATE")
  private Date updateDate;

  @Column(name = "INVOICE_NUMBER")
  private String invoiceNumber;

  @Column(name = "IS_SPLIT")
  private String isSplit;

  @Column(name = "IS_LOADED",nullable = false)
  private String isLoaded = "N";

  @Column(name = "ITEM_CATEGORY")
  private String itemCategory;
}
