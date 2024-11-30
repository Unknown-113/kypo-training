package cz.muni.ics.kypo.training.api.dto.imports;

import cz.muni.ics.kypo.training.api.enums.TDState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/**
 * Encapsulates information about training definition and its levels.
 *
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(value = "ImportTrainingDefinitionDTO", description = "A basic information about hint.")
public class ImportTrainingDefinitionDTO {

	@ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", example = "TrainingDefinition2")
	@NotEmpty(message = "{trainingDefinition.title.NotEmpty.message}")
	private String title;
	@ApiModelProperty(value = "Description of training definition that is visible to the participant.", example = "Unreleased training definition")
	private String description;
	@ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.", example = "")
	private String[] prerequisites;
	@ApiModelProperty(value = "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ", example = "")
	private String[] outcomes;
	@ApiModelProperty(value = "Current state of training definition.", example = "UNRELEASED")
	@NotNull(message = "{trainingDefinition.state.NotNull.message}")
	private TDState state;
	@ApiModelProperty(value = "Sign if stepper bar should be displayed.", example = "false")
	@NotNull(message = "{trainingDefinition.showStepperBar.NotNull.message}")
	private boolean showStepperBar;
	@Valid
	@ApiModelProperty(value = "Information about all levels in training definition.")
	private List<AbstractLevelImportDTO> levels = new ArrayList<>();
	@ApiModelProperty(value = "Estimated time it takes to finish runs created from this definition.", example = "5")
	private Integer estimatedDuration;
	@ApiModelProperty(value = "Marking if levels flags/answers are randomly generated and are different for each trainee. Default is false.", example = "false")
	private boolean variantSandboxes;

	/**
	 * Sets levels.
	 *
	 * @param levels the list of {@link AbstractLevelImportDTO}
	 */
	public void setLevels(List<AbstractLevelImportDTO> levels) {
		this.levels = new ArrayList<>(levels);
	}
}
