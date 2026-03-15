package com.javacorner.admin.service.impl;

import com.javacorner.admin.dao.StudentDao;
import com.javacorner.admin.dto.StudentDTO;
import com.javacorner.admin.dto.UserDTO;
import com.javacorner.admin.entity.Course;
import com.javacorner.admin.entity.Student;
import com.javacorner.admin.entity.User;
import com.javacorner.admin.mapper.StudentMapper;
import com.javacorner.admin.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityNotFoundException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentDao studentDao;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private StudentServiceImpl studentService;

    @Test
    void loadStudentByIdReturnsEntityWhenPresent() {
        Student student = student(2L);
        when(studentDao.findById(2L)).thenReturn(Optional.of(student));

        Student result = studentService.loadStudentById(2L);

        assertSame(student, result);
    }

    @Test
    void loadStudentByIdThrowsWhenMissing() {
        when(studentDao.findById(77L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> studentService.loadStudentById(77L));

        assertEquals("Student with ID 77 not found", exception.getMessage());
    }

    @Test
    void loadStudentsByNameReturnsMappedPage() {
        PageRequest pageRequest = PageRequest.of(0, 2);
        Student student = student(5L);
        StudentDTO dto = studentDto(5L, "student@site.com", "pass123");
        Page<Student> page = new PageImpl<>(List.of(student), pageRequest, 1);

        when(studentDao.findStudentsByName("Jane", pageRequest)).thenReturn(page);
        when(studentMapper.fromStudent(student)).thenReturn(dto);

        Page<StudentDTO> result = studentService.loadStudentsByName("Jane", 0, 2);

        assertEquals(1, result.getTotalElements());
        assertEquals(List.of(dto), result.getContent());
    }

    @Test
    void loadStudentByEmailReturnsMappedDto() {
        Student student = student(3L);
        StudentDTO dto = studentDto(3L, "student@site.com", "pass123");
        when(studentDao.findStudentByEmail("student@site.com")).thenReturn(student);
        when(studentMapper.fromStudent(student)).thenReturn(dto);

        StudentDTO result = studentService.loadStudentByEmail("student@site.com");

        assertSame(dto, result);
    }

    @Test
    void createStudentCreatesUserAssignsRoleAndSavesStudent() {
        StudentDTO request = studentDto(10L, "student@site.com", "pass123");
        User createdUser = user("student@site.com", "pass123");
        Student mappedStudent = student(10L);
        Student savedStudent = student(10L);
        savedStudent.setUser(createdUser);
        StudentDTO response = studentDto(10L, "student@site.com", "pass123");

        when(userService.createUser("student@site.com", "pass123")).thenReturn(createdUser);
        when(studentMapper.fromStudentDTO(request)).thenReturn(mappedStudent);
        when(studentDao.save(mappedStudent)).thenReturn(savedStudent);
        when(studentMapper.fromStudent(savedStudent)).thenReturn(response);

        StudentDTO result = studentService.createStudent(request);

        assertSame(response, result);
        assertSame(createdUser, mappedStudent.getUser());
        verify(userService).assignRoleToUser("student@site.com", "Student");
    }

    @Test
    void updateStudentPreservesUserAndCourses() {
        StudentDTO request = studentDto(11L, "new@site.com", "newpass");
        User existingUser = user("existing@site.com", "encoded");
        Course existingCourse = course(30L);
        Student loadedStudent = student(11L);
        loadedStudent.setUser(existingUser);
        loadedStudent.setCourses(Set.of(existingCourse));
        Student mappedStudent = student(11L);
        Student updatedStudent = student(11L);
        updatedStudent.setUser(existingUser);
        updatedStudent.setCourses(Set.of(existingCourse));
        StudentDTO response = studentDto(11L, "new@site.com", "newpass");

        when(studentDao.findById(11L)).thenReturn(Optional.of(loadedStudent));
        when(studentMapper.fromStudentDTO(request)).thenReturn(mappedStudent);
        when(studentDao.save(mappedStudent)).thenReturn(updatedStudent);
        when(studentMapper.fromStudent(updatedStudent)).thenReturn(response);

        StudentDTO result = studentService.updateStudent(request);

        assertSame(response, result);
        assertSame(existingUser, mappedStudent.getUser());
        assertEquals(Set.of(existingCourse), mappedStudent.getCourses());
    }

    @Test
    void updateStudentThrowsWhenStudentMissing() {
        StudentDTO request = studentDto(11L, "new@site.com", "newpass");
        when(studentDao.findById(11L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> studentService.updateStudent(request));

        assertEquals("Student with ID 11 not found", exception.getMessage());
        verify(studentMapper, never()).fromStudentDTO(any());
        verify(studentDao, never()).save(any());
    }

    @Test
    void removeStudentRemovesOneCourseAssociationThenDeletesStudent() {
        Student student = student(9L);
        Course firstCourse = course(100L);
        Course secondCourse = course(101L);
        LinkedHashSet<Course> courses = new LinkedHashSet<>();
        courses.add(firstCourse);
        courses.add(secondCourse);
        student.setCourses(courses);
        firstCourse.getStudents().add(student);
        secondCourse.getStudents().add(student);
        when(studentDao.findById(9L)).thenReturn(Optional.of(student));

        studentService.removeStudent(9L);

        assertTrue(!student.getCourses().contains(firstCourse));
        assertTrue(student.getCourses().contains(secondCourse));
        assertTrue(!firstCourse.getStudents().contains(student));
        assertTrue(secondCourse.getStudents().contains(student));
        verify(studentDao).deleteById(9L);
    }

    @Test
    void removeStudentWithoutCoursesDeletesStudent() {
        Student student = student(12L);
        when(studentDao.findById(12L)).thenReturn(Optional.of(student));

        studentService.removeStudent(12L);

        verify(studentDao).deleteById(12L);
    }

    @Test
    void removeStudentThrowsWhenStudentMissing() {
        when(studentDao.findById(15L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> studentService.removeStudent(15L));

        assertEquals("Student with ID 15 not found", exception.getMessage());
        verify(studentDao, never()).deleteById(any());
    }

    private Student student(Long id) {
        Student student = new Student();
        student.setStudentId(id);
        student.setFirstName("Jane");
        student.setLastName("Doe");
        student.setLevel("Beginner");
        return student;
    }

    private StudentDTO studentDto(Long id, String email, String password) {
        StudentDTO dto = new StudentDTO();
        dto.setStudentId(id);
        dto.setFirstName("Jane");
        dto.setLastName("Doe");
        dto.setLevel("Beginner");
        dto.setUser(userDto(email, password));
        return dto;
    }

    private User user(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    private UserDTO userDto(String email, String password) {
        UserDTO dto = new UserDTO();
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    private Course course(Long id) {
        Course course = new Course();
        course.setCourseId(id);
        course.setCourseName("Course " + id);
        course.setCourseDuration("6 weeks");
        course.setCourseDescription("desc");
        return course;
    }
}
