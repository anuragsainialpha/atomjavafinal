package com.api.apollo.atom.entity.plan;

import com.api.apollo.atom.constant.Constants.DispatchPlanItemAppStatus;
import com.api.apollo.atom.constant.Constants.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "DISPATCH_PLAN")
@NoArgsConstructor
@Getter
@Setter
public class DispatchPlanItemInfo {

  @Id
//  @GeneratedValue(strategy = GenerationType.AUTO)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "RUNNING_ID_GENERATION_SEQ")
  @SequenceGenerator(name = "RUNNING_ID_GENERATION_SEQ", sequenceName = "RUNNING_ID_GENERATION_SEQ", allocationSize = 1)
  @Column(name = "ID")
  private Long id;

  @JoinColumn(name = "DISPATCH_PLAN_ID")
  @ManyToOne(fetch = FetchType.EAGER)
  private DispatchPlan dispatchPlan;

  @Column(name = "LINE_NUM")
  private Integer lineNumber;

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

  @Column(name = "ITEM_CATEGORY")
  private String category;

  @Column(name = "TTE")
  private Double tte;

  @Column(name = "BATCH_CODE")
  private String batchCode;

  @Column(name = "QUANTITY")
  private Integer quantity;

  @Column(name = "PRIORITY")
  private Integer priority;

  @Column(name = "STATUS")
  @Enumerated(EnumType.STRING)
  private Status status;

  @Column(name = "APP_STATUS")
  @Enumerated(EnumType.STRING)
  private DispatchPlanItemAppStatus appStatus;

  @Column(name = "MARKET_SEGMENT")
  private String marketSegment;

  @Column(name = "APPROVED_QTY")
  private Integer approvedQuantity;

  @Column(name = "DELETED_QTY")
  private Integer deletedQuantity;

  @Column(name = "AVAIL_QTY")
  private Integer availableQuantity;

  @Column(name = "RESERVED_QTY")
  private Integer reservedQuantity;

  @Column(name = "DISPATCHED_QTY")
  private Integer dispatchedQuantity;

  @Column(name = "INSERT_USER")
  private String insertUser;

  @Column(name = "UPDATE_USER")
  private String updateUser;
	
	/*@ManyToOne
	@JoinColumn(name = "INSERT_USER")
	private ApplicationUser insertUser;*/

  @Column(name = "INSERT_DATE")
  private Date insertDate = new Date();

	/*@ManyToOne
	@JoinColumn(name = "UPDATE_USER")
	private ApplicationUser updateUser;*/

  @Column(name = "UPDATE_DATE")
  private Date updateDate;

  @Column(name = "DEST_DESCRIPTION")
  private String destinationDescription;

	@Column(name = "COMMENTS")
	private String comments;
	
	@Column(name = "TOT_AVAIL_QTY")
	private Integer totalAvailableQuantity;
	
	@Column(name = "UNAPP_QTY")
	private Integer unapprovedQuantity;
	
	@Column(name = "UNAPP_DEL_QTY")
	private Integer unapprovedDeletedQuantity;

	@Column(name = "LOADED_QTY")
	private Integer loadedQty;

  @Column(name = "WEIGHT")
  private Double weight;

  @Column(name = "WEIGHT_UOM")
  private String weightUom;

  @Column(name = "VOLUME")
  private Double volume;

  @Column(name = "VOLUME_UOM")
  private String volumeUom;


  public DispatchPlanItemInfo(Map<String, Object> planMap){
    this.appStatus = !StringUtils.isEmpty(planMap.get("appStatus")) ? DispatchPlanItemAppStatus.valueOf(planMap.get("appStatus").toString()) : null;
    this.approvedQuantity = !StringUtils.isEmpty(planMap.get("approvedQuantity")) ? Integer.parseInt(planMap.get("approvedQuantity").toString()) : null;
    this.availableQuantity = !StringUtils.isEmpty(planMap.get("availableQuantity")) ? Integer.parseInt(planMap.get("availableQuantity").toString()) : null;
    this.batchCode = !StringUtils.isEmpty(planMap.get("batchCode")) ? planMap.get("batchCode").toString() : null;
    this.category = !StringUtils.isEmpty(planMap.get("category")) ? planMap.get("category").toString() : null;
    this.comments = !StringUtils.isEmpty(planMap.get("comments")) ? planMap.get("comments").toString() : null;
    this.deletedQuantity = !StringUtils.isEmpty(planMap.get("deletedApprQuantity")) ? Integer.parseInt(planMap.get("deletedApprQuantity").toString()) : null;
    this.destinationDescription =!StringUtils.isEmpty(planMap.get("destinationDescription")) ? planMap.get("destinationDescription").toString() : null;
    this.destinationLocation = !StringUtils.isEmpty(planMap.get("destinationLocation")) ? planMap.get("destinationLocation").toString() : null;
    this.dispatchDate = !StringUtils.isEmpty(planMap.get("dispatchDate")) ? (Date) planMap.get("dispatchDate") : null;
    this.dispatchedQuantity = !StringUtils.isEmpty(planMap.get("dispatchedQuantity")) ? Integer.parseInt(planMap.get("dispatchedQuantity").toString()) : null;
    this.id = !StringUtils.isEmpty(planMap.get("id")) ? Long.parseLong(planMap.get("id").toString()) : null;
    this.insertDate = !StringUtils.isEmpty(planMap.get("insertDate")) ? (Date) planMap.get("insertDate") : null;
    this.insertUser = !StringUtils.isEmpty(planMap.get("insertUser")) ? planMap.get("insertUser").toString() : null;
    this.itemDescription = !StringUtils.isEmpty(planMap.get("itemDescription")) ? planMap.get("itemDescription").toString() : null;
    this.itemId = !StringUtils.isEmpty(planMap.get("itemId")) ? planMap.get("itemId").toString() : null;
    this.lineNumber = !StringUtils.isEmpty(planMap.get("lineNumber")) ? Integer.parseInt(planMap.get("lineNumber").toString()) : null;
    this.marketSegment =  !StringUtils.isEmpty(planMap.get("marketSegment")) ? planMap.get("marketSegment").toString() : null;
    this.dispatchPlan = new DispatchPlan(!StringUtils.isEmpty(planMap.get("planId")) ? Long.parseLong(planMap.get("planId").toString()) : null);
    this.priority = !StringUtils.isEmpty(planMap.get("priority")) ? Integer.parseInt(planMap.get("priority").toString()) : null;
    this.quantity = !StringUtils.isEmpty(planMap.get("quantity")) ? Integer.parseInt(planMap.get("quantity").toString()) : null;
    this.reservedQuantity = !StringUtils.isEmpty(planMap.get("reservedQuantity")) ? Integer.parseInt(planMap.get("reservedQuantity").toString()) : null;
    this.sourceLocation = !StringUtils.isEmpty(planMap.get("sourceLocation")) ? planMap.get("sourceLocation").toString() : null;
    this.status = !StringUtils.isEmpty(planMap.get("status")) ? Status.valueOf(planMap.get("status").toString()) : null;
    this.totalAvailableQuantity = !StringUtils.isEmpty(planMap.get("totalAvailableQuantity")) ? Integer.parseInt(planMap.get("totalAvailableQuantity").toString()) : null;
    this.tte = !StringUtils.isEmpty(planMap.get("tte")) ? Double.parseDouble(planMap.get("tte").toString()) : null;
    this.unapprovedQuantity = !StringUtils.isEmpty(planMap.get("unapprovedQuantity")) ? Integer.parseInt(planMap.get("unapprovedQuantity").toString()) : null;
    this.unapprovedDeletedQuantity =!StringUtils.isEmpty(planMap.get("deletedUnApprQuantity")) ? Integer.parseInt(planMap.get("deletedUnApprQuantity").toString()) : null;
    this.updateUser = !StringUtils.isEmpty(planMap.get("updateUser")) ? planMap.get("updateUser").toString() : null;
    this.updateDate = !StringUtils.isEmpty(planMap.get("updateDate")) ? (Date) planMap.get("updateDate") : null;
    this.loadedQty =!StringUtils.isEmpty(planMap.get("loadedQty")) ? Integer.parseInt(planMap.get("loadedQty").toString()) : null;
    this.weight = !StringUtils.isEmpty(planMap.get("weight")) ? Double.parseDouble(planMap.get("weight").toString()) : null;
    this.weightUom = !StringUtils.isEmpty(planMap.get("weightUom")) ? planMap.get("weightUom").toString() : null;
    this.volume = !StringUtils.isEmpty(planMap.get("volume")) ? Double.parseDouble(planMap.get("volume").toString()) : null;
    this.volumeUom = !StringUtils.isEmpty(planMap.get("volumeUom")) ? planMap.get("volumeUom").toString() : null;
  }


}
