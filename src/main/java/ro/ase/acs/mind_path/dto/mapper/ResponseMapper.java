package ro.ase.acs.mind_path.dto.mapper;

import org.springframework.stereotype.Component;
import ro.ase.acs.mind_path.dto.response.ResponseDto;
import ro.ase.acs.mind_path.entity.UserResponse;
import ro.ase.acs.mind_path.entity.enums.QuestionType;

@Component
public class ResponseMapper implements DtoMapper<UserResponse, ResponseDto> {

    @Override
    public ResponseDto toDto(UserResponse r) {
        return ResponseDto.builder()
                .questionId(r.getQuestion().getQuestionId())
                .answerId(r.getSelectedAnswer() == null ? null : r.getSelectedAnswer().getAnswerId())
                .textResponse(r.getOpenEndedAnswer())
                .isMultipleChoice(r.getQuestion().getType() == QuestionType.MULTIPLE_CHOICE)
                .isOpenEnded(r.getQuestion().getType() == QuestionType.OPEN_ENDED)
                .build();
    }
}
