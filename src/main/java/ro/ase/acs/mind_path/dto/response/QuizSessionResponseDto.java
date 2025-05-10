package ro.ase.acs.mind_path.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.acs.mind_path.entity.enums.SessionStatus;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizSessionResponseDto {
    private Long sessionId;
    private Long quizId;
    private String accessCode;
    private Long createdBy;
    private SessionStatus status;
    private String createdAt;
    private String expiresAt;
}
