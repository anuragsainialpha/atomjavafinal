package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.util.DateUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
public class ClsDataDto {

  private String shipmentId;
  private String loadslipId;
  private String gateouDateStr;

  private Date gateouDate;

  private String sourceLoc;
  private String destLoc;
  private String containerNum;
  private String stosoNum;
  private String delivery;
  private String sapInvoice;
  private String sapInvoiceDateStr;
  private Date sapInvoiceDate;
  private String customInvNumber;
  private String piNo;
  private String customerName ;
  private String preInvNo;
  private String incoterm;
  private String pol;
  private String pod;
  private String billingParty;
  private String shippingLine;
  private String contPickDateStr;

  private Date contPickDate;

  private String stuffingDateStr;

  private Date stuffingDate;

  private String bookingNum;
  private String postInvNo;
  private String cha;
  private String plannedVessel;
  private String vesselDepartPolDateStr;

  private Date vesselDepartPolDate;

  private String shippinBill;
  private String shippingBillDateStr;

  private Date shippingBillDate;

  private String gateInDateStr;

  private Date gateInDate;

  private String customsExamDateStr;

  private Date customsExamDate;

  private String gateoutDateCfsStr;

  private Date gateoutDateCfs;

  private String gateinDatePortStr;

  private Date gateinDatePort;
  private String actualVessel;

  private String shippedOnboardDateStr;

  private Date shippedOnboardDate;

  private String eportRemark;
  private String isSyncOtm;
  private String status;

  private String insertDateStr;

  private Date insertDate;

  private String cofd;

  private Date etaPod;

  private String etaPodStr;

  private String forwarder;

  private String invAmount;

  private Date leoDate;

  private String leoDateStr;

  private String paymentTerms;


  public void setDateToString(ClsDataDto clsDataDto){
    this.gateouDateStr = clsDataDto.gateouDate != null ? DateUtils.formatDate(clsDataDto.gateouDate, Constants.DATE_TIME_FORMAT) : null;
    this.sapInvoiceDateStr = clsDataDto.sapInvoiceDate != null ? DateUtils.formatDate(clsDataDto.sapInvoiceDate, Constants.DATE_TIME_FORMAT) : null;
    this.contPickDateStr = clsDataDto.contPickDate != null ? DateUtils.formatDate(clsDataDto.contPickDate, Constants.DATE_TIME_FORMAT) : null;
    this.stuffingDateStr = clsDataDto.stuffingDate != null ? DateUtils.formatDate(clsDataDto.stuffingDate, Constants.DATE_TIME_FORMAT) : null;

    this.vesselDepartPolDateStr = clsDataDto.vesselDepartPolDate != null ? DateUtils.formatDate(clsDataDto.vesselDepartPolDate, Constants.DATE_TIME_FORMAT) : null;
    this.shippingBillDateStr = clsDataDto.shippingBillDate != null ? DateUtils.formatDate(clsDataDto.shippingBillDate, Constants.DATE_TIME_FORMAT) : null;
    this.gateInDateStr = clsDataDto.gateInDate != null ? DateUtils.formatDate(clsDataDto.gateInDate, Constants.DATE_TIME_FORMAT) : null;
    this.customsExamDateStr = clsDataDto.customsExamDate != null ? DateUtils.formatDate(clsDataDto.customsExamDate, Constants.DATE_TIME_FORMAT) : null;
    this.gateoutDateCfsStr = clsDataDto.gateoutDateCfs != null ? DateUtils.formatDate(clsDataDto.gateoutDateCfs, Constants.DATE_TIME_FORMAT) : null;

    this.gateinDatePortStr = clsDataDto.gateinDatePort != null ? DateUtils.formatDate(clsDataDto.gateinDatePort, Constants.DATE_TIME_FORMAT) : null;
    this.shippedOnboardDateStr = clsDataDto.shippedOnboardDate != null ? DateUtils.formatDate(clsDataDto.shippedOnboardDate, Constants.DATE_TIME_FORMAT) : null;
    this.insertDateStr = clsDataDto.insertDate != null ? DateUtils.formatDate(clsDataDto.insertDate, Constants.DATE_TIME_FORMAT) : null;
    this.etaPodStr = clsDataDto.getEtaPod() != null ? DateUtils.formatDate(clsDataDto.etaPod, Constants.DATE_TIME_FORMAT) : null;
    this.leoDateStr = clsDataDto.getLeoDate() != null ? DateUtils.formatDate(clsDataDto.getLeoDate(), Constants.DATE_TIME_FORMAT) : null;
  }

}
