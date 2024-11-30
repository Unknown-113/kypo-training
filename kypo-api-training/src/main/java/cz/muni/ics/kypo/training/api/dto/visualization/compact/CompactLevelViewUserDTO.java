package cz.muni.ics.kypo.training.api.dto.visualization.compact;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "CompactLevelViewUserDTO", description = "Information about a specific user for compact level visualization.")
public class CompactLevelViewUserDTO {

    @ApiModelProperty(value = "Information about the user.")
    private UserRefDTO user;
    @ApiModelProperty(value = "Events from the training run this user was in.")
    private List<CompactLevelViewEventDTO> events;
}
