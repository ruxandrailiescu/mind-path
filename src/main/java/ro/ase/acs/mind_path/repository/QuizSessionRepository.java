package ro.ase.acs.mind_path.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.ase.acs.mind_path.entity.QuizSession;
import ro.ase.acs.mind_path.entity.enums.SessionStatus;

import java.util.Optional;

@Repository
public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    Optional<QuizSession> findByQuizQuizIdAndStatus(Long quizId, SessionStatus status);
    Optional<QuizSession> findByAccessCodeAndStatus(String accessCode, SessionStatus status);

    QuizSession findByAccessCode(String accessCode);
}
