package cz.muni.ics.kypo.training.api.dto.visualization.clustering;

import io.swagger.annotations.ApiModel;

import java.util.List;

@ApiModel(value = "ClusteringVisualizationDTO", description = "Clustering visualization.")
public class ClusteringVisualizationDTO {

    private TrainingResultsDTO finalResults;
    private List<ClusteringLevelDTO> levels;

    public ClusteringVisualizationDTO(TrainingResultsDTO finalResults, List<ClusteringLevelDTO> levels) {
        this.finalResults = finalResults;
        this.levels = levels;
    }

    public TrainingResultsDTO getFinalResults() {
        return finalResults;
    }

    public void setFinalResults(TrainingResultsDTO finalResults) {
        this.finalResults = finalResults;
    }

    public List<ClusteringLevelDTO> getLevels() {
        return levels;
    }

    public void addLevel(ClusteringLevelDTO clusteringLevelDTO) {
        this.levels.add(clusteringLevelDTO);
    }

    public void setLevels(List<ClusteringLevelDTO> levels) {
        this.levels = levels;
    }
}
