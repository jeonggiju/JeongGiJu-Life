package com.life.jeonggiju.security.core.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.life.jeonggiju.security.api.handler.SpaCsrfTokenRequestHandler;
import com.life.jeonggiju.security.authentication.jwt.filter.JwtAuthenticationFilter;
import com.life.jeonggiju.security.authentication.jwt.handler.JwtAuthenticationEntryPoint;
import com.life.jeonggiju.security.authentication.local.handler.LifeLoginSuccessHandler;
import com.life.jeonggiju.security.authentication.local.handler.LifeLogoutSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http,
		LifeLoginSuccessHandler lifeLoginSuccessHandler,
		LifeLogoutSuccessHandler lifeLogoutSuccessHandler,
		JwtAuthenticationFilter jwtAuthenticationFilter,
		JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
	) throws Exception {
		http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(SecurityPaths.PUBLIC_PATHS).permitAll()
				.requestMatchers(HttpMethod.POST, SecurityPaths.MethodSpecific.POST_ONLY).permitAll()
				.anyRequest().authenticated()
			)
			.formLogin(login -> login
				.loginProcessingUrl("/api/auth/sign-in")
				.successHandler(lifeLoginSuccessHandler)
			)
			.securityContext(context ->
				context.requireExplicitSave(false)
			)
			.logout(logout -> logout
				.logoutUrl("/api/auth/sign-out")
				.logoutSuccessHandler(lifeLogoutSuccessHandler)
			)
			.csrf(csrf -> csrf
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
			)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(jwtAuthenticationEntryPoint)
			)
			.addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setExposedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

}
