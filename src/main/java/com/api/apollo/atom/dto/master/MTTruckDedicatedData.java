package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.MTTruckDedicated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
public class MTTruckDedicatedData {

  private List<MTTruckDedicatedDto> mtTruckDedicatedDtos = new ArrayList<>();

  private Long total;

  public MTTruckDedicatedData(Page<MTTruckDedicated> mtTruckDedicatedPage){
    this.mtTruckDedicatedDtos = mtTruckDedicatedPage.stream().parallel().map(MTTruckDedicatedDto::new).collect(Collectors.toList());
    this.total = mtTruckDedicatedPage.getTotalElements();
  }

}
