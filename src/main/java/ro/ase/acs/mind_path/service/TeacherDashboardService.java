package ro.ase.acs.mind_path.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.ase.acs.mind_path.dto.response.StudentProgressDto;
import ro.ase.acs.mind_path.dto.response.TeacherDashboardStatsDto;
import ro.ase.acs.mind_path.entity.Quiz;
import ro.ase.acs.mind_path.entity.QuizAttempt;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.entity.enums.AttemptStatus;
import ro.ase.acs.mind_path.repository.QuizAttemptRepository;
import ro.ase.acs.mind_path.repository.QuizRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherDashboardService {
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;


    public List<StudentProgressDto> getStudentProgress(Long teacherId) {
        List<Quiz> teacherQuizzes = quizRepository.findByCreatedByUserId(teacherId);
        if (teacherQuizzes.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> quizIds = teacherQuizzes.stream()
                .map(Quiz::getQuizId)
                .toList();
        List<QuizAttempt> completedAttempts = quizAttemptRepository.findByQuizQuizIdIn(quizIds)
                .stream()
                .filter(attempt -> attempt.getStatus() == AttemptStatus.SUBMITTED ||
                        attempt.getStatus() == AttemptStatus.GRADED ||
                        attempt.getStatus() == AttemptStatus.ABANDONED)
                .toList();
        Map<Long, List<QuizAttempt>> attemptsByStudent = completedAttempts.stream()
                .collect(Collectors.groupingBy(attempt -> attempt.getUser().getUserId()));
        return attemptsByStudent.entrySet().stream()
                .map(entry -> {
                    Long studentId = entry.getKey();
                    List<QuizAttempt> studentAttempts = entry.getValue();
                    User student = studentAttempts.getFirst().getUser();
                    double avgScore = studentAttempts.stream()
                            .mapToDouble(QuizAttempt::getScore)
                            .average()
                            .orElse(0.0);
                    LocalDateTime lastQuizDate = studentAttempts.stream()
                            .map(QuizAttempt::getCompletedAt)
                            .filter(Objects::nonNull)
                            .max(LocalDateTime::compareTo)
                            .orElse(null);
                    return StudentProgressDto.builder()
                            .studentId(studentId)
                            .firstName(student.getFirstName())
                            .lastName(student.getLastName())
                            .quizzesTaken(studentAttempts.size())
                            .avgScore(avgScore)
                            .lastActive(lastQuizDate)
                            .build();
                })
                .sorted(Comparator.comparing(StudentProgressDto::getLastName))
                .toList();
    }

    public TeacherDashboardStatsDto getDashboardStats(Long teacherId) {
        List<Quiz> teacherQuizzes = quizRepository.findByCreatedByUserId(teacherId);
        if (teacherQuizzes.isEmpty()) {
            return TeacherDashboardStatsDto.builder()
                    .activeStudentsCount(0)
                    .completionRate(0.0)
                    .build();
        }
        List<Long> quizIds = teacherQuizzes.stream()
                .map(Quiz::getQuizId)
                .toList();
        List<QuizAttempt> allAttempts = quizAttemptRepository.findByQuizQuizIdIn(quizIds);
        if (allAttempts.isEmpty()) {
            return TeacherDashboardStatsDto.builder()
                    .activeStudentsCount(0)
                    .completionRate(0.0)
                    .build();
        }
        int activeStudentsCount = (int) allAttempts.stream()
                .map(attempt -> attempt.getUser().getUserId())
                .distinct()
                .count();
        int totalAttempts = allAttempts.size();
        int completedAttempts = (int) allAttempts.stream()
                .filter(attempt -> attempt.getStatus() == AttemptStatus.SUBMITTED ||
                        attempt.getStatus() == AttemptStatus.GRADED)
                .count();
        double completionRate = completedAttempts * 100.0 / totalAttempts;
        return TeacherDashboardStatsDto.builder()
                .activeStudentsCount(activeStudentsCount)
                .completionRate(completionRate)
                .build();
    }
}
