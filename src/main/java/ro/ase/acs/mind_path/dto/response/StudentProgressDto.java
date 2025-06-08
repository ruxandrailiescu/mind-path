package ro.ase.acs.mind_path.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentProgressDto {
    private Long studentId;
    private String firstName;
    private String lastName;
    private Integer quizzesTaken;
    private Double avgScore;
    private LocalDateTime lastActive;
}
