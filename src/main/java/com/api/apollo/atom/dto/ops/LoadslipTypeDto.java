package com.api.apollo.atom.dto.ops;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoadslipTypeDto {

  private String loadlipId;

  private  String type;


}
