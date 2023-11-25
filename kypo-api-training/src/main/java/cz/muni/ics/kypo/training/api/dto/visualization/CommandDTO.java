package cz.muni.ics.kypo.training.api.dto.visualization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.converters.DurationConverter;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCDeserializer;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.Duration;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "CommandDTO", description = "Command with valid syntax and semantic.", parent = AbstractCommandDTO.class)
@JsonPropertyOrder({"commandType", "cmd", "timestamp", "trainingTime", "fromHostIp", "options"})
public class CommandDTO extends AbstractCommandDTO {

    @Past
    @NotNull
    @ApiModelProperty(value = "Time when command was recorded.", required = true, example = "2022-07-21T13:16:41.435559")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
    private LocalDateTime timestamp;
    @ApiModelProperty(value = "Training time when command was recorded.", required = true, example = "07:23:43")
    @JsonSerialize(converter = DurationConverter.class)
    @JsonProperty("training_time")
    private Duration trainingTime;
    @NotEmpty
    @ApiModelProperty(value = "Ip address where command was submitted.", required = true, example = "10.10.17.5")
    @JsonProperty("from_host_ip")
    private String fromHostIp;
    @NotEmpty
    @ApiModelProperty(value = "Valid command options", required = true, example = "-p 25 -a 20.20.15.18")
    private String options;

    @Builder
    public CommandDTO(@NotEmpty String commandType, @NotEmpty String cmd, @Past @NotNull LocalDateTime timestamp, @NotNull Duration trainingTime, @NotEmpty String fromHostIp, @NotEmpty String options) {
        super(commandType, cmd);
        this.timestamp = timestamp;
        this.trainingTime = trainingTime;
        this.fromHostIp = fromHostIp;
        this.options = options;
    }

}
