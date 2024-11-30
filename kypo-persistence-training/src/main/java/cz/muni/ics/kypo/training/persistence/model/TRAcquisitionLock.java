package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.*;

/**
 * The entity which prevents multiple training runs to be created in parallel threads. Basically it determines active training runs.
 */
@Getter
@Setter
@Entity
@Table(name = "training_run_acquisition_lock",
        uniqueConstraints = @UniqueConstraint(columnNames = {"participant_ref_id", "training_instance_id"}))
@NamedQueries({
        @NamedQuery(
                name = "TRAcquisitionLock.deleteByParticipantRefIdAndTrainingInstanceId",
                query = "DELETE FROM TRAcquisitionLock tral WHERE tral.participantRefId = :participantRefId AND tral.trainingInstanceId = :trainingInstanceId"
        )
})
public class TRAcquisitionLock extends AbstractEntity<Long> {

    @Column(name = "participant_ref_id")
    private Long participantRefId;
    @Column(name = "training_instance_id")
    private Long trainingInstanceId;
    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    /**
     * Instantiates a new Tr acquisition lock.
     */
    public TRAcquisitionLock() {
    }

    /**
     * Instantiates a new Tr acquisition lock.
     *
     * @param participantRefId   the participant ref id
     * @param trainingInstanceId the training instance id
     * @param creationTime       the creation time
     */
    public TRAcquisitionLock(Long participantRefId, Long trainingInstanceId, LocalDateTime creationTime) {
        this.participantRefId = participantRefId;
        this.trainingInstanceId = trainingInstanceId;
        this.creationTime = creationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TRAcquisitionLock))
            return false;
        TRAcquisitionLock that = (TRAcquisitionLock) o;
        return Objects.equals(getParticipantRefId(), that.getParticipantRefId()) &&
                Objects.equals(getTrainingInstanceId(), that.getTrainingInstanceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getParticipantRefId(), getTrainingInstanceId());
    }

    @Override
    public String toString() {
        return "TRAcquisitionLock{" +
                "id=" + super.getId() +
                ", participantRefId=" + participantRefId +
                ", trainingInstanceId=" + trainingInstanceId +
                ", creationTime=" + creationTime +
                '}';
    }
}