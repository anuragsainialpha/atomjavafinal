package com.api.apollo.atom.entity.ops;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoadslipInvLineId implements Serializable {

  @Column(name = "LOADSLIP_ID")
  private String loadslipId;

  @Column(name = "INVOICE_NUMBER")
  private String invoiceNum;

  @Column(name = "LINE_NO")
  private Integer lineNum;

  @Column(name = "SAP_LINE_NO")
  private Integer sapLineNum;

  @Column(name = "ITEM_ID")
  private String itemId;
}
