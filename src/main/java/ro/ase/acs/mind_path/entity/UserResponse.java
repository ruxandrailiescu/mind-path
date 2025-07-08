package ro.ase.acs.mind_path.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "User_responses")
public class UserResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "response_id")
    private Long responseId;
    @ManyToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt quizAttempt;
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    @ManyToOne
    @JoinColumn(name = "selected_answer")
    private Answer selectedAnswer;
    @Column(name = "open_ended_answer", columnDefinition = "TEXT")
    private String openEndedAnswer;
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;
    @Column(name = "response_time")
    private Integer responseTime;
    @Column(name = "teacher_score")
    private Float teacherScore;
    @Column(name = "ai_score")
    private Float aiScore;
    @Column(name = "aiFeedback", columnDefinition = "TEXT")
    private String aiFeedback;

    public Float getFinalScore() {
        return teacherScore != null ? teacherScore : aiScore;
    }
}
