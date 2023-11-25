package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.persistence.model.AbstractEntity;
import cz.muni.ics.kypo.training.persistence.model.enums.CheatingDetectionState;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Class representing cheating detection.
 * Cheating detections are executed by organizers.
 * Cheating detections are bound to a training instance.
 */
@Entity
@Table(name = "cheating_detection")
@NamedQueries({
        @NamedQuery(
                name = "CheatingDetection.findAllByTrainingInstanceId",
                query = "SELECT cd FROM CheatingDetection cd " +
                        "WHERE cd.trainingInstanceId = :trainingInstanceId"
        ),
        @NamedQuery(
                name = "CheatingDetection.findCheatingDetectionById",
                query = "SELECT cd FROM CheatingDetection cd " +
                        "WHERE cd.id = :cheatingDetectionId"
        ),
        @NamedQuery(
                name = "CheatingDetection.deleteCheatingDetectionById",
                query = "DELETE FROM CheatingDetection cd WHERE cd.id = :cheatingDetectionId"
        ),
        @NamedQuery(
                name = "CheatingDetection.deleteAllCheatingDetectionsOfTrainingInstance",
                query = "DELETE FROM CheatingDetection cd WHERE cd.trainingInstanceId = :trainingInstanceId"
        )
})
public class CheatingDetection extends AbstractEntity<Long> {

    @Column(name = "training_instance_id", nullable = false)
    private Long trainingInstanceId;
    @Column(name = "executed_by")
    private String executedBy;
    @Column(name = "execute_time", nullable = false)
    private LocalDateTime executeTime;
    @Column(name = "proximity_threshold", nullable = true)
    private Long proximityThreshold;
    @Enumerated(EnumType.STRING)
    @Column(name = "current_state", nullable = false)
    private CheatingDetectionState currentState;
    @Column(name = "results")
    private Long results;
    @Enumerated(EnumType.STRING)
    @Column(name = "answer_similarity_state")
    private CheatingDetectionState answerSimilarityState;
    @Enumerated(EnumType.STRING)
    @Column(name = "location_similarity_state")
    private CheatingDetectionState locationSimilarityState;
    @Enumerated(EnumType.STRING)
    @Column(name = "time_proximity_state")
    private CheatingDetectionState timeProximityState;
    @Enumerated(EnumType.STRING)
    @Column(name = "minimal_solve_time_state")
    private CheatingDetectionState minimalSolveTimeState;
    @Enumerated(EnumType.STRING)
    @Column(name = "forbidden_commands_state")
    private CheatingDetectionState forbiddenCommandsState;
    @Enumerated(EnumType.STRING)
    @Column(name = "no_commands_state")
    private CheatingDetectionState noCommandsState;
    @OneToMany(
            mappedBy = "detection",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<ForbiddenCommand> commands = new HashSet<>();

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

    public Set<ForbiddenCommand> getForbiddenCommands() {
        return commands;
    }

    public void setForbiddenCommands(Set<ForbiddenCommand> commands) {
        this.commands = commands;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheatingDetection that = (CheatingDetection) o;
        return Objects.equals(trainingInstanceId, that.trainingInstanceId) &&
                Objects.equals(executedBy, that.executedBy) &&
                Objects.equals(executeTime, that.executeTime) &&
                Objects.equals(proximityThreshold, that.proximityThreshold) &&
                currentState == that.currentState &&
                Objects.equals(results, that.results) &&
                answerSimilarityState == that.answerSimilarityState &&
                locationSimilarityState == that.locationSimilarityState &&
                timeProximityState == that.timeProximityState &&
                minimalSolveTimeState == that.minimalSolveTimeState &&
                forbiddenCommandsState == that.forbiddenCommandsState &&
                noCommandsState == that.noCommandsState &&
                Objects.equals(commands, that.commands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainingInstanceId, executedBy, executeTime, proximityThreshold,
                currentState, results, answerSimilarityState, locationSimilarityState, timeProximityState,
                minimalSolveTimeState, forbiddenCommandsState, noCommandsState, commands);
    }

    @Override
    public String toString() {
        return "CheatingDetection{" +
                "trainingInstanceId=" + trainingInstanceId +
                ", executedBy='" + executedBy + '\'' +
                ", executeTime=" + executeTime +
                ", proximityThreshold=" + proximityThreshold +
                ", currentState=" + currentState +
                ", results=" + results +
                ", answerSimilarityState=" + answerSimilarityState +
                ", locationSimilarityState=" + locationSimilarityState +
                ", timeProximityState=" + timeProximityState +
                ", minimalSolveTimeState=" + minimalSolveTimeState +
                ", forbiddenCommandsState=" + forbiddenCommandsState +
                ", noCommandsState=" + noCommandsState +
                ", forbiddenCommands=" + commands +
                '}';
    }
}
