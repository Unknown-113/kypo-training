package cz.muni.ics.kypo.training.api.dto.visualization.clustering;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataWithScoreDTO;

public class TrainingResultsPlayerDTO extends PlayerDataWithScoreDTO {

    private Boolean finished;

    public TrainingResultsPlayerDTO(UserRefDTO userRef, Long trainingRunId, long trainingTime, Integer trainingScore, Integer assessmentScore, Boolean finished) {
        super(userRef.getUserRefId(), userRef.getUserRefFullName(), userRef.getPicture(), trainingRunId, trainingTime, trainingScore, assessmentScore);
        this.finished = finished;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "TrainingResultsPlayerDTO{" +
                "id=" + getId() +
                ", trainingRunId=" + getTrainingRunId() +
                ", name='" + getName() + '\'' +
                ", trainingScore=" + getTrainingScore() +
                ", assessmentScore=" + getAssessmentScore() +
                ", trainingTime=" + getTrainingTime() +
                ", finished=" + finished +
                '}';
    }
}
