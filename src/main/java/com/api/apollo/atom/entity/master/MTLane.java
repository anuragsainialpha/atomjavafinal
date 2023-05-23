package com.api.apollo.atom.entity.master;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "MT_LANE"
    , schema = "ATOM")
@Setter
@Getter
public class MTLane {

  @Id
  @Column(name = "LANE", unique = true, nullable = false)
  private String lane;

  @Column(name = "DESCRIPTION")
  private String description;

  @Column(name = "SOURCE_LOCATION_ID", unique = true, nullable = false)
  private String sourceLocationId;

  @Column(name = "DEST_LOCATION_ID", unique = true, nullable = false)
  private String destLocationId;

  @Column(name = "DISTANCE", precision = 0)
  private Double distance;

  @Column(name = "UOM")
  private String uom;

  @Column(name = "INSERT_USER")
  private String insertUser;

  @Column(name = "UPDATE_USER")
  private String updateUser;

  @Column(name = "INSERT_DATE")
  private Date insertDate;

  @Column(name = "UPDATE_DATE")
  private Date updateDate;

}
