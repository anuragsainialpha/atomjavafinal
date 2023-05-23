package com.api.apollo.atom.entity.master;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CT_REJECTION_CODE")
@Getter
@Setter
@NoArgsConstructor
public class CTRejection {

  @Id
  @Column(name = "REJECTION_CODE")
  private String rejectionCode;

  @Column(name = "REJECTION_DESC")
  private String rejectionDesc;
}
