package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.persistence.model.AbstractEntity;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@Entity
@Table(name = "detection_event_participant")
@NamedQueries({
        @NamedQuery(
                name = "DetectionEventParticipant.findAllByEventId",
                query = "SELECT dep FROM DetectionEventParticipant dep " +
                        "WHERE dep.detectionEventId = :eventId " +
                        "ORDER BY dep.occurredAt"
        ),
        @NamedQuery(
                name = "DetectionEventParticipant.deleteAllParticipantsByCheatingDetectionId",
                query = "DELETE FROM DetectionEventParticipant dep WHERE dep.cheatingDetectionId = :cheatingDetectionId"
        )
})
public class DetectionEventParticipant extends AbstractEntity<Long> {

    @Column(name = "ip_address", nullable = true)
    private String ipAddress;
    @Column(name = "occurred_at", nullable = true)
    private LocalDateTime occurredAt;
    @Column(name = "participant_name", nullable = false)
    private String participantName;
    @Column(name = "solved_in_time", nullable = true)
    private Long solvedInTime;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "detection_event_id", nullable = false)
    private Long detectionEventId;
    @Column(name = "cheating_detection_id", nullable = false)
    private Long cheatingDetectionId;
}
