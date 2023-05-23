package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.constant.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ClsDetailsFilterDto {

  private String shipmentId;

  private String sourceLoc;

  private String fromInsertDate;

  private String toInsertDate;

  private String fromShipmentOnboardDate;

  private String toShipmentOnboardDate;

  private String fromGateOutDate;

  private String toGateOutDate;

  private String fromSapInvDate;

  private String toSapInvDate;

  private int index = Constants.PAGE_INDEX;

  private int pageLength = Constants.PAGE_LIMIT;

}
