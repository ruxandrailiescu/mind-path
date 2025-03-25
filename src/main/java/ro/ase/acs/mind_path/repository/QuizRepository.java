package ro.ase.acs.mind_path.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.ase.acs.mind_path.entity.Quiz;
import ro.ase.acs.mind_path.entity.enums.QuizStatus;

import java.util.Collection;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    boolean existsByTitleIgnoreCase(String title);

    List<Quiz> findAllByStatus(QuizStatus quizStatus);
}
