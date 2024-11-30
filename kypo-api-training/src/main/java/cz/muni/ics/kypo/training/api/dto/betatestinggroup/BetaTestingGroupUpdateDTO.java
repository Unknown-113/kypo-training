package cz.muni.ics.kypo.training.api.dto.betatestinggroup;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Set;
import lombok.*;

/**
 * Encapsulates information needed for update of Beta testing group
 */
@Getter
@Setter
@ToString
@ApiModel(value = "BetaTestingGroupUpdateDTO", description = "BetaTesting group to update. (Deprecated)")
public class BetaTestingGroupUpdateDTO {

    @ApiModelProperty(value = "Logins of users who is allowed to see training definition.", required = true)
    @NotNull(message = "{betaTestingGroup.organizersRefIds.NotNull.message}")
    private Set<Long> organizersRefIds;
}
