package cz.muni.ics.kypo.training.api.dto.visualization.progress;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.*;

/**
 * Encapsulates information about Hint needed for progress visualization.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "HintProgressDTO", description = "A brief textual description to aid the participant.")
public class HintProgressDTO {

    @JsonProperty("hint_id")
    @ApiModelProperty(value = "Main identifier of hint.", example = "1")
    private Long id;
    @JsonProperty("hint_title")
    @ApiModelProperty(value = "Short textual description of the hint.", example = "Hint1")
    private String title;
    @JsonProperty("hint_content")
    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Very good advice")
    private String content;
}
