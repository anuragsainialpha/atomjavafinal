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
@Table(name = "ORDER_TYPE_LOOKUP" , schema = "ATOM")
@Getter
@Setter
@NoArgsConstructor
public class OrderTypeLookup {


/*    @Column(name = "ID", nullable = false, precision = 0)
    private Double id;*/
    @Id
    @Column(name = "ORDER_TYPE")
    private String orderType;

    @Column(name = "MOVEMENT_TYPE")
    private String movementType;

    @Column(name = "MARKET_SEGMENT")
    private String marketSegment;

    @Column(name = "SAP_ORDER_TYPE")
    private String sapOrderType;

    @Column(name = "SAP_DOC_TYPE")
    private String sapDocType;

    @Column(name = "BOM_TYPE")
    private String bomType;

    @Column(name = "INSERT_USER")
    private String insertUser;

    @Column(name = "INSERT_DATE")
    private Date insertDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    private Date updateDate;

}
