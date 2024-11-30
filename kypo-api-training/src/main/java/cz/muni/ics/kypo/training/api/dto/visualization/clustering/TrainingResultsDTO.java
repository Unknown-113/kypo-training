package cz.muni.ics.kypo.training.api.dto.visualization.clustering;

import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataDTO;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
public class TrainingResultsDTO {

    private long estimatedTime;
    private int maxAchievableScore;
    private int maxParticipantScore;
    private int maxParticipantTrainingScore;
    private int maxParticipantAssessmentScore;
    private long maxParticipantTime;
    private float averageTime;
    private float averageScore;
    private float averageTrainingScore;
    private float averageAssessmentScore;
    private List<PlayerDataDTO> playerData = new ArrayList<>();

    public void addPlayerData(PlayerDataDTO playerDataDTO) {
        this.playerData.add(playerDataDTO);
    }
}
