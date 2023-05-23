package com.api.apollo.atom.entity.plan;

import lombok.Getter;
import lombok.Setter;


import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "DISPATCH_PLAN_T")
@Getter
@Setter
public class DispatchPlanItemTempInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	//@JoinColumn(name = "DISPATCH_PLAN_ID")
	//@ManyToOne
	//private DispatchPlan dispatchPlan;
	@Column(name = "DISPATCH_PLAN_ID")
	private Long planId;

	@Column(name = "DISPATCH_DATE")
	private Date dispatchDate;

	@Column(name = "SOURCE_LOC")
	private String sourceLocation;

	@Column(name = "DEST_LOC")
	private String destinationLocation;

	@Column(name = "ITEM_ID")
	private String itemId;

	@Column(name = "ITEM_DESCRIPTION")
	private String itemDescription;

	@Column(name = "TTE")
	private Double tte;

	@Column(name = "BATCH_CODE")
	private String batchCode;

	@Column(name = "QUANTITY")
	private Integer quantity;

	@Column(name = "PRIORITY")
	private Integer priority;

	@Column(name = "STATUS")
	private String status;

	@Column(name = "APP_STATUS")
	private String appStatus;

	@Column(name = "MARKET_SEGMENT")
	private String marketSegment;

	/*@ManyToOne
	@JoinColumn(name = "INSERT_USER")
	private ApplicationUser insertUser;*/
	@Column(name = "INSERT_USER")
	private String insertUser;

	@Column(name = "INSERT_DATE")
	private Date insertDate = new Date();

	/*@ManyToOne
	@JoinColumn(name = "UPDATE_USER")
	private ApplicationUser updateUser;*/
	@Column(name = "UPDATE_USER")
	private String updateUser;

	@Column(name = "UPDATE_DATE")
	private Date updateDate;

	@Column(name = "LINE_NUM")
	private Integer lineNumber;

	@Column(name = "ITEM_CATEGORY")
	private String category;

	@Column(name = "DEST_DESCRIPTION")
	private String destinationDescription;

	@Column(name = "COMMENTS")
	private String comments;

	public DispatchPlanItemTempInfo() {}

	public DispatchPlanItemTempInfo(Long id, Long planId, Integer lineNumber, Integer qantity) {
		super();
		this.id = id;
		this.planId = planId;
		this.lineNumber = lineNumber;
		this.quantity=qantity;
	}

	public DispatchPlanItemTempInfo(Long id, Long planId, Date dispatchDate, String sourceLocation,
			String destinationLocation, String itemId, String itemDescription, String batchCode, Integer quantity,
			Integer priority, String marketSegment, String insertUser, Date insertDate, Integer lineNumber,
			String category,Double tte) {
		super();
		this.id = id;
		this.planId = planId;
		this.dispatchDate = dispatchDate;
		this.sourceLocation = sourceLocation;
		this.destinationLocation = destinationLocation;
		this.itemId = itemId;
		this.itemDescription = itemDescription;
		this.batchCode = batchCode;
		this.quantity = quantity;
		this.priority = priority;
		this.marketSegment = marketSegment;
		this.insertUser = insertUser;
		this.insertDate = insertDate;
		this.lineNumber = lineNumber;
		this.category = category;
		this.tte = tte;
	}



}
