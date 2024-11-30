package cz.muni.ics.kypo.training.api.dto.visualization.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.muni.ics.kypo.training.api.enums.LevelType;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class VisualizationAbstractLevelDTO {
    private Long id;
    private int order;
    private LevelType levelType;


    public VisualizationAbstractLevelDTO(BaseBuilder<?, ?> timelineLevelBuilder) {
        this.id = timelineLevelBuilder.id;
        this.order = timelineLevelBuilder.order;
        this.levelType = timelineLevelBuilder.levelType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public LevelType getLevelType() {
        return levelType;
    }

    public void setLevelType(LevelType levelType) {
        this.levelType = levelType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VisualizationAbstractLevelDTO that = (VisualizationAbstractLevelDTO) o;
        return getOrder() == that.getOrder() &&
                getId().equals(that.getId()) &&
                getLevelType() == that.getLevelType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getOrder(), getLevelType());
    }

    public static abstract class BaseBuilder<T extends VisualizationAbstractLevelDTO, B extends BaseBuilder<?, ?>> {
        protected T actualClass;
        protected B actualClassBuilder;

        protected abstract B getActualBuilder();

        protected BaseBuilder() {
            actualClassBuilder = getActualBuilder();
        }


        private Long id;
        private int order;
        private LevelType levelType;

        public B id(Long id) {
            this.id = id;
            return actualClassBuilder;
        }

        public B order(int order) {
            this.order = order;
            return actualClassBuilder;
        }

        public B levelType(LevelType levelType) {
            this.levelType = levelType;
            return actualClassBuilder;
        }

        public abstract VisualizationAbstractLevelDTO build();
    }
}
