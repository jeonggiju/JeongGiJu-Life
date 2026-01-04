package com.life.jeonggiju.security.dto;

import com.life.jeonggiju.domain.user.entity.Authority;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserDto {
    private UUID id;

    private String email;

    private String username;

    private String password;

    private String title;

    private String description;

    private Authority authority;

    private int birthYear;

    private int birthMonth;

    private int birthDay;

}
