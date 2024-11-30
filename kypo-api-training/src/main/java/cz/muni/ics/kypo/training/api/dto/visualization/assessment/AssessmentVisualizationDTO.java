package cz.muni.ics.kypo.training.api.dto.visualization.assessment;

import cz.muni.ics.kypo.training.api.dto.visualization.AbstractLevelVisualizationDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.*;

/**
 * Encapsulates information about assessment used for visualization.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "AssessmentVisualizationDTO", description = "Information needed to visualize assessments statistic.")
public class AssessmentVisualizationDTO {

    @ApiModelProperty(value = "Main identifier of level.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Short textual description of the level.", example = "Training Level1")
    private String title;
    @ApiModelProperty(value = "Order of level among levels in training definition.", example = "1")
    private int order;
    @ApiModelProperty(value = "Type of assessment.", example = "TEST")
    private AssessmentType assessmentType;
    @ApiModelProperty(value = "List of questions in this assessment as JSON.", example = "What is my mothers name?")
    private List<QuestionVisualizationDTO> questions = new ArrayList<>();

    public void addQuestion(QuestionVisualizationDTO question) {
        this.questions.add(question);
    }
}
