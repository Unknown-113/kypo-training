package cz.muni.ics.kypo.training.api.dto.assessmentlevel.preview;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.ExtendedMatchingOptionDTO;
import cz.muni.ics.kypo.training.api.enums.QuestionType;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.*;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QuestionPreviewDTO {

    @ApiModelProperty(value = "Main identifier of the question.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Type of the question.", required = true, example = "FFQ")
    private QuestionType questionType = QuestionType.FFQ;
    @ApiModelProperty(value = "The content of the question.", example = "What transport protocol is used for reliable transmission?")
    private String text = "Example Question";
    @ApiModelProperty(value = "Order of the question, starts with 0", example = "0")
    private int order;
    @ApiModelProperty(value = "Sign if the question must be answered by the participant or not.", example = "true")
    private boolean answerRequired;
    @ApiModelProperty(value = "Choices displayed to the participant in case of FFQ or MCQ.")
    private List<QuestionChoicePreviewDTO> choices = new ArrayList<>();
    @ApiModelProperty(value = "Options displayed to the participant in case of EMI.")
    private List<ExtendedMatchingOptionDTO> extendedMatchingOptions = new ArrayList<>();
    @ApiModelProperty(value = "Statements displayed to the participant in case of EMI.")
    private List<ExtendedMatchingStatementPreviewDTO> extendedMatchingStatements = new ArrayList<>();
    @ApiModelProperty(value = "User answers to the question", example = "[\"An answer\"]")
    private Set<String> userAnswers;

    public void setChoices(List<QuestionChoicePreviewDTO> choices) {
        this.choices = choices;
        this.choices.sort(Comparator.comparingInt(QuestionChoicePreviewDTO::getOrder));

    }

    public void setExtendedMatchingOptions(List<ExtendedMatchingOptionDTO> extendedMatchingOptions) {
        this.extendedMatchingOptions = extendedMatchingOptions;
        this.extendedMatchingOptions.sort(Comparator.comparingInt(ExtendedMatchingOptionDTO::getOrder));
    }

    public void setExtendedMatchingStatements(List<ExtendedMatchingStatementPreviewDTO> extendedMatchingStatements) {
        this.extendedMatchingStatements = extendedMatchingStatements;
        this.extendedMatchingStatements.sort(Comparator.comparingInt(ExtendedMatchingStatementPreviewDTO::getOrder));
    }
}
