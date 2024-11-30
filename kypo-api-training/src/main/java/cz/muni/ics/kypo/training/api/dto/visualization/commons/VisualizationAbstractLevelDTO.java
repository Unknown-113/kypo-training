package cz.muni.ics.kypo.training.api.dto.visualization.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.muni.ics.kypo.training.api.enums.LevelType;

import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class VisualizationAbstractLevelDTO {
    private Long id;
    private int order;
    private LevelType levelType;

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
