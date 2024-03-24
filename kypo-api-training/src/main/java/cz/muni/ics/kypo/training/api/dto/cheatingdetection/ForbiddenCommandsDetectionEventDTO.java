package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Encapsulates information about forbidden commands detection event.
 */
@ApiModel(value = "ForbiddenCommandsDetectionEventDTO", description = "A detection event of type Forbidden Commands.", parent = AbstractDetectionEventDTO.class)
public class ForbiddenCommandsDetectionEventDTO extends AbstractDetectionEventDTO {

    @ApiModelProperty(value = "count of forbidden commands.", example = "10")
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
        ForbiddenCommandsDetectionEventDTO that = (ForbiddenCommandsDetectionEventDTO) o;
        return commandCount == that.commandCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), commandCount);
    }

    @Override
    public String toString() {
        return "ForbiddenCommandsDetectionEventDTO{" +
                "commandCount=" + commandCount +
                '}';
    }
}
