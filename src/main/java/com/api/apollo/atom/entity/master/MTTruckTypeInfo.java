package com.api.apollo.atom.entity.master;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "MT_TRUCK_TYPE")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MTTruckTypeInfo {

  @Id
  @Column(name = "TT_ID")
  private Integer ttId;

  @Column(name = "TRUCK_TYPE")
  private String type;

  @Column(name = "LOAD_FACTOR")
  private Double loadFactor;

  @Column(name = "TRUCK_DESC")
  private String truckDescription;

  @Column(name = "TTE_CAPACITY")
  private Double tteCapacity;

  @Column(name = "GROSS_WT")
  private Integer grossWt;

  @Column(name = "GROSS_WT_UOM")
  private String grossWtUom;

  @Column(name = "GROSS_VOL")
  private Integer grossVol;

  @Column(name = "GROSS_VOL_UOM")
  private String grossVolUom;

  @Column(name = "VARIANT1")
  private String variant1;

  @Column(name = "VARIANT2")
  private String variant2;



  public MTTruckTypeInfo(String truckType, Double tteCapacity) {
    super();
    this.type = truckType;
    this.tteCapacity = tteCapacity;
  }

  public MTTruckTypeInfo(String truckType, Double tteCapacity, Double loadFactor) {
    this(truckType, tteCapacity);
    this.loadFactor = loadFactor;
  }

  public MTTruckTypeInfo(MTTruckTypeInfo t) {
    this.type = t.type;
    this.tteCapacity = t.getTteCapacity();
    this.grossWt = t.getGrossWt();
    this.grossVol = t.getGrossVol();
    this.loadFactor = t.getLoadFactor();
    this.truckDescription = t.getTruckDescription();
    this.grossWtUom = t.getGrossWtUom();
    this.grossVolUom = t.getGrossVolUom();
    this.variant1 = t.getVariant1();
    this.variant2 = t.getVariant2();
  }

  public MTTruckTypeInfo(String variant1, String variant2) {
    this.variant1 = variant1;
    this.variant2 = variant2;
  }
}
