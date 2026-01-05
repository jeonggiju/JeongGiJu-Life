package com.life.jeonggiju.security.fliter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.life.jeonggiju.exception.dto.ErrorResponse;
import com.life.jeonggiju.security.jwt.provider.JwtTokenProvider;
import com.life.jeonggiju.security.jwt.registry.JwtRegistry;
import com.life.jeonggiju.security.jwt.token.AccessTokenJwt;
import com.life.jeonggiju.security.principal.LifeUserDetails;
import com.life.jeonggiju.security.principal.LifeUserDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final JwtRegistry jwtRegistry;

    @Override
    public boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();

        return path.equals("/api/auth/refresh") || path.equals("/api/auth/login") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if(StringUtils.hasText(token)){
                if(jwtTokenProvider.validateAccessToken(token) && jwtRegistry.hasActiveJwtInformationByAccessToken(token)){
                    AccessTokenJwt accessTokenJwt = jwtTokenProvider.parseAccessToken(token);
                    AccessTokenJwt.AccessTokenUserInfo user = accessTokenJwt.getUser();
                    LifeUserDto principal = LifeUserDto.builder()
                            .id(user.getUserId())
                            .email(user.getUserEmail())
                            .authority(user.getAuthority())
                            .password(null)
                            .username(user.getUsername()).build();

                    LifeUserDetails userDetails = new LifeUserDetails(principal);
                    UsernamePasswordAuthenticationToken authentication= new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.debug("Invalid JWT token");
                    sendErrorResponse(response, "Invalid JWT token", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
        } catch (Exception e) {
            log.error("Authentication failed. {}", e.getMessage());
            SecurityContextHolder.clearContext();
            sendErrorResponse(response, "Authentication failed.", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse("" + status, message);
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
