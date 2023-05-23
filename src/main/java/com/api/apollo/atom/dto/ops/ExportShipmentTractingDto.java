package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.ops.ExportShipment;
import com.api.apollo.atom.util.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExportShipmentTractingDto {

  private String actualVessel;

  private String billingParty;

  private String bookingNum;

  private String cha;

  private String cofd;

  private String containerNum;

  private String countPicDate;

  private String customerName;

  private String customsExamDate;

  private String etaPod;

  private String exportRemarks;

  private String forwarder;

  private String gateInDateCfs;

  private String gateInDatePort;

  private String gateOutDateCgs;

  private String incoTerm;

  private String insertDate;

  private String insertUser;

  private String invAmount;

  private String isSyncOtm;

  private String leoDate;

  private String paymetTerms;

  private String piNum;

  private String plannedVessel;

  private String pod;

  private String pol;

  private String postInvNum;

  private String preInvNum;

  private String sapInvoice;

  private String shipmentId;

  private String shipmentOnBordDate;

  private String shippingBill;

  private String shippingBillDate;

  private String shippingLine;

  private String sourceLoc;

  private String stuffingDate;

  private String updateDate;

  private String updateUser;

  private String vesslDepartPolDate;

  private int index;

  private  int pageLength = Constants.PAGE_LIMIT;

  public ExportShipmentTractingDto(ExportShipment exportShipment) {
    this.actualVessel = exportShipment.getActualVessel();
    this.billingParty = exportShipment.getBillingParty();
    this.bookingNum = exportShipment.getBookingNum();
    this.cha = exportShipment.getCha();
    this.cofd = exportShipment.getCofd();
    this.containerNum = exportShipment.getContainerNum();
    this.countPicDate = exportShipment.getCountPicDate() != null ?  DateUtils.formatDate(exportShipment.getCountPicDate(), Constants.DATE_TIME_FORMAT) : null;
    this.customerName = exportShipment.getCustomerName();
    this.customsExamDate = exportShipment.getCustomsExamDate() != null ?  DateUtils.formatDate(exportShipment.getCustomsExamDate(), Constants.DATE_TIME_FORMAT) : null;
    this.etaPod = exportShipment.getEtaPod() != null ?  DateUtils.formatDate(exportShipment.getEtaPod(), Constants.DATE_TIME_FORMAT) : null;
    this.exportRemarks = exportShipment.getExportRemarks();
    this.forwarder = exportShipment.getForwarder();
    this.gateInDateCfs = exportShipment.getGateInDateCfs()!= null ?  DateUtils.formatDate(exportShipment.getGateInDateCfs(), Constants.DATE_TIME_FORMAT) : null;
    this.gateInDatePort = exportShipment.getGateInDatePort() != null ?  DateUtils.formatDate(exportShipment.getGateInDatePort(), Constants.DATE_TIME_FORMAT) : null;
    this.gateOutDateCgs = exportShipment.getGateOutDateCgs() != null ?  DateUtils.formatDate(exportShipment.getGateOutDateCgs(), Constants.DATE_TIME_FORMAT) : null;
    this.incoTerm = exportShipment.getIncoTerm();
    this.insertDate = exportShipment.getInsertDate() != null ?  DateUtils.formatDate(exportShipment.getInsertDate(), Constants.DATE_TIME_FORMAT) : null;
    this.insertUser = exportShipment.getInsertUser();
    this.invAmount = exportShipment.getInvAmount();
    this.isSyncOtm = exportShipment.getIsSyncOtm();
    this.leoDate = exportShipment.getLeoDate() != null ?  DateUtils.formatDate(exportShipment.getLeoDate(), Constants.DATE_TIME_FORMAT) : null;;
    this.paymetTerms = exportShipment.getPaymetTerms();
    this.piNum = exportShipment.getPiNum();
    this.plannedVessel = exportShipment.getPlannedVessel();
    this.pod = exportShipment.getPod();
    this.pol = exportShipment.getPol();
    this.postInvNum = exportShipment.getPostInvNum();
    this.preInvNum = exportShipment.getPreInvNum();
    this.sapInvoice = exportShipment.getSapInvoice();
    this.shipmentId = exportShipment.getShipmentId();
    this.shipmentOnBordDate = exportShipment.getShipmentOnBordDate() != null ?  DateUtils.formatDate(exportShipment.getShipmentOnBordDate(), Constants.DATE_TIME_FORMAT) : null;
    this.shippingBill = exportShipment.getShippingBill();
    this.shippingBillDate = exportShipment.getShippingBillDate()!= null ?  DateUtils.formatDate(exportShipment.getShippingBillDate(), Constants.DATE_TIME_FORMAT) : null;;
    this.shippingLine = exportShipment.getShippingLine();
    this.sourceLoc = exportShipment.getSourceLoc();
    this.stuffingDate = exportShipment.getStuffingDate() != null ?  DateUtils.formatDate(exportShipment.getStuffingDate(), Constants.DATE_TIME_FORMAT) : null;;
    this.updateDate = exportShipment.getUpdateDate() != null ?  DateUtils.formatDate(exportShipment.getUpdateDate(), Constants.DATE_TIME_FORMAT) : null;;
    this.updateUser = exportShipment.getUpdateUser();
    this.vesslDepartPolDate = exportShipment.getVesslDepartPolDate()!= null ?  DateUtils.formatDate(exportShipment.getVesslDepartPolDate(), Constants.DATE_TIME_FORMAT) : null;;
  }

  public boolean isFilterOfExportShipments(){
    return !StringUtils.isEmpty(this.getShipmentId()) || !StringUtils.isEmpty(this.getPol()) || !StringUtils.isEmpty(this.getPod()) || !StringUtils.isEmpty(this.getContainerNum()) ||
        !StringUtils.isEmpty(this.getCustomerName()) || !StringUtils.isEmpty(this.getForwarder()) || !StringUtils.isEmpty(this.getCha()) || !StringUtils.isEmpty(this.getPiNum()) ||
        !StringUtils.isEmpty(this.getSapInvoice()) || !StringUtils.isEmpty(this.getBookingNum()) || !StringUtils.isEmpty(this.getSourceLoc());
  }
}
