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
@Table(name = "CT_OTM_FREIGHT_BASIS")
@Getter
@Setter
@NoArgsConstructor
public class CtOtmFreightBasis {

    @Id
    @Column(name = "ID", nullable = false, precision = 0)
    private Double id;

    @Column(name = "BASIS")
    private String basis;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "IN_PAAS")
    private String inPaas;

    @Column(name = "IN_OTM")
    private String inOtm;

    @Column(name = "OTM_BASIS")
    private String otmBasis;




}
