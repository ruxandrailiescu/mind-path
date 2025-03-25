package ro.ase.acs.mind_path.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.acs.mind_path.dto.request.QuizCreationDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/quizzes")
public class QuizController {

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<String> createQuiz(@RequestBody @Valid QuizCreationDto quiz) {
        return null;
    }
}
