package ro.ase.acs.mind_path.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.acs.mind_path.dto.request.StudentCreationDto;
import ro.ase.acs.mind_path.dto.request.UserSessionDto;
import ro.ase.acs.mind_path.dto.response.AuthenticationDto;
import ro.ase.acs.mind_path.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationDto> createSession(@RequestBody @Valid UserSessionDto userSessionDto) {
        return ResponseEntity.ok(userService.createSession(userSessionDto));
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerAsStudent(@RequestBody @Valid StudentCreationDto student) {
        return ResponseEntity.ok(userService.createStudent(student));
    }
}
