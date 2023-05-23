package com.api.apollo.atom.controller;


import com.api.apollo.atom.dto.ops.IndentInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.ops.IndentFilterDto;
import com.api.apollo.atom.service.TransporterService;
import com.api.apollo.atom.util.Utility;

import io.swagger.annotations.Api;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.Valid;

@Api(value = "FGS - Transporter")
@RestController
@RequestMapping("/api/v1/transporter/")
public class TransportController  {

    @Autowired
    TransporterService transporterService;


    @PostMapping(value = "indents")
    public ResponseEntity<ApiResponse> getIndents(@RequestBody IndentFilterDto indentSearchDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(transporterService.getIndents(indentSearchDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }

    @PostMapping(value = "update-indent")
    public  ResponseEntity<ApiResponse> updateIndent(@Valid  @RequestBody IndentInfoDto indentInfoDto, Authentication authentication) throws Exception {
        return ResponseEntity.ok(transporterService.updateIndents(indentInfoDto, Utility.getApplicationUserFromAuthentication(authentication)));
    }




}


