package com.api.apollo.atom.entity.ops;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "del_inv_line")
@Getter
@Setter
@NoArgsConstructor
public class DelInvLine {

    @EmbeddedId
    private DelInvLineId delInvLineId;

    @Column(name = "LINE_NO")
    private Integer lineNo;

    @Column(name = "QTY")
    private Integer qty;

    @Column(name = "WEIGHT")
    private Double weight;

    @Column(name = "WEIGHT_UOM")
    private String weightUom;

    @Column(name = "INSERT_USER", nullable = false)
    private String insertUser;

    @Column(name = "INSERT_DATE", nullable = false)
    private Date insertDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    private Date updateDate;

}
