package ro.ase.acs.mind_path.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import ro.ase.acs.mind_path.config.JwtService;
import ro.ase.acs.mind_path.dto.request.PasswordChangeDto;
import ro.ase.acs.mind_path.dto.request.StudentCreationDto;
import ro.ase.acs.mind_path.dto.request.TeacherCreationDto;
import ro.ase.acs.mind_path.dto.request.UserSessionDto;
import ro.ase.acs.mind_path.dto.response.AuthenticationDto;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.entity.enums.UserRole;
import ro.ase.acs.mind_path.exception.BadRequestException;
import ro.ase.acs.mind_path.exception.UserAlreadyExistsException;
import ro.ase.acs.mind_path.exception.UserNotFoundException;
import ro.ase.acs.mind_path.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void UserService_CreateSession_ThrowExceptionWhenCredentialsAreInvalid() {
        UserSessionDto userSessionDto = new UserSessionDto("test.user@example.com", "mockpass");

        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> userService.createSession(userSessionDto));

        verify(jwtService, never()).generateToken(any());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void UserService_CreateSession_ThrowExceptionWhenCredentialsAreValidButRepositoryFails() {
        UserSessionDto userSessionDto = new UserSessionDto("test.user@example.com", "mockpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("test.user@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.createSession(userSessionDto));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail("test.user@example.com");
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void UserService_CreateSession_ReturnValidJwtWhenCredentialsAreValid() {
        UserSessionDto userSessionDto = new UserSessionDto("test.user@example.com", "mockpass");
        String generatedJwt = "mockGeneratedJwt";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("test.user@example.com")).thenReturn(Optional.of(new User()));
        when(jwtService.generateToken(any())).thenReturn(generatedJwt);

        AuthenticationDto jwtDto = userService.createSession(userSessionDto);

        assertNotNull(jwtDto);
        assertEquals(generatedJwt, jwtDto.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail("test.user@example.com");
        verify(jwtService, times(1)).generateToken(any());
    }

    @Test
    void UserService_ChangePassword_ThrowExceptionWhenWrongOldPassword() {
        User user = new User();
        user.setUserId(1L);
        user.setPassword("encodedOldPass");

        PasswordChangeDto passwordChangeDto = new PasswordChangeDto("wrongOldPass", "newPass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPass", "encodedOldPass")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> userService.changePassword(1L, passwordChangeDto));

        verify(userRepository, never()).save(any());
    }

    @Test
    void UserService_ChangePassword_ThrowExceptionWhenNewPasswordSameAsOldPassword() {
        User user = new User();
        user.setUserId(1L);
        user.setPassword("encodedOldPass");

        PasswordChangeDto passwordChangeDto = new PasswordChangeDto("correctOldPass", "oldPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctOldPass", "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.matches("oldPassword", "encodedOldPass")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.changePassword(1L, passwordChangeDto));

        verify(userRepository, never()).save(any());
    }

    @Test
    void UserService_ChangePassword_ChangePasswordWithValidInputs() {
        User user = new User();
        user.setUserId(1L);
        user.setPassword("encodedOldPass");

        PasswordChangeDto passwordChangeDto = new PasswordChangeDto("correctOldPass", "newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctOldPass", "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.matches("newPassword", "encodedOldPass")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPass");

        userService.changePassword(1L, passwordChangeDto);

        assertEquals("encodedNewPass", user.getPassword(), "The password should be updated");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(user);
    }

    @Test
    void UserService_CreateStudent_ThrowExceptionWhenStudentExists() {
        StudentCreationDto studentCreationDto = new StudentCreationDto();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> userService.createStudent(studentCreationDto));
    }

    @Test
    void UserService_CreateTeacher_ThrowExceptionWhenTeacherExists() {
        TeacherCreationDto teacherCreationDto = new TeacherCreationDto();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> userService.createTeacher(teacherCreationDto));
    }

    @Test
    void UserService_CreateStudent_SuccessfullyCreatesStudent() {
        StudentCreationDto dto = new StudentCreationDto(
                "student@example.com",
                "password123",
                "STUDENT",
                "Alice",
                "Smith"
        );

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        String result = userService.createStudent(dto);

        assertEquals("STUDENT saved in the database", result);
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("student@example.com") &&
                        user.getFirstName().equals("Alice") &&
                        user.getLastName().equals("Smith") &&
                        user.getPassword().equals("encodedPassword") &&
                        user.getRole() == UserRole.STUDENT
        ));
    }

    @Test
    void UserService_CreateTeacher_SuccessfullyCreatesTeacher() {
        TeacherCreationDto dto = new TeacherCreationDto(
                "teacher@example.com",
                "securePass",
                "TEACHER",
                "John",
                "Doe"
        );

        when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("securePass")).thenReturn("encodedSecurePass");

        String result = userService.createTeacher(dto);

        assertEquals("TEACHER saved in the database", result);
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("teacher@example.com") &&
                        user.getFirstName().equals("John") &&
                        user.getLastName().equals("Doe") &&
                        user.getPassword().equals("encodedSecurePass") &&
                        user.getRole() == UserRole.TEACHER
        ));
    }
}
