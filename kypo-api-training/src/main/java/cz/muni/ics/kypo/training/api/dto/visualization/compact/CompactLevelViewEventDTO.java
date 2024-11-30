package cz.muni.ics.kypo.training.api.dto.visualization.compact;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "CompactLevelViewEventDTO", description = "Information about a specific event for compact level visualization.")
public class CompactLevelViewEventDTO {

    @ApiModelProperty(value = "Timestamp of the event.", example = "364897891")
    private Long timestamp;
    @ApiModelProperty(value = "Type of the event.", example = "cz.muni.csirt.kypo.events.trainings.TrainingRunResumed")
    private String type;
    @ApiModelProperty(value = "Commands used between the last event and this event.")
    private List<String> commands = new ArrayList<>();

    /**
     * Add command to the list of commands
     * @param command command to add
     * @return true if the command was added, false otherwise
     */
    public boolean addCommand(String command) {
        return this.commands.add(command);
    }
}
