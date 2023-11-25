package cz.muni.ics.kypo.training.persistence.repository.detection;

import cz.muni.ics.kypo.training.persistence.model.detection.AbstractDetectionEvent;
import cz.muni.ics.kypo.training.persistence.model.detection.CheatingDetection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;


/**
 * The JPA repository interface to manage {@link AbstractDetectionEvent} instances.
 */
@Repository
public interface AbstractDetectionEventRepository extends JpaRepository<AbstractDetectionEvent, Long>, QuerydslPredicateExecutor<AbstractDetectionEvent> {

    /**
     * Delete all detection events by cheating detection id.
     *
     * @param cheatingDetectionId the cheating detection id
     */
    @Modifying
    void deleteDetectionEventsOfCheatingDetection(@Param("cheatingDetectionId") Long cheatingDetectionId);

    /**
     * Finds all detection events by cheating detection id.
     *
     * @param cheatingDetectionId the cheating detection id
     * @param pageable            the pageable
     */
    Page<AbstractDetectionEvent> findAllByCheatingDetectionId(@Param("cheatingDetectionId") Long cheatingDetectionId,
                                                              @Param("pageable") Pageable pageable);

    /**
     * Delete all cheats by training instance.
     *
     * @param trainingInstanceId the training instance id
     */
    @Modifying
    void deleteDetectionEventsOfTrainingInstance(@Param("trainingInstanceId") Long trainingInstanceId);

    /**
     * Returns the number of detection events occurred in cheating detection
     *
     * @param cheatingDetectionId the cheating detection id
     */
    Long getNumberOfDetections(@Param("cheatingDetectionId") Long cheatingDetectionId);

    /**
     * Returns the detection event based on its id
     *
     * @param eventId the detection event id
     */
    AbstractDetectionEvent findDetectionEventById(@Param("eventId") Long eventId);
}

