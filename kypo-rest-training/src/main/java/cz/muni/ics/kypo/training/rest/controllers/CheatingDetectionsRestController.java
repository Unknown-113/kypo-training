package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import cz.muni.ics.kypo.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.cheatingdetection.*;
import cz.muni.ics.kypo.training.api.dto.export.FileToReturnDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.facade.CheatingDetectionFacade;
import cz.muni.ics.kypo.training.rest.ApiError;
import cz.muni.ics.kypo.training.rest.utils.annotations.ApiPageableSwagger;
import cz.muni.ics.kypo.training.utils.AbstractFileExtensions;
import io.swagger.annotations.*;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Time;
import java.util.List;

/**
 * The rest controller for Cheating detections.
 */

@Api(value = "/cheating-detections",
        tags = "Cheating detection",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        authorizations = @Authorization(value = "bearerAuth"))
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource.", response = ApiError.class),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.", response = ApiError.class)
})
@RestController
@RequestMapping(value = "/cheating-detections", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class CheatingDetectionsRestController {

    private CheatingDetectionFacade cheatingDetectionFacade;
    private ObjectMapper objectMapper;

    /**
     * Instantiates a new Cheating detections rest controller.
     *
     * @param cheatingDetectionFacade the cheating detection facade
     * @param objectMapper            the object mapper
     */
    @Autowired
    public CheatingDetectionsRestController(CheatingDetectionFacade cheatingDetectionFacade,
                                            ObjectMapper objectMapper) {
        this.cheatingDetectionFacade = cheatingDetectionFacade;
        this.objectMapper = objectMapper;
    }

    /**
     * Create and executed a cheating detection.
     *
     * @param cheatingDetectionDTO the Cheating Detection object to be created
     * @return the new Training Definition
     */
    @ApiOperation(httpMethod = "POST",
            value = "Create and Execute cheating detection",
            response = TrainingDefinitionByIdDTO.class,
            nickname = "createAndExecuteCheatingDetection",
            notes = "This can only be done by organizer of training instance or administrator.",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The Cheating Detection has been created and executed.", response = CheatingDetectionDTO.class),
            @ApiResponse(code = 400, message = "The provided cheating detection is not valid", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @PostMapping(path = "/create-detection", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createAndExecuteCheatingDetection(@ApiParam(value = "CheatingDetection to be created")
                                                                  @RequestBody @Valid CheatingDetectionDTO cheatingDetectionDTO) {
        cheatingDetectionFacade.createAndExecute(cheatingDetectionDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * Reruns All Cheating Detections on cheating detection.
     *
     * @param cheatingDetectionId id of cheating detection.
     * @param trainingInstanceId id of training instance.
     * @return the response entity
     */
    @ApiOperation(httpMethod = "PATCH",
            value = "rerun cheating detection",
            nickname = "rerunCheatingDetection",
            notes = "This can only be done by organizer of training instance or administrator."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The cheating detection was executed."),
            @ApiResponse(code = 404, message = "The cheating detection has not been found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @PatchMapping(path = "/{cheatingDetectionId}/rerun/{trainingInstanceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> rerunCheatingDetection(@ApiParam(value = "Cheating Detection ID", required = true)
                                                       @PathVariable("cheatingDetectionId") Long cheatingDetectionId,
                                                       @ApiParam(value = "id of training instance", required = true)
                                                       @PathVariable("trainingInstanceId") Long trainingInstanceId) {
        cheatingDetectionFacade.rerunCheatingDetection(cheatingDetectionId, trainingInstanceId);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a cheating detection and all its associated events.
     *
     * @param cheatingDetectionId id of cheating detection.
     * @param trainingInstanceId id of training instance.
     * @return the response entity
     */
    @ApiOperation(httpMethod = "DELETE",
            value = "Delete detection events of cheating detection",
            nickname = "deleteDetectionEventsOfCheatingDetection",
            notes = "This can only be done by organizer of training instance or administrator.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The detection events have been deleted."),
            @ApiResponse(code = 404, message = "The cheating detection has not been found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered", response = ApiError.class)
    })
    @DeleteMapping(path = "/{cheatingDetectionId}/delete")
    public ResponseEntity<Void> deleteDetectionEvents(@ApiParam(value = "Cheating detection ID", required = true)
                                                      @PathVariable("cheatingDetectionId") Long cheatingDetectionId,
                                                      @ApiParam(value = "id of training instance", required = true)
                                                      @RequestParam(value = "trainingInstanceId")  Long trainingInstanceId) {
        cheatingDetectionFacade.deleteCheatingDetection(cheatingDetectionId, trainingInstanceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Get all detection events of a CheatingDetection.
     *
     * @param cheatingDetectionId id of cheating detection.
     * @param trainingInstanceId id of training instance.
     * @param pageable            pageable parameter with information about pagination.
     * @param fields              attributes of the object to be returned as the result.
     * @return all Detection Events occurred in a cheating detection.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all detection events of cheating detection.",
            response = DetectionEventRestResource.class,
            nickname = "findAllDetectionEvents",
            notes = "This can only be done by organizer of training instance or administrator.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Detection Events have been found.", response = DetectionEventRestResource.class),
            @ApiResponse(code = 404, message = "The cheating detection has not been found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(path = "/{cheatingDetectionId}/find-all-events", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findAllDetectionEventsOfCheatingDetection(@ApiParam(value = "id of cheating detection", required = true)
                                                                            @PathVariable("cheatingDetectionId") Long cheatingDetectionId,
                                                                            @ApiParam(value = "id of training instance", required = true)
                                                                            @RequestParam(value = "trainingInstanceId", required = true) Long trainingInstanceId,
                                                                            @ApiParam(value = "Pagination support.", required = false) Pageable pageable,
                                                                            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                                            @RequestParam(value = "fields", required = false) String fields) {
        PageResultResource<AbstractDetectionEventDTO> detectionEventResource = cheatingDetectionFacade.findAllDetectionEventsOfCheatingDetection(cheatingDetectionId, trainingInstanceId, pageable);
        Squiggly.init(objectMapper, fields);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, detectionEventResource), HttpStatus.OK);
    }

    /**
     * Get all participants of Detection Event.
     *
     * @param eventId             id of detection event.
     * @param pageable            pageable parameter with information about pagination.
     * @param fields              attributes of the object to be returned as the result.
     * @return all Detection Events occurred in a cheating detection.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all participants of detection event.",
            response = DetectionEventParticipantRestResource.class,
            nickname = "findAllParticipantsOfEvent",
            notes = "This can only be done by organizer of training instance or administrator.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Participants have been found.", response = DetectionEventRestResource.class),
            @ApiResponse(code = 404, message = "The participants have not been found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(path = "/find-all-participants", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findAllParticipantsOfDetectionEvent(@ApiParam(value = "the event id", required = true)
                                                                      @RequestParam(value = "eventId", required = true) Long eventId,
                                                                      @ApiParam(value = "Pagination support.", required = false) Pageable pageable,
                                                                      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                                      @RequestParam(value = "fields", required = false) String fields) {
        PageResultResource<DetectionEventParticipantDTO> participantsResource = cheatingDetectionFacade.findAllParticipantsOfDetectionEvent(eventId, pageable);
        Squiggly.init(objectMapper, fields);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, participantsResource), HttpStatus.OK);
    }

    /**
     * Archive cheating detection
     *
     * @param cheatingDetectionId the cheating detection id
     * @return file containing wanted cheating detection results
     */
    @ApiOperation(httpMethod = "GET",
            value = "Archive cheating detection results",
            response = CheatingDetectionDTO.class,
            nickname = "archiveCheatingDetectionResults",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Cheating Detection results archived.", response = TrainingInstanceArchiveDTO.class),
            @ApiResponse(code = 404, message = "Cheating Detection not found.", response = ApiError.class),
            @ApiResponse(code = 409, message = "Cannot archive detection that is not finished.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/exports/{cheatingDetectionId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> archiveCheatingDetectionResults(
            @ApiParam(value = "Id of cheating detection", required = true)
            @PathVariable("cheatingDetectionId") Long cheatingDetectionId) {
        FileToReturnDTO file = cheatingDetectionFacade.archiveCheatingDetectionResults(cheatingDetectionId);
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "octet-stream"));
        header.set("Content-Disposition", "inline; filename=" + file.getTitle() + AbstractFileExtensions.ZIP_FILE_EXTENSION);
        header.setContentLength(file.getContent().length);
        return new ResponseEntity<>(file.getContent(), header, HttpStatus.OK);
    }

    /**
     * Detection Event by ID.
     *
     * @param eventId the detection event id
     * @return detection event.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Detection event.",
            response = AbstractDetectionEventDTO.class,
            nickname = "findDetectionEventById",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The detection event has been found.", response = AccessTrainingRunDTO.class),
            @ApiResponse(code = 404, message = "The detection event has not been found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/find-event", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AbstractDetectionEventDTO> findDetectionEventById(@ApiParam(value = "eventId", required = true)
                                                                  @RequestParam(value = "eventId", required = true) Long eventId) {
        AbstractDetectionEventDTO abstractDetectionEventDTO = cheatingDetectionFacade.findDetectionEventById(eventId);
        return ResponseEntity.ok(abstractDetectionEventDTO);
    }

    /**
     * Detection Event of type Answer Similarity by ID.
     *
     * @param eventId the detection event id
     * @return detection event.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Answer Similarity Detection event.",
            response = AnswerSimilarityDetectionEventDTO.class,
            nickname = "findDetectionEventOfAnswerSimilarityById",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The detection event has been found.", response = AccessTrainingRunDTO.class),
            @ApiResponse(code = 404, message = "The detection event has not been found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/find-answer-similarity", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnswerSimilarityDetectionEventDTO> findAnswerSimilarityDetectionEventById(@ApiParam(value = "eventId", required = true)
                                                                                                    @RequestParam(value = "eventId", required = true) Long eventId) {
        AnswerSimilarityDetectionEventDTO answerSimilarityDetectionEventDTO = cheatingDetectionFacade.findAnswerSimilarityEventById(eventId);
        return ResponseEntity.ok(answerSimilarityDetectionEventDTO);
    }

    /**
     * Detection Event of type Location Similarity by ID.
     *
     * @param eventId the detection event id
     * @return detection event.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Location Similarity Detection event.",
            response = LocationSimilarityDetectionEventDTO.class,
            nickname = "findDetectionEventOfLocationSimilarityById",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The detection event has been found.", response = AccessTrainingRunDTO.class),
            @ApiResponse(code = 404, message = "The detection event has not been found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/find-location-similarity", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LocationSimilarityDetectionEventDTO> findLocationSimilarityDetectionEventById(@ApiParam(value = "eventId", required = true)
                                                                                                        @RequestParam(value = "eventId", required = true) Long eventId) {
        LocationSimilarityDetectionEventDTO locationSimilarityDetectionEventDTO = cheatingDetectionFacade.findLocationSimilarityEventById(eventId);
        return ResponseEntity.ok(locationSimilarityDetectionEventDTO);
    }

    /**
     * Detection Event of type Time Proximity by ID.
     *
     * @param eventId the detection event id
     * @return detection event.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Time Proximity Detection event.",
            response = TimeProximityDetectionEventDTO.class,
            nickname = "findDetectionEventOfTimeProximityById",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The detection event has been found.", response = AccessTrainingRunDTO.class),
            @ApiResponse(code = 404, message = "The detection event has not been found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/find-time-proximity", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TimeProximityDetectionEventDTO> findTimeProximityDetectionEventById(@ApiParam(value = "eventId", required = true)
                                                                                              @RequestParam(value = "eventId", required = true) Long eventId) {
        TimeProximityDetectionEventDTO timeProximityDetectionEventDTO = cheatingDetectionFacade.findTimeProximityEventById(eventId);
        return ResponseEntity.ok(timeProximityDetectionEventDTO);
    }

    /**
     * Detection Event of type Minimal Solve Time by ID.
     *
     * @param eventId the detection event id
     * @return detection event.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Minimal Solve Time Detection event.",
            response = MinimalSolveTimeDetectionEventDTO.class,
            nickname = "findDetectionEventOfMinimalSolveTimeById",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The detection event has been found.", response = AccessTrainingRunDTO.class),
            @ApiResponse(code = 404, message = "The detection event has not been found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/find-minimal-solve-time", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MinimalSolveTimeDetectionEventDTO> findMinimalSolveTimeDetectionEventById(@ApiParam(value = "eventId", required = true)
                                                                                                    @RequestParam(value = "eventId", required = true) Long eventId) {
        MinimalSolveTimeDetectionEventDTO minimalSolveTimeDetectionEventDTO = cheatingDetectionFacade.findMinimalSolveTimeEventById(eventId);
        return ResponseEntity.ok(minimalSolveTimeDetectionEventDTO);
    }

    /**
     * Detection Event of type No Commands by ID.
     *
     * @param eventId the detection event id
     * @return detection event.
     */
    @ApiOperation(httpMethod = "GET",
            value = "No Commands Detection event.",
            response = NoCommandsDetectionEventDTO.class,
            nickname = "findDetectionEventOfNoCommandsById",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The detection event has been found.", response = AccessTrainingRunDTO.class),
            @ApiResponse(code = 404, message = "The detection event has not been found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/find-no-commands", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NoCommandsDetectionEventDTO> findNoCommandsDetectionEventById(@ApiParam(value = "eventId", required = true)
                                                                                        @RequestParam(value = "eventId", required = true) Long eventId) {
        NoCommandsDetectionEventDTO NoCommandsDetectionEventDTO = cheatingDetectionFacade.findNoCommandsEventById(eventId);
        return ResponseEntity.ok(NoCommandsDetectionEventDTO);
    }

    /**
     * Detection Event of type Forbidden Commands by ID.
     *
     * @param eventId the detection event id
     * @return detection event.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Forbidden Commands Detection event.",
            response = ForbiddenCommandsDetectionEventDTO.class,
            nickname = "findDetectionEventOfForbiddenCommandsById",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The detection event has been found.", response = AccessTrainingRunDTO.class),
            @ApiResponse(code = 404, message = "The detection event has not been found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/find-forbidden-commands", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ForbiddenCommandsDetectionEventDTO> findForbiddenCommandsDetectionEventById(@ApiParam(value = "eventId", required = true)
                                                                                                      @RequestParam(value = "eventId", required = true) Long eventId) {
        ForbiddenCommandsDetectionEventDTO forbiddenCommandsDetectionEventDTO = cheatingDetectionFacade.findForbiddenCommandsEventById(eventId);
        return ResponseEntity.ok(forbiddenCommandsDetectionEventDTO);
    }

    /**
     * Get all cheating detections of a training instance.
     *
     * @param trainingInstanceId id of training instance.
     * @param pageable           pageable parameter with information about pagination.
     * @param fields             attributes of the object to be returned as the result.
     * @return all cheating Detections occurred in a training instance.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all cheating detections of training instance.",
            response = CheatingDetectionRestResource.class,
            nickname = "findAllCheatingDetections",
            notes = "This can only be done by organizer of training instance or administrator.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Cheating Detections have been found.", response = CheatingDetectionRestResource.class),
            @ApiResponse(code = 404, message = "The training instance has not been found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(path = "/{trainingInstanceId}/find-all-detections", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findAllCheatingDetectionsOfInstance(@ApiParam(value = "id of training instance", required = true)
                                                                      @PathVariable("trainingInstanceId") Long trainingInstanceId,
                                                                      @ApiParam(value = "Pagination support.", required = false) Pageable pageable,
                                                                      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                                      @RequestParam(value = "fields", required = false) String fields) {
        PageResultResource<CheatingDetectionDTO> cheatingDetectionResource = cheatingDetectionFacade.findAllCheatingDetectionsOfTrainingInstance(trainingInstanceId, pageable);
        Squiggly.init(objectMapper, fields);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, cheatingDetectionResource), HttpStatus.OK);
    }

    /**
     * The type Detection Event rest resource.
     */
    @ApiModel(value = "DetectionEventRestResource",
            description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
    public static class DetectionEventRestResource extends PageResultResource<AbstractDetectionEventDTO> {
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Retrieved Detection events from databases.")
        private List<AbstractDetectionEventDTO> content;
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
        private Pagination pagination;
    }

    /**
     * The type Cheating Detection rest resource.
     */
    @ApiModel(value = "CheatingDetectionRestResource",
            description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
    public static class CheatingDetectionRestResource extends PageResultResource<CheatingDetectionDTO> {
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Retrieved Cheating detections from databases.")
        private List<CheatingDetectionDTO> content;
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
        private Pagination pagination;
    }

    /**
     * The type Detection event participant rest resource.
     */
    @ApiModel(value = "DetectionEventParticipantRestResource",
            description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
    public static class DetectionEventParticipantRestResource extends PageResultResource<DetectionEventParticipantDTO> {
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Retrieved Event participants from databases.")
        private List<DetectionEventParticipantDTO> content;
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
        private Pagination pagination;
    }
}
