package cz.muni.ics.kypo.training.api.dto.traininglevel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;
import java.util.List;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "ReferenceSolutionNodeDTO", description = "Definition of the node in the reference graph..")
public class ReferenceSolutionNodeDTO {

    @ApiModelProperty(value = "The name of the node in the graph.", required = true, example = "20")
    @NotNull(message = "{referenceSolutionNode.stateName.NotEmpty.message}")
    private String stateName;
    @ApiModelProperty(value = "The list of the states (nodes) that must be reached before this node.", required = true, example = "20")
    @NotNull(message = "{referenceSolutionNode.prereqStates.NotNull.message}")
    private List<String> prereqState;
    @ApiModelProperty(value = "The command which should be executed during the training.", required = true, example = "20")
    @NotNull(message = "{referenceSolutionNode.cmd.NotEmpty.message}")
    private String cmd;
    @ApiModelProperty(value = "The type of the command (bash-command and msf-command are allowed for now).", required = true, example = "20")
    @NotNull(message = "{referenceSolutionNode.cmdType.NotEmpty.message}")
    private String cmdType;
    @ApiModelProperty(value = "Regular expression used to determine if execution of the command shift trainee to the node..", required = true, example = "20")
    @NotNull(message = "{referenceSolutionNode.cmdRegex.NotEmpty.message}")
    private String cmdRegex;
    @ApiModelProperty(value = "Mark if the node is optional in the training progress. Default is 'false'", required = false, example = "20")
    private boolean optional;
}
