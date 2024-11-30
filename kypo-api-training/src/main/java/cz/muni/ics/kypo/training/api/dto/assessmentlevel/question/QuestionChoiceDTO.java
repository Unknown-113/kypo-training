package cz.muni.ics.kypo.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.muni.ics.kypo.training.validation.Ordered;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionChoiceDTO implements Serializable, Ordered {

    @ApiModelProperty(value = "Main identifier of the question choice.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Text content of the question choice.", example = "Yes")
    @NotEmpty(message = "{questionChoice.text.NotEmpty.message}")
    private String text;
    @ApiModelProperty(value = "Sign if the choice is correct.", example = "true")
    @NotNull(message = "{questionChoice.correct.NotNull.message}")
    private Boolean correct;
    @ApiModelProperty(value = "The order of the choice in question of type MCQ or FFQ", example = "1")
    @Min(value = 0, message = "{questionChoice.order.Min.message}")
    private int order;
}
