package com.api.apollo.atom.entity.ops;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.persistence.*;

import com.api.apollo.atom.constant.Constants.TruckReportStatus;
import com.api.apollo.atom.entity.master.MTTruck;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.poi.util.StringUtil;
import org.springframework.util.StringUtils;

@Entity
@Table(name = "INDENT_DETAIL")
@Getter
@Setter
@NoArgsConstructor
public class IndentDetails implements Serializable {
 
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private IndentId indent;
	
	@ManyToOne
    @JoinColumns({
        @JoinColumn(name = "INDENT_ID", referencedColumnName = "INDENT_ID", insertable = false, updatable = false)
    })
    private IndentSummary indentSummary;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "TRUCK_NUMBER", referencedColumnName = "TRUCK_NUMBER")
	private MTTruck truck;

	@Column(name = "CONTAINER_NUM")
	private String containerNum;

	@Column(name = "TRUCK_TYPE")
	private String truckType;

	@Column(name = "ACTUAL_TRUCK_TYPE")
	private String actualTruckType;

	@Column(name = "VARIANT1")
	private String variant1;

	@Column(name = "TTE_CAPACITY")
	private Double tte;

	@Column(name = "DRIVER_NAME")
	private String driverName;

	@Column(name = "DRIVER_MOBILE")
	private String driverMobile;

	@Column(name = "DRIVER_LICENSE")
	private String driverLicense;

	@Column(name = "PASSING_WEIGHT")
	private Double passingWeight;

	@Column(name = "GPS_ENABLED")
	private String gpsEnabled;

	@Column(name = "GPS_PROVIDER")
	private String gpsProvider;
	
	//@OneToOne(fetch = FetchType.EAGER)
	//@JoinColumn(name = "GATE_CONTROL_CODE", referencedColumnName = "GATE_CONTROL_CODE")
	//private TruckReport truckReport;
	@Column(name = "GATE_CONTROL_CODE")
	private String gateControlCode;

	@Column(name = "STATUS")
	@Enumerated(EnumType.STRING)
	private TruckReportStatus status;

	@Column(name = "INSERT_USER")
	private String insertUser;

	@Column(name = "UPDATE_USER")
	private String updateUser;

	@Column(name = "INSERT_DATE")
	private Date insertDate = new Date();

	@Column(name = "UPDATE_DATE")
	private Date updateDate;



	@Transient
	private String indentId;

	@Transient
	private  String truckNumber;

	public IndentDetails(Map<String, Object> indentDetailsMap) {
		this.indentId = !StringUtils.isEmpty(indentDetailsMap.get("indentId")) ? (String) indentDetailsMap.get("indentId") : null;
		this.truckNumber = !StringUtils.isEmpty(indentDetailsMap.get("truckNumber")) ? (String) indentDetailsMap.get("truckNumber") : null;
		this.containerNum = !StringUtils.isEmpty(indentDetailsMap.get("containerNumber")) ? (String) indentDetailsMap.get("containerNumber") : null;
		this.truckType = !StringUtils.isEmpty(indentDetailsMap.get("truckType")) ? (String) indentDetailsMap.get("truckType") : null;
		this.actualTruckType = !StringUtils.isEmpty(indentDetailsMap.get("actualTruckType")) ? (String) indentDetailsMap.get("actualTruckType") : null;
		this.variant1 = !StringUtils.isEmpty(indentDetailsMap.get("variant1")) ? (String) indentDetailsMap.get("variant1") : null;
		this.tte = !StringUtils.isEmpty(indentDetailsMap.get("tteCapacity")) ? Double.parseDouble(indentDetailsMap.get("tteCapacity").toString()) : null;
		this.driverName = !StringUtils.isEmpty(indentDetailsMap.get("driverName")) ? (String) indentDetailsMap.get("driverName") : null;
		this.driverMobile = !StringUtils.isEmpty(indentDetailsMap.get("driverMobile")) ? (String) indentDetailsMap.get("driverMobile") : null;
		this.driverLicense = !StringUtils.isEmpty(indentDetailsMap.get("driverLicense")) ? (String) indentDetailsMap.get("driverLicense") : null;
		this.passingWeight = !StringUtils.isEmpty(indentDetailsMap.get("passingWeight")) ? Double.parseDouble(indentDetailsMap.get("passingWeight").toString()) : null;
		this.gpsEnabled = !StringUtils.isEmpty(indentDetailsMap.get("gpsEnabled")) ? (String) indentDetailsMap.get("gpsEnabled") : null;
		this.gpsProvider = !StringUtils.isEmpty(indentDetailsMap.get("gpsProvider")) ? (String) indentDetailsMap.get("gpsProvider") : null;
		this.gateControlCode = !StringUtils.isEmpty(indentDetailsMap.get("gateControlCode")) ? (String) indentDetailsMap.get("gateControlCode") : null;
		this.status = !StringUtils.isEmpty(indentDetailsMap.get("status")) ? TruckReportStatus.valueOf(indentDetailsMap.get("status").toString()) : null;
		this.insertUser = !StringUtils.isEmpty(indentDetailsMap.get("insertUser")) ? (String) indentDetailsMap.get("insertUser") : null;
		this.updateUser = !StringUtils.isEmpty(indentDetailsMap.get("updateUser")) ? (String) indentDetailsMap.get("updateUser") : null;
		this.insertDate = !StringUtils.isEmpty(indentDetailsMap.get("insertDate")) ? (Date) indentDetailsMap.get("insertDate") : null;
		this.updateDate = !StringUtils.isEmpty(indentDetailsMap.get("updateDate")) ? (Date) indentDetailsMap.get("updateDate") : null;
	}
}
