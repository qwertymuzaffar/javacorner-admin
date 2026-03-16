package com.javacorner.admin.entity;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleTest {

    @Test
    void constructorSetsName() {
        Role role = new Role("ROLE_ADMIN");

        assertEquals("ROLE_ADMIN", role.getName());
    }

    @Test
    void equalsAndHashCodeMatchForSameState() {
        Role first = role(1L, "ROLE_USER");
        Role second = role(1L, "ROLE_USER");

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equalsReturnsFalseForDifferentId() {
        Role first = role(1L, "ROLE_USER");
        Role second = role(2L, "ROLE_USER");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseForDifferentNameWithSameId() {
        Role first = role(1L, "ROLE_USER");
        Role second = role(1L, "ROLE_ADMIN");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsHandlesIdentityAndTypeGuards() {
        Role role = role(5L, "ROLE_USER");

        assertTrue(role.equals(role));
        assertFalse(role.equals(null));
        assertFalse(role.equals("not-a-role"));
    }

    @Test
    void toStringContainsReadableFields() {
        Role role = role(7L, "ROLE_MANAGER");

        String text = role.toString();

        assertTrue(text.contains("roleId=7"));
        assertTrue(text.contains("ROLE_MANAGER"));
    }

    @Test
    void settersUpdateFields() {
        Role role = new Role();
        Set<User> users = new HashSet<>();
        users.add(user("person@site.com"));

        role.setRoleId(10L);
        role.setName("ROLE_EDITOR");
        role.setUsers(users);

        assertEquals(10L, role.getRoleId());
        assertEquals("ROLE_EDITOR", role.getName());
        assertEquals(users, role.getUsers());
    }

    private Role role(Long id, String name) {
        Role role = new Role();
        role.setRoleId(id);
        role.setName(name);
        return role;
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("encoded");
        return user;
    }
}
