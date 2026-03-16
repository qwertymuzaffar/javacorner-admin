package com.javacorner.admin.security;

import com.javacorner.admin.entity.Role;
import com.javacorner.admin.entity.User;
import com.javacorner.admin.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserService userService;

    @Test
    void loadUserByUsernameReturnsUserDetailsWithMappedAuthorities() {
        UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl(userService);
        Role admin = role(1L, "Admin");
        Role student = role(2L, "Student");
        User user = new User();
        user.setEmail("user@site.com");
        user.setPassword("encoded");
        user.setRoles(Set.of(admin, student));
        when(userService.loadUserByEmail("user@site.com")).thenReturn(user);

        UserDetails result = userDetailsService.loadUserByUsername("user@site.com");

        assertEquals("user@site.com", result.getUsername());
        assertEquals("encoded", result.getPassword());
        assertEquals(
                Set.of("Admin", "Student"),
                result.getAuthorities().stream()
                        .map(grantedAuthority -> grantedAuthority.getAuthority())
                        .collect(Collectors.toSet())
        );
    }

    @Test
    void loadUserByUsernameThrowsWhenUserDoesNotExist() {
        UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl(userService);
        when(userService.loadUserByEmail("missing@site.com")).thenReturn(null);

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing@site.com")
        );
    }

    private Role role(Long id, String name) {
        Role role = new Role();
        role.setRoleId(id);
        role.setName(name);
        return role;
    }
}
