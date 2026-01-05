package com.life.jeonggiju.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.life.jeonggiju.security.authentication.LifeAuthenticationProvider;
import com.life.jeonggiju.security.fliter.JsonUsernamePasswordAuthenticationFilter;
import com.life.jeonggiju.security.fliter.JwtAuthenticationFilter;
import com.life.jeonggiju.security.handler.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Profile("dev")
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfigDev {

	private final LifeAuthenticationProvider authenticationProvider;

	@Bean
	public SecurityFilterChain filterChain(
			HttpSecurity http,
			ObjectMapper objectMapper,
			AuthenticationManager authenticationManager,
			JwtLoginSuccessHandler jwtLoginSuccessHandler,
			JwtLogoutHandler jwtLogoutHandler,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			LoginFailureHandler loginFailureHandler,
			Http403ForbiddenAccessDeniedHandler http403ForbiddenAccessDeniedHandler

	) throws Exception {
		http
				.authorizeHttpRequests(authorize -> authorize
						.anyRequest().permitAll()
				)
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/api/auth/logout")
						.ignoringRequestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api/**")
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
						.csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
				)
				.formLogin(AbstractHttpConfigurer::disable)
				.logout(logout->logout
						.logoutUrl("/api/auth/logout")
						.logoutSuccessHandler(jwtLogoutHandler)
				)
				.exceptionHandling(ex-> ex
						.authenticationEntryPoint(new Http403ForbiddenEntryPoint())
						.accessDeniedHandler(http403ForbiddenAccessDeniedHandler)
				)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				.addFilterAt(
						jsonUsernamePasswordAuthenticationFilter(objectMapper, authenticationManager, jwtLoginSuccessHandler, loginFailureHandler),
						JsonUsernamePasswordAuthenticationFilter.class
				)
				.addFilterBefore(
						jwtAuthenticationFilter,
						JsonUsernamePasswordAuthenticationFilter.class
				);
		http.cors(cors -> cors.configurationSource(request ->{
			CorsConfiguration config = new CorsConfiguration();
			config.addAllowedOriginPattern("*");
			config.addAllowedHeader("*");
			config.addAllowedMethod("*");
			config.setAllowCredentials(true);
			return config;
		}));

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		return new ProviderManager(authenticationProvider);
	}

	private JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordAuthenticationFilter(
			ObjectMapper objectMapper,
			AuthenticationManager authenticationManager,
			JwtLoginSuccessHandler jwtLoginSuccessHandler,
			LoginFailureHandler loginFailureHandler
	) {
		JsonUsernamePasswordAuthenticationFilter filter = new JsonUsernamePasswordAuthenticationFilter(objectMapper, authenticationManager);
		filter.setAuthenticationSuccessHandler(jwtLoginSuccessHandler);
		filter.setAuthenticationFailureHandler(loginFailureHandler);
		filter.setFilterProcessesUrl("/api/auth/login"); // 로그인 URL 설정
		return filter;
	}
}

