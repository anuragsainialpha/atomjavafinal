package com.api.apollo.atom.entity.master;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CT_COUNTRY")
@Getter
@NoArgsConstructor
public class CTCountry {

  @Id
  @Column(name = "COUNTRY_CODE")
  private String countryCode;

  @Column(name = "COUNTRY_NAME")
  private String countryName;

  @Column(name = "IS_EXPORT")
  private String isExport;

}
