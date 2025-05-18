package com.example.userservice.controller;

import com.example.userservice.dto.UserDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserModelAssembler {

    public EntityModel<UserDTO> toModel(UserDTO userDTO) {
        return EntityModel.of(userDTO,
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"),
                linkTo(methodOn(UserController.class).updateUser(userDTO.getId(), null)).withSelfRel()
        );
    }
}