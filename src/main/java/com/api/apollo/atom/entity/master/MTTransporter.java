package com.api.apollo.atom.entity.master;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "MT_TRANSPORTER")
@Getter
@Setter
@NoArgsConstructor
public class MTTransporter {

	@Id
	@Column(name = "TRANSPORTER_ID")
	private String id;

	@Column(name = "TRANSPORTER_DESC", columnDefinition = "TEXT")
	private String description;

	@Column(name = "SERVPROV")
	private String servprov;

	@Column(name = "TRANSPORTER_ADDRESS", columnDefinition = "TEXT")
	private String address;

	@Column(name = "CITY")
	private String city;

	@Column(name = "STATE")
	private String state;

	@Column(name = "POSTAL_CODE")
	private String postalCode;

	@Column(name = "COUNTRY")
	private String country;

	@Column(name = "IS_ACTIVE")
	private String isActive;

	@Column(name = "INSERT_USER")
	private String insertUser;

	@Column(name = "INSERT_DATE")
	private Date insertDate;

	@Column(name = "UPDATE_USER")
	private String updateUser;

	@Column(name = "UPDATE_DATE")
	private Date updateDate;

	@Column(name = "INDUSTRY_KEY")
	private String industryKey;

	@Column(name = "STATE_CODE")
	private String stateCode;

	@Column(name = "GST_NO")
	private String gstNo;

	@Column(name = "GST_STATE")
	private String gstState;

	@Column(name = "PAN_NO")
	private String panNo;

	public MTTransporter(String transporter){
		this.servprov = transporter;
	}

}
