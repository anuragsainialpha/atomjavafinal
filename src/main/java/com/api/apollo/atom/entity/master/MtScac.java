package com.api.apollo.atom.entity.master;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "MT_SCAC", schema = "ATOM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MtScac {
    @Id
    @Column(name = "ID", nullable = false, precision = 0)
    private Double id;

    @Column(name = "SCAC")
    private String scac;

    @Column(name = "COMPANY_NAME")
    private String companyName;
}
