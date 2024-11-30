package cz.muni.ics.kypo.training.api.dto.visualization.leveltabs;

import java.util.Objects;

public class LevelTabsHintDTO {

    private Long id;
    private int order;
    private String title;
    private int penalty;

    public LevelTabsHintDTO() {
    }

    public LevelTabsHintDTO(Long id, int order, String title, int penalty) {
        this.id = id;
        this.order = order;
        this.title = title;
        this.penalty = penalty;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LevelTabsHintDTO that = (LevelTabsHintDTO) o;
        return getId().equals(that.getId()) &&
                getTitle().equals(that.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle());
    }

    @Override
    public String toString() {
        return "LevelTabHintDTO{" +
                "id=" + id +
                ", order=" + order +
                ", title='" + title + '\'' +
                ", penalty=" + penalty +
                '}';
    }

}
