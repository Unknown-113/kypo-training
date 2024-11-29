package cz.muni.ics.kypo.training.api.dto.betatestinggroup;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;
import lombok.*;

/**
 * Encapsulates information about Beta testing group.
 */
@Getter
@Setter
@ToString
@ApiModel(value = "BetaTestingGroupDTO", description = "Group of organizers who are allowed to see the specific training definitions. (Deprecated)")
public class BetaTestingGroupDTO {

    @ApiModelProperty(value = "Main identifier of beta testing group.", required = true, example = "1")
    private Long id;
    @ApiModelProperty(value = "Logins of users who is allowed to see training definition.", required = true)
    private Set<Long> organizersRefIds;
}
