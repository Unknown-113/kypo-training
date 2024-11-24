package cz.muni.ics.kypo.training.persistence.model.detection;

import javax.persistence.*;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
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
}
