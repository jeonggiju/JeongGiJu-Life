package com.study.jeonggiju.domain;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.auth.domain.CustomUserDetailService;
import com.study.jeonggiju.auth.domain.CustomUserDetails;

@RestController
@RequestMapping("/api/test")
public class TestController {

	@GetMapping
	public String test(@AuthenticationPrincipal CustomUserDetails userDetails) {

		if (userDetails == null) {
			return "인증 정보 없음";
		}

		return """
            === Authentication Details ===
            email: %s
            password: %s
            authorities: %s
            accountNonExpired: %s
            accountNonLocked: %s
            credentialsNonExpired: %s
            enabled: %s
            userId: %s
            """.formatted(
			userDetails.getUsername(),
			userDetails.getPassword(),
			userDetails.getAuthorities(),
			userDetails.isAccountNonExpired(),
			userDetails.isAccountNonLocked(),
			userDetails.isCredentialsNonExpired(),
			userDetails.isEnabled(),
			userDetails.getUser().getId()
		);
	}
}

