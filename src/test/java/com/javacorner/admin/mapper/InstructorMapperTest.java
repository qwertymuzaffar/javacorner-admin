package com.javacorner.admin.mapper;

import com.javacorner.admin.dto.InstructorDTO;
import com.javacorner.admin.dto.UserDTO;
import com.javacorner.admin.entity.Instructor;
import com.javacorner.admin.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InstructorMapperTest {

    private final InstructorMapper instructorMapper = new InstructorMapper();

    @Test
    void fromInstructorMapsBasicFields() {
        Instructor instructor = new Instructor();
        instructor.setInstructorId(5L);
        instructor.setFirstName("John");
        instructor.setLastName("Doe");
        instructor.setSummary("Java expert");
        User user = new User();
        user.setUserId(9L);
        instructor.setUser(user);

        InstructorDTO result = instructorMapper.fromInstructor(instructor);

        assertEquals(5L, result.getInstructorId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("Java expert", result.getSummary());
        assertNull(result.getUser());
    }

    @Test
    void fromInstructorDTOMapsBasicFields() {
        InstructorDTO instructorDTO = new InstructorDTO();
        instructorDTO.setInstructorId(6L);
        instructorDTO.setFirstName("Jane");
        instructorDTO.setLastName("Smith");
        instructorDTO.setSummary("Spring Boot");
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("instructor@site.com");
        instructorDTO.setUser(userDTO);

        Instructor result = instructorMapper.fromInstructorDTO(instructorDTO);

        assertEquals(6L, result.getInstructorId());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("Spring Boot", result.getSummary());
        assertNull(result.getUser());
    }
}
