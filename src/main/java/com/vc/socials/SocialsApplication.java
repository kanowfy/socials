package com.vc.socials;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SecurityScheme(type = SecuritySchemeType.HTTP, name = "basic_auth", scheme = "basic")
@OpenAPIDefinition(info = @Info(title = "Socials application API", version = "1.0.0"), security = {
		@SecurityRequirement(name = "basic_auth") })
@SpringBootApplication
public class SocialsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialsApplication.class, args);
	}

}
