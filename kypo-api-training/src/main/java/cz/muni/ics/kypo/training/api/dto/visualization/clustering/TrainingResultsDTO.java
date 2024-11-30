package cz.muni.ics.kypo.training.api.dto.visualization.clustering;

import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public long getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(long estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public int getMaxAchievableScore() {
        return maxAchievableScore;
    }

    public void setMaxAchievableScore(int maxAchievableScore) {
        this.maxAchievableScore = maxAchievableScore;
    }

    public int getMaxParticipantScore() {
        return maxParticipantScore;
    }

    public void setMaxParticipantScore(int maxParticipantScore) {
        this.maxParticipantScore = maxParticipantScore;
    }

    public int getMaxParticipantTrainingScore() {
        return maxParticipantTrainingScore;
    }

    public void setMaxParticipantTrainingScore(int maxParticipantTrainingScore) {
        this.maxParticipantTrainingScore = maxParticipantTrainingScore;
    }

    public int getMaxParticipantAssessmentScore() {
        return maxParticipantAssessmentScore;
    }

    public void setMaxParticipantAssessmentScore(int maxParticipantAssessmentScore) {
        this.maxParticipantAssessmentScore = maxParticipantAssessmentScore;
    }

    public long getMaxParticipantTime() {
        return maxParticipantTime;
    }

    public void setMaxParticipantTime(long maxParticipantTime) {
        this.maxParticipantTime = maxParticipantTime;
    }

    public float getAverageTime() {
        return averageTime;
    }

    public void setAverageTime(float averageTime) {
        this.averageTime = averageTime;
    }

    public float getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(float averageScore) {
        this.averageScore = averageScore;
    }

    public float getAverageTrainingScore() {
        return averageTrainingScore;
    }

    public void setAverageTrainingScore(float averageTrainingScore) {
        this.averageTrainingScore = averageTrainingScore;
    }

    public float getAverageAssessmentScore() {
        return averageAssessmentScore;
    }

    public void setAverageAssessmentScore(float averageAssessmentScore) {
        this.averageAssessmentScore = averageAssessmentScore;
    }

    public List<PlayerDataDTO> getPlayerData() {
        return playerData;
    }

    public void addPlayerData(PlayerDataDTO playerDataDTO) {
        this.playerData.add(playerDataDTO);
    }

    public void setPlayerData(List<PlayerDataDTO> playerData) {
        this.playerData = playerData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainingResultsDTO that = (TrainingResultsDTO) o;
        return getEstimatedTime() == that.getEstimatedTime() &&
                getMaxParticipantScore() == that.getMaxParticipantScore() &&
                getMaxParticipantTrainingScore() == that.getMaxParticipantTrainingScore() &&
                getMaxParticipantAssessmentScore() == that.getMaxParticipantAssessmentScore() &&
                getMaxParticipantTime() == that.getMaxParticipantTime() &&
                getAverageTime() == that.getAverageTime() &&
                getAverageTrainingScore() == that.getAverageTrainingScore() &&
                getAverageAssessmentScore() == that.getAverageAssessmentScore();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEstimatedTime(), getMaxParticipantScore(), getMaxParticipantTrainingScore(), getMaxParticipantAssessmentScore(), getMaxParticipantTime(),
                getAverageTime(), getAverageTrainingScore(), getAverageAssessmentScore());
    }


    @Override
    public String toString() {
        return "TrainingResultsDTO{" +
                "estimatedTime=" + estimatedTime +
                ", maxParticipantScore=" + maxParticipantScore +
                ", maxParticipantTrainingScore=" + maxParticipantTrainingScore +
                ", maxParticipantAssessmentScore=" + maxParticipantAssessmentScore +
                ", maxParticipantTime=" + maxParticipantTime +
                ", averageTime=" + averageTime +
                ", averageScore=" + averageScore +
                ", averageTrainingScore=" + averageTrainingScore +
                ", averageAssessmentScore=" + averageAssessmentScore +
                '}';
    }
}
