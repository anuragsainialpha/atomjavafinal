package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.dto.core.MasterTrucktypeDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TruckTypesWithFreightDto {

  private String name;

  private List<MasterTrucktypeDto> mtTruckTypeInfos = new ArrayList<>();
}
