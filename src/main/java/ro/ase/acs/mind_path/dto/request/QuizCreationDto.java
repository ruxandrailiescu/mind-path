package ro.ase.acs.mind_path.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizCreationDto {
    @NotBlank(message = "Quiz title cannot be blank")
    private String title;
    private String status;
}
