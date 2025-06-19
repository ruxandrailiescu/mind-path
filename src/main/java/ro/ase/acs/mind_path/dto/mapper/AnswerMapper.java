package ro.ase.acs.mind_path.dto.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ro.ase.acs.mind_path.dto.response.AnswerDto;
import ro.ase.acs.mind_path.entity.Answer;

@Component
@RequiredArgsConstructor
public class AnswerMapper implements DtoMapper<Answer, AnswerDto> {

    @Override
    public AnswerDto toDto(Answer a) {
        return AnswerDto.builder()
                .id(a.getAnswerId())
                .text(a.getAnswerText())
                .build();
    }
}
