package cz.muni.ics.kypo.training.api.dto.visualization.table;

import cz.muni.ics.kypo.training.api.dto.visualization.commons.VisualizationAbstractLevelDTO;

import lombok.*;

@EqualsAndHashCode
@Getter
public class TableLevelDTO extends VisualizationAbstractLevelDTO {
    private final Integer wrongAnswers;
    private final Integer hintsTaken;
    private final int participantLevelScore;

    public TableLevelDTO(TableLevelBuilder builder) {
        super(builder);
        this.wrongAnswers = builder.wrongAnswers;
        this.hintsTaken = builder.hintsTaken;
        this.participantLevelScore = builder.score;
    }

    public static class TableLevelBuilder extends BaseBuilder<TableLevelDTO, TableLevelDTO.TableLevelBuilder> {
        private Integer wrongAnswers;
        private Integer hintsTaken;
        private int score;

        @Override
        protected TableLevelDTO.TableLevelBuilder getActualBuilder() {
            return this;
        }

        public TableLevelDTO.TableLevelBuilder wrongAnswers(Integer wrongAnswers) {
            this.wrongAnswers = wrongAnswers;
            return this;
        }

        public TableLevelDTO.TableLevelBuilder hintsTaken(Integer hintsTaken) {
            this.hintsTaken = hintsTaken;
            return this;
        }

        public TableLevelDTO.TableLevelBuilder score(int score) {
            this.score = score;
            return this;
        }

        @Override
        public TableLevelDTO build() {
            return new TableLevelDTO(this);
        }
    }
}
