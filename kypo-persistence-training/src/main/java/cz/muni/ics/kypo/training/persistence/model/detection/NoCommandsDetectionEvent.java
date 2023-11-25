package cz.muni.ics.kypo.training.persistence.model.detection;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

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
public class NoCommandsDetectionEvent extends AbstractDetectionEvent {

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoCommandsDetectionEvent)) return false;
        if (!super.equals(o)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public String toString() {
        return "NoCommandsDetectionEvent{" +
                '}';
    }
}
