package com.api.apollo.atom.entity.master;

import lombok.AllArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "MT_TRUCK_TYPE", schema = "ATOM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MtTruckType {
    @Id
    @Column(name = "ID", nullable = false, precision = 0)
    private Double id;

    @Column(name = "TRUCK_TYPE")
    private String truckType;

    @Column(name = "LOAD_FACTOR")
    private Double loadFactor;

    @Column(name = "TRUCK_DESC")
    private String truckDesc;

    @Column(name = "TTE_CAPACITY", precision = 0)
    private Double tteCapacity;

    @Column(name = "GROSS_WT", precision = 0)
    private Double grossWt;

    @Column(name = "GROSS_WT_UOM")
    private String grossWtUom;

    @Column(name = "GROSS_VOL")
    private Double grossVol;

    @Column(name = "GROSS_VOL_UOM")
    private String grossVolUom;

    @Column(name = "VARIANT1")
    private String variant1;

    @Column(name = "VARIANT2")
    private String variant2;

    @Column(name = "INSERT_USER")
    private String insertUser;

    @Column(name = "INSERT_DATE")
    private Date insertDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    private Date updateDate;

    @Column(name = "TT_ID")
    private Double ttId;

}
