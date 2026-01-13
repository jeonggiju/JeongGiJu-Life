package com.life.jeonggiju.security.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.life.jeonggiju.security.authentication.local.provider.LifeAuthenticationProvider;

@Configuration
public class AuthConfig {

	@Bean
	public AuthenticationManager authenticationManager(LifeAuthenticationProvider authenticationProvider) {
		return new ProviderManager(authenticationProvider);
	}

	@Bean("passwordEncoder")
	@Profile({"local", "dev"})
	public PasswordEncoder devPasswordEncoder() {
		return new PlainTextPasswordEncoder();
	}

	@Bean("passwordEncoder")
	@Profile("prod")
	public PasswordEncoder prodPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	public static class PlainTextPasswordEncoder implements PasswordEncoder {

		@Override
		public String encode(CharSequence rawPassword) {
			return rawPassword == null ? null : rawPassword.toString();
		}

		@Override
		public boolean matches(CharSequence rawPassword, String encodedPassword) {
			if (rawPassword == null || encodedPassword == null) {
				return false;
			}
			return rawPassword.toString().equals(encodedPassword);
		}
	}
}
