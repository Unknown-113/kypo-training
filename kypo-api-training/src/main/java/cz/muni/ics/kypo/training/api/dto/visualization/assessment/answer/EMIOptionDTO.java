package cz.muni.ics.kypo.training.api.dto.visualization.assessment.answer;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;

import java.util.List;

@ApiModel(value = "EMIOptionDTO", description = "Holds a data about the specific EMI option.")
@Builder
public class EMIOptionDTO {

    @ApiModelProperty(value = "Text of the EMI option", example = "ssh")
    private String text;
    @ApiModelProperty(value = "Indicates the correctness of the MCQ option.", example = "true")
    private boolean isCorrect;
    @ApiModelProperty(value = "List of the participants that have chosen this FFQ answer.")
    private List<UserRefDTO> participants;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public List<UserRefDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<UserRefDTO> participants) {
        this.participants = participants;
    }
}
