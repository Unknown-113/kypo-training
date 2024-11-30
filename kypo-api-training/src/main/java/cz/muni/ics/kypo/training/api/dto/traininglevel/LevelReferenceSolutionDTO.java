package cz.muni.ics.kypo.training.api.dto.traininglevel;

import java.util.List;

public class LevelReferenceSolutionDTO {

    private Long id;
    private Integer order;
    private List<ReferenceSolutionNodeDTO> referenceSolution;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public List<ReferenceSolutionNodeDTO> getReferenceSolution() {
        return referenceSolution;
    }

    public void setReferenceSolution(List<ReferenceSolutionNodeDTO> referenceSolution) {
        this.referenceSolution = referenceSolution;
    }

    public LevelReferenceSolutionDTO(Long id, Integer order, List<ReferenceSolutionNodeDTO> referenceSolution) {
        this.id = id;
        this.order = order;
        this.referenceSolution = referenceSolution;
    }
}
