package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(value = "ForbiddenCommandsDetectionEventDTO", description = "A detection event of type Forbidden Commands.", parent = AbstractDetectionEventDTO.class)
public class ForbiddenCommandsDetectionEventDTO extends AbstractDetectionEventDTO {

    @ApiModelProperty(value = "count of forbidden commands.", example = "10")
    private int commandCount;
}
