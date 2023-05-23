package com.api.apollo.atom.dto.ops;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadSlipUtilizationDto {

  // Truck Weight Utilization Values
  private Object truckWeightCapacity; // PASSING_WEIGHT from MT_TRUCK if truck available else GROSS_WT  from MT_TRUCK_TYPE
 // private double itemWeight; // Item Weight from DISPATCH_PLAN_BOM table

  // Truck TTE Utilization Values
  private Object tteCapacity; // TTE_CAPACITY from MT_TRUCK if truck available else TTE_CAPACITY  from MT_TRUCK_TYPE
//  private double tte; // TTE from DISPATCH_PLAN table

  // Truck Volume Utilization Values
  private Object truckVolumeCapacity; //  GROSS_VOL from MT_TRUCK if truck available else  GROSS_VOL  from MT_TRUCK_TYPE
//  private double itemVolume; // Item volume from DISPATCH_PLAN_BOM table

  private Object shipmentTotalTTEUtil;

  private Object shipmentTotalWtUtil;

  private Object shipmentTotalVolUtil;

  private Object truckType;

  private Object truckNumber;

  private Object containerNum;

  private Object actualTruckType;

  private Object variant1;

  private Object destLoc;

  private Object servprov;

  private String freightAvailable;

  private String shipToDestination;

  private String truckIndentCategory;

  private String destDesc;

  private String indentCategory;

  private String city;


}
