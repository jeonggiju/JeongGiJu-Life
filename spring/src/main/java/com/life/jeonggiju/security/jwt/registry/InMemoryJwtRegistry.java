package com.life.jeonggiju.security.jwt.registry;


import com.life.jeonggiju.security.jwt.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class InMemoryJwtRegistry implements JwtRegistry{

    private final Map<UUID, Queue<JwtRegistryInformation>> origin = new ConcurrentHashMap<>();
    private final Set<String> accessTokenIndexes = ConcurrentHashMap.newKeySet(); // 강제 로그아웃을 위해 access, refresh 둘 다 보관
    private final Set<String> refreshTokenIndexes = ConcurrentHashMap.newKeySet();

    private final int maxActiveJwtCount = 3;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void registerJwtInformation(JwtRegistryInformation information) {
        origin.compute(information.getUserId(), (key, queue)->{
            if(queue == null){
                queue = new ConcurrentLinkedQueue<>();
            }
            if(queue.size() >= maxActiveJwtCount){
                JwtRegistryInformation deprecated = queue.poll();
                if(deprecated != null){
                    this.removeTokenIndex(deprecated.getAccessToken(), deprecated.getRefreshToken());
                }
            }

            queue.add(information);
            addTokenIndex(information.getAccessToken(), information.getRefreshToken());
            return queue;
        });
    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {
        origin.computeIfPresent(userId, (key, queue)->{
            queue.forEach(information -> {
                this.removeTokenIndex(information.getAccessToken(), information.getRefreshToken());
            });

            queue.clear();
            return null;
        });
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        return origin.containsKey(userId);
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        return accessTokenIndexes.contains(accessToken);
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        return refreshTokenIndexes.contains(refreshToken);
    }

    @Override
    public void rotateJwtInformationByRefreshToken(String refreshToken, JwtRegistryInformation newInformation) {
        origin.computeIfPresent(newInformation.getUserId(), (key, queue)->{
            queue.stream().filter(information -> information.getRefreshToken().equals(refreshToken))
                    .findFirst()
                    .ifPresent(information -> {
                        removeTokenIndex(information.getAccessToken(), information.getRefreshToken());
                        information.rotate(newInformation.getAccessToken(), newInformation.getRefreshToken());
                        addTokenIndex(information.getAccessToken(), information.getRefreshToken());
                    });
            return queue;
        });
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Override
    public void clearExpiredJwtInformation() {
        origin.entrySet().removeIf(entry -> {
            Queue<JwtRegistryInformation> queue = entry.getValue();
            queue.removeIf(jwtInformation -> {
                boolean isExpired = !jwtTokenProvider.validateRefreshToken(jwtInformation.getRefreshToken()); // 메모리 관리는 refresh로만
                if (isExpired) {
                    removeTokenIndex(
                            jwtInformation.getAccessToken(),
                            jwtInformation.getRefreshToken()
                    );
                }
                return isExpired;
            });

            return queue.isEmpty();
        });
    }

    private void addTokenIndex(String accessToken, String refreshToken) {
        accessTokenIndexes.add(accessToken);
        refreshTokenIndexes.add(refreshToken);
    }


    private void removeTokenIndex(String accessToken, String refreshToken) {
        accessTokenIndexes.remove(accessToken);
        refreshTokenIndexes.remove(refreshToken);
    }
}
