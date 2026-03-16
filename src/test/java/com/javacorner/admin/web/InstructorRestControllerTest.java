package com.javacorner.admin.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javacorner.admin.dto.CourseDTO;
import com.javacorner.admin.dto.InstructorDTO;
import com.javacorner.admin.dto.UserDTO;
import com.javacorner.admin.entity.User;
import com.javacorner.admin.service.CourseService;
import com.javacorner.admin.service.InstructorService;
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
class InstructorRestControllerTest {

    @Mock
    private InstructorService instructorService;

    @Mock
    private UserService userService;

    @Mock
    private CourseService courseService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        InstructorRestController controller = new InstructorRestController(instructorService, userService, courseService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void searchInstructorsUsesParamsAndReturnsPage() throws Exception {
        Page<InstructorDTO> page = new PageImpl<>(List.of(instructorDTO(1L, "John"), instructorDTO(2L, "Jane")));
        when(instructorService.findInstructorsByName("ja", 1, 2)).thenReturn(page);

        mockMvc.perform(get("/instructors")
                        .param("keyword", "ja")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].instructorId").value(1))
                .andExpect(jsonPath("$.content[1].instructorId").value(2));
    }

    @Test
    void findAllInstructorsReturnsList() throws Exception {
        when(instructorService.fetchInstructors()).thenReturn(List.of(instructorDTO(1L, "John")));

        mockMvc.perform(get("/instructors/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].instructorId").value(1));
    }

    @Test
    void deleteInstructorDelegatesToService() throws Exception {
        mockMvc.perform(delete("/instructors/{instructorId}", 9L))
                .andExpect(status().isOk());

        verify(instructorService).removeInstructor(9L);
    }

    @Test
    void saveInstructorThrowsWhenEmailAlreadyExists() throws Exception {
        InstructorDTO requestBody = instructorDTO( null, "John");
        User existing = new User();
        existing.setEmail("john@site.com");
        when(userService.loadUserByEmail("john@site.com")).thenReturn(existing);

        assertThrows(NestedServletException.class, () -> mockMvc.perform(post("/instructors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))));
    }

    @Test
    void saveInstructorCreatesWhenEmailIsAvailable() throws Exception {
        InstructorDTO requestBody = instructorDTO(null, "John");
        InstructorDTO responseBody = instructorDTO(15L, "John");
        when(userService.loadUserByEmail("john@site.com")).thenReturn(null);
        when(instructorService.createInstructor(any(InstructorDTO.class))).thenReturn(responseBody);

        mockMvc.perform(post("/instructors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instructorId").value(15));

        ArgumentCaptor<InstructorDTO> captor = ArgumentCaptor.forClass(InstructorDTO.class);
        verify(instructorService).createInstructor(captor.capture());
        assertEquals("john@site.com", captor.getValue().getUser().getEmail());
    }

    @Test
    void updateInstructorOverridesIdFromPathAndDelegates() throws Exception {
        InstructorDTO requestBody = instructorDTO(99L, "Jane");
        InstructorDTO responseBody = instructorDTO(21L, "Jane");
        when(instructorService.updateInstructor(any(InstructorDTO.class))).thenReturn(responseBody);

        mockMvc.perform(put("/instructors/{instructorId}", 21L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instructorId").value(21));

        ArgumentCaptor<InstructorDTO> captor = ArgumentCaptor.forClass(InstructorDTO.class);
        verify(instructorService).updateInstructor(captor.capture());
        assertEquals(21L, captor.getValue().getInstructorId());
    }

    @Test
    void coursesByInstructorIdDelegatesAndReturnsPage() throws Exception {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setCourseId(7L);
        courseDTO.setCourseName("Java");
        Page<CourseDTO> page = new PageImpl<>(List.of(courseDTO));
        when(courseService.fetchCoursesForInstructor(5L, 0, 3)).thenReturn(page);

        mockMvc.perform(get("/instructors/{instructorId}/courses", 5L)
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].courseId").value(7))
                .andExpect(jsonPath("$.content[0].courseName").value("Java"));
    }

    @Test
    void loadInstructorByEmailDelegatesAndReturnsResult() throws Exception {
        when(instructorService.loadInstructorByEmail("john@site.com")).thenReturn(instructorDTO(5L, "John"));

        mockMvc.perform(get("/instructors/find").param("email", "john@site.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instructorId").value(5))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    private InstructorDTO instructorDTO(Long id, String firstName) {
        InstructorDTO dto = new InstructorDTO();
        dto.setInstructorId(id);
        dto.setFirstName(firstName);
        dto.setLastName("Doe");
        dto.setSummary("Backend");
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("john@site.com");
        dto.setUser(userDTO);
        return dto;
    }
}
