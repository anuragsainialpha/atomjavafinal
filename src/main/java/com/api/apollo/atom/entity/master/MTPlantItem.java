package com.api.apollo.atom.entity.master;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;


@Entity
@Table(name = "MT_ITEM_PLANT_WEIGHT", schema = "ATOM")
@Setter
@Getter
public class MTPlantItem {

    @Id
    @Column(name = "ID", nullable = false, precision = 0)
    private Double id;

    @Column(name = "PLANT_CODE")
    private String plantCode;

    @Column(name = "EFFECTIVE_DATE")
    private Date effectiveDate;

    @Column(name = "ITEM_ID")
    private String itemId;

    @Column(name = "WEIGHT")
    private Double weight;

    @Column(name = "WEIGHT_UOM")
    private String weightUom;

    @Column(name = "INSERT_USER")
    private String insertUser;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "INSERT_DATE")
    private Date insertDate;

    @Column(name = "UPDATE_DATE")
    private Date updateDate;

}
