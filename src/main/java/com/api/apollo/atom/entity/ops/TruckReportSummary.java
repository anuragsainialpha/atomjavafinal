package com.api.apollo.atom.entity.ops;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TRUCK_REPORTING_SUMMARY")
@Getter
@Setter
@NoArgsConstructor
public class TruckReportSummary {

	@Id
	@Column(name = "REPORTING_LOC")
	private String location;

	@Column(name = "TRUCKS_REPORTED")
	private Integer reportedCount;

	@Column(name = "TRUCKS_GATEDIN")
	private Integer gatedInCount;

	@Column(name = "EXCESS_WAITING_REP")
	private Integer excessWaitingCount;

	@Column(name = "EXCESS_WAITING_LOC")
	private Integer excessWaitingLocCount;

	@Column(name = "INSERT_USER")
	private String insertUser;

	@Column(name = "UPDATE_USER")
	private String updateUser;

	@Column(name = "INSERT_DATE")
	private Date insertDate = new Date();

	@Column(name = "UPDATE_DATE")
	private Date updateDate;

	@Column(name = "TRUCKS_LOADINGIN")
	private String trucksLoadingIn;

}
