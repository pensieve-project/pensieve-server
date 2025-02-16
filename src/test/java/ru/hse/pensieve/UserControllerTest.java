package ru.hse.pensieve;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserControllerTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUser_UserAlreadyExists() {
        User user = new User("existingUser", "hashedPassword");
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);
        ResponseEntity<Map<String, String>> response = userController.registerUser(user);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("The user is already registered!", response.getBody().get("message"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegisterUser_UserSuccessfullyRegistered() {
        User user = new User("newUser", "hashedPassword");
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);
        ResponseEntity<Map<String, String>> response = userController.registerUser(user);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("User successfully registered", response.getBody().get("message"));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("newUser", userCaptor.getValue().getUsername());
    }
}
