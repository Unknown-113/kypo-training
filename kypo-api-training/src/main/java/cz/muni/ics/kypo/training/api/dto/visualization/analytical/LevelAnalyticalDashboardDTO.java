package cz.muni.ics.kypo.training.api.dto.visualization.analytical;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Set;
import lombok.*;

/**
 * Analytical dashboard - training level details.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(value = "LevelAnalyticalDashboardDTO", description = "Analytical dashboard - training level details.")
public class LevelAnalyticalDashboardDTO {

    @ApiModelProperty(value = "Identifier of the level.", example = "1")
    private Long levelId;
    @ApiModelProperty(value = "Order of the level.", example = "4")
    private Integer levelOrder;
    @ApiModelProperty(value = "The title of the level.", example = "Find open ports")
    private String levelTitle;
    @ApiModelProperty(value = "Level correct answer", example = "Secret answer")
    private String correctAnswer;
    @ApiModelProperty(value = "Overall number of correctly submitted answers. How many participants solved level.", example = "10")
    private int correctAnswersSubmitted;
    @ApiModelProperty(value = "List of all wrong answers submitted by participants.")
    private Set<String> wrongAnswers = new HashSet<>();

    public LevelAnalyticalDashboardDTO(Long levelId, Integer levelOrder, String levelTitle, String correctAnswer, String answerVariableName) {
        this.levelId = levelId;
        this.levelOrder = levelOrder;
        this.levelTitle = levelTitle;
        this.correctAnswer = correctAnswer == null ? answerVariableName : correctAnswer;
    }

    public void addCorrectAnswerSubmit() {
        this.correctAnswersSubmitted++;
    }
}
