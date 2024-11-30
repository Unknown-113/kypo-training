package cz.muni.ics.kypo.training.api.dto.traininglevel;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
public class ValidateAnswerDTO {
    @ApiModelProperty(value = "Answer to be validated.", required = true, example = "answer")
    @NotEmpty(message = "{answerToValidate.answer.NotEmpty.message}")
    private String answer;
}
