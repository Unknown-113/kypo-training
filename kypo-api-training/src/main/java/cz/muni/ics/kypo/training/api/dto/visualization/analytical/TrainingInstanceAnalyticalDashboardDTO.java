package cz.muni.ics.kypo.training.api.dto.visualization.analytical;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.export.AbstractLevelExportDTO;
import cz.muni.ics.kypo.training.converters.LocalDateSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.List;
import lombok.*;

/**
 * Aggregated data of the training instance provided to analytical dashboard visualization
 */
@Getter
@Setter
@ToString
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
}
