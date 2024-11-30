package cz.muni.ics.kypo.training.api.dto.export;

import cz.muni.ics.kypo.training.api.dto.imports.AttachmentImportDTO;
import cz.muni.ics.kypo.training.api.dto.technique.MitreTechniqueDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.ReferenceSolutionNodeDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.*;

/**
 * Encapsulates information about training level. Inherits from {@link AbstractLevelExportDTO}
 *
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(value = "TrainingLevelExportDTO", description = "Exported training level.", parent = AbstractLevelExportDTO.class)
public class TrainingLevelExportDTO extends AbstractLevelExportDTO {

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
    private Set<HintExportDTO> hints = new HashSet<>();
    @ApiModelProperty(value = "How many times player can submit incorrect answer before displaying solution.", example = "5")
    private int incorrectAnswerLimit;
    @ApiModelProperty(value = "List of attachments.", example = "[]")
    private List<AttachmentImportDTO> attachments;
    @ApiModelProperty(value = "The maximum score a participant can achieve during a level.", example = "20")
    private int maxScore;
    @ApiModelProperty(value = "Indicates if flags/answers are randomly generated and are different for each trainee. Default is false.", example = "false")
    private boolean variantAnswers;
    @ApiModelProperty(value = "Sequence of commands that leads to the level answer.", example = "false")
    private List<ReferenceSolutionNodeDTO> referenceSolution = new ArrayList<>();
    @ApiModelProperty(value = "Set of mitre techniques used in the training level.")
    private Set<MitreTechniqueDTO> mitreTechniques;
    @ApiModelProperty(value = "Set of the expected commands to be executed during the training level.")
    private Set<String> expectedCommands;
    @ApiModelProperty(value = "Indicates if at least one command has to be executed to complete the level. Default is true.", example = "true")
    private boolean commandsRequired;
}
