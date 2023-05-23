package com.api.apollo.atom.entity.ops;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoadslipDetailBomId implements java.io.Serializable {

  private static final long serialVersionUID = 1L;
  // Fields
  @Column(name = "LOADSLIP_ID", nullable = false)
  private String loadslipId;
  @Column(name = "LINE_NO", nullable = false, scale = 1)
  private Double lineNo;
  @Column(name = "ITEM_ID", nullable = false)
  private String itemId;

}
