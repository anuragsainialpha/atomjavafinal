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
@Table(name = "MT_BATCH_CODES", schema = "ATOM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MtBatchCodes {

    @Id
    @Column(name = "ID", nullable = false, precision = 0)
    private Double id;

    @Column(name = "BATCH_CODE")
    private String batchCode;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "PLANT_CODE")
    private String plantCode;

    @Column(name = "BATCH_DESCRIPTION")
    private String batchDescription;

    @Column(name = "INSERT_USER")
    private String insertUser;

    @Column(name = "INSERT_DATE")
    private Date insertDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    private Date updateDate;

    @Column(name = "BC_ID")
    private Double bcId;
}

