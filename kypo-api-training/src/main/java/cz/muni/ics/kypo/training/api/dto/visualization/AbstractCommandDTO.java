package cz.muni.ics.kypo.training.api.dto.visualization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@ApiModel(value = "AbstractCommandDTO", subTypes = {CommandDTO.class},
        description = "CommandDTO")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CommandDTO.class, name = "CommandDTO")})
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractCommandDTO {

    @ApiModelProperty(value = "Distinguish tool/command line  in which command ran.", required = true, example = "BASH")
    @NotEmpty
    @JsonProperty("command_type")
    public String commandType;

    @ApiModelProperty(value = "Command without arguments/options.", required = true, example = "ls")
    @NotEmpty
    public String cmd;
}
