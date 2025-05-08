package ro.ase.acs.mind_path.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import ro.ase.acs.mind_path.entity.enums.AttemptStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Column(name = "completed_at")
    @UpdateTimestamp
    private LocalDateTime completedAt;
    @OneToMany(mappedBy = "quizAttempt", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<UserResponse> userResponses;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AttemptStatus status;
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    @ManyToOne
    @JoinColumn(name = "session_id")
    private QuizSession quizSession;
}
