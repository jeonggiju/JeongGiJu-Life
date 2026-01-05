package com.life.jeonggiju.security.principal;

import com.life.jeonggiju.domain.user.entity.Authority;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LifeUserDto {
    private final UUID id;
    private final String email;
    private final String username;
    private final Authority authority;
}
