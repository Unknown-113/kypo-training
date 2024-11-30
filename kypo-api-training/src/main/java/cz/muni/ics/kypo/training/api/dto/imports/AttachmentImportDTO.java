package cz.muni.ics.kypo.training.api.dto.imports;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(value = "AttachmentImportDTO", description = "Imported attachment.")
public class AttachmentImportDTO {
    @ApiModelProperty(value = "URL link to file or website.")
    @NotEmpty(message = "{attachment.content.NotEmpty.message}")
    private String content;
}
