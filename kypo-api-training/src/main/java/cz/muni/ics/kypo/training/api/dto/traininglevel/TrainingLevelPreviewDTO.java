package cz.muni.ics.kypo.training.api.dto.traininglevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.hint.TakenHintDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Set;


/**
 * Encapsulates basic information about training level.
 */

@ApiModel(value = "TrainingLevelPreviewDTO", description = "An assignment containing security tasks whose completion yields a answer.", parent = AbstractLevelDTO.class)
public class TrainingLevelPreviewDTO extends AbstractLevelDTO {

    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Play me")
    private String content;
    @ApiModelProperty(value = "Information which helps player resolve the level.")
    private Set<TakenHintDTO> hints = new HashSet<>();
    @ApiModelProperty(value = "Instruction how to get answer in training.", example = "This is how you do it")
    private String solution;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Set<TakenHintDTO> getHints() {
        return hints;
    }

    public void setHints(Set<TakenHintDTO> hints) {
        this.hints = hints;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    @Override
    public String toString() {
        return "TrainingLevelPreviewDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", levelType=" + levelType +
                ", order=" + order +
                ", content='" + content + '\'' +
                ", solution='" + solution + '\'' +
                '}';
    }
}
