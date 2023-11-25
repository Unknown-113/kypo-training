package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;

import javax.persistence.*;
import java.util.Objects;

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

    @Column(name = "forbidden_commands", nullable = false)
    private String[] forbiddenCommands;

    public String[] getForbiddenCommands() {
        return forbiddenCommands;
    }

    public void setForbiddenCommands(String[] forbiddenCommands) {
        this.forbiddenCommands = forbiddenCommands;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForbiddenCommandsDetectionEvent)) return false;
        if (!super.equals(o)) return false;
        ForbiddenCommandsDetectionEvent other = (ForbiddenCommandsDetectionEvent) o;
        return Objects.equals(getForbiddenCommands(), other.getForbiddenCommands());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getForbiddenCommands());
    }

    @Override
    public String toString() {
        return "ForbiddenCommandsDetectionEvent{" +
                ", forbiddenCommands='" + forbiddenCommands +
                '}';
    }
}