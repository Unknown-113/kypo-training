package cz.muni.ics.kypo.training.api.dto.visualization.analytical;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import lombok.*;

/**
 * Analytical dashboard - participant details.
 */
@Getter
@Setter
@ApiModel(value = "ParticipantAnalyticalDashboardDTO", description = "Analytical dashboard - participant details.")
public class ParticipantAnalyticalDashboardDTO {

    private transient Long userRefId;
    @ApiModelProperty(value = "Participant name", example = "John Doe")
    private String userName;
    @ApiModelProperty(value = "Detailed information about training levels.")
    private List<ParticipantLevelAnalyticalDashboardDTO> levels;

    public void addLevel(ParticipantLevelAnalyticalDashboardDTO level) {
        this.levels.add(level);
    }
}
