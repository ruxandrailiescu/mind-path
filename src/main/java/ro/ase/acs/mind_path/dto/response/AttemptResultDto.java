package ro.ase.acs.mind_path.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttemptResultDto {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private Float score;
    private Integer attemptTime;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private List<QuestionResultDto> questions;
}
