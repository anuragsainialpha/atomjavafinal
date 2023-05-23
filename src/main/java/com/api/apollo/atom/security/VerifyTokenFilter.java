package com.api.apollo.atom.security;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.GenericFilterBean;

import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.exception.InvalidUserTokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;

/*
This filter checks if there is a token in the Request service header and the token is not expired
it is applied to all the routes which are protected
*/
public class VerifyTokenFilter extends GenericFilterBean {

	private final TokenUtilService tokenUtilService;

	public VerifyTokenFilter(TokenUtilService tokenUtilService) {
		this.tokenUtilService = tokenUtilService;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		try {
			Optional<Authentication> authentication = tokenUtilService.verifyToken(request);
			if (authentication.isPresent()) {
				SecurityContextHolder.getContext().setAuthentication(authentication.get());
			} else {
				SecurityContextHolder.getContext().setAuthentication(null);
			}
			filterChain.doFilter(req, res);
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof ExpiredJwtException)
				setResponse(response, "Session Expired", HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED);
			if (e instanceof SignatureException)
				setResponse(response, "Invalid Token", HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED);
			if (e instanceof UsernameNotFoundException)
				setResponse(response, "User Not Found", HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND);
			if (e instanceof InvalidUserTokenException)
				setResponse(response, "Session Expired", HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED);
			else {
				setResponse(response, e.getMessage(), HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED);
			}
		} finally {
			SecurityContextHolder.getContext().setAuthentication(null);
		}
	}

	private void setResponse(HttpServletResponse response, String message, int httpStatusValue, HttpStatus httpStatus)
			throws IOException {
		response.setStatus(httpStatusValue);
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setStatusCode(httpStatusValue);
		apiResponse.setMessage(message);
		String jsonRespString = ow.writeValueAsString(apiResponse);
		response.setContentType("application/json");
		response.getWriter().write(jsonRespString);
		response.getWriter().flush();
		response.getWriter().close();
	}
}
