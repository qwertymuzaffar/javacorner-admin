package com.javacorner.admin.service.impl;

import com.javacorner.admin.dao.InstructorDao;
import com.javacorner.admin.dto.InstructorDTO;
import com.javacorner.admin.dto.UserDTO;
import com.javacorner.admin.entity.Course;
import com.javacorner.admin.entity.Instructor;
import com.javacorner.admin.entity.User;
import com.javacorner.admin.mapper.InstructorMapper;
import com.javacorner.admin.service.CourseService;
import com.javacorner.admin.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstructorServiceImplTest {

    @Mock
    private InstructorDao instructorDao;

    @Mock
    private InstructorMapper instructorMapper;

    @Mock
    private UserService userService;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private InstructorServiceImpl instructorService;

    @Test
    void loadInstructorByIdReturnsEntityWhenPresent() {
        Instructor instructor = instructor(3L);
        when(instructorDao.findById(3L)).thenReturn(Optional.of(instructor));

        Instructor result = instructorService.loadInstructorById(3L);

        assertSame(instructor, result);
    }

    @Test
    void loadInstructorByIdThrowsWhenMissing() {
        when(instructorDao.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> instructorService.loadInstructorById(99L));

        assertEquals("Instructor with ID99 not found", exception.getMessage());
    }

    @Test
    void findInstructorsByNameReturnsMappedPage() {
        PageRequest pageRequest = PageRequest.of(0, 2);
        Instructor instructor = instructor(1L);
        InstructorDTO dto = instructorDto(1L);
        Page<Instructor> page = new PageImpl<>(List.of(instructor), pageRequest, 1);

        when(instructorDao.findInstructorsByName("John", pageRequest)).thenReturn(page);
        when(instructorMapper.fromInstructor(instructor)).thenReturn(dto);

        Page<InstructorDTO> result = instructorService.findInstructorsByName("John", 0, 2);

        assertEquals(1, result.getTotalElements());
        assertEquals(List.of(dto), result.getContent());
    }

    @Test
    void loadInstructorByEmailReturnsMappedDto() {
        Instructor instructor = instructor(5L);
        InstructorDTO dto = instructorDto(5L);
        when(instructorDao.findInstructorByEmail("john@site.com")).thenReturn(instructor);
        when(instructorMapper.fromInstructor(instructor)).thenReturn(dto);

        InstructorDTO result = instructorService.loadInstructorByEmail("john@site.com");

        assertSame(dto, result);
    }

    @Test
    void createInstructorCreatesUserAssignsRoleAndSavesInstructor() {
        InstructorDTO request = instructorDto(8L);
        User createdUser = user("instructor@site.com", "pass123");
        Instructor mappedInstructor = instructor(8L);
        Instructor savedInstructor = instructor(8L);
        savedInstructor.setUser(createdUser);
        InstructorDTO response = instructorDto(8L);

        when(userService.createUser("instructor@site.com", "pass123")).thenReturn(createdUser);
        when(instructorMapper.fromInstructorDTO(request)).thenReturn(mappedInstructor);
        when(instructorDao.save(mappedInstructor)).thenReturn(savedInstructor);
        when(instructorMapper.fromInstructor(savedInstructor)).thenReturn(response);

        InstructorDTO result = instructorService.createInstructor(request);

        assertSame(response, result);
        assertSame(createdUser, mappedInstructor.getUser());
        verify(userService).assignRoleToUser("instructor@site.com", "Instructor");
    }

    @Test
    void updateInstructorPreservesUserAndCourses() {
        InstructorDTO request = instructorDto(11L);
        User existingUser = user("existing@site.com", "encoded");
        Course existingCourse = course(20L);
        Instructor loadedInstructor = instructor(11L);
        loadedInstructor.setUser(existingUser);
        loadedInstructor.setCourses(Set.of(existingCourse));
        Instructor mappedInstructor = instructor(11L);
        Instructor updatedInstructor = instructor(11L);
        updatedInstructor.setUser(existingUser);
        updatedInstructor.setCourses(Set.of(existingCourse));
        InstructorDTO response = instructorDto(11L);

        when(instructorDao.findById(11L)).thenReturn(Optional.of(loadedInstructor));
        when(instructorMapper.fromInstructorDTO(request)).thenReturn(mappedInstructor);
        when(instructorDao.save(mappedInstructor)).thenReturn(updatedInstructor);
        when(instructorMapper.fromInstructor(updatedInstructor)).thenReturn(response);

        InstructorDTO result = instructorService.updateInstructor(request);

        assertSame(response, result);
        assertSame(existingUser, mappedInstructor.getUser());
        assertEquals(Set.of(existingCourse), mappedInstructor.getCourses());
    }

    @Test
    void updateInstructorThrowsWhenInstructorMissing() {
        InstructorDTO request = instructorDto(11L);
        when(instructorDao.findById(11L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> instructorService.updateInstructor(request));

        assertEquals("Instructor with ID11 not found", exception.getMessage());
        verify(instructorMapper, never()).fromInstructorDTO(any());
        verify(instructorDao, never()).save(any());
    }

    @Test
    void fetchInstructorsReturnsMappedList() {
        Instructor one = instructor(1L);
        Instructor two = instructor(2L);
        InstructorDTO oneDto = instructorDto(1L);
        InstructorDTO twoDto = instructorDto(2L);

        when(instructorDao.findAll()).thenReturn(List.of(one, two));
        when(instructorMapper.fromInstructor(one)).thenReturn(oneDto);
        when(instructorMapper.fromInstructor(two)).thenReturn(twoDto);

        List<InstructorDTO> result = instructorService.fetchInstructors();

        assertEquals(List.of(oneDto, twoDto), result);
    }

    @Test
    void removeInstructorRemovesCoursesThenDeletesInstructor() {
        Course firstCourse = course(100L);
        Course secondCourse = course(101L);
        Instructor instructor = instructor(6L);
        Set<Course> courses = new LinkedHashSet<>();
        courses.add(firstCourse);
        courses.add(secondCourse);
        instructor.setCourses(courses);
        when(instructorDao.findById(6L)).thenReturn(Optional.of(instructor));

        instructorService.removeInstructor(6L);

        InOrder order = inOrder(courseService, instructorDao);
        order.verify(courseService).removeCourse(100L);
        order.verify(courseService).removeCourse(101L);
        order.verify(instructorDao).deleteById(6L);
    }

    @Test
    void removeInstructorThrowsWhenInstructorMissing() {
        when(instructorDao.findById(7L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> instructorService.removeInstructor(7L));

        assertEquals("Instructor with ID7 not found", exception.getMessage());
        verify(courseService, never()).removeCourse(any());
        verify(instructorDao, never()).deleteById(any());
    }

    private Instructor instructor(Long id) {
        Instructor instructor = new Instructor();
        instructor.setInstructorId(id);
        instructor.setFirstName("John");
        instructor.setLastName("Doe");
        instructor.setSummary("Senior");
        return instructor;
    }

    private InstructorDTO instructorDto(Long id) {
        InstructorDTO dto = new InstructorDTO();
        dto.setInstructorId(id);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setSummary("Senior");
        dto.setUser(userDto("instructor@site.com", "pass123"));
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
