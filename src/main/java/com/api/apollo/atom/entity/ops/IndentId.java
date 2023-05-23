package com.api.apollo.atom.entity.ops;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class IndentId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "INDENT_ID")
	private String indentId;

	@Column(name = "LINE_NUM")
	private Integer lineNum;

	public IndentId(String indentId, Integer lineNumber) {
		this.indentId = indentId;
		this.lineNum = lineNumber;
	}

}
