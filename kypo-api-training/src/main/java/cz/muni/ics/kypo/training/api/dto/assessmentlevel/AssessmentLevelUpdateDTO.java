package cz.muni.ics.kypo.training.api.dto.assessmentlevel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/**
 * Encapsulates information needed to update assessment level.
 *
 */
@Getter
@Setter
@ToString
@ApiModel(value = "AssessmentLevelUpdateDTO", description = "Assessment level to update.")
@JsonIgnoreProperties(value = { "max_score" })
public class AssessmentLevelUpdateDTO extends AbstractLevelUpdateDTO {

    @ApiModelProperty(value = "Questions of assessment level to update.")
    @Valid
    private List<QuestionDTO> questions = new ArrayList<>();
    @ApiModelProperty(value = "Instructions of assessment level to update.", example = "Fill me up slowly")
    @NotNull(message = "{assessmentLevel.instructions.NotNull.message}")
    private String instructions;
    @ApiModelProperty(value = "Type of assessment level to update.", required = true, example = "TEST")
    @NotNull(message = "{assessmentLevel.type.NotNull.message}")
    private AssessmentType type;
    @ApiModelProperty(value = "Estimated time (minutes) taken by the player to solve the level.", required = true, example = "5")
    private int estimatedDuration;
    @ApiModelProperty(value = "Minimal possible solve time (minutes) that must be taken by the player to solve the level.", example = "5")
    protected Integer minimalPossibleSolveTime;

    public AssessmentLevelUpdateDTO() {
        this.levelType = LevelType.ASSESSMENT_LEVEL;
    }
}
