package com.study.jeonggiju.domain.user.dto;

import java.util.List;

import com.study.jeonggiju.domain.category.entity.Category;
import com.study.jeonggiju.domain.user.entity.Authority;

import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest {
	private String email;
	private String password;
}
