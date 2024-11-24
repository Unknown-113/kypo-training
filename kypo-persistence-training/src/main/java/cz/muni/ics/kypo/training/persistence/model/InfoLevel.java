package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import lombok.*;

/**
 * Class specifying Abstract level as Info level.
 * Info levels contain information for trainees.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "info_level")
@PrimaryKeyJoinColumn(name = "id")
public class InfoLevel extends AbstractLevel {

    @Lob
    @Column(name = "content", nullable = false)
    private String content;
}
