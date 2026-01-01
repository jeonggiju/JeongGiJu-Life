package com.study.jeonggiju.security.authentication;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LifeAuthenticationProvider implements AuthenticationProvider {

	private final LifeUserDetailService userDetailService;
	// private final PasswordEncoder passwordEncoder;

	@Override
	public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String email = authentication.getName();
		String password = authentication.getCredentials().toString();

		UserDetails userDetails = userDetailService.loadUserByUsername(email);

		// if(!passwordEncoder.matches(password, userDetails.getPassword())) {
		// 	throw new BadCredentialsException("비밀번호 불일치");
		// }
		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
