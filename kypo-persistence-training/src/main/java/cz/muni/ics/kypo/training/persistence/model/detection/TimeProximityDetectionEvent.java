package cz.muni.ics.kypo.training.persistence.model.detection;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

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

    public Long getThreshold() {
        return threshold;
    }

    public void setThreshold(Long threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeProximityDetectionEvent)) return false;
        if (!super.equals(o)) return false;
        TimeProximityDetectionEvent other = (TimeProximityDetectionEvent) o;
        return Objects.equals(getThreshold(), other.getThreshold());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getThreshold());
    }

    @Override
    public String toString() {
        return "TimeProximityDetectionEvent{" +
                ", threshold='" + threshold +
                '}';
    }
}
