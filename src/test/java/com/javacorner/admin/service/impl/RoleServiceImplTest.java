package com.javacorner.admin.service.impl;

import com.javacorner.admin.dao.RoleDao;
import com.javacorner.admin.entity.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleDao roleDao;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void createRoleSavesRoleWithProvidedNameAndReturnsSavedRole() {
        Role savedRole = new Role();
        savedRole.setRoleId(1L);
        savedRole.setName("Instructor");
        when(roleDao.save(org.mockito.ArgumentMatchers.any(Role.class))).thenReturn(savedRole);

        Role result = roleService.createRole("Instructor");

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleDao).save(captor.capture());
        assertEquals("Instructor", captor.getValue().getName());
        assertSame(savedRole, result);
    }
}
