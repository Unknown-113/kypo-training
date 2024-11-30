package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.*;

/**
 * Response to attempt of answer input
 *
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "IsCorrectAnswerDTO", description = "A response for the request about the validation of the answer. May also " +
        "include solution if remaining attempts reach 0.")
public class IsCorrectAnswerDTO {

    @ApiModelProperty(value = "True/false if answer has been correct/incorrect.", example = "false")
    private boolean isCorrect;
    @ApiModelProperty(value = "Number of attempts to submit a bad answer.", example = "3")
    private int remainingAttempts;
    @ApiModelProperty(value = "Instruction how to get answer in training.", example = "This is how you do it")
    private String solution;
}
