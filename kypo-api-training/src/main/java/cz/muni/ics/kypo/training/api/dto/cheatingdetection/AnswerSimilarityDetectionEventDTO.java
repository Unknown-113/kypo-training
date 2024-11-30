package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(value = "AnswerSimilarityDetectionEventDTO", description = "A detection event of type Answer Similarity.", parent = AbstractDetectionEventDTO.class)
public class AnswerSimilarityDetectionEventDTO extends AbstractDetectionEventDTO {
    @ApiModelProperty(value = "Correct answer to the level.", example = "pass")
    private String answer;
    @ApiModelProperty(value = "Name of a player who was assigned the correct answer.", example = "John Doe")
    private String answerOwner;
}
