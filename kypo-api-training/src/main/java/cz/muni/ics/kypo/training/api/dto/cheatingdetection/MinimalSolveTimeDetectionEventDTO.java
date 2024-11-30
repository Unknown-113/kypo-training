package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(value = "MinimalSolveTimeDetectionEventDTO", description = "A detection event of type Minimal Solve Time.", parent = AbstractDetectionEventDTO.class)
public class MinimalSolveTimeDetectionEventDTO extends AbstractDetectionEventDTO {

    @ApiModelProperty(value = "Minimal time required to solve the level.", example = "1")
    private Long minimalSolveTime;
}
