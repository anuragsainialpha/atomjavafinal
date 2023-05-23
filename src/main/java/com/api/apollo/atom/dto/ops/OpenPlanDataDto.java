package com.api.apollo.atom.dto.ops;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class OpenPlanDataDto {
  private Double tyreAvailQtySum;

  private Double tyreTTESum;

  private Double tyreTruckCount;

  private int pageLength ;
  private Double totalApprovedQty;

  private Double totalReservedQty;


  public OpenPlanDataDto(Double tyreAvailQtySum, Double tyreTTESum, Double tyreTruckCount, int pageLength, Double totalApprovedQty, Double totalReservedQty) {
    this.tyreAvailQtySum = tyreAvailQtySum;
    this.tyreTTESum = tyreTTESum;
    this.tyreTruckCount = tyreTruckCount;
    this.pageLength = pageLength;
    this.totalApprovedQty = totalApprovedQty;
    this.totalReservedQty = totalReservedQty;
  }
}
