package ro.ase.acs.mind_path.dto.mapper;

import org.springframework.stereotype.Component;
import ro.ase.acs.mind_path.dto.response.AnswerSummaryDto;
import ro.ase.acs.mind_path.dto.response.QuestionSummaryDto;
import ro.ase.acs.mind_path.entity.Answer;
import ro.ase.acs.mind_path.entity.Question;

import java.util.List;

@Component
public class QuestionMapper {

    public AnswerSummaryDto toAnswerSummaryDto(Answer answer) {
        return new AnswerSummaryDto(
                answer.getAnswerId(),
                answer.getAnswerText(),
                answer.getIsCorrect()
        );
    }

    public QuestionSummaryDto toQuestionSummaryDto(Question question) {
        List<AnswerSummaryDto> answers = question.getAnswers()
                .stream()
                .map(this::toAnswerSummaryDto)
                .toList();

        return new QuestionSummaryDto(
                question.getQuestionId(),
                question.getQuestionText(),
                question.getType(),
                question.getDifficulty(),
                answers
        );
    }
}
