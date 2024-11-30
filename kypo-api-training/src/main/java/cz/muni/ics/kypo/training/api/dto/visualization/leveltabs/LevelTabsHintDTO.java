package cz.muni.ics.kypo.training.api.dto.visualization.leveltabs;

import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LevelTabsHintDTO {

    private Long id;
    private int order;
    private String title;
    private int penalty;
}
