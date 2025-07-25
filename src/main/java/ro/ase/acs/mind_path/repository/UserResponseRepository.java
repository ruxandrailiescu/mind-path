package ro.ase.acs.mind_path.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ro.ase.acs.mind_path.entity.UserResponse;
import ro.ase.acs.mind_path.entity.enums.QuestionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserResponseRepository extends JpaRepository<UserResponse, Long> {
    List<UserResponse> findByQuizAttemptAttemptId(Long attemptId);
    void deleteByQuizAttemptAttemptIdAndQuestionQuestionId(Long attemptId, Long questionId);
    Optional<UserResponse> findByQuizAttemptAttemptIdAndQuestionQuestionId(Long attemptId, Long questionId);
    List<UserResponse> findByQuizAttemptAttemptIdAndQuestionType(Long attemptId, QuestionType type);
    List<UserResponse> findByQuizAttemptUserUserIdAndQuizAttemptCompletedAtBetween(
            Long userId,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );
}
