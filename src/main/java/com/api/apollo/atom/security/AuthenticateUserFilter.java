package com.api.apollo.atom.security;

import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.core.UserBean;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.repository.UserRepository;
import com.api.apollo.atom.util.Utility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

/* This filter maps to /session and tries to validate the user id and password */
public class AuthenticateUserFilter extends AbstractAuthenticationProcessingFilter {

  private TokenUtilService tokenUtilService;
  private UserRepository userRepository;
  //private BCryptPasswordEncoder bCryptPasswordEncoder;


  private Environment environment;

  public AuthenticateUserFilter(String urlMapping, AuthenticationManager authenticationManager,
                                TokenUtilService tokenUtilService, UserRepository userRepository, Environment environment) {
    super(new AntPathRequestMatcher(urlMapping));
    setAuthenticationManager(authenticationManager);
    this.tokenUtilService = tokenUtilService;
    this.userRepository = userRepository;
    this.environment = environment;
    //this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, IOException {
    try {
      String jsonString = Utility.toString(request.getInputStream(), "UTF-8");
      JSONObject userJSON = new JSONObject(jsonString);
      String username = userJSON.getString("userId");
      String password = userJSON.getString("password");
      if (StringUtils.isEmpty(username) && StringUtils.isEmpty(password))
        return setErrorResponse(response, "user id and password are required", HttpStatus.BAD_REQUEST, 0);
      ApplicationUser user = userRepository.findOneByUserIdIgnoreCase(username)
          .orElseThrow(() -> new UsernameNotFoundException(String.format("user id %s not found", username)));
      byte[] base64Password = Base64.getEncoder().encode((username + ":" + password).getBytes());
      if (!(new String(base64Password).equals(user.getPassword())))
        return setErrorResponse(response, "Invalid credentials !", HttpStatus.UNAUTHORIZED, 0);
      UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, new String(base64Password));
      user.setLastLoginDate(Utility.currentTimestamp());
      userRepository.save(user);
      return getAuthenticationManager().authenticate(authToken);
    } catch (Throwable e) {
      e.printStackTrace();
      if (e instanceof BadCredentialsException)
        return setErrorResponse(response, "Invalid credentials !", HttpStatus.UNAUTHORIZED, 0);
      else if (e instanceof UsernameNotFoundException)
        return setErrorResponse(response, e.getMessage(), HttpStatus.NOT_FOUND, 0);
      else if (e instanceof InvalidDataAccessApiUsageException)
        return setErrorResponse(response, "Access Denied", HttpStatus.NOT_FOUND, 0);
      throw new AuthenticationServiceException(e.getMessage());
    }
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
                                          Authentication authentication) throws IOException, ServletException {
    SecurityContextHolder.getContext().setAuthentication(authentication);
    LoginUser tokenUser = (LoginUser) authentication.getPrincipal();
    String tokenString = this.tokenUtilService.createTokenForUser(tokenUser);
    String persistenceInstance = environment.getProperty("spring.profiles.active");
    setLoginResponse(res, tokenUser, tokenString, persistenceInstance);
  }

  private void setLoginResponse(HttpServletResponse res, LoginUser tokenUser, String tokenString, String persistenceInstance) throws IOException {
    UserBean resp = new UserBean();
    resp.setFirstName(tokenUser.getUser().getFirstName());
    resp.setLastName(tokenUser.getUser().getLastName());
    resp.setEmail(tokenUser.getUser().getEmail());
    resp.setToken(tokenString);
    resp.setRole(tokenUser.getRole());
    resp.setUserId(tokenUser.getUser().getUserId());
    resp.setSourceId(tokenUser.getUser().getPlantCode());
    resp.setIsExtWarehouse(tokenUser.getUser().getIsExtWarehouse());
    resp.setPersistenceInstance(persistenceInstance);

    ApiResponse apiResponse = new ApiResponse();
    apiResponse.setStatusCode(HttpServletResponse.SC_OK);
    apiResponse.setMessage("successfully login");
    apiResponse.setTimestamp(System.currentTimeMillis());
    apiResponse.setData(resp);
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    String jsonRespString = ow.writeValueAsString(apiResponse);
    res.setStatus(HttpServletResponse.SC_OK);
    res.setContentType("application/json");
    res.getWriter().write(jsonRespString);
    res.getWriter().flush();
    res.getWriter().close();
  }

  private Authentication setErrorResponse(HttpServletResponse response, String message, HttpStatus httpStatus,
                                          int customCode) throws IOException {
    response.setStatus(httpStatus.value());
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    ApiResponse apiResponse = new ApiResponse();
    if (customCode != 0) {
      apiResponse.setStatusCode(customCode);
    } else {
      apiResponse.setStatusCode(httpStatus.value());
    }
    apiResponse.setTimestamp(System.currentTimeMillis());
    apiResponse.setMessage(message);
    String jsonRespString = ow.writeValueAsString(apiResponse);
    response.setContentType("application/json");
    response.getWriter().write(jsonRespString);
    response.getWriter().flush();
    response.getWriter().close();
    return null;
  }
}
