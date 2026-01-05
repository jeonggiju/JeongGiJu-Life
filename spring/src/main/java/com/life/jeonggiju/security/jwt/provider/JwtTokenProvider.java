package com.life.jeonggiju.security.jwt.provider;

import com.github.f4b6a3.uuid.UuidCreator;
import com.life.jeonggiju.domain.user.entity.Authority;
import com.life.jeonggiju.security.jwt.token.AccessTokenJwt;
import com.life.jeonggiju.security.jwt.token.RefreshTokenJwt;
import com.life.jeonggiju.security.principal.LifeUserDetails;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;
    private final String issuer;

    private final JWSSigner accessSigner;
    private final JWSSigner refreshSigner;
    private final JWSVerifier accessVerifier;
    private final JWSVerifier refreshVerifier;

    public JwtTokenProvider(
            @Value("${security.jwt.access-secret-key}") String accessSecret,
            @Value("${security.jwt.refresh-secret-key}") String refreshSecret,
            @Value("${security.jwt.access-token-validation-seconds}") long accessTokenSeconds,
            @Value("${security.jwt.refresh-token-validation-seconds}") long refreshTokenSeconds,
            @Value("${security.jwt.issuer}") String issuer
    ) throws JOSEException {

        this.issuer = issuer;
        this.accessTokenExpirationMs = accessTokenSeconds * 1000L;
        this.refreshTokenExpirationMs = refreshTokenSeconds * 1000L;

        this.accessSigner = new MACSigner(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.accessVerifier = new MACVerifier(accessSecret.getBytes(StandardCharsets.UTF_8));

        this.refreshSigner = new MACSigner(refreshSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshVerifier = new MACVerifier(refreshSecret.getBytes(StandardCharsets.UTF_8));
    }


    public String generateAccessToken(LifeUserDetails user) throws JOSEException {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(accessTokenExpirationMs);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(user.getUsername())
                .jwtID(UuidCreator.getTimeOrderedEpoch().toString())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .claim("type", "access")
                .claim("userId", user.getId().toString())
                .claim("email", user.getUserPrincipal().getEmail())
                .claim("authority", user.getUserPrincipal().getAuthority().name())
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(accessSigner);
        return jwt.serialize();
    }

    public String generateRefreshToken(LifeUserDetails user) throws JOSEException {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(refreshTokenExpirationMs);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(user.getUsername())
                .jwtID(UuidCreator.getTimeOrderedEpoch().toString())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .claim("type", "refresh")
                .claim("userId", user.getId().toString())
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(refreshSigner);
        return jwt.serialize();
    }

    public AccessTokenJwt parseAccessToken(String token) {
        return parseAccessInternal(token);
    }

    public RefreshTokenJwt parseRefreshToken(String token) {
        return parseRefreshInternal(token);
    }

    private AccessTokenJwt parseAccessInternal(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(accessVerifier)) {
                throw new IllegalArgumentException("AccessToken 서명 검증 실패");
            }

            JWTClaimsSet c = jwt.getJWTClaimsSet();
            validateCommon(c, "access");

            UUID userId = UUID.fromString(c.getStringClaim("userId"));
            Authority authority = Authority.valueOf(c.getStringClaim("authority"));

            AccessTokenJwt.AccessTokenUserInfo user = AccessTokenJwt.AccessTokenUserInfo.builder()
                    .userId(userId)
                    .username(c.getSubject())
                    .userEmail(c.getStringClaim("email"))
                    .authority(authority)
                    .build();

            return AccessTokenJwt.builder()
                    .token(token)
                    .issueTime(c.getIssueTime().toInstant())
                    .expirationTime(c.getExpirationTime().toInstant())
                    .user(user)
                    .build();

        } catch (Exception e) {
            throw new IllegalArgumentException("AccessToken 파싱 실패", e);
        }
    }

    private RefreshTokenJwt parseRefreshInternal(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(refreshVerifier)) {
                throw new IllegalArgumentException("RefreshToken 서명 검증 실패");
            }

            JWTClaimsSet c = jwt.getJWTClaimsSet();
            validateCommon(c, "refresh");
            RefreshTokenJwt.RefreshTokenUserInfo user = RefreshTokenJwt.RefreshTokenUserInfo.builder()
                    .userId(UUID.fromString(c.getStringClaim("userId")))
                    .username(c.getStringClaim("username"))
                    .build();

            return RefreshTokenJwt.builder()
                    .token(token)
                    .issueTime(c.getIssueTime().toInstant())
                    .expirationTime(c.getExpirationTime().toInstant())
                    .user(user)
                    .build();

        } catch (Exception e) {
            throw new IllegalArgumentException("RefreshToken 파싱 실패", e);
        }
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, accessVerifier, "access");
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, refreshVerifier, "refresh");
    }

    private boolean validateToken(String token, JWSVerifier verifier, String expectedType) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            if (!signedJWT.verify(verifier)) {
                log.debug("JWT signature verification failed for {} token", expectedType);
                return false;
            }

            String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
            if (!expectedType.equals(tokenType)) {
                log.debug("JWT token type mismatch: expected {}, got {}", expectedType, tokenType);
                return false;
            }

            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expirationTime == null || expirationTime.before(new Date())) {
                log.debug("JWT {} token expired", expectedType);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.debug("JWT {} token validation failed: {}", expectedType, e.getMessage());
            return false;
        }
    }

    private void validateCommon(JWTClaimsSet c, String expectedType) throws ParseException {
        if (!expectedType.equals(c.getStringClaim("type"))) {
            throw new IllegalArgumentException("JWT 타입 불일치");
        }
        if (c.getExpirationTime().before(new Date())) {
            throw new IllegalArgumentException("JWT 만료");
        }
    }

    public Cookie generateRefreshTokenCookie(String token) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshTokenExpirationMs / 1000L));
        return cookie;
    }

    public Cookie generateExpiredRefreshTokenCookie() {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
