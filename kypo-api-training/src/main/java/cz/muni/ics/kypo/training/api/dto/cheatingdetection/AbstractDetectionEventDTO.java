package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

import cz.muni.ics.kypo.training.api.enums.DetectionEventType;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(value = "AbstractDetectionEventDTO", subTypes = {AnswerSimilarityDetectionEventDTO.class,
        ForbiddenCommandsDetectionEventDTO.class,
        LocationSimilarityDetectionEventDTO.class,
        MinimalSolveTimeDetectionEventDTO.class,
        NoCommandsDetectionEventDTO.class,
        TimeProximityDetectionEventDTO.class},
        description = "Superclass for classes AnswerSimilarityDetectionEventDTO, ForbiddenCommandsDetectionEventDTO, LocationSimilarityDetectionEventDTO," +
                "MinimalSolveTimeDetectionEventDTO, NoCommandsDetectionEventDTO and TimeProximityDetectionEventDTO")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AnswerSimilarityDetectionEventDTO.class, name = "AnswerSimilarityDetectionEventDTO"),
        @JsonSubTypes.Type(value = ForbiddenCommandsDetectionEventDTO.class, name = "ForbiddenCommandsDetectionEventDTO"),
        @JsonSubTypes.Type(value = LocationSimilarityDetectionEventDTO.class, name = "LocationSimilarityDetectionEventDTO"),
        @JsonSubTypes.Type(value = MinimalSolveTimeDetectionEventDTO.class, name = "MinimalSolveTimeDetectionEventDTO"),
        @JsonSubTypes.Type(value = NoCommandsDetectionEventDTO.class, name = "NoCommandsDetectionEventDTO"),
        @JsonSubTypes.Type(value = TimeProximityDetectionEventDTO.class, name = "TimeProximityDetectionEventDTO")})
public class AbstractDetectionEventDTO {

    @ApiModelProperty(value = "id of detection event.", example = "1")
    private Long id;
    @ApiModelProperty(value = "id of a training instance in which the event was detected.", example = "1")
    private Long trainingInstanceId;
    @ApiModelProperty(value = "id of a cheating detection during which the event was detected.", example = "2")
    private Long cheatingDetectionId;
    @ApiModelProperty(value = "id of a training run in which the event was detected.", example = "2")
    private Long trainingRunId;
    @ApiModelProperty(value = "Training level id in which the event occurred.", example = "3")
    private Long levelId;
    @ApiModelProperty(value = "Training level order in which the event occurred.", example = "3")
    private int levelOrder;
    @ApiModelProperty(value = "Title of the training level.", example = "SQL injection")
    private String levelTitle;
    @ApiModelProperty(value = "Time at which the event was detected.", example = "1.1.2022 5:55:23")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime detectedAt;
    @ApiModelProperty(value = "number of participants of the event.", example = "3")
    private Long participantCount;
    @ApiModelProperty(value = "type of the event.", example = "answer similarity")
    private DetectionEventType detectionEventType;
    @ApiModelProperty(value = "participants of the event.", example = "John Doe,Jane Doe")
    private String participants;
}
