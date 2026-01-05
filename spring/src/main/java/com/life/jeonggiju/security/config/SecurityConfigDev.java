package com.life.jeonggiju.security.config;

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
			JwtLoginSuccessHandler jwtLoginSuccessHandler,
			JwtLogoutHandler jwtLogoutHandler,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			LoginFailureHandler loginFailureHandler,
			Http403ForbiddenAccessDeniedHandler http403ForbiddenAccessDeniedHandler

	) throws Exception {
		http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/api/auth/login", "/error", "/").permitAll()
						.requestMatchers("/index.html", "/manage.html", "/insert.html", "/friend.html").permitAll()
						.requestMatchers("/swagger-ui/**", "/v3/api-docs/**","/swagger-ui.html").permitAll()

						.requestMatchers("/api/auth/csrf-token").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/users").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
						.requestMatchers("/api/**").authenticated()
				)
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/api/auth/logout")
						.ignoringRequestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api/**")
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
						.csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
				)
				.formLogin(login -> login
						.loginProcessingUrl("/api/auth/login")
						.successHandler(jwtLoginSuccessHandler)
						.failureHandler(loginFailureHandler)
				)
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
}


//package com.life.jeonggiju.security.config;
//
//import java.util.List;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.ProviderManager;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import com.life.jeonggiju.security.authentication.LifeAuthenticationProvider;
//
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//
//@Profile("dev")
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfigDev {
//
//	private final LifeAuthenticationProvider authenticationProvider;
//
//	@Bean
//	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//		http
//				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
//				.csrf(csrf -> csrf.disable())
//				.authorizeHttpRequests(auth -> auth
//						.anyRequest().permitAll()
//				)
//				.sessionManagement(session -> session
//						.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//				)
//				.formLogin(form -> form.disable())
//				.httpBasic(basic -> basic.disable())
//				.exceptionHandling(exception -> exception
//						.authenticationEntryPoint((request, response, authException) -> {
//							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//							response.setContentType("application/json;charset=UTF-8");
//							response.getWriter().write("{\"error\": \"인증이 필요합니다\"}");
//						})
//				);
//
//		return http.build();
//	}
//
//	@Bean
//	public CorsConfigurationSource corsConfigurationSource() {
//		CorsConfiguration configuration = new CorsConfiguration();
//		configuration.setAllowedOrigins(List.of("*"));
//		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//		configuration.setAllowedHeaders(List.of("*"));
//
//		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//		source.registerCorsConfiguration("/**", configuration);
//		return source;
//	}
//
//	@Bean
//	public AuthenticationManager authenticationManager() {
//		return new ProviderManager(authenticationProvider);
//	}
//
//}
