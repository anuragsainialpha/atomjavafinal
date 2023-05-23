package com.api.apollo.atom.security;

import java.util.Collection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class UserAuthentication implements Authentication {
	private final LoginUser user;
	private boolean authenticated = true;

	public UserAuthentication(LoginUser user) {
		this.user = user;
	}

	@Override
	public String getName() {
		return user.getUsername();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return user.getAuthorities();
	}

	@Override
	public Object getCredentials() {
		return user.getPassword();
	}

	@Override
	@Bean
	public LoginUser getDetails() {
		return user;
	}

	@Override
	public Object getPrincipal() {
		return user.getUsername();
	}

	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	@Override
	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}
}
