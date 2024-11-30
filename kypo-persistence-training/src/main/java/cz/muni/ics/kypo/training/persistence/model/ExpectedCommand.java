package cz.muni.ics.kypo.training.persistence.model;


import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.*;

/**
 * Class specifying command to be executed during the training level.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@Embeddable
public class ExpectedCommand {

    @Column(name = "command")
    private String command;
}
