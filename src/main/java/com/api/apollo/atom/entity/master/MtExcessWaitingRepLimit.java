
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
@Table(name = "MT_EXCESS_WAITING_REP_LIMIT")
@Getter
@Setter
@NoArgsConstructor
public class MtExcessWaitingRepLimit {

    @Id
    @Column(name = "ID", nullable = false, precision = 0)
    private Double id;

    @Column(name = "REPORTING_LOC")
    private String reportingLoc;

    @Column(name = "EXCESS_TIME")
    private Double excessTime;

    @Column(name = "INSERT_USER")
    private String insertUser;

    @Column(name = "INSERT_DATE")
    private Date insertDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    private Date updateDate;

}
