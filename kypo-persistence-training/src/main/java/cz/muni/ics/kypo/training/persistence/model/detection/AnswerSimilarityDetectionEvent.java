package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.persistence.model.question.Question;

import javax.persistence.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "answer_similarity_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
        @NamedQuery(
                name = "AnswerSimilarityDetectionEvent.findAnswerSimilarityEventById",
                query = "SELECT asde FROM AnswerSimilarityDetectionEvent asde WHERE asde.id = :eventId"
        ),
        @NamedQuery(
                name = "AnswerSimilarityDetectionEvent.findAllByCheatingDetectionId",
                query = "SELECT asde FROM AnswerSimilarityDetectionEvent asde WHERE asde.cheatingDetectionId = :cheatingDetectionId"
        )
})
public class AnswerSimilarityDetectionEvent extends AbstractDetectionEvent {

    @Column(name = "answer")
    private String answer;
    @Column(name = "answer_owner", nullable = false)
    private String answerOwner;

    /**
     * Gets passkey that needs to be entered by trainee to complete level
     *
     * @return the passkey
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Sets passkey that needs to be entered by trainee to complete level
     *
     * @param answer the passkey
     */
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    /**
     * Gets passkey that needs to be entered by trainee to complete level
     *
     * @return the passkey
     */
    public String getAnswerOwner() {
        return answerOwner;
    }

    /**
     * Sets passkey that needs to be entered by trainee to complete level
     *
     * @param answerOwner the passkey
     */
    public void setAnswerOwner(String answerOwner) {
        this.answerOwner = answerOwner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AnswerSimilarityDetectionEvent that = (AnswerSimilarityDetectionEvent) o;
        return Objects.equals(answer, that.answer) &&
                Objects.equals(answerOwner, that.answerOwner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), answer, answerOwner);
    }

    @Override
    public String toString() {
        return "AnswerSimilarityDetectionEvent{" +
                "answer='" + answer + '\'' +
                ", answerOwner='" + answerOwner + '\'' +
                ", participants='" + '}';
    }
}
