package ro.ase.acs.mind_path.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.ase.acs.mind_path.dto.mapper.QuestionMapper;
import ro.ase.acs.mind_path.dto.request.StartAttemptRequest;
import ro.ase.acs.mind_path.dto.request.SubmitAnswerRequest;
import ro.ase.acs.mind_path.dto.request.SubmitAttemptRequest;
import ro.ase.acs.mind_path.dto.response.AnswerDto;
import ro.ase.acs.mind_path.dto.response.AttemptResponseDto;
import ro.ase.acs.mind_path.dto.response.QuestionDto;
import ro.ase.acs.mind_path.entity.*;
import ro.ase.acs.mind_path.entity.enums.AttemptStatus;
import ro.ase.acs.mind_path.entity.enums.QuizStatus;
import ro.ase.acs.mind_path.entity.enums.SessionStatus;
import ro.ase.acs.mind_path.exception.QuizAttemptException;
import ro.ase.acs.mind_path.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizAttemptService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserResponseRepository userResponseRepository;
    private final QuizSessionRepository quizSessionRepository;

    public AttemptResponseDto startAttempt(Long userId, StartAttemptRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new QuizAttemptException("User not found"));

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new QuizAttemptException("Quiz not found"));

        // Check if quiz is active
        if (quiz.getStatus() != QuizStatus.ACTIVE) {
            throw new QuizAttemptException("Quiz is not active");
        }

        // Check for in-class restriction if access code is provided
        if (request.getAccessCode() != null && !request.getAccessCode().isEmpty()) {
            QuizSession session = quizSessionRepository
                    .findByAccessCodeAndStatus(request.getAccessCode(), SessionStatus.ACTIVE)
                    .orElseThrow(() -> new QuizAttemptException("Invalid access code or session is not active"));

            if (!session.getQuiz().getQuizId().equals(quiz.getQuizId())) {
                throw new QuizAttemptException("Access code does not match this quiz");
            }

            // Check if session is still valid (within time window)
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(session.getStartTime()) || now.isAfter(session.getEndTime())) {
                throw new QuizAttemptException("Quiz session is not currently active");
            }
        }

        // Check if user already has an in-progress attempt
        List<QuizAttempt> existingAttempts = quizAttemptRepository
                .findByUserUserIdAndStatus(userId, AttemptStatus.IN_PROGRESS);

        if (!existingAttempts.isEmpty()) {
            throw new QuizAttemptException("You already have an in-progress quiz attempt");
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
        if (request.getAccessCode() != null && !request.getAccessCode().isEmpty()) {
            QuizSession session = quizSessionRepository
                    .findByAccessCodeAndStatus(request.getAccessCode(), SessionStatus.ACTIVE)
                    .orElseThrow(() -> new QuizAttemptException("Invalid access code"));
            attempt.setQuizSession(session);
        }

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        // Build the response DTO
        return buildAttemptResponse(savedAttempt);
    }

    public AttemptResponseDto getAttempt(Long attemptId, Long userId) {
        QuizAttempt attempt = quizAttemptRepository.findByAttemptIdAndUserUserId(attemptId, userId)
                .orElseThrow(() -> new QuizAttemptException("Attempt not found or not accessible"));

        return buildAttemptResponse(attempt);
    }

    public UserResponse submitAnswer(Long attemptId, Long userId, SubmitAnswerRequest request) {
        QuizAttempt attempt = quizAttemptRepository.findByAttemptIdAndUserUserId(attemptId, userId)
                .orElseThrow(() -> new QuizAttemptException("Attempt not found or not accessible"));

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

        Answer selectedAnswer = answerRepository.findById(request.getAnswerId())
                .orElseThrow(() -> new QuizAttemptException("Answer not found"));

        // Verify answer belongs to the question
        if (!selectedAnswer.getQuestion().getQuestionId().equals(question.getQuestionId())) {
            throw new QuizAttemptException("Answer does not belong to this question");
        }

        // Check if response already exists for this question and update it
        Optional<UserResponse> existingResponse = userResponseRepository
                .findByQuizAttemptAttemptIdAndQuestionQuestionId(attemptId, question.getQuestionId());

        UserResponse response;
        if (existingResponse.isPresent()) {
            response = existingResponse.get();
            response.setSelectedAnswer(selectedAnswer);
            response.setIsCorrect(selectedAnswer.getIsCorrect());
            response.setResponseTime(request.getResponseTime());
        } else {
            // Create new response
            response = UserResponse.builder()
                    .quizAttempt(attempt)
                    .question(question)
                    .selectedAnswer(selectedAnswer)
                    .isCorrect(selectedAnswer.getIsCorrect())
                    .responseTime(request.getResponseTime())
                    .build();
        }

        return userResponseRepository.save(response);
    }

    public AttemptResponseDto submitAttempt(Long attemptId, Long userId, SubmitAttemptRequest request) {
        QuizAttempt attempt = quizAttemptRepository.findByAttemptIdAndUserUserId(attemptId, userId)
                .orElseThrow(() -> new QuizAttemptException("Attempt not found or not accessible"));

        // Check if attempt is still in progress
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new QuizAttemptException("Attempt is no longer in progress");
        }

        // Get all responses for this attempt
        List<UserResponse> responses = userResponseRepository.findByQuizAttemptAttemptId(attemptId);

        // Get all questions for this quiz
        List<Question> questions = questionRepository.findByQuizQuizId(attempt.getQuiz().getQuizId());

        // Check if all questions have been answered
        if (responses.size() < questions.size()) {
            throw new QuizAttemptException("Not all questions have been answered");
        }

        // Calculate score
        long correctAnswers = responses.stream()
                .filter(UserResponse::getIsCorrect)
                .count();

        float score = (float) correctAnswers / questions.size() * 100;

        // Update attempt
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setAttemptTime(request.getTotalTime());
        attempt.setScore(score);

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        return buildAttemptResponse(savedAttempt);
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
                .build();
    }
}