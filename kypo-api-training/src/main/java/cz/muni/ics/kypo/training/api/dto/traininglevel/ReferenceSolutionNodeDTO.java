package cz.muni.ics.kypo.training.api.dto.traininglevel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;
import java.util.List;
import java.util.Objects;

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

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public List<String> getPrereqState() {
        return prereqState;
    }

    public void setPrereqState(List<String> prereqState) {
        this.prereqState = prereqState;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getCmdType() {
        return cmdType;
    }

    public void setCmdType(String cmdType) {
        this.cmdType = cmdType;
    }

    public String getCmdRegex() {
        return cmdRegex;
    }

    public void setCmdRegex(String cmdRegex) {
        this.cmdRegex = cmdRegex;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReferenceSolutionNodeDTO)) return false;
        ReferenceSolutionNodeDTO that = (ReferenceSolutionNodeDTO) o;
        return isOptional() == that.isOptional() && Objects.equals(getStateName(), that.getStateName())
                && Objects.equals(getPrereqState(), that.getPrereqState())
                && Objects.equals(getCmd(), that.getCmd())
                && Objects.equals(getCmdType(), that.getCmdType())
                && Objects.equals(getCmdRegex(), that.getCmdRegex());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStateName(), getPrereqState(), getCmd(), getCmdType(), getCmdRegex(), isOptional());
    }

    @Override
    public String toString() {
        return "ReferenceSolutionNodeDTO{" +
                "stateName='" + stateName + '\'' +
                ", prereqStates=" + prereqState +
                ", cmd='" + cmd + '\'' +
                ", cmdType='" + cmdType + '\'' +
                ", cmdRegex='" + cmdRegex + '\'' +
                ", optional=" + optional +
                '}';
    }
}
