package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import java.util.List;

/**
 * Encapsulates information about answer similarity detection event.
 */
@ApiModel(value = "AnswerSimilarityDetectionEventDTO", description = "A detection event of type Answer Similarity.", parent = AbstractDetectionEventDTO.class)
public class AnswerSimilarityDetectionEventDTO extends AbstractDetectionEventDTO {
    @ApiModelProperty(value = "Correct answer to the level.", example = "pass")
    private String answer;
    @ApiModelProperty(value = "Name of a player who was assigned the correct answer.", example = "John Doe")
    private String answerOwner;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnswerOwner() {
        return answerOwner;
    }

    public void setAnswerOwner(String answerOwner) {
        this.answerOwner = answerOwner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AnswerSimilarityDetectionEventDTO that = (AnswerSimilarityDetectionEventDTO) o;
        return Objects.equals(answer, that.answer) &&
                Objects.equals(answerOwner, that.answerOwner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), answer, answerOwner);
    }

    @Override
    public String toString() {
        return "AnswerSimilarityDetectionEventDTO{" +
                "answer='" + answer + '\'' +
                ", answerOwner='" + answerOwner + '\'' +
                '}';
    }
}
