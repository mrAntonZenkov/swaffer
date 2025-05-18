package com.example.userservice.service;

import com.example.userservice.dto.UserDTO;
import com.example.userservice.dto.UserRequestDTO;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(UserService.class)
public class UserServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void createAndRetrieveUser_IntegrationTest() {
        UserRequestDTO request = new UserRequestDTO();
        request.setName("Integration Test");
        request.setEmail("integration@test.com");

        UserDTO created = userService.createUser(request);
        assertNotNull(created.getId());

        List<UserDTO> users = userService.getAllUsers();
        assertEquals(1, users.size());
        assertEquals("Integration Test", users.get(0).getName());
    }
}