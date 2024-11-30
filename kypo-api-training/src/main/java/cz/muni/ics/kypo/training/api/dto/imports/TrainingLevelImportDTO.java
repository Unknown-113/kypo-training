package cz.muni.ics.kypo.training.api.dto.imports;

import com.fasterxml.jackson.annotation.JsonAlias;
import cz.muni.ics.kypo.training.api.dto.export.AbstractLevelExportDTO;
import cz.muni.ics.kypo.training.api.dto.technique.MitreTechniqueDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.ReferenceSolutionNodeDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.*;

/**
 * Encapsulates information about training level. Inherits from {@link AbstractLevelImportDTO}
 *
 */
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingLevelImportDTO", description = "Imported training level.", parent = AbstractLevelExportDTO.class)
public class TrainingLevelImportDTO extends AbstractLevelImportDTO{

	@ApiModelProperty(value = "Keyword found in training, used for access next level.", example = "secretAnswer")
	@Size(max = 50, message = "{trainingLevel.answer.Size.message}")
	@JsonAlias({"flag"})
	private String answer;
	@ApiModelProperty(value = "Identifier that is used to obtain answer from remote storage.", example = "username")
	private String answerVariableName;
	@ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Play me")
	@NotEmpty(message = "{trainingLevel.content.NotEmpty.message}")
	private String content;
	@ApiModelProperty(value = "Instruction how to get answer in training.", example = "This is how you do it")
	@NotEmpty(message = "{trainingLevel.solution.NotEmpty.message}")
	private String solution;
	@ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
	@NotNull(message = "{trainingLevel.solutionPenalized.NotNull.message}")
	private boolean solutionPenalized;
	@Valid
	@ApiModelProperty(value = "Information which helps player resolve the level.")
	private Set<HintImportDTO> hints = new HashSet<>();
	@ApiModelProperty(value = "How many times player can submit incorrect answer before displaying solution.", example = "5")
	@NotNull(message = "{trainingLevel.incorrectAnswerLimit.NotNull.message}")
	@Min(value = 0, message = "{trainingLevel.incorrectAnswerLimit.Min.message}")
	@Max(value = 100, message = "{trainingLevel.incorrectAnswerLimit.Max.message}")
	@JsonAlias({"incorrect_flag_limit"})
	private int incorrectAnswerLimit;
	@Valid
	@ApiModelProperty(value = "List of attachments.", example = "[]")
	private List<AttachmentImportDTO> attachments;
	@ApiModelProperty(value = "The maximum score a participant can achieve during a level.", example = "20")
	@NotNull(message = "{abstractLevel.maxScore.NotNull.message}")
	@Min(value = 0, message = "{abstractLevel.maxScore.Min.message}")
	@Max(value = 100, message = "{abstractLevel.maxScore.Max.message}")
	protected int maxScore;
	@ApiModelProperty(value = "Marking if flags/answers are randomly generated and are different for each trainee. Default is false.", example = "false")
	private boolean variantAnswers;
	@Valid
	@ApiModelProperty(value = "Sequence of commands that leads to the level answer.", example = "false")
	private List<ReferenceSolutionNodeDTO> referenceSolution = new ArrayList<>();
	@Valid
	@ApiModelProperty(value = "List of mitre techniques used in the training level.")
	private List<MitreTechniqueDTO> mitreTechniques;
	@ApiModelProperty(value = "Set of the expected commands to be executed during the training level.")
	private Set<String> expectedCommands;
	@ApiModelProperty(value = "Indicates if at least one command has to be executed to complete the level. Default is true.", example = "true")
	private boolean commandsRequired;
}
