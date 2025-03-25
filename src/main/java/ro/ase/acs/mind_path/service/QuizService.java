package ro.ase.acs.mind_path.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.ase.acs.mind_path.dto.request.QuizCreationDto;
import ro.ase.acs.mind_path.dto.request.QuizUpdateDto;
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

    private final QuizRepository quizRepository;

    public Long createQuiz(QuizCreationDto dto, User user) {
        if (quizRepository.existsByTitleIgnoreCase(dto.getTitle())) {
            throw new BadRequestException("A quiz with this title already exists");
        }

        QuizStatus quizStatus = QuizStatus.DRAFT;

        if (dto.getStatus() != null) {
            try {
                quizStatus = QuizStatus.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid quiz status: " + dto.getStatus());
            }
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
                .map(q -> new QuizSummaryDto(
                        q.getQuizId(),
                        q.getTitle(),
                        q.getCreatedBy().getEmail(),
                        q.getStatus(),
                        q.getCreatedAt()
                ))
                .toList();
    }

    public List<QuizSummaryDto> getActiveQuizzes() {
        return quizRepository.findAllByStatus(QuizStatus.ACTIVE)
                .stream()
                .map(q -> new QuizSummaryDto(
                        q.getQuizId(),
                        q.getTitle(),
                        q.getCreatedBy().getEmail(),
                        q.getStatus(),
                        q.getCreatedAt()
                ))
                .toList();
    }

    public QuizSummaryDto getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(QuizNotFoundException::new);

        return new QuizSummaryDto(
                quiz.getQuizId(),
                quiz.getTitle(),
                quiz.getCreatedBy().getEmail(),
                quiz.getStatus(),
                quiz.getCreatedAt()
        );
    }

    public void updateQuiz(Long id, QuizUpdateDto dto, User user) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(QuizNotFoundException::new);

        if (!quiz.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("Only the teacher that created the quiz can update it");
        }

        if (quiz.getStatus() == QuizStatus.ARCHIVED) {
            throw new BadRequestException("Archived quizzes cannot be updated");
        }

        if (dto.getTitle() != null) {
            quiz.setTitle(dto.getTitle());
        }

        if (dto.getStatus() != null) {
            quiz.setStatus(QuizStatus.valueOf(dto.getStatus().toUpperCase()));
        }

        quizRepository.save(quiz);
    }
}
