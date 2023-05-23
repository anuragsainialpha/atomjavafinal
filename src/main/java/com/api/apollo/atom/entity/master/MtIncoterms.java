
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
@Table(name = "MT_VALVE")
@Getter
@Setter
@NoArgsConstructor
public class MtIncoterms {

    @Id
    @Column(name = "ITEM_ID")
    private String itemId;

    @Column(name = "ITEM_DESCRIPTION")
    private String itemDescription;

    @Column(name = "ITEM_CATEGORY")
    private String itemCategory;

    @Column(name = "BATCH_CODE")
    private String batchCode;

    @Column(name = "INSERT_USER")
    private String insertUser;

    @Column(name = "INSERT_DATE")
    private Date insertDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    private Date updateDate;

    @Column(name = "VALVE_ID")
    private Double valveId;

}