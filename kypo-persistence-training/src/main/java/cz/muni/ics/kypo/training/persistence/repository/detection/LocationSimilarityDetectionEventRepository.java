package cz.muni.ics.kypo.training.persistence.repository.detection;

import cz.muni.ics.kypo.training.persistence.model.detection.AnswerSimilarityDetectionEvent;
import cz.muni.ics.kypo.training.persistence.model.detection.ForbiddenCommandsDetectionEvent;
import cz.muni.ics.kypo.training.persistence.model.detection.LocationSimilarityDetectionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LocationSimilarityDetectionEventRepository extends JpaRepository<LocationSimilarityDetectionEvent, Long>, QuerydslPredicateExecutor<LocationSimilarityDetectionEvent> {

    /**
     * Returns the detection event based on its id
     *
     * @param eventId the detection event id
     */
    LocationSimilarityDetectionEvent findLocationSimilarityEventById(@Param("eventId") Long eventId);

    /**
     * Returns all location similarity detection events of cheating detection
     *
     * @param cheatingDetectionId the detection event id
     */
    List<LocationSimilarityDetectionEvent> findAllByCheatingDetectionId(@Param("cheatingDetectionId") Long cheatingDetectionId);
}
