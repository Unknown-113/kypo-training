package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * The entity which prevents multiple training runs to be created in parallel threads. Basically it determines active training runs.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
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
}
