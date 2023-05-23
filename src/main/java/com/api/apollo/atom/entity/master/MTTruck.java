package com.api.apollo.atom.entity.master;

import com.api.apollo.atom.constant.Constants.TruckStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "MT_TRUCK")
@Getter
@Setter
@NoArgsConstructor
public class MTTruck {

	@Id
	@Column(name = "TRUCK_NUMBER")
	private String truckNumber;

	@Column(name = "SERVPROV")
	private String servprov;

	@ManyToOne
	@JoinColumn(name = "TT_ID")
	private MTTruckTypeInfo ttId;

	@Column(name = "TRUCK_BODY_TYPE")
	private String truckBodyType;

	@Column(name = "VARIANT1")
	private String variant1;

	@Column(name = "VARIANT2")
	private String variant2;

	@Column(name = "GPS_ENABLED")
	private String gpsEnabled;

	@Column(name = "GPS_DEVICE_ID")
	private String gpsDeviceId;

	@ManyToOne
	@JoinColumn(name = "GPS_PROVIDER")
	private GpsProviderInfo gpsProvider;

	@Column(name = "WIDTH")
	private Integer width;

	@Column(name = "WIDTH_UOM")
	private String widthUom;

	@Column(name = "HEIGHT")
	private Integer height;

	@Column(name = "HEIGHT_UOM")
	private String heightUom;

	@Column(name = "LENGTH")
	private Integer length;

	@Column(name = "LENGTH_UOM")
	private String lengthUom;

	@Column(name = "TARE_WEIGHT")
	private Double tareWeight;

	@Column(name = "TARE_WEIGHT_UOM")
	private String tareWeightUom;

	@Column(name = "PASSING_WEIGHT")
	private Double passingWeight;

	@Column(name = "PASSING_WEIGHT_UOM")
	private String passingWeightUom;

	@Column(name = "STATUS")
	@Enumerated(EnumType.STRING)
	private TruckStatus status;

	@Column(name = "LAST_UPDATE_PLANTCODE")
	private String lastUpdatePlantCode;

	@Column(name = "MAX_VOLUME")
	private Integer maxVolume;

	@Column(name = "MAX_VOLUME_UOM")
	private String maxVolumeUom;

	@Column(name = "INSERT_USER")
	private String insertUser;

	@Column(name = "UPDATE_USER")
	private String updateUser;

	@Column(name = "INSERT_DATE")
	private Date insertDate = new Date();

	@Column(name = "UPDATE_DATE")
	private Date updateDate;

}
