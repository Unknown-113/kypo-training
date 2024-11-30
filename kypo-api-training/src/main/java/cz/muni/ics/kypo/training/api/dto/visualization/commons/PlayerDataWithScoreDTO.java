package cz.muni.ics.kypo.training.api.dto.visualization.commons;

import lombok.*;

@Getter
@Setter
@ToString
public class PlayerDataWithScoreDTO extends PlayerDataDTO {

    private Integer trainingScore;
    private Integer assessmentScore;

    public PlayerDataWithScoreDTO(Long id, String name, byte[] picture, Long trainingRunId) {
        super(id, name, picture, trainingRunId);
    }

    public PlayerDataWithScoreDTO(Long id, String name, byte[] picture, Long trainingRunId, Integer trainingScore,
                                  Integer assessmentScore) {
        super(id, name, picture, trainingRunId);
        this.trainingScore = trainingScore;
        this.assessmentScore = assessmentScore;
    }

    public PlayerDataWithScoreDTO(Long id, String name, byte[] picture, Long trainingRunId,
                                  long trainingTime, Integer trainingScore, Integer assessmentScore) {
        super(id, name, picture, trainingRunId, trainingTime);
        this.trainingScore = trainingScore;
        this.assessmentScore = assessmentScore;
    }
}
