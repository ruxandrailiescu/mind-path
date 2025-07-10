package ro.ase.acs.mind_path.dto.response;

import java.util.Map;

public record WeaknessReportDto(
        int totalQuestions,
        int rushingErrors,
        Map<String, QuestionTypeStats> statsByType
) {
    public static final int FAST_SEC = 5;

    public record QuestionTypeStats(
            int attempted,
            int incorrect,
            double averageTimeSec
    ) {}
}
