package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Group of users that can test Training runs created from unreleased Training Definition
 */
@Entity
@Table(name = "beta_testing_group")
public class BetaTestingGroup extends AbstractEntity<Long> {

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "beta_testing_group_user_ref",
            joinColumns = @JoinColumn(name = "beta_testing_group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_ref_id")
    )
    private Set<UserRef> organizers = new HashSet<>();
    @OneToOne(mappedBy = "betaTestingGroup", fetch = FetchType.LAZY)
    private TrainingDefinition trainingDefinition;

    /**
     * Gets unique identification number of beta testing group
     *
     * @return the id
     */
    public Long getId() {
        return super.getId();
    }

    /**
     * Sets unique identification number of beta testing group
     *
     * @param id the id
     */
    public void setId(Long id) {
        super.setId(id);
    }

    /**
     * Gets set of users allowed to test associated Training Definition
     *
     * @return the organizers
     */
    public Set<UserRef> getOrganizers() {
        return Collections.unmodifiableSet(organizers);
    }

    /**
     * Sets set of users allowed to test associated Training Definition
     *
     * @param organizers the organizers
     */
    public void setOrganizers(Set<UserRef> organizers) {
        this.organizers = organizers;
    }

    /**
     * Adds organizer to set of users allowed to test associated Training Definition
     *
     * @param organizer to be added
     */
    public void addOrganizer(UserRef organizer) {
        this.organizers.add(organizer);
        organizer.addViewGroup(this);
    }

    /**
     * Removes organizer from set of users allowed to test associated Training Definition
     *
     * @param organizer to be removed
     */
    public void removeOrganizer(UserRef organizer) {
        this.organizers.remove(organizer);
        organizer.removeViewGroup(this);
    }

    /**
     * Gets associated Training Definition
     *
     * @return the training definition
     */
    public TrainingDefinition getTrainingDefinition() {
        return trainingDefinition;
    }

    /**
     * Sets associated Training Definition
     *
     * @param trainingDefinition the training definition
     */
    public void setTrainingDefinition(TrainingDefinition trainingDefinition) {
        this.trainingDefinition = trainingDefinition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BetaTestingGroup)) return false;
        BetaTestingGroup that = (BetaTestingGroup) o;
        return Objects.equals(getOrganizers(), that.getOrganizers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrganizers());
    }

    @Override
    public String toString() {
        return "BetaTestingGroup{" +
                "id=" + super.getId() +
                '}';
    }
}