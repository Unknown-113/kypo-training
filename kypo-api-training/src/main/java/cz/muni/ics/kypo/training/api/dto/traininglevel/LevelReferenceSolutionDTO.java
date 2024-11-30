package cz.muni.ics.kypo.training.api.dto.traininglevel;

import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class LevelReferenceSolutionDTO {

    private Long id;
    private Integer order;
    private List<ReferenceSolutionNodeDTO> referenceSolution;
}
