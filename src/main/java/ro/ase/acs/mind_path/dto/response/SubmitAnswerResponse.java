package ro.ase.acs.mind_path.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubmitAnswerResponse {
    private final boolean isCorrect;
}
