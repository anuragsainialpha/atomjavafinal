package com.api.apollo.atom;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import java.util.EnumSet;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class AtomApplication extends SpringBootServletInitializer implements WebApplicationInitializer {

  public static void main(String[] args) {
    SpringApplication.run(AtomApplication.class, args);
  }

  @Bean
  public static NoOpPasswordEncoder passwordEncoder() {
    return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
  }

  @Bean
  public FilterRegistrationBean springSecurityFilterChainRegistrationBean(@Qualifier("springSecurityFilterChain") Filter filter) {
    FilterRegistrationBean bean = new FilterRegistrationBean();
    bean.setFilter(filter);
    bean.setEnabled(false);
    return bean;
  }

  @Override
  public void onStartup(ServletContext context) throws ServletException {
    FilterRegistration.Dynamic registration =
        context.addFilter("springSecurityFilterChain", DelegatingFilterProxy.class);
    EnumSet<DispatcherType> dispatcherTypes =
        EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR, DispatcherType.ASYNC);
    registration.addMappingForUrlPatterns(dispatcherTypes, true, "/*");
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(AtomApplication.class);
  }

  @PostConstruct
  public void init(){
    // Setting Spring Boot SetTimeZone
    TimeZone.setDefault(TimeZone.getTimeZone("IST"));
  }

}
