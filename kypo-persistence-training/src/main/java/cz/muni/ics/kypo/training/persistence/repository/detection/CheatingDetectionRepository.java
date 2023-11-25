package cz.muni.ics.kypo.training.persistence.repository.detection;

import cz.muni.ics.kypo.training.persistence.model.detection.AbstractDetectionEvent;
import cz.muni.ics.kypo.training.persistence.model.detection.CheatingDetection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

public interface CheatingDetectionRepository extends JpaRepository<CheatingDetection, Long>, QuerydslPredicateExecutor<CheatingDetection> {

    /**
     * Find all cheating detections of a training Instance
     *
     * @param trainingInstanceId the training instance id
     * @param pageable           the pageable
     * @return the page of all {@link CheatingDetection}s occurred in a training instance
     */
    Page<CheatingDetection> findAllByTrainingInstanceId(@Param("trainingInstanceId") Long trainingInstanceId, Pageable pageable);

    /**
     * Find all cheating detections of a training Instance
     *
     * @param cheatingDetectionId the training instance id
     * @return {@link CheatingDetection} occurred in a training instance
     */
    CheatingDetection findCheatingDetectionById(@Param("cheatingDetectionId") Long cheatingDetectionId);

    /**
     * Delete cheating detection by cheating detection id.
     *
     * @param cheatingDetectionId the cheating detection id
     */
    @Modifying
    void deleteCheatingDetectionById(@Param("cheatingDetectionId") Long cheatingDetectionId);

    /**
     * Delete all cheating detections by training instance id.
     *
     * @param trainingInstanceId the training instance id
     */
    @Modifying
    void deleteAllCheatingDetectionsOfTrainingInstance(@Param("trainingInstanceId") Long trainingInstanceId);
}
