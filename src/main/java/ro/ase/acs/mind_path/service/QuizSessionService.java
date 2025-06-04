package ro.ase.acs.mind_path.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizSessionService {
    private static final Logger logger = LoggerFactory.getLogger(QuizSessionService.class);

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizSessionRepository quizSessionRepository;

    public QuizSession createSession(Long teacherId, CreateSessionRequest request) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new QuizAttemptException("Teacher not found"));

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new QuizAttemptException("Quiz not found"));

        if (!quiz.getCreatedBy().getUserId().equals(teacherId)) {
            throw new QuizAttemptException("You can only create sessions for your own quizzes");
        }

        if (quiz.getStatus() != QuizStatus.ACTIVE) {
            throw new QuizAttemptException("Cannot create a session for an inactive quiz");
        }

        quizSessionRepository.findByQuizQuizIdAndStatus(quiz.getQuizId(), SessionStatus.ACTIVE)
                .ifPresent(session -> {
                    if (session.getEndTime().isAfter(LocalDateTime.now())) {
                        throw new QuizAttemptException("An active session already exists for this quiz");
                    } else {
                        session.setStatus(SessionStatus.EXPIRED);
                        quizSessionRepository.save(session);
                    }
                });

        LocalDateTime now = LocalDateTime.now();
        int duration = request.getDurationMinutes() != null ? request.getDurationMinutes() : 30;

        QuizSession session = QuizSession.builder()
                .quiz(quiz)
                .createdBy(teacher)
                .status(SessionStatus.ACTIVE)
                .startTime(now)
                .endTime(now.plusMinutes(duration))
                .build();

        return quizSessionRepository.save(session);
    }

    public QuizSession validateAccessCode(String accessCode) {
        QuizSession session = quizSessionRepository.findByAccessCode(accessCode);

        if (session != null && session.getStatus() == SessionStatus.ACTIVE &&
                session.getEndTime().isBefore(LocalDateTime.now())) {
            session.setStatus(SessionStatus.EXPIRED);
            return quizSessionRepository.save(session);
        }

        return session;
    }

    @Scheduled(fixedRate = 60000)
    public void updateExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<QuizSession> activeSessions = quizSessionRepository.findByStatus(SessionStatus.ACTIVE);

        int updatedCount = 0;

        for (QuizSession session : activeSessions) {
            if (session.getEndTime().isBefore(now)) {
                session.setStatus(SessionStatus.EXPIRED);
                quizSessionRepository.save(session);
                updatedCount++;
            }
        }

        if (updatedCount > 0) {
            logger.info("Updated {} expired quiz sessions", updatedCount);
        }
    }
}