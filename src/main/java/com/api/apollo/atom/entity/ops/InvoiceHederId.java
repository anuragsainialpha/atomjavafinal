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
public class InvoiceHederId implements Serializable {

  private static final long serialVersionUID = 1L;

  @Column(name = "LOADSLIP_ID",nullable = false)
  private String loadslipId;

  @Column(name = "INVOICE_NUMBER",nullable = false)
  private String invoiceNum;
}
