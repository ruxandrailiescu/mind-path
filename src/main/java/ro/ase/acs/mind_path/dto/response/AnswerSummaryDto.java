package ro.ase.acs.mind_path.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswerSummaryDto {
    private Long id;
    private String text;
    private boolean isCorrect;
}
