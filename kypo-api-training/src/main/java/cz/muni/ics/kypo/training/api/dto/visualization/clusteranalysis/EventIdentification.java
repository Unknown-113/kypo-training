package cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis;

public class EventIdentification {

    private Long trainingDefinitionId;
    private Long trainingInstanceId;
    private Long trainingRunId;

    public EventIdentification() {
    }

    public EventIdentification(Long trainingInstanceId, Long trainingDefinitionId, Long trainingRunId) {
        this.trainingInstanceId = trainingInstanceId;
        this.trainingDefinitionId = trainingDefinitionId;
        this.trainingRunId = trainingRunId;
    }

    public Long getTrainingInstanceId() {
        return trainingInstanceId;
    }

    public void setTrainingInstanceId(Long trainingInstanceId) {
        this.trainingInstanceId = trainingInstanceId;
    }

    public Long getTrainingDefinitionId() {
        return trainingDefinitionId;
    }

    public void setTrainingDefinitionId(Long trainingDefinitionId) {
        this.trainingDefinitionId = trainingDefinitionId;
    }

    public Long getTrainingRunId() {
        return trainingRunId;
    }

    public void setTrainingRunId(Long trainingRunId) {
        this.trainingRunId = trainingRunId;
    }
}
