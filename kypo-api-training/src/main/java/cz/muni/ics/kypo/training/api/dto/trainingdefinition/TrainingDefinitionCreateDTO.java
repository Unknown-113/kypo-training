package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupCreateDTO;
import cz.muni.ics.kypo.training.api.enums.TDState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.*;

/**
 * Encapsulates information about Training definition, intended for creation of new definition.
 *
 */
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingDefinitionCreateDTO", description = "Training definition to create.")
public class TrainingDefinitionCreateDTO {

    @ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", required = true, example = "Photo Hunter")
    @NotEmpty(message = "{trainingDefinition.title.NotEmpty.message}")
    private String title;
    @ApiModelProperty(value = "Description of training definition that is visible to the participant.", example = "Description of Photo Hunter")
    private String description;
    @ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.", example = "[HTML, http protocol]")
    private String[] prerequisites;
    @ApiModelProperty(value = "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ", example = "[outcomes]")
    private String[] outcomes;
    @ApiModelProperty(value = "Current state of training definition.", required = true, example = "UNRELEASED")
    @NotNull(message = "{trainingDefinition.state.NotNull.message}")
    private TDState state;
    @ApiModelProperty(value = "Group of organizers who is allowed to see the training definition.", required = true)
    @Valid
    private BetaTestingGroupCreateDTO betaTestingGroup;
    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", required = true, example = "true")
    @NotNull(message = "{trainingDefinition.showStepperBar.NotNull.message}")
    private boolean showStepperBar;
    @ApiModelProperty(value = "Sign if default levels should be created.", example = "false")
    private boolean defaultContent;
}
