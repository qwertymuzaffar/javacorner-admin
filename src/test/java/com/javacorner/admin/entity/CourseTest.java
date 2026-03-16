package com.javacorner.admin.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseTest {

    @Test
    void constructorSetsMainFields() {
        Instructor instructor = instructor(7L);

        Course course = new Course("Java", "6 weeks", "Core Java", instructor);

        assertEquals("Java", course.getCourseName());
        assertEquals("6 weeks", course.getCourseDuration());
        assertEquals("Core Java", course.getCourseDescription());
        assertEquals(instructor, course.getInstructor());
    }

    @Test
    void assignStudentToCourseAddsRelationOnBothSides() {
        Course course = course(1L, "Java");
        Student student = student(10L);

        course.assignStudentToCourse(student);

        assertTrue(course.getStudents().contains(student));
        assertTrue(student.getCourses().contains(course));
    }

    @Test
    void removeStudentFromCourseRemovesRelationOnBothSides() {
        Course course = course(1L, "Java");
        Student student = student(10L);
        course.assignStudentToCourse(student);

        course.removeStudentFromCourse(student);

        assertFalse(course.getStudents().contains(student));
        assertFalse(student.getCourses().contains(course));
    }

    @Test
    void equalsAndHashCodeMatchForSameState() {
        Course first = course(3L, "Spring");
        Course second = course(3L, "Spring");

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equalsReturnsFalseForDifferentId() {
        Course first = course(3L, "Spring");
        Course second = course(4L, "Spring");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseForDifferentNameWithSameId() {
        Course first = course(3L, "Spring");
        Course second = course(3L, "Java");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseForDifferentDurationWhenIdAndNameMatch() {
        Course first = course(3L, "Spring");
        Course second = course(3L, "Spring");
        second.setCourseDuration("8 weeks");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseForDifferentDescriptionWhenOtherFieldsMatch() {
        Course first = course(3L, "Spring");
        Course second = course(3L, "Spring");
        second.setCourseDescription("Different");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsHandlesIdentityAndTypeGuards() {
        Course course = course(5L, "Java");

        assertTrue(course.equals(course));
        assertFalse(course.equals(null));
        assertFalse(course.equals("not-a-course"));
    }

    @Test
    void toStringContainsReadableFields() {
        Course course = course(9L, "Algorithms");

        String text = course.toString();

        assertTrue(text.contains("courseId=9"));
        assertTrue(text.contains("Algorithms"));
    }

    private Course course(Long id, String name) {
        Course course = new Course();
        course.setCourseId(id);
        course.setCourseName(name);
        course.setCourseDuration("6 weeks");
        course.setCourseDescription("Description");
        course.setInstructor(instructor(99L));
        return course;
    }

    private Student student(Long id) {
        Student student = new Student();
        student.setStudentId(id);
        student.setFirstName("Jane");
        student.setLastName("Doe");
        student.setLevel("Beginner");
        return student;
    }

    private Instructor instructor(Long id) {
        Instructor instructor = new Instructor();
        instructor.setInstructorId(id);
        instructor.setFirstName("John");
        instructor.setLastName("Doe");
        instructor.setSummary("Senior");
        return instructor;
    }
}
