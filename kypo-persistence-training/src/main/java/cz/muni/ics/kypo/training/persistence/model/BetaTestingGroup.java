package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

/**
 * Group of users that can test Training runs created from unreleased Training Definition
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
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
     * Gets set of users allowed to test associated Training Definition
     *
     * @return the organizers
     */
    public Set<UserRef> getOrganizers() {
        return Collections.unmodifiableSet(organizers);
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
}
