package cz.muni.ics.kypo.training.api.dto.infolevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * Encapsulates information about info level. Inherits from {@link AbstractLevelDTO}
 *
 */
@Getter
@Setter
@ToString
@ApiModel(value = "InfoLevelDTO", description = "A HTML content for the participant to read.", parent = AbstractLevelDTO.class)
public class InfoLevelDTO extends AbstractLevelDTO {

    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Informational stuff")
    private String content;
}
