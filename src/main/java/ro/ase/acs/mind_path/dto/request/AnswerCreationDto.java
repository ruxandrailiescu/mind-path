package ro.ase.acs.mind_path.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnswerCreationDto {
    @NotBlank(message = "Answer text cannot be blank")
    private String answerText;
    @NotNull(message = "Must state if the answer is correct or not")
    private Boolean isCorrect;
}
