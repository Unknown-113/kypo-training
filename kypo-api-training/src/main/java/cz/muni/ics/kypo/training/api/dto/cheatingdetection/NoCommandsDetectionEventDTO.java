package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import lombok.*;

@ToString
@EqualsAndHashCode
@ApiModel(value = "NoCommandsDetectionEventDTO", description = "A detection event of type No Commands.", parent = AbstractDetectionEventDTO.class)
public class NoCommandsDetectionEventDTO extends AbstractDetectionEventDTO {

}
