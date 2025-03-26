package ro.ase.acs.mind_path.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionCreationDto {
    @NotBlank(message = "Question text cannot be blank")
    private String questionText;
    @NotBlank(message = "Question type cannot be blank")
    private String type;
    @NotBlank(message = "Question difficulty cannot be blank")
    private String difficulty;
}
