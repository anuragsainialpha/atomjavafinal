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
@Table(name = "LOCATION_SCAN", schema = "ATOM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationScan {

    @Id
    @Column(name = "ID", nullable = false, precision = 0)
    private Double id;

    @Column(name = "LOCATION_ID")
    private String locationId;

    @Column(name = "SCANNABLE")
    private String scannable;

    @Column(name = "ITEM_CATEGORY")
    private String itemCategory;

    @Column(name = "INSERT_USER")
    private String insertUser;

    @Column(name = "INSERT_DATE")
    private Date insertDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    private Date updateDate;

}
