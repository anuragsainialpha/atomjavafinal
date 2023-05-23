package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.entity.ops.LoadslipDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StosoItemBean {

  private String invoiceNum;

  private String loadslipId;

  private String itemId;

  private String batchCode;

  private int quantity;

  private int detailQty;

  List<LoadslipDetail> loadslipDetails;
}
