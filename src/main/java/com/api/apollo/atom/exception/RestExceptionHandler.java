package com.api.apollo.atom.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.service.MessageByLocaleService;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

	@Autowired
	MessageByLocaleService localeService;

	@ExceptionHandler(value = { Exception.class })
	protected ApiResponse handleBaseException(Exception ex) {
		ex.printStackTrace();

		if (ex instanceof ImproperDataException) {
			return new ApiResponse(HttpStatus.NOT_FOUND, ex.getMessage());
		}

		if (ex instanceof InvalidUserTokenException) {
			return new ApiResponse(HttpStatus.BAD_REQUEST, "Missing header authorization token");
		}
		
		if (ex instanceof UsernameNotFoundException) {
			return new ApiResponse(HttpStatus.NOT_FOUND, ex.getMessage());
		}
		
		if (ex instanceof AccessDeniedException)
			return new ApiResponse(HttpStatus.FORBIDDEN, ex.getMessage());

		//return new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, localeService.getMessage("error.internal.problem"));
		return new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error occurred" + ex.getMessage());
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		String name = ex.getParameterName();
		ApiResponse res = new ApiResponse(HttpStatus.BAD_REQUEST, String.format("%s is required ", name));
		return new ResponseEntity<Object>(res, HttpStatus.BAD_REQUEST);
	}

}
