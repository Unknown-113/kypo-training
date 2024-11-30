package cz.muni.ics.kypo.training.api.dto.visualization.commons;

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

    public Integer getTrainingScore() {
        return trainingScore;
    }

    public void setTrainingScore(Integer trainingScore) {
        this.trainingScore = trainingScore;
    }

    public Integer getAssessmentScore() {
        return assessmentScore;
    }

    public void setAssessmentScore(Integer assessmentScore) {
        this.assessmentScore = assessmentScore;
    }

    @Override
    public String toString() {
        return "PlayerDataWithScoreDTO{" +
                "id=" + getId() +
                ", trainingRunId=" + getTrainingRunId() +
                ", name='" + getName() + '\'' +
                ", trainingTime=" + getTrainingTime() +
                ", trainingScore=" + trainingScore +
                ", assessmentScore=" + assessmentScore +
                '}';
    }
}
