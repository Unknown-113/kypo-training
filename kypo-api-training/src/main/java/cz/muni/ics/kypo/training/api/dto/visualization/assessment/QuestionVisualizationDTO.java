package cz.muni.ics.kypo.training.api.dto.visualization.assessment;

import com.google.common.base.Objects;
import cz.muni.ics.kypo.training.api.dto.visualization.assessment.answer.AbstractAnswerDTO;
import cz.muni.ics.kypo.training.api.enums.QuestionType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates information about question of the specific assessment. Data used for visualizations.
 */
@ApiModel(value = "QuestionVisualizationDTO", description = "Information needed to visualize assessments statistic.")
public class QuestionVisualizationDTO {

    @ApiModelProperty(value = "Main identifier of the question.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Type of the question.", required = true, example = "FFQ")
    private QuestionType questionType;
    @ApiModelProperty(value = "The content of the question.", example = "What transport protocol is used for reliable transmission?")
    private String text = "Example Question";
    @ApiModelProperty(value = "Order of the question, starts with 0", example = "0")
    private int order;
    @ApiModelProperty(value = "Free form question answers to questions submitted by participants.")
    private List<? extends AbstractAnswerDTO> answers = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<? extends AbstractAnswerDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<? extends AbstractAnswerDTO> answers) {
        this.answers = answers;
    }

    @Override
    public String toString() {
        return "QuestionVisualizationDTO{" +
                "id=" + id +
                ", questionType=" + questionType +
                ", text='" + text + '\'' +
                ", order=" + order +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestionVisualizationDTO)) return false;
        QuestionVisualizationDTO that = (QuestionVisualizationDTO) o;
        return order == that.order && Objects.equal(id, that.id) && questionType == that.questionType && Objects.equal(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, questionType, text, order);
    }
}
