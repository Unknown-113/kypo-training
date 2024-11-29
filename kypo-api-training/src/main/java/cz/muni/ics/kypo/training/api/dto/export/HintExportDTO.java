package cz.muni.ics.kypo.training.api.dto.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 *  Encapsulates information about Hint.
 *
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@ApiModel(value = "HintExportDTO", description = "An exported brief textual description to aid the participant.")
public class HintExportDTO {

    @ApiModelProperty(value = "Short textual description of the hint.", example = "Hint1")
    private String title;
    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Very good advice")
    private String content;
    @ApiModelProperty(value = "The number of points the participant loses after receiving the hint.", example = "10")
    private Integer hintPenalty;
    @ApiModelProperty(value = "The order of hint in training level", example = "1")
    private int order;
}
