package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import lombok.*;

/**
 * Class representing hints associated with training level that can be displayed by trainee if they are in need of help with
 * solving given level
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hint")
@NamedQueries({
        @NamedQuery(
                name = "Hint.deleteHintsByLevelId",
                query = "DELETE FROM Hint h WHERE h.trainingLevel.id = :levelId"
        ),
})
public class Hint extends AbstractEntity<Long> {

    @Column(name = "title", nullable = false)
    private String title;
    @Lob
    @Column(name = "content", nullable = false)
    private String content;
    @Column(name = "hint_penalty", nullable = false)
    private Integer hintPenalty;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_level_id")
    private TrainingLevel trainingLevel;
    @Column(name = "order_in_level", nullable = false)
    private int order;
}
