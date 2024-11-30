package cz.muni.ics.kypo.training.api.dto.visualization.leveltabs;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LevelTabsPlayerDTO {

    private Long id;
    private Long trainingRunId;
    private long participantLevelScore;
    private Integer hints;
    private List<String> wrongAnswers = new ArrayList<>();
    private long time;
    private Boolean displayedSolution;

    public void addHint() {
        if(this.hints == null) {
            this.hints = 0;
        }
        this.hints++;
    }

    public void addWrongAnswer(String wrongAnswer) {
        this.wrongAnswers.add(wrongAnswer);
    }
}
