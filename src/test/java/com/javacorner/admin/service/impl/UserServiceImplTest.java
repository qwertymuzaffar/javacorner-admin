package com.javacorner.admin.service.impl;

import com.javacorner.admin.dao.RoleDao;
import com.javacorner.admin.dao.UserDao;
import com.javacorner.admin.entity.Role;
import com.javacorner.admin.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @Mock
    private RoleDao roleDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void loadUserByEmailReturnsDaoResult() {
        User user = user("user@site.com", "encoded");
        when(userDao.findByEmail("user@site.com")).thenReturn(user);

        User result = userService.loadUserByEmail("user@site.com");

        assertSame(user, result);
    }

    @Test
    void createUserEncodesPasswordAndSavesEncodedValue() {
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        User savedUser = user("user@site.com", "encoded");
        when(userDao.save(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(savedUser);

        User result = userService.createUser("user@site.com", "plain");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).save(userCaptor.capture());
        assertEquals("user@site.com", userCaptor.getValue().getEmail());
        assertEquals("encoded", userCaptor.getValue().getPassword());
        assertSame(savedUser, result);
    }

    @Test
    void assignRoleToUserAddsRoleToBothUserAndRoleCollections() {
        User user = user("user@site.com", "encoded");
        Role role = role("Student");
        when(userDao.findByEmail("user@site.com")).thenReturn(user);
        when(roleDao.findByName("Student")).thenReturn(role);

        userService.assignRoleToUser("user@site.com", "Student");

        assertTrue(user.getRoles().contains(role));
        assertTrue(role.getUsers().contains(user));
    }

    private User user(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    private Role role(String name) {
        Role role = new Role();
        role.setName(name);
        return role;
    }
}
