package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.master.MTCustomer;
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
public class MTCustomerResponseDto {

  private Long total;

  private List<MTCustomerDto> mtCustomerDtos = new ArrayList<>();

  public MTCustomerResponseDto(Page<MTCustomer> mtCustomers){
    this.total = mtCustomers.getTotalElements();
    this.mtCustomerDtos = mtCustomers.stream().parallel().map(MTCustomerDto::new).collect(Collectors.toList());
  }

}
