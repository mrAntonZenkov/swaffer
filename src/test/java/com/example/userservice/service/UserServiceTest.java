package com.example.userservice.service;
import com.example.userservice.dto.UserDTO;
import com.example.userservice.dto.UserRequestDTO;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_ValidRequest_ReturnsUserDTO() {
        UserRequestDTO request = new UserRequestDTO();
        request.setName("Test User");
        request.setEmail("test@example.com");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName(request.getName());
        savedUser.setEmail(request.getEmail());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO result = userService.createUser(request);

        assertNotNull(result.getId());
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getEmail(), result.getEmail());
    }

    @Test
    void getAllUsers_ReturnsListOfUsers() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        List<UserDTO> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals(user.getName(), result.get(0).getName());
    }

    @Test
    void updateUser_ExistingId_ReturnsUpdatedUser() {
        Long userId = 1L;
        UserRequestDTO request = new UserRequestDTO();
        request.setName("Updated Name");
        request.setEmail("updated@example.com");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserDTO result = userService.updateUser(userId, request);

        assertEquals(request.getName(), result.getName());
        assertEquals(request.getEmail(), result.getEmail());
    }

    @Test
    void updateUser_NonExistingId_ThrowsException() {
        Long userId = 999L;
        UserRequestDTO request = new UserRequestDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    void deleteUser_ValidId_DeletesUser() {
        Long userId = 1L;

        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }
}