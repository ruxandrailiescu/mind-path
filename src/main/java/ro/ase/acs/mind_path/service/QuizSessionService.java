package ro.ase.acs.mind_path.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ase.acs.mind_path.dto.request.CreateSessionRequest;
import ro.ase.acs.mind_path.entity.Quiz;
import ro.ase.acs.mind_path.entity.QuizSession;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.entity.enums.QuizStatus;
import ro.ase.acs.mind_path.entity.enums.SessionStatus;
import ro.ase.acs.mind_path.exception.QuizAttemptException;
import ro.ase.acs.mind_path.repository.QuizRepository;
import ro.ase.acs.mind_path.repository.QuizSessionRepository;
import ro.ase.acs.mind_path.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuizSessionService {
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizSessionRepository quizSessionRepository;

    public QuizSession createSession(Long teacherId, CreateSessionRequest request) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new QuizAttemptException("Teacher not found"));

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new QuizAttemptException("Quiz not found"));

        // Validate the quiz belongs to this teacher
        if (!quiz.getCreatedBy().getUserId().equals(teacherId)) {
            throw new QuizAttemptException("You can only create sessions for your own quizzes");
        }

        // Check if quiz is active
        if (quiz.getStatus() != QuizStatus.ACTIVE) {
            throw new QuizAttemptException("Cannot create a session for an inactive quiz");
        }

        // Check for existing active sessions
        quizSessionRepository.findByQuizQuizIdAndStatus(quiz.getQuizId(), SessionStatus.ACTIVE)
                .ifPresent(session -> {
                    // Check if the session has expired
                    if (session.getEndTime().isAfter(LocalDateTime.now())) {
                        throw new QuizAttemptException("An active session already exists for this quiz");
                    } else {
                        // Automatically update expired session status
                        session.setStatus(SessionStatus.EXPIRED);
                        quizSessionRepository.save(session);
                    }
                });

        // Create new session
        LocalDateTime now = LocalDateTime.now();
        int duration = request.getDurationMinutes() != null ? request.getDurationMinutes() : 30; // Default 30 minutes

        QuizSession session = QuizSession.builder()
                .quiz(quiz)
                .createdBy(teacher)
                .status(SessionStatus.ACTIVE)
                .startTime(now)
                .endTime(now.plusMinutes(duration))
                .build();

        // The access code will be auto-generated in the prePersist method

        return quizSessionRepository.save(session);
    }

    public QuizSession validateAccessCode(String accessCode) {
        return quizSessionRepository.findByAccessCode(accessCode);
    }
}