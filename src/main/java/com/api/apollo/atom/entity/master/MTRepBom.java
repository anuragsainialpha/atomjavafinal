package com.api.apollo.atom.entity.master;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;


@Entity
@Table(name = "MT_ITEM_REP_BOM", schema = "ATOM")
@Setter
@Getter
public class MTRepBom {

    @Id
    @Column(name = "ID", nullable = false, precision = 0)
    private Double id;

    @Column(name = "SALES_SKU")
    private String salesSku;

    @Column(name = "ITEM_ID")
    private String itemId;

    @Column(name = "COMP_QTY")
    private Integer compQty;

    @Column(name = "ITEM_SEQ")
    private Integer itemSeq;

    @Column(name = "INSERT_USER")
    private String insertUser;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "INSERT_DATE")
    private Date insertDate;

    @Column(name = "UPDATE_DATE")
    private Date updateDate;
}
