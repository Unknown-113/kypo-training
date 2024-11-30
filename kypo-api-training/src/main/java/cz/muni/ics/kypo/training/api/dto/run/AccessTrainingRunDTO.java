package cz.muni.ics.kypo.training.api.dto.run;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.hint.TakenHintDTO;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/**
 * Encapsulates information about Training Run, intended as a response to run accessing.
 */
@Getter
@Setter
@ToString
@ApiModel(value = "AccessTrainingRunDTO", description = "Just accessed training run.")
public class AccessTrainingRunDTO {

    @ApiModelProperty(value = "Main identifier of training run.", example = "1")
    private Long trainingRunID;
    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", example = "false")
    private boolean showStepperBar;
    @ApiModelProperty(value = "Main identifier of sandbox which is assigned to training run.", example = "2")
    private String sandboxInstanceRefId;
    @ApiModelProperty(value = "First level in the current training run.")
    private AbstractLevelDTO abstractLevelDTO;
    @ApiModelProperty(value = "Information about all levels in training instance.")
    private List<BasicLevelInfoDTO> infoAboutLevels;
    @ApiModelProperty(value = "Id of associated training instance", example = "1")
    private Long instanceId;
    @ApiModelProperty(value = "Date when training run started.", example = "2016-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime startTime;
    @ApiModelProperty(value = "Sign if solution of current training level was taken", example = "true")
    private String takenSolution;
    @ApiModelProperty(value = "All already taken hints.")
    private List<TakenHintDTO> takenHints = new ArrayList<>();
    @ApiModelProperty(value = "Indicates if local sandboxes are used for training runs.", example = "true")
    private boolean localEnvironment;
    @ApiModelProperty(value = "Main identifier of sandbox definition which is assigned to training instance of the training run.", example = "2")
    private Long sandboxDefinitionId;
    @ApiModelProperty(value = "Indicates if trainee can during training run move to the previous already solved levels.", example = "true")
    private boolean backwardMode;
    @ApiModelProperty(value = "Indicates if the current level has been already corrected/answered.", example = "true")
    private boolean isLevelAnswered;

    /**
     * Add taken hint to list of taken hints.
     *
     * @param takenHintDTO the {@link TakenHintDTO}
     */
    public void addTakenHint(TakenHintDTO takenHintDTO) {
        this.takenHints.add(takenHintDTO);
    }
}
