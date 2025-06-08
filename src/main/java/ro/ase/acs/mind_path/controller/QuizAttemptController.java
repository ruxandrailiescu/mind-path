package ro.ase.acs.mind_path.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ro.ase.acs.mind_path.dto.request.StartAttemptRequest;
import ro.ase.acs.mind_path.dto.request.SubmitAnswerRequest;
import ro.ase.acs.mind_path.dto.request.SubmitAttemptRequest;
import ro.ase.acs.mind_path.dto.response.AttemptResponseDto;
import ro.ase.acs.mind_path.dto.response.AttemptResultDto;
import ro.ase.acs.mind_path.dto.response.TeacherDashboardStatsDto;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.service.QuizAttemptService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class QuizAttemptController {
    private final QuizAttemptService quizAttemptService;

    @GetMapping("/teacher/dashboard/stats")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherDashboardStatsDto> getDashboardStats(Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        TeacherDashboardStatsDto stats = quizAttemptService.getDashboardStats(teacher.getUserId());
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/quizzes/attempts")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttemptResponseDto> startAttempt(
            @RequestBody StartAttemptRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        AttemptResponseDto response = quizAttemptService.startAttempt(user.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/attempts/{attemptId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttemptResponseDto> getAttempt(
            @PathVariable Long attemptId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        AttemptResponseDto response = quizAttemptService.getAttempt(attemptId, user.getUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/attempts/{attemptId}/responses")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> submitAnswer(
            @PathVariable Long attemptId,
            @RequestBody SubmitAnswerRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        quizAttemptService.submitAnswer(attemptId, user.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/attempts/{attemptId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttemptResponseDto> submitAttempt(
            @PathVariable Long attemptId,
            @RequestBody @Valid SubmitAttemptRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        AttemptResponseDto response = quizAttemptService.submitAttempt(attemptId, user.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/attempts/{attemptId}/results")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttemptResultDto> getAttemptResults(
            @PathVariable Long attemptId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        AttemptResultDto results = quizAttemptService.getAttemptResults(attemptId, user.getUserId());
        return ResponseEntity.ok(results);
    }

    @GetMapping("/attempts/in-progress")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AttemptResponseDto>> getInProgressAttempts(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<AttemptResponseDto> attempts = quizAttemptService.getInProgressAttempts(user.getUserId());
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/attempts/completed")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AttemptResultDto>> getCompletedAttempts(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<AttemptResultDto> attempts = quizAttemptService.getCompletedAttempts(user.getUserId());
        return ResponseEntity.ok(attempts);
    }

    @PostMapping("/attempts/{attemptId}/save-progress")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttemptResponseDto> saveProgress(
            @PathVariable Long attemptId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        AttemptResponseDto response = quizAttemptService.saveProgress(attemptId, user.getUserId());
        return ResponseEntity.ok(response);
    }
}