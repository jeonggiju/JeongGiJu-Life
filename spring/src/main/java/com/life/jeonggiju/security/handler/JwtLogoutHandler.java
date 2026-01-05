package com.life.jeonggiju.security.handler;

import com.life.jeonggiju.security.jwt.provider.JwtTokenProvider;
import com.life.jeonggiju.security.jwt.registry.JwtRegistry;
import com.life.jeonggiju.security.principal.LifeUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Cookie expireCookie = jwtTokenProvider.generateExpiredRefreshTokenCookie();
        response.addCookie(expireCookie);

        try{
            jwtRegistry.invalidateJwtInformationByUserId(((LifeUserDetails)authentication.getPrincipal()).getId());
        } catch(Exception e){
            log.error("Logout Error", e);
        }
    }
}
