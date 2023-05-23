package com.api.apollo.atom.dto.ops;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LoadslipInvoiceData {

  private List<InvoiceDataDto> invoiceDataDtos;

  private LoadslipDraftDto loadslipDraftDto;

}
