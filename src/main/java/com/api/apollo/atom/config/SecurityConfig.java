package com.api.apollo.atom.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.api.apollo.atom.repository.UserRepository;
import com.api.apollo.atom.security.AuthenticateUserFilter;
import com.api.apollo.atom.security.CorsFilter;
import com.api.apollo.atom.security.TokenUtilService;
import com.api.apollo.atom.security.VerifyTokenFilter;

@Configuration
@EnableWebSecurity
@Order(1)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	private TokenUtilService tokenUtilService;
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private Environment environment;

	@Override
	public void configure(WebSecurity web) {
		// Filters will not get executed for the resources

		web.ignoring().antMatchers("/","/csrf","/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/**", "/swagger-ui.html", "/webjars/**",
		 "/api/sample/**", "/api/common/**", "/api/file/**"
		);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		        .antMatchers("/api/v1/user/login","/api/v1/user/update-dispatchQty").permitAll().anyRequest().fullyAuthenticated().and()
				.exceptionHandling().and().csrf().disable()
				.addFilterBefore(new CorsFilter(), ChannelProcessingFilter.class)
				.addFilterBefore(new VerifyTokenFilter(tokenUtilService), UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(new AuthenticateUserFilter("/api/v1/user/login", authenticationManager(), tokenUtilService,
						userRepository,environment), UsernamePasswordAuthenticationFilter.class)
				.authorizeRequests().anyRequest().authenticated();
	}
}
