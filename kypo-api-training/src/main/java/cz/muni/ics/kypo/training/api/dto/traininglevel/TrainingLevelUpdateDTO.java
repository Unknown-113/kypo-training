package cz.muni.ics.kypo.training.api.dto.traininglevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.api.dto.technique.MitreTechniqueDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.*;
import lombok.*;

/**
 * Encapsulates information needed to update training level.
 *
 */
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingLevelUpdateDTO", description = "Training level to update.")
public class TrainingLevelUpdateDTO extends AbstractLevelUpdateDTO {

    @ApiModelProperty(value = "The maximum score a participant can achieve during a level.", required = true, example = "20")
    @NotNull(message = "{trainingLevel.maxScore.NotNull.message}")
    @Min(value = 0, message = "{trainingLevel.maxScore.Min.message}")
    @Max(value = 100, message = "{trainingLevel.maxScore.Max.message}")
    private int maxScore;
    @ApiModelProperty(value = "Keyword found in training, used for access next level.", required = true, example = "secretAnswer")
    @Size(max = 50, message = "{trainingLevel.answer.Size.message}")
    private String answer;
    @ApiModelProperty(value = "Identifier that is used to obtain answer from remote storage.", example = "username")
    @Size(max = 50, message = "{trainingLevel.answerVariableName.Size.message}")
    private String answerVariableName;
    @ApiModelProperty(value = "The information and experiences that are directed towards an player.", example = "Play me")
    @NotEmpty(message = "{trainingLevel.content.NotEmpty.message}")
    private String content;
    @ApiModelProperty(value = "Instruction how to get answer in training.", example = "This is how you do it")
    @NotEmpty(message = "{trainingLevel.solution.NotEmpty.message}")
    private String solution;
    @ApiModelProperty(value = "Sign if displaying of solution is penalized.", required = true, example = "false")
    @NotNull(message = "{trainingLevel.solutionPenalized.NotNull.message}")
    private boolean solutionPenalized;
    @ApiModelProperty(value = "Estimated time (minutes) taken by the player to solve the level.", example = "20")
    private int estimatedDuration;
    @ApiModelProperty(value = "How many times participant can submit incorrect answer before displaying solution.", required = true, example = "5")
    @NotNull(message = "{trainingLevel.incorrectAnswerLimit.NotNull.message}")
    @Min(value = 0, message = "{trainingLevel.incorrectAnswerLimit.Min.message}")
    @Max(value = 100, message = "{trainingLevel.incorrectAnswerLimit.Max.message}")
    private int incorrectAnswerLimit;
    @Valid
    @ApiModelProperty(value = "Information which helps participant resolve the level.")
    private Set<HintDTO> hints = new HashSet<>();
    @ApiModelProperty(value = "Indicates if flags/answers are randomly generated and are different for each trainee. Default is false.", example = "false")
    private boolean variantAnswers;
    @Valid
    @ApiModelProperty(value = "Sequence of commands that leads to the level answer.", example = "false")
    private List<ReferenceSolutionNodeDTO> referenceSolution = new ArrayList<>();
    @Valid
    @ApiModelProperty(value = "List of mitre techniques used in the training level.")
    private List<MitreTechniqueDTO> mitreTechniques;
    @ApiModelProperty(value = "Set of the expected commands to be executed during the training level.")
    private Set<String> expectedCommands;
    @ApiModelProperty(value = "Minimal possible solve time (minutes) that must be taken by the player to solve the level.", example = "5")
    protected Integer minimalPossibleSolveTime;
    @ApiModelProperty(value = "Indicates if at least one command has to be executed to complete the level. Default is true.", example = "true")
    private boolean commandsRequired;

    public TrainingLevelUpdateDTO() {
        this.levelType = LevelType.TRAINING_LEVEL;
    }
}
