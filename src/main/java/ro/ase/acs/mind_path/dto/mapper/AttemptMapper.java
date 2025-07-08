package ro.ase.acs.mind_path.dto.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ro.ase.acs.mind_path.dto.response.*;
import ro.ase.acs.mind_path.entity.Question;
import ro.ase.acs.mind_path.entity.QuizAttempt;
import ro.ase.acs.mind_path.entity.UserResponse;
import ro.ase.acs.mind_path.repository.QuestionRepository;
import ro.ase.acs.mind_path.repository.UserResponseRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AttemptMapper {

    private final QuestionMapper questionMapper;
    private final ResponseMapper responseMapper;

    private final QuestionRepository questionRepository;
    private final UserResponseRepository userResponseRepository;

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

    public AttemptResultDto toResultDto(QuizAttempt attempt) {
        List<Question> questions = questionRepository.findByQuizQuizId(attempt.getQuiz().getQuizId());
        List<UserResponse> responses = userResponseRepository.findByQuizAttemptAttemptId(attempt.getAttemptId());

        Map<Long, List<UserResponse>> responseMap = responses.stream()
                .collect(Collectors.groupingBy(r -> r.getQuestion().getQuestionId()));

        List<QuestionResultDto> questionResults = questions.stream()
                .map(q -> questionMapper.toResultDto(q, responseMap))
                .collect(Collectors.toList());

        long correctAnswersCount = questionResults.stream()
                .filter(QuestionResultDto::getIsCorrect)
                .count();

        return AttemptResultDto.builder()
                .attemptId(attempt.getAttemptId())
                .quizId(attempt.getQuiz().getQuizId())
                .quizTitle(attempt.getQuiz().getTitle())
                .score(attempt.getScore())
                .attemptTime(attempt.getAttemptTime())
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .totalQuestions(questions.size())
                .correctAnswers((int) correctAnswersCount)
                .questions(questionResults)
                .build();
    }
}
