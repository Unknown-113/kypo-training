package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.enums.CheatingDetectionState;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * Encapsulates information about cheating detection.
 */
@ApiModel(value = "CheatingDetectionDTO", description = "Basic information about cheating detection.")
public class CheatingDetectionDTO {

    @ApiModelProperty(value = "id of a training instance in which the event was detected.", example = "1")
    private Long trainingInstanceId;
    @ApiModelProperty(value = "Name of user who executed the detection.", example = "John Doe")
    private String executedBy;
    @ApiModelProperty(value = "Time when the cheating detection was executed.", example = "1.1.2022 5:55:23")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime executeTime;
    @ApiModelProperty(value = "Proximity threshold for time proximity cheat.", example = "120")
    private Long proximityThreshold;
    @ApiModelProperty(value = "id of cheating detection.", example = "1")
    private Long id;
    @ApiModelProperty(value = "State of the detection.", example = "RUNNING")
    private CheatingDetectionState currentState;
    @ApiModelProperty(value = "Number of detected events in detection.", example = "20")
    private Long results;
    @ApiModelProperty(value = "state of detection run of answer similarity.", example = "RUNNING")
    private CheatingDetectionState answerSimilarityState;
    @ApiModelProperty(value = "state of detection run of location_similarity.", example = "RUNNING")
    private CheatingDetectionState locationSimilarityState;
    @ApiModelProperty(value = "state of detection run of time proximity.", example = "RUNNING")
    private CheatingDetectionState timeProximityState;
    @ApiModelProperty(value = "state of detection run of minimal solve time.", example = "RUNNING")
    private CheatingDetectionState minimalSolveTimeState;
    @ApiModelProperty(value = "state of detection run of forbidden commands.", example = "RUNNING")
    private CheatingDetectionState forbiddenCommandsState;
    @ApiModelProperty(value = "state of detection run of no commands.", example = "RUNNING")
    private CheatingDetectionState noCommandsState;

    public Long getTrainingInstanceId() {
        return trainingInstanceId;
    }

    public void setTrainingInstanceId(Long trainingInstanceId) {
        this.trainingInstanceId = trainingInstanceId;
    }

    public String getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(String executedBy) {
        this.executedBy = executedBy;
    }

    public LocalDateTime getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(LocalDateTime executeTime) {
        this.executeTime = executeTime;
    }

    public Long getProximityThreshold() {
        return proximityThreshold;
    }

    public void setProximityThreshold(Long proximityThreshold) {
        this.proximityThreshold = proximityThreshold;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CheatingDetectionState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(CheatingDetectionState currentState) {
        this.currentState = currentState;
    }

    public Long getResults() {
        return results;
    }

    public void setResults(Long results) {
        this.results = results;
    }

    public CheatingDetectionState getAnswerSimilarityState() {
        return answerSimilarityState;
    }

    public void setAnswerSimilarityState(CheatingDetectionState answerSimilarityState) {
        this.answerSimilarityState = answerSimilarityState;
    }

    public CheatingDetectionState getLocationSimilarityState() {
        return locationSimilarityState;
    }

    public void setLocationSimilarityState(CheatingDetectionState locationSimilarityState) {
        this.locationSimilarityState = locationSimilarityState;
    }

    public CheatingDetectionState getTimeProximityState() {
        return timeProximityState;
    }

    public void setTimeProximityState(CheatingDetectionState timeProximityState) {
        this.timeProximityState = timeProximityState;
    }

    public CheatingDetectionState getMinimalSolveTimeState() {
        return minimalSolveTimeState;
    }

    public void setMinimalSolveTimeState(CheatingDetectionState minimalSolveTimeState) {
        this.minimalSolveTimeState = minimalSolveTimeState;
    }

    public CheatingDetectionState getForbiddenCommandsState() {
        return forbiddenCommandsState;
    }

    public void setForbiddenCommandsState(CheatingDetectionState forbiddenCommandsState) {
        this.forbiddenCommandsState = forbiddenCommandsState;
    }

    public CheatingDetectionState getNoCommandsState() {
        return noCommandsState;
    }

    public void setNoCommandsState(CheatingDetectionState noCommandsState) {
        this.noCommandsState = noCommandsState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheatingDetectionDTO that = (CheatingDetectionDTO) o;
        return Objects.equals(trainingInstanceId, that.trainingInstanceId) &&
                Objects.equals(executedBy, that.executedBy) &&
                Objects.equals(executeTime, that.executeTime) &&
                Objects.equals(proximityThreshold, that.proximityThreshold) &&
                Objects.equals(id, that.id) &&
                currentState == that.currentState &&
                Objects.equals(results, that.results) &&
                answerSimilarityState == that.answerSimilarityState &&
                locationSimilarityState == that.locationSimilarityState &&
                timeProximityState == that.timeProximityState &&
                minimalSolveTimeState == that.minimalSolveTimeState &&
                forbiddenCommandsState == that.forbiddenCommandsState &&
                noCommandsState == that.noCommandsState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainingInstanceId, executedBy, executeTime, proximityThreshold, id,
                currentState, results, answerSimilarityState, locationSimilarityState, timeProximityState,
                minimalSolveTimeState, forbiddenCommandsState, noCommandsState);
    }

    @Override
    public String toString() {
        return "CheatingDetectionDTO{" +
                "trainingInstanceId=" + trainingInstanceId +
                ", executedBy='" + executedBy + '\'' +
                ", executeTime=" + executeTime +
                ", proximityThreshold=" + proximityThreshold +
                ", id=" + id +
                ", currentState=" + currentState +
                ", results=" + results +
                ", answerSimilarityState=" + answerSimilarityState +
                ", locationSimilarityState=" + locationSimilarityState +
                ", timeProximityState=" + timeProximityState +
                ", minimalSolveTimeState=" + minimalSolveTimeState +
                ", forbiddenCommandsState=" + forbiddenCommandsState +
                ", noCommandsState=" + noCommandsState +
                '}';
    }
}
