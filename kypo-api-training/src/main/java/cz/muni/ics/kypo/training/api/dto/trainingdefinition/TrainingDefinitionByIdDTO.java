package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Encapsulates information about Training Definition.
 *
 */
@ApiModel(value = "TrainingDefinitionByIdDTO", description = "A blueprint of abstract levels.")
public class TrainingDefinitionByIdDTO {

    @ApiModelProperty(value = "Main identifier of training definition.", example = "1")
    private Long id;
    @ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", example = "TrainingDefinition2")
    private String title;
    @ApiModelProperty(value = "Description of training definition that is visible to the participant.", example = "Unreleased training definition")
    private String description;
    @ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.", example = "")
    private String[] prerequisites;
    @ApiModelProperty(value = "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ", example = "")
    private String[] outcomes;
    @ApiModelProperty(value = "Current state of training definition.", example = "UNRELEASED")
    private TDState state;
    @ApiModelProperty(value = "Group of organizers who is allowed to see the training definition.", example = "2")
    private Long betaTestingGroupId;
    @ApiModelProperty(value = "Information about all levels in training definition.")
    private List<AbstractLevelDTO> levels = new ArrayList<>();
    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", example = "false")
    private boolean showStepperBar;
    @ApiModelProperty(value = "Sign if training definition can be archived or not.", example = "true")
    private boolean canBeArchived;
    @ApiModelProperty(value = "Estimated time it takes to finish runs created from this definition.", example = "5")
    private long estimatedDuration;
    @ApiModelProperty(value = "Time of last edit done to definition.", example = "2017-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime lastEdited;
    @ApiModelProperty(value = "Name of the user who has done the last edit in definition.", example = "John Doe")
    private String lastEditedBy;
    @ApiModelProperty(value = "Indicates if any of the training levels have defined reference solution.")
    private Boolean hasReferenceSolution;
    @ApiModelProperty(value = "Time of creation of definition.", example = "2017-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime createdAt;

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
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get prerequisites.
     *
     * @return the prerequisites
     */
    public String[] getPrerequisites() {
        return prerequisites;
    }

    /**
     * Sets prerequisites.
     *
     * @param prerequisites the prerequisites
     */
    public void setPrerequisites(String[] prerequisites) {
        this.prerequisites = prerequisites;
    }

    /**
     * Get outcomes.
     *
     * @return the outcomes
     */
    public String[] getOutcomes() {
        return outcomes;
    }

    /**
     * Sets outcomes.
     *
     * @param outcomes the outcomes
     */
    public void setOutcomes(String[] outcomes) {
        this.outcomes = outcomes;
    }

    /**
     * Gets state.
     *
     * @return the {@link TDState}
     */
    public TDState getState() {
        return state;
    }

    /**
     * Sets state.
     *
     * @param state the {@link TDState}
     */
    public void setState(TDState state) {
        this.state = state;
    }

    /**
     * Gets beta testing group ID.
     *
     * @return the ID of the beta testing group
     */
    public Long getBetaTestingGroupId() {
        return betaTestingGroupId;
    }

    /**
     * Sets beta testing group ID.
     *
     * @param betaTestingGroupId the ID of the beta testing group
     */
    public void setBetaTestingGroupId(Long betaTestingGroupId) {
        this.betaTestingGroupId = betaTestingGroupId;
    }

    /**
     * Gets levels.
     *
     * @return the list of {@link AbstractLevelDTO}
     */
    public List<AbstractLevelDTO> getLevels() {
        return levels;
    }

    /**
     * Sets levels.
     *
     * @param levels the list of {@link AbstractLevelDTO}
     */
    public void setLevels(List<AbstractLevelDTO> levels) {
        this.levels = levels;
    }

    /**
     * Gets if stepper bar is shown while in run.
     *
     * @return true if bar is shown
     */
    public boolean isShowStepperBar() {
        return showStepperBar;
    }

    /**
     * Sets if stepper bar is shown while in run.
     *
     * @param showStepperBar true if bar is shown
     */
    public void setShowStepperBar(boolean showStepperBar) {
        this.showStepperBar = showStepperBar;
    }

    /**
     * Definition can be archived if no associated instances are active.
     *
     * @return true if definition can be archived
     */
    public boolean isCanBeArchived() {
        return canBeArchived;
    }

    /**
     * Definition can be archived if no associated instances are active.
     *
     * @param canBeArchived true if definition can be archived
     */
    public void setCanBeArchived(boolean canBeArchived) {
        this.canBeArchived = canBeArchived;
    }

    /**
     * Gets estimated duration.
     *
     * @return the estimated duration
     */
    public long getEstimatedDuration() {
        return estimatedDuration;
    }

    /**
     * Sets estimated duration.
     *
     * @param estimatedDuration the estimated duration
     */
    public void setEstimatedDuration(long estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    /**
     * Gets time of last edit.
     *
     * @return the last edited
     */
    public LocalDateTime getLastEdited() {
        return lastEdited;
    }

    /**
     * Sets time of last edit.
     *
     * @param lastEdited the last edited
     */
    public void setLastEdited(LocalDateTime lastEdited) {
        this.lastEdited = lastEdited;
    }

    /**
     * Gets the name of the user who has done the last edit in Training Definition
     *
     * @return the name of the user
     */
    public String getLastEditedBy() {
        return lastEditedBy;
    }

    /**
     * Sets the name of the user who has done the last edit in Training Definition
     *
     * @param lastEditedBy the name of the user
     */
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     * Gets indication if any of the training levels have the reference solution defined.
     *
     * @return true if any of the training levels have a reference solution defined, false otherwise
     */
    public Boolean getHasReferenceSolution() {
        return hasReferenceSolution;
    }

    /**
     * Sets indication if any of the training levels have the reference solution defined.
     *
     * @param hasReferenceSolution the boolean value
     */
    public void setHasReferenceSolution(Boolean hasReferenceSolution) {
        this.hasReferenceSolution = hasReferenceSolution;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TrainingDefinitionByIdDTO)) return false;
        TrainingDefinitionByIdDTO that = (TrainingDefinitionByIdDTO) object;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getState(), that.getState());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getState());
    }

    @Override
    public String toString() {
        return "TrainingDefinitionByIdDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", state=" + state +
                ", betaTestingGroupId=" + betaTestingGroupId +
                ", showStepperBar=" + showStepperBar +
                ", canBeArchived=" + canBeArchived +
                ", estimatedDuration=" + estimatedDuration +
                ", lastEdited=" + lastEdited +
                ", createdAt=" + createdAt +
                '}';
    }
}
