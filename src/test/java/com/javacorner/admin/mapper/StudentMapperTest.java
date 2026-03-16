package com.javacorner.admin.mapper;

import com.javacorner.admin.dto.StudentDTO;
import com.javacorner.admin.dto.UserDTO;
import com.javacorner.admin.entity.Student;
import com.javacorner.admin.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StudentMapperTest {

    private final StudentMapper studentMapper = new StudentMapper();

    @Test
    void fromStudentMapsBasicFields() {
        Student student = new Student();
        student.setStudentId(5L);
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setLevel("Beginner");
        User user = new User();
        user.setUserId(9L);
        student.setUser(user);

        StudentDTO result = studentMapper.fromStudent(student);

        assertEquals(5L, result.getStudentId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("Beginner", result.getLevel());
        assertNull(result.getUser());
    }

    @Test
    void fromStudentDTOMapsBasicFields() {
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setStudentId(6L);
        studentDTO.setFirstName("Jane");
        studentDTO.setLastName("Smith");
        studentDTO.setLevel("Advanced");
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("student@site.com");
        studentDTO.setUser(userDTO);

        Student result = studentMapper.fromStudentDTO(studentDTO);

        assertEquals(6L, result.getStudentId());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("Advanced", result.getLevel());
        assertNull(result.getUser());
    }
}
