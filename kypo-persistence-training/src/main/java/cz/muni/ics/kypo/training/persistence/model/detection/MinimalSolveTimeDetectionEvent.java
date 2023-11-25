package cz.muni.ics.kypo.training.persistence.model.detection;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "minimal_solve_time_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
        @NamedQuery(
                name = "MinimalSolveTimeDetectionEvent.findMinimalSolveTimeEventById",
                query = "SELECT mstde FROM MinimalSolveTimeDetectionEvent mstde WHERE mstde.id = :eventId"
        ),
        @NamedQuery(
                name = "MinimalSolveTimeDetectionEvent.findAllByCheatingDetectionId",
                query = "SELECT mstde FROM MinimalSolveTimeDetectionEvent mstde WHERE mstde.cheatingDetectionId = :cheatingDetectionId"
        )
})
public class MinimalSolveTimeDetectionEvent extends AbstractDetectionEvent {

    @Column(name = "minimal_solve_time")
    private Long minimalSolveTime;

    public Long getMinimalSolveTime() {
        return minimalSolveTime;
    }

    public void setMinimalSolveTime(Long minimalSolveTime) {
        this.minimalSolveTime = minimalSolveTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MinimalSolveTimeDetectionEvent)) return false;
        if (!super.equals(o)) return false;
        MinimalSolveTimeDetectionEvent other = (MinimalSolveTimeDetectionEvent) o;
        return Objects.equals(getMinimalSolveTime(), other.getMinimalSolveTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getMinimalSolveTime());
    }

    @Override
    public String toString() {
        return "MinimalSolveTimeDetectionEvent{" +
                "minimalSolveTime='" + minimalSolveTime + '}';
    }
}
