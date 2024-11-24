package cz.muni.ics.kypo.training.persistence.model.detection;

import javax.persistence.*;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@Entity
@Table(name = "forbidden_commands_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
        @NamedQuery(
                name = "ForbiddenCommandsDetectionEvent.findForbiddenCommandsEventById",
                query = "SELECT fcde FROM ForbiddenCommandsDetectionEvent fcde WHERE fcde.id = :eventId"
        ),
        @NamedQuery(
                name = "ForbiddenCommandsDetectionEvent.findAllByCheatingDetectionId",
                query = "SELECT fcde FROM ForbiddenCommandsDetectionEvent fcde WHERE fcde.cheatingDetectionId = :cheatingDetectionId"
        )
})
public class ForbiddenCommandsDetectionEvent extends AbstractDetectionEvent {

    @Column(name = "command_count", nullable = false)
    private int commandCount;
}