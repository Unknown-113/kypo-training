package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import cz.muni.ics.kypo.training.api.enums.TDState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * Encapsulates basic information about Training Definition.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingDefinitionInfoDTO", description = "Basic training definition information.")
public class TrainingDefinitionInfoDTO {

    @ApiModelProperty(value = "Main identifier of training definition.", example = "1")
    private Long id;
    @ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", example = "TrainingDefinition2")
    private String title;
    @ApiModelProperty(value = "Current state of training definition.", example = "UNRELEASED")
    private TDState state;
}
