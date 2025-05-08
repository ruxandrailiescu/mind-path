package ro.ase.acs.mind_path.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.ase.acs.mind_path.dto.mapper.QuestionMapper;
import ro.ase.acs.mind_path.dto.request.QuizCreationDto;
import ro.ase.acs.mind_path.dto.request.QuizUpdateDto;
import ro.ase.acs.mind_path.dto.response.AnswerSummaryDto;
import ro.ase.acs.mind_path.dto.response.QuestionSummaryDto;
import ro.ase.acs.mind_path.dto.response.QuizSummaryDto;
import ro.ase.acs.mind_path.entity.Quiz;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.entity.enums.QuizStatus;
import ro.ase.acs.mind_path.exception.BadRequestException;
import ro.ase.acs.mind_path.exception.ForbiddenException;
import ro.ase.acs.mind_path.exception.QuizNotFoundException;
import ro.ase.acs.mind_path.repository.QuizRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuestionMapper questionMapper;
    private final QuizRepository quizRepository;

    public Long createQuiz(QuizCreationDto dto, User user) {
        if (quizRepository.existsByTitleIgnoreCase(dto.getTitle())) {
            throw new BadRequestException("A quiz with this title already exists");
        }

        QuizStatus quizStatus = QuizStatus.DRAFT;

        if (dto.getStatus() != null) {
            quizStatus = QuizStatus.valueOf(dto.getStatus().toUpperCase());
        }

        Quiz quiz = Quiz.builder()
                .title(dto.getTitle())
                .status(quizStatus)
                .createdBy(user)
                .build();

        quizRepository.save(quiz);

        return quiz.getQuizId();
    }

    public List<QuizSummaryDto> getAllQuizzes() {
        return quizRepository.findAll()
                .stream()
                .map(this::mapToQuizSummaryDto)
                .toList();
    }

    public List<QuizSummaryDto> getActiveQuizzes() {
        return quizRepository.findAllByStatus(QuizStatus.ACTIVE)
                .stream()
                .map(this::mapToQuizSummaryDto)
                .toList();
    }

    private QuizSummaryDto mapToQuizSummaryDto(Quiz quiz) {
        List<QuestionSummaryDto> questions = quiz.getQuestions()
                .stream()
                .map(questionMapper::toQuestionSummaryDto)
                .toList();

        return new QuizSummaryDto(
                quiz.getQuizId(),
                quiz.getTitle(),
                quiz.getCreatedBy().getEmail(),
                quiz.getStatus(),
                quiz.getCreatedAt(),
                questions
        );
    }

    public QuizSummaryDto getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(QuizNotFoundException::new);

        return mapToQuizSummaryDto(quiz);
    }

    public void updateQuiz(Long id, QuizUpdateDto dto, User user) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(QuizNotFoundException::new);

        if (!quiz.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("Only the teacher that created the quiz can update it");
        }

        if (dto.getStatus() != null) {
            quiz.setStatus(QuizStatus.valueOf(dto.getStatus().toUpperCase()));
        }

        if (dto.isEmpty()) {
            throw new BadRequestException("Nothing to update");
        }

        if (quizRepository.existsByTitleIgnoreCase(dto.getTitle())) {
            throw new BadRequestException("A quiz with this title already exists");
        }

        if (dto.getTitle() != null) {
            quiz.setTitle(dto.getTitle());
        }

        quizRepository.save(quiz);
    }

    public void deleteQuiz(Long id, User user) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(QuizNotFoundException::new);

        if (!quiz.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("Only the teacher that created the quiz can delete it");
        }

        quizRepository.delete(quiz);
    }
}
