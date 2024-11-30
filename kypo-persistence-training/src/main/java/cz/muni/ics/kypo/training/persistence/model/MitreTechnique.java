package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

/**
 * Class representing mitre technique used in Training Level.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mitre_technique")
@NamedQueries({
        @NamedQuery(
                name = "MitreTechnique.findByTechniqueKey",
                query = "SELECT mt FROM MitreTechnique mt WHERE mt.techniqueKey = :techniqueKey"
        ),
})
public class MitreTechnique extends AbstractEntity<Long> {

    @ManyToMany(mappedBy = "mitreTechniques")
    private Set<TrainingLevel> trainingLevels = new HashSet<>();
    @Column(name = "technique_key", nullable = false, unique = true)
    private String techniqueKey;

    public void addTrainingLevel(TrainingLevel trainingLevel) {
        this.trainingLevels.add(trainingLevel);
    }

    public void removeTrainingLevel(TrainingLevel trainingLevel) {
        this.trainingLevels.remove(trainingLevel);
    }
}
