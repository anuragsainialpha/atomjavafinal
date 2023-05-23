package com.api.apollo.atom.entity.master;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "FREIGHT_TEMP", schema = "ATOM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FreightTemp {

  @Id
  @Column(name = "ID", nullable = false, precision = 0)
  private Integer id;

  @Column(name = "TRANSPORTER_SAP_CODE")
  private String transporterSapCode;

  @Column(name = "SERVPROV")
  private String servprov;

  @Column(name = "SOURCE_LOC")
  private String sourceLoc;

  @Column(name = "SOURCE_DESC")
  private String sourceDesc;

  @Column(name = "DEST_LOC")
  private String destLoc;

  @Column(name = "DEST_DESC")
  private String destDesc;

  @Column(name = "LANE_CODE")
  private String laneCode;

  @Column(name = "TRUCK_TYPE")
  private String truckType;

  @Column(name = "CONDITION1")
  private String condition1;

  @Column(name = "CONDITION2")
  private String condition2;

  @Column(name = "EFFECTIVE_DATE")
  private Date effectiveDate;

  @Column(name = "EXPIRY_DATE")
  private Date expiryDate;

  @Column(name = "TT_DAYS", precision = 0)
  private Double ttDays;

  @Column(name = "BASE_FREIGHT", precision = 0)
  private Double baseFreight;

  @Column(name = "BASE_FREIGHT_UOM")
  private String baseFreightUom;

  @Column(name = "BASIS")
  private String basis;

  @Column(name = "MIN_VALUE")
  private Double minValue;

  @Column(name = "MIN_VALUE_UOM")
  private String minValueUom;

  @Column(name = "APPROVAL1_USER")
  private String approval1User;

  @Column(name = "APPROVAL1_DATE")
  private Date approval1Date;

  @Column(name = "APPROVAL2_USER")
  private String approval2User;

  @Column(name = "APPROVAL2_DATE")
    private Date approval2Date;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "ERROR_MESSAGE")
  private String errorMessage;

  @Column(name = "INSERT_USER")
  private String insertUser;

  @Column(name = "INSERT_DATE")
  private Date insertDate;

  @Column(name = "UPDATE_USER")
  private String updateUser;

  @Column(name = "UPDATE_DATE")
  private Date updateDate;

  @Column(name = "TRANSPORT_MODE")
  private String transportMode;

  @Column(name = "REMARKS")
  private String remarks;
/*
  @Column(name = "PREVIOUS_RATE")
  private Integer previousRate;

  @Column(name = "DIFF")
  private String diff;

  @Column(name = "PERCENTILE")
  private String percentile;

  @Column(name = "RATE_RECORD_ID")
  private String rateRecordId;

*/
}