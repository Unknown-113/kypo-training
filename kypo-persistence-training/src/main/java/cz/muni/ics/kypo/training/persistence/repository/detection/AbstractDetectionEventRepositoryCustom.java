package cz.muni.ics.kypo.training.persistence.repository.detection;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.persistence.model.detection.AbstractDetectionEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface AbstractDetectionEventRepositoryCustom {

    /**
     * Finds all detection events by cheating detection id.
     *
     * @param cheatingDetectionId the cheating detection id
     * @param pageable            the pageable
     */
    Page<AbstractDetectionEvent> findAllByCheatingDetectionId(@Param("cheatingDetectionId") Long cheatingDetectionId,
                                                              @Param("pageable") Pageable pageable,
                                                              Predicate predicate);

}
