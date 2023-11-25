package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

/**
 * Encapsulates information about no commands detection event.
 */
@ApiModel(value = "NoCommandsDetectionEventDTO", description = "A detection event of type No Commands.", parent = AbstractDetectionEventDTO.class)
public class NoCommandsDetectionEventDTO extends AbstractDetectionEventDTO {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public String toString() {
        return "NoCommandsDetectionEventDTO{" +
                '}';
    }
}
