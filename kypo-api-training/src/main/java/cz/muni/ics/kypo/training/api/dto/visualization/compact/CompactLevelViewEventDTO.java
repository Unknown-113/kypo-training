package cz.muni.ics.kypo.training.api.dto.visualization.compact;

import com.google.common.base.Objects;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "CompactLevelViewEventDTO", description = "Information about a specific event for compact level visualization.")
public class CompactLevelViewEventDTO {

    @ApiModelProperty(value = "Timestamp of the event.", example = "364897891")
    private Long timestamp;
    @ApiModelProperty(value = "Type of the event.", example = "cz.muni.csirt.kypo.events.trainings.TrainingRunResumed")
    private String type;
    @ApiModelProperty(value = "Commands used between the last event and this event.")
    private List<String> commands = new ArrayList<>();

    /**
     * Gets timestamp
     * @return the timestamp
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp
     * @param timestamp new timestamp
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets type
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type
     * @param type new type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets commands
     * @return the commands
     */
    public List<String> getCommands() {
        return commands;
    }

    /**
     * Sets the commands
     * @param commands new commands
     */
    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    /**
     * Add command to the list of commands
     * @param command command to add
     * @return true if the command was added, false otherwise
     */
    public boolean addCommand(String command) {
        return this.commands.add(command);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompactLevelViewEventDTO)) return false;
        CompactLevelViewEventDTO that = (CompactLevelViewEventDTO) o;
        return Objects.equal(timestamp, that.timestamp) && Objects.equal(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(timestamp, type);
    }

    @Override
    public String toString() {
        return "CompactLevelViewEventDTO{" +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                ", commands=" + commands +
                '}';
    }
}
