package com.study.jeonggiju.domain.user.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.study.jeonggiju.domain.user.dto.UpdateUser;
import com.study.jeonggiju.domain.user.dto.UserInfo;
import com.study.jeonggiju.domain.user.repository.UserRepository;
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
			.birthYear(dto.getBirthYear())
			.birthMonth(dto.getBirthMonth())
			.birthDay(dto.getBirthDay())
			.password(passwordEncoder.encode(dto.getPassword())).build();

		userRepository.save(user);
	}

	public UserInfo find(UUID id){
		User user = userRepository.findById(id).orElseThrow();

		return UserInfo.builder()
			.username(user.getUsername())
			.email(user.getEmail())
			.title(user.getTitle())
			.description(user.getDescription())
			.build();
	}

	public void update(UUID id,UpdateUser dto){
		User user = userRepository.findById(id).orElseThrow();
		user.update(dto.getTitle(), dto.getDescription());
		userRepository.save(user);
	}

	public void delete(UUID id){
		userRepository.deleteById(id);
	}
}
