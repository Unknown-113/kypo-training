package cz.muni.ics.kypo.training.api.dto.imports;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import lombok.*;

/**
 * The type Info level import dto. * Encapsulates information about info level. Inherits from {@link AbstractLevelImportDTO}
 *
 */
@Getter
@Setter
@ToString
@ApiModel(value = "InfoLevelImportDTO", description = "An imported info level.", parent = AbstractLevelImportDTO.class)
public class InfoLevelImportDTO extends AbstractLevelImportDTO{

	@ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Informational stuff")
	@NotEmpty(message = "{infoLevel.content.NotEmpty.message}")
	private String content;
}
