package cz.muni.ics.kypo.training.api.dto.visualization;

import cz.muni.ics.kypo.training.api.dto.export.AbstractLevelExportDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import lombok.*;

/**
 * Encapsulates information about training level. Inherits from {@link AbstractLevelVisualizationDTO}
 * Used for visualization.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(value = "TrainingLevelVisualizationDTO", description = "Information about training level needed for visualizations.", parent = AbstractLevelExportDTO.class)
public class TrainingLevelVisualizationDTO extends AbstractLevelVisualizationDTO {

    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Play me")
    private String content;
    @ApiModelProperty(value = "Instruction how to get answer in training.", example = "This is how you do it")
    private String solution;
    @ApiModelProperty(value = "Keyword found in training, used for access next level.", example = "secretAnswer")
    private String answer;
    @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
    private boolean solutionPenalized;
    @ApiModelProperty(value = "Information which helps player resolve the level.")
    private List<HintDTO> hints;
}
