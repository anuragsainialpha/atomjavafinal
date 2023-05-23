package com.api.apollo.atom.entity.master;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "MT_TRUCK_DEDICATED")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MTTruckDedicated {
/*

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "ISEQ$$_133823")
  @SequenceGenerator(name = "ISEQ$$_133823", sequenceName = "ISEQ$$_133823", allocationSize = 1)
  private Long id;
*/


  @Id
  @Column(name="ID")
//  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(name = "SERPVORV")
  private String servProv;

  @Column(name = "SOURCE_LOC")
  private String sourceLoc;

  @Column(name = "DEST_LOC")
  private String destLoc;

  @Column(name = "SOURCE_DESC")
  private String sourceDesc;

  @Column(name = "DEST_DESC")
  private String destDesc;

  @Column(name = "TRUCK_TYPE")
  private String truckType;

  @Column(name = "TRUCK_NUMBER")
  private String truckNumber;

  @Column(name = "EXPIRY_DATE")
  private Date expiryDate;

  @Column(name = "INSERT_USER")
  private String insertUser;

  @Column(name = "INSERT_DATE")
  private Date insertDate;

  @Column(name = "UPDATE_USER")
  private String updateUser;

  @Column(name = "UPDATE_DATE")
  private Date updateDate;
}
