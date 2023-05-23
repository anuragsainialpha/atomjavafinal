package com.api.apollo.atom.entity.plan;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.api.apollo.atom.constant.Constants.Status;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "DISP_PLAN")
@Getter
@Setter
@NoArgsConstructor
public class DispatchPlan {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "DISPATCH_PLAN_ID")
	private Long id;

	@Column(name = "TOTAL_QTY", nullable = false)
	private Integer totalQuantity;

	@Column(name = "STATUS")
	@Enumerated(EnumType.STRING)
	private Status status;

	@Column(name = "INSERT_USER")
	private String insertUser;

	@Column(name = "INSERT_DATE")
	private Date insertDate = new Date();

	public DispatchPlan(Long planId) {
		this.id = planId;
	}
}
