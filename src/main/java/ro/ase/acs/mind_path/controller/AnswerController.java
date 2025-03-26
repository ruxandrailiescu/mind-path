package ro.ase.acs.mind_path.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ro.ase.acs.mind_path.dto.request.AnswerCreationDto;
import ro.ase.acs.mind_path.dto.response.AnswerSummaryDto;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.service.AnswerService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping("/questions/{questionId}/answers")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> addAnswerToQuestion(@PathVariable Long questionId,
                                                    @RequestBody @Valid AnswerCreationDto dto,
                                                    @AuthenticationPrincipal User user) {
        answerService.addAnswerToQuestion(questionId, dto, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/questions/{questionId}/answers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AnswerSummaryDto>> getAnswersForQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(answerService.getAnswersForQuestion(questionId));
    }

    @PatchMapping("/answers/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> updateAnswer(@PathVariable Long id,
                                             @RequestBody @Valid AnswerCreationDto dto,
                                             @AuthenticationPrincipal User user) {
        answerService.updateAnswer(id, dto, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/answers/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long id,
                                             @AuthenticationPrincipal User user) {
        answerService.deleteAnswer(id, user);
        return ResponseEntity.noContent().build();
    }
}
