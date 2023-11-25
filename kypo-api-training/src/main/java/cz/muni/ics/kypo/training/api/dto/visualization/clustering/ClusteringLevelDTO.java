package cz.muni.ics.kypo.training.api.dto.visualization.clustering;

import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.VisualizationAbstractLevelDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClusteringLevelDTO extends VisualizationAbstractLevelDTO {

    private final String title;
    private final long estimatedTime;
    private final int maxParticipantScore;
    private final int maxAchievableScore;
    private final long maxParticipantTime;
    private final float averageTime;
    private final float averageScore;
    private final List<ClusteringLevelPlayerDTO> playerData;

    public ClusteringLevelDTO(ClusteringLevelBuilder builder) {
        super(builder);
        this.title = builder.title;
        this.estimatedTime = builder.estimatedTime;
        this.maxParticipantScore = builder.maxParticipantScore;
        this.maxAchievableScore = builder.maxAchievableScore;
        this.maxParticipantTime = builder.maxParticipantTime;
        this.averageTime = builder.averageTime;
        this.averageScore = builder.averageScore;
        this.playerData = builder.playerData;
    }

    public String getTitle() {
        return title;
    }

    public long getEstimatedTime() {
        return estimatedTime;
    }

    public int getMaxParticipantScore() {
        return maxParticipantScore;
    }
    public int getMaxAchievableScore() {
        return maxAchievableScore;
    }

    public long getMaxParticipantTime() {
        return maxParticipantTime;
    }

    public float getAverageTime() {
        return averageTime;
    }

    public float getAverageScore() {
        return averageScore;
    }

    public List<ClusteringLevelPlayerDTO> getPlayerData() {
        return playerData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ClusteringLevelDTO that = (ClusteringLevelDTO) o;
        return getTitle().equals(that.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getTitle());
    }

    public static class ClusteringLevelBuilder extends VisualizationAbstractLevelDTO.BaseBuilder<ClusteringLevelDTO, ClusteringLevelBuilder> {
        private String title;
        private long estimatedTime;
        private int maxParticipantScore;
        private int maxAchievableScore;
        private long maxParticipantTime;
        private float averageTime;
        private float averageScore;
        private List<ClusteringLevelPlayerDTO> playerData = new ArrayList<>();

        @Override
        protected ClusteringLevelBuilder getActualBuilder() {
            return this;
        }

        public ClusteringLevelBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ClusteringLevelBuilder estimatedTime(long estimatedTime) {
            this.estimatedTime = estimatedTime;
            return this;
        }

        public ClusteringLevelBuilder maxParticipantScore(int maxParticipantScore) {
            this.maxParticipantScore = maxParticipantScore;
            return this;
        }
        public ClusteringLevelBuilder maxAchievableScore(int maxAchievableScore) {
            this.maxAchievableScore = maxAchievableScore;
            return this;
        }

        public ClusteringLevelBuilder maxParticipantTime(long maxParticipantTime) {
            this.maxParticipantTime = maxParticipantTime;
            return this;
        }

        public ClusteringLevelBuilder averageTime(float averageTime) {
            this.averageTime = averageTime;
            return this;
        }

        public ClusteringLevelBuilder averageScore(float averageScore) {
            this.averageScore = averageScore;
            return this;
        }

        public ClusteringLevelBuilder playerData(List<ClusteringLevelPlayerDTO> playerData) {
            this.playerData = playerData;
            return this;
        }

        public ClusteringLevelBuilder addPlayerData(ClusteringLevelPlayerDTO playerData) {
            this.playerData.add(playerData);
            return this;
        }

        @Override
        public ClusteringLevelDTO build() {
            return new ClusteringLevelDTO(this);
        }
    }

}
