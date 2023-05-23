package com.api.apollo.atom.dto.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class MasterTrucktypeDto {

  private String value;
  private Double description;


  public MasterTrucktypeDto(String value, double description) {
    this.value = value;
    this.description = description;
  }
}
