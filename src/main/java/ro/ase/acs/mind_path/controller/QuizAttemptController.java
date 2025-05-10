package ro.ase.acs.mind_path.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ro.ase.acs.mind_path.dto.request.StartAttemptRequest;
import ro.ase.acs.mind_path.dto.request.SubmitAnswerRequest;
import ro.ase.acs.mind_path.dto.request.SubmitAttemptRequest;
import ro.ase.acs.mind_path.dto.response.AttemptResponseDto;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.entity.UserResponse;
import ro.ase.acs.mind_path.service.QuizAttemptService;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class QuizAttemptController {
    private final QuizAttemptService quizAttemptService;

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
    public ResponseEntity<UserResponse> submitAnswer(
            @PathVariable Long attemptId,
            @RequestBody SubmitAnswerRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        UserResponse response = quizAttemptService.submitAnswer(attemptId, user.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/attempts/{attemptId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttemptResponseDto> submitAttempt(
            @PathVariable Long attemptId,
            @RequestBody SubmitAttemptRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        AttemptResponseDto response = quizAttemptService.submitAttempt(attemptId, user.getUserId(), request);
        return ResponseEntity.ok(response);
    }
}