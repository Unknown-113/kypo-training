package cz.muni.ics.kypo.training.api.dto.run;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.enums.TRState;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Encapsulates information about Training Run.
 *
 */
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
    @ApiModelProperty(value = "Reference to participant of training run.")
    private UserRefDTO participantRef;
    @ApiModelProperty(value = "Boolean to check whether event logging works.", example = "true")
    private boolean eventLoggingState;
    @ApiModelProperty(value = "Boolean to check whether command logging works.", example = "true")
    private boolean commandLoggingState;
    @ApiModelProperty(value = "Boolean to check whether the run has any detection events logged", example = "true")
    private boolean hasDetectionEvent;

    /**
     * Gets id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets start time.
     *
     * @return the start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Sets start time.
     *
     * @param startTime the start time
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets end time.
     *
     * @return the end time
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Sets end time.
     *
     * @param endTime the end time
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets event log reference.
     *
     * @return the event log reference
     */
    public String getEventLogReference() {
        return eventLogReference;
    }

    /**
     * Sets event log reference.
     *
     * @param eventLogReference the event log reference
     */
    public void setEventLogReference(String eventLogReference) {
        this.eventLogReference = eventLogReference;
    }

    /**
     * Gets state.
     *
     * @return the {@link TRState}
     */
    public TRState getState() {
        return state;
    }

    /**
     * Sets state.
     *
     * @param state the {@link TRState}
     */
    public void setState(TRState state) {
        this.state = state;
    }

    /**
     * Gets sandbox instance ref.
     *
     * @return the sandbox instance ref id
     */
    public String getSandboxInstanceRefId() {
        return sandboxInstanceRefId;
    }

    /**
     * Sets sandbox instance ref.
     *
     * @param sandboxInstanceRefId the sandbox instance ref id
     */
    public void setSandboxInstanceRefId(String sandboxInstanceRefId) {
        this.sandboxInstanceRefId = sandboxInstanceRefId;
    }

    /**
     * Gets participant ref.
     *
     * @return the {@link UserRefDTO}
     */
    public UserRefDTO getParticipantRef() {
        return participantRef;
    }

    /**
     * Sets participant ref.
     *
     * @param participantRef the {@link UserRefDTO}
     */
    public void setParticipantRef(UserRefDTO participantRef) {
        this.participantRef = participantRef;
    }

    /**
     * Gets event logging state
     *
     * @return the event logging state
     */
    public boolean getEventLoggingState() {
        return eventLoggingState;
    }

    /**
     * Sets event logging state
     *
     * @param eventLoggingState the new event logging state
     */
    public void setEventLoggingState(boolean eventLoggingState) {
        this.eventLoggingState = eventLoggingState;
    }

    /**
     * Gets command logging state
     *
     * @return the command logging state
     */
    public boolean getCommandLoggingState() {
        return commandLoggingState;
    }

    /**
     * Sets command logging state
     *
     * @param commandLoggingState the new command logging state
     */
    public void setCommandLoggingState(boolean commandLoggingState) {
        this.commandLoggingState = commandLoggingState;
    }

    public boolean getHasDetectionEvent() {
        return hasDetectionEvent;
    }

    public void setHasDetectionEvent(boolean hasDetectionEvent) {
        this.hasDetectionEvent = hasDetectionEvent;
    }

    @Override
    public String toString() {
        return "TrainingRunDTO{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", eventLogReference='" + eventLogReference + '\'' +
                ", state=" + state +
                ", sandboxInstanceRefId=" + sandboxInstanceRefId +
                ", participantRef=" + participantRef +
                ", eventLoggingState=" + eventLoggingState +
                ", commandLoggingState=" + commandLoggingState +
                ", hasDetectionEvent=" + hasDetectionEvent +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TrainingRunDTO)) return false;
        TrainingRunDTO that = (TrainingRunDTO) object;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getState(), that.getState()) &&
                Objects.equals(getParticipantRef(), that.getParticipantRef()) &&
                Objects.equals(getHasDetectionEvent(), that.getHasDetectionEvent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getState(), getParticipantRef(), getHasDetectionEvent());
    }
}
