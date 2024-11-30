package cz.muni.ics.kypo.training.api.dto.traininglevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.hint.TakenHintDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Set;
import lombok.*;

/**
 * Encapsulates basic information about training level.
 */
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingLevelPreviewDTO", description = "An assignment containing security tasks whose completion yields a answer.", parent = AbstractLevelDTO.class)
public class TrainingLevelPreviewDTO extends AbstractLevelDTO {

    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Play me")
    private String content;
    @ApiModelProperty(value = "Information which helps player resolve the level.")
    private Set<TakenHintDTO> hints = new HashSet<>();
    @ApiModelProperty(value = "Instruction how to get answer in training.", example = "This is how you do it")
    private String solution;
}
