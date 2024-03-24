package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.accesslevel.AccessLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelDTO;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

import cz.muni.ics.kypo.training.api.enums.DetectionEventType;
import cz.muni.ics.kypo.training.api.enums.CommandType;

import java.util.List;
import java.util.Objects;

/**
 * Encapsulates information about abstract detection event.
 */
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

    /**
     * Gets training instance id.
     *
     * @return the training instance id
     */
    public Long getTrainingInstanceId() {
        return trainingInstanceId;
    }

    /**
     * Sets training instance id.
     *
     * @param id the training instance id
     */
    public void setTrainingInstanceId(Long id) {
        this.trainingInstanceId = id;
    }

    /**
     * Gets cheating detection id.
     *
     * @return the id of cheating detection
     */
    public Long getCheatingDetectionId() {
        return cheatingDetectionId;
    }

    /**
     * Sets cheater id.
     *
     * @param id the cheating detection
     */
    public void setCheatingDetectionId(Long id) {
        this.cheatingDetectionId = id;
    }

    public Long getTrainingRunId() {
        return trainingRunId;
    }

    public void setTrainingRunId(Long trainingRunId) {
        this.trainingRunId = trainingRunId;
    }

    /**
     * Gets level id.
     *
     * @return the id of level
     */
    public Long getLevelId() {
        return levelId;
    }

    /**
     * Sets level id.
     *
     * @param id the level id
     */
    public void setLevelId(Long id) {
        this.levelId = id;
    }

    /**
     * Gets IP.
     *
     * @return the level title
     */
    public String getLevelTitle() {
        return levelTitle;
    }

    /**
     * Sets IP.
     *
     * @param title the level title
     */
    public void setLevelTitle(String title) {
        this.levelTitle = title;
    }

    /**
     * Gets provided.
     *
     * @return the detected at timestamp
     */
    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    /**
     * Sets provided.
     *
     * @param detectedAt the detected at timestamp
     */
    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    /**
     * Gets date.
     *
     * @return the participant count
     */
    public Long getParticipantCount() {
        return participantCount;
    }

    /**
     * Sets date.
     *
     * @param count the participant count
     */
    public void setParticipantCount(Long count) {
        this.participantCount = count;
    }

    /**
     * Gets description.
     *
     * @return the detection event type
     */
    public DetectionEventType getDetectionEventType() {
        return detectionEventType;
    }

    /**
     * Sets description.
     *
     * @param detectionEventType the detection event type
     */
    public void setDetectionEventType(DetectionEventType detectionEventType) {
        this.detectionEventType = detectionEventType;
    }

    public String getParticipants() {
        return participants;
    }

    public void setParticipants(String participants) {
        this.participants = participants;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getLevelOrder() {
        return levelOrder;
    }

    public void setLevelOrder(int levelOrder) {
        this.levelOrder = levelOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractDetectionEventDTO that = (AbstractDetectionEventDTO) o;
        return levelOrder == that.levelOrder &&
                Objects.equals(id, that.id) &&
                Objects.equals(trainingInstanceId, that.trainingInstanceId) &&
                Objects.equals(cheatingDetectionId, that.cheatingDetectionId) &&
                Objects.equals(trainingRunId, that.trainingRunId) &&
                Objects.equals(levelId, that.levelId) &&
                Objects.equals(levelTitle, that.levelTitle) &&
                Objects.equals(detectedAt, that.detectedAt) &&
                Objects.equals(participantCount, that.participantCount) &&
                detectionEventType == that.detectionEventType &&
                Objects.equals(participants, that.participants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, trainingInstanceId, cheatingDetectionId, trainingRunId, levelId, levelOrder, levelTitle, detectedAt, participantCount, detectionEventType, participants);
    }

    @Override
    public String toString() {
        return "AbstractDetectionEventDTO{" +
                "id=" + id +
                ", trainingInstanceId=" + trainingInstanceId +
                ", cheatingDetectionId=" + cheatingDetectionId +
                ", trainingRunId=" + trainingRunId +
                ", levelId=" + levelId +
                ", levelOrder=" + levelOrder +
                ", levelTitle='" + levelTitle + '\'' +
                ", detectedAt=" + detectedAt +
                ", participantCount=" + participantCount +
                ", detectionEventType=" + detectionEventType +
                ", participants='" + participants + '\'' +
                '}';
    }
}
