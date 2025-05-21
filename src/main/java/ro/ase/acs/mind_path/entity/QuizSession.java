package ro.ase.acs.mind_path.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import ro.ase.acs.mind_path.entity.enums.SessionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Quiz_sessions")
public class QuizSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;
    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    @Column(name = "access_code", nullable = false, unique = true)
    private String accessCode;
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionStatus status;
//    @Column(nullable = false, name = "created_at", updatable = false)
//    @CreationTimestamp
//    private LocalDateTime createdAt;

    /**
     * Automatically generates a unique access code when a session is created.
     */
    @PrePersist
    public void prePersist() {
        if (accessCode == null || accessCode.isEmpty()) {
            accessCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }
    }

    /**
     * Checks if the session is expired when loaded from database and updates status if needed.
     * Note: This doesn't save the entity, so the status change won't be persisted unless the entity is saved.
     */
    @PostLoad
    public void checkExpiration() {
        if (status == SessionStatus.ACTIVE && endTime.isBefore(LocalDateTime.now())) {
            status = SessionStatus.EXPIRED;
            // The actual save operation needs to happen in the service layer
        }
    }
}
