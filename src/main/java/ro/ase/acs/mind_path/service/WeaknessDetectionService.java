package ro.ase.acs.mind_path.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.ase.acs.mind_path.dto.response.WeaknessReportDto;
import ro.ase.acs.mind_path.entity.UserResponse;
import ro.ase.acs.mind_path.entity.enums.QuestionType;
import ro.ase.acs.mind_path.repository.UserResponseRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeaknessDetectionService {

    private final UserResponseRepository userResponseRepository;

    public WeaknessReportDto generateWeaknessReport(Long studentId,
                                                    LocalDate from,
                                                    LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end   = to.plusDays(1).atStartOfDay();

        List<UserResponse> responses = userResponseRepository
                .findByQuizAttemptUserUserIdAndQuizAttemptCompletedAtBetween(studentId, start, end);

        int total = responses.size();

        Map<QuestionType, List<UserResponse>> byType =
                responses.stream().collect(Collectors.groupingBy(r -> r.getQuestion().getType()));

        Map<String, WeaknessReportDto.QuestionTypeStats> stats =
                byType.entrySet().stream().collect(Collectors.toMap(
                        e -> e.getKey().name(),
                        e -> {
                            List<UserResponse> list = e.getValue();
                            int incorrect = (int) list.stream().filter(r -> !r.getIsCorrect()).count();
                            double avg = list.stream().mapToInt(UserResponse::getResponseTime).average().orElse(0);
                            return new WeaknessReportDto.QuestionTypeStats(list.size(), incorrect, avg);
                        }
                ));

        int rushingErrors = (int) responses.stream()
                .filter(r -> !r.getIsCorrect() && r.getResponseTime() < WeaknessReportDto.FAST_SEC)
                .count();

        return new WeaknessReportDto(total, rushingErrors, stats);
    }

}
