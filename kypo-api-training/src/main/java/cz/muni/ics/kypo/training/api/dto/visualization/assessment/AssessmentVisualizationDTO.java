package cz.muni.ics.kypo.training.api.dto.visualization.assessment;

import cz.muni.ics.kypo.training.api.dto.visualization.AbstractLevelVisualizationDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulates information about assessment used for visualization.
 */
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(AssessmentType assessmentType) {
        this.assessmentType = assessmentType;
    }

    public List<QuestionVisualizationDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionVisualizationDTO> questions) {
        this.questions = questions;
    }

    public void addQuestion(QuestionVisualizationDTO question) {
        this.questions.add(question);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AbstractLevelVisualizationDTO)) return false;
        if (!super.equals(object)) return false;
        AssessmentVisualizationDTO that = (AssessmentVisualizationDTO) object;
        return getAssessmentType() == that.getAssessmentType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getAssessmentType());
    }

    @Override
    public String toString() {
        return "AssessmentVisualizationDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", order=" + order +
                '}';
    }
}
