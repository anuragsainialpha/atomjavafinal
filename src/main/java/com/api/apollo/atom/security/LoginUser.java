package com.api.apollo.atom.security;

import org.springframework.security.core.authority.AuthorityUtils;

import com.api.apollo.atom.entity.ApplicationUser;

public class LoginUser extends org.springframework.security.core.userdetails.User {
	private ApplicationUser user;

	public LoginUser(ApplicationUser user) {
		super(user.getUserId(), user.getPassword(), AuthorityUtils.createAuthorityList(user.getRole().toString()));
		this.user = user;
	}

	public ApplicationUser getUser() {
		return user;
	}

	public String getRole() {
		return user.getRole().toString();
	}
}
