package com.javacorner.admin.web;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.javacorner.admin.entity.Role;
import com.javacorner.admin.entity.User;
import com.javacorner.admin.helper.JWTHelper;
import com.javacorner.admin.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.javacorner.admin.constant.JWTUtil.AUTH_HEADER;
import static com.javacorner.admin.constant.JWTUtil.SECRET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserRestControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JWTHelper jwtHelper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserRestController userRestController = new UserRestController(userService, jwtHelper);
        mockMvc = MockMvcBuilders.standaloneSetup(userRestController).build();
    }

    @Test
    void checkIfEmailExistsReturnsTrueWhenUserExists() throws Exception {
        User user = new User();
        user.setEmail("user@site.com");
        when(userService.loadUserByEmail("user@site.com")).thenReturn(user);

        mockMvc.perform(get("/users").param("email", "user@site.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void checkIfEmailExistsReturnsFalseWhenUserDoesNotExist() throws Exception {
        when(userService.loadUserByEmail("missing@site.com")).thenReturn(null);

        mockMvc.perform(get("/users").param("email", "missing@site.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void generateNewAccessTokenReturnsTokensMapWhenRefreshTokenIsValid() throws Exception {
        String email = "user@site.com";
        String refreshToken = JWT.create()
                .withSubject(email)
                .sign(Algorithm.HMAC256(SECRET));

        Role admin = new Role();
        admin.setRoleId(1L);
        admin.setName("Admin");
        Role student = new Role();
        student.setRoleId(2L);
        student.setName("Student");
        User user = new User();
        user.setEmail(email);
        Set<Role> roles = new HashSet<>();
        roles.add(admin);
        roles.add(student);
        user.setRoles(roles);

        when(jwtHelper.extractTokenFromHeaderIfExists("Bearer refresh-token")).thenReturn(refreshToken);
        when(userService.loadUserByEmail(email)).thenReturn(user);
        when(jwtHelper.generateAccessToken(eq(email), anyList())).thenReturn("new-access-token");
        Map<String, String> tokensMap = new LinkedHashMap<>();
        tokensMap.put("accessToken", "new-access-token");
        tokensMap.put("refreshToken", refreshToken);
        when(jwtHelper.getTokensMap("new-access-token", refreshToken)).thenReturn(tokensMap);

        mockMvc.perform(get("/refresh-token").header(AUTH_HEADER, "Bearer refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value(refreshToken));

        ArgumentCaptor<List<String>> rolesCaptor = ArgumentCaptor.forClass(List.class);
        verify(jwtHelper).generateAccessToken(eq(email), rolesCaptor.capture());
        assertEquals(Set.of("Admin", "Student"), Set.copyOf(rolesCaptor.getValue()));
    }

    @Test
    void generateNewAccessTokenReturnsServerErrorWhenRefreshTokenMissing() throws Exception {
        when(jwtHelper.extractTokenFromHeaderIfExists(null)).thenReturn(null);

        assertThrows(NestedServletException.class, () -> mockMvc.perform(get("/refresh-token")));
    }
}
