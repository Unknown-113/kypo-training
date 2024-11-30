package cz.muni.ics.kypo.training.api.dto.run;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.enums.TRState;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import lombok.*;

/**
 * Encapsulates information about Training Run.
 *
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(value = "TrainingRunDTO", description = "The act, or a recording, of performing actions during training from a perspective of one concrete participant.")
public class TrainingRunDTO {

    @ApiModelProperty(value = "Main identifier of training run.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Date when training run started.", example = "2016-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime startTime;
    @ApiModelProperty(value = "Date when training run ends.", example = "2022-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime endTime;
    private String eventLogReference;
    @ApiModelProperty(value = "Current state of training run.", example = "ALLOCATED")
    private TRState state;
    @ApiModelProperty(value = "Reference to the received sandbox.")
    private String sandboxInstanceRefId;
    @ApiModelProperty(value = "Allocation id to the received sandbox.")
    private Integer sandboxInstanceAllocationId;
    @ApiModelProperty(value = "Reference to participant of training run.")
    private UserRefDTO participantRef;
    @ApiModelProperty(value = "Boolean to check whether event logging works.", example = "true")
    private boolean eventLoggingState;
    @ApiModelProperty(value = "Boolean to check whether command logging works.", example = "true")
    private boolean commandLoggingState;
    @ApiModelProperty(value = "Boolean to check whether the run has any detection events logged", example = "true")
    private boolean hasDetectionEvent;
}
