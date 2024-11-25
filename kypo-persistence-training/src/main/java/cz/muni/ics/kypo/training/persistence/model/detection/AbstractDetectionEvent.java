package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.persistence.model.AbstractEntity;
import cz.muni.ics.kypo.training.persistence.model.Submission;
import cz.muni.ics.kypo.training.persistence.model.enums.DetectionEventType;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Class representing Detection event.
 * Detection event occurs based on a submission
 * Detection events can be created based on suspicious
 * submissions in a training instance.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@Entity
@Table(name = "abstract_detection_event")
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({
        @NamedQuery(
                name = "AbstractDetectionEvent.deleteDetectionEventsOfTrainingInstance",
                query = "DELETE FROM AbstractDetectionEvent de " +
                        "WHERE de.trainingInstanceId = :trainingInstanceId"
        ),
        @NamedQuery(
                name = "AbstractDetectionEvent.findAllByCheatingDetectionId",
                query = "SELECT de FROM AbstractDetectionEvent de " +
                        "WHERE de.cheatingDetectionId = :cheatingDetectionId"
        ),
        @NamedQuery(
                name = "AbstractDetectionEvent.deleteDetectionEventsOfCheatingDetection",
                query = "DELETE FROM AbstractDetectionEvent de WHERE de.cheatingDetectionId = :cheatingDetectionId"
        ),
        @NamedQuery(
                name = "AbstractDetectionEvent.getNumberOfDetections",
                query = "SELECT COUNT(de) FROM AbstractDetectionEvent de WHERE de.cheatingDetectionId = :cheatingDetectionId"
        ),
        @NamedQuery(
                name = "AbstractDetectionEvent.findDetectionEventById",
                query = "SELECT de FROM AbstractDetectionEvent de WHERE de.id = :eventId"
        )
})
public class AbstractDetectionEvent extends AbstractEntity<Long> {

    @Column(name = "training_instance_id", nullable = false)
    private Long trainingInstanceId;
    @Column(name = "cheating_detection_id", nullable = false)
    private Long cheatingDetectionId;
    @Column(name = "training_run_id")
    private Long trainingRunId;
    @Column(name = "level_id", nullable = false)
    private Long levelId;
    @Column(name = "level_order", nullable = false)
    private int levelOrder;
    @Column(name = "level_title", nullable = false)
    private String levelTitle;
    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;
    @Column(name = "participant_count", nullable = false)
    private int participantCount;
    @Enumerated(EnumType.STRING)
    @Column(name = "detection_event_type", nullable = false)
    private DetectionEventType detectionEventType;
    @Column(name = "participants", nullable = false)
    private String participants;

    public void setCommonDetectionEventParameters(Submission submission, CheatingDetection cd, DetectionEventType type, int size) {
        this.setCheatingDetectionId(cd.getId());
        this.setDetectedAt(cd.getExecuteTime());
        this.setTrainingRunId(submission.getTrainingRun().getId());
        this.setLevelId(submission.getLevel().getId());
        this.setLevelOrder(submission.getLevel().getOrder());
        this.setLevelTitle(submission.getLevel().getTitle());
        this.setTrainingInstanceId(cd.getTrainingInstanceId());
        this.setDetectionEventType(type);
        this.setParticipantCount(size);
    }
}
