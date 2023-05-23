package com.api.apollo.atom.entity.master;


import com.api.apollo.atom.constant.LocationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "MT_CUSTOMER")
@Getter
@Setter
@NoArgsConstructor
public class MTCustomer {

	@Id
	@Column(name = "CUST_ID")
	private String id;

	@Column(name = "CUST_NAME")
	private String customerName;

//	Commented this because the values in DB were not matching with any of the ENUM values
/*	@Column(name = "CUST_ACCT_GRP")
	private LocationType custAcctGRP;*/

	@Column(name = "CUST_ACCT_GRP")
	private String custAcctGRP;

	@Column(name = "CUST_ADDRESS")
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

	@Column(name = "LAT")
	private Double latitude;

	@Column(name = "LON")
	private Double longitude;
	
	@Column(name="DELIVERY_TERMS")
	private String deliveryTerms;

	@Column(name = "INSERT_USER")
	private String insertUser;

	@Column(name = "INSERT_DATE")
	private Date insertDate;

	@Column(name = "UPDATE_USER")
	private String updateUser;

	@Column(name = "UPDATE_DATE")
	private Date updateDate;
	
	@Column(name = "CUST_TYPE")
	private String customerType;

	@Column(name = "GST_NO")
	private String gstNum;

	@Column(name = "PAN_NO")
	private String panNum;

	@Column(name = "STATE_CODE")
	private String stateCode;

	@Column(name = "GST_STATE")
	private String gstCode;

	public MTCustomer(String id, String name) {
		super();
		this.id = id;
		this.customerName = name;
	}

	public MTCustomer(String id) {
		super();
		this.id = id;
	}

	public MTCustomer(String id, String gstNum, String panNum) {
		this.id = id;
		this.gstNum = gstNum;
		this.panNum = panNum;
	}
}
