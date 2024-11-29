package cz.muni.ics.kypo.training.api.dto.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * Encapsulates information about training level. Inherits from {@link AbstractLevelExportDTO}
 *
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(value = "AccessLevelExportDTO", description = "Exported access level.", parent = AbstractLevelExportDTO.class)
public class AccessLevelExportDTO extends AbstractLevelExportDTO {

    @ApiModelProperty(value = "Keyword used for access next level.", example = "secretAnswer")
    private String passkey;
    @ApiModelProperty(value = "The instructions on how to connect to the machine in cloud environment.", example = "Connect using SSH config.")
    private String cloudContent;
    @ApiModelProperty(value = "The instructions on how to connect to the machine in local (non-cloud) environment.", example = "Use vagrant SSH connection.")
    private String localContent;
}
