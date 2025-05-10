package ro.ase.acs.mind_path.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ro.ase.acs.mind_path.dto.request.CreateSessionRequest;
import ro.ase.acs.mind_path.dto.response.QuizSessionResponseDto;
import ro.ase.acs.mind_path.entity.QuizSession;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.service.QuizSessionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/quiz-sessions")
public class QuizSessionController {
    private final QuizSessionService quizSessionService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<QuizSessionResponseDto> createSession(
            @RequestBody CreateSessionRequest request,
            Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        QuizSession session = quizSessionService.createSession(teacher.getUserId(), request);

        QuizSessionResponseDto responseDto = QuizSessionResponseDto.builder()
                .sessionId(session.getSessionId())
                .quizId(session.getQuiz().getQuizId())
                .accessCode(session.getAccessCode())
                .createdBy(session.getCreatedBy().getUserId())
                .status(session.getStatus())
                .createdAt(session.getCreatedAt().toString())
                .expiresAt(session.getEndTime().toString())
                .build();
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/validate")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Boolean> validateAccessCode(
            @RequestParam String accessCode) {
        try {
            quizSessionService.validateAccessCode(accessCode);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
}