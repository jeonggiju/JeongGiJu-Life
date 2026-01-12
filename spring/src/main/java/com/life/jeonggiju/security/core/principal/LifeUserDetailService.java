package com.life.jeonggiju.security.core.principal;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.life.jeonggiju.domain.user.entity.User;
import com.life.jeonggiju.domain.user.exception.UserNotFoundException;
import com.life.jeonggiju.domain.user.repository.UserRepository;
import com.life.jeonggiju.security.core.dto.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LifeUserDetailService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(username).orElseThrow(() -> UserNotFoundException.withEmail(username));

		UserPrincipal principal = UserPrincipal.builder()
			.userId(user.getId())
			.username(user.getUsername())
			.email(user.getEmail())
			.title(user.getTitle())
			.authority(user.getAuthority()).build();

		return new LifeUserDetails(principal, user.getPassword());
	}
}
