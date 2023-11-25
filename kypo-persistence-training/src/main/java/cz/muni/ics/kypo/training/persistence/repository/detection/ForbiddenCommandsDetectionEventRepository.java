package cz.muni.ics.kypo.training.persistence.repository.detection;

import cz.muni.ics.kypo.training.persistence.model.detection.AnswerSimilarityDetectionEvent;
import cz.muni.ics.kypo.training.persistence.model.detection.ForbiddenCommandsDetectionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ForbiddenCommandsDetectionEventRepository extends JpaRepository<ForbiddenCommandsDetectionEvent, Long>, QuerydslPredicateExecutor<ForbiddenCommandsDetectionEvent> {

    /**
     * Returns the detection event based on its id
     *
     * @param eventId the detection event id
     */
    ForbiddenCommandsDetectionEvent findForbiddenCommandsEventById(@Param("eventId") Long eventId);

    /**
     * Returns all forbidden commands detection events of cheating detection
     *
     * @param cheatingDetectionId the detection event id
     */
    List<ForbiddenCommandsDetectionEvent> findAllByCheatingDetectionId(@Param("cheatingDetectionId") Long cheatingDetectionId);
}
