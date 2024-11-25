package cz.muni.ics.kypo.training.api.dto.accesslevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;
import lombok.*;

/**
 * Encapsulates information needed to update training level.
 *
 */
@Getter
@Setter
@ToString
@ApiModel(value = "AccessLevelUpdateDTO", description = "Access level to update.")
public class AccessLevelUpdateDTO extends AbstractLevelUpdateDTO {

    @ApiModelProperty(value = "Keyword found in training, used for access next level.", required = true, example = "secretAnswer")
    @Size(max = 50, message = "{accessLevel.passkey.Size.message}")
    @NotEmpty(message = "{accessLevel.passkey.NotEmpty.message}")
    private String passkey;
    @ApiModelProperty(value = "The instructions on how to connect to the machine in cloud environment.", example = "Connect using SSH config.")
    @NotEmpty(message = "{accessLevel.cloudContent.NotEmpty.message}")
    private String cloudContent;
    @ApiModelProperty(value = "The instructions on how to connect to the machine in local (non-cloud) environment.", example = "Use vagrant SSH connection.")
    @NotEmpty(message = "{accessLevel.localContent.NotEmpty.message}")
    private String localContent;

    public AccessLevelUpdateDTO() {
        this.levelType = LevelType.ACCESS_LEVEL;
    }
}
