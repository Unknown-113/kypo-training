package cz.muni.ics.kypo.training.api.dto.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(value = "AttachmentExportDTO", description = "An exported attachment of training level.")
public class AttachmentExportDTO {

    @ApiModelProperty(value = "URL link to file or website.")
    private String content;
}
