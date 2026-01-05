package com.life.jeonggiju.security.fliter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.life.jeonggiju.domain.user.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

@RequiredArgsConstructor
public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if(!request.getMethod().equals("POST")){
            throw new RuntimeException("Only POST method is allowed");
        }

        try{
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

            setDetails(request, authenticationToken);
            return this.getAuthenticationManager().authenticate(authenticationToken);
        }catch(Exception e){
            throw new AuthenticationServiceException("Bad credentials");
        }
    }

    public static class Configurer extends AbstractAuthenticationFilterConfigurer<HttpSecurity, Configurer, JsonUsernamePasswordAuthenticationFilter>{

        public static final String DEFAULT_LOGIN_URL ="/api/auth/login";

        public Configurer(ObjectMapper objectMapper) {
            super(new JsonUsernamePasswordAuthenticationFilter(objectMapper), DEFAULT_LOGIN_URL);
        }

        @Override
        protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
            return request ->
                    request.getRequestURI().equals(loginProcessingUrl)
                            && request.getMethod().equals("POST");
        }

        @Override
        public void init(HttpSecurity http) throws Exception {
            loginProcessingUrl(DEFAULT_LOGIN_URL);
            super.init(http);
        }
    }
}
