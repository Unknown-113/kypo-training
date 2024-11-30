package cz.muni.ics.kypo.training.api.dto.visualization.commons;

import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class EventDTO {

    private String text;
    private long time;
    private int score;

    public EventDTO(long time) {
        this.time = time;
    }

}
