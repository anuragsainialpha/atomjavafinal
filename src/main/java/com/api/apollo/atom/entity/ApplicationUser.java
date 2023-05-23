package com.api.apollo.atom.entity;

import java.util.Date;

import javax.persistence.*;

import com.api.apollo.atom.constant.UserActiveType;
import com.api.apollo.atom.constant.UserRole;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "UM_USER")
@Getter
@Setter
public class ApplicationUser {

	@Id
	@Column(name = "USER_ID")
	private String userId;

	@Column(name = "STATUS")
	@Enumerated(EnumType.STRING)
	private UserActiveType activeType;

	@Column(name = "USER_ROLE_ID")
	@Enumerated(EnumType.STRING)
	private UserRole role;

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
	private String email;
	
	@Column(name = "LAST_LOGIN_DATE")
	private Date lastLoginDate;

	@Transient
	private  Boolean isExtWarehouse = false;

	public ApplicationUser() {}
	public ApplicationUser(String userId,String password, UserRole role, String firstName, String lastName, String email) {
		super();
		this.userId = userId;
		this.password = password;
		this.role = role;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
	}
	
	public ApplicationUser(String userId) {
		super();
		this.userId = userId;
	}
	
	
	

}
