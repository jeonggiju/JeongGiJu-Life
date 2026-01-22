package com.life.jeonggiju.domain.user.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.life.jeonggiju.domain.user.dto.SignUpRequest;
import com.life.jeonggiju.domain.user.dto.UpdateUserRequest;
import com.life.jeonggiju.domain.user.dto.UserFindResponse;
import com.life.jeonggiju.domain.user.entity.Authority;
import com.life.jeonggiju.domain.user.entity.User;
import com.life.jeonggiju.domain.user.exception.UserNotFoundException;
import com.life.jeonggiju.domain.user.repository.UserRepository;
import com.life.jeonggiju.storage.BinaryStorage;
import com.life.jeonggiju.storage.exception.ProfileImageUploadException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final BinaryStorage binaryStorage;

	@Transactional
	public void signup(SignUpRequest dto) {
		User user = User.builder()
			.username(dto.getUsername())
			.title(dto.getTitle())
			.description(dto.getDescription())
			.email(dto.getEmail())
			.authority(Authority.ROLE_USER)
			.nickname(dto.getNickname())
			.birthYear(dto.getBirthYear())
			.birthMonth(dto.getBirthMonth())
			.birthDay(dto.getBirthDay())
			.password(passwordEncoder.encode(dto.getPassword())).build();

		userRepository.save(user);
	}

	@Transactional(readOnly = true)
	public UserFindResponse find(UUID id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> UserNotFoundException.withUserId(id));

		return UserFindResponse.builder()
			.username(user.getUsername())
			.email(user.getEmail())
			.title(user.getTitle())
			.nickname(user.getNickname())
			.description(user.getDescription())
			.profileImageUrl(user.getProfileImageUrl())
			.birthYear(user.getBirthYear())
			.birthMonth(user.getBirthMonth())
			.birthDay(user.getBirthDay())
			.build();
	}
	@Transactional
	public void update(UUID id, UpdateUserRequest dto, MultipartFile image) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> UserNotFoundException.withUserId(id));

		String profileImageUrl = null;
		if (image != null && !image.isEmpty()) {
			try {
				binaryStorage.put(id, image.getBytes());
				profileImageUrl = binaryStorage.getUrl(id);
			} catch (IOException e) {
				throw new ProfileImageUploadException(e);
			}
		}

		user.update(dto.getTitle(), dto.getDescription(), dto.getNickName(), profileImageUrl);
		userRepository.save(user);
	}

	@Transactional
	public void delete(UUID id) {
		userRepository.deleteById(id);
	}
}
