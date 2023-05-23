package com.api.apollo.atom.config;

import java.time.LocalDate;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("ATOM API").description("API Reference").version("1.0.0").build();
	}

	@Bean
	public Docket customImplementation() {
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
				.securitySchemes(Collections.singletonList(authorizationKey())).select().paths(PathSelectors.any())
				// .apis(RequestHandlerSelectors.any()) // If you want to list all the apis
				// including springboots own
				.apis(RequestHandlerSelectors.basePackage("com.api.apollo.atom")).build().pathMapping("/")
				.useDefaultResponseMessages(false).directModelSubstitute(LocalDate.class, String.class)
				.genericModelSubstitutes(ResponseEntity.class);
	}

	private ApiKey authorizationKey() {
		// return new ApiKey("Authorization", "api_key", "header");
		return new ApiKey("Authorization", "Authorization", "header"); // <<< === Create a Heaader (We are createing
																		// header named "Authorization" here)
	}

}
