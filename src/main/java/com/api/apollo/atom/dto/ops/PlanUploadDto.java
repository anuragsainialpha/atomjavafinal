package com.api.apollo.atom.dto.ops;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PlanUploadDto {

  private String sourceLoc;

  private Double uploadedBy3PL;

  private Double uploadedByOthers;

  private Double total;

  private Double percentage3PL;

  private Double percentageOthers;

  private Double cummUploadedBy3PL;

  private Double cummUploadedByOthers;

  private Double cummTotal;

  private Double cummPercentage3PL;

  private Double cummPercentageOthers;
}
