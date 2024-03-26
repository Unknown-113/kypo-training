package cz.muni.ics.kypo.training.persistence.repository.detection;

import cz.muni.ics.kypo.training.persistence.model.detection.AbstractDetectionEvent;
import cz.muni.ics.kypo.training.persistence.model.detection.DetectionEventParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetectionEventParticipantRepository extends JpaRepository<DetectionEventParticipant, Long>, QuerydslPredicateExecutor<DetectionEventParticipant> {

    /**
     * Finds all detection event participants by detection event id.
     *
     * @param eventId the detection event id
     * @param pageable            the pageable
     */
    Page<DetectionEventParticipant> findAllByEventId(@Param("eventId") Long eventId,
                                                     @Param("pageable") Pageable pageable);


    /**
     * Finds all detection event participants by detection event id.
     *
     * @param eventId the detection event id
     */
    List<DetectionEventParticipant> findAllByEventId(@Param("eventId") Long eventId);

    /**
     * Finds all participant userIds by cheating detection id.
     *
     * @param cheatingDetectionId the cheating detection id
     */
    @Query("SELECT dep FROM DetectionEventParticipant dep WHERE dep.cheatingDetectionId = :cheatingDetectionId")
    List<DetectionEventParticipant> findAllParticipantsOfCheatingDetection(@Param("cheatingDetectionId") Long cheatingDetectionId);

    /**
     * Delete all participants of cheating detection.
     *
     * @param cheatingDetectionId the cheating detection id
     */
    @Modifying
    void deleteAllParticipantsByCheatingDetectionId(@Param("cheatingDetectionId") Long cheatingDetectionId);

    /**
     * Finds all detection event ids by user id.
     *
     * @param userId the user id
     */
    @Query("SELECT DISTINCT dep.detectionEventId FROM DetectionEventParticipant dep WHERE dep.userId = :userId")
    List<Long> getAllDetectionEventsIdsOfParticipant(@Param("userId") Long userId);
}
