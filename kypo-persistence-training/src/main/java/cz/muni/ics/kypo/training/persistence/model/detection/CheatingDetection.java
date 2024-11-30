package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.persistence.model.AbstractEntity;
import cz.muni.ics.kypo.training.persistence.model.enums.CheatingDetectionState;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.*;

/**
 * Class representing cheating detection.
 * Cheating detections are executed by organizers.
 * Cheating detections are bound to a training instance.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@Entity
@Table(name = "cheating_detection")
@NamedQueries({
        @NamedQuery(
                name = "CheatingDetection.findAllByTrainingInstanceId",
                query = "SELECT cd FROM CheatingDetection cd " +
                        "WHERE cd.trainingInstanceId = :trainingInstanceId " +
                        "ORDER BY cd.executeTime"
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
            mappedBy = "cheatingDetection",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ForbiddenCommand> commands = new ArrayList<>();

    private CheatingDetectionState setExecuteState(CheatingDetectionState state) {
        return state != CheatingDetectionState.DISABLED ? CheatingDetectionState.QUEUED : CheatingDetectionState.DISABLED;
    }

    public void setExecuteStates() {
        this.setCurrentState(CheatingDetectionState.RUNNING);
        this.setAnswerSimilarityState(setExecuteState(this.getAnswerSimilarityState()));
        this.setLocationSimilarityState(setExecuteState(this.getLocationSimilarityState()));
        this.setMinimalSolveTimeState(setExecuteState(this.getMinimalSolveTimeState()));
        this.setTimeProximityState(setExecuteState(this.getTimeProximityState()));
        this.setNoCommandsState(setExecuteState(this.getNoCommandsState()));
        this.setForbiddenCommandsState(setExecuteState(this.getForbiddenCommandsState()));
    }
}
