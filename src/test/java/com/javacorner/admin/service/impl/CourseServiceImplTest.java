package com.javacorner.admin.service.impl;

import com.javacorner.admin.dao.CourseDao;
import com.javacorner.admin.dao.InstructorDao;
import com.javacorner.admin.dao.StudentDao;
import com.javacorner.admin.dto.CourseDTO;
import com.javacorner.admin.dto.InstructorDTO;
import com.javacorner.admin.entity.Course;
import com.javacorner.admin.entity.Instructor;
import com.javacorner.admin.entity.Student;
import com.javacorner.admin.mapper.CourseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseDao courseDao;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private InstructorDao instructorDao;

    @Mock
    private StudentDao studentDao;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Test
    void loadCourseByIdReturnsCourseWhenPresent() {
        Course course = course(1L, "Java Basics");
        when(courseDao.findById(1L)).thenReturn(Optional.of(course));

        Course result = courseService.loadCourseById(1L);

        assertSame(course, result);
    }

    @Test
    void loadCourseByIdThrowsWhenMissing() {
        when(courseDao.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> courseService.loadCourseById(99L));

        assertEquals("Course with ID 99 Not Found", exception.getMessage());
    }

    @Test
    void createCourseMapsInstructorSavesAndReturnsMappedDto() {
        CourseDTO request = courseDto(10L, 7L, "Spring");
        Course mappedCourse = course(10L, "Spring");
        Instructor instructor = instructor(7L);
        Course savedCourse = course(10L, "Spring");
        savedCourse.setInstructor(instructor);
        CourseDTO response = courseDto(10L, 7L, "Spring");

        when(courseMapper.fromCourseDTO(request)).thenReturn(mappedCourse);
        when(instructorDao.findById(7L)).thenReturn(Optional.of(instructor));
        when(courseDao.save(mappedCourse)).thenReturn(savedCourse);
        when(courseMapper.fromCourse(savedCourse)).thenReturn(response);

        CourseDTO result = courseService.createCourse(request);

        assertSame(response, result);
        assertSame(instructor, mappedCourse.getInstructor());
    }

    @Test
    void createCourseThrowsWhenInstructorMissing() {
        CourseDTO request = courseDto(10L, 7L, "Spring");
        Course mappedCourse = course(10L, "Spring");

        when(courseMapper.fromCourseDTO(request)).thenReturn(mappedCourse);
        when(instructorDao.findById(7L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> courseService.createCourse(request));

        assertEquals("Instructor with ID 7 Not Found", exception.getMessage());
        verify(courseDao, never()).save(any());
    }

    @Test
    void updateCoursePreservesExistingStudentsAndReturnsMappedDto() {
        CourseDTO request = courseDto(15L, 3L, "Updated");
        Instructor instructor = instructor(3L);
        Student student = student(12L);
        Course loadedCourse = course(15L, "Existing");
        loadedCourse.setStudents(Set.of(student));
        Course mappedCourse = course(15L, "Updated");
        Course savedCourse = course(15L, "Updated");
        savedCourse.setInstructor(instructor);
        savedCourse.setStudents(Set.of(student));
        CourseDTO response = courseDto(15L, 3L, "Updated");

        when(courseDao.findById(15L)).thenReturn(Optional.of(loadedCourse));
        when(instructorDao.findById(3L)).thenReturn(Optional.of(instructor));
        when(courseMapper.fromCourseDTO(request)).thenReturn(mappedCourse);
        when(courseDao.save(mappedCourse)).thenReturn(savedCourse);
        when(courseMapper.fromCourse(savedCourse)).thenReturn(response);

        CourseDTO result = courseService.updateCourse(request);

        assertSame(response, result);
        assertSame(instructor, mappedCourse.getInstructor());
        assertEquals(Set.of(student), mappedCourse.getStudents());
    }

    @Test
    void updateCourseThrowsWhenCourseMissing() {
        CourseDTO request = courseDto(15L, 3L, "Updated");
        when(courseDao.findById(15L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> courseService.updateCourse(request));

        assertEquals("Course with ID 15 Not Found", exception.getMessage());
        verify(instructorDao, never()).findById(any());
        verify(courseDao, never()).save(any());
    }

    @Test
    void updateCourseThrowsWhenInstructorMissing() {
        CourseDTO request = courseDto(15L, 3L, "Updated");
        Course loadedCourse = course(15L, "Existing");
        when(courseDao.findById(15L)).thenReturn(Optional.of(loadedCourse));
        when(instructorDao.findById(3L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> courseService.updateCourse(request));

        assertEquals("Instructor with ID 3 Not Found", exception.getMessage());
        verify(courseDao, never()).save(any());
    }

    @Test
    void findCoursesByCourseNameReturnsMappedPage() {
        PageRequest pageRequest = PageRequest.of(0, 2);
        Course course = course(1L, "Java Basics");
        CourseDTO dto = courseDto(1L, 5L, "Java Basics");
        Page<Course> page = new PageImpl<>(List.of(course), pageRequest, 1);

        when(courseDao.findCoursesByCourseNameContains("Java", pageRequest)).thenReturn(page);
        when(courseMapper.fromCourse(course)).thenReturn(dto);

        Page<CourseDTO> result = courseService.findCoursesByCourseName("Java", 0, 2);

        assertEquals(1, result.getTotalElements());
        assertEquals(List.of(dto), result.getContent());
    }

    @Test
    void assignStudentToCourseAssociatesBothSides() {
        Student student = student(4L);
        Course course = course(8L, "Algorithms");
        when(studentDao.findById(4L)).thenReturn(Optional.of(student));
        when(courseDao.findById(8L)).thenReturn(Optional.of(course));

        courseService.assignStudentToCourse(8L, 4L);

        assertTrue(course.getStudents().contains(student));
        assertTrue(student.getCourses().contains(course));
    }

    @Test
    void assignStudentToCourseThrowsWhenStudentMissing() {
        when(studentDao.findById(4L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> courseService.assignStudentToCourse(8L, 4L));

        assertEquals("Student with ID 4 Not Found", exception.getMessage());
        verify(courseDao, never()).findById(any());
    }

    @Test
    void assignStudentToCourseThrowsWhenCourseMissing() {
        Student student = student(4L);
        when(studentDao.findById(4L)).thenReturn(Optional.of(student));
        when(courseDao.findById(8L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> courseService.assignStudentToCourse(8L, 4L));

        assertEquals("Course with ID 8 Not Found", exception.getMessage());
    }

    @Test
    void fetchCoursesForStudentReturnsMappedPage() {
        assertMappedPageResult(
                () -> courseService.fetchCoursesForStudent(2L, 1, 3),
                page -> when(courseDao.getCoursesByStudentId(eq(2L), eq(PageRequest.of(1, 3)))).thenReturn(page)
        );
    }

    @Test
    void fetchNonEnrolledInCoursesForStudentReturnsMappedPage() {
        assertMappedPageResult(
                () -> courseService.fetchNonEnrolledInCoursesForStudent(2L, 1, 3),
                page -> when(courseDao.getNonEnrolledInCoursesByStudentId(eq(2L), eq(PageRequest.of(1, 3)))).thenReturn(page)
        );
    }

    @Test
    void fetchCoursesForInstructorReturnsMappedPage() {
        assertMappedPageResult(
                () -> courseService.fetchCoursesForInstructor(9L, 1, 3),
                page -> when(courseDao.getCoursesByInstructorId(eq(9L), eq(PageRequest.of(1, 3)))).thenReturn(page)
        );
    }

    @Test
    void removeCourseDeletesById() {
        courseService.removeCourse(25L);

        verify(courseDao).deleteById(25L);
    }

    private void assertMappedPageResult(PageSupplier invocation, DaoStubber daoStubber) {
        PageRequest pageRequest = PageRequest.of(1, 3);
        Course course = course(21L, "Testing");
        CourseDTO dto = courseDto(21L, 11L, "Testing");
        Page<Course> page = new PageImpl<>(List.of(course), pageRequest, 7);

        daoStubber.stub(page);
        when(courseMapper.fromCourse(course)).thenReturn(dto);

        Page<CourseDTO> result = invocation.get();

        assertEquals(7, result.getTotalElements());
        assertEquals(List.of(dto), result.getContent());
    }

    private Course course(Long id, String name) {
        Course course = new Course();
        course.setCourseId(id);
        course.setCourseName(name);
        course.setCourseDuration("6 weeks");
        course.setCourseDescription("desc");
        return course;
    }

    private CourseDTO courseDto(Long courseId, Long instructorId, String name) {
        CourseDTO dto = new CourseDTO();
        dto.setCourseId(courseId);
        dto.setCourseName(name);
        dto.setCourseDuration("6 weeks");
        dto.setCourseDescription("desc");
        dto.setInstructor(instructorDto(instructorId));
        return dto;
    }

    private Instructor instructor(Long id) {
        Instructor instructor = new Instructor();
        instructor.setInstructorId(id);
        instructor.setFirstName("John");
        instructor.setLastName("Doe");
        instructor.setSummary("Senior instructor");
        return instructor;
    }

    private InstructorDTO instructorDto(Long id) {
        InstructorDTO dto = new InstructorDTO();
        dto.setInstructorId(id);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setSummary("Senior instructor");
        return dto;
    }

    private Student student(Long id) {
        Student student = new Student();
        student.setStudentId(id);
        student.setFirstName("Jane");
        student.setLastName("Doe");
        student.setLevel("Beginner");
        return student;
    }

    @FunctionalInterface
    private interface PageSupplier {
        Page<CourseDTO> get();
    }

    @FunctionalInterface
    private interface DaoStubber {
        org.mockito.stubbing.OngoingStubbing<Page<Course>> stub(Page<Course> page);
    }
}
