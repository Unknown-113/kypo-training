package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.persistence.model.AbstractEntity;
import cz.muni.ics.kypo.training.persistence.model.enums.CommandType;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@Entity
@Table(name = "detected_forbidden_command")
@NamedQueries({
        @NamedQuery(
                name = "DetectedForbiddenCommand.findAllByEventId",
                query = "SELECT dfc FROM DetectedForbiddenCommand dfc " +
                        "WHERE dfc.detectionEventId = :eventId"
        ),
        @NamedQuery(
                name = "DetectedForbiddenCommand.deleteAllByDetectionEventId",
                query = "DELETE FROM DetectedForbiddenCommand dfc WHERE dfc.detectionEventId = :eventId"
        )
})
public class DetectedForbiddenCommand extends AbstractEntity<Long> {

    @Column(name = "command", nullable = false)
    private String command;
    @Column(name = "command_type", nullable = false)
    private CommandType type;
    @Column(name = "detection_event_id", nullable = false)
    private Long detectionEventId;
    @Column(name = "hostname")
    private String hostname;
    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;
}
