package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.entity.ops.LoadslipDetail;
import com.api.apollo.atom.entity.ops.LoadslipDetailBom;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
public class LoadslipDraftDataDto {

  private double lineNumber;

  private String itemId;
  private String itemDesc;

  private String batchCode;
  private Object availableQty;
  private int loadedQty;


  private double grossWt;
  private Double grossVol;

  private double tte;
  private String isScannable;
  private int scannedQty;
  private String isSplit;

  private String tubeSKU;
  private String tubeBatch;
  private int tubeQty;
  private Object tubeCompQty;

  private String flapSKU;
  private String flapBatch;
  private int falpQty;
  private Object flapCompQty;

  private String valveSKU;
  private String valveBatch;
  private int valveQty;
  private Object valveCompQty;
  private Object itemCategory;
  private Object priority;

  private String tubeDesc;
  private String flapDesc;
  private String valveDesc;

  private Object invoiceList;
  private String containerNum;
  private String invoiceNumber;
  private Object weight;
  private int pctr;
  private String itemClassification;

  private boolean canLSItemDeleted = true;

  public LoadslipDraftDataDto(LoadslipDetail loadslipDetail, Optional<LoadslipDetailBom> optionalLoadslipDetailBom, Object avlQty, Object tubeCompQty, Object flapCompQty, Object valveCompQty, Object itemCategory, Object priority, Optional<ExportInfoDto> exportitem,String itemClassification) {
    this.lineNumber = loadslipDetail.getLoadslipDetailId().getLineNo();
    this.invoiceNumber = loadslipDetail.getInvoiceNumber();
    this.itemId = loadslipDetail.getLoadslipDetailId().getItemId();
    this.itemDesc = loadslipDetail.getItemDescription();
    this.batchCode = loadslipDetail.getBatchCode();
    this.loadedQty = loadslipDetail.getLoadQty();
    this.grossWt = loadslipDetail.getGrossWt();
    this.grossVol = loadslipDetail.getGrossVol();
    this.weight = loadslipDetail.getGrossWt();
    this.tte = loadslipDetail.getTte();
    this.isScannable = loadslipDetail.getScannable();
    this.availableQty = avlQty;
    this.tubeCompQty = tubeCompQty;
    this.valveCompQty = valveCompQty;
    this.flapCompQty = flapCompQty;
    //if item category getting empty then set category from loadslip detail
    this.itemCategory = StringUtils.isEmpty(itemCategory) ? loadslipDetail.getItemCategory() : itemCategory;
    this.itemClassification = StringUtils.isEmpty(itemClassification) ? null : itemClassification;
    this.priority = priority;
    this.isSplit = loadslipDetail.getIsSplit();
    this.scannedQty = loadslipDetail.getScannedQty() != null ? loadslipDetail.getScannedQty() : 0;

    if(StringUtils.isEmpty(this.itemCategory) && exportitem.isPresent()){
      ExportInfoDto exportItem = exportitem.get();
      this.itemCategory = exportitem.get().getItemCategory();
      this.invoiceNumber = this.invoiceNumber!=null?this.invoiceNumber:exportItem.getInvoiceNumber();
      this.weight = this.weight!=null ? this.weight:exportItem.getWeight();
    }

    if (optionalLoadslipDetailBom.isPresent()) {
      this.tubeSKU = optionalLoadslipDetailBom.get().getTubeSku();
      this.tubeDesc = optionalLoadslipDetailBom.get().getTubeDesc();
      this.tubeBatch = optionalLoadslipDetailBom.get().getTubeBatch();
      this.tubeQty = optionalLoadslipDetailBom.get().getTubeQty();
      this.flapSKU = optionalLoadslipDetailBom.get().getFlapSku();
      this.flapDesc = optionalLoadslipDetailBom.get().getFlapDesc();
      this.flapBatch = optionalLoadslipDetailBom.get().getFlapBatch();
      this.falpQty = optionalLoadslipDetailBom.get().getFlapQty();
      this.valveSKU = optionalLoadslipDetailBom.get().getValveSku();
      this.valveBatch = optionalLoadslipDetailBom.get().getValveBatch();
      this.valveDesc = optionalLoadslipDetailBom.get().getValveDesc();
      this.valveQty = optionalLoadslipDetailBom.get().getValveQty();
    }

  }
}
