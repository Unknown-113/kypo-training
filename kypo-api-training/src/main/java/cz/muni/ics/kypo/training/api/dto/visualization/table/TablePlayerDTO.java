package cz.muni.ics.kypo.training.api.dto.visualization.table;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataWithScoreDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.VisualizationAbstractLevelDTO;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
public class TablePlayerDTO extends PlayerDataWithScoreDTO {

    private Boolean finished;
    private List<VisualizationAbstractLevelDTO> levels = new ArrayList<>();

    public TablePlayerDTO(Long id, String name, byte[] picture, Long trainingRunId) {
        super(id, name, picture, trainingRunId);
    }

    public TablePlayerDTO(UserRefDTO userRef, Long trainingRunId) {
        super(userRef.getUserRefId(), userRef.getUserRefFullName(), userRef.getPicture(), trainingRunId);
    }

    public TablePlayerDTO(Long id, String name, byte[] picture, Long trainingRunId,
                          Integer trainingScore, Integer assessmentScore) {
        super(id, name, picture, trainingRunId, trainingScore, assessmentScore);
    }

    public TablePlayerDTO(UserRefDTO userRef, Long trainingRunId, Integer trainingScore, Integer assessmentScore) {
        super(userRef.getUserRefId(), userRef.getUserRefFullName(), userRef.getPicture(), trainingRunId, trainingScore, assessmentScore);
    }

    public void addTableLevel(VisualizationAbstractLevelDTO visualizationAbstractLevelDTO) {
        this.levels.add(visualizationAbstractLevelDTO);
    }
}
