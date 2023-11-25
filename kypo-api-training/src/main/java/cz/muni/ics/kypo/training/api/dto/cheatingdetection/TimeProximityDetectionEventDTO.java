package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

/**
 * Encapsulates information about time proximity detection event.
 */
@ApiModel(value = "TimeProximityDetectionEventDTO", description = "A detection event of type Time Proximity.", parent = AbstractDetectionEventDTO.class)
public class TimeProximityDetectionEventDTO extends AbstractDetectionEventDTO {

    @ApiModelProperty(value = "Time threshold for detection.", example = "1")
    private Long threshold;

    public Long getThreshold() {
        return threshold;
    }

    public void setThreshold(Long threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TimeProximityDetectionEventDTO that = (TimeProximityDetectionEventDTO) o;
        return Objects.equals(threshold, that.threshold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), threshold);
    }

    @Override
    public String toString() {
        return "TimeProximityDetectionEventDTO{" +
                "threshold=" + threshold +
                '}';
    }
}
