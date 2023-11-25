package cz.muni.ics.kypo.training.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.persistence.model.Hint;

/**
 * The JPA repository interface to manage {@link Hint} instances.
 */
@Repository
public interface HintRepository extends JpaRepository<Hint, Long>, QuerydslPredicateExecutor<Hint> {

    /**
     * Delete hints by level id.
     *
     * @param levelId the level id
     */
    @Modifying
    void deleteHintsByLevelId(@Param("levelId") Long levelId);

}
