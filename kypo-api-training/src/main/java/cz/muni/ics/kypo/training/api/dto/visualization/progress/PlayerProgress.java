package cz.muni.ics.kypo.training.api.dto.visualization.progress;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@ApiModel(value = "PlayerProgress", description = "Progress of player in Training Run.")
@JsonRootName(value = "player_progress")
public class PlayerProgress {

    @ApiModelProperty(value = "User Ref ID.", required = true)
    @JsonProperty(value = "user_ref_id", required = true)
    private long userRefId;
    @ApiModelProperty(value = "Training Run ID.", required = true)
    @JsonProperty(value = "training_run_id", required = true)
    private long trainingRunId;
    @ApiModelProperty(value = "Levels ID.", required = true)
    @JsonProperty(value = "levels", required = true)
    private List<LevelProgress> levels;

    public void addLevelProgress(LevelProgress levelProgress) {
        if (levels == null) {
            levels = new ArrayList<>();
        }
        levels.add(levelProgress);
    }
}
