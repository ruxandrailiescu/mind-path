package ro.ase.acs.mind_path.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Answers")
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    @Column(nullable = false, name = "answer_text", columnDefinition = "TEXT")
    private String answerText;
    @Column(nullable = false, name = "is_correct")
    private Boolean isCorrect;
}
