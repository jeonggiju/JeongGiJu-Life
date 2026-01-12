package com.life.jeonggiju.security.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.life.jeonggiju.security.api.handler.SpaCsrfTokenRequestHandler;
import com.life.jeonggiju.security.authentication.jwt.filter.JwtAuthenticationFilter;
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
		JwtAuthenticationFilter jwtAuthenticationFilter
	) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(SecurityPaths.PUBLIC_PATHS).permitAll()
				.requestMatchers(HttpMethod.POST, SecurityPaths.MethodSpecific.POST_ONLY).permitAll()
				.anyRequest().authenticated()
			)
			.formLogin(login -> login
				.loginProcessingUrl("/api/auth/sign-in")
				.successHandler(lifeLoginSuccessHandler)
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
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

}
