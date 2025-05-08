package ro.ase.acs.mind_path.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ro.ase.acs.mind_path.dto.request.QuestionCreationDto;
import ro.ase.acs.mind_path.dto.request.QuestionUpdateDto;
import ro.ase.acs.mind_path.dto.response.QuestionSummaryDto;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.service.QuestionService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/quizzes/{quizId}/questions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Long> addQuestionToQuiz(@PathVariable Long quizId,
                                                  @RequestBody @Valid QuestionCreationDto dto,
                                                  @AuthenticationPrincipal User user) {
        Long questionId = questionService.addQuestionToQuiz(quizId, dto, user);
        return ResponseEntity.ok(questionId);
    }

    @GetMapping("/quizzes/{quizId}/questions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuestionSummaryDto>> getQuestionsForQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(questionService.getQuestionsForQuiz(quizId));
    }

    @PatchMapping("/questions/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> updateQuestion(@PathVariable Long id,
                                                @RequestBody @Valid QuestionUpdateDto dto,
                                                @AuthenticationPrincipal User user) {
        questionService.updateQuestion(id, dto, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/questions/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id,
                                               @AuthenticationPrincipal User user) {
        questionService.deleteQuestion(id, user);
        return ResponseEntity.noContent().build();
    }
}
