package ro.ase.acs.mind_path.dto.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ro.ase.acs.mind_path.dto.response.AnswerDto;
import ro.ase.acs.mind_path.dto.response.QuestionDto;
import ro.ase.acs.mind_path.entity.Question;
import ro.ase.acs.mind_path.entity.enums.QuestionType;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class QuestionMapper implements DtoMapper<Question, QuestionDto> {

    private final AnswerMapper answerMapper;

    @Override
    public QuestionDto toDto(Question q) {
        List<AnswerDto> answers =
                q.getType() == QuestionType.OPEN_ENDED
                ? Collections.emptyList()
                : q.getAnswers().stream()
                    .map(answerMapper::toDto)
                    .toList();

        return QuestionDto.builder()
                .id(q.getQuestionId())
                .text(q.getQuestionText())
                .type(q.getType())
                .difficulty(q.getDifficulty())
                .answers(answers)
                .build();
    }
}
