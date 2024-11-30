package cz.muni.ics.kypo.training.api.dto.visualization.clustering;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataDTO;

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

    public int getParticipantLevelScore() {
        return participantLevelScore;
    }

    public void setParticipantLevelScore(int participantLevelScore) {
        this.participantLevelScore = participantLevelScore;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "ClusteringPlayerDTO{" +
                "id=" + getId() +
                ", trainingRunId=" + getTrainingRunId() +
                ", name='" + getName() + '\'' +
                ", trainingTime=" + getTrainingTime() +
                ", participantLevelScore=" + participantLevelScore +
                ", finished=" + finished +
                '}';
    }
}
