package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.persistence.model.AbstractEntity;
import cz.muni.ics.kypo.training.persistence.model.enums.CommandType;

import javax.persistence.*;
import java.util.Objects;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@Entity
@Table(name = "forbidden_command")
public class ForbiddenCommand extends AbstractEntity<Long> {

    @Column(name = "command", nullable = false)
    private String command;
    @Column(name = "command_type", nullable = false)
    private CommandType type;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cheating_detection_id")
    private CheatingDetection cheatingDetection;
}
