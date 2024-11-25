package cz.muni.ics.kypo.training.persistence.model;

import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Class represents Training run.
 * Training runs can be created based on instances.
 * Training runs are accessed by trainees
 */
@Entity
@Table(name = "training_run")
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = "TrainingRun.findAllParticipantRef",
                attributeNodes = @NamedAttributeNode(value = "participantRef")
        ),
        @NamedEntityGraph(
                name = "TrainingRun.findByIdParticipantRefTrainingInstance",
                attributeNodes = {
                        @NamedAttributeNode(value = "participantRef"),
                        @NamedAttributeNode(value = "trainingInstance")
                }
        )
})
@NamedQueries({
        @NamedQuery(
                name = "TrainingRun.findRunningTrainingRunOfUser",
                query = "SELECT tr FROM TrainingRun tr " +
                        "JOIN FETCH tr.trainingInstance ti " +
                        "JOIN FETCH tr.participantRef pr " +
                        "JOIN FETCH tr.currentLevel cl " +
                        "WHERE ti.accessToken = :accessToken AND pr.userRefId = :userRefId AND tr.sandboxInstanceRefId IS NOT NULL AND tr.state NOT LIKE 'FINISHED'"
        ),
        @NamedQuery(
                name = "TrainingRun.findByIdWithLevel",
                query = "SELECT tr FROM TrainingRun tr " +
                        "JOIN FETCH tr.currentLevel " +
                        "JOIN FETCH tr.trainingInstance ti " +
                        "JOIN FETCH ti.trainingDefinition " +
                        "WHERE tr.id= :trainingRunId",
                lockMode = LockModeType.PESSIMISTIC_WRITE
        ),
        @NamedQuery(
                name = "TrainingRun.deleteTrainingRunsByTrainingInstance",
                query = "DELETE FROM TrainingRun tr WHERE tr.trainingInstance.id = :trainingInstanceId"
        ),
        @NamedQuery(
                name = "TrainingRun.existsAnyForTrainingInstance",
                query = "SELECT (COUNT(tr) > 0) FROM TrainingRun tr INNER JOIN tr.trainingInstance ti WHERE ti.id = :trainingInstanceId"
        ),
        @NamedQuery(
                name = "TrainingRun.findAllByParticipantRefId",
                query = "SELECT tr FROM TrainingRun tr " +
                        "INNER JOIN tr.participantRef pr " +
                        "INNER JOIN tr.trainingInstance ti " +
                        "INNER JOIN ti.trainingDefinition " +
                        "WHERE pr.userRefId = :userRefId"
        ),
        @NamedQuery(
                name = "TrainingRun.findAllByTrainingDefinitionIdAndParticipantUserRefId",
                query = "SELECT tr FROM TrainingRun tr " +
                        "INNER JOIN tr.participantRef pr " +
                        "INNER JOIN tr.trainingInstance ti " +
                        "INNER JOIN ti.trainingDefinition td " +
                        "WHERE td.id = :trainingDefinitionId AND pr.userRefId = :userRefId"
        ),
        @NamedQuery(
                name = "TrainingRun.findAllActiveByTrainingInstanceId",
                query = "SELECT tr FROM TrainingRun tr " +
                        "INNER JOIN tr.trainingInstance ti " +
                        "WHERE ti.id = :trainingInstanceId AND tr.state <> 'ARCHIVED'"
        ),
        @NamedQuery(
                name = "TrainingRun.findAllInactiveByTrainingInstanceId",
                query = "SELECT tr FROM TrainingRun tr " +
                        "INNER JOIN tr.trainingInstance ti " +
                        "WHERE ti.id = :trainingInstanceId AND tr.state = 'ARCHIVED'"
        ),
        @NamedQuery(
                name = "TrainingRun.findAllFinishedByTrainingInstanceId",
                query = "SELECT tr FROM TrainingRun tr " +
                        "INNER JOIN tr.trainingInstance ti " +
                        "WHERE ti.id = :trainingInstanceId AND tr.state = 'FINISHED'"
        ),
        @NamedQuery(
                name = "TrainingRun.findAllByTrainingDefinitionId",
                query = "SELECT tr FROM TrainingRun tr " +
                        "INNER JOIN tr.trainingInstance ti " +
                        "INNER JOIN ti.trainingDefinition td " +
                        "WHERE td.id = :trainingDefinitionId"
        ),
        @NamedQuery(
                name = "TrainingRun.findAllSandboxIdsByTrainingInstanceId",
                query = "SELECT tr.sandboxInstanceRefId FROM TrainingRun tr " +
                        "INNER JOIN tr.trainingInstance ti " +
                        "WHERE ti.id = :trainingInstanceId"
        )
})
public class TrainingRun extends AbstractEntity<Long> {

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    @Column(name = "event_log_reference", nullable = true)
    private String eventLogReference;
    @Column(name = "state", length = 128, nullable = false)
    @Enumerated(EnumType.STRING)
    private TRState state;
    @Column(name = "incorrect_answer_count", nullable = false)
    private int incorrectAnswerCount;
    @Column(name = "solution_taken", nullable = false)
    private boolean solutionTaken;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AbstractLevel currentLevel;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private TrainingInstance trainingInstance;
    @Column(name = "sandbox_instance_ref_id", length = 36)
    private String sandboxInstanceRefId;
    @Column(name = "sandbox_instance_allocation_id")
    private Integer sandboxInstanceAllocationId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_ref_id", nullable = false)
    private UserRef participantRef;
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "assessment_responses", nullable = true)
    private String assessmentResponses;
    @Column(name = "total_training_score")
    private int totalTrainingScore;
    @Column(name = "total_assessment_score")
    private int totalAssessmentScore;
    @Column(name = "max_level_score")
    private int maxLevelScore;
    @Column(name = "level_answered")
    private boolean levelAnswered;
    @ElementCollection(targetClass = SolutionInfo.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "solution_info", joinColumns = @JoinColumn(name = "training_run_id"))
    private Set<SolutionInfo> solutionInfoList = new HashSet<>();
    @ElementCollection(targetClass = HintInfo.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "hint_info", joinColumns = @JoinColumn(name = "training_run_id"))
    private Set<HintInfo> hintInfoList = new HashSet<>();
    @Column(name = "previous_sandbox_instance_ref_id", length = 36)
    private String previousSandboxInstanceRefId;
    @Column(name = "current_penalty")
    private int currentPenalty;
    @Column(name = "has_detection_event")
    private boolean hasDetectionEvent;

    /**
     * Gets unique identification number of Training run
     *
     * @return the id
     */
    public Long getId() {
        return super.getId();
    }

    /**
     * Sets unique identification number of Training run
     *
     * @param id the id
     */
    public void setId(Long id) {
        super.setId(id);
    }

    /**
     * Gets initiation time of Training run
     *
     * @return the start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Sets initiation time of Training run
     *
     * @param startTime the start time
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets finish time of Training run
     *
     * @return the end time
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Sets finish time of Training run
     *
     * @param endTime the end time
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets event log reference.
     *
     * @return the event log reference
     */
    public String getEventLogReference() {
        return eventLogReference;
    }

    /**
     * Sets event log reference.
     *
     * @param eventLogReference the event log reference
     */
    public void setEventLogReference(String eventLogReference) {
        this.eventLogReference = eventLogReference;
    }

    /**
     * Gets completion state of Training run
     * States are RUNNING, FINISHED, ARCHIVED
     *
     * @return the state
     */
    public TRState getState() {
        return state;
    }

    /**
     * Sets completion state of Training run
     * States are RUNNING, FINISHED, ARCHIVED
     *
     * @param state the state
     */
    public void setState(TRState state) {
        this.state = state;
    }

    /**
     * Gets level that is currently being displayed to the trainee
     *
     * @return the current level
     */
    public AbstractLevel getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Sets level that is currently being displayed to the trainee
     * Sets default data about level to training run
     *
     * @param currentLevel the current level
     */
    public void setCurrentLevel(AbstractLevel currentLevel) {
        this.currentPenalty = 0;
        this.maxLevelScore = currentLevel.getMaxScore();
        this.levelAnswered = currentLevel instanceof InfoLevel;
        this.solutionTaken = false;
        this.currentLevel = currentLevel;
    }

    /**
     * Gets Training instance associated to Training run
     *
     * @return the training instance
     */
    public TrainingInstance getTrainingInstance() {
        return trainingInstance;
    }

    /**
     * Sets Training instance associated to Training run
     *
     * @param trainingInstance the training instance
     */
    public void setTrainingInstance(TrainingInstance trainingInstance) {
        this.trainingInstance = trainingInstance;
    }

    /**
     * Gets id of sandbox instance reference associated with Training run
     *
     * @return the sandbox instance ref id
     */
    public String getSandboxInstanceRefId() {
        return sandboxInstanceRefId;
    }

    /**
     * Sets id of sandbox instance reference associated with Training run
     *
     * @param sandboxInstanceRefId the sandbox instance ref id
     */
    public void setSandboxInstanceRefId(String sandboxInstanceRefId) {
        this.sandboxInstanceRefId = sandboxInstanceRefId;
    }

    /**
     * Gets sandbox instance allocation id associated with Training run
     *
     * @return the sandbox instance allocation id
     */
    public Integer getSandboxInstanceAllocationId() {
        return sandboxInstanceAllocationId;
    }

    /**
     * Sets sandbox instance allocation id associated with Training run
     * @param sandboxInstanceAllocationId the sandbox instance allocation id
     */
    public void setSandboxInstanceAllocationId(Integer sandboxInstanceAllocationId) {
        this.sandboxInstanceAllocationId = sandboxInstanceAllocationId;
    }

    /**
     * Gets number of failed attempts by trainee to submit correct answer on current level
     *
     * @return the incorrect answer count
     */
    public int getIncorrectAnswerCount() {
        return incorrectAnswerCount;
    }

    /**
     * Sets number of failed attempts trainee can submit on current level
     *
     * @param incorrectAnswerCount the incorrect answer count
     */
    public void setIncorrectAnswerCount(int incorrectAnswerCount) {
        this.incorrectAnswerCount = incorrectAnswerCount;
    }

    /**
     * Gets solution was taken on current level
     *
     * @return the boolean
     */
    public boolean isSolutionTaken() {
        return solutionTaken;
    }

    /**
     * Sets solution was taken on current level
     *
     * @param solutionTaken the solution taken
     */
    public void setSolutionTaken(boolean solutionTaken) {
        this.solutionTaken = solutionTaken;
    }

    /**
     * Gets responses of current assessment level
     *
     * @return the assessment responses
     */
    public String getAssessmentResponses() {
        return assessmentResponses;
    }

    /**
     * Sets responses of current assessment level
     *
     * @param assessmentResponses the assessment responses
     */
    public void setAssessmentResponses(String assessmentResponses) {
        this.assessmentResponses = assessmentResponses;
    }

    /**
     * Gets DB reference of trainee
     *
     * @return the participant ref
     */
    public UserRef getParticipantRef() {
        return participantRef;
    }

    /**
     * Sets DB reference of trainee
     *
     * @param participantRef the participant ref
     */
    public void setParticipantRef(UserRef participantRef) {
        this.participantRef = participantRef;
    }

    /**
     * Gets score achieved in training levels
     *
     * @return the total training score
     */
    public int getTotalTrainingScore() {
        return totalTrainingScore;
    }

    /**
     * Sets score achieved in training levels.
     *
     * @param totalTrainingScore the total training score
     */
    public void setTotalTrainingScore(int totalTrainingScore) {
        this.totalTrainingScore = totalTrainingScore;
    }

    public void decreaseTotalTrainingScore(int penalty) {
        this.totalTrainingScore -= penalty;
    }

    /**
     * Gets score achieved in assessment levels.
     *
     * @return the total assessment score
     */
    public int getTotalAssessmentScore() {
        return totalAssessmentScore;
    }

    /**
     * Sets score achieved in assessment levels.
     *
     * @param totalAssessmentScore the total assessment score
     */
    public void setTotalAssessmentScore(int totalAssessmentScore) {
        this.totalAssessmentScore = totalAssessmentScore;
    }

    /**
     * Takes away points from total assessment level score
     *
     * @param penalty the penalty
     */
    public void decreaseTotalAssessmentScore(int penalty) {
        this.totalAssessmentScore -= penalty;
    }

    /**
     * Increase total training level score.
     *
     * @param points the points
     */
    public void increaseTotalTrainingScore(int points) {
        this.totalTrainingScore += points;
    }

    /**
     * Increase total assessment score.
     *
     * @param points the points
     */
    public void increaseTotalAssessmentScore(int points) {
        this.totalAssessmentScore += points;
    }


    /**
     * Gets max level score.
     *
     * @return the max level score
     */
    public int getMaxLevelScore() {
        return maxLevelScore;
    }

    /**
     * Sets max level score.
     *
     * @param maxLevelScore the max level score
     */
    public void setMaxLevelScore(int maxLevelScore) {
        this.maxLevelScore = maxLevelScore;
    }

    /**
     * Increase current penalty.
     *
     * @param penalty the penalty
     */
    public void increaseCurrentPenalty(int penalty) {
        this.currentPenalty += penalty;
    }

    /**
     * Gets if level was answered
     *
     * @return the boolean
     */
    public boolean isLevelAnswered() {
        return levelAnswered;
    }

    /**
     * Sets if level was answered
     *
     * @param levelAnswered the level answered
     */
    public void setLevelAnswered(boolean levelAnswered) {
        this.levelAnswered = levelAnswered;
    }

    /**
     * Gets hints associated to current training level
     *
     * @return the hint info list
     */
    public Set<HintInfo> getHintInfoList() {
        return hintInfoList;
    }

    /**
     * Sets hint info list.
     *
     * @param hintInfoList the hint info list
     */
    public void setHintInfoList(Set<HintInfo> hintInfoList) {
        this.hintInfoList = hintInfoList;
    }

    /**
     * Adds hint to current training level
     *
     * @param hintInfo the hint info
     */
    public void addHintInfo(HintInfo hintInfo) {
        this.hintInfoList.add(hintInfo);
    }

    /**
     * Removes hint to current training level
     *
     * @param hintInfo the hint info
     */
    public void removeHintInfo(HintInfo hintInfo) {
        this.hintInfoList.remove(hintInfo);
    }

    /**
     * Gets taken solutions associated to training run
     *
     * @return the solution info list
     */
    public Set<SolutionInfo> getSolutionInfoList() {
        return solutionInfoList;
    }

    /**
     * Sets solution info list.
     *
     * @param solutionInfo the solution info list
     */
    public void setSolutionInfoList(Set<SolutionInfo> solutionInfo) {
        this.solutionInfoList = solutionInfo;
    }

    /**
     * Adds solution to current training run
     *
     * @param solutionInfo the solution info
     */
    public void addSolutionInfo(SolutionInfo solutionInfo) {
        this.solutionInfoList.add(solutionInfo);
    }

    /**
     * Removes solution to current training run
     *
     * @param solutionInfo the solution info
     */
    public void removeSolutionInfo(SolutionInfo solutionInfo) {
        this.solutionInfoList.remove(solutionInfo);
    }

    /**
     * Gets id of previous sandbox instance ref assigned by training run.
     *
     * @return the id of previous sandbox instance ref
     */
    public String getPreviousSandboxInstanceRefId() {
        return previousSandboxInstanceRefId;
    }

    /**
     * Sets previous sandbox instance ref ID
     *
     * @param previousSandboxInstanceRefId the id of previous sandbox instance ref
     */
    public void setPreviousSandboxInstanceRefId(String previousSandboxInstanceRefId) {
        this.previousSandboxInstanceRefId = previousSandboxInstanceRefId;
    }

    /**
     * Gets current penalty.
     *
     * @return the current penalty
     */
    public int getCurrentPenalty() {
        return currentPenalty;
    }

    /**
     * Sets current penalty.
     *
     * @param currentPenalty the current penalty
     */
    public void setCurrentPenalty(int currentPenalty) {
        this.currentPenalty = currentPenalty;
    }

    public boolean isHasDetectionEvent() {
        return hasDetectionEvent;
    }

    public void setHasDetectionEvent(boolean hasDetectionEvent) {
        this.hasDetectionEvent = hasDetectionEvent;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentLevel, eventLogReference, startTime, endTime, state, trainingInstance, incorrectAnswerCount);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof TrainingRun))
            return false;
        TrainingRun other = (TrainingRun) obj;
        return Objects.equals(currentLevel, other.getCurrentLevel())
                && Objects.equals(eventLogReference, other.getEventLogReference())
                && Objects.equals(startTime, other.getStartTime())
                && Objects.equals(endTime, other.getEndTime())
                && Objects.equals(state, other.getState())
                && Objects.equals(incorrectAnswerCount, other.getIncorrectAnswerCount())
                && Objects.equals(trainingInstance, other.getTrainingInstance())
                && Objects.equals(participantRef, other.getParticipantRef())
                && Objects.equals(solutionTaken, other.isSolutionTaken());
    }

    @Override
    public String toString() {
        return "TrainingRun{" +
                "id=" + super.getId() +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", eventLogReference='" + eventLogReference + '\'' +
                ", state=" + state +
                ", incorrectAnswerCount=" + incorrectAnswerCount +
                ", solutionTaken=" + solutionTaken +
                ", currentLevel=" + currentLevel +
                ", sandboxInstanceRefId=" + sandboxInstanceRefId +
                ", sandboxInstanceAllocationId=" + sandboxInstanceAllocationId +
                ", totalTrainingScore=" + totalTrainingScore +
                ", totalAssessmentScore=" + totalAssessmentScore +
                ", maxLevelScore=" + maxLevelScore +
                ", levelAnswered=" + levelAnswered +
                '}';
    }
}