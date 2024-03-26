package cz.muni.ics.kypo.training.persistence.repository.detection;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import cz.muni.ics.kypo.training.persistence.model.detection.AbstractDetectionEvent;
import cz.muni.ics.kypo.training.persistence.model.detection.QAbstractDetectionEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

public class AbstractDetectionEventRepositoryImpl extends QuerydslRepositorySupport implements AbstractDetectionEventRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Instantiates a new Abstract Detection Event repository.
     */
    public AbstractDetectionEventRepositoryImpl() {
        super(AbstractDetectionEvent.class);
    }

    @Override
    @Transactional
    public Page<AbstractDetectionEvent> findAllByCheatingDetectionId(Long cheatingDetectionId, Pageable pageable, Predicate predicate) {

        QAbstractDetectionEvent abstractDetectionEvent = QAbstractDetectionEvent.abstractDetectionEvent;

        JPQLQuery<AbstractDetectionEvent> query = new JPAQueryFactory(entityManager).selectFrom(abstractDetectionEvent).distinct()
                .where(abstractDetectionEvent.cheatingDetectionId.eq(cheatingDetectionId));

        if (predicate != null) {
            query.where(predicate);
        }
        return getPage(query, pageable);
    }

    private <T> Page getPage(JPQLQuery<T> query, Pageable pageable) {
        if (pageable == null) {
            pageable = PageRequest.of(0, 20);
        }
        query = getQuerydsl().applyPagination(pageable, query);
        long count = query.fetchCount();
        return new PageImpl<>(query.fetch(), pageable, count);
    }
}
