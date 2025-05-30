package ro.ase.acs.mind_path.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.ase.acs.mind_path.entity.UserResponse;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserResponseRepository extends JpaRepository<UserResponse, Long> {
    List<UserResponse> findByQuizAttemptAttemptId(Long attemptId);
    List<UserResponse> findByQuizAttemptAttemptIdAndQuestionQuestionId(Long attemptId, Long questionId);
    Optional<UserResponse> findFirstByQuizAttemptAttemptIdAndQuestionQuestionId(Long attemptId, Long questionId);
    Optional<UserResponse> findByQuizAttemptAttemptIdAndQuestionQuestionIdAndSelectedAnswerAnswerId(
            Long attemptId, Long questionId, Long selectedAnswerId
    );
    boolean existsByQuizAttemptAttemptIdAndQuestionQuestionIdAndSelectedAnswerAnswerId(Long attemptId, Long questionId, Long answerId);
    void deleteByQuizAttemptAttemptIdAndQuestionQuestionId(Long attemptId, Long questionId);
}
