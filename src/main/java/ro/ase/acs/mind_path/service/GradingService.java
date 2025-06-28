package ro.ase.acs.mind_path.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ro.ase.acs.mind_path.entity.Answer;
import ro.ase.acs.mind_path.entity.Question;
import ro.ase.acs.mind_path.entity.QuizAttempt;
import ro.ase.acs.mind_path.entity.UserResponse;
import ro.ase.acs.mind_path.entity.enums.QuestionType;
import ro.ase.acs.mind_path.repository.AnswerRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GradingService {

    private static final Logger logger = LoggerFactory.getLogger(GradingService.class);

    private final AnswerRepository answerRepository;

    // REFACTOR
    public float grade(QuizAttempt attempt, List<Question> questions, List<UserResponse> responses) {
        float totalCorrect = 0;
        float scoreMultipleChoiceQuestion;
        float numCorrect;
        float numIncorrect;
        float numCorrectSelected;
        float numIncorrectSelected;

        for (Question question : questions) {
            List<UserResponse> questionResponses = responses.stream()
                    .filter(r -> r.getQuestion().getQuestionId().equals(question.getQuestionId()))
                    .toList();

            if (question.getType() == QuestionType.OPEN_ENDED) {
                if (!questionResponses.isEmpty() &&
                        questionResponses.getFirst().getOpenEndedAnswer() != null &&
                        !questionResponses.getFirst().getOpenEndedAnswer().trim().isEmpty()) {

                    logger.info("Open-ended response received for question {} in attempt {}",
                            question.getQuestionId(), attempt.getAttemptId());
                }

            } else if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
                numCorrectSelected = 0;
                numIncorrectSelected = 0;

                List<Long> correctAnswerIds = answerRepository.findByQuestionQuestionIdAndIsCorrect(
                                question.getQuestionId(), true).stream()
                        .map(Answer::getAnswerId)
                        .toList();
                numCorrect = correctAnswerIds.size();
                numIncorrect = answerRepository.findByQuestionQuestionIdAndIsCorrect(
                        question.getQuestionId(), false).stream().count();
                List<Long> selectedAnswerIds = questionResponses.stream()
                        .map(r -> r.getSelectedAnswer().getAnswerId())
                        .toList();

                for (Long selectedAnswer : selectedAnswerIds) {
                    if (correctAnswerIds.contains(selectedAnswer)) {
                        numCorrectSelected++;
                    } else {
                        numIncorrectSelected++;
                    }
                }

                scoreMultipleChoiceQuestion = (numCorrectSelected / numCorrect)
                        - (numIncorrectSelected / (numCorrect + numIncorrect));
                scoreMultipleChoiceQuestion = Math.max(0, scoreMultipleChoiceQuestion);
                totalCorrect += scoreMultipleChoiceQuestion;

            } else {
                if (!questionResponses.isEmpty() && questionResponses.getFirst().getIsCorrect()) {
                    totalCorrect++;
                }
            }
        }
        return (totalCorrect / questions.size()) * 100;
    }
}
