package cz.muni.ics.kypo.training.api.dto.visualization.compact;

import com.google.common.base.Objects;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "CompactLevelViewUserDTO", description = "Information about a specific user for compact level visualization.")
public class CompactLevelViewUserDTO {

    @ApiModelProperty(value = "Information about the user.")
    private UserRefDTO user;
    @ApiModelProperty(value = "Events from the training run this user was in.")
    private List<CompactLevelViewEventDTO> events;

    /**
     * Gets user
     * @return the user
     */
    public UserRefDTO getUser() {
        return user;
    }

    /**
     * Sets the user
     * @param user new user
     */
    public void setUser(UserRefDTO user) {
        this.user = user;
    }

    /**
     * Gets events
     * @return the events
     */
    public List<CompactLevelViewEventDTO> getEvents() {
        return events;
    }

    /**
     * Sets the events
     * @param events new events
     */
    public void setEvents(List<CompactLevelViewEventDTO> events) {
        this.events = events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompactLevelViewUserDTO)) return false;
        CompactLevelViewUserDTO that = (CompactLevelViewUserDTO) o;
        return Objects.equal(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(user);
    }

    @Override
    public String toString() {
        return "CompactLevelViewUserDTO{" +
                "user=" + user +
                '}';
    }
}
