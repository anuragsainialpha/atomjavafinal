package com.api.apollo.atom.entity.master;

import com.api.apollo.atom.constant.LocationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "MT_LOCATION")
@Getter
@Setter
@NoArgsConstructor
public class MTLocation {

	@Id
	@Column(name = "LOCATION_ID")
	private String id;

	@Column(name = "LOCATION_DESC")
	private String description;

//	@Column(name = "LOCATION_TYPE")
//	@Enumerated(EnumType.STRING)
//	private LocationType locationType;

	@Column(name = "LOCATION_TYPE")
	private String type;

	@Column(name = "LOCATION_ADDRESS")
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

	@Column(name = "INSERT_USER")
	private String insertUser;

	@Column(name = "INSERT_DATE")
	private Date insertDate;

	@Column(name = "UPDATE_USER")
	private String updateUser;

	@Column(name = "UPDATE_DATE")
	private Date updateDate;

	@Column(name = "STATE_CODE")
	private Integer stateCode;

	@Column(name = "GST_NO")
	private String gstNum;

	@Column(name = "GST_STATE")
	private String gstState;

	@Column(name = "PAN_NO")
	private String panNum;

	@Column(name = "LOCATION_CLASS")
	private String locationClass;

	@Column(name = "LINKED_PLANT")
	private String linkedPlant;

	@Column(name = "FT_ACCESS_KEY")
	private String ftAccessKey;

	@Column(name = "EMAIL_ID")
	private String emailID;


	public MTLocation(String id, String description) {
		super();
		this.id = id;
		this.description = description;
	}

	public MTLocation(String id) {
		super();
		this.id = id;
	}

	public MTLocation(String id, String gstNum, String panNum) {
		this.id = id;
		this.gstNum = gstNum;
		this.panNum = panNum;
	}
}
