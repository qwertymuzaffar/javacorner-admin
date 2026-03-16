package com.javacorner.admin.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javacorner.admin.dto.CourseDTO;
import com.javacorner.admin.dto.StudentDTO;
import com.javacorner.admin.dto.UserDTO;
import com.javacorner.admin.entity.User;
import com.javacorner.admin.service.CourseService;
import com.javacorner.admin.service.StudentService;
import com.javacorner.admin.service.UserService;
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
import org.springframework.web.util.NestedServletException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
class StudentRestControllerTest {

    @Mock
    private StudentService studentService;

    @Mock
    private UserService userService;

    @Mock
    private CourseService courseService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        StudentRestController controller = new StudentRestController(studentService, userService, courseService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void searchStudentsUsesParamsAndReturnsPage() throws Exception {
        Page<StudentDTO> page = new PageImpl<>(List.of(studentDTO(1L, "John"), studentDTO(2L, "Jane")));
        when(studentService.loadStudentsByName("ja", 1, 2)).thenReturn(page);

        mockMvc.perform(get("/students")
                        .param("keyword", "ja")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].studentId").value(1))
                .andExpect(jsonPath("$.content[1].studentId").value(2));
    }

    @Test
    void deleteStudentDelegatesToService() throws Exception {
        mockMvc.perform(delete("/students/{studentId}", 9L))
                .andExpect(status().isOk());

        verify(studentService).removeStudent(9L);
    }

    @Test
    void saveStudentThrowsWhenEmailAlreadyExists() throws Exception {
        StudentDTO requestBody = studentDTO(null, "John");
        User existing = new User();
        existing.setEmail("john@site.com");
        when(userService.loadUserByEmail("john@site.com")).thenReturn(existing);

        assertThrows(NestedServletException.class, () -> mockMvc.perform(post("/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))));
    }

    @Test
    void saveStudentCreatesWhenEmailIsAvailable() throws Exception {
        StudentDTO requestBody = studentDTO(null, "John");
        StudentDTO responseBody = studentDTO(15L, "John");
        when(userService.loadUserByEmail("john@site.com")).thenReturn(null);
        when(studentService.createStudent(any(StudentDTO.class))).thenReturn(responseBody);

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(15));

        ArgumentCaptor<StudentDTO> captor = ArgumentCaptor.forClass(StudentDTO.class);
        verify(studentService).createStudent(captor.capture());
        assertEquals("john@site.com", captor.getValue().getUser().getEmail());
    }

    @Test
    void updateStudentOverridesIdFromPathAndDelegates() throws Exception {
        StudentDTO requestBody = studentDTO(99L, "Jane");
        StudentDTO responseBody = studentDTO(21L, "Jane");
        when(studentService.updateStudent(any(StudentDTO.class))).thenReturn(responseBody);

        mockMvc.perform(put("/students/{studentId}", 21L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(21));

        ArgumentCaptor<StudentDTO> captor = ArgumentCaptor.forClass(StudentDTO.class);
        verify(studentService).updateStudent(captor.capture());
        assertEquals(21L, captor.getValue().getStudentId());
    }

    @Test
    void coursesByStudentIdDelegatesAndReturnsPage() throws Exception {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setCourseId(7L);
        courseDTO.setCourseName("Java");
        Page<CourseDTO> page = new PageImpl<>(List.of(courseDTO));
        when(courseService.fetchCoursesForStudent(5L, 0, 3)).thenReturn(page);

        mockMvc.perform(get("/students/{studentId}/courses", 5L)
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].courseId").value(7))
                .andExpect(jsonPath("$.content[0].courseName").value("Java"));
    }

    @Test
    void nonSubscribedCoursesByStudentIdDelegatesAndReturnsPage() throws Exception {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setCourseId(8L);
        courseDTO.setCourseName("Spring");
        Page<CourseDTO> page = new PageImpl<>(List.of(courseDTO));
        when(courseService.fetchNonEnrolledInCoursesForStudent(5L, 1, 2)).thenReturn(page);

        mockMvc.perform(get("/students/{studentId}/other-courses", 5L)
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].courseId").value(8))
                .andExpect(jsonPath("$.content[0].courseName").value("Spring"));
    }

    @Test
    void loadStudentByEmailDelegatesAndReturnsResult() throws Exception {
        when(studentService.loadStudentByEmail("john@site.com")).thenReturn(studentDTO(5L, "John"));

        mockMvc.perform(get("/students/find").param("email", "john@site.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(5))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    private StudentDTO studentDTO(Long id, String firstName) {
        StudentDTO dto = new StudentDTO();
        dto.setStudentId(id);
        dto.setFirstName(firstName);
        dto.setLastName("Doe");
        dto.setLevel("Beginner");
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("john@site.com");
        dto.setUser(userDTO);
        return dto;
    }
}
