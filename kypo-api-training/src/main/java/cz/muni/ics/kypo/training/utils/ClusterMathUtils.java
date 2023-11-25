package cz.muni.ics.kypo.training.utils;

import org.apache.commons.math3.stat.clustering.Clusterable;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Util methods for feature normalization and other math functions.
 */

public class ClusterMathUtils {

    /**
     * Simple 2D distance calculation
     *
     * @param firstX  first X coordinate
     * @param secondX second X coordinate
     * @param firstY  first Y coordinate
     * @param secondY second X coordinate
     * @return distance between given points
     */
    public static Double calculateDistance2D(Double firstX, Double secondX, Double firstY, Double secondY) {
        return FastMath.sqrt(
                FastMath.pow(FastMath.abs(firstX - secondX), 2)
                        + FastMath.pow(FastMath.abs(firstY - secondY), 2));
    }

    /**
     * Normalizes given sample.
     *
     * @param sample    list of given clusterables
     * @param suppliers functions to extract and insert values into objects
     * @param <T>       clusterable type
     * @return list of normalized values
     */
    @SafeVarargs
    public static <T extends Clusterable<T>> List<T> normalize(List<T> sample, Pair<Function<T, Double>, BiConsumer<T, Double>>... suppliers) {
        for (Pair<Function<T, Double>, BiConsumer<T, Double>> supplierConsumerPair : suppliers) {

            DescriptiveStatistics stats = new DescriptiveStatistics();

            // Add the data from the series to stats
            for (T t : sample) {
                stats.addValue(supplierConsumerPair.getFirst().apply(t));
            }

            // Compute mean and standard deviation
            double mean = handleNaN(stats.getMean());
            double standardDeviation = handleNaN(stats.getStandardDeviation());

            if (standardDeviation == 0) {
                standardDeviation = 1;
            }
            for (T t : sample) {
                // z = (x- mean)/standardDeviation
                supplierConsumerPair.getSecond().accept(t, (supplierConsumerPair.getFirst().apply(t) - mean) / standardDeviation);
            }

        }
        return sample;
    }

    public static double handleNaN(Double value) {
        return Double.isNaN(value) ? 0.0 : value;
    }

}
