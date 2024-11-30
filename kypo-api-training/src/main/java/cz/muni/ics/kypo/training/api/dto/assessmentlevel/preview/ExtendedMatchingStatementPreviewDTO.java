package cz.muni.ics.kypo.training.api.dto.assessmentlevel.preview;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.muni.ics.kypo.training.validation.Ordered;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtendedMatchingStatementPreviewDTO implements Ordered {

    @ApiModelProperty(value = "Main identifier of the extended matching statement.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Text content of the extended matching statement.", example = "SSH")
    @NotEmpty(message = "{emiStatement.text.NotEmpty.message}")
    private String text;
    @ApiModelProperty(value = "The order of the statement in question of type EMI.", example = "0")
    @Min(value = 0, message = "{emiStatement.order.Min.message}")
    private int order;
    @ApiModelProperty(value = "The order of the user chosen option from the list of extended matching options.", example = "0")
    private Integer userOptionOrder;
}
