package ro.ase.acs.mind_path.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ro.ase.acs.mind_path.entity.enums.QuizStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "Quizzes")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Long quizId;
    @Column(nullable = false)
    private String title;
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizStatus status;
    @Column(nullable = false, name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;
}
