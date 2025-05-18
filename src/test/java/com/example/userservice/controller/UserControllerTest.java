package com.example.userservice.controller;

import com.example.userservice.dto.UserDTO;
import com.example.userservice.dto.UserRequestDTO;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserModelAssembler userModelAssembler;

    private UserDTO userDTO;
    private UserRequestDTO requestDTO;
    private EntityModel<UserDTO> entityModel;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("Ivan Sidorov");
        userDTO.setEmail("ivan@example.com");

        Link selfLink = linkTo(methodOn(UserController.class).updateUser(1L, null)).withSelfRel();
        Link allUsersLink = linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users");
        entityModel = EntityModel.of(userDTO, selfLink, allUsersLink);

        requestDTO = new UserRequestDTO();
        requestDTO.setName("Ivan Sidorov");
        requestDTO.setEmail("ivan@example.com");
    }

    @Test
    void createUser_ValidRequest_ReturnsCreatedWithHateoas() throws Exception {
        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(userDTO);
        when(userModelAssembler.toModel(any(UserDTO.class))).thenReturn(entityModel);

        ResultActions result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Ivan Sidorov")))
                .andExpect(jsonPath("$.email", is("ivan@example.com")))
                .andExpect(jsonPath("$._links.self.href", containsString("/api/users/1")))
                .andExpect(jsonPath("$._links['all-users'].href", containsString("/api/users")));
    }

    @Test
    void getAllUsers_ReturnsListWithHateoasLinks() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.singletonList(userDTO));
        when(userModelAssembler.toModel(any(UserDTO.class))).thenReturn(entityModel);

        ResultActions result = mockMvc.perform(get("/api/users"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.userDTOList", hasSize(1)))  // ← изменено здесь
                .andExpect(jsonPath("$._embedded.userDTOList[0].id", is(1)))  // ← и здесь
                .andExpect(jsonPath("$._embedded.userDTOList[0]._links.self.href", containsString("/api/users/1")))
                .andExpect(jsonPath("$._embedded.userDTOList[0]._links['all-users'].href", containsString("/api/users")));
    }

    @Test
    void updateUser_ValidRequest_ReturnsUpdatedUserWithHateoas() throws Exception {
        Long userId = 1L;
        UserRequestDTO updateRequest = new UserRequestDTO();
        updateRequest.setName("Updated Name");
        updateRequest.setEmail("updated@example.com");

        UserDTO updatedUser = new UserDTO();
        updatedUser.setId(userId);
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("updated@example.com");

        Link selfLink = linkTo(methodOn(UserController.class).updateUser(userId, null)).withSelfRel();
        Link allUsersLink = linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users");
        EntityModel<UserDTO> updatedModel = EntityModel.of(updatedUser, selfLink, allUsersLink);

        when(userService.updateUser(eq(userId), any(UserRequestDTO.class))).thenReturn(updatedUser);
        when(userModelAssembler.toModel(any(UserDTO.class))).thenReturn(updatedModel);

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$._links.self.href", containsString("/api/users/1")));
    }

    @Test
    void deleteUser_ValidId_ReturnsNoContent() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void createUser_InvalidRequest_ReturnsBadRequest() throws Exception {
        UserRequestDTO invalidRequest = new UserRequestDTO();
        invalidRequest.setName("");
        invalidRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", is("Name is required")))
                .andExpect(jsonPath("$.email", is("Invalid email format")));
    }
}