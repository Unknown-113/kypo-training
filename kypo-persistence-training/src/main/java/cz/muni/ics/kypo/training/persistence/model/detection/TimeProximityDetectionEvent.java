package cz.muni.ics.kypo.training.persistence.model.detection;

import javax.persistence.*;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@Entity
@Table(name = "time_proximity_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
        @NamedQuery(
                name = "TimeProximityDetectionEvent.findTimeProximityEventById",
                query = "SELECT tpde FROM TimeProximityDetectionEvent tpde WHERE tpde.id = :eventId"
        ),
        @NamedQuery(
                name = "TimeProximityDetectionEvent.findAllByCheatingDetectionId",
                query = "SELECT tpde FROM TimeProximityDetectionEvent tpde WHERE tpde.cheatingDetectionId = :cheatingDetectionId"
        )
})
public class TimeProximityDetectionEvent extends AbstractDetectionEvent {

    @Column(name = "threshold")
    private Long threshold;
}
