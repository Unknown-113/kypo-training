package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.persistence.model.AbstractEntity;
import cz.muni.ics.kypo.training.persistence.model.enums.CommandType;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "forbidden_command")
public class ForbiddenCommand extends AbstractEntity<Long> {

    @Column(name = "command", nullable = false)
    private String command;
    @Column(name = "command_type", nullable = false)
    private CommandType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cheating_detection_id")
    private CheatingDetection detection;

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

    public CheatingDetection getDetection() {
        return detection;
    }

    public void setDetection(CheatingDetection detection) {
        this.detection = detection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForbiddenCommand that = (ForbiddenCommand) o;
        return Objects.equals(command, that.command) &&
                type == that.type &&
                Objects.equals(detection, that.detection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, type, detection);
    }

    @Override
    public String toString() {
        return "ForbiddenCommand{" +
                "command='" + command + '\'' +
                ", type=" + type +
                ", detection=" + detection +
                '}';
    }
}
