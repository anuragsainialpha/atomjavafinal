package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.ops.DelInvHeader;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
public class LoadslipDraftDto {

  private String shipmentID;

  private String action;

  private String loadSlipId;

  private String truckNumber;

  private String containerNum;

  private String truckType;

  private String servprov;

  private String destination;

  private String source;

  private String shipTo;

  private String bay;

  private double tteUtil;

  private double weightUtil;

  private double volumeUtil;

  private double shipmentTTEUtil;

  private double shipmentWeightUtil;

  private double shipmentVolumeUtil;

  private int totalTyres;

  private int totalTubes;

  private int totalFlaps;

  private int totalValves;

  private String lsPrintDate;

  private String bayArrivedDate;

  private String loadingStartDate;

  private String loadingEndDate;

  private String confirmedDate;

  private String releasedDate;

  private String sendForBarcodeDate;

  private int totalLoadedQty;

  private double totalTTE;

  private double totalWeight;

  private double totalVolume;

  private String variant1;

  private String variant2;

  private String loadslipCategory;

  private int loadSeq;

  private int dropSeq;

  private String actualTruckType;

  private String freightAvailableflag;

  private  String loadslipType;

  private boolean isMultiStop = false;

  private boolean sostoNumber;

  private  String sostoNumberVal;

  private boolean sapInvoice;

  private int totPctr;

  private int totQty;

  private int otherQty;

  private String comments;

  private Integer ditQty;

  private Integer shortQty;

  private Map<String,Double> splitNumbersMap = new HashMap<>();

  private List<LoadslipDraftDataDto> loadslipDraftDataDtos = new ArrayList<>();

  private Constants.DelInvType type;

  private String weightUom;

  private String  countryName;

  private String lsMarketSegment;

  private String truckIndentCategory;

  private String description;

  private String city;

  private boolean elrFlag;

  private String elrNumber;

  private String elrDate;

  private Boolean isInDraft  = false;
  private List<String> invoiceList = new ArrayList<>();
  private List<DelInvHeader> delInvHeaderList = new ArrayList<>();
  private List<ExportInfoDto> delInvLineList = new ArrayList<>();

  private Set<String> selectedItemCategories = new HashSet<>();


  //Adding additional fields in LoadSlip for Driver Consent Status - Freight Tiger
  private String trackingConsentStatus;
  private String consentPhoneTelecom;
  private String goApprovalReason;

  public LoadslipDraftDto(String loadSlipId, String type) {
    this.loadSlipId = loadSlipId;
    this.loadslipType = type;


  }
}
