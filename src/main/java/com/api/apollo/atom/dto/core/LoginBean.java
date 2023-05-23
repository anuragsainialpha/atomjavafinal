package com.api.apollo.atom.dto.core;

import org.springframework.lang.NonNull;

import lombok.Data;

@Data
public class LoginBean {
	
  @NonNull
  private String userId = "";
  
  @NonNull
  private String password = "";
}
