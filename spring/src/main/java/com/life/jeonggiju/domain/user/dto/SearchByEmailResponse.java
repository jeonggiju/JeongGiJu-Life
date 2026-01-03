package com.life.jeonggiju.domain.user.dto;

import java.util.UUID;

import org.springframework.web.bind.annotation.BindParam;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchByEmailResponse {
	private UUID id;
	private String email;
	private String username;
}
