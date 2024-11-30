package cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventIdentification {

    private Long trainingDefinitionId;
    private Long trainingInstanceId;
    private Long trainingRunId;
}
