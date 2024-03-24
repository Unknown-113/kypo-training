package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

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

    public int getCommandCount() {
        return commandCount;
    }

    public void setCommandCount(int commandCount) {
        this.commandCount = commandCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ForbiddenCommandsDetectionEvent that = (ForbiddenCommandsDetectionEvent) o;
        return commandCount == that.commandCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), commandCount);
    }

    @Override
    public String toString() {
        return "ForbiddenCommandsDetectionEvent{" +
                ", commandCount='" + commandCount +
                '}';
    }
}