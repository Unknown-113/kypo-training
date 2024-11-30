package cz.muni.ics.kypo.training.api.dto.traininginstance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * Encapsulates the information stating if training instance has finished.
 */
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingInstanceIsFinishedInfoDTO", description = "Information stating if training instance has finished.")
public class TrainingInstanceIsFinishedInfoDTO {

    @ApiModelProperty(value = "Sign if training instance is finished or not.", example = "false")
    private boolean hasFinished;
    @ApiModelProperty(value = "Message about training instance state.", example = "false")
    private String message;
}
