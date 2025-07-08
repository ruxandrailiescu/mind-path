package ro.ase.acs.mind_path.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ase.acs.mind_path.entity.QuizAttempt;
import ro.ase.acs.mind_path.entity.UserResponse;
import ro.ase.acs.mind_path.exception.QuizAttemptException;
import ro.ase.acs.mind_path.repository.UserResponseRepository;

@Service
@RequiredArgsConstructor
public class TeacherGradingService {

    private final UserResponseRepository userResponseRepository;
    private final GradingService gradingService;

    @Transactional
    public void gradeOpenEnded(Long attemptId, Long questionId, float score) {
        if (score < 0 || score > 1) {
            throw new IllegalArgumentException("Score must be between 0 and 1");
        }
        UserResponse resp = userResponseRepository
                .findByQuizAttemptAttemptIdAndQuestionQuestionId(attemptId, questionId)
                .orElseThrow(() -> new QuizAttemptException("Response not found"));

        resp.setTeacherScore(score);
        resp.setIsCorrect(score >= 0.5);
        userResponseRepository.save(resp);

        QuizAttempt attempt = resp.getQuizAttempt();
        gradingService.regradeAttempt(attempt);
    }
}
