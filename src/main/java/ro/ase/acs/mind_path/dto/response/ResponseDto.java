package ro.ase.acs.mind_path.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto {
    private Long questionId;
    private Long answerId;
    private Boolean isMultipleChoice;
    private String textResponse;
    private Boolean isOpenEnded;
}
