package cz.muni.ics.kypo.training.api.dto.visualization;/**
     * Gets assessment type.
     *
     * @return the {@link AssessmentType}
     */

import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.muni.ics.kypo.training.api.dto.imports.AbstractLevelImportDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import lombok.*;

/**
 * Encapsulates information about assessment level. Inherits from {@link AbstractLevelVisualizationDTO}
 * Used for visualization.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(value = "AssessmentLevelVisualizationDTO", description = "Information about assessment level needed for visualizations.", parent = AbstractLevelImportDTO.class)
public class AssessmentLevelVisualizationDTO extends AbstractLevelVisualizationDTO{

    @ApiModelProperty(value = "Type of assessment.", example = "TEST")
    private AssessmentType assessmentType;
    @ApiModelProperty(value = "List of questions in this assessment as JSON.", example = "What is my mothers name?")
    private List<QuestionDTO> questions;
}
