package cz.muni.ics.kypo.training.api.dto.visualization.analytical;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Analytical dashboard - participant details.
 */
@ApiModel(value = "ParticipantAnalyticalDashboardDTO", description = "Analytical dashboard - participant details.")
public class ParticipantAnalyticalDashboardDTO {

    private transient Long userRefId;
    @ApiModelProperty(value = "Participant name", example = "John Doe")
    private String userName;
    @ApiModelProperty(value = "Detailed information about training levels.")
    private List<ParticipantLevelAnalyticalDashboardDTO> levels;

    public Long getUserRefId() {
        return userRefId;
    }

    public void setUserRefId(Long userRefId) {
        this.userRefId = userRefId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<ParticipantLevelAnalyticalDashboardDTO> getLevels() {
        return levels;
    }

    public void setLevels(List<ParticipantLevelAnalyticalDashboardDTO> levels) {
        this.levels = levels;
    }

    public void addLevel(ParticipantLevelAnalyticalDashboardDTO level) {
        this.levels.add(level);
    }
}
