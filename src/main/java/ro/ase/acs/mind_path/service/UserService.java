package ro.ase.acs.mind_path.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.ase.acs.mind_path.config.JwtService;
import ro.ase.acs.mind_path.dto.request.StudentCreationDto;
import ro.ase.acs.mind_path.dto.request.TeacherCreationDto;
import ro.ase.acs.mind_path.dto.request.UserSessionDto;
import ro.ase.acs.mind_path.dto.response.AuthenticationDto;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.entity.enums.UserRole;
import ro.ase.acs.mind_path.exception.UserAlreadyExistsException;
import ro.ase.acs.mind_path.exception.UserNotFoundException;
import ro.ase.acs.mind_path.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public String createStudent(StudentCreationDto student) {
        return createUser(student.getEmail(), student.getFirstName(), student.getLastName(), student.getPassword(), student.getUserType());
    }

    public String createTeacher(TeacherCreationDto teacher) {
        return createUser(teacher.getEmail(), teacher.getFirstName(), teacher.getLastName(), teacher.getPassword(), teacher.getUserType());
    }

    private String createUser(String email, String firstName, String lastName, String password, String userType) {
        userRepository.findByEmail(email)
                .ifPresent(t -> {
                    throw new UserAlreadyExistsException();
                });

        var user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(UserRole.valueOf(userType.toUpperCase()))
                .build();

        userRepository.save(user);

        return (userType + " saved in the database");
    }

    public AuthenticationDto createSession(UserSessionDto userSessionDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userSessionDto.getEmail(),
                        userSessionDto.getPassword()
                )
        );

        var user = userRepository.findByEmail(userSessionDto.getEmail())
                .orElseThrow(UserNotFoundException::new);

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationDto.builder()
                .token(jwtToken)
                .build();
    }
}
