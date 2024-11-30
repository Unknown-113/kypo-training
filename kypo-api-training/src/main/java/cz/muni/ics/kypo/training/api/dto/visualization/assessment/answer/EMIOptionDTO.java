package cz.muni.ics.kypo.training.api.dto.visualization.assessment.answer;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

@ApiModel(value = "EMIOptionDTO", description = "Holds a data about the specific EMI option.")
@Builder
@Getter
@Setter
public class EMIOptionDTO {

    @ApiModelProperty(value = "Text of the EMI option", example = "ssh")
    private String text;
    @ApiModelProperty(value = "Indicates the correctness of the MCQ option.", example = "true")
    private boolean isCorrect;
    @ApiModelProperty(value = "List of the participants that have chosen this FFQ answer.")
    private List<UserRefDTO> participants;
}
