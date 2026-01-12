package com.life.jeonggiju.security.authentication.local.provider;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.life.jeonggiju.security.authentication.local.exception.InvalidCredentialsException;
import com.life.jeonggiju.security.core.principal.LifeUserDetailService;
import com.life.jeonggiju.security.core.principal.LifeUserDetails;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LifeAuthenticationProvider implements AuthenticationProvider {

	private final LifeUserDetailService userDetailService;
	private final PasswordEncoder passwordEncoder;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String email = authentication.getName();
		String password = authentication.getCredentials().toString();
		LifeUserDetails userDetails = (LifeUserDetails)userDetailService.loadUserByUsername(email);

		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			InvalidCredentialsException.withPassword();
		}

		userDetails.invalidatePassword();

		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
