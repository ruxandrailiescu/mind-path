package ro.ase.acs.mind_path.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.ase.acs.mind_path.dto.mapper.QuestionMapper;
import ro.ase.acs.mind_path.dto.request.AnswerCreationDto;
import ro.ase.acs.mind_path.dto.response.AnswerSummaryDto;
import ro.ase.acs.mind_path.entity.Answer;
import ro.ase.acs.mind_path.entity.Question;
import ro.ase.acs.mind_path.entity.Quiz;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.exception.AnswerNotFoundException;
import ro.ase.acs.mind_path.exception.BadRequestException;
import ro.ase.acs.mind_path.exception.ForbiddenException;
import ro.ase.acs.mind_path.exception.QuestionNotFoundException;
import ro.ase.acs.mind_path.repository.AnswerRepository;
import ro.ase.acs.mind_path.repository.QuestionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final QuestionMapper questionMapper;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    public void addAnswerToQuestion(Long questionId, AnswerCreationDto dto, User user) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(QuestionNotFoundException::new);

        Quiz quiz = question.getQuiz();

        if (!quiz.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("Only the teacher that created the quiz can add answers to questions");
        }

        boolean duplicateExists = question.getAnswers()
                .stream()
                .anyMatch(a -> a.getAnswerText().equals(dto.getAnswerText()));

        if (duplicateExists) {
            throw new BadRequestException("An answer with the same text already exists for this question");
        }

        Answer answer = Answer.builder()
                .answerText(dto.getAnswerText())
                .isCorrect(dto.getIsCorrect())
                .question(question)
                .build();

        question.getAnswers().add(answer);
        answerRepository.save(answer);
    }

    public List<AnswerSummaryDto> getAnswersForQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(QuestionNotFoundException::new);

        return question.getAnswers()
                .stream()
                .map(questionMapper::toAnswerSummaryDto)
                .toList();
    }

    public void updateAnswer(Long id, AnswerCreationDto dto, User user) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(AnswerNotFoundException::new);

        Question question = answer.getQuestion();
        Quiz quiz = question.getQuiz();

        if (!quiz.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("Only the teacher that created the quiz can edit answers");
        }

        boolean duplicateExists = question.getAnswers()
                .stream()
                .anyMatch(a -> a.getAnswerText().equals(dto.getAnswerText()));

        if (duplicateExists) {
            throw new BadRequestException("An answer with the same text already exists for this question");
        }

        answer.setAnswerText(dto.getAnswerText());
        answer.setIsCorrect(dto.getIsCorrect());
        answerRepository.save(answer);
    }

    public void deleteAnswer(Long id, User user) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(AnswerNotFoundException::new);

        Quiz quiz = answer.getQuestion().getQuiz();

        if (!quiz.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("Only the teacher that created the quiz can delete answers");
        }

        answerRepository.delete(answer);
    }
}
