package com.life.jeonggiju.security.jwt.registry;


import com.life.jeonggiju.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class InMemoryJwtRegistry implements JwtRegistry{

    private final Map<UUID, Queue<JwtRegistry>> origin = new ConcurrentHashMap<>();
    private final Set<String> accessTokenIndexes = ConcurrentHashMap.newKeySet();
    private final Set<String> refreshTokenIndexes = ConcurrentHashMap.newKeySet();

    private final int maxActiveJwtCount;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void registerJwtInformation(JwtRegistryInformation information) {

    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {

    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        return false;
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        return false;
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        return false;
    }

    @Override
    public void rotateJwtInformationByRefreshToken(String refreshToken, JwtRegistryInformation newInformation) {

    }

    @Override
    public void clearExpiredJwtInformation() {

    }
}
