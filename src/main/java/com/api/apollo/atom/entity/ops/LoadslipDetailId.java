package com.api.apollo.atom.entity.ops;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoadslipDetailId implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "LOADSLIP_ID", nullable = false)
	private String loadslipId;
	@Column(name = "LINE_NO",nullable = false,scale = 1)
	private Double lineNo;
	@Column(name = "ITEM_ID", nullable = false)
	private String itemId;
}
