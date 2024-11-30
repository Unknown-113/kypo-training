package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class representing DB reference for user and training instances and definition they can access
 */
@Entity
@Table(name = "user_ref", uniqueConstraints = @UniqueConstraint(columnNames = {"user_ref_id"}))
@NamedQueries({
        @NamedQuery(
                name = "UserRef.findUsers",
                query = "SELECT ur FROM UserRef ur WHERE ur.userRefId IN :userRefId"
        ),
        @NamedQuery(
                name = "UserRef.findUserByUserRefId",
                query = "SELECT ur FROM UserRef ur WHERE ur.userRefId = :userRefId"
        ),
        @NamedQuery(
                name = "UserRef.findParticipantsRefIdsByTrainingInstanceId",
                query = "SELECT pr.userRefId FROM TrainingRun tr " +
                        "INNER JOIN tr.participantRef pr " +
                        "INNER JOIN tr.trainingInstance ti " +
                        "WHERE ti.id = :trainingInstanceId"
        )
})
public class UserRef extends AbstractEntity<Long> {

    @Column(name = "user_ref_id", nullable = false)
    private Long userRefId;
    @ManyToMany(mappedBy = "organizers", fetch = FetchType.LAZY)
    private Set<TrainingInstance> trainingInstances = new HashSet<>();
    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    private Set<TrainingDefinition> trainingDefinitions = new HashSet<>();
    @ManyToMany(mappedBy = "organizers", fetch = FetchType.LAZY)
    private Set<BetaTestingGroup> betaTesters = new HashSet<>();

    /**
     * Instantiates a new user reference
     */
    public UserRef() {
    }

    /**
     * Gets unique identification number of user reference
     *
     * @return the id
     */
    public Long getId() {
        return super.getId();
    }

    /**
     * Sets unique identification number of user reference
     *
     * @param id the id
     */
    public void setId(Long id) {
        super.setId(id);
    }

    /**
     * Gets user ref id.
     *
     * @return the user ref id
     */
    public Long getUserRefId() {
        return userRefId;
    }

    /**
     * Sets user ref id.
     *
     * @param userRefId the user ref id
     */
    public void setUserRefId(Long userRefId) {
        this.userRefId = userRefId;
    }

    /**
     * Gets set of training instances user can access
     *
     * @return the training instances
     */
    public Set<TrainingInstance> getTrainingInstances() {
        return Collections.unmodifiableSet(trainingInstances);
    }

    /**
     * Sets set of training instances user can access
     *
     * @param trainingInstances the training instances
     */
    public void setTrainingInstances(Set<TrainingInstance> trainingInstances) {
        this.trainingInstances = trainingInstances;
    }

    /**
     * Gets set of training definitions user can access
     *
     * @return the training definitions
     */
    public Set<TrainingDefinition> getTrainingDefinitions() {
        return Collections.unmodifiableSet(trainingDefinitions);
    }

    /**
     * Sets set of training definitions user can access
     *
     * @param trainingDefinitions the training definitions
     */
    public void setTrainingDefinitions(Set<TrainingDefinition> trainingDefinitions) {
        this.trainingDefinitions = trainingDefinitions;
    }

    /**
     * Adds definition to the set of training definitions user can access
     *
     * @param trainingDefinition the training definition
     */
    public void addTrainingDefinition(TrainingDefinition trainingDefinition) {
        this.trainingDefinitions.add(trainingDefinition);
    }

    /**
     * Removes definition from the set of training definitions user can access
     *
     * @param trainingDefinition the training definition
     */
    public void removeTrainingDefinition(TrainingDefinition trainingDefinition) {
        this.trainingDefinitions.remove(trainingDefinition);
    }

    /**
     * Adds beta testing group that can be accessed by user
     *
     * @param viewGroup the view group
     */
    public void addViewGroup(BetaTestingGroup viewGroup) {
        this.betaTesters.add(viewGroup);
    }

    /**
     * Removes beta testing group that can be accessed by user
     *
     * @param viewGroup the view group
     */
    public void removeViewGroup(BetaTestingGroup viewGroup) {
        this.betaTesters.remove(viewGroup);
    }

    /**
     * Adds instance to the set of training instances user can access
     *
     * @param trainingInstance the training instance
     */
    public void addTrainingInstance(TrainingInstance trainingInstance) {
        this.trainingInstances.add(trainingInstance);
    }

    /**
     * Removes instance from the set of training instances user can access
     *
     * @param trainingInstance the training instance
     */
    public void removeTrainingInstance(TrainingInstance trainingInstance) {
        this.trainingInstances.remove(trainingInstance);
    }

    /**
     * Gets set of Beta testing groups user can access
     *
     * @return the beta testers
     */
    public Set<BetaTestingGroup> getBetaTesters() {
        return Collections.unmodifiableSet(betaTesters);
    }

    /**
     * Sets set of Beta testing groups user can access
     *
     * @param betaTesters the beta testers
     */
    public void setBetaTesters(Set<BetaTestingGroup> betaTesters) {
        this.betaTesters = betaTesters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRef)) return false;
        UserRef userRef = (UserRef) o;
        return Objects.equals(getUserRefId(), userRef.getUserRefId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserRefId());
    }

    @Override
    public String toString() {
        return "UserRef{" +
                "id=" + super.getId() +
                ", userRefId=" + userRefId +
                '}';
    }
}