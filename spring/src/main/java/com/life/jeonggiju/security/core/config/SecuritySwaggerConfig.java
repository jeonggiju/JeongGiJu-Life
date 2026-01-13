package com.life.jeonggiju.security.core.config;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.life.jeonggiju.security.authentication.jwt.dto.JwtDto;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;

@Configuration
public class SecuritySwaggerConfig {

	@Bean
	public OpenApiCustomizer securityEndpointsCustomizer() {
		return (OpenAPI openApi) -> {

			// 1) POST /api/auth/sign-in  (formLogin loginProcessingUrl)
			Operation signIn = new Operation()
				.summary("로그인 (Spring Security formLogin)")
				.description("""
					Spring Security의 formLogin 필터가 처리합니다.
					성공 시 SuccessHandler에서 Access/Refresh 토큰 처리(쿠키/바디 등)를 수행합니다.
					""")
				.responses(new io.swagger.v3.oas.models.responses.ApiResponses()
					.addApiResponse("200", new ApiResponse()
						.description("로그인 성공")
						.content(new Content().addMediaType(
							"application/json",
							new MediaType().schema(
								new Schema<JwtDto>().$ref("#/components/schemas/JwtDto")
							)
						))
					)
					.addApiResponse("401", new ApiResponse()
						.description("아이디 또는 비밀번호 불일치")
					)
				);

			openApi.path("/api/auth/sign-in", new PathItem().post(signIn));

			// 2) POST /api/auth/sign-out (logoutUrl)
			Operation signOut = new Operation()
				.summary("로그아웃 (Spring Security logout)")
				.description("""
					Spring Security의 logout 필터가 처리합니다.
					성공 시 LogoutSuccessHandler에서 쿠키 삭제/토큰 블랙리스트 처리 등을 수행합니다.
					""")
				.responses(new io.swagger.v3.oas.models.responses.ApiResponses()
					.addApiResponse("200", new ApiResponse().description("로그아웃 성공"))
					.addApiResponse("401", new ApiResponse().description("인증 필요"))
				);

			openApi.path("/api/auth/sign-out", new PathItem().post(signOut));
		};
	}
}
