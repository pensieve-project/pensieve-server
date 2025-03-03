package ru.hse.pensieve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

public class AuthenticationControllerTest {
//    @Mock
//    private AuthenticationService authenticationService;
//
//    @InjectMocks
//    private AuthenticationController authenticationController;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testRegisterUser_Success() {
//        RegisterRequest request = new RegisterRequest("user", "test@gmail.com", "password");
//        AuthenticationResponse response = new AuthenticationResponse(1, "user");
//        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(CompletableFuture.completedFuture(response));
//
//        ResponseEntity<AuthenticationResponse> result = authenticationController.registerUser(request);
//
//        assertEquals(200, result.getStatusCode().value());
//        assertEquals(response, result.getBody());
//        verify(authenticationService).register(request);
//    }
//
//    @Test
//    public void testLoginUser_Success() {
//        AuthenticationRequest request = new AuthenticationRequest("user", "password");
//        AuthenticationResponse response = new AuthenticationResponse(1, "user");
//        when(authenticationService.login(any(AuthenticationRequest.class))).thenReturn(CompletableFuture.completedFuture(response));
//
//        ResponseEntity<AuthenticationResponse> result = authenticationController.loginUser(request);
//
//        assertEquals(200, result.getStatusCode().value());
//        assertEquals(response, result.getBody());
//        verify(authenticationService).login(request);
//    }
}
