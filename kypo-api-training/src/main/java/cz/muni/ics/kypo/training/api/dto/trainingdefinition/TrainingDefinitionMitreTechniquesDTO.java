package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingDefinitionMitreTechniqueDTO", description = "Represent training definition and all used mitre techniques.")
public class TrainingDefinitionMitreTechniquesDTO {

    @ApiModelProperty(value = "Unique identifier of the training definition.", example = "Delta")
    private Long id;
    @ApiModelProperty(value = "Title of the training definition.", example = "Delta")
    private String title;
    @ApiModelProperty(value = "Indicates if the training definition has been played by user.", example = "true")
    private boolean played;
    @ApiModelProperty(value = "List of MITRE technique keys.", example = "[TA0043.T1595, TA0042.T1588.006]")
    private List<String> mitreTechniques;
}
