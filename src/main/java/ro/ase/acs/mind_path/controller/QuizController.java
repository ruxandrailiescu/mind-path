package ro.ase.acs.mind_path.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ro.ase.acs.mind_path.dto.request.QuizCreationDto;
import ro.ase.acs.mind_path.dto.request.QuizUpdateDto;
import ro.ase.acs.mind_path.dto.response.QuizSummaryDto;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.service.QuizService;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/quizzes")
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Long> createQuiz(@RequestBody @Valid QuizCreationDto quiz,
                                             @AuthenticationPrincipal User user) {
        Long quizId = quizService.createQuiz(quiz, user);
        return ResponseEntity.ok(quizId);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuizSummaryDto>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuizSummaryDto>> getActiveQuizzes() {
        return ResponseEntity.ok(quizService.getActiveQuizzes());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuizSummaryDto> getQuizById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> updateQuiz(@PathVariable Long id,
                                           @RequestBody @Valid QuizUpdateDto dto,
                                           @AuthenticationPrincipal User user) {
        quizService.updateQuiz(id, dto, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id,
                                           @AuthenticationPrincipal User user) {
        quizService.deleteQuiz(id, user);
        return ResponseEntity.noContent().build();
    }
}
