package com.life.jeonggiju.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class RefreshResponse {
    private UUID userId;
    private String accessToken;
}
