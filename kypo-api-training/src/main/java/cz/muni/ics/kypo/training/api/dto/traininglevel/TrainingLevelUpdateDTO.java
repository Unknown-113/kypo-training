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

/**
 * Encapsulates information needed to update training level.
 *
 */
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

    /**
     * Gets answer.
     *
     * @return the answer
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Sets answer.
     *
     * @param answer the answer
     */
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    /**
     * Gets answer identifier.
     *
     * @return the answer identifier
     */
    public String getAnswerVariableName() {
        return answerVariableName;
    }

    /**
     * Sets answer identifier.
     *
     * @param answerVariableName the answer identifier
     */
    public void setAnswerVariableName(String answerVariableName) {
        this.answerVariableName = answerVariableName;
    }

    /**
     * Gets content.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets content.
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets solution.
     *
     * @return the solution
     */
    public String getSolution() {
        return solution;
    }

    /**
     * Sets solution.
     *
     * @param solution the solution
     */
    public void setSolution(String solution) {
        this.solution = solution;
    }

    /**
     * Is solution penalized boolean.
     *
     * @return true if incorrect solution is penalized
     */
    public boolean isSolutionPenalized() {
        return solutionPenalized;
    }

    /**
     * Sets solution penalized.
     *
     * @param solutionPenalized the solution penalized
     */
    public void setSolutionPenalized(boolean solutionPenalized) {
        this.solutionPenalized = solutionPenalized;
    }

    /**
     * Gets estimated duration.
     *
     * @return the estimated duration
     */
    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    /**
     * Sets estimated duration.
     *
     * @param estimatedDuration the estimated duration
     */
    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    /**
     * Gets incorrect answer limit.
     *
     * @return the incorrect answer limit
     */
    public int getIncorrectAnswerLimit() {
        return incorrectAnswerLimit;
    }

    /**
     * Sets incorrect answer limit.
     *
     * @param incorrectAnswerLimit the incorrect answer limit
     */
    public void setIncorrectAnswerLimit(int incorrectAnswerLimit) {
        this.incorrectAnswerLimit = incorrectAnswerLimit;
    }

    /**
     * Gets max score.
     *
     * @return the max score
     */
    public int getMaxScore() {
        return maxScore;
    }

    /**
     * Sets max score.
     *
     * @param maxScore the max score
     */
    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    /**
     * Gets hints.
     *
     * @return the set of {@link HintDTO}s
     */
    public Set<HintDTO> getHints() {
        return hints;
    }

    /**
     * Sets hints.
     *
     * @param hints the set of {@link HintDTO}s
     */
    public void setHints(Set<HintDTO> hints) {
        this.hints = hints;
    }

    /**
     * Is variant answers boolean.
     *
     * @return the boolean
     */
    public boolean isVariantAnswers() {
        return variantAnswers;
    }

    /**
     * Sets variant answers.
     *
     * @param variantAnswers the variant answers
     */
    public void setVariantAnswers(boolean variantAnswers) {
        this.variantAnswers = variantAnswers;
    }


    /**
     * Gets reference solution fo the training level.
     *
     * @return the list of {@link ReferenceSolutionNodeDTO}s
     */
    public List<ReferenceSolutionNodeDTO> getReferenceSolution() {
        return referenceSolution;
    }


    /**
     * Sets reference solution of the training level.
     *
     * @param referenceSolution list of the reference solution node
     */
    public void setReferenceSolution(List<ReferenceSolutionNodeDTO> referenceSolution) {
        this.referenceSolution = referenceSolution;
    }

    /**
     * Gets set of MITRE techniques used in the training level
     *
     * @return set of MITRE techniques
     */
    public List<MitreTechniqueDTO> getMitreTechniques() {
        return mitreTechniques;
    }

    /**
     * Sets set of MITRE techniques used in the training level
     *
     * @param mitreTechniques set of MITRE techniques
     */
    public void setMitreTechniques(List<MitreTechniqueDTO> mitreTechniques) {
        this.mitreTechniques = mitreTechniques;
    }

    /**
     * Gets set of expected commands executed in the training level
     *
     * @return set of expected commands
     */
    public Set<String> getExpectedCommands() {
        return expectedCommands;
    }

    /**
     * Sets set of expected commands executed in the training level
     *
     * @param expectedCommands set of expected commands
     */
    public void setExpectedCommands(Set<String> expectedCommands) {
        this.expectedCommands = expectedCommands;
    }

    /**
     * Gets minimal possible solve time.
     *
     * @return the minimal possible solve time
     */
    public Integer getMinimalPossibleSolveTime() {
        return minimalPossibleSolveTime;
    }

    /**
     * Sets minimal possible solve time.
     *
     * @param minimalPossibleSolveTime the minimal possible solve time
     */
    public void setMinimalPossibleSolveTime(Integer minimalPossibleSolveTime) {
        this.minimalPossibleSolveTime = minimalPossibleSolveTime;
    }

    /**
     * Gets boolean if at least one command has to be executed to complete the training level
     *
     * @return true if commands are required, false otherwise
     */
    public boolean isCommandsRequired() {
        return commandsRequired;
    }

    /**
     * Sets a boolean if at least one command has to be executed to complete the training level
     *
     * @param commandsRequired boolean value
     */
    public void setCommandsRequired(boolean commandsRequired) {
        this.commandsRequired = commandsRequired;
    }

    @Override
    public String toString() {
        return "TrainingLevelUpdateDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", maxScore=" + maxScore +
                ", answer='" + answer + '\'' +
                ", answerVariableName='" + answerVariableName + '\'' +
                ", content='" + content + '\'' +
                ", solution='" + solution + '\'' +
                ", solutionPenalized=" + solutionPenalized +
                ", estimatedDuration=" + estimatedDuration +
                ", incorrectAnswerLimit=" + incorrectAnswerLimit +
                ", hints=" + hints +
                ", variantAnswers=" + variantAnswers +
                ", minimalPossibleSolveTime=" + minimalPossibleSolveTime +
                ", commandsRequired=" + commandsRequired +
                '}';
    }
}
