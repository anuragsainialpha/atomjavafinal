package com.api.apollo.atom.dto.ops;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class PlansDataDto {

  private Double totalPlanQty;

  private Double totalAvailQty;

  private Double totalTotAvailQty;

  private Double totalReservedQty;

  private Double total;

  private Double totalTTE;

  private Double cummTotalPlanQty;

  private Double cummTotalAvailQty;

  private Double cummTotalTotAvailQty;

  private Double cummTotalReservedQty;

  private Double cummTotal;

  private Double cummTotalTTE;

  private  String itemCategory;

  private Double truckCount;

  private Double cummTruckCount;

  private Double totalDispatchedQty;

  private Double cummTotalDispatchedQty;

}
