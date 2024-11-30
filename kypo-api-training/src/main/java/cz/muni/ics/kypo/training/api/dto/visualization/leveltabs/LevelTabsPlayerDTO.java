package cz.muni.ics.kypo.training.api.dto.visualization.leveltabs;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LevelTabsPlayerDTO {

    private Long id;
    private Long trainingRunId;
    private long participantLevelScore;
    private Integer hints;
    private List<String> wrongAnswers = new ArrayList<>();
    private long time;
    private Boolean displayedSolution;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTrainingRunId() {
        return trainingRunId;
    }

    public void setTrainingRunId(Long trainingRunId) {
        this.trainingRunId = trainingRunId;
    }

    public long getParticipantLevelScore() {
        return participantLevelScore;
    }

    public void setParticipantLevelScore(long participantLevelScore) {
        this.participantLevelScore = participantLevelScore;
    }

    public Integer getHints() {
        return hints;
    }

    public void addHint() {
        if(this.hints == null) {
            this.hints = 0;
        }
        this.hints++;
    }

    public void setHints(Integer hints) {
        this.hints = hints;
    }

    public List<String> getWrongAnswers() {
        return wrongAnswers;
    }

    public void addWrongAnswer(String wrongAnswer) {
        this.wrongAnswers.add(wrongAnswer);
    }

    public void setWrongAnswers(List<String> wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Boolean getDisplayedSolution() {
        return displayedSolution;
    }

    public void setDisplayedSolution(Boolean displayedSolution) {
        this.displayedSolution = displayedSolution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LevelTabsPlayerDTO that = (LevelTabsPlayerDTO) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getTrainingRunId(), that.getTrainingRunId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTrainingRunId());
    }

    @Override
    public String toString() {
        return "LevelTabPlayerDTO{" +
                "id=" + id +
                ", trainingRunId=" + trainingRunId +
                ", participantLevelScore=" + participantLevelScore +
                ", hints=" + hints +
                ", wrongAnswers=" + wrongAnswers +
                ", time=" + time +
                ", displayedSolution=" + displayedSolution +
                '}';
    }
}
