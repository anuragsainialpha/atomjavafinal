package com.api.apollo.atom.dto.ops;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class LoadslpEventDto {

  //@NotNull(message = "Load Slip Id can Not Be Empty.")
  private String loadslipID;

  private String eventType;
}
