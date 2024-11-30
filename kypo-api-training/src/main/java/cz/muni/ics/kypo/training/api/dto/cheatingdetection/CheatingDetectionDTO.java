package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.enums.CheatingDetectionState;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.*;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(value = "CheatingDetectionDTO", description = "Basic information about cheating detection.")
public class CheatingDetectionDTO {

    @ApiModelProperty(value = "id of a training instance in which the event was detected.", example = "1")
    private Long trainingInstanceId;
    @ApiModelProperty(value = "Name of user who executed the detection.", example = "John Doe")
    private String executedBy;
    @ApiModelProperty(value = "Time when the cheating detection was executed.", example = "1.1.2022 5:55:23")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime executeTime;
    @ApiModelProperty(value = "Proximity threshold for time proximity cheat.", example = "120")
    private Long proximityThreshold;
    @ApiModelProperty(value = "id of cheating detection.", example = "1")
    private Long id;
    @ApiModelProperty(value = "State of the detection.", example = "RUNNING")
    private CheatingDetectionState currentState;
    @ApiModelProperty(value = "Number of detected events in detection.", example = "20")
    private Long results;
    @ApiModelProperty(value = "state of detection run of answer similarity.", example = "RUNNING")
    private CheatingDetectionState answerSimilarityState;
    @ApiModelProperty(value = "state of detection run of location_similarity.", example = "RUNNING")
    private CheatingDetectionState locationSimilarityState;
    @ApiModelProperty(value = "state of detection run of time proximity.", example = "RUNNING")
    private CheatingDetectionState timeProximityState;
    @ApiModelProperty(value = "state of detection run of minimal solve time.", example = "RUNNING")
    private CheatingDetectionState minimalSolveTimeState;
    @ApiModelProperty(value = "state of detection run of forbidden commands.", example = "RUNNING")
    private CheatingDetectionState forbiddenCommandsState;
    @ApiModelProperty(value = "state of detection run of no commands.", example = "RUNNING")
    private CheatingDetectionState noCommandsState;
    @ApiModelProperty(value = "list of forbidden commands.", example = "[]")
    private List<ForbiddenCommandDTO> forbiddenCommands;
}
