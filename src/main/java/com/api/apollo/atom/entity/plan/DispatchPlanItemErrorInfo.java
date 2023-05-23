package com.api.apollo.atom.entity.plan;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.api.apollo.atom.constant.Constants.ErrorCode;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DISPATCH_PLAN_T_ERROR")
@Getter
@Setter
public class DispatchPlanItemErrorInfo {

	@Id
	@Column(name = "ERROR_REC_ID")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/*@JoinColumn(name = "DISPATCH_PLAN_ID")
	@ManyToOne
	private DispatchPlan dispatchPlan;*/
	@Column(name = "DISPATCH_PLAN_ID")
	private Long planId;

	@Column(name = "ERROR_DESC")
	private String description;

	@Column(name = "ERROR_CODE")
	@Enumerated(EnumType.STRING)
	private ErrorCode code;

	@Column(name = "LINE_NUM")
	private Integer lineNumber;

	/*@ManyToOne
	@JoinColumn(name = "INSERT_USER")
	private ApplicationUser insertUser;*/
	@Column(name = "INSERT_USER")
	private String insertUser;

	@Column(name = "INSERT_DATE")
	private Date insertDate = new Date();

}
