package com.api.apollo.atom.entity.master;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;

@Entity
@Table(name = "CT_GPS")
@Getter
public class GpsProviderInfo {

	@Id
	@Column(name = "GPS_PROVIDER")
	private String gpsProvider;

	@Column(name = "URL")
	private String url;

	@Column(name = "PASSWORD")
	private String password;

}
