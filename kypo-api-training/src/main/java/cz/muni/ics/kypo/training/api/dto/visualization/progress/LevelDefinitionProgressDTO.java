package cz.muni.ics.kypo.training.api.dto.visualization.progress;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;
import lombok.*;

/**
 * Encapsulates information about levels.
 *
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "LevelDefinitionProgressDTO", description = "Contains info about levels based on the level type.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LevelDefinitionProgressDTO {

    @ApiModelProperty(value = "Main identifier of level.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Short textual description of the level.", example = "Training Level1")
    private String title;
    @ApiModelProperty(value = "The maximum score a participant can achieve during a level.", example = "20")
    private int maxScore;
    @ApiModelProperty(value = "Type of the level.", example = "TRAINING")
    private LevelType levelType;
    @ApiModelProperty(value = "Estimated time taken by the player to resolve the level.", example = "5")
    private int estimatedDuration;
    @ApiModelProperty(value = "Order of level, starts with 0", example = "2")
    private int order;
    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Play me")
    private String content;
    @ApiModelProperty(value = "Keyword found in training, used for access next level.", example = "secretAnswer")
    private String answer;
    @ApiModelProperty(value = "Instruction how to get answer in training.", example = "This is how you do it")
    private String solution;
    @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
    private Boolean solutionPenalized;
    @ApiModelProperty(value = "Information which helps player resolve the level.")
    private Set<HintProgressDTO> hints;
    @ApiModelProperty(value = "Type of assessment.", example = "TEST")
    private AssessmentType assessmentType;
}
