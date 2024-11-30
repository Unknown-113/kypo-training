package cz.muni.ics.kypo.training.persistence.model.detection;

import javax.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Entity
@Table(name = "no_commands_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
        @NamedQuery(
                name = "NoCommandsDetectionEvent.findNoCommandsEventById",
                query = "SELECT ncde FROM NoCommandsDetectionEvent ncde WHERE ncde.id = :eventId"
        ),
        @NamedQuery(
                name = "NoCommandsDetectionEvent.findAllByCheatingDetectionId",
                query = "SELECT ncde FROM NoCommandsDetectionEvent ncde WHERE ncde.cheatingDetectionId = :cheatingDetectionId"
        )
})
public class NoCommandsDetectionEvent extends AbstractDetectionEvent { }
