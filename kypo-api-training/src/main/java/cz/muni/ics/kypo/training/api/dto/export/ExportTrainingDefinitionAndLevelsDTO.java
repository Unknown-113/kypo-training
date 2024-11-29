package cz.muni.ics.kypo.training.api.dto.export;

import cz.muni.ics.kypo.training.api.enums.TDState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

/**
 * Encapsulates information about training definition and its levels.
 *
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(value = "ExportTrainingDefinitionAndLevelsDTO", description = "An exported detailed information about training definition which also include individual levels.")
public class ExportTrainingDefinitionAndLevelsDTO {

    @ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", example = "TrainingDefinition2")
    private String title;
    @ApiModelProperty(value = "Description of training definition that is visible to the participant.", example = "Unreleased training definition")
    private String description;
    @ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.", example = "")
    private String[] prerequisites;
    @ApiModelProperty(value = "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ", example = "")
    private String[] outcomes;
    @ApiModelProperty(value = "Current state of training definition.", example = "UNRELEASED")
    private TDState state;
    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", example = "false")
    private boolean showStepperBar;
    @ApiModelProperty(value = "Information about all levels in training definition.")
    private List<AbstractLevelExportDTO> levels = new ArrayList<>();
    @ApiModelProperty(value = "Estimated time (minutes) taken by the player to finish run created from this definition.", example = "5")
    private int estimatedDuration;
    @ApiModelProperty(value = "Marking if levels flags/answers are randomly generated and are different for each trainee. Default is false.", example = "false")
    private boolean variantSandboxes;
}
