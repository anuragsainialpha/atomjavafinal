package com.api.apollo.atom.entity.master;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "UM_USER_ASSOCIATION", schema = "ATOM")
public class UmUserAssociationEntity {

    @Id
    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "ASSOCIATION_IDENTIFIER")
    private String associationIdentifier;

    @Column(name = "ASSOCIATION_VALUE")
    private String associationValue;

    @Column(name = "INSERT_USER")
    private String insertUser;

    @Column(name = "INSERT_DATE")
    private Date insertDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    private Date updateDate;

    @Column(name = "UA_ID")
    private Integer uaId;

}
