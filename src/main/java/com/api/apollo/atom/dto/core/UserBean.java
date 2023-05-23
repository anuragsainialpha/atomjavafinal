package com.api.apollo.atom.dto.core;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserBean {

	private String email;
	private String userId;
	private String firstName;
	private String lastName;
	private String role;
	private String token;
	private String sourceId;
	private Boolean isExtWarehouse;
	private String persistenceInstance;

}
