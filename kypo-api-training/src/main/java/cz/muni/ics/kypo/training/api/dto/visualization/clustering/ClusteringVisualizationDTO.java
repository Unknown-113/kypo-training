package cz.muni.ics.kypo.training.api.dto.visualization.clustering;

import io.swagger.annotations.ApiModel;

import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@ApiModel(value = "ClusteringVisualizationDTO", description = "Clustering visualization.")
public class ClusteringVisualizationDTO {

    private TrainingResultsDTO finalResults;
    private List<ClusteringLevelDTO> levels;

    public void addLevel(ClusteringLevelDTO clusteringLevelDTO) {
        this.levels.add(clusteringLevelDTO);
    }
}
