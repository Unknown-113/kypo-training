package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.util.Objects;

/**
 * Class representing hints associated with training level that can be displayed by trainee if they are in need of help with
 * solving given level
 */
@Entity
@Table(name = "hint")
@NamedQueries({
        @NamedQuery(
                name = "Hint.deleteHintsByLevelId",
                query = "DELETE FROM Hint h WHERE h.trainingLevel.id = :levelId"
        ),
})
public class Hint extends AbstractEntity<Long> {

    @Column(name = "title", nullable = false)
    private String title;
    @Lob
    @Column(name = "content", nullable = false)
    private String content;
    @Column(name = "hint_penalty", nullable = false)
    private Integer hintPenalty;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_level_id")
    private TrainingLevel trainingLevel;
    @Column(name = "order_in_level", nullable = false)
    private int order;

    /**
     * Instantiates a new Hint
     */
    public Hint() {
    }

    /**
     * Instantiates a new Hint
     *
     * @param title       title of the hint
     * @param content     text advice of the hint
     * @param hintPenalty score penalty trainee pays to display hint
     * @param trainingLevel   training level associated with given hint
     * @param order       the order
     */
    public Hint(String title, String content, Integer hintPenalty, TrainingLevel trainingLevel, int order) {
        this.title = title;
        this.content = content;
        this.hintPenalty = hintPenalty;
        this.trainingLevel = trainingLevel;
        this.order = order;
    }

    /**
     * Gets unique identification number of hint
     *
     * @return the id
     */
    public Long getId() {
        return super.getId();
    }

    /**
     * Sets unique identification number of hint
     *
     * @param id the id
     */
    public void setId(Long id) {
        super.setId(id);
    }

    /**
     * Gets title of the hint
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title of the hint
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets text advice of the hint
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets text advice of the hint
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets score penalty trainee pays to display hint
     *
     * @return the hint penalty
     */
    public Integer getHintPenalty() {
        return hintPenalty;
    }

    /**
     * Sets score penalty trainee pays to display hint
     *
     * @param hintPenalty the hint penalty
     */
    public void setHintPenalty(Integer hintPenalty) {
        this.hintPenalty = hintPenalty;
    }

    /**
     * Gets training level associated with given hint
     *
     * @return the training level
     */
    public TrainingLevel getTrainingLevel() {
        return trainingLevel;
    }

    /**
     * Sets training level associated with given hint
     *
     * @param trainingLevel the training level
     */
    public void setTrainingLevel(TrainingLevel trainingLevel) {
        this.trainingLevel = trainingLevel;
    }

    /**
     * Gets order.
     *
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * Sets order.
     *
     * @param order the order
     */
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hint)) return false;
        Hint hint = (Hint) o;
        return Objects.equals(getTitle(), hint.getTitle()) &&
                Objects.equals(getContent(), hint.getContent()) &&
                Objects.equals(getHintPenalty(), hint.getHintPenalty()) &&
                getOrder() == getOrder();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getContent(), getHintPenalty(), getOrder());
    }

    @Override
    public String toString() {
        return "Hint{" +
                "id=" + super.getId() +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", hintPenalty=" + hintPenalty +
                ", order=" + order +
                '}';
    }
}
