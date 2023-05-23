package com.api.apollo.atom.dto.ops;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TruckInventoryDto {

  private String indentId;
  private String gateControlCode;
  private String truckNumber;
  private String truckType;
  private String driverName;
  private String driverContact;
  private String driverLicense;
  private String containerNum;
  private String destDesc;
  private String destCountryName;
  private String trackingConsentStatus;
  private String consentPhoneTelecom;
  //truck report flags
  private boolean isPuc = Boolean.TRUE;
  private boolean isInsurance = Boolean.TRUE;
  private boolean isSeatBelt = Boolean.TRUE;
  private boolean isFirstAid = Boolean.TRUE;
  private boolean  isFireExtenguisher = Boolean.TRUE;
  private boolean isEmergencyCard = Boolean.TRUE;
  private boolean isSparKArrestor = Boolean.TRUE;
  private boolean isFitnessCert = Boolean.TRUE;
  private Double truckCapacity;
  private Double truckGrossVehicleWt;
  private Double truckUnladenWt;
  private String bsNorms;
  private String fuelType;
  private String goApprovalReason;
  private String GOApprovedBy;
  private String GOApprovalDate;




}

