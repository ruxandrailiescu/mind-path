package ro.ase.acs.mind_path.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionUpdateDto {
    private String questionText;
    private String type;
    private String difficulty;

    public boolean isEmpty() {
        return questionText == null && type == null && difficulty == null;
    }
}
