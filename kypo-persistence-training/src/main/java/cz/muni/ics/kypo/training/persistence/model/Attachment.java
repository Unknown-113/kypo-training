package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Class representing attachments of Training Level
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attachment")
public class Attachment extends AbstractEntity<Long> {

    @Column(name = "content", nullable = false)
    private String content;
    @Column(name = "creation_time", nullable = false)
    private LocalDateTime creationTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_level_id")
    private TrainingLevel trainingLevel;
}

