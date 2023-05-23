package com.api.apollo.atom.entity.master;

import com.api.apollo.atom.constant.ItemType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "MT_ITEM")
@Getter
@Setter
@SqlResultSetMapping(name = "findNotExistItems", classes = @ConstructorResult(targetClass = MTItem.class, columns = {
		@ColumnResult(name = "ITEM_ID", type = String.class), @ColumnResult(name = "DESCRIPTION", type = String.class),
		@ColumnResult(name = "TTE", type = Double.class), @ColumnResult(name = "IS_ITEM_EXISTS", type = String.class),
		@ColumnResult(name = "IS_TTE_EXISTS", type = String.class) }))
@NamedNativeQuery(name = "findNotExistItems", resultClass = MTItem.class, resultSetMapping = "findNotExistItems", 
                  query = "SELECT I_DATA.ITEM_ID,I_DATA.DESCRIPTION,I_DATA.TTE,I_DATA.IS_ITEM_EXISTS,I_DATA.IS_TTE_EXISTS FROM TABLE(ATL_BUSINESS_FLOW_PKG.GET_ITEM_DATA(ITEM_ARRAY(?1)))I_DATA")

public class MTItem {

	@Id
	@Column(name = "ITEM_ID")
	private String id;

	@Column(name = "ITEM_CLASSIFICATION")
	private String classification;

	@Column(name = "ITEM_DESCRIPTION")
	private String description;

	@Column(name = "ITEM_TYPE")
	@Enumerated(EnumType.STRING)
	private ItemType type;

	@Column(name = "ITEM_GROUP")
	private String group;

	@Column(name = "TTE")
	private Double tte;

	@Column(name = "LOAD_FACTOR")
	private Double loadFactor;

	@Column(name = "GROSS_WT")
	private Double grossWt;

	@Column(name = "GROSS_WT_UOM")
	private String grossWtUom;

	@Column(name = "NET_WT")
	private Double netWt;

	@Column(name = "NET_WT_UOM")
	private String netWtUom;

	@Column(name = "VOLUME")
	private Double volume;

	@Column(name = "VOL_UOM")
	private String volUom;

	@Column(name = "LENGTH")
	private Integer length;

	@Column(name = "LEN_UOM")
	private String lenUom;

	@Column(name = "WIDTH")
	private Integer width;

	@Column(name = "WD_UOM")
	private String wdUom;

	@Column(name = "HEIGHT")
	private Integer height;

	@Column(name = "HT_UOM")
	private String htUom;

	@Column(name = "DIAMETER")
	private Integer diameter;

	@Column(name = "DM_UOM")
	private String dmUom;

	@Column(name = "INSERT_USER")
	private String insertUser;

	@Column(name = "INSERT_DATE")
	private Date insertDate;

	@Column(name = "UPDATE_USER")
	private String updateUser;

	@Column(name = "UPDATE_DATE")
	private Date updateDate;

	@Column(name = "ITEM_CATEGORY")
	private String category;

	/*@Transient
	@Column(name = "IS_ITEM_EXISTS")
	private String isItemExist;

	@Transient
	@Column(name = "IS_TTE_EXISTS")
	private String isTteExist;

	@Transient
	@Column(name = "DESCRIPTION")
	private String itemDescription;*/
	
	public MTItem() {}

	/*public MTItem(String id, String description, Double tte, String isItemExist, String isTteExist) {
		super();
		this.id = id;
		this.description = description;
		this.tte = tte;
		this.isItemExist = isItemExist;
		this.isTteExist = isTteExist;
	}*/

	public MTItem(String category) {
		this.category = category;
	}
	public MTItem(String id, String description) {
		this.id = id;
		this.description = description;
	}

	public MTItem(String id, Double tte, String category, String description) {
		super();
		this.id = id;
		this.tte = tte;
		this.category = category;
		this.description = description;
	}

}
