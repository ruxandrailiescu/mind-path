package ro.ase.acs.mind_path.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitAnswerRequest {
    private Long questionId;
    private List<Long> selectedAnswerIds;
    private Integer responseTime;
    private Boolean isMultipleChoice;
    private String textResponse;
    private Boolean isOpenEnded;
}
