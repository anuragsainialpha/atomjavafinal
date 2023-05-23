package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.ItemType;
import com.api.apollo.atom.entity.master.MTItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class MTItemInfoDto {

    @JsonProperty("id")
    @SerializedName(value = "id")
    private String id;

    @JsonProperty("classification")
    @SerializedName(value = "classification")
    private String classification;

    @JsonProperty("description")
    @SerializedName(value = "description")
    private String description;

    @JsonProperty("type")
    @SerializedName(value = "type")
    private ItemType type;


    @JsonProperty("group")
    @SerializedName(value = "group")
    private String group;

    @JsonProperty("tte")
    @SerializedName(value = "tte")
    private Double tte;

    @JsonProperty("loadFactor")
    @SerializedName(value = "loadFactor")
    private Double loadFactor;

    @JsonProperty("grossWt")
    @SerializedName(value = "grossWt")
    private Double grossWt;

    @JsonProperty("grossWtUom")
    @SerializedName(value = "grossWtUom")
    private String grossWtUom;

    @JsonProperty("netWt")
    @SerializedName(value = "netWt")
    private Double netWt;

    @JsonProperty("netWtUom")
    @SerializedName(value = "netWtUom")
    private String netWtUom;

    @JsonProperty("volume")
    @SerializedName(value = "volume")
    private Double volume;

    @JsonProperty("volUom")
    @SerializedName(value = "volUom")
    private String volUom;

    @JsonProperty("length")
    @SerializedName(value = "length")
    private Integer length;

    @JsonProperty("lenUom")
    @SerializedName(value = "lenUom")
    private String lenUom;

    @JsonProperty("width")
    @SerializedName(value = "width")
    private Integer width;

    @JsonProperty("wdUom")
    @SerializedName(value = "wdUom")
    private String wdUom;

    @JsonProperty("height")
    @SerializedName(value = "height")
    private Integer height;

    @JsonProperty("htUom")
    @SerializedName(value = "htUom")
    private String htUom;

    @JsonProperty("diameter")
    @SerializedName(value = "diameter")
    private Integer diameter;

    @JsonProperty("dmUom")
    @SerializedName(value = "dmUom")
    private String dmUom;

    @JsonProperty("insertUser")
    @SerializedName(value = "insertUser")
    private String insertUser;

    @JsonProperty("insertDate")
    @SerializedName(value = "insertDate")
    private Date insertDate;

    @JsonProperty("updateUser")
    @SerializedName(value = "updateUser")
    private String updateUser;

    @JsonProperty("updateDate")
    @SerializedName(value = "updateDate")
    private Date updateDate;

    @JsonProperty("category")
    @SerializedName(value = "category")
    private String category;


    public MTItemInfoDto(MTItem item) {
        this.id = item.getId();
        this.classification = item.getClassification();
        this.description = item.getDescription();
        this.type = item.getType();
        this.group = item.getGroup();
        this.tte = item.getTte();
        this.loadFactor = item.getLoadFactor();
        this.grossWt = item.getGrossWt();
        this.grossWtUom = item.getGrossWtUom();
        this.netWt = item.getNetWt();
        this.netWtUom = item.getNetWtUom();
        this.volume = item.getVolume();
        this.volUom = item.getVolUom();
        this.length = item.getLength();
        this.lenUom = item.getLenUom();
        this.width = item.getWidth();
        this.wdUom = item.getWdUom();
        this.height = item.getHeight();
        this.htUom = item.getHtUom();
        this.diameter = item.getDiameter();
        this.dmUom = item.getDmUom();
        this.insertUser = item.getInsertUser();
        this.insertDate = item.getInsertDate();
        this.updateUser = item.getUpdateUser();
        this.updateDate = item.getUpdateDate();
        this.category = item.getCategory();
    }
}
