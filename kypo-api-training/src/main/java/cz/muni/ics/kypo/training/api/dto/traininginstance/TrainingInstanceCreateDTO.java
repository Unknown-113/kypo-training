package cz.muni.ics.kypo.training.api.dto.traininginstance;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCDeserializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Encapsulates information about Training Instance, intended for creation of new instance.
 *
 */
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingInstanceCreateDTO", description = "Training Instance to create.")
public class TrainingInstanceCreateDTO {

    @ApiModelProperty(value = "Date when training instance starts.", required = true, example = "2020-11-20T10:28:02.727Z")
    @NotNull(message = "{trainingInstance.startTime.NotNull.message}")
    @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
    private LocalDateTime startTime;
    @ApiModelProperty(value = "Date when training instance ends.", required = true, example = "2020-11-25T10:26:02.727Z")
    @NotNull(message = "{traininginstancecreate.endTime.NotNull.message}")
    @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
    private LocalDateTime endTime;
    @ApiModelProperty(value = "Short textual description of the training instance.", required = true, example = "December instance")
    @NotEmpty(message = "{traininginstancecreate.title.NotEmpty.message}")
    private String title;
    @ApiModelProperty(value = "AccessToken which will be modified and then used for accessing training run.", required = true, example = "hunter")
    @NotEmpty(message = "{traininginstancecreate.accessToken.NotEmpty.message}")
    private String accessToken;
    @ApiModelProperty(value = "Reference to training definition from which is training instance created.", required = true, example = "1")
    @NotNull(message = "{traininginstancecreate.trainingDefinition.NotNull.message}")
    private long trainingDefinitionId;
    @ApiModelProperty(value = "Id of sandbox pool assigned to training instance", example = "1")
    private Long poolId;
    @ApiModelProperty(value = "Indicates if local sandboxes are used for training runs.", example = "true")
    private boolean localEnvironment;
    @ApiModelProperty(value = "Id of sandbox definition assigned to training instance", example = "1")
    private Long sandboxDefinitionId;
    @ApiModelProperty(value = "Indicates if trainee can during training run move to the previous already solved levels.", example = "true")
    private boolean backwardMode;
}
