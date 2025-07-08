package ro.ase.acs.mind_path.dto.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ro.ase.acs.mind_path.dto.response.AnswerDto;
import ro.ase.acs.mind_path.dto.response.AnswerResultDto;
import ro.ase.acs.mind_path.dto.response.QuestionDto;
import ro.ase.acs.mind_path.dto.response.QuestionResultDto;
import ro.ase.acs.mind_path.entity.Answer;
import ro.ase.acs.mind_path.entity.Question;
import ro.ase.acs.mind_path.entity.UserResponse;
import ro.ase.acs.mind_path.entity.enums.QuestionType;
import ro.ase.acs.mind_path.repository.AnswerRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QuestionMapper implements DtoMapper<Question, QuestionDto> {

    private final AnswerMapper answerMapper;

    private final AnswerRepository answerRepository;

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

    public QuestionResultDto toResultDto(Question q,
                                         Map<Long, List<UserResponse>> responseMap) {
        List<Answer> answers = answerRepository.findByQuestionQuestionId(q.getQuestionId());
        List<UserResponse> userResponses = responseMap.getOrDefault(q.getQuestionId(), List.of());

        if (q.getType() == QuestionType.OPEN_ENDED) {
            String studentResponse = userResponses.isEmpty() ? "" :
                    (userResponses.getFirst().getOpenEndedAnswer() != null ?
                            userResponses.getFirst().getOpenEndedAnswer() : "");

            List<AnswerResultDto> answerResults = List.of(
                    AnswerResultDto.builder()
                            .id(0L)
                            .text(studentResponse)
                            .isSelected(true)
                            .isCorrect(false)
                            .build()
            );

            return QuestionResultDto.builder()
                    .id(q.getQuestionId())
                    .text(q.getQuestionText())
                    .type(q.getType().toString())
                    .isCorrect(false)
                    .aiScore(userResponses.getFirst().getAiScore())
                    .aiFeedback(userResponses.getFirst().getAiFeedback())
                    .teacherScore(userResponses.getFirst().getTeacherScore())
                    .answers(answerResults)
                    .build();
        } else {
            List<AnswerResultDto> answerResults = answers.stream()
                    .map(answer -> {
                        boolean isSelected = userResponses.stream()
                                .anyMatch(r -> r.getSelectedAnswer().getAnswerId().equals(answer.getAnswerId()));

                        return AnswerResultDto.builder()
                                .id(answer.getAnswerId())
                                .text(answer.getAnswerText())
                                .isSelected(isSelected)
                                .isCorrect(answer.getIsCorrect())
                                .build();
                    })
                    .collect(Collectors.toList());

            boolean isCorrect;
            if (q.getType() == QuestionType.MULTIPLE_CHOICE) {
                List<Long> selectedAnswerIds = userResponses.stream()
                        .map(r -> r.getSelectedAnswer().getAnswerId())
                        .toList();

                List<Long> correctAnswerIds = answers.stream()
                        .filter(Answer::getIsCorrect)
                        .map(Answer::getAnswerId)
                        .toList();

                isCorrect = new HashSet<>(selectedAnswerIds).containsAll(correctAnswerIds) &&
                        new HashSet<>(correctAnswerIds).containsAll(selectedAnswerIds);
            } else {
                isCorrect = !userResponses.isEmpty() && userResponses.getFirst().getIsCorrect();
            }

            return QuestionResultDto.builder()
                    .id(q.getQuestionId())
                    .text(q.getQuestionText())
                    .type(q.getType().toString())
                    .isCorrect(isCorrect)
                    .answers(answerResults)
                    .build();
        }
    }
}
