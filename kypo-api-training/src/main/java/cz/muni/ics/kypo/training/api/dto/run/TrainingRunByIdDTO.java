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
@ApiModel(value = "TrainingRunByIdDTO", description = "The act, or a recording, of performing actions during training from a perspective of one concrete participant.")
public class TrainingRunByIdDTO {

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
	@ApiModelProperty(value = "Id of associated training definition")
	private Long definitionId;
	@ApiModelProperty(value = "Id of associated training instance")
	private Long instanceId;
	@ApiModelProperty(value = "Id of a previous sandbox instance assigned to the training run.", example = "12")
	private String previousSandboxInstanceRefId;
}
