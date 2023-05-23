package com.api.apollo.atom.entity.ops;

import com.api.apollo.atom.constant.Constants.Status;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "INDENT_SUMMARY")
@Getter
@Setter
@NoArgsConstructor
@SequenceGenerator(name = "indent_summary_seq", sequenceName = "INDENT_SUMMARY_SEQ", allocationSize = 1)
public class IndentSummary implements Serializable {

  private static final long serialVersionUID = -9092555498816647699L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "indent_summary_seq")
  @Column(name = "ID")
  private Long id;

  @Column(name = "INDENT_ID")
  private String indentId;

  @Column(name = "DISPATCH_DATE")
  private Date dispatchDate;

  @Column(name = "SOURCE_LOC")
  private String sourceLocation;

  @Column(name = "DEST_LOC")
  private String destinationLocation;

  @Column(name = "TRUCK_TYPE")
  private String truckType;

  @Column(name = "LOAD_FACTOR")
  private Double loadFactor = 0.0;

  @Column(name = "SERVPROV")
  private String transporter;

  @Column(name = "ITEM_CATEGORY")
  private String category;

  @Column(name = "TTE_CAPACITY")
  private Double tte = 0.0;

  @Column(name = "INDENTED")
  private Integer indented = 0;

  @Column(name = "CANCELLED")
  private Integer cancelled = 0;

  @Column(name = "NET_REQUESTED")
  private Integer netRequested = 0;

  @Column(name = "TRANS_CONFIRMED")
  private Integer transConfirmed = 0;

  @Column(name = "TRANS_DECLINED")
  private Integer transDeclined = 0;

  @Column(name = "TRANS_ASSIGNED")
  private Integer transAssigned = 0;

  @Column(name = "REPORTED")
  private Integer reported = 0;

  @Column(name = "REJECTED")
  private Integer rejected = 0;

  @Column(name = "NET_PLACED")
  private Integer netPlaced = 0;

  @Column(name = "NET_BALANCE")
  private Integer netBalance = 0;

  @Column(name = "STATUS")
  @Enumerated(EnumType.STRING)
  private Status status;

  @Column(name = "INDENT_COMMENTS", columnDefinition = "TEXT")
  private String comments;

  @Column(name = "INDENT_AGING")
  private Integer indentAging = 0;

  @Column(name = "INSERT_USER")
  private String insertUser;

  @Column(name = "UPDATE_USER")
  private String updateUser;

  @Column(name = "INSERT_DATE")
  private Date insertDate = new Date();

  @Column(name = "UPDATE_DATE")
  private Date updateDate;

  @Column(name = "FRT_AVAIL_FLAG")
  private String isFreightAvailable;

  @Transient
  private String destDis;

  @Column(name = "DEST_COUNTRY")
  private String destCountry;

  @Column(name = "POD")
  private String pod;

  @Transient
  private String destCountryName;

	@Column(name = "DEST_DESCRIPTION")
	private String destinationDescription;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "indentSummary")
  public Set<IndentDetails> indentDetails = new HashSet<>();

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "indentSummary")
  @JsonManagedReference
  public Set<TruckReport> truckReports = new HashSet<>();

	public IndentSummary(Long id, String indentId, Date dispatchDate, String sourceLocation, String destinationLocation,
			String truckType, Double loadFactor, String transporter, String category, Double tte, Integer indented,
			Integer cancelled, Integer netRequested, Integer transConfirmed, Integer transDeclined,
			Integer transAssigned, Integer reported, Integer rejected, Integer netPlaced, Integer netBalance,
			Status status, String comments, Integer indentAging, String insertUser, String updateUser, Date insertDate,
			Date updateDate,String isFreightAvailable, String destDis, String destCountry, String pod) {

		this.id = id;
		this.indentId = indentId;
		this.dispatchDate = dispatchDate;
		this.sourceLocation = sourceLocation;
		this.destinationLocation = destinationLocation;
		this.truckType = truckType;
		this.loadFactor = loadFactor;
		this.transporter = transporter;
		this.tte = tte;
		this.indented = indented;
		this.cancelled = cancelled;
		this.category = category;
		this.netRequested = netRequested;
		this.transConfirmed = transConfirmed;
		this.transDeclined = transDeclined;
		this.transAssigned = transAssigned;
		this.rejected = rejected;
		this.netPlaced = netPlaced;
		this.reported = reported;

		this.netBalance = netBalance;
		this.status = status;
		this.comments = comments;

		this.indentAging = indentAging;
		this.insertUser = insertUser;
		this.updateUser = updateUser;

		this.insertDate = insertDate;
		this.updateDate = updateDate;
		this.destDis = destDis;
    this.isFreightAvailable = isFreightAvailable;
    this.destCountry = destCountry;
    this.pod = pod;
	}

	/*Used In filter indent multiSelect*/
  public IndentSummary(Object id, Object indentId, Object dispatchDate, Object sourceLocation, Object destinationLocation,
                       Object truckType, Object loadFactor, Object transporter, Object category, Object tte, Object indented,
                       Object cancelled, Object netRequested, Object transConfirmed, Object transDeclined,
                       Object transAssigned, Object reported, Object rejected, Object netPlaced, Object netBalance,
                       Object status, Object comments, Object indentAging, Object insertUser, Object updateUser, Object insertDate,
                       Object updateDate,Object isFreightAvailable, Object destCountry, Object pod) {

    this.id = !StringUtils.isEmpty(id) ? (Long) id : null;
    this.indentId = !StringUtils.isEmpty(indentId) ? (String) indentId : null;
    this.dispatchDate = !StringUtils.isEmpty(dispatchDate) ? (Date) dispatchDate : null;
    this.sourceLocation = !StringUtils.isEmpty(id) ? (String) sourceLocation : null;
    this.destinationLocation = !StringUtils.isEmpty(destinationLocation) ? (String) destinationLocation : null;
    this.truckType =  !StringUtils.isEmpty(truckType) ? (String) truckType : null;
    this.loadFactor = !StringUtils.isEmpty(loadFactor) ? (Double) loadFactor : null;
    this.transporter =  !StringUtils.isEmpty(transporter) ? (String) transporter : null;
    this.tte = !StringUtils.isEmpty(tte) ? (Double) tte : null;
    this.indented = !StringUtils.isEmpty(indented) ? (Integer) indented : null;
    this.cancelled = !StringUtils.isEmpty(cancelled) ? (Integer) cancelled : null;
    this.category = !StringUtils.isEmpty(category) ? (String) category : null;
    this.netRequested = !StringUtils.isEmpty(netRequested) ? (Integer) netRequested : null;
    this.transConfirmed = !StringUtils.isEmpty(transConfirmed) ? (Integer) transConfirmed : null;
    this.transDeclined = !StringUtils.isEmpty(transDeclined) ? (Integer) transDeclined : null;
    this.transAssigned = !StringUtils.isEmpty(transAssigned) ? (Integer) transAssigned : null;
    this.rejected = !StringUtils.isEmpty(rejected) ? (Integer) rejected : null;
    this.netPlaced = !StringUtils.isEmpty(netPlaced) ? (Integer) netPlaced : null;
    this.reported = !StringUtils.isEmpty(netBalance) ? (Integer) reported : null;

    this.netBalance = !StringUtils.isEmpty(netBalance) ? (Integer) netBalance : null;
    this.status = !StringUtils.isEmpty(status) ? (Status) status : null;
    this.comments = !StringUtils.isEmpty(comments) ? (String) comments : null;

    this.indentAging = !StringUtils.isEmpty(indentAging) ? (Integer) indentAging : null;
    this.insertUser = !StringUtils.isEmpty(insertUser) ? (String) insertUser : null;
    this.updateUser = !StringUtils.isEmpty(updateUser) ? (String) updateUser : null;

    this.insertDate = !StringUtils.isEmpty(insertDate) ? (Date) insertDate : null;
    this.updateDate = !StringUtils.isEmpty(updateDate) ? (Date) updateDate : null;
    this.isFreightAvailable = !StringUtils.isEmpty(isFreightAvailable) ? (String) isFreightAvailable : null;
    this.destCountry = !StringUtils.isEmpty(destCountry) ? (String) destCountry : null;
    this.pod = !StringUtils.isEmpty(pod) ? (String) pod : null;
  }
  
}
