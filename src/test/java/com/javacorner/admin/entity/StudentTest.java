package com.javacorner.admin.entity;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentTest {

    @Test
    void constructorSetsMainFields() {
        User user = user("student@site.com", "encoded");

        Student student = new Student("Jane", "Doe", "Beginner", user);

        assertEquals("Jane", student.getFirstName());
        assertEquals("Doe", student.getLastName());
        assertEquals("Beginner", student.getLevel());
        assertEquals(user, student.getUser());
    }

    @Test
    void equalsAndHashCodeMatchForSameState() {
        Student first = student(3L, "Jane", "Doe", "Beginner");
        Student second = student(3L, "Jane", "Doe", "Beginner");

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equalsReturnsFalseForDifferentId() {
        Student first = student(3L, "Jane", "Doe", "Beginner");
        Student second = student(4L, "Jane", "Doe", "Beginner");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseForDifferentFirstNameWithSameId() {
        Student first = student(3L, "Jane", "Doe", "Beginner");
        Student second = student(3L, "John", "Doe", "Beginner");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseForDifferentLastNameWhenIdAndFirstNameMatch() {
        Student first = student(3L, "Jane", "Doe", "Beginner");
        Student second = student(3L, "Jane", "Smith", "Beginner");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseForDifferentLevelWhenOtherFieldsMatch() {
        Student first = student(3L, "Jane", "Doe", "Beginner");
        Student second = student(3L, "Jane", "Doe", "Advanced");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsHandlesIdentityAndTypeGuards() {
        Student student = student(5L, "Jane", "Doe", "Beginner");

        assertTrue(student.equals(student));
        assertFalse(student.equals(null));
        assertFalse(student.equals("not-a-student"));
    }

    @Test
    void toStringContainsReadableFields() {
        Student student = student(11L, "Alex", "Smith", "Intermediate");

        String text = student.toString();

        assertTrue(text.contains("studentId=11"));
        assertTrue(text.contains("Alex"));
        assertTrue(text.contains("Smith"));
        assertTrue(text.contains("Intermediate"));
    }

    @Test
    void settersUpdateFields() {
        Student student = new Student();
        User user = user("new@site.com", "pass");
        Set<Course> courses = new HashSet<>();
        Course course = new Course();
        course.setCourseId(9L);
        course.setCourseName("Java");
        courses.add(course);

        student.setStudentId(20L);
        student.setFirstName("New");
        student.setLastName("Learner");
        student.setLevel("Advanced");
        student.setCourses(courses);
        student.setUser(user);

        assertEquals(20L, student.getStudentId());
        assertEquals("New", student.getFirstName());
        assertEquals("Learner", student.getLastName());
        assertEquals("Advanced", student.getLevel());
        assertEquals(courses, student.getCourses());
        assertEquals(user, student.getUser());
    }

    private Student student(Long id, String firstName, String lastName, String level) {
        Student student = new Student();
        student.setStudentId(id);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setLevel(level);
        student.setUser(user("student@site.com", "encoded"));
        return student;
    }

    private User user(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }
}
