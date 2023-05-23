package com.api.apollo.atom.entity.master;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "MT_BATCH_CODES")
@Getter
public class MTBatchCode {

	@Id
	@Column(name = "BC_ID")
	private Integer batchCodeId;

	@Column(name = "BATCH_CODE")
	private String code;

	@Column(name = "CATEGORY")
	private String category;

	@Column(name = "PLANT_CODE")
	private String plantCode;

	@Column(name = "BATCH_DESCRIPTION")
	private String description;

}
