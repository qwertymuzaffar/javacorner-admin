package com.javacorner.admin.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstructorTest {

    @Test
    void constructorSetsMainFields() {
        User user = user("instructor@site.com", "encoded");

        Instructor instructor = new Instructor("John", "Doe", "Java expert", user);

        assertEquals("John", instructor.getFirstName());
        assertEquals("Doe", instructor.getLastName());
        assertEquals("Java expert", instructor.getSummary());
        assertEquals(user, instructor.getUser());
    }

    @Test
    void equalsAndHashCodeMatchForSameState() {
        Instructor first = instructor(3L, "John", "Doe", "Java");
        Instructor second = instructor(3L, "John", "Doe", "Java");

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equalsReturnsFalseForDifferentId() {
        Instructor first = instructor(3L, "John", "Doe", "Java");
        Instructor second = instructor(4L, "John", "Doe", "Java");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseForDifferentFirstNameWithSameId() {
        Instructor first = instructor(3L, "John", "Doe", "Java");
        Instructor second = instructor(3L, "Jane", "Doe", "Java");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseForDifferentLastNameWhenIdAndFirstNameMatch() {
        Instructor first = instructor(3L, "John", "Doe", "Java");
        Instructor second = instructor(3L, "John", "Smith", "Java");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseForDifferentSummaryWhenOtherFieldsMatch() {
        Instructor first = instructor(3L, "John", "Doe", "Java");
        Instructor second = instructor(3L, "John", "Doe", "Spring");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsHandlesIdentityAndTypeGuards() {
        Instructor instructor = instructor(5L, "John", "Doe", "Java");

        assertTrue(instructor.equals(instructor));
        assertFalse(instructor.equals(null));
        assertFalse(instructor.equals("not-an-instructor"));
    }

    @Test
    void toStringContainsReadableFields() {
        Instructor instructor = instructor(11L, "Jane", "Smith", "Backend");

        String text = instructor.toString();

        assertTrue(text.contains("instructorId=11"));
        assertTrue(text.contains("Jane"));
        assertTrue(text.contains("Smith"));
    }

    @Test
    void settersUpdateFields() {
        Instructor instructor = new Instructor();
        User user = user("new@site.com", "pass");

        instructor.setInstructorId(20L);
        instructor.setFirstName("New");
        instructor.setLastName("Teacher");
        instructor.setSummary("Summary");
        instructor.setUser(user);

        assertEquals(20L, instructor.getInstructorId());
        assertEquals("New", instructor.getFirstName());
        assertEquals("Teacher", instructor.getLastName());
        assertEquals("Summary", instructor.getSummary());
        assertEquals(user, instructor.getUser());
    }

    private Instructor instructor(Long id, String firstName, String lastName, String summary) {
        Instructor instructor = new Instructor();
        instructor.setInstructorId(id);
        instructor.setFirstName(firstName);
        instructor.setLastName(lastName);
        instructor.setSummary(summary);
        instructor.setUser(user("instructor@site.com", "encoded"));
        return instructor;
    }

    private User user(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }
}
