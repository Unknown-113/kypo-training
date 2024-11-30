package cz.muni.ics.kypo.training.api.dto.visualization.commons;

import java.util.Objects;

public class EventDTO {

    private String text;
    private long time;
    private int score;

    public EventDTO() {
    }

    public EventDTO(long time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventDTO that = (EventDTO) o;
        return getTime() == that.getTime() &&
                getScore() == that.getScore() &&
                getText().equals(that.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getText(), getTime(), getScore());
    }
}
