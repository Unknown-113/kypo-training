package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulates information about forbidden commands detection event.
 */
@ApiModel(value = "ForbiddenCommandsDetectionEventDTO", description = "A detection event of type Forbidden Commands.", parent = AbstractDetectionEventDTO.class)
public class ForbiddenCommandsDetectionEventDTO extends AbstractDetectionEventDTO {

    @ApiModelProperty(value = "List of forbidden commands.", example = "1")
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
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ForbiddenCommandsDetectionEventDTO that = (ForbiddenCommandsDetectionEventDTO) o;
        return Objects.equals(forbiddenCommands, that.forbiddenCommands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), forbiddenCommands);
    }

    @Override
    public String toString() {
        return "ForbiddenCommandsDetectionEventDTO{" +
                "forbiddenCommands=" + forbiddenCommands +
                '}';
    }
}
