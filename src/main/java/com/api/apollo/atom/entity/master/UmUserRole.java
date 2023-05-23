package com.api.apollo.atom.entity.master;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "UM-USER-ROLE")
@Getter
@Setter
@NoArgsConstructor
public class UmUserRole {

//
//    @Id
//    @Column(name = "ID", nullable = false, precision = 0)
//    private Double id;

    @Id
    @Column(name = "USER_ROLE_ID")
    private String userRoleId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "INSERT_USER")
    private String insertUser;

    @Column(name = "INSERT_DATE")
    @JsonFormat(pattern="dd-MMM-yyyy",timezone = "IST")
    private Date insertDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    private Date updateDate;

}