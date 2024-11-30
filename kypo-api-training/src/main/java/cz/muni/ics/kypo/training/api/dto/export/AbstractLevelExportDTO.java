package cz.muni.ics.kypo.training.api.dto.export;

import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * Encapsulates information about abstract level.
 * Extended by {@link AssessmentLevelExportDTO}, {@link TrainingLevelExportDTO}, {@link AccessLevelExportDTO} and {@link InfoLevelExportDTO}
 *
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(value = "AbstractLevelExportDTO", subTypes = {TrainingLevelExportDTO.class, AccessLevelExportDTO.class, InfoLevelExportDTO.class, AssessmentLevelExportDTO.class},
        description = "Superclass for classes TrainingLevelExportDTO, AccessLevelExportDTO, InfoLevelExportDTO and AssessmentLevelExportDTO")
public class AbstractLevelExportDTO {

    @ApiModelProperty(value = "Short textual description of the level.", example = "Training Level1")
    protected String title;
    @ApiModelProperty(value = "Type of the level.", example = "TRAINING")
    protected LevelType levelType;
    @ApiModelProperty(value = "Order of level, starts with 0", example = "2")
    protected int order;
    @ApiModelProperty(value = "Estimated time (minutes) taken by the player to solve the level.", example = "5")
    protected int estimatedDuration;
    @ApiModelProperty(value = "Minimal possible solve time (minutes) that must be taken by the player to solve the level.", example = "5")
    protected Integer minimalPossibleSolveTime;
}
