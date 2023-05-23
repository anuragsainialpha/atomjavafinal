package com.api.apollo.atom.entity.master;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CT_SCALE_INVOICE_WT_DIFF")
@Getter
@Setter
@NoArgsConstructor
public class CTScaleInvoiceWtDiff {

  @Id
  @Column(name = "LOCATION_ID")
  private String locationId;

  @Column(name = "WEIGHT_DIFF")
  private String weightDiff;

}
