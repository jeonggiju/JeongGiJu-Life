package com.life.jeonggiju.auth.service;

import com.life.jeonggiju.security.authentication.LifeUserDetailService;
import com.life.jeonggiju.security.jwt.provider.JwtTokenProvider;
import com.life.jeonggiju.security.jwt.registry.JwtRegistry;
import com.life.jeonggiju.security.jwt.registry.JwtRegistryInformation;
import com.life.jeonggiju.security.jwt.token.RefreshTokenJwt;
import com.life.jeonggiju.security.principal.LifeUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final JwtRegistry jwtRegistry;
    private final JwtTokenProvider jwtTokenProvider;
    private final LifeUserDetailService userDetailService;

    public JwtRegistryInformation refreshToken(String refreshToken){
        if(!jwtTokenProvider.validateRefreshToken(refreshToken) || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)){
            throw new RuntimeException("Invalid Refresh Token");
        }

        RefreshTokenJwt refreshTokenJwt = jwtTokenProvider.parseRefreshToken(refreshToken);
        LifeUserDetails userDetails = (LifeUserDetails)userDetailService.loadUserByUsername(refreshTokenJwt.getUser().getUserEmail());

        if(userDetails == null){
            throw new RuntimeException("User Not Found");
        }

        try{
            String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            JwtRegistryInformation jwtInfo = JwtRegistryInformation.builder()
                    .userId(userDetails.getId())
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();
            jwtRegistry.rotateJwtInformationByRefreshToken(refreshToken, jwtInfo);
            return jwtInfo;
        } catch(Exception e){
            log.error("Failed to generate new tokens for user: {}", refreshTokenJwt.getUser().getUserEmail(), e);
            throw new RuntimeException("INTERNAL_SERVER_ERROR");
        }
    }
}
