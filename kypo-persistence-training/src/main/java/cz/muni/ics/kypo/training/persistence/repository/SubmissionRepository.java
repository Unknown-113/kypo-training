package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.InfoLevel;
import cz.muni.ics.kypo.training.persistence.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long>, QuerydslPredicateExecutor<InfoLevel> {

    @Modifying
    void deleteAllByTrainingRunId(Long trainingRunId);

    /**
     * Find all correct submissions of training instance.
     *
     * @param trainingInstanceId  the training instance id
     * @return list of all correct {@link Submission}s of training instance.
     */
    List<Submission> getCorrectSubmissionsOfTrainingInstance(@Param("trainingInstanceId") Long trainingInstanceId);

    /**
     * Find all correct submissions of training run sorted by time.
     *
     * @param trainingRunId  the training run id
     * @return list of all correct {@link Submission}s of training run sorted by time.
     */
    List<Submission> getCorrectSubmissionsOfTrainingRunSorted(@Param("trainingRunId") Long trainingRunId);     
    
    /**
     * Find all incorrect submissions of training instance.
     *
     * @param trainingInstanceId  the training instance id
     * @return list of all incorrect {@link Submission}s of training instance.
     */
    List<Submission> getIncorrectSubmissionsOfTrainingInstance(@Param("trainingInstanceId") Long trainingInstanceId);
    
    /**
     * Find all submissions with similar IP addresses of different players.
     *
     * @param trainingInstanceId  the training instance id
     * @param levelId the training level
     * @return list of all {@link Submission}s with similar IP addresses of different players.
     */
    List<Submission> getSubmissionsByLevelAndInstance(@Param("trainingInstanceId") Long trainingInstanceId,
                                                      @Param("levelId") Long levelId);

    /**
     * Find all time proximity submissions of training instance.
     *
     * @param trainingInstanceId  the training instance id
     * @param levelId the training level
     * @return list of all incorrect {@link Submission}s of training instance.
     */
    List<Submission> getAllTimeProximitySubmissionsOfLevel(@Param("trainingInstanceId") Long trainingInstanceId,
                                                           @Param("levelId") Long levelId);

}
