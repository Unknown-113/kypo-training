package cz.muni.ics.kypo.training.api.dto.betatestinggroup;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Set;
import lombok.*;

/**
 * Encapsulates information needed for creation of new Beta testing group.
 */
@Getter
@Setter
@ToString
@ApiModel(value = "BetaTestingGroupCreateDTO", description = "BetaTestingGroup to create. (Deprecated)")
public class BetaTestingGroupCreateDTO {

    @ApiModelProperty(value = "Logins of users who is allowed to see training definition.", required = true)
    @NotNull(message = "{betaTestingGroup.organizersRefIds.NotNull.message}")
    private Set<Long> organizersRefIds;
}
