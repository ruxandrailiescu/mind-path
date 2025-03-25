package ro.ase.acs.mind_path.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public Long createStudent(StudentCreationDto student) {
        return createUser(student.getEmail(), student.getFirstName(), student.getLastName(), student.getPassword(), student.getUserType());
    }

    public Long createTeacher(TeacherCreationDto teacher) {
        return createUser(teacher.getEmail(), teacher.getFirstName(), teacher.getLastName(), teacher.getPassword(), teacher.getUserType());
    }

    private Long createUser(String email, String firstName, String lastName, String password, String userType) {
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

        return user.getUserId();
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

    public void changePassword(Long userId, PasswordChangeDto passwordChangeDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(passwordChangeDto.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        if (passwordEncoder.matches(passwordChangeDto.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be the same as the old password");
        }

        user.setPassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }
}
