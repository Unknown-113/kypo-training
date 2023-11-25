package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionMitreTechniquesDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.VisualizationInfoDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.analytical.TrainingInstanceAnalyticalDashboardDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.assessment.AssessmentVisualizationDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clustering.ClusteringVisualizationDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.compact.CompactLevelViewDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.leveltabs.LevelTabsLevelDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.timeline.TimelineDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.progress.VisualizationProgressDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.facade.visualization.AssessmentVisualizationFacade;
import cz.muni.ics.kypo.training.facade.visualization.VisualizationFacade;
import cz.muni.ics.kypo.training.facade.AnalyticalDashboardFacade;
import cz.muni.ics.kypo.training.facade.CompactLevelViewFacade;
import cz.muni.ics.kypo.training.rest.ApiError;
import cz.muni.ics.kypo.training.rest.utils.annotations.ApiPageableSwagger;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The rest controller for Visualizations.
 */
@Api(value = "/visualizations",
        tags = "Visualizations",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        authorizations = @Authorization(value = "bearerAuth"))
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource.", response = ApiError.class),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.", response = ApiError.class)
})
@RestController
@RequestMapping(value = "/visualizations", produces = MediaType.APPLICATION_JSON_VALUE)
public class VisualizationRestController {

    private static final Logger LOG = LoggerFactory.getLogger(VisualizationRestController.class);

    private VisualizationFacade visualizationFacade;
    private AssessmentVisualizationFacade assessmentVisualizationFacade;
    private AnalyticalDashboardFacade analyticalDashboardFacade;
    private CompactLevelViewFacade compactLevelViewFacade;
    private ObjectMapper objectMapper;

    /**
     * Instantiates a new Visualization rest controller.
     *
     * @param visualizationFacade the visualization facade
     * @param objectMapper        the object mapper
     */
    @Autowired
    public VisualizationRestController(VisualizationFacade visualizationFacade,
                                       AssessmentVisualizationFacade assessmentVisualizationFacade,
                                       AnalyticalDashboardFacade analyticalDashboardFacade,
                                       CompactLevelViewFacade compactLevelViewFacade,
                                       ObjectMapper objectMapper) {
        this.visualizationFacade = visualizationFacade;
        this.compactLevelViewFacade = compactLevelViewFacade;
        this.analyticalDashboardFacade = analyticalDashboardFacade;
        this.assessmentVisualizationFacade = assessmentVisualizationFacade;
        this.objectMapper = objectMapper;
    }

    /**
     * Gather all necessary information about levels of given training run to visualize results of the training run.
     *
     * @param runId id of training run.
     * @return necessary info about levels for specific training run and additional info about training definition.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary visualization info for training run.",
            response = VisualizationInfoDTO.class,
            nickname = "gatherVisualizationInfoForTrainingRun",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Visualization info found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-runs/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VisualizationInfoDTO> gatherVisualizationInfoForTrainingRun(@ApiParam(value = "Training run ID", required = true)
                                                                                      @PathVariable("runId") Long runId) {
        VisualizationInfoDTO visualizationInfoAboutTrainingRunDTO = visualizationFacade.getVisualizationInfoAboutTrainingRun(runId);
        return ResponseEntity.ok(visualizationInfoAboutTrainingRunDTO);
    }

    /**
     * Gather all commands in a training run.
     *
     * @param runId id of training run.
     * @return the list of commands including its timestamp.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all commands in a training run.",
            response = VisualizationInfoDTO.class,
            nickname = "gatherAllCommandsInTrainingRun",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Commands in training run.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/training-runs/{runId}/commands", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> gatherAllCommandsInTrainingRun(@ApiParam(value = "Training instance ID", required = true)
                                                                                    @PathVariable("instanceId") Long instanceId,
                                                                                    @ApiParam(value = "Training run ID", required = true)
                                                                                    @PathVariable("runId") Long runId) {
        return ResponseEntity.ok(visualizationFacade.getAllCommandsInTrainingRun(instanceId, runId));
    }

    /**
     * Gather all necessary information about levels of given training instance to visualize results of the training instance.
     *
     * @param trainingInstanceId id of training instance.
     * @return necessary info about levels for specific training instance and additional info about training definition.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary visualization info for training instance.",
            response = VisualizationInfoDTO.class,
            nickname = "gatherVisualizationInfoForTrainingInstance",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Visualization info found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VisualizationInfoDTO> gatherVisualizationInfoForTrainingInstance(@ApiParam(value = "Training instance ID", required = true)
                                                                                           @PathVariable("instanceId") Long trainingInstanceId) {
        VisualizationInfoDTO visualizationInfoDTO = visualizationFacade.getVisualizationInfoAboutTrainingInstance(trainingInstanceId);
        return ResponseEntity.ok(visualizationInfoDTO);
    }


    /**
     * Gather all necessary information about participants of the given training instance.
     *
     * @param trainingInstanceId id of training instance.
     * @return necessary info about participants specific training instance.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary info about participants for specific training instance.",
            response = UserRefDTO[].class,
            nickname = "getParticipantsForGivenTrainingInstance",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Visualization info found.", response = UserRefDTO[].class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserRefDTO>> getParticipantsForGivenTrainingInstance(@ApiParam(value = "Training instance ID", required = true)
                                                                                    @PathVariable("instanceId") Long trainingInstanceId) {
        List<UserRefDTO> participants = visualizationFacade.getParticipantsForGivenTrainingInstance(trainingInstanceId);
        return ResponseEntity.ok(participants);
    }

    /**
     * Gather all necessary information about users with given ids.
     *
     * @param pageable the pageable
     * @param usersIds ids of users to retrieve.
     * @return necessary info about participants specific training instance.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get users by IDs.",
            response = TrainingDefinitionsRestController.UserInfoRestResource.class,
            nickname = "getUsersByIds",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Visualization info found.", response = TrainingDefinitionsRestController.UserInfoRestResource.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUsersByIds(Pageable pageable,
                                                @ApiParam(value = "usersIds", required = true)
                                                @RequestParam Set<Long> usersIds) {
        PageResultResource<UserRefDTO> visualizationInfoDTO = visualizationFacade.getUsersByIds(usersIds, pageable);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, visualizationInfoDTO));
    }

    /**
     * Gather all necessary information for clustering visualization from all instances of the specified training definition
     * @param trainingDefinitionId id of training definition
     * @return {@link ClusteringVisualizationDTO} containing all the necessary information
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary clustering visualization data for training definition.",
            response = ClusteringVisualizationDTO.class,
            nickname = "getClusteringVisualizationsForTrainingDefinition",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data for visualization found.", response = ClusteringVisualizationDTO.class),
            @ApiResponse(code = 404, message = "Training definition with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-definitions/{definitionId}/clustering", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClusteringVisualizationDTO> getClusteringVisualizationsForTrainingDefinition(
            @ApiParam(value = "Training definition ID", required = true)
            @PathVariable("definitionId") Long trainingDefinitionId) {
        ClusteringVisualizationDTO clusteringVisualizationDTO =
                visualizationFacade.getClusteringVisualizationsForTrainingDefinition(trainingDefinitionId);
        return ResponseEntity.ok(clusteringVisualizationDTO);
    }

    /**
     * Gather all necessary information for clustering visualization from the specified instances
     * @param instanceIds ids of instances to use
     * @return {@link ClusteringVisualizationDTO} containing all the necessary information
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary clustering visualization data for the specified training instances.",
            response = ClusteringVisualizationDTO.class,
            nickname = "getClusteringVisualizationsForTrainingInstances",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data for visualization found.", response = ClusteringVisualizationDTO.class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/clustering", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClusteringVisualizationDTO> getClusteringVisualizationsForTrainingInstances(
            @ApiParam(value = "Training instance IDs", required = true)
            @RequestParam(value = "instanceIds", required = true) List<Long> instanceIds) {
        ClusteringVisualizationDTO clusteringVisualizationDTO = visualizationFacade.getClusteringForTrainingInstances(instanceIds);
        return ResponseEntity.ok(clusteringVisualizationDTO);
    }

    /**
     * Get data for clustering visualizations available for organizer.
     *
     * @param trainingInstanceId id of training instance.
     * @return data for clustering visualizations.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary clustering visualization data for organizer.",
            response = ClusteringVisualizationDTO.class,
            nickname = "getClusteringVisualizationsForOrganizer",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data for visualization found.", response = ClusteringVisualizationDTO.class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/clustering", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClusteringVisualizationDTO> getClusteringVisualizationsForOrganizer(@ApiParam(value = "Training instance ID", required = true)
                                                                                  @PathVariable("instanceId") Long trainingInstanceId) {
        ClusteringVisualizationDTO clusteringVisualizationDTO = visualizationFacade.getClusteringVisualizationsForOrganizer(trainingInstanceId);
        return ResponseEntity.ok(clusteringVisualizationDTO);
    }

    /**
     * Get data for clustering visualizations available for trainee.
     *
     * @param trainingRunId id of training run.
     * @return data for clustering visualizations.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary clustering visualization data for trainee.",
            response = ClusteringVisualizationDTO.class,
            nickname = "getClusteringVisualizationsForTrainee",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data for visualization found.", response = ClusteringVisualizationDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-runs/{runId}/clustering", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClusteringVisualizationDTO> getClusteringVisualizationsForTrainee(@ApiParam(value = "Training run ID", required = true)
                                                                                  @PathVariable("runId") Long trainingRunId) {
        ClusteringVisualizationDTO clusteringVisualizationDTO = visualizationFacade.getClusteringVisualizationsForTrainee(trainingRunId);
        return ResponseEntity.ok(clusteringVisualizationDTO);
    }

    /**
     * Get data for table visualizations available for organizer.
     *
     * @param trainingInstanceId id of training instance.
     * @return data for timeline visualizations.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary table visualization data for organizer.",
            response = PlayerDataDTO[].class,
            nickname = "getTableVisualizationsForOrganizer",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data for visualization found.", response = PlayerDataDTO[].class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/table", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PlayerDataDTO>> getTableVisualizationsForOrganizer(@ApiParam(value = "Training instance ID", required = true)
                                                                      @PathVariable("instanceId") Long trainingInstanceId) {
        List<PlayerDataDTO> tableVisualizationsDTO = visualizationFacade.getTableVisualizationsForOrganizer(trainingInstanceId);
        return ResponseEntity.ok(tableVisualizationsDTO);
    }

    /**
     * Get data for table dat visualizations.
     *
     * @param runId id of training run.
     * @return data for timeline visualizations.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary table visualization data for trainee.",
            response = PlayerDataDTO[].class,
            nickname = "getTableVisualizationsForTrainee",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data for visualization found.", response = PlayerDataDTO[].class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-runs/{runId}/table", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PlayerDataDTO>> getTableVisualizationsForTrainee(@ApiParam(value = "Training run ID", required = true)
                                                                      @PathVariable("runId") Long runId) {
        List<PlayerDataDTO> tableVisualizationsDTO = visualizationFacade.getTableVisualizationsForTrainee(runId);
        return ResponseEntity.ok(tableVisualizationsDTO);
    }

    /**
     * Get data for level tabs visualizations.
     *
     * @param trainingInstanceId id of training instance.
     * @return data for level tabs visualizations.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary level tabs visualization info for training instance.",
            response = LevelTabsLevelDTO[].class,
            nickname = "getLevelTabsVisualizations",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data for visualization found.", response = LevelTabsLevelDTO[].class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/level-tabs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LevelTabsLevelDTO>> getLevelTabsVisualizations(@ApiParam(value = "Training instance ID", required = true)
                                                                              @PathVariable("instanceId") Long trainingInstanceId) {
        List<LevelTabsLevelDTO> levelTabsVisualizationsDTO = visualizationFacade.getLevelTabsVisualizations(trainingInstanceId);
        return ResponseEntity.ok(levelTabsVisualizationsDTO);
    }

    /**
     * Get data for timeline visualizations available for organizer.
     *
     * @param trainingInstanceId id of training instance.
     * @return data for timeline visualizations.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary timeline visualization data for organizer.",
            response = TimelineDTO.class,
            nickname = "getTimelineVisualizationsForOrganizer",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data for visualization found.", response = TimelineDTO.class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/timeline", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TimelineDTO> getTimelineVisualizationsForOrganizer(@ApiParam(value = "Training instance ID", required = true)
                                                                 @PathVariable("instanceId") Long trainingInstanceId) {
        TimelineDTO timelineVisualizationDTO = visualizationFacade.getTimelineVisualizationsForTrainingInstance(trainingInstanceId);
        return ResponseEntity.ok(timelineVisualizationDTO);
    }

    /**
     * Get data for timeline visualizations available for trainee.
     *
     * @param runId id of training run.
     * @return data for timeline visualizations.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary timeline visualization data for trainee.",
            response = TimelineDTO.class,
            nickname = "getTimelineVisualizationsForTrainee",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data for visualization found.", response = TimelineDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-runs/{runId}/timeline", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TimelineDTO> getTimelineVisualizationsForTrainee(@ApiParam(value = "Training run ID", required = true)
                                                                 @PathVariable("runId") Long runId) {
        TimelineDTO timelineVisualizationDTO = visualizationFacade.getTimelineVisualizationsForTrainee(runId);
        return ResponseEntity.ok(timelineVisualizationDTO);
    }

    /**
     * Gather all necessary information about levels of given training instance to visualize results of the training instance.
     *
     * @param trainingInstanceId id of training instance.
     * @return necessary info about levels for specific training instance and additional info about training definition.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary visualization info for training instance.",
            response = VisualizationInfoDTO.class,
            nickname = "gatherVisualizationInfoForTrainingInstanceProgress",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Visualization info found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/progress", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getProgressVisualizations(@ApiParam(value = "Training instance ID", required = true) @PathVariable("instanceId") Long trainingInstanceId) {
        VisualizationProgressDTO visualizationProgressDTO = visualizationFacade.getProgressVisualization(trainingInstanceId);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, visualizationProgressDTO));
    }

    /**
     * Get data for analytical dashboard.
     *
     * @param definitionId id of training definition.
     * @return data for analytical dashboard.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary data for analytical dashboard.",
            response = TrainingInstanceAnalyticalDashboardDTO[].class,
            nickname = "analytical dashboard.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data for analytical dashboard found.", response = TimelineDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-definitions/{definitionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TrainingInstanceAnalyticalDashboardDTO>> getAnalyticalDashboard(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable("definitionId") Long definitionId) {
        List<TrainingInstanceAnalyticalDashboardDTO> result = analyticalDashboardFacade.getDataForAnalyticalDashboard(definitionId);
        return ResponseEntity.ok(result);
    }

    /**
     * Gather all mitre techniques of the training definitions with indication if the definition has been played by user.
     *
     * @return summarized mitre techniques from all training definitions
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get summarized mitre techniques.",
            response = TrainingDefinitionMitreTechniquesDTO[].class,
            nickname = "getSummarizedMitreTechniques",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Mitre techniques found.", response = TrainingDefinitionMitreTechniquesDTO[].class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-definitions/mitre-techniques", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getTrainingDefinitionsWithMitreTechniques() {
        List<TrainingDefinitionMitreTechniquesDTO> trainingDefinitionMitreTechniquesDTOS = visualizationFacade.getTrainingDefinitionsWithMitreTechniques();
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionMitreTechniquesDTOS));
    }

    /**
     * Get data for compact level view visualization.
     *
     * @param instanceId id of training instance.
     * @param levelId id of level.
     * @return data for compact level view visualization.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get necessary data for compact level view visualization.",
            response = CompactLevelViewDTO.class,
            nickname = "compact level view visualization",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data for analytical dashboard found.", response = TimelineDTO.class),
            @ApiResponse(code = 404, message = "Training run with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/levels/{levelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompactLevelViewDTO> getCompactLevelViewVisualization(
            @ApiParam(value = "Training instance ID", required = true) @PathVariable Long instanceId,
            @ApiParam(value = "Level ID", required = true) @PathVariable Long levelId) {
        return ResponseEntity.ok(compactLevelViewFacade.getCompactLevelViewData(instanceId, levelId));
    }

    /**
     * Get data for assessment visualizations. Return list of relevant data for each assessment level that can be found in the definition
     * of the particular training instance.
     *
     * @return assessment visualization data
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get assessment visualization data.",
            response = AssessmentVisualizationDTO[].class,
            nickname = "getAssessmentVisualizations"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data for assessment visualization found.", response = AssessmentVisualizationDTO[].class),
            @ApiResponse(code = 404, message = "Training instance with given id not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/assessments")
    public ResponseEntity<Object> getAssessmentVisualizations(@ApiParam(value = "Training instance ID", required = true) @PathVariable("instanceId") Long instanceId) {
        List<AssessmentVisualizationDTO> assessmentVisualizationDTOs = assessmentVisualizationFacade.getAssessmentVisualizationData(instanceId);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, assessmentVisualizationDTOs));
    }

}
