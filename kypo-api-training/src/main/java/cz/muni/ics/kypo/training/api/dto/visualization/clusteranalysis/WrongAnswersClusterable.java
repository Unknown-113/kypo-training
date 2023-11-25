package cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis;

import cz.muni.ics.kypo.training.utils.ClusterMathUtils;
import org.apache.commons.math3.stat.clustering.Clusterable;

import java.util.Collection;

public class WrongAnswersClusterable implements Clusterable<WrongAnswersClusterable> {

    private final Long userRefId;
    private Double wrongAnswersSubmitted;
    private Double timePlayed;
    private Double wrongAnswersSubmittedNormalized;
    private Double timePlayedNormalized;

    public WrongAnswersClusterable(Long userRefId, Double wrongAnswersSubmitted, Double timePlayed) {
        this.userRefId = userRefId;
        this.wrongAnswersSubmitted = wrongAnswersSubmitted;
        this.timePlayed = timePlayed;
        this.wrongAnswersSubmittedNormalized = wrongAnswersSubmitted;
        this.timePlayedNormalized = timePlayed;
    }

    public Long getUserRefId() {
        return userRefId;
    }

    public Double getWrongAnswersSubmitted() {
        return wrongAnswersSubmitted;
    }

    public void setWrongAnswersSubmitted(Double wrongAnswersSubmitted) {
        this.wrongAnswersSubmitted = wrongAnswersSubmitted;
    }

    public Double getTimePlayed() {
        return timePlayed;
    }

    public void setTimePlayed(Double timePlayed) {
        this.timePlayed = timePlayed;
    }

    public Double getWrongAnswersSubmittedNormalized() {
        return wrongAnswersSubmittedNormalized;
    }

    public void setWrongAnswersSubmittedNormalized(Double wrongAnswersSubmittedNormalized) {
        this.wrongAnswersSubmittedNormalized = wrongAnswersSubmittedNormalized;
    }

    public Double getTimePlayedNormalized() {
        return timePlayedNormalized;
    }

    public void setTimePlayedNormalized(Double timePlayedNormalized) {
        this.timePlayedNormalized = timePlayedNormalized;
    }

    @Override
    public double distanceFrom(WrongAnswersClusterable p) {
        return ClusterMathUtils.calculateDistance2D(wrongAnswersSubmittedNormalized, p.getWrongAnswersSubmittedNormalized(), timePlayedNormalized, p.getTimePlayedNormalized());
    }

    @Override
    public WrongAnswersClusterable centroidOf(Collection<WrongAnswersClusterable> p) {
        return new WrongAnswersClusterable(0L, computerWrongAnswersSubmitted(p), computeTimePlayed(p));
    }

    private Double computerWrongAnswersSubmitted(Collection<WrongAnswersClusterable> p) {
        return p.stream().reduce(
                0.0,
                (value, featureOne) -> value + featureOne.getWrongAnswersSubmittedNormalized() / p.size(),
                Double::sum
        );
    }

    private Double computeTimePlayed(Collection<WrongAnswersClusterable> p) {
        return p.stream().reduce(
                0.0,
                (value, featureOne) -> value + featureOne.getTimePlayedNormalized() / p.size(),
                Double::sum
        );
    }

}

