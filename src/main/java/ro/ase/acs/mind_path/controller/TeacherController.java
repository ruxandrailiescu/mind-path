package ro.ase.acs.mind_path.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ro.ase.acs.mind_path.dto.request.TeacherCreationDto;
import ro.ase.acs.mind_path.dto.response.StudentProgressDto;
import ro.ase.acs.mind_path.dto.response.TeacherDashboardStatsDto;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.service.TeacherDashboardService;
import ro.ase.acs.mind_path.service.UserService;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class TeacherController {

    private final UserService userService;
    private final TeacherDashboardService teacherDashboardService;

    @PostMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> create(@RequestBody @Valid TeacherCreationDto teacher) {
        Long teacherId = userService.createTeacher(teacher);
        return ResponseEntity
                .created(URI.create("/users/" + teacherId))
                .body("Teacher created successfully");
    }

    @GetMapping("/teacher/dashboard/students")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<StudentProgressDto>> getStudentProgress(Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        List<StudentProgressDto> studentProgress = teacherDashboardService.getStudentProgress(teacher.getUserId());
        return ResponseEntity.ok(studentProgress);
    }

    @GetMapping("/teacher/dashboard/stats")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherDashboardStatsDto> getDashboardStats(Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        TeacherDashboardStatsDto stats = teacherDashboardService.getDashboardStats(teacher.getUserId());
        return ResponseEntity.ok(stats);
    }
}
