package ro.ase.acs.mind_path.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnswerResultDto {
    private Long id;
    private String text;
    private Boolean isSelected;
    private Boolean isCorrect;
}
