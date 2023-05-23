package com.api.apollo.atom.entity.master;

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
@Table(name = "MT_SAP_TRUCK_TYPE", schema = "ATOM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MtSapTruckType {
    @Id
    @Column(name = "ID", nullable = false, precision = 0)
    private Double id;

    @Column(name = "SAP_TRUCK_TYPE")
    private String sapTruckType;

    @Column(name = "SAP_TRUCK_TYPE_DESC")
    private String sapTruckTypeDesc;

    @Column(name = "OPS_TRUCK_TYPE")
    private String opsTruckType;

    @Column(name = "OPS_VARIANT_1")
    private String opsVariant1;

    @Column(name = "STT_ID")
    private Double sttId;

    @Column(name = "INSERT_USER")
    private String insertUser;

    @Column(name = "INSERT_DATE")
    private Date insertDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    private Date updateDate;
}

