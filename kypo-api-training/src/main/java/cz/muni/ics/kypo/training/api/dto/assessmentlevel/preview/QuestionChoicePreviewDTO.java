package cz.muni.ics.kypo.training.api.dto.assessmentlevel.preview;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.muni.ics.kypo.training.validation.Ordered;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionChoicePreviewDTO implements Serializable, Ordered {

    @ApiModelProperty(value = "Main identifier of the question choice.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Text content of the question choice.", example = "Yes")
    private String text;
    @ApiModelProperty(value = "The order of the choice in question of type MCQ or FFQ", example = "1")
    private int order;
}
