package cz.muni.ics.kypo.training.persistence.repository.detection;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import cz.muni.ics.kypo.training.persistence.model.detection.QAbstractDetectionEvent;
import cz.muni.ics.kypo.training.persistence.model.detection.AbstractDetectionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


/**
 * The JPA repository interface to manage {@link AbstractDetectionEvent} instances.
 */
@Repository
public interface AbstractDetectionEventRepository extends JpaRepository<AbstractDetectionEvent, Long>, AbstractDetectionEventRepositoryCustom, QuerydslPredicateExecutor<AbstractDetectionEvent>, QuerydslBinderCustomizer<QAbstractDetectionEvent> {

    /**
     * That method is used to make the query dsl string values case insensitive and also it supports partial matches in the database.
     *
     * @param querydslBindings
     * @param qAbstractDetectionEvent
     */
    @Override
    default void customize(QuerydslBindings querydslBindings, QAbstractDetectionEvent qAbstractDetectionEvent) {
        querydslBindings.bind(String.class).all((StringPath path, Collection<? extends String> values) -> {
            BooleanBuilder predicate = new BooleanBuilder();
            values.forEach(value -> predicate.and(path.containsIgnoreCase(value)));
            return Optional.ofNullable(predicate);
        });
    }

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
                                                              @Param("pageable") Pageable pageable,
                                                              Predicate predicate);

    /**
     * Finds all detection events by cheating detection id.
     *
     * @param cheatingDetectionId the cheating detection id
     */
    List<AbstractDetectionEvent> findAllByCheatingDetectionId(@Param("cheatingDetectionId") Long cheatingDetectionId);

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

