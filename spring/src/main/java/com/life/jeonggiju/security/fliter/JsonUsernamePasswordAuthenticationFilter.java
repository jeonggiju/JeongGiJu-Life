package com.life.jeonggiju.security.fliter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.life.jeonggiju.domain.user.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;
    public static final String DEFAULT_LOGIN_URL ="/api/auth/login";

    public JsonUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper, AuthenticationManager authenticationManager) {
        this.objectMapper = objectMapper;
        setAuthenticationManager(authenticationManager);
        setFilterProcessesUrl(DEFAULT_LOGIN_URL);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if(!request.getMethod().equals("POST")){
            throw new RuntimeException("Only POST method is allowed");
        }

        try{
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
            log.info("Authentication attempt with email: {}", loginRequest.getEmail());
            log.info("Authentication attempt with password: {}", loginRequest.getPassword());

            setDetails(request, authenticationToken);
            return this.getAuthenticationManager().authenticate(authenticationToken);
        }catch(Exception e){
            throw new AuthenticationServiceException("Bad credentials");
        }
    }
}
