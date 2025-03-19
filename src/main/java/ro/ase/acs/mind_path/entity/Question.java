package ro.ase.acs.mind_path.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ro.ase.acs.mind_path.entity.enums.QuestionDifficulty;
import ro.ase.acs.mind_path.entity.enums.QuestionType;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "Questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;
    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    @Column(nullable = false, name = "question_text", columnDefinition = "TEXT")
    private String questionText;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionDifficulty difficulty;
    @Column(nullable = false, name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers;
}
