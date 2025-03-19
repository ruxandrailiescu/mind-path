package ro.ase.acs.mind_path.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "Quiz_attempts")
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long attemptId;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    @Column(nullable = false)
    private Float score;
    @Column(name = "attempt_time")
    private Integer attemptTime;
    @Column(name = "completed_at", updatable = false)
    private LocalDateTime completedAt;
    @OneToMany(mappedBy = "quizAttempt", cascade = CascadeType.ALL)
    private List<UserResponse> userResponses;
}
