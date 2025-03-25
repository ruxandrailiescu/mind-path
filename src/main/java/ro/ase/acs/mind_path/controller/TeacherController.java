package ro.ase.acs.mind_path.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.acs.mind_path.dto.request.TeacherCreationDto;
import ro.ase.acs.mind_path.service.UserService;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class TeacherController {

    private final UserService userService;

    @PostMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> create(@RequestBody @Valid TeacherCreationDto teacher) {
        Long teacherId = userService.createTeacher(teacher);
        return ResponseEntity
                .created(URI.create("/users/" + teacherId))
                .body("Teacher created successfully");
    }
}
