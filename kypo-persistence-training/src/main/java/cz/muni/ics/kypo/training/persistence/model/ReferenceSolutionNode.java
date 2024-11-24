package cz.muni.ics.kypo.training.persistence.model;

import java.util.List;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
public class ReferenceSolutionNode {

    private String stateName;
    private List<String> prereqState;
    private String cmd;
    private String cmdType;
    private String cmdRegex;
    private boolean optional;
}
