package ro.ase.acs.mind_path.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.acs.mind_path.entity.enums.QuestionDifficulty;
import ro.ase.acs.mind_path.entity.enums.QuestionType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionDto {
    private Long id;
    private String text;
    private QuestionType type;
    private QuestionDifficulty difficulty;
    private List<AnswerDto> answers;
}
