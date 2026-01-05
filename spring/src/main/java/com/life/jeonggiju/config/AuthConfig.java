package com.life.jeonggiju.config;

import com.life.jeonggiju.security.jwt.provider.JwtTokenProvider;
import com.life.jeonggiju.security.jwt.registry.InMemoryJwtRegistry;
import com.life.jeonggiju.security.jwt.registry.JwtRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Configuration
public class AuthConfig {


	@Bean
	public PasswordEncoder passwordEncoder() {
		return new PlainTextPasswordEncoder();
		// return new BCryptPasswordEncoder();
	}

	@Bean
	public CommandLineRunner debugFilterChain(SecurityFilterChain filterChain) {
		return args -> {
			int filterSize = filterChain.getFilters().size();
			List<String> filterNames = IntStream.range(0, filterSize)
					.mapToObj(idx -> String.format("\t[%s/%s] %s", idx + 1, filterSize,
							filterChain.getFilters().get(idx).getClass()))
					.toList();
			log.debug("Debug Filter Chain...\n{}", String.join(System.lineSeparator(), filterNames));
		};
	}


	public static class PlainTextPasswordEncoder implements PasswordEncoder {
		@Override
		public String encode(CharSequence rawPassword) {
			return rawPassword.toString();
		}

		@Override
		public boolean matches(CharSequence rawPassword, String encodedPassword) {
			return rawPassword.toString().equals(encodedPassword);
		}
	}
}
