package com.study.jeonggiju.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(apiInfo())
			.servers(List.of(
				new Server().url("http://localhost:8080").description("Local")
			));
	}

	private Info apiInfo() {
		return new Info()
			.title("JeongGiJu")
			.description("JeongGiJu API")
			.version("v1.0.0")
			.contact(new Contact()
				.name("정기주")
				.email("halogiju123@gmail.com"));
	}
}
