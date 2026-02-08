package com.life.jeonggiju.security.authentication.jwt.provider;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.life.jeonggiju.domain.user.entity.Authority;
import com.life.jeonggiju.security.authentication.jwt.exception.TokenGenerateException;
import com.life.jeonggiju.security.authentication.jwt.token.TokenType;
import com.life.jeonggiju.security.core.dto.UserPrincipal;
import com.life.jeonggiju.security.core.principal.LifeUserDetails;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.servlet.http.Cookie;

@Component
public class JwtTokenProvider {

	public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";
	private static final int SECONDS_IN_MS = 1000;

	private final int accessTokenExpirationMs;
	private final int refreshTokenExpirationMs;

	private final JWSSigner accessTokenSigner;
	private final JWSVerifier accessTokenVerifier;

	private final JWSSigner refreshTokenSigner;
	private final JWSVerifier refreshTokenVerifier;

	private final boolean cookieSecure;

	public JwtTokenProvider(
		@Value("${jwt.access-token.secret}") String accessTokenSecret,
		@Value("${jwt.access-token.expiration-ms}") int accessTokenExpirationMs,
		@Value("${jwt.refresh-token.secret}") String refreshTokenSecret,
		@Value("${jwt.refresh-token.expiration-ms}") int refreshTokenExpirationMs,
		@Value("${app.cookie.secure}") boolean cookieSecure
	) throws JOSEException {
		this.accessTokenExpirationMs = accessTokenExpirationMs;
		this.refreshTokenExpirationMs = refreshTokenExpirationMs;

		byte[] accessTokenSecretBytes = accessTokenSecret.getBytes(StandardCharsets.UTF_8);
		this.accessTokenSigner = new MACSigner(accessTokenSecretBytes);
		this.accessTokenVerifier = new MACVerifier(accessTokenSecretBytes);

		byte[] refreshTokenSecretBytes = refreshTokenSecret.getBytes(StandardCharsets.UTF_8);
		this.refreshTokenSigner = new MACSigner(refreshTokenSecretBytes);
		this.refreshTokenVerifier = new MACVerifier(refreshTokenSecretBytes);
		this.cookieSecure = cookieSecure;
	}

	public String generateAccessToken(LifeUserDetails userDetails) {
		return generateToken(userDetails, accessTokenExpirationMs, accessTokenSigner, TokenType.ACCESS);
	}

	public String generateRefreshToken(LifeUserDetails userDetails) {
		return generateToken(userDetails, refreshTokenExpirationMs, refreshTokenSigner, TokenType.REFRESH);
	}

	private String generateToken(LifeUserDetails userDetails, int expirationMs, JWSSigner signer, TokenType tokenType) {
		try {
			String tokenId = UUID.randomUUID().toString();
			UserPrincipal user = userDetails.getPrincipal();

			Date now = new Date();
			Date expiryDate = new Date(now.getTime() + expirationMs);

			JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.subject(user.getEmail())
				.jwtID(tokenId)
				.claim("userId", user.getUserId().toString())
				.claim("type", tokenType.getValue())
				.claim("authority", userDetails.getAuthorities().stream()
					.map(GrantedAuthority::getAuthority)
					.findFirst()
					.orElse(null))
				.issueTime(now)
				.expirationTime(expiryDate)
				.build();

			SignedJWT signedJWT = new SignedJWT(
				new JWSHeader(JWSAlgorithm.HS256),
				claimsSet
			);

			signedJWT.sign(signer);
			return signedJWT.serialize();
		} catch (JOSEException e) {
			throw new TokenGenerateException();
		}
	}

	public boolean validateAccessToken(String token) {
		return validateToken(token, accessTokenVerifier, TokenType.ACCESS);
	}

	public boolean validateRefreshToken(String token) {
		return validateToken(token, refreshTokenVerifier, TokenType.REFRESH);
	}

	private boolean validateToken(String token, JWSVerifier verifier, TokenType expectedType) {
		try {
			SignedJWT signedJWT = SignedJWT.parse(token);

			if (!signedJWT.verify(verifier)) {
				return false;
			}

			String tokenType = (String)signedJWT.getJWTClaimsSet().getClaim("type");
			if (!expectedType.getValue().equals(tokenType)) {
				return false;
			}

			Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
			return expirationTime != null && !expirationTime.before(new Date());
		} catch (Exception e) {
			return false;
		}
	}

	public Cookie generateRefreshTokenCookie(String refreshToken) {
		return createRefreshCookie(refreshToken, refreshTokenExpirationMs / SECONDS_IN_MS);
	}

	public Cookie generateRefreshTokenExpirationCookie() {
		return createRefreshCookie("", 0);
	}

	private Cookie createRefreshCookie(String value, int maxAgeSeconds) {
		Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, value);
		cookie.setHttpOnly(true);
		cookie.setSecure(cookieSecure);
		cookie.setPath("/");
		cookie.setMaxAge(maxAgeSeconds);
		return cookie;
	}

	public String getTokenId(String token) {
		return getClaim(token, JWTClaimsSet::getJWTID);
	}

	public UUID getUserId(String token) {
		String userIdStr = getClaim(token, claims -> (String)claims.getClaim("userId"));
		if (userIdStr == null) {
			throw new IllegalArgumentException("User ID claim not found in JWT token");
		}
		return UUID.fromString(userIdStr);
	}

	public Authority getAuthority(String token) {
		String authority = getClaim(token, claims -> (String)claims.getClaim("authority"));
		if (authority == null) {
			throw new IllegalArgumentException("Role claim not found in JWT token");
		}
		return Authority.valueOf(authority);
	}

	public String getSubject(String token) {
		return getClaim(token, JWTClaimsSet::getSubject);
	}

	private <T> T getClaim(String token, ClaimExtractor<T> extractor) {
		try {
			SignedJWT signedJWT = SignedJWT.parse(token);
			JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
			return extractor.extract(claims);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid JWT token", e);
		}
	}

	@FunctionalInterface
	private interface ClaimExtractor<T> {
		T extract(JWTClaimsSet claims) throws ParseException;
	}
}