package cz.muni.ics.kypo.training.api.dto.visualization.progress;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "VisualizationProgressDTO", description = "A model includes necessary information about training instance which is needed for visualizations.")
public class VisualizationProgressDTO {

    @ApiModelProperty(value = "Start time of the training run.", example = "1")
    private long startTime;
    @ApiModelProperty(value = "Estimated time it takes to finish runs created from this definition.")
    private long estimatedEndTime;
    @ApiModelProperty(value = "A current time of the training.")
    private long currentTime;
    @ApiModelProperty(value = "Information about all players in training instance.")
    private List<UserRefDTO> players;
    @ApiModelProperty(value = "Information about all levels in training definition.")
    @JsonIgnoreProperties({"snapshot_hook", "training_definition", "hints.hint_penalty", "incorrect_answer_limit"})
    private List<LevelDefinitionProgressDTO> levels;
    @ApiModelProperty(value = "Information about all levels in training definition.")
    private List<PlayerProgress> playerProgress;
}
