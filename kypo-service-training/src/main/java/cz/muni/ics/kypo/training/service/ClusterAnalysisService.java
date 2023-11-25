package cz.muni.ics.kypo.training.service;

import cz.muni.csirt.kypo.events.AbstractAuditPOJO;
import cz.muni.csirt.kypo.events.trainings.CorrectAnswerSubmitted;
import cz.muni.csirt.kypo.events.trainings.HintTaken;
import cz.muni.csirt.kypo.events.trainings.SolutionDisplayed;
import cz.muni.csirt.kypo.events.trainings.WrongAnswerSubmitted;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.HintClusterable;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.WrongAnswersClusterable;
import cz.muni.ics.kypo.training.utils.ClusterMathUtils;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.clustering.Cluster;
import org.apache.commons.math3.stat.clustering.Clusterable;
import org.apache.commons.math3.stat.clustering.EuclideanDoublePoint;
import org.apache.commons.math3.stat.clustering.KMeansPlusPlusClusterer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClusterAnalysisService {

    private final static Logger logger = LoggerFactory.getLogger(ClusterAnalysisService.class);

    /**
     * Retrieve list of {@link WrongAnswersClusterable}s from events
     *
     * @param events events to use
     * @return list of {@link WrongAnswersClusterable}s
     */
    public List<WrongAnswersClusterable> transformToWrongAnswersClusterable(List<AbstractAuditPOJO> events) {

        Map<Long, List<AbstractAuditPOJO>> userEvents = events.stream()
                .collect(Collectors.groupingBy(AbstractAuditPOJO::getUserRefId, LinkedHashMap::new, Collectors.toList()));

        List<WrongAnswersClusterable> result = userEvents.entrySet().stream()
                .map(userEvs -> {
                    long wrongAnswers = userEvs.getValue().stream().filter(WrongAnswerSubmitted.class::isInstance).count();
                    List<Long> collect = userEvs.getValue().stream()
                            .map(AbstractAuditPOJO::getTimestamp)
                            .collect(Collectors.toList());
                    return new WrongAnswersClusterable(userEvs.getKey(), (double) wrongAnswers,
                            (double) (collect.get(collect.size() - 1) - collect.get(0)));
                }).collect(Collectors.toList());
        return ClusterMathUtils.normalize(
                result,
                Pair.of(WrongAnswersClusterable::getTimePlayed, WrongAnswersClusterable::setTimePlayedNormalized),
                Pair.of(WrongAnswersClusterable::getWrongAnswersSubmitted, WrongAnswersClusterable::setWrongAnswersSubmittedNormalized)
        );
    }

    /**
     * Retrieve list of {@link HintClusterable}s from events
     *
     * @param events events to use
     * @return list of {@link HintClusterable}s
     */
    public List<HintClusterable> transformToHintClusterable(List<AbstractAuditPOJO> events) {
        Map<Long, List<AbstractAuditPOJO>> userEvents = events.stream()
                .collect(Collectors.groupingBy(AbstractAuditPOJO::getUserRefId, LinkedHashMap::new, Collectors.toList()));
        List<HintClusterable> result = new ArrayList<>();
        userEvents.forEach((key, value) -> {

            // max time between hint and the end of a level
            Map<Long, List<AbstractAuditPOJO>> collect = value.stream()
                    .filter(trainingEvent -> trainingEvent instanceof HintTaken
                            || trainingEvent instanceof WrongAnswerSubmitted
                            || trainingEvent instanceof CorrectAnswerSubmitted
                            || trainingEvent instanceof SolutionDisplayed)
                    .collect(Collectors.groupingBy(AbstractAuditPOJO::getLevel, Collectors.toList()));
            collect.forEach((key1, value1) -> {
                Optional<AbstractAuditPOJO> hint = value1.stream()
                        .filter(trainingEvent -> trainingEvent instanceof HintTaken)
                        .findFirst();

                if (hint.isPresent()) {
                    long timeAfterHint = value1.get(value1.size() - 1).getTimestamp()
                            - hint.get().getTimestamp();

                    long wrongAnswers = value1.stream()
                            .filter(trainingEvent -> trainingEvent instanceof WrongAnswerSubmitted
                                    && Long.compare(trainingEvent.getTimestamp(), hint.get().getTimestamp()) < 1)
                            .count();
                    result.add(new HintClusterable(key, key1, (double) timeAfterHint, (double) wrongAnswers));
                }
            });
        });
        return ClusterMathUtils.normalize(
                result,
                Pair.of(HintClusterable::getTimeSpentAfterHint, HintClusterable::setTimeSpentAfterHintNormalized),
                Pair.of(HintClusterable::getWrongAnswersAfterHint, HintClusterable::setWrongAnswersAfterHintNormalized)
        );
    }

    /**
     * Retrieve list of {@link EuclideanDoublePoint}s from events
     *
     * @param events events to use
     * @return list of {@link EuclideanDoublePoint}s
     */
    public List<EuclideanDoublePoint> transformToNDimensionalCluster(List<AbstractAuditPOJO> events) {
        Map<Long, List<AbstractAuditPOJO>> userEvents = events.stream()
                .collect(Collectors.groupingBy(AbstractAuditPOJO::getUserRefId, LinkedHashMap::new, Collectors.toList()));
        List<EuclideanDoublePoint> result = new ArrayList<>();

        // max time between hint and the end of the level
        double[] timeAfterHint = StatUtils.normalize(extractTimeAfterHint(userEvents));

        // wrong answers
        double[] wrongAnswers = StatUtils.normalize(extractWrongAnswers(userEvents));

        // scoreTotal
        double[] totalScores = StatUtils.normalize(extractTotalScores(userEvents));

        // time played
        double[] timePlayed = StatUtils.normalize(extractTimePlayed(userEvents));

        // number of hints
        double[] numOfHints = StatUtils.normalize(extractHintsTaken(userEvents));

        for (int i = 0; i < userEvents.size(); i++) {
            result.add(new EuclideanDoublePoint(new double[]{
                ClusterMathUtils.handleNaN(timeAfterHint[i]),
                ClusterMathUtils.handleNaN(wrongAnswers[i]),
                ClusterMathUtils.handleNaN(totalScores[i]),
                ClusterMathUtils.handleNaN(timePlayed[i]),
                ClusterMathUtils.handleNaN(numOfHints[i])
            }));
        }
        return result;

    }

    /**
     * Converge list of points to clusters
     *
     * @param points list of points to converge
     * @param numberOfClusters number of clustesr
     * @return list of clusters
     */
    public <T extends Clusterable<T>> List<Cluster<T>> findClusters(List<T> points, int numberOfClusters) {
        KMeansPlusPlusClusterer<T> clusterer = new KMeansPlusPlusClusterer<>(new Random());
        if (points.size() < numberOfClusters) {
            return new ArrayList<>();
        }
        try {
            // sort the clusters to get consistent coloring on UI
            return clusterer.cluster(points, numberOfClusters, 5, -1).stream()
                    .sorted(Comparator.comparingInt(o -> o.getPoints().size()))
                    .collect(Collectors.toList());
        } catch (ConvergenceException e) {
            logger.error("Failed to converge specified number of clusters", e);
            return new ArrayList<>();
        }
    }

    /**
     * Calculate sum of squared errors from features
     *
     * @param features list of features
     * @param maxNumberOfClusters max number of clusters
     * @return list of values
     */
    public <T extends Clusterable<T>> List<Double> calculateSSE(List<T> features, int maxNumberOfClusters) {
        List<Double> result = new ArrayList<>();
        for (int curNumberOfClusters = 1; curNumberOfClusters < maxNumberOfClusters + 1; curNumberOfClusters++) {
            List<Cluster<T>> clusters = findClusters(features, curNumberOfClusters);
            double sum = 0;
            for (Cluster<T> cluster : clusters) {
                T center = cluster.getCenter();
                for (T point : cluster.getPoints()) {
                    double d = center.distanceFrom(point);
                    sum += Math.pow(d, 2);
                }
            }
            result.add(sum);
        }
        return result;
    }

    private double[] extractTimeAfterHint(Map<Long, List<AbstractAuditPOJO>> userEvents) {
        return userEvents.values().stream()
                .map(userEvs -> {
                    Map<Long, List<AbstractAuditPOJO>> collect = userEvs.stream()
                            .filter(trainingEvent -> trainingEvent instanceof HintTaken
                                    || trainingEvent instanceof CorrectAnswerSubmitted
                                    || trainingEvent instanceof SolutionDisplayed)
                            .collect(Collectors.groupingBy(AbstractAuditPOJO::getLevel, Collectors.toList()));
                    return collect.values().stream()
                            .map((events1) ->
                                    events1.get(events1.size() - 1).getTimestamp()
                                            - events1.get(0).getTimestamp())
                            .max(Long::compareTo)
                            .orElse(0L);
                }).mapToDouble(Long::doubleValue).toArray();
    }

    private double[] extractWrongAnswers(Map<Long, List<AbstractAuditPOJO>> userEvents) {
        return userEvents.values().stream()
                .map(userEvs -> userEvs.stream()
                        .filter(WrongAnswerSubmitted.class::isInstance)
                        .count())
                .mapToDouble(Long::doubleValue).toArray();
    }

    private double[] extractTotalScores(Map<Long, List<AbstractAuditPOJO>> userEvents) {
        return userEvents.values().stream()
                .map(userEvs -> userEvs.stream()
                        .map(AbstractAuditPOJO::getTotalTrainingScore)
                        .max(Integer::compareTo)
                        .orElse(0))
                .mapToDouble(Integer::doubleValue).toArray();
    }

    private double[] extractTimePlayed(Map<Long, List<AbstractAuditPOJO>> userEvents) {
        return userEvents.values().stream()
                .map(userEvs -> {
                    List<Long> collect = userEvs.stream()
                            .map(AbstractAuditPOJO::getTimestamp)
                            .collect(Collectors.toList());
                    return collect.get(collect.size() - 1) - collect.get(0);
                })
                .mapToDouble(Long::doubleValue).toArray();
    }

    private double[] extractHintsTaken(Map<Long, List<AbstractAuditPOJO>> userEvents) {
        return userEvents.values().stream()
                .map(userEvs -> userEvs.stream()
                        .filter(HintTaken.class::isInstance)
                        .count())
                .mapToDouble(Long::doubleValue).toArray();
    }

}
