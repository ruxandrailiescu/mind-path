package ro.ase.acs.mind_path.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.acs.mind_path.entity.enums.QuestionDifficulty;
import ro.ase.acs.mind_path.entity.enums.QuestionType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionSummaryDto {
    private Long id;
    private String text;
    private QuestionType type;
    private QuestionDifficulty difficulty;
}
