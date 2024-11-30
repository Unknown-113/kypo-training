package cz.muni.ics.kypo.training.api.dto.imports;

import cz.muni.ics.kypo.training.api.dto.export.AbstractLevelExportDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;
import lombok.*;

/**
 * Encapsulates information about training level. Inherits from {@link AbstractLevelImportDTO}
 *
 */
@Getter
@Setter
@ToString
@ApiModel(value = "AccessLevelImportDTO", description = "Imported access level.", parent = AbstractLevelExportDTO.class)
public class AccessLevelImportDTO extends AbstractLevelImportDTO {

	@ApiModelProperty(value = "Keyword used for access next level.", example = "secretAnswer")
	@Size(max = 50, message = "{accessLevel.passkey.Size.message}")
	@NotEmpty(message = "{accessLevel.passkey.NotEmpty.message}")
	private String passkey;
	@ApiModelProperty(value = "The instructions on how to connect to the machine in cloud environment.", example = "Connect using SSH config.")
	@NotEmpty(message = "{accessLevel.cloudContent.NotEmpty.message}")
	private String cloudContent;
	@ApiModelProperty(value = "The instructions on how to connect to the machine in local (non-cloud) environment.", example = "Use vagrant SSH connection.")
	@NotEmpty(message = "{accessLevel.localContent.NotEmpty.message}")
	private String localContent;
}
