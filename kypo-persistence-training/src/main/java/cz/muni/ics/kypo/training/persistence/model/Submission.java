package cz.muni.ics.kypo.training.persistence.model;

import cz.muni.ics.kypo.training.persistence.model.enums.SubmissionType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "submission")
@NamedQueries({
	   @NamedQuery(
	   name = "Submission.getCorrectSubmissionsOfTrainingRunSorted",
           query = "SELECT s FROM Submission s " +
                   "JOIN FETCH s.trainingRun tr " +
                   "WHERE s.type ='CORRECT' AND tr.id = :trainingRunId " +
                   "ORDER BY s.date"
	   ),
	   @NamedQuery(
	   name = "Submission.getCorrectSubmissionsOfTrainingInstance",
           query = "SELECT s FROM Submission s " +
                   "JOIN FETCH s.trainingRun tr " +
                   "JOIN FETCH tr.trainingInstance ti " +
                   "WHERE s.type = 'CORRECT' AND ti.id = :trainingInstanceId " +
                   "ORDER BY tr.id, s.date"
	   ),
           @NamedQuery(
	   name = "Submission.getIncorrectSubmissionsOfTrainingInstance",
           query = "SELECT s FROM Submission s " +
                   "JOIN FETCH s.trainingRun tr " +
                   "JOIN FETCH tr.trainingInstance ti " +
                   "JOIN FETCH tr.participantRef pr " +
                   "WHERE s.type = 'INCORRECT' AND ti.id = :trainingInstanceId " +
                   "ORDER BY pr.userRefId, s.date"
	   ),
           @NamedQuery(
	   name = "Submission.getAllTimeProximitySubmissionsOfLevel",
           query = "SELECT s FROM Submission s " +
                   "JOIN FETCH s.trainingRun tr " +
                   "JOIN FETCH tr.trainingInstance ti " +
                   "JOIN FETCH s.level l " +
                   "WHERE s.type = 'CORRECT' AND ti.id = :trainingInstanceId AND l.id = :levelId " +
                   "ORDER BY s.date"
           ),
           @NamedQuery(
	   name = "Submission.getSubmissionsByLevelAndInstance",
           query = "SELECT s FROM Submission s " +
                   "JOIN FETCH s.trainingRun tr " +
                   "JOIN FETCH tr.trainingInstance ti " +
                   "JOIN FETCH s.level l " +
                   "WHERE l.id = :levelId AND ti.id = :trainingInstanceId " +
                   "ORDER BY tr.id"
           )
})
public class Submission extends AbstractEntity<Long> implements Serializable {

    @Column(name = "provided", nullable = false)
    private String provided;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private SubmissionType type;
    @Column(name = "date", nullable = false)
    private LocalDateTime date;
    @Column(name = "ip_address", nullable = false)
    private String ipAddress;
    @JoinColumn(name = "level_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private AbstractLevel level;
    @JoinColumn(name = "training_run_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private TrainingRun trainingRun;
}
