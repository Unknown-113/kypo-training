package cz.muni.ics.kypo.training.api.dto.visualization.clustering;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataDTO;
import lombok.*;

@Getter
@Setter
@ToString
public class ClusteringLevelPlayerDTO extends PlayerDataDTO {

    private int participantLevelScore;
    private Boolean finished;

    public ClusteringLevelPlayerDTO(Long id, Long trainingRunId, String name, byte[] picture,
                                    long trainingTime, int participantLevelScore, Boolean finished) {
        super(id, name, picture, trainingRunId, trainingTime);
        this.participantLevelScore = participantLevelScore;
        this.finished = finished;
    }

    public ClusteringLevelPlayerDTO(UserRefDTO userRef, Long trainingRunId,  long trainingTime,
                                    int participantLevelScore, Boolean finished) {
        super(userRef.getUserRefId(), userRef.getUserRefFullName(), userRef.getPicture(), trainingRunId, trainingTime);
        this.participantLevelScore = participantLevelScore;
        this.finished = finished;
    }
}
