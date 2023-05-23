package com.api.apollo.atom.dto.ops;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class IndentStatusDto {
  private Double totalNetIndented;

  private Double totalReported;

  private Double totalRejected;

  private Double totalNetBalance;

  private int total;

  private Double cummTotalNetIndented;

  private Double cummTotalReported;

  private Double cummTotalRejected;

  private Double cummTotalNetBalance;

  private int cummTotal;

  private String itemCategory;

  private String cummItemCategory;

  private Double totalNetPlaced;

  private Double placementPercentage;

  private Double cummTotalNetPlaced;

  private Double cummPlacementPercentage;

  public IndentStatusDto(Double totalIndented, Double totalReported, Double totalRejected, Double totalNetBalance, int pageLength) {
    this.totalNetIndented = totalIndented;
    this.totalReported = totalReported;
    this.totalRejected = totalRejected;
    this.totalNetBalance = totalNetBalance;
    this.total = pageLength;
  }
}
