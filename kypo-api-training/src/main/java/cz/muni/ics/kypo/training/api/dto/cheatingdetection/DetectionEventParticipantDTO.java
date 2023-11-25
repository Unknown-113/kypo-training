package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Encapsulates information about detection event participant.
 */
@ApiModel(value = "DetectionEventParticipantDTO", description = "Basic Information about a detection event participant.")
public class DetectionEventParticipantDTO {

    @ApiModelProperty(value = "Ip address of participant.", example = "1.1.1.1")
    private String ipAddress;
    @ApiModelProperty(value = "Time when the event occurred.", example = "1.1.2022 5:55:23")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime occurredAt;
    @ApiModelProperty(value = "Time in which the level was solved.", example = "20")
    private Long solvedInTime;
    @ApiModelProperty(value = "Name of the participant.", example = "John Doe")
    private String participantName;

    @ApiModelProperty(value = "User id of participant.", example = "6")
    private Long userId;
    @ApiModelProperty(value = "the id of detection event", example = "3")
    private Long detectionEventId;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Long getSolvedInTime() {
        return solvedInTime;
    }

    public void setSolvedInTime(Long solvedInTime) {
        this.solvedInTime = solvedInTime;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDetectionEventId() {
        return detectionEventId;
    }

    public void setDetectionEventId(Long detectionEventId) {
        this.detectionEventId = detectionEventId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetectionEventParticipantDTO that = (DetectionEventParticipantDTO) o;
        return Objects.equals(ipAddress, that.ipAddress) &&
                Objects.equals(occurredAt, that.occurredAt) &&
                Objects.equals(solvedInTime, that.solvedInTime) &&
                Objects.equals(participantName, that.participantName) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(detectionEventId, that.detectionEventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddress, occurredAt, solvedInTime, participantName, detectionEventId);
    }

    @Override
    public String toString() {
        return "DetectionEventParticipantDTO{" +
                "ipAddress='" + ipAddress + '\'' +
                ", occurredAt=" + occurredAt +
                ", solvedInTime=" + solvedInTime +
                ", participantName='" + participantName + '\'' +
                ", userId='" + userId + '\'' +
                ", detectionEventId='" + detectionEventId + '\'' +
                '}';
    }
}
