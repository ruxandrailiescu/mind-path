package ro.ase.acs.mind_path.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.ase.acs.mind_path.dto.mapper.QuestionMapper;
import ro.ase.acs.mind_path.dto.request.QuestionCreationDto;
import ro.ase.acs.mind_path.dto.request.QuestionUpdateDto;
import ro.ase.acs.mind_path.dto.response.AnswerSummaryDto;
import ro.ase.acs.mind_path.dto.response.QuestionSummaryDto;
import ro.ase.acs.mind_path.entity.Answer;
import ro.ase.acs.mind_path.entity.Question;
import ro.ase.acs.mind_path.entity.Quiz;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.entity.enums.QuestionDifficulty;
import ro.ase.acs.mind_path.entity.enums.QuestionType;
import ro.ase.acs.mind_path.entity.enums.QuizStatus;
import ro.ase.acs.mind_path.exception.BadRequestException;
import ro.ase.acs.mind_path.exception.ForbiddenException;
import ro.ase.acs.mind_path.exception.QuestionNotFoundException;
import ro.ase.acs.mind_path.exception.QuizNotFoundException;
import ro.ase.acs.mind_path.repository.QuestionRepository;
import ro.ase.acs.mind_path.repository.QuizRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public Long addQuestionToQuiz(Long quizId, QuestionCreationDto dto, User user) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(QuizNotFoundException::new);

        if (!quiz.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("Only the teacher that created the quiz can add questions");
        }

        if (quiz.getStatus() == QuizStatus.ARCHIVED) {
            throw new BadRequestException("Cannot add questions to an archived quiz");
        }

        boolean duplicateExists = quiz.getQuestions()
                .stream()
                .anyMatch(q -> q.getQuestionText().equalsIgnoreCase(dto.getQuestionText()));

        if (duplicateExists) {
            throw new BadRequestException("A question with the same text already exists in this quiz");
        }

        Question question = Question.builder()
                .questionText(dto.getQuestionText())
                .type(QuestionType.valueOf(dto.getType().toUpperCase()))
                .difficulty(QuestionDifficulty.valueOf(dto.getDifficulty().toUpperCase()))
                .quiz(quiz)
                .build();

        quiz.getQuestions().add(question);
        questionRepository.save(question);
        return question.getQuestionId();
    }

    public List<QuestionSummaryDto> getQuestionsForQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(QuizNotFoundException::new);

        return quiz.getQuestions()
                .stream()
                .map(questionMapper::toQuestionSummaryDto)
                .toList();
    }

    public void updateQuestion(Long id, QuestionUpdateDto dto, User user) {
        Question question = questionRepository.findById(id)
                .orElseThrow(QuestionNotFoundException::new);

        Quiz quiz = question.getQuiz();

        if (!quiz.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("Only the teacher that created the quiz can edit questions");
        }

        if (dto.isEmpty()) {
            throw new BadRequestException("Nothing to update");
        }

        if (dto.getQuestionText() != null) {
            boolean duplicateExists = quiz.getQuestions()
                    .stream()
                    .anyMatch(q ->
                            !q.getQuestionId().equals(question.getQuestionId()) &&
                            q.getQuestionText().equalsIgnoreCase(dto.getQuestionText())
                    );
            if (duplicateExists) {
                throw new BadRequestException("A question with the same text already exists in this quiz");
            }
            question.setQuestionText(dto.getQuestionText());
        }

        if (dto.getType() != null) {
            question.setType(QuestionType.valueOf(dto.getType().toUpperCase()));
        }

        if (dto.getDifficulty() != null) {
            question.setDifficulty(QuestionDifficulty.valueOf(dto.getDifficulty().toUpperCase()));
        }

        questionRepository.save(question);
    }

    public void deleteQuestion(Long id, User user) {
        Question question = questionRepository.findById(id)
                .orElseThrow(QuestionNotFoundException::new);

        Quiz quiz = question.getQuiz();

        if (!quiz.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("Only the teacher that created the quiz can delete questions");
        }

        questionRepository.delete(question);
    }
}
