package com.api.apollo.atom.entity.master;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "MT_ELR")
@Getter
@Setter
@NoArgsConstructor
public class MtElr {

    @Id
    @Column(name = "LOCATION_ID")
    private String locationId;

    @Column(name = "ITEM_DESCRIPTION")
    private String servprov;

    @Column(name = "ELR_FLAG")
    private String elrFlag;

    @Column(name = "INSERT_USER")
    private String insertUser;

    @Column(name = "INSERT_DATE")
    private Date insertDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    private Date updateDate;

}
