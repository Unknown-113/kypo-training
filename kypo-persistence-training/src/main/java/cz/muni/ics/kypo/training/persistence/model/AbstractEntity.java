package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.io.Serializable;
import lombok.*;

/**
 * The type Abstract entity.
 *
 * @param <PK> Primary key for a given entity.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@MappedSuperclass
public class AbstractEntity<PK extends Serializable> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private PK id;
}
