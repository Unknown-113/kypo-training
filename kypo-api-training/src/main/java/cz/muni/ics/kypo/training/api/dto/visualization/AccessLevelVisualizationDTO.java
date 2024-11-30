package cz.muni.ics.kypo.training.api.dto.visualization;

import cz.muni.ics.kypo.training.api.dto.export.AbstractLevelExportDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.*;

/**
 * Encapsulates information about access level. Inherits from {@link AbstractLevelVisualizationDTO}
 * Used for visualization.
 */
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@ApiModel(value = "AccessLevelVisualizationDTO", description = "Information about access level needed for visualizations.", parent = AbstractLevelExportDTO.class)
public class AccessLevelVisualizationDTO extends AbstractLevelVisualizationDTO {

    @ApiModelProperty(value = "Keyword found in training, used for access next level.", example = "secretAnswer")
    private String passkey;
    @ApiModelProperty(value = "The instructions on how to connect to the machine in cloud environment.", example = "Connect using SSH config.")
    private String cloudContent;
    @ApiModelProperty(value = "The instructions on how to connect to the machine in local (non-cloud) environment.", example = "Use vagrant SSH connection.")
    private String localContent;
}
