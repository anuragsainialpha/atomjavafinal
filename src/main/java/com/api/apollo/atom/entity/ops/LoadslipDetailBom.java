package com.api.apollo.atom.entity.ops;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "LOADSLIP_DETAIL_BOM", schema = "ATOM")
@Getter
@Setter
public class LoadslipDetailBom {

	@EmbeddedId
	@AttributeOverrides({
			@AttributeOverride(name = "loadslipId", column = @Column(name = "LOADSLIP_ID", nullable = false)),
			@AttributeOverride(name = "lineNo", column = @Column(name = "LINE_NO", nullable = false)),
			@AttributeOverride(name = "itemId", column = @Column(name = "ITEM_ID", nullable = false)) })
	private LoadslipDetailBomId loadslipDetailBomId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LOADSLIP_ID", nullable = false, insertable = false, updatable = false)
	@JsonBackReference
	private Loadslip loadslip;

	@Column(name = "TUBE_SKU")
	private String tubeSku;

	@Column(name = "FLAP_SKU")
	private String flapSku;

	@Column(name = "VALVE_SKU")
	private String valveSku;

	@Column(name = "TUBE_BATCH")
	private String tubeBatch;

	@Column(name = "FLAP_BATCH")
	private String flapBatch;

	@Column(name = "VALVE_BATCH")
	private String valveBatch;

	@Column(name = "TUBE_QTY")
	private Integer tubeQty;

	@Column(name = "FLAP_QTY")
	private Integer flapQty;

	@Column(name = "VALVE_QTY")
	private Integer valveQty;

	@Column(name = "INSERT_USER", nullable = false)
	private String insertUser;

	@Column(name = "INSERT_DATE", nullable = false)
	private Date insertDate;

	@Column(name = "UPDATE_USER")
	private String updateUser;
	
	@Column(name = "UPDATE_DATE")
	private Date updateDate;

	@Column(name = "TUBE_DESC")
	private String tubeDesc;

	@Column(name = "FLAP_DEC")
	private String flapDesc;

	@Column(name = "VALVE_DESC")
	private String valveDesc;

	@Column(name = "PCTR")
	private Integer pctr;
}
