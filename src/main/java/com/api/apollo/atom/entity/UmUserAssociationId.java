package com.api.apollo.atom.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UmUserAssociationId implements java.io.Serializable {

  // Fields
  @Column(name = "USER_ID", nullable = false)
  private String userId;

  @Column(name = "ASSOCIATION_IDENTIFIER")
  private String associationIdentifier;

  @Column(name = "ASSOCIATION_VALUE")
  private String associationValue;

}
