package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import cz.muni.ics.kypo.training.api.enums.CommandType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(value = "DetectedForbiddenCommandDTO", description = "Basic information about detected forbidden command.")
public class DetectedForbiddenCommandDTO {

    @ApiModelProperty(value = "Command.", example = "nmap")
    private String command;
    @ApiModelProperty(value = "Type of command.", example = "BASH")
    private CommandType type;
    @ApiModelProperty(value = "Hostname.", example = "attacker")
    private String hostname;
    @ApiModelProperty(value = "When the command was submitted", example = "1.1.2022 5:55:23")
    private LocalDateTime occurredAt;
}