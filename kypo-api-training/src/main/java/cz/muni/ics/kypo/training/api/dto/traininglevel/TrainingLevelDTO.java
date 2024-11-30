package cz.muni.ics.kypo.training.api.dto.traininglevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.api.dto.technique.MitreTechniqueDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.*;
import lombok.*;

/**
 * Encapsulates information about training level. Inherits from {@link AbstractLevelDTO}
 *
 */
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingLevelDTO", description = "An assignment containing security tasks whose completion yields a answer.", parent = AbstractLevelDTO.class)
public class TrainingLevelDTO extends AbstractLevelDTO {

    @ApiModelProperty(value = "Keyword found in training, used for access next level.", example = "secretAnswer")
    private String answer;
    @ApiModelProperty(value = "Identifier that is used to obtain answer from remote storage.", example = "username")
    private String answerVariableName;
    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Play me")
    private String content;
    @ApiModelProperty(value = "Instruction how to get answer in training.", example = "This is how you do it")
    private String solution;
    @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
    private boolean solutionPenalized;
    @ApiModelProperty(value = "Information which helps player resolve the level.")
    private Set<HintDTO> hints = new HashSet<>();
    @ApiModelProperty(value = "How many times player can submit incorrect answer before displaying solution.", example = "5")
    private int incorrectAnswerLimit;
    @ApiModelProperty(value = "Marking if flags/answers are randomly generated and are different for each trainee. Default is false.", example = "false")
    private boolean variantAnswers;
    private List<ReferenceSolutionNodeDTO> referenceSolution;
    @ApiModelProperty(value = "List of mitre techniques used in the training level.")
    private List<MitreTechniqueDTO> mitreTechniques;
    @ApiModelProperty(value = "Set of the expected commands to be executed during the training level.")
    private Set<String> expectedCommands;
    @ApiModelProperty(value = "Indicates if at least one command has to be executed to complete the level. Default is true.", example = "true")
    private boolean commandsRequired;
}
