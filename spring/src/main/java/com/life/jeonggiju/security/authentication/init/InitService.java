package com.life.jeonggiju.security.authentication.init;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.user.entity.Authority;
import com.life.jeonggiju.domain.user.entity.User;
import com.life.jeonggiju.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InitService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	@Value("${init.admin.password}")
	private String adminPassword;
	@Value("${init.admin.email}")
	private String adminEmail;
	@Value("${init.user.password}")
	private String userPassword;
	@Value("${init.user.email}")
	private String userEmail;

	@Transactional
	public void initAdmin() {
		if (userRepository.existsByEmail(adminEmail)) {
			return;
		}

		String encodedPassword = passwordEncoder.encode(adminPassword);
		User user = User.builder()
			.email(adminEmail)
			.username("정기주")
			.password(encodedPassword)
			.title("testTitle")
			.description("testDesc")
			.authority(Authority.ROLE_USER)
			.birthDay(1999)
			.birthMonth(6)
			.birthDay(8).build();

		userRepository.save(user);
	}

	@Transactional
	public void initDefaultUser() {
		if (userRepository.existsByEmail(userEmail)) {
			return;
		}

		String encodedPassword = passwordEncoder.encode(userPassword);
		User user = User.builder()
			.email(userEmail)
			.username("정기주")
			.password(encodedPassword)
			.title("testTitle")
			.description("testDesc")
			.authority(Authority.ROLE_USER)
			.birthDay(1999)
			.birthMonth(6)
			.birthDay(8).build();

		userRepository.save(user);
	}
}
