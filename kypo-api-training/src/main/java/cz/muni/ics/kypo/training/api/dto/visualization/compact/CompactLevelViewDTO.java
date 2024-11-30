package cz.muni.ics.kypo.training.api.dto.visualization.compact;

import com.google.common.base.Objects;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "CompactLevelViewDTO", description = "Information needed for compact level visualization.")
public class CompactLevelViewDTO {

    @ApiModelProperty(value = "Main identifier of the level.", example = "1")
    private Long levelId;
    @ApiModelProperty(value = "Title of the level.", example = "Intro level")
    private String title;
    @ApiModelProperty(value = "Users that played the level.")
    private List<CompactLevelViewUserDTO> users = new ArrayList<>();

    public CompactLevelViewDTO() {
    }

    public CompactLevelViewDTO(Long levelId) {
        this.levelId = levelId;
    }

    /**
     * Gets the level id
     * @return the level id
     */
    public Long getLevelId() {
        return levelId;
    }

    /**
     * Sets the level id
     * @param levelId new level id
     */
    public void setLevelId(Long levelId) {
        this.levelId = levelId;
    }

    /**
     * Gets the title
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the level id
     * @param title new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the users
     * @return the users
     */
    public List<CompactLevelViewUserDTO> getUsers() {
        return users;
    }

    /**
     * Sets the level id
     * @param users new users
     */
    public void setUsers(List<CompactLevelViewUserDTO> users) {
        this.users = users;
    }

    /**
     * Add user to the list of users
     * @param user user to add
     * @return true if the user was added, false otherwise
     */
    public boolean addUser(CompactLevelViewUserDTO user) {
        return this.users.add(user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompactLevelViewDTO)) return false;
        CompactLevelViewDTO that = (CompactLevelViewDTO) o;
        return Objects.equal(levelId, that.levelId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(levelId);
    }

    @Override
    public String toString() {
        return "CompactLevelViewDTO{" +
                "levelId=" + levelId +
                ", title='" + title + '\'' +
                '}';
    }
}
