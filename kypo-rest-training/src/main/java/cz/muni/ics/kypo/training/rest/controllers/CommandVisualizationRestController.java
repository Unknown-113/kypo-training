package cz.muni.ics.kypo.training.rest.controllers;

import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.CommandDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.VisualizationInfoDTO;
import cz.muni.ics.kypo.training.api.enums.MistakeType;
import cz.muni.ics.kypo.training.facade.visualization.CommandVisualizationFacade;
import cz.muni.ics.kypo.training.rest.ApiError;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * The rest controller for Command Visualizations.
 */
@Api(value = "/visualizations",
        tags = "Command Visualizations",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        authorizations = @Authorization(value = "bearerAuth"))
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource.", response = ApiError.class),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.", response = ApiError.class),
        @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
})
@RestController
@RequestMapping(path = "/visualizations", produces = MediaType.APPLICATION_JSON_VALUE)
public class CommandVisualizationRestController {

    private static final Logger LOG = LoggerFactory.getLogger(CommandVisualizationRestController.class);

    private final CommandVisualizationFacade commandVisualizationFacade;

    /**
     * Instantiates a new Visualization rest controller.
     *
     * @param commandVisualizationFacade the command visualization facade
     */
    @Autowired
    public CommandVisualizationRestController(CommandVisualizationFacade commandVisualizationFacade) {
        this.commandVisualizationFacade = commandVisualizationFacade;
    }

    /**
     * Get reference graph for the given training definition.
     *
     * @param definitionId ID of training definition.
     * @return graph that represents the recommended execution of the commands during the levels and transitions among them.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get reference graph for the given training definition.",
            response = Object.class,
            nickname = "getReferenceGraphForDesigner",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Reference graph found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Reference graph for the given training definition not found.", response = ApiError.class)
    })
    @GetMapping(path = "/graphs/reference/training-definitions/{definitionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getReferenceGraphForDesigner(
            @ApiParam(value = "Training definition ID.", required = true) @PathVariable("definitionId") Long definitionId
    ) {
        return ResponseEntity.ok(commandVisualizationFacade.getReferenceGraphByDefinitionId(definitionId));
    }

    /**
     * Get reference graph for the given training instance.
     *
     * @param instanceId ID of training instance.
     * @return graph that represents the recommended execution of the commands during the levels and transitions among them.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get reference graph for the given training instance.",
            response = Object.class,
            nickname = "getReferenceGraphForInstructor",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Reference graph found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Reference graph for the given training instance not found.", response = ApiError.class)
    })
    @GetMapping(path = "/graphs/reference/training-instances/{instanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getReferenceGraphForInstructor(
            @ApiParam(value = "Training instance ID.", required = true) @PathVariable("instanceId") Long instanceId
    ) {
        return ResponseEntity.ok(commandVisualizationFacade.getReferenceGraphByInstanceId(instanceId));
    }

    /**
     * Get reference graph for the given training run.
     *
     * @param runId id of training run.
     * @return graph that represents the recommended command execution during the individual levels.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get reference graph for the given training run.",
            response = Object.class,
            nickname = "getReferenceGraphForTrainee",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Reference graph found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Reference graph for the given training run not found.", response = ApiError.class)
    })
    @GetMapping(path = "/graphs/reference/training-runs/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getReferenceGraphForTrainee(
            @ApiParam(value = "Training run ID", required = true) @PathVariable("runId") Long runId
    ) {
        return ResponseEntity.ok(commandVisualizationFacade.getReferenceGraphByRunId(runId));
    }

    /**
     * Get summary graph for the given training instance.
     *
     * @param instanceId ID of training instance.
     * @return graph that represents the summarized command execution of the trainees during the training.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get summary graph for the given training instance.",
            response = Object.class,
            nickname = "getSummaryGraph",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Summary graph found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Summary graph for the given training instance not found.", response = ApiError.class)
    })
    @GetMapping(path = "/graphs/summary/training-instances/{instanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getSummaryGraph(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable("instanceId") Long instanceId
    ) {
        return ResponseEntity.ok(commandVisualizationFacade.getSummaryGraph(instanceId));
    }

    /**
     * Get trainee graph for the given training run.
     *
     * @param runId ID of training run.
     * @return graph that represents the command execution of the trainee during the training.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get reference graph for the given training run.",
            response = Object.class,
            nickname = "getReferenceGraphForTrainee",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Reference graph found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Reference graph for the given training run not found.", response = ApiError.class)
    })
    @GetMapping(path = "/graphs/trainee/training-runs/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getTraineeGraph(
            @ApiParam(value = "Training definition ID", required = true) @PathVariable("runId") Long runId
    ) {
        return ResponseEntity.ok(commandVisualizationFacade.getTraineeGraph(runId));
    }

    /**
     * Get aggregated correct/incorrect commands for the given training runs of the specific training instance.
     *
     * @param runIds IDs of training run.
     * @return semantically and syntactically correct/incorrect commands executed during the given training runs and aggregated by tools and type.
     * The frequency of the used tools is also included.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get aggregated correct/incorrect commands executed during the given training runs.",
            response = Object.class,
            nickname = "getAggregatedCommandsForOrganizer",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Aggregated correct/incorrect commands found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Aggregated correct/incorrect commands for the given training runs not found.", response = ApiError.class)
    })
    @GetMapping(path = "/commands/training-instances/{instanceId}/aggregated", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAggregatedCommandsForOrganizer(
            @ApiParam(value = "Training Instance ID", required = true) @PathVariable Long instanceId,
            @ApiParam(value = "Training Run IDs", required = true, type = "List of numbers") @RequestParam List<Long> runIds,
            @ApiParam(value = "If correct or incorrect commands should be returned.") @RequestParam boolean correct,
            @ApiParam(value = "Mistake types", type = "List of strings") @RequestParam(required = false) List<MistakeType> mistakeTypes
    ) {
        return ResponseEntity.ok(commandVisualizationFacade.getAggregatedCommandsForOrganizer(instanceId, runIds, correct, mistakeTypes));
    }

    /**
     * Get aggregated correct/incorrect commands for the given training run.
     *
     * @param runId ID of training run.
     * @return semantically and syntactically correct/incorrect commands executed during the given training run and aggregated by tools and type.
     * The frequency of the used tools is also included.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get aggregated correct/incorrect commands executed during the given training runs.",
            response = Object.class,
            nickname = "getAggregatedCommandsForTrainee",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Aggregated correct/incorrect commands found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Aggregated correct/incorrect commands for the given training runs not found.", response = ApiError.class)
    })
    @GetMapping(path = "/commands/training-runs/{runId}/aggregated", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAggregatedCommandsForTrainee(
            @ApiParam(value = "Training Run ID", required = true) @PathVariable Long runId,
            @ApiParam(value = "If correct or incorrect commands should be returned.") @RequestParam boolean correct,
            @ApiParam(value = "Mistake types", type = "List of strings") @RequestParam(required = false) List<MistakeType> mistakeTypes
    ) {
        return ResponseEntity.ok(commandVisualizationFacade.getAggregatedCommandsForTrainee(runId, correct, mistakeTypes));
    }

    /**
     * Get all commands executed in the given finished training run.
     *
     * @param runId ID of training run.
     * @return all commands executed during the given training run.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get commands executed during the given training run.",
            response = Object.class,
            nickname = "getAllCommandsByTrainingRun",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Commands for the given training run found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Commmands for the given training run not found.", response = ApiError.class)
    })
    @GetMapping(path = "/commands/training-runs/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllCommandsByTrainingRun(
            @ApiParam(value = "Training Run ID", required = true) @PathVariable Long runId
    ) {
        return ResponseEntity.ok(commandVisualizationFacade.getAllCommandsByFinishedTrainingRun(runId));
    }

    /**
     * Get all finished training runs in the given training instance.
     *
     * @param instanceId ID of training instance.
     * @return all finished training runs of the given training instance.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get finished training runs of the training instance.",
            response = Object.class,
            nickname = "findFinishedTrainingRunsByTrainingInstanceId",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Finished runs in the given training instance found.", response = VisualizationInfoDTO.class),
            @ApiResponse(code = 404, message = "Finished runs in the given training instance not found.", response = ApiError.class)
    })
    @GetMapping(path = "/training-instances/{instanceId}/training-runs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TrainingRunDTO>> findFinishedTrainingRunsByTrainingInstanceId(
            @ApiParam(value = "Training Instance ID", required = true) @PathVariable("instanceId") Long instanceId
    ) {
        List<TrainingRunDTO> trainingRuns = commandVisualizationFacade.findTrainingRunsByTrainingInstance(instanceId);
        return ResponseEntity.ok(trainingRuns);
    }

    /**
     * Get all commands executed in the given training instance associated by training runs
     * @param instanceId ID of training instance.
     * @return all commands executed in the given training instance associated by training runs
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all commands executed in the given training instance associated by training runs",
            response = Object.class,
            nickname = "getAllCommandsByTrainingInstance",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All commands for the given training instance found", response = Map.class),
            @ApiResponse(code = 404, message = "All commands for the given training instance not found", response = ApiError.class)
    })
    @GetMapping(path = "/commands/training-instances/{instanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<Long, List<CommandDTO>>> getAllCommandsByTrainingInstance(
            @ApiParam(value = "Training Instance ID", required = true) @PathVariable("instanceId") Long instanceId
    ) {
        return ResponseEntity.ok(commandVisualizationFacade.getAllCommandsByTrainingInstance(instanceId));
    }


    /**
     * Get all commands executed in the given Training Run
     * @param runId ID of Training Run
     * @return all commands executed in the given Training Run
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all commands executed in the given Training Run",
            response = Object.class,
            nickname = "getAllCommandsByRun",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All commands for the given Training Run found", response = List.class),
            @ApiResponse(code = 404, message = "All commands for the given Training Run not found", response = ApiError.class)
    })
    @GetMapping(path = "/commands/training-runs/{runId}/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CommandDTO>> getAllCommandsByRun(
            @ApiParam(value = "Training Run ID", required = true) @PathVariable("runId") Long runId
    ) {
        return ResponseEntity.ok(commandVisualizationFacade.getAllCommandsByTrainingRun(runId));
    }
}
