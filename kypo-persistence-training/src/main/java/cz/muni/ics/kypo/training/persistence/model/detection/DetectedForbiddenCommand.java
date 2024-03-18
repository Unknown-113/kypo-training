package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.persistence.model.AbstractEntity;
import cz.muni.ics.kypo.training.persistence.model.enums.CommandType;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "detected_forbidden_command")
@NamedQueries({
        @NamedQuery(
                name = "DetectedForbiddenCommand.findAllByEventId",
                query = "SELECT dfc FROM DetectedForbiddenCommand dfc " +
                        "WHERE dfc.detectionEventId = :eventId"
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

    public DetectedForbiddenCommand() {
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    public Long getDetectionEventId() {
        return detectionEventId;
    }

    public void setDetectionEventId(Long detectionEventId) {
        this.detectionEventId = detectionEventId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetectedForbiddenCommand that = (DetectedForbiddenCommand) o;
        return Objects.equals(command, that.command) &&
                type == that.type &&
                Objects.equals(detectionEventId, that.detectionEventId) &&
                Objects.equals(hostname, that.hostname) &&
                Objects.equals(occurredAt, that.occurredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, type, detectionEventId, hostname, occurredAt);
    }

    @Override
    public String toString() {
        return "DetectedForbiddenCommand{" +
                "command='" + command + '\'' +
                ", type=" + type +
                ", detectionEventId=" + detectionEventId +
                ", hostname='" + hostname + '\'' +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
