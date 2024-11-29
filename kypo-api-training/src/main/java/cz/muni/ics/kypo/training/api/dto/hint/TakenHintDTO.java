package cz.muni.ics.kypo.training.api.dto.hint;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import lombok.*;

/**
 * Encapsulates information about hint that was already taken
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "TakenHintDTO", description = "A taken brief textual description to aid the participant.")
public class TakenHintDTO {

    @ApiModelProperty(value = "Main identifier of hint.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Short textual description of the hint.", example = "Hint1")
    private String title;
    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Very good advice")
    private String content;
    @ApiModelProperty(value = "The order of hint in training level", example = "1")
    private int order;

    /**
     * Gets id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets content.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets content.
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TakenHintDTO hintDTO = (TakenHintDTO) o;

        if (!id.equals(hintDTO.id)) return false;
        if (!title.equals(hintDTO.title)) return false;
        return content.equals(hintDTO.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content);
    }

    @Override
    public String toString() {
        return "TakenHintDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", order=" + order +
                '}';
    }
}
