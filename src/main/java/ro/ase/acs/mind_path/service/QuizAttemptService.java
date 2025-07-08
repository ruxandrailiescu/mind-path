package ro.ase.acs.mind_path.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ase.acs.mind_path.dto.mapper.AttemptMapper;
import ro.ase.acs.mind_path.dto.request.StartAttemptRequest;
import ro.ase.acs.mind_path.dto.request.SubmitAnswerRequest;
import ro.ase.acs.mind_path.dto.request.SubmitAttemptRequest;
import ro.ase.acs.mind_path.dto.response.*;
import ro.ase.acs.mind_path.entity.*;
import ro.ase.acs.mind_path.entity.enums.AttemptStatus;
import ro.ase.acs.mind_path.entity.enums.QuestionType;
import ro.ase.acs.mind_path.entity.enums.QuizStatus;
import ro.ase.acs.mind_path.entity.enums.SessionStatus;
import ro.ase.acs.mind_path.exception.QuizAttemptException;
import ro.ase.acs.mind_path.repository.*;

import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class QuizAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(QuizAttemptService.class);

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserResponseRepository userResponseRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final QuizSessionService quizSessionService;
    private final AttemptMapper attemptMapper;
    private final GradingService gradingService;
    private final AiGradingJob aiGradingJob;

    public AttemptResponseDto startAttempt(Long userId, StartAttemptRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new QuizAttemptException("User not found"));

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new QuizAttemptException("Quiz not found"));

        if (quiz.getStatus() != QuizStatus.ACTIVE) {
            throw new QuizAttemptException("Quiz is not active");
        }

        QuizSession session = null;
        if (request.getAccessCode() != null && !request.getAccessCode().isEmpty()) {
            session = quizSessionService.validateAccessCode(request.getAccessCode());

            if (session == null) {
                throw new QuizAttemptException("Invalid access code");
            }

            if (session.getStatus() != SessionStatus.ACTIVE) {
                throw new QuizAttemptException("Session has expired");
            }

            if (!session.getQuiz().getQuizId().equals(quiz.getQuizId())) {
                throw new QuizAttemptException("Access code does not match this quiz");
            }

            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(session.getStartTime())) {
                throw new QuizAttemptException("Quiz session has not started yet");
            }

            if (now.isAfter(session.getEndTime())) {
                session.setStatus(SessionStatus.EXPIRED);
                quizSessionRepository.save(session);
                throw new QuizAttemptException("Quiz session has expired");
            }
        }

        List<QuizAttempt> userAttempts = quizAttemptRepository.findByUserUserIdAndQuizQuizId(userId, quiz.getQuizId());

        Optional<QuizAttempt> inProgressAttempt = userAttempts.stream()
                .filter(a -> a.getStatus() == AttemptStatus.IN_PROGRESS)
                .findFirst();

        if (inProgressAttempt.isPresent()) {
            QuizAttempt attempt = inProgressAttempt.get();

            if (attempt.getQuizSession() != null) {
                LocalDateTime now = LocalDateTime.now();

                if (attempt.getQuizSession().getStatus() == SessionStatus.EXPIRED ||
                        attempt.getQuizSession().getEndTime().isBefore(now)) {
                    attempt.setStatus(AttemptStatus.ABANDONED);
                    quizAttemptRepository.save(attempt);

                } else {
                    return buildAttemptResponse(attempt);
                }
            } else {
                return buildAttemptResponse(attempt);
            }
        }

        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .score(0.0f)
                .status(AttemptStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();

        if (session != null) {
            attempt.setQuizSession(session);
        }

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        return buildAttemptResponse(savedAttempt);
    }

    public AttemptResponseDto getAttempt(Long attemptId, Long userId) {
        QuizAttempt attempt = quizAttemptRepository.findByAttemptIdAndUserUserId(attemptId, userId)
                .orElseThrow(() -> new QuizAttemptException("Attempt not found or not accessible"));

        checkAndUpdateAttemptStatus(attempt);

        return buildAttemptResponse(attempt);
    }

    private boolean checkAndUpdateAttemptStatus(QuizAttempt attempt) {
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            return false;
        }

        if (attempt.getQuizSession() != null) {
            QuizSession session = attempt.getQuizSession();

            LocalDateTime now = LocalDateTime.now();
            if (session.getStatus() != SessionStatus.ACTIVE || session.getEndTime().isBefore(now)) {
                attempt.setStatus(AttemptStatus.ABANDONED);
                quizAttemptRepository.save(attempt);
                return false;
            }
        }

        return true;
    }

    @Transactional
    public SubmitAnswerResponse submitAnswer(Long attemptId, Long userId, SubmitAnswerRequest request) {
        QuizAttempt attempt = quizAttemptRepository.findByAttemptIdAndUserUserId(attemptId, userId)
                .orElseThrow(() -> new QuizAttemptException("Attempt not found or not accessible"));

        if (!checkAndUpdateAttemptStatus(attempt)) {
            throw new QuizAttemptException("Quiz session has expired. This attempt is no longer valid.");
        }

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new QuizAttemptException("Attempt is no longer in progress");
        }

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new QuizAttemptException("Question not found"));

        if (!question.getQuiz().getQuizId().equals(attempt.getQuiz().getQuizId())) {
            throw new QuizAttemptException("Question does not belong to this quiz");
        }

        userResponseRepository.deleteByQuizAttemptAttemptIdAndQuestionQuestionId(attemptId, question.getQuestionId());

        if (question.getType() == QuestionType.OPEN_ENDED) {
            if (request.getTextResponse() == null || request.getTextResponse().trim().isEmpty()) {
                throw new QuizAttemptException("Text response is required for open-ended questions");
            }

            Answer exampleAnswer = answerRepository.findByQuestionQuestionIdAndIsCorrect(
                            question.getQuestionId(), true)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new QuizAttemptException("No example answer found for this question"));

            UserResponse response = UserResponse.builder()
                    .quizAttempt(attempt)
                    .question(question)
                    .selectedAnswer(exampleAnswer)
                    .openEndedAnswer(request.getTextResponse())
                    .responseTime(request.getResponseTime())
                    .isCorrect(false)
                    .build();

            userResponseRepository.save(response);
            return new SubmitAnswerResponse(false);
        }

        boolean isMultipleChoice = request.getIsMultipleChoice() != null &&
                request.getIsMultipleChoice() &&
                question.getType() == QuestionType.MULTIPLE_CHOICE;

        List<Long> selectedAnswerIds = request.getSelectedAnswerIds();
        List<Long> correctAnswerIds = answerRepository
                .findByQuestionQuestionIdAndIsCorrect(request.getQuestionId(), true)
                .stream().map(Answer::getAnswerId).toList();
        boolean isCorrect = new HashSet<>(selectedAnswerIds).equals(
                new HashSet<>(correctAnswerIds));

        if (selectedAnswerIds == null || selectedAnswerIds.isEmpty()) {
            throw new QuizAttemptException("No answers submitted for this question");
        }

        if (!isMultipleChoice && selectedAnswerIds.size() > 1) {
            throw new QuizAttemptException("Single choice question must have only one selected answer");
        }

        for (Long answerId : selectedAnswerIds) {
            Answer answer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new QuizAttemptException("Answer not found"));

            if (!answer.getQuestion().getQuestionId().equals(question.getQuestionId())) {
                throw new QuizAttemptException("Answer does not belong to this question");
            }

            UserResponse response = UserResponse.builder()
                    .quizAttempt(attempt)
                    .question(question)
                    .selectedAnswer(answer)
                    .responseTime(request.getResponseTime())
                    .isCorrect(answer.getIsCorrect())
                    .build();

            userResponseRepository.save(response);
        }

        return new SubmitAnswerResponse(isCorrect);
    }

    public AttemptResultDto getAttemptResults(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new QuizAttemptException("Attempt not found or not accessible"));

        if (attempt.getStatus() != AttemptStatus.SUBMITTED && attempt.getStatus() != AttemptStatus.GRADED) {
            throw new QuizAttemptException("Attempt results are not available. The quiz must be submitted first.");
        }

        attempt.setStatus(AttemptStatus.GRADED);
        quizAttemptRepository.save(attempt);

        return attemptMapper.toResultDto(attempt);
    }

    public AttemptResponseDto submitAttempt(Long attemptId, Long userId, SubmitAttemptRequest request) {
        QuizAttempt attempt = quizAttemptRepository.findByAttemptIdAndUserUserId(attemptId, userId)
                .orElseThrow(() -> new QuizAttemptException("Attempt not found or not accessible"));

        if (!checkAndUpdateAttemptStatus(attempt)) {
            throw new QuizAttemptException("Quiz session has expired. This attempt is no longer valid.");
        }

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new QuizAttemptException("Attempt is no longer in progress");
        }

        List<UserResponse> responses = userResponseRepository.findByQuizAttemptAttemptId(attemptId);
        List<Question> questions = questionRepository.findByQuizQuizId(attempt.getQuiz().getQuizId());

        float score = gradingService.grade(questions, responses);

        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setAttemptTime(request.getTotalTime());
        attempt.setScore(score);

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        try {
            aiGradingJob.gradeAttempt(savedAttempt.getAttemptId());
        } catch (JsonProcessingException e) {
            throw new QuizAttemptException("JSON could not be processed.");
        }

        return buildAttemptResponse(savedAttempt);
    }

    @Scheduled(fixedRate = 300000)
    public void updateAbandonedAttempts() {
        LocalDateTime now = LocalDateTime.now();

        List<QuizAttempt> inProgressAttempts = quizAttemptRepository.findByStatus(AttemptStatus.IN_PROGRESS);

        int updatedCount = 0;

        for (QuizAttempt attempt : inProgressAttempts) {
            if (attempt.getQuizSession() != null) {
                QuizSession session = attempt.getQuizSession();

                if (session.getStatus() == SessionStatus.EXPIRED || session.getEndTime().isBefore(now)) {
                    attempt.setStatus(AttemptStatus.ABANDONED);
                    quizAttemptRepository.save(attempt);
                    updatedCount++;
                }
            }
        }

        if (updatedCount > 0) {
            logger.info("Updated {} quiz attempts to ABANDONED due to expired sessions", updatedCount);
        }
    }

    private AttemptResponseDto buildAttemptResponse(QuizAttempt attempt) {
        List<Question> questions = questionRepository.findByQuizQuizId(attempt.getQuiz().getQuizId());
        List<UserResponse> responses = userResponseRepository.findByQuizAttemptAttemptId(attempt.getAttemptId());

        return attemptMapper.toDto(attempt, questions, responses);
    }

    public List<AttemptResponseDto> getInProgressAttempts(Long userId) {
        List<QuizAttempt> inProgressAttempts = quizAttemptRepository.findByUserUserIdAndStatus(
                userId, AttemptStatus.IN_PROGRESS);
        List<AttemptResponseDto> result = new ArrayList<>();

        for (QuizAttempt attempt : inProgressAttempts) {
            if (checkAndUpdateAttemptStatus(attempt)) {
                result.add(buildAttemptResponse(attempt));
            }
        }

        return result;
    }

    public List<AttemptResultDto> getCompletedAttempts(Long userId) {
        List<QuizAttempt> pastAttempts = quizAttemptRepository.findByUserUserIdAndStatus(
                userId, AttemptStatus.GRADED);
        List<AttemptResultDto> results = new ArrayList<>();

        for (QuizAttempt attempt : pastAttempts) {
            AttemptResultDto resultDto = getAttemptResults(attempt.getAttemptId());
            results.add(resultDto);
        }

        return results;
    }

    public void saveProgress(Long attemptId, Long userId) {
        QuizAttempt attempt = quizAttemptRepository.findByAttemptIdAndUserUserId(attemptId, userId)
                .orElseThrow(() -> new QuizAttemptException("Attempt not found or not accessible"));

        if (!checkAndUpdateAttemptStatus(attempt)) {
            throw new QuizAttemptException("Quiz session has expired. This attempt is no longer valid.");
        }

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new QuizAttemptException("Attempt is no longer in progress");
        }

        attempt.setLastAccessedAt(LocalDateTime.now());
        quizAttemptRepository.save(attempt);
    }
}