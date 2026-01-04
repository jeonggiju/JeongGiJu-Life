package com.life.jeonggiju.security.authentication;

import com.life.jeonggiju.security.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.life.jeonggiju.domain.user.entity.User;
import com.life.jeonggiju.domain.user.repository.UserRepository;
import com.life.jeonggiju.security.principal.LifeUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LifeUserDetailService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

		UserDto dto = UserDto.builder()
				.id(user.getId())
				.title(user.getTitle())
				.description(user.getDescription())
				.email(user.getEmail())
				.birthYear(user.getBirthYear())
				.birthMonth(user.getBirthMonth())
				.birthDay(user.getBirthDay())
				.username(user.getUsername())
				.password(user.getPassword())
				.authority(user.getAuthority())
				.build();

		return new LifeUserDetails(dto);

	}
}
