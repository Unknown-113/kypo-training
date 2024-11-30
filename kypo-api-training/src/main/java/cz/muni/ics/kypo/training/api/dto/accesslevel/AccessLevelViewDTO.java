package cz.muni.ics.kypo.training.api.dto.accesslevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * Encapsulates basic information about access level.
 */
@Getter
@Setter
@ToString
@ApiModel(value = "AccessLevelViewDTO", description = "A level containing instructions on how to connect to the virtual machines.", parent = AbstractLevelDTO.class)
public class AccessLevelViewDTO extends AbstractLevelDTO {

    @ApiModelProperty(value = "The instructions on how to connect to the machine in cloud environment.", example = "Connect using SSH config.")
    private String cloudContent;
    @ApiModelProperty(value = "The instructions on how to connect to the machine in local (non-cloud) environment.", example = "Use vagrant SSH connection.")
    private String localContent;
}
