package cz.muni.ics.kypo.training.rest.controllers;

import cz.muni.ics.kypo.training.api.dto.visualization.VisualizationInfoDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.HintClusterable;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.WrongAnswersClusterable;
import cz.muni.ics.kypo.training.facade.ClusterAnalysisFacade;
import cz.muni.ics.kypo.training.rest.ApiError;
import io.swagger.annotations.*;
import org.apache.commons.math3.stat.clustering.Cluster;
import org.apache.commons.math3.stat.clustering.EuclideanDoublePoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The rest controller for Visualizations.
 */
@Api(value = "/clusters",
        tags = "Clusters",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        authorizations = @Authorization(value = "bearerAuth"))
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource.", response = ApiError.class),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.", response = ApiError.class)
})
@RestController
@RequestMapping(value = "/clusters", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClusterAnalysisRestController {

    private static final Logger LOG = LoggerFactory.getLogger(VisualizationRestController.class);

    private ClusterAnalysisFacade clusterAnalysisFacade;

    /**
     * Instantiates a new Cluster Analysis rest controller.
     *
     * @param clusterAnalysisFacade the cluster analysis facade
     */
    @Autowired
    public ClusterAnalysisRestController(ClusterAnalysisFacade clusterAnalysisFacade) {
        this.clusterAnalysisFacade = clusterAnalysisFacade;
    }

    /**
     * Get N-dimensional clusters
     *
     * @param definitionId id of definition
     * @param instanceIds optional list of instance ids (all instances must be from the same definition)
     * @param numberOfClusters number of clusters
     * @param levelId optional level id
     * @return list of {@link Cluster}s
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get n-dimensional cluster.",
            response = VisualizationInfoDTO.class,
            nickname = "getNDimensionalCluster",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "N-dimensional cluster found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-definitions/{definitionId}/n-dimensional", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Cluster<EuclideanDoublePoint>>> getNDimensionalCluster(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable("definitionId") Long definitionId,
            @ApiParam(value = "List of training instance IDs", required = false) @RequestParam(value = "instanceIds", required = false) List<Long> instanceIds,
            @ApiParam(value = "Number of clusters", required = true) @RequestParam(value = "numberOfClusters") Integer numberOfClusters,
            @ApiParam(value = "Level id", required = false) @RequestParam(value = "levelId", required = false) Long levelId) {
        List<Cluster<EuclideanDoublePoint>> nDimensionalCluster = clusterAnalysisFacade.getNDimensionalCluster(definitionId, instanceIds, numberOfClusters, levelId);
        return ResponseEntity.ok(nDimensionalCluster);
    }

    /**
     * Get sum of squared errors for n-dimensional clusters
     *
     * @param definitionId id of definition
     * @param instanceIds optional list of instance ids (all instances must be from the same definition)
     * @param numberOfClusters number of clusters
     * @param levelId optional level id
     * @return list of values
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get n-dimensional cluster SSE.",
            response = Double[].class,
            nickname = "getNDimensionalClusterSSE",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "N-dimensional cluster SSE found.", response = Double[].class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-definitions/{definitionId}/n-dimensional/sse", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Double>> getNDimensionalClusterSSE(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable("definitionId") Long definitionId,
            @ApiParam(value = "List of training instance IDs", required = false) @RequestParam(value = "instanceIds", required = false) List<Long> instanceIds,
            @ApiParam(value = "Number of clusters", required = true) @RequestParam(value = "numberOfClusters") Integer numberOfClusters,
            @ApiParam(value = "Level id", required = false) @RequestParam(value = "levelId", required = false) Long levelId) {
        List<Double> nDimensionalClusterSSE = clusterAnalysisFacade.getNDimensionalSSE(definitionId, instanceIds, numberOfClusters, levelId);
        return ResponseEntity.ok(nDimensionalClusterSSE);
    }

    /**
     * Get hint clusters
     *
     * @param definitionId id of definition
     * @param instanceIds optional list of instance ids (all instances must be from the same definition)
     * @param numberOfClusters number of clusters
     * @param levelId optional level id
     * @return list of {@link HintClusterable}s
     */

    @ApiOperation(httpMethod = "GET",
            value = "Get hint cluster.",
            response = VisualizationInfoDTO.class,
            nickname = "getHintCluster",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Hint cluster found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-definitions/{definitionId}/hints", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Cluster<HintClusterable>>> getHintCluster(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable("definitionId") Long definitionId,
            @ApiParam(value = "List of training instance IDs", required = false) @RequestParam(value = "instanceIds", required = false) List<Long> instanceIds,
            @ApiParam(value = "Number of clusters", required = true) @RequestParam(value = "numberOfClusters") Integer numberOfClusters,
            @ApiParam(value = "Level id", required = false) @RequestParam(value = "levelId", required = false) Long levelId) {
        List<Cluster<HintClusterable>> hintCluster = clusterAnalysisFacade.getHintCluster(definitionId, instanceIds, numberOfClusters, levelId);
        return ResponseEntity.ok(hintCluster);
    }

    /**
     * Get sum of squared errors for hint clusters
     *
     * @param definitionId id of definition
     * @param instanceIds optional list of instance ids (all instances must be from the same definition)
     * @param numberOfClusters number of clusters
     * @param levelId optional level id
     * @return list of values
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get hint cluster SSE.",
            response = Double[].class,
            nickname = "getHintClusterSSE",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Hint cluster SSE found.", response = Double[].class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-definitions/{definitionId}/hints/sse", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Double>> getHintClusterSSE(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable("definitionId") Long definitionId,
            @ApiParam(value = "List of training instance IDs", required = false) @RequestParam(value = "instanceIds", required = false) List<Long> instanceIds,
            @ApiParam(value = "Number of clusters", required = true) @RequestParam(value = "numberOfClusters") Integer numberOfClusters,
            @ApiParam(value = "Level id", required = false) @RequestParam(value = "levelId", required = false) Long levelId) {
        List<Double> hintClusterSSE = clusterAnalysisFacade.getHintClusterSSE(definitionId, instanceIds, numberOfClusters, levelId);
        return ResponseEntity.ok(hintClusterSSE);
    }

    /**
     * Get wrong answer clusters
     *
     * @param definitionId id of definition
     * @param instanceIds optional list of instance ids (all instances must be from the same definition)
     * @param numberOfClusters number of clusters
     * @param levelId optional level id
     * @return list of {@link WrongAnswersClusterable}s
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get wrong answers cluster.",
            response = VisualizationInfoDTO.class,
            nickname = "getWrongAnswersCluster",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Wrong answers cluster found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-definitions/{definitionId}/wrong-answers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Cluster<WrongAnswersClusterable>>> getWrongAnswersCluster(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable("definitionId") Long definitionId,
            @ApiParam(value = "List of training instance IDs", required = false) @RequestParam(value = "instanceIds", required = false) List<Long> instanceIds,
            @ApiParam(value = "Number of clusters", required = true) @RequestParam(value = "numberOfClusters") Integer numberOfClusters,
            @ApiParam(value = "Level id", required = false) @RequestParam(value = "levelId", required = false) Long levelId) {
        List<Cluster<WrongAnswersClusterable>> wrongAnswersCluster = clusterAnalysisFacade.getWrongAnswersCluster(definitionId, instanceIds, numberOfClusters, levelId);
        return ResponseEntity.ok(wrongAnswersCluster);
    }

    /**
     * Get sum of squared errors for wrong answer clusters
     *
     * @param definitionId id of definition
     * @param instanceIds optional list of instance ids (all instances must be from the same definition)
     * @param numberOfClusters number of clusters
     * @param levelId optional level id
     * @return list of values
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get wrong answers SSE.",
            response = Double[].class,
            nickname = "getWrongAnswersClusterSSE",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Wrong answer cluster SSE found.", response = Double[].class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-definitions/{definitionId}/wrong-answers/sse", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Double>> getWrongAnswersClusterSSE(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable("definitionId") Long definitionId,
            @ApiParam(value = "List of training instance IDs", required = false) @RequestParam(value = "instanceIds", required = false) List<Long> instanceIds,
            @ApiParam(value = "Number of clusters", required = true) @RequestParam(value = "numberOfClusters") Integer numberOfClusters,
            @ApiParam(value = "Level id", required = false) @RequestParam(value = "levelId", required = false) Long levelId) {
        List<Double> wrongClusterSSE = clusterAnalysisFacade.getWrongAnswersSSE(definitionId, instanceIds, numberOfClusters, levelId);
        return ResponseEntity.ok(wrongClusterSSE);
    }
}
