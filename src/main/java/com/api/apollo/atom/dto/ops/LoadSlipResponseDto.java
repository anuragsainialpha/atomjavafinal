package com.api.apollo.atom.dto.ops;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoadSlipResponseDto {

  private String loadSlipId;

  private String shipmentId;

  private Object loadslip;

  private String lsPrintDate;

  private String variant1;

  private  String variant2;

  private String shipmentFreightFlag;

  private String actucalTruckType;

  private List<String> errorMsgs = new ArrayList<>();

  private LoadslipDraftDto loadslipDraftDto;

  private String loadslipType;


  public LoadSlipResponseDto(String loadSlipId, byte[] loadslip) {
    this.loadSlipId = loadSlipId;
    this.loadslip = loadslip;
  }

  public LoadSlipResponseDto(String loadSlipId, String shipmentId, byte[] loadslip, String lsPrintDate, List<String> errorMsgs, LoadslipDraftDto loadslipDraftDto,String loadslipType) {
    this.loadSlipId = loadSlipId;
    this.shipmentId = shipmentId;
    this.loadslip = loadslip;
    this.lsPrintDate = lsPrintDate;
    this.errorMsgs = errorMsgs;
    this.loadslipDraftDto = loadslipDraftDto;
    this.loadslipType = loadslipType;
  }

  public LoadSlipResponseDto(String loadSlipId, String shipmentId, ByteArrayOutputStream loadslip, String lsPrintDate, String variant1, String variant2, String shipmentFreightFlag, String actucalTruckType, List<String> errorMsgs, LoadslipDraftDto loadslipDraftDto) {
    this.loadSlipId = loadSlipId;
    this.shipmentId = shipmentId;
    this.loadslip = loadslip;
    this.lsPrintDate = lsPrintDate;
    this.variant1 = variant1;
    this.variant2 = variant2;
    this.shipmentFreightFlag = shipmentFreightFlag;
    this.actucalTruckType = actucalTruckType;
    this.errorMsgs = errorMsgs;
    this.loadslipDraftDto = loadslipDraftDto;
  }

  public LoadSlipResponseDto(List<String> errorMesssages) {
    this.errorMsgs = errorMesssages;
  }
  public LoadSlipResponseDto(String errorMsg) {
    this.errorMsgs.add(errorMsg);
  }
}
