package com.javacorner.admin.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.javacorner.admin.helper.JWTHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.javacorner.admin.constant.JWTUtil.AUTH_HEADER;
import static com.javacorner.admin.constant.JWTUtil.SECRET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JWTAuthorizationFilterTest {

    @Mock
    private JWTHelper jwtHelper;

    @Mock
    private FilterChain filterChain;

    private JWTAuthorizationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JWTAuthorizationFilter(jwtHelper);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternalBypassesRefreshTokenEndpoint() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/refresh-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtHelper);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternalContinuesChainWhenNoAccessToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/users");
        request.addHeader(AUTH_HEADER, "Bearer missing");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtHelper.extractTokenFromHeaderIfExists("Bearer missing")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtHelper).extractTokenFromHeaderIfExists("Bearer missing");
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternalSetsAuthenticationForValidToken() throws Exception {
        String token = JWT.create()
                .withSubject("user@site.com")
                .withClaim("roles", List.of("ROLE_USER", "ROLE_ADMIN"))
                .withExpiresAt(new Date(System.currentTimeMillis() + 60_000))
                .sign(Algorithm.HMAC256(SECRET));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/courses");
        request.addHeader(AUTH_HEADER, "Bearer real-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtHelper.extractTokenFromHeaderIfExists("Bearer real-token")).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtHelper).extractTokenFromHeaderIfExists("Bearer real-token");
        verify(filterChain).doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("user@site.com", authentication.getPrincipal());
        assertNull(authentication.getCredentials());
        assertEquals(
                Set.of("ROLE_USER", "ROLE_ADMIN"),
                authentication.getAuthorities().stream()
                        .map(grantedAuthority -> grantedAuthority.getAuthority())
                        .collect(Collectors.toSet())
        );
        assertEquals(UsernamePasswordAuthenticationToken.class, authentication.getClass());
    }
}
