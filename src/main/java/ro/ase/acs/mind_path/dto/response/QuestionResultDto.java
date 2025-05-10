package ro.ase.acs.mind_path.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResultDto {
    private Long id;
    private String text;
    private String type;
    private Boolean isCorrect;
    private List<AnswerResultDto> answers;
}
