package com.api.apollo.atom.dto.ops;

import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoadSlipPlansDto {

  private List<String> shipmentToLocs = new ArrayList<>();

  private List<Map<String,String>> gatedInTrucks = new ArrayList<>();

  private List<Map<String, Object>> loadSlipPlans = new ArrayList<>();

  private Map<String, String> marketSegmentMap = new HashMap<>();

  private Set<String> itemCategories = new HashSet<>();

  private String city;

}
