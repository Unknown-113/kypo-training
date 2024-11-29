package cz.muni.ics.kypo.training.api.dto.assessmentlevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import lombok.*;

/**
 * Encapsulates information about assessment level. Inherits from {@link AbstractLevelDTO}
 *
 */
@Getter
@Setter
@ToString
@ApiModel(value = "AssessmentLevelDTO", description = "A questionnaire or a test that is displayed to the participant.", parent = AbstractLevelDTO.class)
public class AssessmentLevelDTO extends AbstractLevelDTO {

    @ApiModelProperty(value = "List of questions in this assessment as JSON.", example = "What is my mothers name?")
    private List<QuestionDTO> questions;
    @ApiModelProperty(value = "Assessment instructions for participant.", example = "Fill me up")
    private String instructions;
    @ApiModelProperty(value = "Type of assessment.", example = "TEST")
    private AssessmentType assessmentType;
}
