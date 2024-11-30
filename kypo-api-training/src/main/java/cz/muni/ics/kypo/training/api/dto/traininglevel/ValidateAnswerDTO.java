package cz.muni.ics.kypo.training.api.dto.traininglevel;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;

public class ValidateAnswerDTO {
    @ApiModelProperty(value = "Answer to be validated.", required = true, example = "answer")
    @NotEmpty(message = "{answerToValidate.answer.NotEmpty.message}")
    private String answer;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
