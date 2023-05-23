package com.api.apollo.atom.entity.master;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.api.apollo.atom.constant.Constants.BayStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "MT_LOCATION_BAY")
@Getter
@Setter
@NoArgsConstructor
public class MTLocationBay implements Serializable {
	 
	private static final long serialVersionUID = 7400485471623895736L;

	@Id
    @Column(name="LOCATION_ID")
    private String locationId;
	
	@ManyToOne
	@PrimaryKeyJoinColumn(name = "LOCATION_ID")
	private MTLocation location;

	@Column(name = "BAY_ID")
	private String bayId;

	@Column(name = "BAY_DESC")
	private String bayDescription;

//	@Column(name = "BAY_SITE")
//	private String baySite;

	@Column(name = "BAY_STATUS")
	@Enumerated(EnumType.STRING)
	private BayStatus bayStatus;

	@Column(name = "INSERT_USER")
	private String insertUser;

	@Column(name = "INSERT_DATE")
	private Date insertDate = new Date();

	@Column(name = "UPDATE_USER")
	private String updateUser;

	@Column(name = "UPDATE_DATE")
	private Date updateDate;

	@Column(name = "LB_ID")
	private Integer lbId;
}
