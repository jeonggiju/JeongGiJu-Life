package com.life.jeonggiju.security.authentication.jwt.filter;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.life.jeonggiju.security.authentication.jwt.exception.InValidAccessTokenException;
import com.life.jeonggiju.security.authentication.jwt.provider.JwtTokenProvider;
import com.life.jeonggiju.security.authentication.jwt.registry.JwtRegistry;
import com.life.jeonggiju.security.core.config.SecurityPaths;
import com.life.jeonggiju.security.core.principal.LifeUserDetails;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";
	private static final int BEARER_PREFIX_LENGTH = 7;

	private final JwtTokenProvider tokenProvider;
	private final JwtRegistry jwtRegistry;
	private final UserDetailsService userDetailsService;

	@Override
	protected boolean shouldNotFilterAsyncDispatch() {
		return false;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		String method = request.getMethod();
		DispatcherType dispatcherType = request.getDispatcherType();

		if (dispatcherType == DispatcherType.ASYNC) {
			return false;
		}

		return SecurityPaths.isPublicPath(path, method);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		DispatcherType dispatcherType = request.getDispatcherType();

		log.debug("=== JWT Filter ===");
		log.debug("Path: {}", request.getRequestURI());
		log.debug("DispatcherType: {}", dispatcherType);

		if (dispatcherType == DispatcherType.ASYNC) {
			Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

			if (existingAuth != null && existingAuth.isAuthenticated()
				&& !(existingAuth instanceof AnonymousAuthenticationToken)) {
				filterChain.doFilter(request, response);
				return;
			}

			filterChain.doFilter(request, response);
			return;
		}

		if (dispatcherType == DispatcherType.ERROR) {
			filterChain.doFilter(request, response);
			return;
		}

		String authHeader = request.getHeader(AUTHORIZATION_HEADER);

		if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
			log.warn("Missing or invalid Authorization header for path: {}", request.getRequestURI());
			throw new InValidAccessTokenException("Authorization 헤더가 없거나 유효하지 않습니다.");
		}

		String accessToken = authHeader.substring(BEARER_PREFIX_LENGTH);

		if (!tokenProvider.validateAccessToken(accessToken)) {
			log.warn("Invalid or expired access token for path: {}", request.getRequestURI());
			throw new InValidAccessTokenException("액세스 토큰이 유효하지 않습니다.");
		}

		boolean hasToken = jwtRegistry.hasActiveJwtInformationByAccessToken(accessToken);

		if (!hasToken) {
			log.warn("Access token not found in registry for path: {}", request.getRequestURI());
			throw new InValidAccessTokenException("토큰이 레지스트리에 없습니다.");
		}

		String email = tokenProvider.getSubject(accessToken);

		LifeUserDetails userDetails = (LifeUserDetails)userDetailsService.loadUserByUsername(email);

		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails, null, userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}
}