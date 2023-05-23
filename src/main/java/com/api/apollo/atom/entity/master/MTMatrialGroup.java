package com.api.apollo.atom.entity.master;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "MT_MATERIAL_GROUP")
@Getter
@Setter
public class MTMatrialGroup {


  @Id
  @Column(name = "MATERIAL_GROUP_ID")
  private String id;

  @Column(name = "DESCRIPTION_1")
  private String description1;

  @Column(name = "DESCRIPTION_2")
  private String description2;

  @Column(name = "SCM_GROUP")
  private String scmGroup;



}
