package com.api.apollo.atom.controller;

import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.exception.InvalidException;
import com.api.apollo.atom.exception.UnAuthorisedException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;


@ControllerAdvice
@RestController
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  /*  @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        *//*ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
                request.getDescription(false));*//*
        ApiResponse apiResponse = new ApiResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        return new ResponseEntity(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public final ResponseEntity<Object> handleEntityNotFoundExceptions(EntityNotFoundException ex, WebRequest request) {
        *//*ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
                request.getDescription(false));*//*
        ApiResponse apiResponse = new ApiResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        return new ResponseEntity(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }*/
  @ExceptionHandler(EntityNotFoundException.class)
  public final ResponseEntity<Object> handleEntityNotFoundExceptions(EntityNotFoundException ex, WebRequest request) {
      ex.printStackTrace();
      ApiResponse apiResponse = new ApiResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
      return new ResponseEntity(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

    @ExceptionHandler(InvalidException.class)
    public final ResponseEntity<Object> handleInvalidExceptions(InvalidException ex, WebRequest request) {
        ex.printStackTrace();
        ApiResponse apiResponse = new ApiResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        return new ResponseEntity(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnAuthorisedException.class)
    public final ResponseEntity<Object> handleUnAthorisedExceptions(UnAuthorisedException ex, WebRequest request) {
        ex.printStackTrace();
        ApiResponse apiResponse = new ApiResponse(HttpStatus.FORBIDDEN, ex.getMessage(), null);
        return new ResponseEntity(apiResponse, HttpStatus.FORBIDDEN);
    }

//  @ExceptionHandler(InvalidFormatException.class)
//  public final ResponseEntity<Object> handleInvalidFormatException(InvalidFormatException ex, WebRequest request) {
//    ex.printStackTrace();
//    ApiResponse apiResponse = new ApiResponse(HttpStatus.FORBIDDEN, "Invalid Input Type", null);
//    return new ResponseEntity(apiResponse, HttpStatus.FORBIDDEN);
//  }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        String errorsString = errors.stream().map(e -> e.getDefaultMessage()).collect(Collectors.joining(","));
        ApiResponse apiResponse = new ApiResponse(HttpStatus.BAD_REQUEST, errorsString, null);
        return new ResponseEntity<>(apiResponse ,HttpStatus.BAD_REQUEST);
    }
}