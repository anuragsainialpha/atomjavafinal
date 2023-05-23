package com.api.apollo.atom.dto.ops;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PlanAgeing {

    public Double lessThan2;

    public Double lessThan2Count;

    public Double lessThan7;

    public Double lessThan7Count;

    public Double greaterThan7;

    public Double greaterThan7Count;

    public Double cummLessThan2;

    public Double cummLessThan2Count;

    public Double cummLessThan7;

    public Double cummLessThan7Count;

    public Double cummGreaterThan7;

    public Double cummGreaterThan7Count;

    public PlanAgeing() {}

    public PlanAgeing(Double lessThan2, Double lessThan2Count, Double lessThan7, Double lessThan7Count, Double greaterThan7, Double greaterThan7Count) {
      this.lessThan2 = lessThan2;
      this.lessThan2Count = lessThan2Count;
      this.lessThan7 = lessThan7;
      this.lessThan7Count = lessThan7Count;
      this.greaterThan7 = greaterThan7;
      this.greaterThan7Count = greaterThan7Count;
    }
}
