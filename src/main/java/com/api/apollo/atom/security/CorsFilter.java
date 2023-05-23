package com.api.apollo.atom.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CorsFilter implements Filter {
	private final List<String> allowedOrigins = Arrays.asList("http://localhost:26", "http://localhost:3000");

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletResponse res = (HttpServletResponse) response;
		HttpServletRequest req = (HttpServletRequest) request;
		String origin = req.getHeader("Origin");
		res.setHeader("Access-Control-Allow-Origin", allowedOrigins.contains(origin) ? origin : "*");
		// Access-Control-Allow-Credentials
		res.setHeader("Access-Control-Allow-Credentials", "true");

		res.setHeader("Vary", "Origin");

		// Access-Control-Max-Age
		res.setHeader("Access-Control-Max-Age", "3600");
		res.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
		res.setHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type,X-API-KEY, Accept, Accept-Encoding, Accept-Language, Host, Referer, Connection, User-Agent, authorization, sw-useragent, sw-version");

		// Just REPLY OK if request method is OPTIONS for CORS (pre-flight)
		if (req.getMethod().equals("OPTIONS")) {
			res.setStatus(HttpServletResponse.SC_OK);
			return;
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}
}
