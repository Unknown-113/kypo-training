package cz.muni.ics.kypo.training.api.dto.visualization;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.*;

/**
 * Encapsulates information about abstract level.
 * Used for visualization.
 * Extended by {@link AssessmentLevelVisualizationDTO}, {@link TrainingLevelVisualizationDTO}, {@link AccessLevelVisualizationDTO} and {@link InfoLevelVisualizationDTO}.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "AbstractLevelVisualizationDTO", subTypes = {TrainingLevelVisualizationDTO.class, AccessLevelVisualizationDTO.class, InfoLevelVisualizationDTO.class, AssessmentLevelVisualizationDTO.class},
        description = "Superclass for classes TrainingLevelVisualizationDTO, AccessLevelVisualizationDTO, AssessmentLevelVisualizationDTO and InfoLevelVisualizationDTO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractLevelVisualizationDTO {

    @ApiModelProperty(value = "Main identifier of level.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Short textual description of the level.", example = "Training Level1")
    private String title;
    @ApiModelProperty(value = "The maximum score a participant can achieve during a level.", example = "20")
    private int maxScore;
    @ApiModelProperty(value = "Type of the level.", example = "TRAINING")
    private LevelType levelType;
    @ApiModelProperty(value = "Estimated time taken by the player to resolve the level.", example = "5")
    private long estimatedDuration;
    @ApiModelProperty(value = "Order of level among levels in training definition.", example = "1")
    private int order;
}
