package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import lombok.*;

/**
 * Encapsulates information about Training Definition
 *
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingDefinitionDTO", description = "A blueprint of abstract levels.")
public class TrainingDefinitionDTO {

    @ApiModelProperty(value = "Main identifier of training definition.", example = "1")
    private Long id;
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
    @ApiModelProperty(value = "Group of organizers who is allowed to see the training definition.", example = "14")
    private Long betaTestingGroupId;
    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", example = "false")
    private boolean showStepperBar;
    @ApiModelProperty(value = "Sign if training definition can be archived or not.", example = "false")
    private boolean canBeArchived;
    @ApiModelProperty(value = "Estimated time it takes to finish runs created from this definition.", example = "5")
    private long estimatedDuration;
    @ApiModelProperty(value = "Time of last edit done to definition.", example = "2017-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime lastEdited;
    @ApiModelProperty(value = "Name of the user who has done the last edit in definition.", example = "John Doe")
    private String lastEditedBy;
    @ApiModelProperty(value = "Time of creation of definition.", example = "2017-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime createdAt;
}
