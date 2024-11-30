package cz.muni.ics.kypo.training.api.dto.visualization.analytical;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.export.AbstractLevelExportDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.AbstractLevelVisualizationDTO;
import cz.muni.ics.kypo.training.converters.LocalDateSerializer;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Aggregated data of the training instance provided to analytical dashboard visualization
 */
@ApiModel(value = "TrainingInstanceAnalyticalDashboardDTO", description = "Information about training level needed for visualizations.", parent = AbstractLevelExportDTO.class)
public class TrainingInstanceAnalyticalDashboardDTO {

    @ApiModelProperty(value = "Title of the training instance", example = "Play me")
    private String title;
    @ApiModelProperty(value = "Training instance commence date.", example = "14.08.2021")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate date;
    @ApiModelProperty(value = "Training instance duration in milliseconds", example = "847848464")
    private Long duration;
    @ApiModelProperty(value = "Training instance ID.", example = "true")
    private Long instanceId;
    @ApiModelProperty(value = "Average score achieved by participants.", example = "true")
    private Double averageScore;
    @ApiModelProperty(value = "Median score achieved by participants.", example = "true")
    private Double medianScore;
    @ApiModelProperty(value = "Details about individual participants.")
    private List<ParticipantAnalyticalDashboardDTO> participants;
    @ApiModelProperty(value = "Details about individual levels.")
    private List<LevelAnalyticalDashboardDTO> levels;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Double getMedianScore() {
        return medianScore;
    }

    public void setMedianScore(Double medianScore) {
        this.medianScore = medianScore;
    }

    public List<ParticipantAnalyticalDashboardDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantAnalyticalDashboardDTO> participants) {
        this.participants = participants;
    }

    public List<LevelAnalyticalDashboardDTO> getLevels() {
        return levels;
    }

    public void setLevels(List<LevelAnalyticalDashboardDTO> levels) {
        this.levels = levels;
    }

    @Override
    public String toString() {
        return "TrainingInstanceAnalyticalDashboardDTO{" +
                "title='" + title + '\'' +
                ", date=" + date +
                ", duration=" + duration +
                ", instanceId=" + instanceId +
                ", averageScore=" + averageScore +
                ", medianScore=" + medianScore +
                '}';
    }
}
