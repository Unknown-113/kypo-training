package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import cz.muni.ics.kypo.training.api.enums.CommandType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Encapsulates information about forbidden command.
 */
@ApiModel(value = "DetectedForbiddenCommandDTO", description = "Basic information about detected forbidden command.")
public class DetectedForbiddenCommandDTO {

    @ApiModelProperty(value = "Command.", example = "nmap")
    private String command;
    @ApiModelProperty(value = "Type of command.", example = "BASH")
    private CommandType type;
    @ApiModelProperty(value = "Hostname.", example = "attacker")
    private String hostname;
    @ApiModelProperty(value = "When the command was submitted", example = "1.1.2022 5:55:23")
    private LocalDateTime occurredAt;
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
        DetectedForbiddenCommandDTO that = (DetectedForbiddenCommandDTO) o;
        return Objects.equals(command, that.command) && type == that.type && Objects.equals(hostname, that.hostname) && Objects.equals(occurredAt, that.occurredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, type, hostname, occurredAt);
    }

    @Override
    public String toString() {
        return "DetectedForbiddenCommandDTO{" +
                "command='" + command + '\'' +
                ", type=" + type +
                ", hostname='" + hostname + '\'' +
                ", occurredAt=" + occurredAt +
                '}';
    }
}