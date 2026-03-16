package com.javacorner.admin.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javacorner.admin.dto.CourseDTO;
import com.javacorner.admin.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CourseRestControllerTest {

    @Mock
    private CourseService courseService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        CourseRestController courseRestController = new CourseRestController(courseService);
        mockMvc = MockMvcBuilders.standaloneSetup(courseRestController).build();
    }

    @Test
    void searchCoursesUsesRequestParamsAndReturnsPage() throws Exception {
        CourseDTO first = courseDTO(1L, "Java");
        CourseDTO second = courseDTO(2L, "Spring");
        Page<CourseDTO> page = new PageImpl<>(List.of(first, second));
        when(courseService.findCoursesByCourseName("jav", 1, 2)).thenReturn(page);

        mockMvc.perform(get("/courses")
                        .param("keyword", "jav")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].courseId").value(1))
                .andExpect(jsonPath("$.content[0].courseName").value("Java"))
                .andExpect(jsonPath("$.content[1].courseId").value(2))
                .andExpect(jsonPath("$.content[1].courseName").value("Spring"));
    }

    @Test
    void deleteCourseDelegatesToService() throws Exception {
        mockMvc.perform(delete("/courses/{courseId}", 8L))
                .andExpect(status().isOk());

        verify(courseService).removeCourse(8L);
    }

    @Test
    void saveCourseDelegatesToServiceAndReturnsCreatedCourse() throws Exception {
        CourseDTO requestBody = courseDTO(null, "Docker");
        CourseDTO responseBody = courseDTO(15L, "Docker");
        when(courseService.createCourse(any(CourseDTO.class))).thenReturn(responseBody);

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(15))
                .andExpect(jsonPath("$.courseName").value("Docker"));

        ArgumentCaptor<CourseDTO> captor = ArgumentCaptor.forClass(CourseDTO.class);
        verify(courseService).createCourse(captor.capture());
        assertEquals("Docker", captor.getValue().getCourseName());
    }

    @Test
    void updateCourseOverridesPathVariableIdAndDelegatesToService() throws Exception {
        CourseDTO requestBody = courseDTO(99L, "Kubernetes");
        CourseDTO responseBody = courseDTO(21L, "Kubernetes");
        when(courseService.updateCourse(any(CourseDTO.class))).thenReturn(responseBody);

        mockMvc.perform(put("/courses/{courseId}", 21L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(21))
                .andExpect(jsonPath("$.courseName").value("Kubernetes"));

        ArgumentCaptor<CourseDTO> captor = ArgumentCaptor.forClass(CourseDTO.class);
        verify(courseService).updateCourse(captor.capture());
        assertEquals(21L, captor.getValue().getCourseId());
        assertEquals("Kubernetes", captor.getValue().getCourseName());
    }

    @Test
    void enrollStudentInCourseDelegatesToService() throws Exception {
        mockMvc.perform(post("/courses/{courseId}/enroll/students/{studentId}", 5L, 9L))
                .andExpect(status().isOk());

        verify(courseService).assignStudentToCourse(5L, 9L);
    }

    private CourseDTO courseDTO(Long id, String name) {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setCourseId(id);
        courseDTO.setCourseName(name);
        courseDTO.setCourseDuration("6 weeks");
        courseDTO.setCourseDescription("Description");
        return courseDTO;
    }
}
