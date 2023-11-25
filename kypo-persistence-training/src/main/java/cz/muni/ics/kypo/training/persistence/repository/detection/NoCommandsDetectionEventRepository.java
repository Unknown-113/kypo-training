package cz.muni.ics.kypo.training.persistence.repository.detection;

import cz.muni.ics.kypo.training.persistence.model.detection.AnswerSimilarityDetectionEvent;
import cz.muni.ics.kypo.training.persistence.model.detection.LocationSimilarityDetectionEvent;
import cz.muni.ics.kypo.training.persistence.model.detection.NoCommandsDetectionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoCommandsDetectionEventRepository extends JpaRepository<NoCommandsDetectionEvent, Long>, QuerydslPredicateExecutor<NoCommandsDetectionEvent> {

    /**
     * Returns the detection event based on its id
     *
     * @param eventId the detection event id
     */
    NoCommandsDetectionEvent findNoCommandsEventById(@Param("eventId") Long eventId);

    /**
     * Returns all no commands detection events of cheating detection
     *
     * @param cheatingDetectionId the detection event id
     */
    List<NoCommandsDetectionEvent> findAllByCheatingDetectionId(@Param("cheatingDetectionId") Long cheatingDetectionId);
}
