package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.*;

/**
 * Class represents information of hint associated with current level of training run
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class HintInfo {
    @Column(name = "training_level_id", nullable = false)
    private Long trainingLevelId;
    @Column(name = "hint_id", nullable = false)
    private long hintId;
    @Column(name = "hint_title", nullable = false)
    private String hintTitle;
    @Column(name = "hint_content", nullable = false)
    private String hintContent;
    @Column(name = "order_in_level", nullable = false)
    private int order;
}
