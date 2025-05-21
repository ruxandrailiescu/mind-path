package ro.ase.acs.mind_path.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.stream.Collectors;
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

    public AttemptResponseDto startAttempt(Long userId, StartAttemptRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new QuizAttemptException("User not found"));

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new QuizAttemptException("Quiz not found"));

        // Check if quiz is active
        if (quiz.getStatus() != QuizStatus.ACTIVE) {
            throw new QuizAttemptException("Quiz is not active");
        }

        QuizSession session = null;
        // Check for in-class restriction if access code is provided
        if (request.getAccessCode() != null && !request.getAccessCode().isEmpty()) {
            // Use quizSessionService to validate the access code and check expiration
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

            // Check if session is still valid (within time window)
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(session.getStartTime())) {
                throw new QuizAttemptException("Quiz session has not started yet");
            }

            if (now.isAfter(session.getEndTime())) {
                // Update the session status if it's expired
                session.setStatus(SessionStatus.EXPIRED);
                quizSessionRepository.save(session);
                throw new QuizAttemptException("Quiz session has expired");
            }
        }

        // Check for existing attempts for this quiz by this user
        List<QuizAttempt> userAttempts = quizAttemptRepository.findByUserUserIdAndQuizQuizId(userId, quiz.getQuizId());

        // First, check for any IN_PROGRESS attempts
        Optional<QuizAttempt> inProgressAttempt = userAttempts.stream()
                .filter(a -> a.getStatus() == AttemptStatus.IN_PROGRESS)
                .findFirst();

        if (inProgressAttempt.isPresent()) {
            QuizAttempt attempt = inProgressAttempt.get();

            // If this attempt has a quiz session, check if the session is still valid
            if (attempt.getQuizSession() != null) {
                LocalDateTime now = LocalDateTime.now();

                // If the session has expired, mark the attempt as ABANDONED
                if (attempt.getQuizSession().getStatus() == SessionStatus.EXPIRED ||
                        attempt.getQuizSession().getEndTime().isBefore(now)) {
                    attempt.setStatus(AttemptStatus.ABANDONED);
                    quizAttemptRepository.save(attempt);

                    // Continue to create a new attempt since this one is abandoned
                } else {
                    // Session is still valid, return the existing attempt
                    return buildAttemptResponse(attempt);
                }
            } else {
                // No session associated, return the existing attempt
                return buildAttemptResponse(attempt);
            }
        }

        // Create new attempt
        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .score(0.0f)
                .status(AttemptStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();

        // If using session-based restriction
        if (session != null) {
            attempt.setQuizSession(session);
        }

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        // Build the response DTO
        return buildAttemptResponse(savedAttempt);
    }

    public AttemptResponseDto getAttempt(Long attemptId, Long userId) {
        QuizAttempt attempt = quizAttemptRepository.findByAttemptIdAndUserUserId(attemptId, userId)
                .orElseThrow(() -> new QuizAttemptException("Attempt not found or not accessible"));

        // Check if the attempt is still valid (if it has a session, make sure the session is still active)
        checkAndUpdateAttemptStatus(attempt);

        return buildAttemptResponse(attempt);
    }

    /**
     * Checks if an attempt is still valid based on its associated session status.
     * Updates the attempt status if needed.
     *
     * @param attempt The quiz attempt to check
     * @return true if the attempt is still valid, false otherwise
     */
    private boolean checkAndUpdateAttemptStatus(QuizAttempt attempt) {
        // Only check IN_PROGRESS attempts
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            return false;
        }

        // If there's an associated session, check if it's still active
        if (attempt.getQuizSession() != null) {
            QuizSession session = attempt.getQuizSession();

            // If the session has expired, update the attempt status
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
    public void submitAnswer(Long attemptId, Long userId, SubmitAnswerRequest request) {
        QuizAttempt attempt = quizAttemptRepository.findByAttemptIdAndUserUserId(attemptId, userId)
                .orElseThrow(() -> new QuizAttemptException("Attempt not found or not accessible"));

        // Check if the attempt is still valid
        if (!checkAndUpdateAttemptStatus(attempt)) {
            throw new QuizAttemptException("Quiz session has expired. This attempt is no longer valid.");
        }

        // Check if attempt is still in progress
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new QuizAttemptException("Attempt is no longer in progress");
        }

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new QuizAttemptException("Question not found"));

        // Verify question belongs to the quiz
        if (!question.getQuiz().getQuizId().equals(attempt.getQuiz().getQuizId())) {
            throw new QuizAttemptException("Question does not belong to this quiz");
        }

        // Check if this is a multiple choice question
        boolean isMultipleChoice = request.getIsMultipleChoice() != null &&
                request.getIsMultipleChoice() &&
                question.getType() == QuestionType.MULTIPLE_CHOICE;

        userResponseRepository.deleteByQuizAttemptAttemptIdAndQuestionQuestionId(attemptId, question.getQuestionId());

        List<Long> selectedAnswerIds = request.getSelectedAnswerIds();
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
    }

    public AttemptResultDto getAttemptResults(Long attemptId, Long userId) {
        // Fetch the attempt
        QuizAttempt attempt = quizAttemptRepository.findByAttemptIdAndUserUserId(attemptId, userId)
                .orElseThrow(() -> new QuizAttemptException("Attempt not found or not accessible"));

        // Check if attempt is submitted or graded
        if (attempt.getStatus() != AttemptStatus.SUBMITTED && attempt.getStatus() != AttemptStatus.GRADED) {
            throw new QuizAttemptException("Attempt results are not available. The quiz must be submitted first.");
        }

        // Update the status to GRADED if it's not already
        if (attempt.getStatus() != AttemptStatus.GRADED) {
            attempt.setStatus(AttemptStatus.GRADED);
            quizAttemptRepository.save(attempt);
        }

        // Get all questions for this quiz
        List<Question> questions = questionRepository.findByQuizQuizId(attempt.getQuiz().getQuizId());

        // Get all responses for this attempt
        List<UserResponse> responses = userResponseRepository.findByQuizAttemptAttemptId(attemptId);

        Map<Long, List<UserResponse>> responseMap = responses.stream()
                .collect(Collectors.groupingBy(r -> r.getQuestion().getQuestionId()));

        // Map questions to result DTOs
        List<QuestionResultDto> questionResults = questions.stream()
                .map(question -> {
                    // Get all possible answers for this question
                    List<Answer> answers = answerRepository.findByQuestionQuestionId(question.getQuestionId());

                    // Get the user's responses for this question
                    List<UserResponse> userResponses = responseMap.getOrDefault(question.getQuestionId(), List.of());

                    // Map answers to result DTOs
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

                    // For multiple choice questions
                    boolean isCorrect = false;
                    if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
                        // All selected answers must be correct, and all correct answers must be selected
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
                        // For single choice, if the user selected an answer and it's correct
                        isCorrect = !userResponses.isEmpty() && userResponses.getFirst().getIsCorrect();
                    }

                    return QuestionResultDto.builder()
                            .id(question.getQuestionId())
                            .text(question.getQuestionText())
                            .type(question.getType().toString())
                            .isCorrect(isCorrect)
                            .answers(answerResults)
                            .build();
                })
                .collect(Collectors.toList());

        // Count correct answers
        long correctAnswersCount = questionResults.stream()
                .filter(QuestionResultDto::getIsCorrect)
                .count();

        // Build and return the result DTO
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

    public AttemptResponseDto submitAttempt(Long attemptId, Long userId, SubmitAttemptRequest request) {
        QuizAttempt attempt = quizAttemptRepository.findByAttemptIdAndUserUserId(attemptId, userId)
                .orElseThrow(() -> new QuizAttemptException("Attempt not found or not accessible"));

        // Check if the attempt is still valid
        if (!checkAndUpdateAttemptStatus(attempt)) {
            throw new QuizAttemptException("Quiz session has expired. This attempt is no longer valid.");
        }

        // Check if attempt is still in progress
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new QuizAttemptException("Attempt is no longer in progress");
        }

        // Get all responses for this attempt
        List<UserResponse> responses = userResponseRepository.findByQuizAttemptAttemptId(attemptId);

        // Get all questions for this quiz
        List<Question> questions = questionRepository.findByQuizQuizId(attempt.getQuiz().getQuizId());

        // Calculate score
        float totalCorrect = 0;
        for (Question question : questions) {
            List<UserResponse> questionResponses = responses.stream()
                    .filter(r -> r.getQuestion().getQuestionId().equals(question.getQuestionId()))
                    .toList();

            if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
                // Get all correct answers for this question
                List<Long> correctAnswerIds = answerRepository.findByQuestionQuestionIdAndIsCorrect(
                        question.getQuestionId(), true).stream()
                        .map(Answer::getAnswerId)
                        .toList();

                // Get user selected answers
                List<Long> selectedAnswerIds = questionResponses.stream()
                        .map(r -> r.getSelectedAnswer().getAnswerId())
                        .toList();

                // All selected answers must be correct, and all correct answers must be selected
                if (new HashSet<>(selectedAnswerIds).containsAll(correctAnswerIds) &&
                    new HashSet<>(correctAnswerIds).containsAll(selectedAnswerIds)) {
                    totalCorrect++;
                }
            } else {
                // For single choice questions
                if (!questionResponses.isEmpty() && questionResponses.getFirst().getIsCorrect()) {
                    totalCorrect++;
                }
            }
        }

        float score = (totalCorrect / questions.size()) * 100;

        // Update attempt
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setAttemptTime(request.getTotalTime());
        attempt.setScore(score);

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        return buildAttemptResponse(savedAttempt);
    }

    /**
     * Scheduled task to check for quiz attempts associated with expired sessions.
     * Updates the status of any IN_PROGRESS attempt to ABANDONED if its session has expired.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void updateAbandonedAttempts() {
        LocalDateTime now = LocalDateTime.now();

        // Get all IN_PROGRESS attempts
        List<QuizAttempt> inProgressAttempts = quizAttemptRepository.findByStatus(AttemptStatus.IN_PROGRESS);

        int updatedCount = 0;

        for (QuizAttempt attempt : inProgressAttempts) {
            // Only check attempts with a session
            if (attempt.getQuizSession() != null) {
                QuizSession session = attempt.getQuizSession();

                // Check if the session has expired
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
        // Get questions for this quiz
        List<Question> questions = questionRepository.findByQuizQuizId(attempt.getQuiz().getQuizId());

        // Map questions to DTOs
        List<QuestionDto> questionDtos = questions.stream()
                .map(q -> {
                    // Get answers for this question
                    List<Answer> answers = answerRepository.findByQuestionQuestionId(q.getQuestionId());

                    // Map answers to DTOs (without showing which is correct)
                    List<AnswerDto> answerDtos = answers.stream()
                            .map(a -> AnswerDto.builder()
                                    .id(a.getAnswerId())
                                    .text(a.getAnswerText())
                                    .build())
                            .collect(Collectors.toList());

                    return QuestionDto.builder()
                            .id(q.getQuestionId())
                            .text(q.getQuestionText())
                            .type(q.getType())
                            .difficulty(q.getDifficulty())
                            .answers(answerDtos)
                            .build();
                })
                .collect(Collectors.toList());

        // Get all responses for this attempt
        List<UserResponse> responses = userResponseRepository.findByQuizAttemptAttemptId(attempt.getAttemptId());

        List<ResponseDto> responseDtos = responses.stream()
                .map(r -> ResponseDto.builder()
                        .questionId(r.getQuestion().getQuestionId())
                        .answerId(r.getSelectedAnswer().getAnswerId())
                        .isMultipleChoice(r.getQuestion().getType() == QuestionType.MULTIPLE_CHOICE)
                        .build())
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

    /**
     * Get all in-progress attempts for a student
     * This method returns attempts that are IN_PROGRESS and not associated with expired sessions
     *
     * @param userId the ID of the student
     * @return a list of attempts that can be resumed
     */
    public List<AttemptResponseDto> getInProgressAttempts(Long userId) {
        // Get all in-progress attempts for this user
        List<QuizAttempt> inProgressAttempts = quizAttemptRepository.findByUserUserIdAndStatus(userId, AttemptStatus.IN_PROGRESS);
        List<AttemptResponseDto> result = new ArrayList<>();

        // Filter out attempts with expired sessions
        for (QuizAttempt attempt : inProgressAttempts) {
            // Check if the attempt is still valid
            if (checkAndUpdateAttemptStatus(attempt)) {
                result.add(buildAttemptResponse(attempt));
            }
        }

        return result;
    }

    /**
     * Save the current progress of an attempt without submitting it
     * This method simply updates the attempt's lastAccessedAt time
     *
     * @param attemptId the ID of the attempt to save
     * @param userId the ID of the student
     * @return the updated attempt
     */
    public AttemptResponseDto saveProgress(Long attemptId, Long userId) {
        QuizAttempt attempt = quizAttemptRepository.findByAttemptIdAndUserUserId(attemptId, userId)
                .orElseThrow(() -> new QuizAttemptException("Attempt not found or not accessible"));

        // Check if the attempt is still valid
        if (!checkAndUpdateAttemptStatus(attempt)) {
            throw new QuizAttemptException("Quiz session has expired. This attempt is no longer valid.");
        }

        // Check if attempt is still in progress
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new QuizAttemptException("Attempt is no longer in progress");
        }

        // Update the last accessed time
        attempt.setLastAccessedAt(LocalDateTime.now());
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        return buildAttemptResponse(savedAttempt);
    }
}