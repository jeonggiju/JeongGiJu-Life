package com.life.jeonggiju.security.api.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.user.entity.User;
import com.life.jeonggiju.domain.user.exception.UserNotFoundException;
import com.life.jeonggiju.domain.user.repository.UserRepository;
import com.life.jeonggiju.security.authentication.jwt.dto.JwtInformation;
import com.life.jeonggiju.security.authentication.jwt.exception.InValidAccessTokenException;
import com.life.jeonggiju.security.authentication.jwt.provider.JwtTokenProvider;
import com.life.jeonggiju.security.authentication.jwt.registry.JwtRegistry;
import com.life.jeonggiju.security.core.principal.LifeUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtRegistry jwtRegistry;
	private final JwtTokenProvider tokenProvider;
	private final UserDetailsService userDetailsService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public JwtInformation refreshToken(String refreshToken) {
		if (!tokenProvider.validateRefreshToken(refreshToken) || !jwtRegistry.hasActiveJwtInformationByRefreshToken(
			refreshToken)) {
			throw new InValidAccessTokenException();
		}

		String email = tokenProvider.getSubject(refreshToken);
		LifeUserDetails userDetails = (LifeUserDetails)userDetailsService.loadUserByUsername(email);

		String newAccessToken = tokenProvider.generateAccessToken(userDetails);
		String newRefreshToken = tokenProvider.generateRefreshToken(userDetails);

		JwtInformation newInfo = new JwtInformation(
			userDetails.getPrincipal(),
			newAccessToken,
			newRefreshToken
		);

		jwtRegistry.rotateJwtInformation(refreshToken, newInfo);
		return newInfo;
	}

	@Transactional
	public void resetPassword(String username, String email, String newPassword) {
		User user = userRepository.findByUsernameAndEmail(username, email)
			.orElseThrow(() -> UserNotFoundException.withEmail(email));

		String encode = passwordEncoder.encode(newPassword);
		user.setPassword(encode);
	}
}
