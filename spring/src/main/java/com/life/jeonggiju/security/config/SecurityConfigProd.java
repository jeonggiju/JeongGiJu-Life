package com.life.jeonggiju.security.config;

import com.life.jeonggiju.security.authentication.LifeAuthenticationProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Profile("prod")
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfigProd {

	private final LifeAuthenticationProvider authenticationProvider;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth
				// .requestMatchers("/swagger", "/swagger-ui.html", "/swagger-ui/**", "/api-docs", "/api-docs/**", "/v3/api-docs/**")
				// .permitAll()
				// .requestMatchers(
				// 	"/api/auth/**"
				// ).permitAll()
				// .requestMatchers("/api/**").authenticated()
				.anyRequest().permitAll()
			)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
			)
			.formLogin(form -> form.disable())
			.httpBasic(basic -> basic.disable())
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, authException) -> {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.setContentType("application/json;charset=UTF-8");
					response.getWriter().write("{\"error\": \"인증이 필요합니다\"}");
				})
			);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("*"));
		// configuration.setAllowedOrigins(List.of("http://localhost:3000"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		// configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		return new ProviderManager(authenticationProvider);
	}

}
