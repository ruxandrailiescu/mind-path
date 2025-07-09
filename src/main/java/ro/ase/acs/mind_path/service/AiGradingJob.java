package ro.ase.acs.mind_path.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.ase.acs.mind_path.entity.Question;
import ro.ase.acs.mind_path.entity.UserResponse;
import ro.ase.acs.mind_path.entity.enums.QuestionType;
import ro.ase.acs.mind_path.repository.UserResponseRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@EnableAsync
public class AiGradingJob {

    private final UserResponseRepository userResponseRepository;
    private final AiGradingService aiGradingService;
    private final GradingService gradingService;

    @Transactional
    public void gradeAttempt(Long attemptId) throws JsonProcessingException {

        List<UserResponse> openEnded = userResponseRepository
                .findByQuizAttemptAttemptIdAndQuestionType(attemptId, QuestionType.OPEN_ENDED);

        if (openEnded.isEmpty()) {
            return;
        }

        for (UserResponse ur : openEnded) {
            if (ur.getTeacherScore() != null || ur.getAiScore() != null) continue;

            Question q = ur.getQuestion();
            String rubric = ur.getSelectedAnswer().getAnswerText();
            AiGradingService.GradeResult gradeResult
                    = aiGradingService.grade(q.getQuestionText(), rubric, ur.getOpenEndedAnswer());

            ur.setAiScore(gradeResult.score());
            ur.setAiFeedback(gradeResult.feedback());
            ur.setIsCorrect(gradeResult.score() >= 0.5);
        }

        gradingService.regradeAttempt(openEnded.getFirst().getQuizAttempt());
    }
}

