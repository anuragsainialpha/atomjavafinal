package com.api.apollo.atom.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "UM_USER_ASSOCIATION", schema = "ATOM")
public class UmUserAssociation {

  @EmbeddedId
  @AttributeOverrides({
      @AttributeOverride(name = "userId", column = @Column(name = "USER_ID", nullable = false)),
      @AttributeOverride(name = "associationIdentifier", column = @Column(name = "ASSOCIATION_IDENTIFIER")),
      @AttributeOverride(name = "associationValue", column = @Column(name = "ASSOCIATION_VALUE"))})
  private UmUserAssociationId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "USER_ID", nullable = false, insertable = false, updatable = false)
  private ApplicationUser umUser;



}


