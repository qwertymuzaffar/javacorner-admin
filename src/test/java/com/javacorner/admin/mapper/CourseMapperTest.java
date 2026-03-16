package com.javacorner.admin.mapper;

import com.javacorner.admin.dto.CourseDTO;
import com.javacorner.admin.dto.InstructorDTO;
import com.javacorner.admin.entity.Course;
import com.javacorner.admin.entity.Instructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseMapperTest {

    @Mock
    private InstructorMapper instructorMapper;

    @Test
    void fromCourseMapsBasicFieldsAndDelegatesInstructorMapping() {
        CourseMapper courseMapper = new CourseMapper(instructorMapper);
        Instructor instructor = new Instructor();
        instructor.setInstructorId(7L);
        Course course = new Course();
        course.setCourseId(3L);
        course.setCourseName("Java");
        course.setCourseDuration("6 weeks");
        course.setCourseDescription("Core Java");
        course.setInstructor(instructor);
        InstructorDTO instructorDTO = new InstructorDTO();
        instructorDTO.setInstructorId(7L);
        when(instructorMapper.fromInstructor(instructor)).thenReturn(instructorDTO);

        CourseDTO result = courseMapper.fromCourse(course);

        assertEquals(3L, result.getCourseId());
        assertEquals("Java", result.getCourseName());
        assertEquals("6 weeks", result.getCourseDuration());
        assertEquals("Core Java", result.getCourseDescription());
        assertSame(instructorDTO, result.getInstructor());
        verify(instructorMapper).fromInstructor(instructor);
    }

    @Test
    void fromCourseDTOMapsBasicFields() {
        CourseMapper courseMapper = new CourseMapper(instructorMapper);
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setCourseId(9L);
        courseDTO.setCourseName("Spring");
        courseDTO.setCourseDuration("8 weeks");
        courseDTO.setCourseDescription("Spring Boot");
        InstructorDTO instructorDTO = new InstructorDTO();
        instructorDTO.setInstructorId(11L);
        courseDTO.setInstructor(instructorDTO);

        Course result = courseMapper.fromCourseDTO(courseDTO);

        assertEquals(9L, result.getCourseId());
        assertEquals("Spring", result.getCourseName());
        assertEquals("8 weeks", result.getCourseDuration());
        assertEquals("Spring Boot", result.getCourseDescription());
        assertNull(result.getInstructor());
    }
}
