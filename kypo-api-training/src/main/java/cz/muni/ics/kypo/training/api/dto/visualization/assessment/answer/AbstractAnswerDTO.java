package cz.muni.ics.kypo.training.api.dto.visualization.assessment.answer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


@ApiModel(value = "AbstractAnswerDTO", subTypes = {EMIAnswerDTO.class, FFQAnswerDTO.class, MCQAnswerDTO.class},
        description = "Superclass for classes EMIAnswerDTO, FFQAnswerDTO, MCQAnswerDTO")
@SuperBuilder
@EqualsAndHashCode
@Getter
@Setter
@ToString
public abstract class AbstractAnswerDTO {

    @ApiModelProperty(value = "Text of the answer option", example = "Ubuntu")
    private String text;
}
