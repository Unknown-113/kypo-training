package cz.muni.ics.kypo.training.api.dto.visualization.analytical;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Analytical dashboard - level details of the particular participant.
 */
@ApiModel(value = "ParticipantLevelAnalyticalDashboardDTO", description = "Analytical dashboard - level details of the particular participant.")
public class ParticipantLevelAnalyticalDashboardDTO {

    @ApiModelProperty(value = "Identifier of the level.", example = "1")
    private Long levelId;
    @ApiModelProperty(value = "The title of the level.", example = "Find open ports")
    private String levelTitle;
    @ApiModelProperty(value = "Number of hint taken during level.", example = "Play me")
    private int hintsTaken;
    @ApiModelProperty(value = "Time spent in the level.", example = "This is how you do it")
    private long duration;
    @ApiModelProperty(value = "Keyword found in training, used for access next level.", example = "secretAnswer")
    private int score;
    @ApiModelProperty(value = "Indicates if the participant submitted a wrong answer.", example = "true")
    private List<String> wrongAnswers = new ArrayList<>();

    public Long getLevelId() {
        return levelId;
    }

    public void setLevelId(Long levelId) {
        this.levelId = levelId;
    }

    public String getLevelTitle() {
        return levelTitle;
    }

    public void setLevelTitle(String levelTitle) {
        this.levelTitle = levelTitle;
    }

    public int getHintsTaken() {
        return hintsTaken;
    }

    public void setHintsTaken(int hintsTaken) {
        this.hintsTaken = hintsTaken;
    }

    public void increaseHintTaken() {
        this.hintsTaken++;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<String> getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(List<String> wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

    public void addWrongAnswer(String wrongAnswer) {
        this.wrongAnswers.add(wrongAnswer);
    }

    @Override
    public String toString() {
        return "ParticipantLevelAnalyticalDashboardDTO{" +
                "levelId=" + levelId +
                ", levelTitle=" + levelTitle +
                ", hintsTaken=" + hintsTaken +
                ", duration=" + duration +
                ", score=" + score +
                ", wrongAnswersSubmitted=" + wrongAnswers +
                '}';
    }
}
