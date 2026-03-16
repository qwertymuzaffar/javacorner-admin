package com.javacorner.admin.helper;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.javacorner.admin.constant.JWTUtil.BEARER_PREFIX;
import static com.javacorner.admin.constant.JWTUtil.ISSUER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JWTHelperTest {

    private final JWTHelper jwtHelper = new JWTHelper();

    @Test
    void generateAccessTokenCreatesTokenWithSubjectIssuerAndRoles() {
        long before = System.currentTimeMillis();
        String token = jwtHelper.generateAccessToken("user@site.com", List.of("ROLE_USER", "ROLE_ADMIN"));
        long after = System.currentTimeMillis();

        DecodedJWT decodedJWT = JWT.decode(token);

        assertEquals("user@site.com", decodedJWT.getSubject());
        assertEquals(ISSUER, decodedJWT.getIssuer());
        assertEquals(List.of("ROLE_USER", "ROLE_ADMIN"), decodedJWT.getClaim("roles").asList(String.class));
        assertNotNull(decodedJWT.getExpiresAt());
        assertTrue(decodedJWT.getExpiresAt().getTime() > before);
        assertTrue(decodedJWT.getExpiresAt().getTime() > after);
    }

    @Test
    void generateRefreshTokenCreatesTokenWithSubjectAndIssuer() {
        long now = System.currentTimeMillis();
        String token = jwtHelper.generateRefreshToken("user@site.com");

        DecodedJWT decodedJWT = JWT.decode(token);

        assertEquals("user@site.com", decodedJWT.getSubject());
        assertEquals(ISSUER, decodedJWT.getIssuer());
        assertNotNull(decodedJWT.getExpiresAt());
        assertTrue(decodedJWT.getExpiresAt().getTime() > now);
    }

    @Test
    void extractTokenFromHeaderIfExistsReturnsTokenWhenBearerPrefixExists() {
        String extracted = jwtHelper.extractTokenFromHeaderIfExists(BEARER_PREFIX + "abc.def.ghi");

        assertEquals("abc.def.ghi", extracted);
    }

    @Test
    void extractTokenFromHeaderIfExistsReturnsNullWhenHeaderIsNull() {
        assertNull(jwtHelper.extractTokenFromHeaderIfExists(null));
    }

    @Test
    void extractTokenFromHeaderIfExistsReturnsNullWhenPrefixIsMissing() {
        assertNull(jwtHelper.extractTokenFromHeaderIfExists("Token abc.def.ghi"));
    }

    @Test
    void getTokensMapReturnsExpectedKeysAndValues() {
        Map<String, String> tokens = jwtHelper.getTokensMap("access-token", "refresh-token");

        assertEquals("access-token", tokens.get("accessToken"));
        assertEquals("refresh-token", tokens.get("refreshToken"));
        assertEquals(2, tokens.size());
    }
}
