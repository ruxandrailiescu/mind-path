package ro.ase.acs.mind_path.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ro.ase.acs.mind_path.dto.request.PasswordChangeDto;
import ro.ase.acs.mind_path.dto.request.StudentCreationDto;
import ro.ase.acs.mind_path.dto.request.UserSessionDto;
import ro.ase.acs.mind_path.dto.response.AuthenticationDto;
import ro.ase.acs.mind_path.dto.response.UserProfileDto;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.service.UserService;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class UserController {

    private final UserService userService;

    @PostMapping("/auth/login")
    public ResponseEntity<AuthenticationDto> createSession(@RequestBody @Valid UserSessionDto userSessionDto) {
        return ResponseEntity.ok(userService.createSession(userSessionDto));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<String> registerAsStudent(@RequestBody @Valid StudentCreationDto student) {
        Long studentId = userService.createStudent(student);
        return ResponseEntity
                .created(URI.create("/users/" + studentId))
                .body("Student registered successfully");
    }

    @PatchMapping("/auth/change-password")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public ResponseEntity<String> changePassword(@RequestBody @Valid PasswordChangeDto passwordChangeDto,
                                                 HttpServletRequest request) {
        userService.changePassword((Long) request.getAttribute("user id"), passwordChangeDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(new UserProfileDto(user));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileDto> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(new UserProfileDto(user));
    }
}
