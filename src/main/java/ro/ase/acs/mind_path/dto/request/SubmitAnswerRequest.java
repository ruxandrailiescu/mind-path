package ro.ase.acs.mind_path.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitAnswerRequest {
    private Long questionId;
    private Long answerId;
    private Integer responseTime;
}
