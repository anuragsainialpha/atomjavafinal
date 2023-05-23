package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.constant.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LsUtilizationDto {

  private String trucktype;

  private String actucaltrucktype;

  private String variant1;

  private String shipmentId;

  private String truckNumber;

  private String souceLoc;

  private String destLoc;

  private Double freight;

  private String trasporter;

  private String loadSlipId;

  private String date;

  private Constants.DelInvType type;

  private String label;

//  For LS on Same Truck
  private String lsDropDestLoc;

  public LsUtilizationDto(String trasporter, Double freight, Map<Double, String> label) {
    this.freight = freight;
    this.trasporter = trasporter;
    this.label = label.get(this.freight);
  }
}
