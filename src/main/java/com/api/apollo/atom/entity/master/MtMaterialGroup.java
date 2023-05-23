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
@Table(name = "MT_MATERIAL_GROUP", schema = "ATOM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MtMaterialGroup {
    @Id
    @Column(name = "ID", nullable = false, precision = 0)
    private Double id;

    @Column(name = "MATERIAL_GROUP_ID")
    private String materialGroupId;

    @Column(name = "DESCRIPTION_1")
    private String description_1;

    @Column(name = "DESCRIPTION_2")
    private String description_2;

    @Column(name = "SCM_GROUP")
    private String scmGroup;

    @Column(name = "INSERT_USER")
    private String insertUser;

    @Column(name = "INSERT_DATE")
    private Date insertDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    private Date updateDate;

    @Column(name = "MG_ID")
    private Double mgId;

}
