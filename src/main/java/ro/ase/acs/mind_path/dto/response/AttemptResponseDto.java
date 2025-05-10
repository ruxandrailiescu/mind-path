package ro.ase.acs.mind_path.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.acs.mind_path.entity.enums.AttemptStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttemptResponseDto {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private AttemptStatus status;
    private Float score;
    private Integer attemptTime;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private List<QuestionDto> questions;
}
