package com.javacorner.admin.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.javacorner.admin.helper.JWTHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.servlet.FilterChain;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JWTAuthenticationFilterTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTHelper jwtHelper;

    @Mock
    private Authentication authResult;

    @Mock
    private FilterChain filterChain;

    private JWTAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JWTAuthenticationFilter(authenticationManager, jwtHelper);
    }

    @Test
    void attemptAuthenticationReadsParametersAndDelegatesToAuthenticationManager() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", "user@site.com");
        request.setParameter("password", "secret");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication expected = new UsernamePasswordAuthenticationToken("user@site.com", "secret");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(expected);

        Authentication result = filter.attemptAuthentication(request, response);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(tokenCaptor.capture());
        assertEquals("user@site.com", tokenCaptor.getValue().getPrincipal());
        assertEquals("secret", tokenCaptor.getValue().getCredentials());
        assertSame(expected, result);
    }

    @Test
    void successfulAuthenticationGeneratesTokensAndWritesJsonResponse() throws Exception {
        User principal = new User(
                "user@site.com",
                "ignored",
                List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        when(authResult.getPrincipal()).thenReturn(principal);
        when(jwtHelper.generateAccessToken(eq("user@site.com"), anyList())).thenReturn("access-token");
        when(jwtHelper.generateRefreshToken("user@site.com")).thenReturn("refresh-token");
        Map<String, String> tokens = new LinkedHashMap<>();
        tokens.put("access_token", "access-token");
        tokens.put("refresh_token", "refresh-token");
        when(jwtHelper.getTokensMap("access-token", "refresh-token")).thenReturn(tokens);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.successfulAuthentication(request, response, filterChain, authResult);

        verify(jwtHelper).generateAccessToken(
                eq("user@site.com"),
                argThat(roles -> Set.copyOf(roles).equals(Set.of("ROLE_USER", "ROLE_ADMIN")))
        );
        verify(jwtHelper).generateRefreshToken("user@site.com");
        verify(jwtHelper).getTokensMap("access-token", "refresh-token");
        assertEquals("application/json", response.getContentType());

        Map<String, String> body = new ObjectMapper().readValue(
                response.getContentAsString(),
                new TypeReference<Map<String, String>>() {}
        );
        assertEquals(tokens, body);
    }
}
