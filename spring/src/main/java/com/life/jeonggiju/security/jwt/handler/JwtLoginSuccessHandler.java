package com.life.jeonggiju.security.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.life.jeonggiju.domain.user.dto.ErrorResponse;
import com.life.jeonggiju.security.dto.JwtLoginSuccessDto;
import com.life.jeonggiju.security.jwt.JwtTokenProvider;
import com.life.jeonggiju.security.jwt.registry.JwtRegistry;
import com.life.jeonggiju.security.jwt.registry.JwtRegistryInformation;
import com.life.jeonggiju.security.principal.LifeUserDetails;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.runtime.ObjectMethods;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final JwtRegistry jwtRegistry;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        if(authentication.getPrincipal() instanceof LifeUserDetails userDetails){
            try{
                String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
                String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

                Cookie refreshTokenCookie = jwtTokenProvider.generateRefreshTokenCookie(refreshToken);
                response.addCookie(refreshTokenCookie);

                JwtLoginSuccessDto dto = JwtLoginSuccessDto.builder().user(userDetails.getUserDto()).accessToken(accessToken).build();
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(objectMapper.writeValueAsString(dto));

                JwtRegistryInformation info = JwtRegistryInformation.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .userId(userDetails.getId()).build();
                jwtRegistry.registerJwtInformation(info);
            } catch(JOSEException e){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ErrorResponse errorResponse = new ErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR+"", e.getMessage());
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            }
        }else{
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ErrorResponse errorResponse = new ErrorResponse(HttpServletResponse.SC_UNAUTHORIZED+"", "Authentication failed");
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }

    }
}
