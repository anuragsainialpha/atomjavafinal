package com.api.apollo.atom.dto.ops;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Setter
@Getter
public class InvoiceDataDto {

  private Object stoSoNum;

  private Object invoieNum;

  private Object grnNum;

  private Object itemId;

  private Object itemDesc;

  private Object souceLoc;

  private Object destLoc;

  private Object loadslipQty;

  private Object invoiceQty;

  private Object grnQty;

  private Object ditQty;

  private Object shotQty;

  public InvoiceDataDto(Map<String,Object> invoiceData){
   this.stoSoNum = invoiceData.get("STO_SO_NUM");
   this.invoieNum = invoiceData.get("INVOICE_NUMBER");
   this.grnNum = invoiceData.get("GRN_NUMBER");
   this.itemId = invoiceData.get("ITEM_ID");
    this.itemDesc = invoiceData.get("ITEM_DESCRIPTION");
    this.souceLoc = invoiceData.get("SOURCE_LOCATION");
    this.destLoc = invoiceData.get("DEST_LOCATION");
    this.loadslipQty = invoiceData.get("LOADSLIP_QUANTITY");
    this.invoiceQty = invoiceData.get("INVOICE_QUANTITY") ;
    this.grnQty = invoiceData.get("GRN_QUANTITY") ;
    this.ditQty = invoiceData.get("DIT_QUANTITY") ;
    this.shotQty = invoiceData.get("SHORT_QUANTITY");
  }
}
