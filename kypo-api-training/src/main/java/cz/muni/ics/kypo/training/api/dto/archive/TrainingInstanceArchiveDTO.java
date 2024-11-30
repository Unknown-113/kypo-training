package cz.muni.ics.kypo.training.api.dto.archive;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.*;

/**
 * Encapsulates information about Training instance.
 * Used for archiving
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@ApiModel(value = "TrainingInstanceArchiveDTO", description = "The finished and archived instance of training definition which includes individual finished training runs of participants.")
public class TrainingInstanceArchiveDTO {

	@ApiModelProperty(value = "Main identifier of training instance.", example = "1")
	private Long id;
	@ApiModelProperty(value = "Main identifier of training definition associated with this instance.", example = "1")
	private Long definitionId;
	@ApiModelProperty(value = "Date when training instance starts.", example = "2016-10-19 10:23:54+02")
	@JsonSerialize(using = LocalDateTimeUTCSerializer.class)
	private LocalDateTime startTime;
	@ApiModelProperty(value = "Date when training instance ends.", example = "2017-10-19 10:23:54+02")
	@JsonSerialize(using = LocalDateTimeUTCSerializer.class)
	private LocalDateTime endTime;
	@ApiModelProperty(value = "Short textual description of the training instance.", example = "Concluded Instance")
	private String title;
	@ApiModelProperty(value = "Reference to organizersRefIds which organize training instance.")
	private Set<Long> organizersRefIds;
	@ApiModelProperty(value = "Token needed to access runs created from this definition", example = "pass-1234")
	private String accessToken;
	@ApiModelProperty(value = "Indicates if local sandboxes are used for training runs.", example = "true")
	private boolean localEnvironment;
	@ApiModelProperty(value = "Indicates if trainee can during training run move to the previous already solved levels.", example = "true")
	private boolean backwardMode;
}
