package cz.muni.ics.kypo.training.api.dto.visualization.compact;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(value = "CompactLevelViewDTO", description = "Information needed for compact level visualization.")
public class CompactLevelViewDTO {

    @ApiModelProperty(value = "Main identifier of the level.", example = "1")
    private Long levelId;
    @ApiModelProperty(value = "Title of the level.", example = "Intro level")
    private String title;
    @ApiModelProperty(value = "Users that played the level.")
    private List<CompactLevelViewUserDTO> users = new ArrayList<>();


    public CompactLevelViewDTO(Long levelId) {
        this.levelId = levelId;
    }

    /**
     * Add user to the list of users
     * @param user user to add
     * @return true if the user was added, false otherwise
     */
    public boolean addUser(CompactLevelViewUserDTO user) {
        return this.users.add(user);
    }
}
