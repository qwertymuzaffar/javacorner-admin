package com.javacorner.admin.entity;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void constructorSetsMainFields() {
        User user = new User("user@site.com", "encoded");

        assertEquals("user@site.com", user.getEmail());
        assertEquals("encoded", user.getPassword());
    }

    @Test
    void assignRoleToUserAddsRelationOnBothSides() {
        User user = user(1L, "user@site.com", "encoded");
        Role role = role(10L, "ROLE_USER");

        user.assignRoleToUser(role);

        assertTrue(user.getRoles().contains(role));
        assertTrue(role.getUsers().contains(user));
    }

    @Test
    void removeRoleFromUserRemovesRelationOnBothSides() {
        User user = user(1L, "user@site.com", "encoded");
        Role role = role(10L, "ROLE_USER");
        user.assignRoleToUser(role);

        user.removeRoleFromUser(role);

        assertFalse(user.getRoles().contains(role));
        assertFalse(role.getUsers().contains(user));
    }

    @Test
    void equalsAndHashCodeMatchForSameState() {
        User first = user(3L, "same@site.com", "pass");
        User second = user(3L, "same@site.com", "pass");

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equalsReturnsFalseForDifferentId() {
        User first = user(3L, "same@site.com", "pass");
        User second = user(4L, "same@site.com", "pass");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseForDifferentEmailWithSameId() {
        User first = user(3L, "one@site.com", "pass");
        User second = user(3L, "two@site.com", "pass");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseForDifferentPasswordWithSameIdAndEmail() {
        User first = user(3L, "same@site.com", "pass1");
        User second = user(3L, "same@site.com", "pass2");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsHandlesIdentityAndTypeGuards() {
        User user = user(5L, "user@site.com", "pass");

        assertTrue(user.equals(user));
        assertFalse(user.equals(null));
        assertFalse(user.equals("not-a-user"));
    }

    @Test
    void toStringContainsReadableFields() {
        User user = user(7L, "dev@site.com", "encoded");

        String text = user.toString();

        assertTrue(text.contains("userId=7"));
        assertTrue(text.contains("dev@site.com"));
    }

    @Test
    void settersUpdateFields() {
        User user = new User();
        Set<Role> roles = new HashSet<>();
        Role role = role(8L, "ROLE_ADMIN");
        roles.add(role);
        Student student = new Student();
        student.setStudentId(20L);
        Instructor instructor = new Instructor();
        instructor.setInstructorId(30L);

        user.setUserId(9L);
        user.setEmail("new@site.com");
        user.setPassword("newpass");
        user.setRoles(roles);
        user.setStudent(student);
        user.setInstructor(instructor);

        assertEquals(9L, user.getUserId());
        assertEquals("new@site.com", user.getEmail());
        assertEquals("newpass", user.getPassword());
        assertEquals(roles, user.getRoles());
        assertEquals(student, user.getStudent());
        assertEquals(instructor, user.getInstructor());
    }

    private User user(Long id, String email, String password) {
        User user = new User();
        user.setUserId(id);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    private Role role(Long id, String name) {
        Role role = new Role();
        role.setRoleId(id);
        role.setName(name);
        return role;
    }
}
