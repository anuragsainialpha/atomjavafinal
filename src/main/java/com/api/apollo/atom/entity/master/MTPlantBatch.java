package com.api.apollo.atom.entity.master;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Setter
@Getter
public class MTPlantBatch {

  @Id
  @Column(name = "PLANT_ID")
  private String locationId;

  @Column(name = "BATCH_CODE")
  private String batchCodePrefix;

  @Column(name = "ITEM_CLASSIFICATION")
  private String itemClassification;
}
