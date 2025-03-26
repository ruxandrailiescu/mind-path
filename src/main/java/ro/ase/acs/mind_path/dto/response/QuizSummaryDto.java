package ro.ase.acs.mind_path.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.acs.mind_path.entity.enums.QuizStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizSummaryDto {
    private Long id;
    private String title;
    private String createdBy;
    private QuizStatus status;
    private LocalDateTime createdAt;
    private List<QuestionSummaryDto> questions;
}
