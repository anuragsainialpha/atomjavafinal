package com.api.apollo.atom.entity.master;

import com.api.apollo.atom.constant.UserActiveType;
import com.api.apollo.atom.constant.UserRole;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "UM_USER")
@Getter
@Setter
public class UserEntity {

    @Id
    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "USER_ROLE_ID")
    private String roleId;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "INSERT_USER")
    private String insertUser;

    @Column(name = "INSERT_DATE")
    private Date insertDate = new Date();

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    private Date updateDate;

    @Column(name = "PLANT_CODE")
    private String plantCode;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "EMAIL_ID")
    private String emailId;

    @Column(name = "LAST_LOGIN_DATE")
    private Date lastLoginDate;




}
