package com.study.jeonggiju.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.study.jeonggiju.auth.domain.UserRepository;
import com.study.jeonggiju.domain.user.dto.SignUpRequest;
import com.study.jeonggiju.domain.user.entity.Authority;
import com.study.jeonggiju.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public void signup(SignUpRequest dto){
		User user = User.builder()
			.username(dto.getUsername())
			.title(dto.getTitle())
			.description(dto.getDescription())
			.email(dto.getEmail())
			.authority(Authority.ROLE_USER)
			.password(passwordEncoder.encode(dto.getPassword())).build();
		userRepository.save(user);
	}
}
