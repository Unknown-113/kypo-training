package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(value = "TimeProximityDetectionEventDTO", description = "A detection event of type Time Proximity.", parent = AbstractDetectionEventDTO.class)
public class TimeProximityDetectionEventDTO extends AbstractDetectionEventDTO {

    @ApiModelProperty(value = "Time threshold for detection.", example = "1")
    private Long threshold;
}
