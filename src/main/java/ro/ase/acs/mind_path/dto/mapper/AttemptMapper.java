package ro.ase.acs.mind_path.dto.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ro.ase.acs.mind_path.dto.response.AttemptResponseDto;
import ro.ase.acs.mind_path.dto.response.QuestionDto;
import ro.ase.acs.mind_path.dto.response.ResponseDto;
import ro.ase.acs.mind_path.entity.Question;
import ro.ase.acs.mind_path.entity.QuizAttempt;
import ro.ase.acs.mind_path.entity.UserResponse;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AttemptMapper {

    private final QuestionMapper questionMapper;
    private final ResponseMapper responseMapper;

    public AttemptResponseDto toDto(QuizAttempt attempt,
                                    List<Question> questions,
                                    List<UserResponse> responses) {
        List<QuestionDto> questionDtos = questions.stream()
                .map(questionMapper::toDto)
                .toList();
        List<ResponseDto> responseDtos = responses.stream()
                .map(responseMapper::toDto)
                .toList();

        return AttemptResponseDto.builder()
                .attemptId(attempt.getAttemptId())
                .quizId(attempt.getQuiz().getQuizId())
                .quizTitle(attempt.getQuiz().getTitle())
                .status(attempt.getStatus())
                .score(attempt.getScore())
                .attemptTime(attempt.getAttemptTime())
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .questions(questionDtos)
                .responses(responseDtos)
                .build();
    }
}
