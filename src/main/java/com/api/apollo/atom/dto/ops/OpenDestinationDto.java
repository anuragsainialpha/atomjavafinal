package com.api.apollo.atom.dto.ops;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class OpenDestinationDto {

  List<Map<String, Object>> destinationList = new ArrayList<>();

  List<Map<String, Object>> descriptionList = new ArrayList<>();

  public OpenDestinationDto(List<Map<String, Object>> destinationList, List<Map<String, Object>> descriptionList) {
    this.descriptionList = descriptionList;
    this.destinationList = destinationList;
  }
}
