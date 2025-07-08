package ro.ase.acs.mind_path.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.ase.acs.mind_path.entity.Answer;
import ro.ase.acs.mind_path.entity.Question;
import ro.ase.acs.mind_path.entity.QuizAttempt;
import ro.ase.acs.mind_path.entity.UserResponse;
import ro.ase.acs.mind_path.entity.enums.AttemptStatus;
import ro.ase.acs.mind_path.entity.enums.QuestionType;
import ro.ase.acs.mind_path.repository.AnswerRepository;
import ro.ase.acs.mind_path.repository.QuestionRepository;
import ro.ase.acs.mind_path.repository.QuizAttemptRepository;
import ro.ase.acs.mind_path.repository.UserResponseRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GradingService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserResponseRepository userResponseRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    // REFACTOR
    public float grade(List<Question> questions, List<UserResponse> responses) {
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
                float s = 0f;
                if (!questionResponses.isEmpty()) {
                    Float finalScore = questionResponses.getFirst().getFinalScore();
                    if (finalScore != null) {
                        s = Math.max(0, Math.min(1, finalScore));
                    }
                }
                totalCorrect += s;

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

    @Transactional
    public void regradeAttempt(QuizAttempt attempt) {
        List<Question> questions  = questionRepository.findByQuizQuizId(attempt.getQuiz().getQuizId());
        List<UserResponse> responses = userResponseRepository.findByQuizAttemptAttemptId(attempt.getAttemptId());

        float score = grade(questions, responses);
        attempt.setScore(score);
        attempt.setStatus(AttemptStatus.GRADED);
        attempt.setHasUngradedOpenEnded(
                responses.stream().anyMatch(r -> r.getQuestion().getType() == QuestionType.OPEN_ENDED
                        && (r.getTeacherScore() == null || r.getAiScore() == null)));
        quizAttemptRepository.save(attempt);
    }

}
