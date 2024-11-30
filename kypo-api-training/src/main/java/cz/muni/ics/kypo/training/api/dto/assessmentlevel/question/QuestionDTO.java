package cz.muni.ics.kypo.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.muni.ics.kypo.training.api.enums.QuestionType;
import cz.muni.ics.kypo.training.validation.ValidOrder;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QuestionDTO {

    @ApiModelProperty(value = "Main identifier of the question.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Type of the question.", required = true, example = "FFQ")
    @NotNull(message = "{question.questionType.NotNull.message}")
    private QuestionType questionType = QuestionType.FFQ;
    @ApiModelProperty(value = "The content of the question.", example = "What transport protocol is used for reliable transmission?")
    @NotEmpty(message = "{question.text.NotEmpty.message}")
    private String text = "Example Question";
    @ApiModelProperty(value = "Number of points the participant will receive for the correct answering of the question.", example = "10")
    @Min(value = 0, message = "{question.points.Min.message}")
    private int points;
    @ApiModelProperty(value = "Number of points the participant will lose for the incorrect answering of the question.", example = "6")
    @Min(value = 0, message = "{question.penalty.Min.message}")
    private int penalty;
    @ApiModelProperty(value = "Order of the question, starts with 0", example = "0")
    @Min(value = 0, message = "{question.order.Min.message}")
    private int order;
    @ApiModelProperty(value = "Sign if the question must be answered by the participant or not.", example = "true")
    @NotNull(message = "{question.answerRequired.NotNull.message}")
    private boolean answerRequired;
    @ApiModelProperty(value = "Choices displayed to the participant in case of FFQ or MCQ.")
    @Valid
    @ValidOrder
    private List<QuestionChoiceDTO> choices = new ArrayList<>();
    @ApiModelProperty(value = "Options displayed to the participant in case of EMI.")
    @Valid
    @ValidOrder
    private List<ExtendedMatchingOptionDTO> extendedMatchingOptions = new ArrayList<>();
    @ApiModelProperty(value = "Statements displayed to the participant in case of EMI.")
    @Valid
    @ValidOrder
    private List<ExtendedMatchingStatementDTO> extendedMatchingStatements = new ArrayList<>();

    public void setChoices(List<QuestionChoiceDTO> choices) {
        this.choices = choices;
        this.choices.sort(Comparator.comparingInt(QuestionChoiceDTO::getOrder));

    }

    public void setExtendedMatchingOptions(List<ExtendedMatchingOptionDTO> extendedMatchingOptions) {
        this.extendedMatchingOptions = extendedMatchingOptions;
        this.extendedMatchingOptions.sort(Comparator.comparingInt(ExtendedMatchingOptionDTO::getOrder));
    }

    public void setExtendedMatchingStatements(List<ExtendedMatchingStatementDTO> extendedMatchingStatements) {
        this.extendedMatchingStatements = extendedMatchingStatements;
        this.extendedMatchingStatements.sort(Comparator.comparingInt(ExtendedMatchingStatementDTO::getOrder));
    }
}
